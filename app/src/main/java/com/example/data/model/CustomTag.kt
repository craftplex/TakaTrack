package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class CustomTag(
    val id: String = "",
    val name: String = "",
    val colorHex: String = "#2589D8",
    val isDefault: Boolean = false
) {
    fun toColor(): Color {
        return try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (e: Exception) {
            Color(0xFF2589D8)
        }
    }
}

object DefaultTags {
    val list: List<CustomTag> = listOf(
        CustomTag(id = "personal", name = "Personal", colorHex = "#2589D8", isDefault = true),
        CustomTag(id = "family", name = "Family", colorHex = "#16A36A", isDefault = true),
        CustomTag(id = "office", name = "Office", colorHex = "#176FB3", isDefault = true),
        CustomTag(id = "travel", name = "Travel", colorHex = "#E6A23C", isDefault = true),
        CustomTag(id = "emergency", name = "Emergency", colorHex = "#E05252", isDefault = true)
    )
}
