package com.canvasvibe.app.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class StorageRepository {
    private val storage = FirebaseStorage.getInstance()

    suspend fun uploadProductImage(
        sellerId: String,
        productId: String,
        imageUri: Uri,
        index: Int
    ): Result<String> {
        return try {
            val ref = storage.reference.child("products/$sellerId/$productId/imagen_$index.jpg")
            ref.putFile(imageUri).await()
            val url = ref.downloadUrl.await().toString()
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
