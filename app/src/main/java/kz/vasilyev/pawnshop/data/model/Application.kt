package kz.vasilyev.pawnshop.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.Timestamp
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Application(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val photoBase64: List<String>? = null,
    val category: String = "",
    val comment: String = "",
    val timestamp: Timestamp? = null,
    val status: ApplicationStatus = ApplicationStatus.UNDER_REVIEW,
    val price: Double? = null,
    val adminComment: String? = null,
    val userFio: String? = null,
    val userPhone: String? = null,
    val userAddress: String? = null,
    val userPaymentMethod: String? = null
) : Parcelable {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "photoBase64" to photoBase64,
            "category" to category,
            "comment" to comment,
            "timestamp" to timestamp,
            "status" to status.name, // Convert enum to String for Firestore
            "price" to price,
            "adminComment" to adminComment,
            "userFio" to userFio, // Добавляем в toMap
            "userPhone" to userPhone, // Добавляем в toMap
            "userAddress" to userAddress, // Добавляем в toMap
            "userPaymentMethod" to userPaymentMethod // Добавляем в toMap
        )
    }
}

enum class ApplicationStatus {
    UNDER_REVIEW, // в рассмотрении
    AWAITING_CONFIRMATION, // ожидает подтверждения
    APPROVED, // одобрена
    REJECTED // отклонена
} 