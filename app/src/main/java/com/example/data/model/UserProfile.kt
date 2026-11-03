package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val monthlyLimit: Double = 20000.0,
    val currency: String = "BDT",
    val language: String = "bn",
    val categoryBudgets: Map<String, Double> = emptyMap(),
    val customTags: List<String> = listOf("Personal", "Family", "Office", "Travel", "Emergency"),
    val budgetWarning50: Boolean = true,
    val budgetWarning75: Boolean = true,
    val budgetWarning90: Boolean = true,
    val budgetWarning100: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
