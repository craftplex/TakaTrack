package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Expense(
    @DocumentId val id: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val title: String = "",
    val categoryId: String = "other",
    val categoryName: String = "Other",
    val shopName: String = "",
    val paymentMethod: String = "Cash",
    val expenseDate: String = "", // YYYY-MM-DD
    val expenseTime: String = "", // HH:mm
    val notes: String = "",
    val tags: List<String> = emptyList(),
    val isSplit: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
