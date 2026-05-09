package com.canvasvibe.app.ui.admin.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar

enum class ReportPeriod { HOY, SEMANA, MES, ANIO }

fun ReportPeriod.label(): String = when (this) {
    ReportPeriod.HOY    -> "Hoy"
    ReportPeriod.SEMANA -> "Semana"
    ReportPeriod.MES    -> "Mes"
    ReportPeriod.ANIO   -> "Año"
}

private fun ReportPeriod.previousLabel(): String = when (this) {
    ReportPeriod.HOY    -> "ayer"
    ReportPeriod.SEMANA -> "semana anterior"
    ReportPeriod.MES    -> "mes anterior"
    ReportPeriod.ANIO   -> "año anterior"
}

data class ChartPoint(val label: String, val valueCop: Long)

data class TopProduct(
    val productId: String,
    val title: String,
    val units: Int,
    val revenueCop: Long
)

data class TopArtist(
    val sellerId: String,
    val name: String,
    val revenueCop: Long,
    val rating: Double
)

data class AdminReportsUiState(
    val isLoading: Boolean = true,
    val period: ReportPeriod = ReportPeriod.SEMANA,
    val totalCop: Long = 0L,
    val variationPct: Double = 0.0,
    val previousAvailable: Boolean = false,
    val previousLabel: String = "",
    val chartPoints: List<ChartPoint> = emptyList(),
    val topProducts: List<TopProduct> = emptyList(),
    val topArtists: List<TopArtist> = emptyList(),
    val errorMessage: String? = null
)

private data class OrderLite(
    val id: String,
    val createdAt: Long,
    val totalPrice: Long,
    val quantity: Int,
    val productId: String,
    val productTitle: String,
    val sellerId: String,
    val sellerName: String,
    val status: String
)

class AdminReportsViewModel : ViewModel() {

    private val _state = MutableStateFlow(AdminReportsUiState())
    val state: StateFlow<AdminReportsUiState> = _state.asStateFlow()

    init { setPeriod(ReportPeriod.SEMANA) }

    fun setPeriod(period: ReportPeriod) {
        _state.value = _state.value.copy(
            isLoading = true,
            period = period,
            previousLabel = period.previousLabel(),
            errorMessage = null
        )
        viewModelScope.launch { reload(period) }
    }

    fun dismissError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    private suspend fun reload(period: ReportPeriod) {
        runCatching {
            val db = FirebaseFirestore.getInstance()
            val ordersSnap = db.collection("orders").get().await()
            val productsSnap = db.collection("products").get().await()

            val now = System.currentTimeMillis()
            val start = periodStartMillis(period, now)
            val previousStart = previousPeriodStart(period, start)

            val allOrders = ordersSnap.documents
                .map { it.toOrderLite() }
                .filter { it.status != "CANCELLED" }

            val periodOrders  = allOrders.filter { it.createdAt in start..now }
            val previousOrders = allOrders.filter { it.createdAt in previousStart until start }

            val total    = periodOrders.sumOf { it.totalPrice }
            val previous = previousOrders.sumOf { it.totalPrice }
            val variation = if (previous == 0L) 0.0
                           else ((total - previous).toDouble() / previous) * 100

            val chartPoints = buildChartPoints(periodOrders, period, start, now)
            val topProducts = topProducts(periodOrders)
            val topArtists  = topArtists(periodOrders, productsSnap.documents)

            _state.value = AdminReportsUiState(
                isLoading = false,
                period = period,
                totalCop = total,
                variationPct = variation,
                previousAvailable = previous > 0L,
                previousLabel = period.previousLabel(),
                chartPoints = chartPoints,
                topProducts = topProducts,
                topArtists = topArtists
            )
        }.onFailure { e ->
            _state.value = _state.value.copy(
                isLoading = false,
                errorMessage = friendly(e)
            )
        }
    }

    private fun DocumentSnapshot.toOrderLite() = OrderLite(
        id = id,
        createdAt = getLong("createdAt") ?: 0L,
        totalPrice = getLong("totalPrice") ?: 0L,
        quantity = (getLong("quantity") ?: 0L).toInt(),
        productId = getString("productId").orEmpty(),
        productTitle = getString("productTitle").orEmpty(),
        sellerId = getString("sellerId").orEmpty(),
        sellerName = getString("sellerName").orEmpty(),
        status = getString("status").orEmpty()
    )

    private fun periodStartMillis(period: ReportPeriod, now: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return when (period) {
            ReportPeriod.HOY    -> cal.timeInMillis
            ReportPeriod.SEMANA -> cal.apply { add(Calendar.DAY_OF_YEAR, -6) }.timeInMillis
            ReportPeriod.MES    -> cal.apply { set(Calendar.DAY_OF_MONTH, 1) }.timeInMillis
            ReportPeriod.ANIO   -> cal.apply {
                set(Calendar.DAY_OF_YEAR, 1)
            }.timeInMillis
        }
    }

    private fun previousPeriodStart(period: ReportPeriod, start: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = start }
        return when (period) {
            ReportPeriod.HOY    -> cal.apply { add(Calendar.DAY_OF_YEAR, -1) }.timeInMillis
            ReportPeriod.SEMANA -> cal.apply { add(Calendar.DAY_OF_YEAR, -7) }.timeInMillis
            ReportPeriod.MES    -> cal.apply { add(Calendar.MONTH, -1) }.timeInMillis
            ReportPeriod.ANIO   -> cal.apply { add(Calendar.YEAR, -1) }.timeInMillis
        }
    }

    private fun buildChartPoints(
        orders: List<OrderLite>,
        period: ReportPeriod,
        start: Long,
        now: Long
    ): List<ChartPoint> = when (period) {
        ReportPeriod.HOY    -> hourlyBuckets(orders, start)
        ReportPeriod.SEMANA -> dailyBuckets(orders, start, days = 7)
        ReportPeriod.MES    -> weeklyBuckets(orders, start, now)
        ReportPeriod.ANIO   -> monthlyBuckets(orders, start)
    }

    private fun hourlyBuckets(orders: List<OrderLite>, start: Long): List<ChartPoint> {
        val buckets = LongArray(6)
        orders.forEach {
            val cal = Calendar.getInstance().apply { timeInMillis = it.createdAt }
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val bucket = (hour / 4).coerceIn(0, 5)
            buckets[bucket] += it.totalPrice
        }
        val labels = listOf("0-4h", "4-8h", "8-12h", "12-16h", "16-20h", "20-24h")
        return buckets.mapIndexed { i, v -> ChartPoint(labels[i], v) }
    }

    private fun dailyBuckets(orders: List<OrderLite>, start: Long, days: Int): List<ChartPoint> {
        val buckets = LongArray(days)
        val labels = MutableList(days) { "" }
        val dayLabels = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
        for (i in 0 until days) {
            val cal = Calendar.getInstance().apply {
                timeInMillis = start
                add(Calendar.DAY_OF_YEAR, i)
            }
            val dow = cal.get(Calendar.DAY_OF_WEEK) // SUN=1..SAT=7
            val ix = ((dow + 5) % 7) // map to MON=0..SUN=6
            labels[i] = dayLabels[ix]
        }
        orders.forEach {
            val daysSinceStart = ((it.createdAt - start) / (1000L * 60 * 60 * 24)).toInt()
            if (daysSinceStart in 0 until days) buckets[daysSinceStart] += it.totalPrice
        }
        return buckets.mapIndexed { i, v -> ChartPoint(labels[i], v) }
    }

    private fun weeklyBuckets(orders: List<OrderLite>, start: Long, now: Long): List<ChartPoint> {
        val totalDays = ((now - start) / (1000L * 60 * 60 * 24)).toInt() + 1
        val weeks = ((totalDays + 6) / 7).coerceAtLeast(1).coerceAtMost(5)
        val buckets = LongArray(weeks)
        orders.forEach {
            val day = ((it.createdAt - start) / (1000L * 60 * 60 * 24)).toInt()
            val w = (day / 7).coerceIn(0, weeks - 1)
            buckets[w] += it.totalPrice
        }
        return buckets.mapIndexed { i, v -> ChartPoint("Sem ${i + 1}", v) }
    }

    private fun monthlyBuckets(orders: List<OrderLite>, start: Long): List<ChartPoint> {
        val buckets = LongArray(12)
        val startCal = Calendar.getInstance().apply { timeInMillis = start }
        val startYear = startCal.get(Calendar.YEAR)
        orders.forEach {
            val cal = Calendar.getInstance().apply { timeInMillis = it.createdAt }
            if (cal.get(Calendar.YEAR) == startYear) {
                val m = cal.get(Calendar.MONTH).coerceIn(0, 11)
                buckets[m] += it.totalPrice
            }
        }
        val labels = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun",
                            "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
        return buckets.mapIndexed { i, v -> ChartPoint(labels[i], v) }
    }

    private fun topProducts(orders: List<OrderLite>): List<TopProduct> {
        return orders.groupBy { it.productId }
            .filterKeys { it.isNotBlank() }
            .map { (id, list) ->
                TopProduct(
                    productId = id,
                    title = list.firstOrNull { it.productTitle.isNotBlank() }
                        ?.productTitle ?: "Producto",
                    units = list.sumOf { it.quantity },
                    revenueCop = list.sumOf { it.totalPrice }
                )
            }
            .sortedByDescending { it.revenueCop }
            .take(3)
    }

    private fun topArtists(
        orders: List<OrderLite>,
        productDocs: List<DocumentSnapshot>
    ): List<TopArtist> {
        val ratingsBySeller = productDocs
            .groupBy { it.getString("sellerId").orEmpty() }
            .mapValues { (_, docs) ->
                val ratings = docs.mapNotNull { it.getDouble("rating") }.filter { it > 0 }
                if (ratings.isEmpty()) 0.0 else ratings.average()
            }
        return orders.groupBy { it.sellerId }
            .filterKeys { it.isNotBlank() }
            .map { (id, list) ->
                TopArtist(
                    sellerId = id,
                    name = list.firstOrNull { it.sellerName.isNotBlank() }
                        ?.sellerName ?: "Artista",
                    revenueCop = list.sumOf { it.totalPrice },
                    rating = ratingsBySeller[id] ?: 0.0
                )
            }
            .sortedByDescending { it.revenueCop }
            .take(3)
    }

    private fun friendly(e: Throwable): String {
        val raw = e.message.orEmpty()
        return when {
            raw.contains("PERMISSION_DENIED", true) ->
                "Firestore rechazó la operación. Revisa las reglas."
            raw.contains("UNAUTHENTICATED", true) ->
                "No estás autenticado. Inicia sesión nuevamente."
            raw.isBlank() -> "Error desconocido"
            else -> raw
        }
    }
}
