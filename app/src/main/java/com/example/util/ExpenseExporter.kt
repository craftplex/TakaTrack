package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.data.model.CategoryConstants
import com.example.data.model.Expense
import java.util.Locale

object ExpenseExporter {

    /**
     * Converts a list of Expense objects into a formatted CSV string.
     */
    fun exportToCsv(expenses: List<Expense>): String {
        val sb = StringBuilder()
        // Header
        sb.append("ID,Date,Time,Title,Category,Amount (BDT),Payment Method,Shop Name,Notes,Tags,Is Split\n")

        for (exp in expenses) {
            val catName = CategoryConstants.getCategoryById(exp.categoryId).nameEn
            val tagsStr = exp.tags.joinToString(";")
            sb.append(escapeCsv(exp.id)).append(",")
                .append(escapeCsv(exp.expenseDate)).append(",")
                .append(escapeCsv(exp.expenseTime)).append(",")
                .append(escapeCsv(exp.title)).append(",")
                .append(escapeCsv(catName)).append(",")
                .append(String.format(Locale.US, "%.2f", exp.amount)).append(",")
                .append(escapeCsv(exp.paymentMethod)).append(",")
                .append(escapeCsv(exp.shopName)).append(",")
                .append(escapeCsv(exp.notes)).append(",")
                .append(escapeCsv(tagsStr)).append(",")
                .append(if (exp.isSplit) "Yes" else "No").append("\n")
        }

        return sb.toString()
    }

    /**
     * Formats expenses into a readable Bengali/English text summary report.
     */
    fun generateFormattedSummary(expenses: List<Expense>, periodName: String = "Selected Period"): String {
        val total = expenses.sumOf { it.amount }
        val count = expenses.size
        val catMap = expenses.groupBy { it.categoryId }
        val paymentMap = expenses.groupBy { it.paymentMethod }

        val sb = StringBuilder()
        sb.append("📊 TakaTrack BDT - Expense Summary Report\n")
        sb.append("=========================================\n")
        sb.append("📅 Period: ").append(periodName).append("\n")
        sb.append("💰 Total Spent: ৳").append(String.format(Locale.US, "%,.2f", total)).append("\n")
        sb.append("🔢 Total Transactions: ").append(count).append("\n")
        sb.append("-----------------------------------------\n")
        sb.append("📂 Category Breakdown (খাতভিত্তিক খরচ):\n")

        for ((catId, list) in catMap.entries.sortedByDescending { it.value.sumOf { item -> item.amount } }) {
            val cat = CategoryConstants.getCategoryById(catId)
            val sum = list.sumOf { it.amount }
            val pct = if (total > 0) (sum / total * 100).toInt() else 0
            sb.append("• ").append(cat.nameBn).append(": ৳")
                .append(String.format(Locale.US, "%,.0f", sum))
                .append(" (").append(pct).append("%)\n")
        }

        sb.append("-----------------------------------------\n")
        sb.append("💳 Payment Methods (পরিশোধের মাধ্যম):\n")
        for ((method, list) in paymentMap.entries.sortedByDescending { it.value.sumOf { item -> item.amount } }) {
            val sum = list.sumOf { it.amount }
            sb.append("• ").append(method).append(": ৳")
                .append(String.format(Locale.US, "%,.0f", sum))
                .append(" (").append(list.size).append(" txns)\n")
        }

        sb.append("=========================================\n")
        sb.append("Generated via TakaTrack (100% Expense-Only Tracker)\n")
        return sb.toString()
    }

    /**
     * Opens Android System Share Intent with text data.
     */
    fun shareText(context: Context, text: String, title: String = "Share Expense Report") {
        try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, title)
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not launch share menu: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Copies text content to System Clipboard.
     */
    fun copyToClipboard(context: Context, text: String, label: String = "Expense Summary") {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "রিপোর্ট ক্লিপবোর্ডে কপি করা হয়েছে! (Copied to Clipboard)", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Copy failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun escapeCsv(value: String): String {
        var str = value.replace("\n", " ").replace("\r", " ")
        if (str.contains(",") || str.contains("\"")) {
            str = str.replace("\"", "\"\"")
            return "\"$str\""
        }
        return str
    }
}
