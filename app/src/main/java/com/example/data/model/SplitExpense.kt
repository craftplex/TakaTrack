package com.example.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class SplitParticipant(
    val name: String = "",
    val amount: Double = 0.0,
    val avatarInitial: String = ""
)

enum class SplitType(val labelEn: String, val labelBn: String) {
    EQUAL("Equal Split", "সমান ভাগ"),
    CUSTOM("Custom Split", "কাস্টম ভাগ")
}
