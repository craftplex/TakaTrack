package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Spa
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class ExpenseCategory(
    val id: String,
    val nameEn: String,
    val nameBn: String,
    val icon: ImageVector,
    val color: Color
)

object CategoryConstants {
    val Categories: List<ExpenseCategory> = listOf(
        ExpenseCategory(
            id = "grocery",
            nameEn = "Grocery",
            nameBn = "মুদি ও বাজার",
            icon = Icons.Default.ShoppingCart,
            color = Color(0xFF10B981)
        ),
        ExpenseCategory(
            id = "food",
            nameEn = "Food & Dining",
            nameBn = "খাবার",
            icon = Icons.Default.Fastfood,
            color = Color(0xFFF59E0B)
        ),
        ExpenseCategory(
            id = "transport",
            nameEn = "Transport",
            nameBn = "যাতায়াত",
            icon = Icons.Default.DirectionsCar,
            color = Color(0xFF3B82F6)
        ),
        ExpenseCategory(
            id = "shopping",
            nameEn = "Shopping",
            nameBn = "কেনাকাটা",
            icon = Icons.Default.CardGiftcard,
            color = Color(0xFFEC4899)
        ),
        ExpenseCategory(
            id = "bills",
            nameEn = "Bills & Utilities",
            nameBn = "বিল ও ইউটিলিটি",
            icon = Icons.Default.Receipt,
            color = Color(0xFF8B5CF6)
        ),
        ExpenseCategory(
            id = "healthcare",
            nameEn = "Health & Medicine",
            nameBn = "স্বাস্থ্য ও ওষুধ",
            icon = Icons.Default.LocalHospital,
            color = Color(0xFFEF4444)
        ),
        ExpenseCategory(
            id = "entertainment",
            nameEn = "Entertainment",
            nameBn = "বিনোদন",
            icon = Icons.Default.Movie,
            color = Color(0xFF06B6D4)
        ),
        ExpenseCategory(
            id = "personal_care",
            nameEn = "Personal Care",
            nameBn = "ব্যক্তিগত যত্ন",
            icon = Icons.Default.Spa,
            color = Color(0xFF14B8A6)
        ),
        ExpenseCategory(
            id = "education",
            nameEn = "Education",
            nameBn = "শিক্ষা",
            icon = Icons.Default.School,
            color = Color(0xFF6366F1)
        ),
        ExpenseCategory(
            id = "other",
            nameEn = "Other",
            nameBn = "অন্যান্য",
            icon = Icons.Default.Category,
            color = Color(0xFF64748B)
        )
    )

    fun getCategoryById(id: String): ExpenseCategory {
        return Categories.find { it.id.equals(id, ignoreCase = true) } ?: Categories.last()
    }
}
