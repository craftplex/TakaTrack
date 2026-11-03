package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DeepBlue
import com.example.ui.theme.LightBlue
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.WarningOrange

data class CategoryBudgetInfo(
    val category: ExpenseCategory,
    val limitAmount: Double,
    val spentAmount: Double,
    val usagePercent: Float,
    val remainingAmount: Double,
    val isOverBudget: Boolean
) {
    /**
     * Color rules per PDF specification section 10.4:
     * 0-49%: Blue
     * 50-74%: Light Blue (with Deep Blue text)
     * 75-89%: Orange
     * 90-99%: Deep Orange / Warning
     * 100%+: Red
     */
    fun getStatusColor(): Color {
        return when {
            usagePercent >= 100f -> DangerRed
            usagePercent >= 90f -> Color(0xFFE65100) // Deep Orange
            usagePercent >= 75f -> WarningOrange
            usagePercent >= 50f -> DeepBlue
            else -> PrimaryBlue
        }
    }

    fun getStatusLabelBn(): String {
        return when {
            usagePercent >= 100f -> "সীমা অতিক্রম! (${String.format("%.0f", usagePercent)}%)"
            usagePercent >= 90f -> "সতর্কতা (${String.format("%.0f", usagePercent)}%)"
            usagePercent >= 75f -> "৭৫% ব্যবহৃত"
            usagePercent >= 50f -> "৫০% ব্যবহৃত"
            limitAmount <= 0 -> "সীমা নির্ধারণ করা হয়নি"
            else -> "স্বাভাবিক (${String.format("%.0f", usagePercent)}%)"
        }
    }
}
