package com.example

import com.example.data.model.Expense
import com.example.ui.reports.ReportAnalyticsCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportAnalyticsCalculatorTest {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val todayStr = dateFormat.format(Date())

    @Test
    fun `test daily report calculation with expenses`() {
        val testExpenses = listOf(
            Expense(
                id = "1",
                amount = 250.0,
                title = "Rice and Fish",
                categoryId = "food",
                expenseDate = todayStr,
                expenseTime = "13:30"
            ),
            Expense(
                id = "2",
                amount = 150.0,
                title = "Rickshaw Fare",
                categoryId = "transport",
                expenseDate = todayStr,
                expenseTime = "18:00"
            )
        )

        val dailyReport = ReportAnalyticsCalculator.calculateDailyReport(testExpenses)

        assertEquals(400.0, dailyReport.todayTotal, 0.01)
        assertEquals(2, dailyReport.transactionCount)
        assertNotNull(dailyReport.highestCategory)
        assertEquals("food", dailyReport.highestCategory?.category?.id)
    }

    @Test
    fun `test monthly report remaining budget calculation`() {
        val testExpenses = listOf(
            Expense(
                id = "1",
                amount = 5000.0,
                title = "Groceries",
                categoryId = "groceries",
                expenseDate = todayStr
            )
        )

        val monthlyReport = ReportAnalyticsCalculator.calculateMonthlyReport(
            allExpenses = testExpenses,
            monthlyLimit = 20000.0
        )

        assertEquals(5000.0, monthlyReport.currentMonthTotal, 0.01)
        assertEquals(15000.0, monthlyReport.unusedBudget, 0.01)
        assertEquals(25.0f, monthlyReport.budgetUsagePercent, 0.01f)
    }

    @Test
    fun `test weekly report contains 7 days`() {
        val weeklyReport = ReportAnalyticsCalculator.calculateWeeklyReport(emptyList())
        assertEquals(7, weeklyReport.dayBars.size)
    }
}
