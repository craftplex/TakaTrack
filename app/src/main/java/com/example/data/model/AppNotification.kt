package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.DangerRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningOrange

enum class NotificationType {
    BUDGET_WARNING,
    LIMIT_EXCEEDED,
    SPENDING_INSIGHT,
    WEEKLY_REPORT,
    MONTHLY_REPORT,
    SPLIT_EXPENSE
}

data class AppNotification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val actionRoute: String = "" // "budget", "reports", "expenses", "split"
) {
    fun getIcon(): ImageVector {
        return when (type) {
            NotificationType.LIMIT_EXCEEDED -> Icons.Default.ReportProblem
            NotificationType.BUDGET_WARNING -> Icons.Default.NotificationsActive
            NotificationType.SPENDING_INSIGHT -> Icons.Default.AutoAwesome
            NotificationType.WEEKLY_REPORT, NotificationType.MONTHLY_REPORT -> Icons.Default.BarChart
            NotificationType.SPLIT_EXPENSE -> Icons.AutoMirrored.Filled.CallSplit
        }
    }

    fun getIconTint(): Color {
        return when (type) {
            NotificationType.LIMIT_EXCEEDED -> DangerRed
            NotificationType.BUDGET_WARNING -> WarningOrange
            NotificationType.SPENDING_INSIGHT -> PrimaryBlue
            NotificationType.WEEKLY_REPORT, NotificationType.MONTHLY_REPORT -> SuccessGreen
            NotificationType.SPLIT_EXPENSE -> Color(0xFF8B5CF6)
        }
    }
}
