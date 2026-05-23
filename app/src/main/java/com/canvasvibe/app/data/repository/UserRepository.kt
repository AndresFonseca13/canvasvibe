package com.canvasvibe.app.data.repository

import android.util.Log
import com.canvasvibe.app.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val col = db.collection("users")

    suspend fun getById(uid: String): Result<User> {
        return try {
            val doc = col.document(uid).get().await()
            val user = doc.toObject(User::class.java)
            if (user != null) Result.success(user.copy(uid = uid))
            else Result.failure(Exception("Usuario no encontrado"))
        } catch (e: Exception) {
            Log.e(TAG, "getById($uid) failed", e)
            Result.failure(e)
        }
    }

    suspend fun updateProfile(uid: String, name: String, role: String): Result<Unit> {
        return try {
            col.document(uid).update(
                mapOf(
                    "name" to name,
                    "role" to role
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "updateProfile($uid) failed", e)
            Result.failure(e)
        }
    }

    suspend fun softDelete(uid: String): Result<Unit> {
        return try {
            col.document(uid).update(
                mapOf(
                    "isActive" to false,
                    "deletedAt" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "softDelete($uid) failed", e)
            Result.failure(e)
        }
    }

    suspend fun restore(uid: String): Result<Unit> {
        return try {
            col.document(uid).update(
                mapOf(
                    "isActive" to true,
                    "deletedAt" to null
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "restore($uid) failed", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "UserRepository"
    }
}
