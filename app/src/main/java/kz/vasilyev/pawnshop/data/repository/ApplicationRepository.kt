package kz.vasilyev.pawnshop.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kz.vasilyev.pawnshop.data.model.Application
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.snapshots

class ApplicationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun saveApplication(application: Application): Result<Nothing?> {
        return try {
            val applicationMap = hashMapOf(
                "userId" to application.userId,
                "photoBase64" to application.photoBase64,
                "category" to application.category,
                "comment" to application.comment,
                "status" to application.status.name,
                "timestamp" to FieldValue.serverTimestamp()
            )
            firestore.collection("applications").add(applicationMap).await()
            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserApplications(userId: String): Flow<List<Application>> {
        return firestore.collection("applications")
            .whereEqualTo("userId", userId)
            .orderBy("status", Query.Direction.ASCENDING)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .snapshots()
            .map { querySnapshot ->
                querySnapshot.documents.mapNotNull { document ->
                    val app = document.toObject(Application::class.java)
                    app?.copy(id = document.id)
                }
            }
    }

    fun getAllApplications(): Flow<List<Application>> {
        return firestore.collection("applications")
            .orderBy("status", Query.Direction.ASCENDING)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .snapshots()
            .map { querySnapshot ->
                querySnapshot.documents.mapNotNull { document ->
                    val app = document.toObject(Application::class.java)
                    app?.copy(id = document.id)
                }
            }
    }

    fun getApplicationById(applicationId: String): Flow<Application?> {
        return firestore.collection("applications").document(applicationId)
            .snapshots()
            .map { documentSnapshot ->
                documentSnapshot.toObject(Application::class.java)?.copy(id = documentSnapshot.id)
            }
    }

    suspend fun updateApplication(application: Application): Result<Nothing?> {
        return try {
            application.id?.let { id ->
                val applicationMap = application.toMap()
                firestore.collection("applications").document(id).update(applicationMap).await()
                Result.success(null)
            } ?: Result.failure(IllegalArgumentException("Application ID cannot be null for update"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 