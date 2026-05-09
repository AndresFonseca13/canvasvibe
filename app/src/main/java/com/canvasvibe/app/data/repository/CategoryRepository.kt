package com.canvasvibe.app.data.repository

import android.util.Log
import com.canvasvibe.app.data.model.Category
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class CategoryRepository {

    private val db = FirebaseFirestore.getInstance()
    private val col = db.collection("categories")

    fun observeAll(): Flow<List<Category>> {
        return col.orderBy("order", Query.Direction.ASCENDING)
            .snapshots()
            .map { snap -> snap.toObjects(Category::class.java) }
    }

    fun observeActive(): Flow<List<Category>> {
        return col.whereEqualTo("isActive", true)
            .orderBy("order", Query.Direction.ASCENDING)
            .snapshots()
            .map { snap -> snap.toObjects(Category::class.java) }
    }

    suspend fun seedDefaultsIfEmpty(): Result<Int> {
        return try {
            val current = col.limit(1).get().await()
            if (!current.isEmpty) {
                Log.d(TAG, "seed skipped: collection already has ${current.size()} doc(s)")
                return Result.success(0)
            }
            seedDefaults()
        } catch (e: Exception) {
            Log.e(TAG, "seedDefaultsIfEmpty failed", e)
            Result.failure(e)
        }
    }

    suspend fun seedDefaults(): Result<Int> {
        return try {
            DEFAULT_CATEGORIES.forEachIndexed { ix, item ->
                val seed = item.copy(
                    id = item.slug,
                    order = ix,
                    createdAt = System.currentTimeMillis()
                )
                col.document(item.slug).set(seed).await()
                Log.d(TAG, "seeded ${item.slug}")
            }
            Result.success(DEFAULT_CATEGORIES.size)
        } catch (e: Exception) {
            Log.e(TAG, "seedDefaults failed", e)
            Result.failure(e)
        }
    }

    suspend fun setActive(id: String, active: Boolean): Result<Unit> {
        return try {
            col.document(id).update("isActive", active).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "setActive($id) failed", e)
            Result.failure(e)
        }
    }

    suspend fun add(category: Category): Result<String> {
        return try {
            val docId = category.slug.ifBlank {
                category.name.lowercase().replace(Regex("\\s+"), "-")
            }.ifBlank { col.document().id }
            val finalCategory = category.copy(id = docId, slug = docId)
            col.document(docId).set(finalCategory).await()
            Log.d(TAG, "add(${finalCategory.slug}) ok")
            Result.success(docId)
        } catch (e: Exception) {
            Log.e(TAG, "add(${category.name}) failed", e)
            Result.failure(e)
        }
    }

    suspend fun delete(id: String): Result<Unit> {
        return try {
            col.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "delete($id) failed", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "CategoryRepo"

        val DEFAULT_CATEGORIES = listOf(
            Category(slug = "gamer",     name = "Gamer",     emoji = "🎮", colorHex = "#7C4DFF"),
            Category(slug = "paisajes",  name = "Paisajes",  emoji = "🌿", colorHex = "#4CAF50"),
            Category(slug = "animales",  name = "Animales",  emoji = "🐾", colorHex = "#FF9800"),
            Category(slug = "anime",     name = "Anime",     emoji = "🎌", colorHex = "#F44336"),
            Category(slug = "abstracto", name = "Abstracto", emoji = "🖥",  colorHex = "#2196F3")
        )
    }
}
