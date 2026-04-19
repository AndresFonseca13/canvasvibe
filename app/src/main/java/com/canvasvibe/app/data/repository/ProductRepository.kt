package com.canvasvibe.app.data.repository

import com.canvasvibe.app.data.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ProductRepository {

    private val db = FirebaseFirestore.getInstance()
    private val productsCollection = db.collection("products")

    // Obtener todos los productos en tiempo real
    fun getProducts(): Flow<List<Product>> {
        return productsCollection
            .whereEqualTo("isActive", true)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Product::class.java)
            }
    }

    // Obtener Productos por categoria en tiempo real
    fun getProductsByCategory(category: String): Flow<List<Product>> {
        return productsCollection
            .whereEqualTo("category", category)
            .whereEqualTo("isActive", true)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Product::class.java)
            }
    }

    // Obtener un producto por su ID
    suspend fun getProductById(productId: String): Result<Product> {
        return try {
            val snapshot = productsCollection.document(productId).get().await()
            val product = snapshot.toObject(Product::class.java)
            if (product != null) {
                Result.success(product)
            } else {
                Result.failure(Exception("Producto no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Agregar Producto
    suspend fun addProduct(product: Product): Result<String> {
        return try {
            val docRef = productsCollection.document()
            val newProduct = product.copy(id = docRef.id)
            docRef.set(newProduct).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Actualizar un producto
    suspend fun updateProduct(product: Product): Result<Unit> {
        return try {
            productsCollection.document(product.id)
                .set(product)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Eliminar un producto (Borrado Lógico)
    suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            productsCollection.document(productId)
                .update("isActive", false)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
