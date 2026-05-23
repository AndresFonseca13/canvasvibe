package com.canvasvibe.app.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

data class GeoAddress(
    val latitude: Double,
    val longitude: Double,
    val street: String,
    val city: String
)

object LocationHelper {

    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentAddress(context: Context): Result<GeoAddress> {
        return runCatching {
            val client = LocationServices.getFusedLocationProviderClient(context)
            val location: Location = suspendCancellableCoroutine<Location?> { cont ->
                client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { loc -> cont.resume(loc) }
                    .addOnFailureListener { cont.resume(null) }
            } ?: throw IllegalStateException("No se pudo obtener la ubicación")

            val (street, city) = reverseGeocode(context, location.latitude, location.longitude)
            GeoAddress(
                latitude = location.latitude,
                longitude = location.longitude,
                street = street,
                city = city
            )
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun reverseGeocode(
        context: Context,
        lat: Double,
        lng: Double
    ): Pair<String, String> {
        if (!Geocoder.isPresent()) return "" to ""
        val geocoder = Geocoder(context, Locale.forLanguageTag("es-CO"))
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { cont ->
                geocoder.getFromLocation(lat, lng, 1) { addresses ->
                    val a = addresses.firstOrNull()
                    val street = listOfNotNull(a?.thoroughfare, a?.subThoroughfare)
                        .filter { it.isNotBlank() }
                        .joinToString(" ")
                        .ifBlank { a?.getAddressLine(0).orEmpty() }
                    val city = a?.locality
                        ?: a?.subAdminArea
                        ?: a?.adminArea
                        ?: ""
                    cont.resume(street to city)
                }
            }
        } else {
            val list = geocoder.getFromLocation(lat, lng, 1).orEmpty()
            val a = list.firstOrNull()
            val street = listOfNotNull(a?.thoroughfare, a?.subThoroughfare)
                .filter { it.isNotBlank() }
                .joinToString(" ")
                .ifBlank { a?.getAddressLine(0).orEmpty() }
            val city = a?.locality
                ?: a?.subAdminArea
                ?: a?.adminArea
                ?: ""
            street to city
        }
    }
}
