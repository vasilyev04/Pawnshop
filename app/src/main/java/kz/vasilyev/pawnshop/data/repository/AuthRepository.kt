package kz.vasilyev.pawnshop.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kz.vasilyev.pawnshop.data.model.User

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun registerUser(email: String, password: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                val user = User(uuid = firebaseUser.uid, email = email, admin = false)
                firestore.collection("users").document(user.uuid).set(user).await()
                Result.success(user)
            } else {
                Result.failure(Exception("User creation failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<User> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                val userDocument = firestore.collection("users").document(firebaseUser.uid).get().await()
                val user = userDocument.toObject(User::class.java)
                if (user != null) {
                    Result.success(user)
                } else {
                    // User exists in Auth but not in Firestore - should not happen if registration worked
                    Result.failure(Exception("User data not found in Firestore"))
                }
            } else {
                Result.failure(Exception("Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser
        return try {
            if (firebaseUser != null) {
                val userDocument = firestore.collection("users").document(firebaseUser.uid).get().await()
                userDocument.toObject(User::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
} 