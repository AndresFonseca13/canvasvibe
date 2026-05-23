package com.canvasvibe.app.ui.buyer.checkout

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canvasvibe.app.data.model.CartItem
import com.canvasvibe.app.data.model.Order
import com.canvasvibe.app.data.repository.CartRepository
import com.canvasvibe.app.data.repository.OrderRepository
import com.canvasvibe.app.payments.EpaycoBus
import com.canvasvibe.app.payments.EpaycoResult
import com.canvasvibe.app.util.LocationHelper
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

data class CheckoutForm(
    val fullName: String = "",
    val phone: String = "",
    val address: String = "",
    val city: String = "",
    val notes: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isLocating: Boolean = false,
    val locationMessage: String? = null
)

sealed interface CheckoutState {
    data class Form(val form: CheckoutForm = CheckoutForm()) : CheckoutState
    data class AwaitingPayment(val form: CheckoutForm, val amount: Long, val invoice: String) : CheckoutState
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

    private var lastForm: CheckoutForm = CheckoutForm()

    init {
        viewModelScope.launch {
            EpaycoBus.events.collect { result ->
                if (_state.value is CheckoutState.AwaitingPayment) {
                    confirmPayment(result)
                }
            }
        }
    }

    fun updateForm(transform: (CheckoutForm) -> CheckoutForm) {
        val current = _state.value
        val updated = when (current) {
            is CheckoutState.Form           -> transform(current.form)
            is CheckoutState.AwaitingPayment -> transform(current.form)
            is CheckoutState.Error           -> transform(lastForm)
            else                             -> transform(lastForm)
        }
        lastForm = updated
        _state.value = CheckoutState.Form(updated)
    }

    fun lastForm(): CheckoutForm {
        val s = _state.value
        return when (s) {
            is CheckoutState.Form            -> s.form
            is CheckoutState.AwaitingPayment -> s.form
            else                              -> lastForm
        }
    }

    fun setFullName(v: String)  = updateForm { it.copy(fullName = v) }
    fun setPhone(v: String)     = updateForm { it.copy(phone = v.filter { c -> c.isDigit() }) }
    fun setAddress(v: String)   = updateForm { it.copy(address = v, locationMessage = null) }
    fun setCity(v: String)      = updateForm { it.copy(city = v) }
    fun setNotes(v: String)     = updateForm { it.copy(notes = v) }
    fun dismissLocationMessage() = updateForm { it.copy(locationMessage = null) }

    fun fetchCurrentLocation(context: Context) {
        val current = _state.value as? CheckoutState.Form ?: return
        if (current.form.isLocating) return
        updateForm { it.copy(isLocating = true, locationMessage = null) }
        viewModelScope.launch {
            LocationHelper.fetchCurrentAddress(context).fold(
                onSuccess = { geo ->
                    updateForm {
                        it.copy(
                            isLocating = false,
                            address = geo.street.ifBlank { it.address },
                            city = geo.city.ifBlank { it.city },
                            latitude = geo.latitude,
                            longitude = geo.longitude,
                            locationMessage = "Ubicación capturada con GPS"
                        )
                    }
                },
                onFailure = {
                    updateForm {
                        it.copy(
                            isLocating = false,
                            locationMessage = "No se pudo obtener tu ubicación. Activa el GPS e intenta nuevamente."
                        )
                    }
                }
            )
        }
    }
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

        val total = list.sumOf { it.unitPrice * it.quantity } + COMMISSION_COP
        val invoice = "CV-" + System.currentTimeMillis()
        _state.value = CheckoutState.AwaitingPayment(form = form, amount = total, invoice = invoice)
    }

    fun cancelPayment() {
        val awaiting = _state.value as? CheckoutState.AwaitingPayment
        _state.value = if (awaiting != null) CheckoutState.Form(awaiting.form)
        else CheckoutState.Form()
    }

    fun confirmPayment(result: EpaycoResult) {
        val u = uid ?: run {
            _state.value = CheckoutState.Error("Sesión inválida. Inicia sesión nuevamente.")
            return
        }
        val awaiting = _state.value as? CheckoutState.AwaitingPayment ?: return
        val list = items.value
        if (list.isEmpty()) {
            _state.value = CheckoutState.Error("El carrito quedó vacío durante el pago")
            return
        }
        if (!result.approved) {
            val msg = result.responseReason.ifBlank { "Pago no aprobado por ePayco" }
            _state.value = CheckoutState.Error("$msg (ref: ${result.refPayco})")
            return
        }
        val form = awaiting.form
        viewModelScope.launch {
            _state.value = CheckoutState.Processing
            try {
                val first = list.first()
                val sellerId = lookupSellerId(first.productId)
                val total = awaiting.amount
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
                    totalPrice = total,
                    shippingAddress = form.address,
                    shippingCity = form.city,
                    shippingPhone = form.phone,
                    shippingNotes = form.notes,
                    shippingLatitude = form.latitude,
                    shippingLongitude = form.longitude,
                    paymentMethod = "ePayco",
                    paymentRef = result.refPayco,
                    paymentStatus = result.status
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
