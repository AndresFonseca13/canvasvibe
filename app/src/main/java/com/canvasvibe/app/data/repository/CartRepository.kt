package com.canvasvibe.app.data.repository

import com.canvasvibe.app.data.model.CartItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class CartRepository {

    private val db = FirebaseFirestore.getInstance()

    // Referencia a la subcolección de carrito del usuario
    private fun getCartCollection(uid: String) = 
        db.collection("users").document(uid).collection("cart")

    // Obtener el carrito en tiempo real
    fun getCart(uid: String): Flow<List<CartItem>> {
        return getCartCollection(uid)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(CartItem::class.java)
            }
    }

    // Agregar al carrito (Si ya existe el producto, se puede actualizar la cantidad)
    suspend fun addToCart(uid: String, item: CartItem): Result<Unit> {
        return try {
            // Usamos el productId como ID del documento para evitar duplicados
            getCartCollection(uid).document(item.productId)
                .set(item)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Eliminar un producto del carrito
    suspend fun removeFromCart(uid: String, productId: String): Result<Unit> {
        return try {
            getCartCollection(uid).document(productId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Actualizar cantidad de un producto
    suspend fun updateQuantity(uid: String, productId: String, qty: Int): Result<Unit> {
        return try {
            getCartCollection(uid).document(productId)
                .update("quantity", qty)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Limpiar todo el carrito (después de una compra)
    suspend fun clearCart(uid: String): Result<Unit> {
        return try {
            val snapshot = getCartCollection(uid).get().await()
            val batch = db.batch()
            
            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
            }
            
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
