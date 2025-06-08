package kz.vasilyev.pawnshop.data.model

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    val uuid: String = "",
    val email: String = "",
    val admin: Boolean = false
) 