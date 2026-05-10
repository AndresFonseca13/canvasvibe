package com.canvasvibe.app.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FavoriteRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun favoritesCol(uid: String) =
        db.collection("users").document(uid).collection("favorites")

    fun observeIsFavorite(uid: String, productId: String): Flow<Boolean> {
        if (uid.isBlank() || productId.isBlank()) return flowOf(false)
        return favoritesCol(uid).document(productId).snapshots()
            .map { it.exists() }
    }

    fun observeAll(uid: String): Flow<List<String>> {
        if (uid.isBlank()) return flowOf(emptyList())
        return favoritesCol(uid).snapshots()
            .map { snap -> snap.documents.map { it.id } }
    }

    suspend fun toggle(uid: String, productId: String): Result<Boolean> {
        if (uid.isBlank() || productId.isBlank())
            return Result.failure(IllegalArgumentException("Usuario o producto inválido"))
        return try {
            val docRef = favoritesCol(uid).document(productId)
            val snap = docRef.get().await()
            if (snap.exists()) {
                docRef.delete().await()
                Result.success(false)
            } else {
                docRef.set(mapOf(
                    "productId" to productId,
                    "createdAt" to System.currentTimeMillis()
                )).await()
                Result.success(true)
            }
        } catch (e: Exception) {
            Log.e("FavoriteRepo", "toggle($productId) failed", e)
            Result.failure(e)
        }
    }
}
