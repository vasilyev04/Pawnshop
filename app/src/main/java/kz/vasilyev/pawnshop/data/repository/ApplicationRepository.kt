package kz.vasilyev.pawnshop.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kz.vasilyev.pawnshop.data.model.Application
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.snapshots
import android.util.Log

class ApplicationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    init {
        Log.d("ApplicationRepository", "Initializing ApplicationRepository")
        firestore.collection("applications")
            .get()
            .addOnSuccessListener { documents ->
                Log.d("ApplicationRepository", "Firestore connection test successful. Found ${documents.size()} documents")
                documents.forEach { doc ->
                    Log.d("ApplicationRepository", "Document ID: ${doc.id}, Data: ${doc.data}")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ApplicationRepository", "Firestore connection test failed", e)
            }
    }

    suspend fun saveApplication(application: Application): Result<Nothing?> {
        Log.d("ApplicationRepository", "Starting saveApplication: $application")
        return try {
            val applicationMap = hashMapOf(
                "userId" to application.userId,
                "photoBase64" to application.photoBase64,
                "category" to application.category,
                "comment" to application.comment,
                "status" to application.status.name,
                "timestamp" to FieldValue.serverTimestamp()
            )
            Log.d("ApplicationRepository", "Created application map: $applicationMap")

            val docRef = firestore.collection("applications").document()
            Log.d("ApplicationRepository", "Created document reference: ${docRef.id}")

            docRef.set(applicationMap).await()
            Log.d("ApplicationRepository", "Successfully saved application with ID: ${docRef.id}")

            // Проверяем, что документ действительно сохранился
            val savedDoc = docRef.get().await()
            Log.d("ApplicationRepository", "Verified saved document: ${savedDoc.data}")

            Result.success(null)
        } catch (e: Exception) {
            Log.e("ApplicationRepository", "Error saving application", e)
            Result.failure(e)
        }
    }

    fun getUserApplications(userId: String): Flow<List<Application>> {
        Log.d("ApplicationRepository", "Starting getUserApplications for userId: $userId")
        return firestore.collection("applications")
            .whereEqualTo("userId", userId)
            .orderBy("status", Query.Direction.ASCENDING)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                Log.d("ApplicationRepository", "Received snapshot with ${snapshot.documents.size} documents")
                snapshot.documents.mapNotNull { document ->
                    try {
                        Log.d("ApplicationRepository", "Processing document: ${document.id}")
                        val app = document.toObject(Application::class.java)
                        Log.d("ApplicationRepository", "Mapped document to Application: $app")
                        app?.copy(id = document.id)
                    } catch (e: Exception) {
                        Log.e("ApplicationRepository", "Error mapping document ${document.id}: ${e.message}", e)
                        null
                    }
                }
            }
    }

    fun getAllApplications(): Flow<List<Application>> {
        Log.d("ApplicationRepository", "Starting getAllApplications")
        val query = firestore.collection("applications")
            .orderBy("status", Query.Direction.ASCENDING)
            .orderBy("timestamp", Query.Direction.DESCENDING)

        Log.d("ApplicationRepository", "Created query: $query")

        return query.snapshots()
            .map { snapshot ->
                Log.d("ApplicationRepository", "Received snapshot with ${snapshot.documents.size} documents")
                if (snapshot.documents.isEmpty()) {
                    Log.d("ApplicationRepository", "No documents found in collection")
                }
                snapshot.documents.mapNotNull { document ->
                    try {
                        Log.d("ApplicationRepository", "Processing document: ${document.id}")
                        Log.d("ApplicationRepository", "Document data: ${document.data}")
                        val app = document.toObject(Application::class.java)
                        Log.d("ApplicationRepository", "Mapped document to Application: $app")
                        app?.copy(id = document.id)
                    } catch (e: Exception) {
                        Log.e("ApplicationRepository", "Error mapping document ${document.id}: ${e.message}", e)
                        null
                    }
                }
            }
    }

    fun getApplicationById(applicationId: String): Flow<Application?> {
        Log.d("ApplicationRepository", "Starting getApplicationById for id: $applicationId")
        return firestore.collection("applications").document(applicationId)
            .snapshots()
            .map { snapshot ->
                try {
                    Log.d("ApplicationRepository", "Received snapshot for document: ${snapshot.id}")
                    val app = snapshot.toObject(Application::class.java)
                    Log.d("ApplicationRepository", "Mapped document to Application: $app")
                    app?.copy(id = snapshot.id)
                } catch (e: Exception) {
                    Log.e("ApplicationRepository", "Error mapping document ${snapshot.id}: ${e.message}", e)
                    null
                }
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