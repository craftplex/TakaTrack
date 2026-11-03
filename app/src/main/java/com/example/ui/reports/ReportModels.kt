package com.example.ui.reports

import com.example.data.model.Expense
import com.example.data.model.ExpenseCategory
import com.example.ui.CategorySummary

data class DayBarData(
    val dayNameEn: String,
    val dayNameBn: String,
    val dateString: String,
    val amount: Double,
    val isPeak: Boolean = false
)

data class MonthBarData(
    val monthNumber: Int,
    val monthNameEn: String,
    val monthNameBn: String,
    val amount: Double,
    val isPeak: Boolean = false
)

data class ShopExpenseSummary(
    val shopName: String,
    val totalAmount: Double,
    val count: Int
)

data class PaymentMethodSummary(
    val paymentMethod: String,
    val totalAmount: Double,
    val percentage: Float,
    val count: Int
)

data class UnusualSpendingItem(
    val title: String,
    val reason: String,
    val amount: Double,
    val category: ExpenseCategory? = null
)

data class DailyReportData(
    val todayTotal: Double = 0.0,
    val yesterdayTotal: Double = 0.0,
    val difference: Double = 0.0,
    val percentageChange: Double? = null,
    val transactionCount: Int = 0,
    val averageExpense: Double = 0.0,
    val highestCategory: CategorySummary? = null,
    val expenses: List<Expense> = emptyList()
)

data class WeeklyReportData(
    val weeklyTotal: Double = 0.0,
    val dayBars: List<DayBarData> = emptyList(),
    val mostExpensiveDay: DayBarData? = null,
    val lowestSpendingDay: DayBarData? = null,
    val weeklyAverage: Double = 0.0,
    val topCategories: List<CategorySummary> = emptyList(),
    val paymentBreakdowns: List<PaymentMethodSummary> = emptyList()
)

data class MonthlyReportData(
    val currentMonthTotal: Double = 0.0,
    val previousMonthTotal: Double = 0.0,
    val difference: Double = 0.0,
    val percentageChange: Double? = null,
    val transactionCount: Int = 0,
    val dailyAverage: Double = 0.0,
    val monthlyLimit: Double = 20000.0,
    val unusedBudget: Double = 0.0,
    val budgetUsagePercent: Float = 0f,
    val categoryBreakdowns: List<CategorySummary> = emptyList(),
    val shopBreakdowns: List<ShopExpenseSummary> = emptyList(),
    val paymentBreakdowns: List<PaymentMethodSummary> = emptyList(),
    val unusualSpendings: List<UnusualSpendingItem> = emptyList()
)

data class YearlyReportData(
    val annualTotal: Double = 0.0,
    val monthBars: List<MonthBarData> = emptyList(),
    val highestMonth: MonthBarData? = null,
    val lowestMonth: MonthBarData? = null,
    val annualLimit: Double = 240000.0,
    val highestCategory: CategorySummary? = null,
    val mostFrequentShop: String? = null,
    val paymentBreakdowns: List<PaymentMethodSummary> = emptyList(),
    val trends: List<String> = emptyList()
)

data class ComparisonCategoryDiff(
    val category: ExpenseCategory,
    val periodAAmount: Double,
    val periodBAmount: Double,
    val difference: Double
)

data class ComparisonReportData(
    val periodALabel: String = "Last 7 Days",
    val periodBLabel: String = "Previous 7 Days",
    val periodATotal: Double = 0.0,
    val periodBTotal: Double = 0.0,
    val difference: Double = 0.0,
    val percentageChange: Double? = null,
    val periodADailyAvg: Double = 0.0,
    val periodBDailyAvg: Double = 0.0,
    val dailyAvgDifference: Double = 0.0,
    val categoryDiffs: List<ComparisonCategoryDiff> = emptyList(),
    val topIncreasedCategory: ExpenseCategory? = null
)

data class SpendingPatternData(
    val dailyAverage: Double = 0.0,
    val weeklyAverage: Double = 0.0,
    val highestSpendingDayName: String = "Friday",
    val highestCategoryName: String = "Grocery",
    val weekdayTotal: Double = 0.0,
    val weekendTotal: Double = 0.0,
    val morningTotal: Double = 0.0,
    val eveningTotal: Double = 0.0,
    val recurringTips: List<String> = emptyList()
)
