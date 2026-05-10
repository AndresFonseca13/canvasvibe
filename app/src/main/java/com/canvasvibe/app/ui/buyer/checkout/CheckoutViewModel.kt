package com.canvasvibe.app.ui.buyer.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canvasvibe.app.data.model.CartItem
import com.canvasvibe.app.data.model.Order
import com.canvasvibe.app.data.repository.CartRepository
import com.canvasvibe.app.data.repository.OrderRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class PaymentMethod { PSE, TARJETA, NEQUI }

fun PaymentMethod.label(): String = when (this) {
    PaymentMethod.PSE     -> "PSE"
    PaymentMethod.TARJETA -> "Tarjeta"
    PaymentMethod.NEQUI   -> "Nequi"
}

fun PaymentMethod.subtitle(): String = when (this) {
    PaymentMethod.PSE     -> "Débito desde tu banco"
    PaymentMethod.TARJETA -> "Crédito o débito"
    PaymentMethod.NEQUI   -> "Pago con celular"
}

data class CheckoutForm(
    val fullName: String = "",
    val phone: String = "",
    val address: String = "",
    val city: String = "",
    val notes: String = "",
    val method: PaymentMethod = PaymentMethod.PSE,
    val cardNumber: String = "",
    val cardExpiry: String = "",
    val cardCvv: String = "",
    val pseBank: String = "",
    val nequiPhone: String = ""
)

sealed interface CheckoutState {
    data class Form(val form: CheckoutForm = CheckoutForm()) : CheckoutState
    object Processing : CheckoutState
    data class Success(val orderId: String) : CheckoutState
    data class Error(val message: String) : CheckoutState
}

private const val COMMISSION_COP = 10000L

class CheckoutViewModel(
    private val cartRepo: CartRepository = CartRepository(),
    private val orderRepo: OrderRepository = OrderRepository()
) : ViewModel() {

    private val uid: String? = FirebaseAuth.getInstance().currentUser?.uid
    private val uidFlow = MutableStateFlow(uid)

    @OptIn(ExperimentalCoroutinesApi::class)
    val items: StateFlow<List<CartItem>> = uidFlow
        .flatMapLatest { currentUid ->
            if (currentUid.isNullOrBlank()) flowOf(emptyList())
            else cartRepo.getCart(currentUid)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _state = MutableStateFlow<CheckoutState>(CheckoutState.Form())
    val state: StateFlow<CheckoutState> = _state.asStateFlow()

    val shippingCost: Long = COMMISSION_COP

    fun updateForm(transform: (CheckoutForm) -> CheckoutForm) {
        val current = _state.value
        if (current is CheckoutState.Form) {
            _state.value = CheckoutState.Form(transform(current.form))
        }
    }

    fun setMethod(method: PaymentMethod) = updateForm { it.copy(method = method) }
    fun setFullName(v: String)  = updateForm { it.copy(fullName = v) }
    fun setPhone(v: String)     = updateForm { it.copy(phone = v.filter { c -> c.isDigit() }) }
    fun setAddress(v: String)   = updateForm { it.copy(address = v) }
    fun setCity(v: String)      = updateForm { it.copy(city = v) }
    fun setNotes(v: String)     = updateForm { it.copy(notes = v) }
    fun setCardNumber(v: String) = updateForm { it.copy(cardNumber = v.filter { c -> c.isDigit() }.take(16)) }
    fun setCardExpiry(v: String) = updateForm { it.copy(cardExpiry = v.take(5)) }
    fun setCardCvv(v: String)    = updateForm { it.copy(cardCvv = v.filter { c -> c.isDigit() }.take(4)) }
    fun setPseBank(v: String)    = updateForm { it.copy(pseBank = v) }
    fun setNequiPhone(v: String) = updateForm { it.copy(nequiPhone = v.filter { c -> c.isDigit() }) }

    fun consumeError() {
        val current = _state.value
        if (current is CheckoutState.Error) _state.value = CheckoutState.Form()
    }

    fun pay() {
        val u = uid ?: run {
            _state.value = CheckoutState.Error("Sesión inválida. Inicia sesión nuevamente.")
            return
        }
        val list = items.value
        if (list.isEmpty()) {
            _state.value = CheckoutState.Error("Tu carrito está vacío")
            return
        }
        val current = _state.value as? CheckoutState.Form ?: return
        val form = current.form

        val validationError = validate(form)
        if (validationError != null) {
            _state.value = CheckoutState.Error(validationError)
            return
        }

        viewModelScope.launch {
            _state.value = CheckoutState.Processing
            try {
                val first = list.first()
                val sellerId = lookupSellerId(first.productId)
                val total = list.sumOf { it.unitPrice * it.quantity } + COMMISSION_COP
                val buyerName = lookupBuyerName(u)
                val order = Order(
                    buyerId = u,
                    buyerName = buyerName,
                    sellerId = sellerId,
                    sellerName = first.sellerName,
                    productId = first.productId,
                    productTitle = first.title,
                    productImageUrl = first.imageUrl,
                    material = first.material,
                    size = first.size,
                    quantity = list.sumOf { it.quantity },
                    unitPrice = first.unitPrice,
                    totalPrice = total
                )
                orderRepo.createOrder(order).fold(
                    onSuccess = { orderId ->
                        cartRepo.clearCart(u)
                        _state.value = CheckoutState.Success(orderId)
                    },
                    onFailure = { e ->
                        _state.value = CheckoutState.Error(friendly(e))
                    }
                )
            } catch (e: Exception) {
                _state.value = CheckoutState.Error(friendly(e))
            }
        }
    }

    private suspend fun lookupSellerId(productId: String): String {
        if (productId.isBlank()) return ""
        return runCatching {
            FirebaseFirestore.getInstance()
                .collection("products").document(productId)
                .get().await()
                .getString("sellerId").orEmpty()
        }.getOrDefault("")
    }

    private suspend fun lookupBuyerName(uid: String): String {
        return runCatching {
            FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .get().await()
                .getString("name").orEmpty()
        }.getOrDefault("")
    }

    private fun validate(form: CheckoutForm): String? {
        if (form.fullName.isBlank()) return "Ingresa el nombre completo"
        if (form.phone.length < 7) return "Ingresa un teléfono válido"
        if (form.address.isBlank()) return "Ingresa la dirección de envío"
        if (form.city.isBlank()) return "Ingresa la ciudad"
        return when (form.method) {
            PaymentMethod.TARJETA -> validateCard(form)
            PaymentMethod.PSE     -> if (form.pseBank.isBlank()) "Selecciona tu banco" else null
            PaymentMethod.NEQUI   -> if (form.nequiPhone.length < 10) "Número Nequi inválido" else null
        }
    }

    private fun validateCard(form: CheckoutForm): String? {
        if (form.cardNumber.length < 13) return "Número de tarjeta inválido"
        if (!form.cardExpiry.matches(Regex("\\d{2}/\\d{2}"))) return "Vencimiento inválido (MM/AA)"
        if (form.cardCvv.length < 3) return "CVV inválido"
        return null
    }

    private fun friendly(e: Throwable): String {
        val raw = e.message.orEmpty()
        return when {
            raw.contains("PERMISSION_DENIED", true) ->
                "Firestore rechazó la operación. Revisa las reglas."
            raw.contains("UNAUTHENTICATED", true) -> "Sesión expirada. Inicia sesión nuevamente."
            raw.isBlank() -> "No se pudo procesar el pago"
            else -> raw
        }
    }
}
