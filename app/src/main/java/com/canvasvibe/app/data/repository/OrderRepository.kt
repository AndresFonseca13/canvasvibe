package com.canvasvibe.app.data.repository

import com.canvasvibe.app.data.model.Order
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class OrderRepository {

    private val db = FirebaseFirestore.getInstance()
    private val ordersCollection = db.collection("orders")

    // Crear un nuevo pedido
    suspend fun createOrder(order: Order): Result<String> {
        return try {
            val docRef = ordersCollection.document()
            val newOrder = order.copy(
                id = docRef.id,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            docRef.set(newOrder).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener pedidos por comprador (Buyer) en tiempo real
    fun getOrdersByBuyer(buyerId: String): Flow<List<Order>> {
        // Asumiendo que existe un campo buyerId en el modelo Order
        // Si no existe, se debería añadir al modelo para filtrar correctamente
        return ordersCollection
            .whereEqualTo("buyerId", buyerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Order::class.java)
            }
    }

    // Obtener pedidos por vendedor (Seller) en tiempo real
    fun getOrdersBySeller(sellerId: String): Flow<List<Order>> {
        return ordersCollection
            .whereEqualTo("sellerId", sellerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Order::class.java)
            }
    }

    // Obtener un pedido por su ID
    suspend fun getOrderById(orderId: String): Result<Order> {
        return try {
            val snapshot = ordersCollection.document(orderId).get().await()
            val order = snapshot.toObject(Order::class.java)
            if (order != null) {
                Result.success(order)
            } else {
                Result.failure(Exception("Pedido no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Actualizar el estado de un pedido
    suspend fun updateOrderStatus(orderId: String, status: String): Result<Unit> {
        return try {
            ordersCollection.document(orderId)
                .update(
                    "status", status,
                    "updatedAt", System.currentTimeMillis()
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
