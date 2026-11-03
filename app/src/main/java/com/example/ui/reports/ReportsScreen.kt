package com.example.ui.reports

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CategorySummary
import com.example.ui.ExpenseScreenState
import com.example.ui.ExpenseViewModel
import com.example.ui.dashboard.RecentExpenseRow
import com.example.ui.insights.AiInsightsViewModel
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.BorderLight
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DeepBlue
import com.example.ui.theme.HeadingDark
import com.example.ui.theme.LightBlue
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.WarningOrange
import java.util.Locale

enum class ReportPeriod(val titleEn: String, val titleBn: String) {
    DAILY("Daily", "দৈনিক"),
    WEEKLY("Weekly", "সাপ্তাহিক"),
    MONTHLY("Monthly", "মাসিক"),
    YEARLY("Yearly", "বার্ষিক"),
    COMPARISON("Comparison", "তুলনা"),
    INSIGHTS("Insights", "বিশ্লেষণ")
}

@Composable
fun ReportsScreen(
    state: ExpenseScreenState,
    viewModel: ExpenseViewModel,
    aiViewModel: AiInsightsViewModel,
    onNavigateToAiAdvisor: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPeriodIndex by remember { mutableIntStateOf(2) } // Default to Monthly
    var comparisonPreset by remember { mutableStateOf("last_7_days") }

    val allExpenses = state.allExpenses
    val monthlyLimit = state.monthlyLimit

    val dailyData = remember(allExpenses) { ReportAnalyticsCalculator.calculateDailyReport(allExpenses) }
    val weeklyData = remember(allExpenses) { ReportAnalyticsCalculator.calculateWeeklyReport(allExpenses) }
    val monthlyData = remember(allExpenses, monthlyLimit) {
        ReportAnalyticsCalculator.calculateMonthlyReport(allExpenses, monthlyLimit)
    }
    val yearlyData = remember(allExpenses, monthlyLimit) {
        ReportAnalyticsCalculator.calculateYearlyReport(allExpenses, monthlyLimit)
    }
    val comparisonData = remember(allExpenses, comparisonPreset) {
        ReportAnalyticsCalculator.calculateComparison(allExpenses, comparisonPreset)
    }
    val patternData = remember(allExpenses) { ReportAnalyticsCalculator.calculateSpendingPatterns(allExpenses) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .testTag("reports_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // 8.1 Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Spending Reports",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = HeadingDark
                    )
                )
                Text(
                    text = "Understand where your money goes • খরচের পূর্ণাঙ্গ রিপোর্ট",
                    style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Period Selector Tabs
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedPeriodIndex,
                containerColor = SurfaceWhite,
                contentColor = PrimaryBlue,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedPeriodIndex]),
                        color = PrimaryBlue,
                        height = 3.dp
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                ReportPeriod.values().forEachIndexed { index, period ->
                    Tab(
                        selected = selectedPeriodIndex == index,
                        onClick = { selectedPeriodIndex = index },
                        text = {
                            Text(
                                text = "${period.titleBn} (${period.titleEn})",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selectedPeriodIndex == index) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedPeriodIndex == index) PrimaryBlue else SecondaryText
                                )
                            )
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Content based on Selected Tab
        when (ReportPeriod.values()[selectedPeriodIndex]) {
            ReportPeriod.DAILY -> {
                item { DailyReportContent(data = dailyData, onExpenseClick = { viewModel.showExpenseDetails(it) }) }
            }
            ReportPeriod.WEEKLY -> {
                item { WeeklyReportContent(data = weeklyData) }
            }
            ReportPeriod.MONTHLY -> {
                item { MonthlyReportContent(data = monthlyData) }
            }
            ReportPeriod.YEARLY -> {
                item { YearlyReportContent(data = yearlyData) }
            }
            ReportPeriod.COMPARISON -> {
                item {
                    ComparisonReportContent(
                        data = comparisonData,
                        selectedPreset = comparisonPreset,
                        onPresetSelected = { comparisonPreset = it }
                    )
                }
            }
            ReportPeriod.INSIGHTS -> {
                item {
                    SpendingInsightsContent(
                        patternData = patternData,
                        smartInsights = state.smartInsights,
                        onOpenAiAdvisor = onNavigateToAiAdvisor
                    )
                }
            }
        }
    }
}

/**
 * 8.2 Daily Report
 */
@Composable
private fun DailyReportContent(
    data: DailyReportData,
    onExpenseClick: (com.example.data.model.Expense) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Today's Spending Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = BorderStroke(1.dp, BorderLight),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Daily Spending Report (আজকের খরচ)",
                    style = MaterialTheme.typography.labelLarge.copy(color = SecondaryText, fontWeight = FontWeight.Medium)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "৳ ${String.format(Locale.US, "%,.2f", data.todayTotal)}",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryBlue
                    )
                )

                // Yesterday comparison
                if (data.percentageChange != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val isMore = data.difference >= 0
                    val badgeColor = if (isMore) WarningOrange else SuccessGreen
                    val icon = if (isMore) Icons.Default.TrendingUp else Icons.Default.TrendingDown
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "গরমিলের তুলনায় ৳${String.format(Locale.US, "%,.0f", Math.abs(data.difference))} ${if (isMore) "বেশি" else "কম"} (${String.format(Locale.US, "%.1f", Math.abs(data.percentageChange))}% ${if (isMore) "বৃদ্ধি" else "হ্রাস"})",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = badgeColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderLight))
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Transactions", style = MaterialTheme.typography.labelSmall, color = SecondaryText)
                        Text(text = "${data.transactionCount}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = HeadingDark))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Highest Category", style = MaterialTheme.typography.labelSmall, color = SecondaryText)
                        Text(
                            text = data.highestCategory?.category?.nameBn ?: "N/A",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        )
                    }
                }
            }
        }

        // Today's Transactions
        Text(
            text = "আজকের খরচের তালিকা",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = HeadingDark),
            modifier = Modifier.padding(top = 8.dp)
        )

        if (data.expenses.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = BorderStroke(1.dp, BorderLight)
            ) {
                Text(
                    text = "আজকে এখনো কোনো খরচ রেকর্ড করা হয়নি।",
                    style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText),
                    modifier = Modifier.padding(20.dp)
                )
            }
        } else {
            data.expenses.forEach { exp ->
                RecentExpenseRow(expense = exp, onClick = { onExpenseClick(exp) })
            }
        }
    }
}

/**
 * 8.3 Weekly Report
 */
@Composable
private fun WeeklyReportContent(data: WeeklyReportData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = BorderStroke(1.dp, BorderLight),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Weekly Total Spending (সাপ্তাহিক মোট খরচ)",
                    style = MaterialTheme.typography.labelLarge.copy(color = SecondaryText)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "৳ ${String.format(Locale.US, "%,.2f", data.weeklyTotal)}",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryBlue
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Bar Chart
                Text(
                    text = "প্রতিদিনের খরচের চিত্র (Bar Chart)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = HeadingDark)
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (data.dayBars.isNotEmpty()) {
                    val maxVal = (data.dayBars.maxOfOrNull { it.amount } ?: 1.0).coerceAtLeast(1.0)
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        val count = data.dayBars.size
                        val spacing = size.width / (count + 1)
                        val barWidth = (spacing * 0.55f).coerceIn(16f, 38f)

                        data.dayBars.forEachIndexed { index, item ->
                            val x = spacing * (index + 1)
                            val barHeight = if (maxVal > 0) ((item.amount / maxVal) * (size.height - 24f)).toFloat() else 0f
                            // PDF Page 25: Bar Chart-এ সবচেয়ে বেশি খরচের দিন গাঢ় Blue Color (#176FB3) দিয়ে Highlight হবে!
                            val barColor = if (item.isPeak) DeepBlue else PrimaryBlue.copy(alpha = 0.5f)

                            drawRect(
                                color = barColor,
                                topLeft = Offset(x - barWidth / 2, size.height - barHeight),
                                size = Size(barWidth, barHeight.coerceAtLeast(4f))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        data.dayBars.forEach { item ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = item.dayNameBn.take(3),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = if (item.isPeak) FontWeight.Bold else FontWeight.Normal,
                                        color = if (item.isPeak) DeepBlue else SecondaryText
                                    )
                                )
                                Text(
                                    text = "৳${item.amount.toInt()}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = SecondaryText)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Metrics Grid (4 items)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ReportMiniMetricCard(
                title = "Most Expensive Day",
                value = data.mostExpensiveDay?.let { "${it.dayNameBn} (৳${it.amount.toInt()})" } ?: "N/A",
                modifier = Modifier.weight(1f),
                isHighlighted = true
            )
            ReportMiniMetricCard(
                title = "Lowest Spending Day",
                value = data.lowestSpendingDay?.let { "${it.dayNameBn} (৳${it.amount.toInt()})" } ?: "N/A",
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ReportMiniMetricCard(
                title = "Weekly Average",
                value = "৳ ${String.format(Locale.US, "%,.0f", data.weeklyAverage)}",
                modifier = Modifier.weight(1f)
            )
            ReportMiniMetricCard(
                title = "Active Days",
                value = "${data.dayBars.count { it.amount > 0 }} Days",
                modifier = Modifier.weight(1f)
            )
        }

        // Top Categories
        if (data.topCategories.isNotEmpty()) {
            Text(
                text = "সাপ্তাহিক শীর্ষ খাত",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = HeadingDark),
                modifier = Modifier.padding(top = 8.dp)
            )
            data.topCategories.take(4).forEach { catSummary ->
                CategoryProgressCard(catSummary = catSummary)
            }
        }
    }
}

/**
 * 8.4 Monthly Report
 */
@Composable
private fun MonthlyReportContent(data: MonthlyReportData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // A. Monthly Expense Summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = BorderStroke(1.dp, BorderLight),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Total Monthly Expense (মাসিক মোট খরচ)",
                    style = MaterialTheme.typography.labelLarge.copy(color = SecondaryText)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "৳ ${String.format(Locale.US, "%,.2f", data.currentMonthTotal)}",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryBlue
                    )
                )

                // Previous month comparison
                if (data.percentageChange != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    val isMore = data.difference >= 0
                    val badgeColor = if (isMore) WarningOrange else SuccessGreen
                    Text(
                        text = "গত মাসের তুলনায় ${if (isMore) "বেড়েছে" else "কমেছে"}: ৳${String.format(Locale.US, "%,.0f", Math.abs(data.difference))} (${String.format(Locale.US, "%.1f", Math.abs(data.percentageChange))}%)",
                        style = MaterialTheme.typography.bodySmall.copy(color = badgeColor, fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total Transactions: ${data.transactionCount}",
                        style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                    )
                    Text(
                        text = "Daily Average: ৳${String.format(Locale.US, "%,.0f", data.dailyAverage)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = HeadingDark)
                    )
                }
            }
        }

        // E. Savings Analysis (Unused Budget / Budget Left)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = LightBlue),
            border = BorderStroke(1.dp, BorderLight)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Budget Left (অবশিষ্ট বাজেট)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DeepBlue)
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.1f", data.budgetUsagePercent)}% Used",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "৳ ${String.format(Locale.US, "%,.0f", data.unusedBudget)}",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepBlue
                    )
                )
                Text(
                    text = "Monthly Limit: ৳${String.format(Locale.US, "%,.0f", data.monthlyLimit)} | Actual Spent: ৳${String.format(Locale.US, "%,.0f", data.currentMonthTotal)}",
                    style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                )
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { (data.budgetUsagePercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = PrimaryBlue,
                    trackColor = SurfaceWhite
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "⚠️ এই অ্যাপে Income বা Balance থাকবে না। তাই এটিকে Budget Left বলা হচ্ছে, প্রকৃত আর্থিক সঞ্চয় নয়।",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = SecondaryText)
                )
            }
        }

        // B. Category Analysis
        if (data.categoryBreakdowns.isNotEmpty()) {
            Text(
                text = "খাতভিত্তিক বিশ্লেষণ (Category Breakdown)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = HeadingDark),
                modifier = Modifier.padding(top = 6.dp)
            )
            data.categoryBreakdowns.forEach { catSummary ->
                CategoryProgressCard(catSummary = catSummary)
            }
        }

        // C. Shop-wise Shopping Report
        if (data.shopBreakdowns.isNotEmpty()) {
            Text(
                text = "দোকানভিত্তিক কেনাকাটা (Shop-wise Report)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = HeadingDark),
                modifier = Modifier.padding(top = 6.dp)
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = BorderStroke(1.dp, BorderLight)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    data.shopBreakdowns.take(5).forEach { shop ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(32.dp).clip(CircleShape).background(LightBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Store, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = shop.shopName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = HeadingDark))
                                    Text(text = "${shop.count} টি কেনাকাটা", style = MaterialTheme.typography.labelSmall.copy(color = SecondaryText))
                                }
                            }
                            Text(
                                text = "৳ ${String.format(Locale.US, "%,.0f", shop.totalAmount)}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue)
                            )
                        }
                    }
                }
            }
        }

        // F. Unusual Spending Detection
        if (data.unusualSpendings.isNotEmpty()) {
            Text(
                text = "সম্ভাব্য অস্বাভাবিক খরচ (Review Suggestions)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = HeadingDark),
                modifier = Modifier.padding(top = 6.dp)
            )
            data.unusualSpendings.forEach { unusual ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                    border = BorderStroke(1.dp, WarningOrange.copy(alpha = 0.5f))
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = WarningOrange, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = unusual.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = HeadingDark))
                            Text(text = unusual.reason, style = MaterialTheme.typography.bodySmall.copy(color = HeadingDark.copy(alpha = 0.8f)))
                        }
                    }
                }
            }
        }
    }
}

/**
 * 8.5 Yearly Report
 */
@Composable
private fun YearlyReportContent(data: YearlyReportData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = BorderStroke(1.dp, BorderLight),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Annual Total Expense (বার্ষিক মোট ব্যয়)",
                    style = MaterialTheme.typography.labelLarge.copy(color = SecondaryText)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "৳ ${String.format(Locale.US, "%,.2f", data.annualTotal)}",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryBlue
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 12-Month Bar Chart
                Text(
                    text = "১২ মাসের ব্যয়ের তুলনা (Monthly Comparison Chart)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = HeadingDark)
                )
                Spacer(modifier = Modifier.height(12.dp))

                val maxMonth = (data.monthBars.maxOfOrNull { it.amount } ?: 1.0).coerceAtLeast(1.0)
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    val count = data.monthBars.size
                    val spacing = size.width / (count + 1)
                    val barWidth = (spacing * 0.5f).coerceIn(10f, 24f)

                    data.monthBars.forEachIndexed { index, mBar ->
                        val x = spacing * (index + 1)
                        val barHeight = ((mBar.amount / maxMonth) * (size.height - 20f)).toFloat()
                        val barColor = if (mBar.isPeak) DeepBlue else PrimaryBlue.copy(alpha = 0.45f)

                        drawRect(
                            color = barColor,
                            topLeft = Offset(x - barWidth / 2, size.height - barHeight),
                            size = Size(barWidth, barHeight.coerceAtLeast(4f))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    data.monthBars.forEach { mBar ->
                        Text(
                            text = mBar.monthNameBn.take(2),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = if (mBar.isPeak) FontWeight.Bold else FontWeight.Normal,
                                color = if (mBar.isPeak) DeepBlue else SecondaryText
                            )
                        )
                    }
                }
            }
        }

        // Yearly Key Metrics Grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ReportMiniMetricCard(
                title = "Highest Spending Month",
                value = data.highestMonth?.let { "${it.monthNameBn} (৳${it.amount.toInt()})" } ?: "N/A",
                modifier = Modifier.weight(1f),
                isHighlighted = true
            )
            ReportMiniMetricCard(
                title = "Lowest Month",
                value = data.lowestMonth?.let { "${it.monthNameBn} (৳${it.amount.toInt()})" } ?: "N/A",
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ReportMiniMetricCard(
                title = "Highest Category",
                value = data.highestCategory?.category?.nameBn ?: "N/A",
                modifier = Modifier.weight(1f)
            )
            ReportMiniMetricCard(
                title = "Frequent Shop",
                value = data.mostFrequentShop ?: "N/A",
                modifier = Modifier.weight(1f)
            )
        }

        // Trends
        if (data.trends.isNotEmpty()) {
            Text(
                text = "বার্ষিক ধারা ও পর্যবেক্ষণ (Yearly Trends)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = HeadingDark),
                modifier = Modifier.padding(top = 6.dp)
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = BorderStroke(1.dp, BorderLight)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    data.trends.forEach { trend ->
                        Row(verticalAlignment = Alignment.Top) {
                            Text(text = "• ", style = MaterialTheme.typography.bodyMedium.copy(color = PrimaryBlue, fontWeight = FontWeight.Bold))
                            Text(text = trend, style = MaterialTheme.typography.bodySmall.copy(color = HeadingDark))
                        }
                    }
                }
            }
        }
    }
}

/**
 * 8.6 Custom Date Comparison
 */
@Composable
private fun ComparisonReportContent(
    data: ComparisonReportData,
    selectedPreset: String,
    onPresetSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Presets Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedPreset == "last_7_days",
                    onClick = { onPresetSelected("last_7_days") },
                    label = { Text("Last 7 Days vs Prev 7 Days") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryBlue,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }
            item {
                FilterChip(
                    selected = selectedPreset == "this_month_vs_last",
                    onClick = { onPresetSelected("this_month_vs_last") },
                    label = { Text("This Month vs Last Month") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryBlue,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }
            item {
                FilterChip(
                    selected = selectedPreset == "last_30_days",
                    onClick = { onPresetSelected("last_30_days") },
                    label = { Text("Last 30 Days vs Prev 30 Days") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryBlue,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        // Period Comparison Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = BorderStroke(1.dp, BorderLight),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Compare Spending (ব্যয় তুলনা)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = HeadingDark)
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Period A
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = LightBlue)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "Period A: ${data.periodALabel}", style = MaterialTheme.typography.labelSmall.copy(color = DeepBlue))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "৳ ${String.format(Locale.US, "%,.2f", data.periodATotal)}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Period B
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BackgroundLight)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "Period B: ${data.periodBLabel}", style = MaterialTheme.typography.labelSmall.copy(color = SecondaryText))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "৳ ${String.format(Locale.US, "%,.2f", data.periodBTotal)}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = HeadingDark)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderLight))
                Spacer(modifier = Modifier.height(14.dp))

                // Difference & Percentage
                val isMore = data.difference >= 0
                val diffColor = if (isMore) WarningOrange else SuccessGreen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Difference (পার্থক্য)", style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText))
                    Text(
                        text = "${if (isMore) "+৳" else "-৳"}${String.format(Locale.US, "%,.0f", Math.abs(data.difference))}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = diffColor)
                    )
                }

                if (data.percentageChange != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Percentage Change", style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText))
                        Text(
                            text = "${if (isMore) "+" else ""}${String.format(Locale.US, "%.1f", data.percentageChange)}%",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = diffColor)
                        )
                    }
                }
            }
        }

        // Category-wise difference
        if (data.categoryDiffs.isNotEmpty()) {
            Text(
                text = "খাত অনুযায়ী পার্থক্য (Category-wise Difference)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = HeadingDark)
            )
            data.categoryDiffs.take(5).forEach { catDiff ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(1.dp, BorderLight)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(32.dp).clip(CircleShape).background(catDiff.category.color.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = catDiff.category.icon, contentDescription = null, tint = catDiff.category.color, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = catDiff.category.nameBn, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = HeadingDark))
                                Text(
                                    text = "A: ৳${catDiff.periodAAmount.toInt()} vs B: ৳${catDiff.periodBAmount.toInt()}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = SecondaryText)
                                )
                            }
                        }

                        val isUp = catDiff.difference >= 0
                        Text(
                            text = "${if (isUp) "+" else "-"}৳${String.format(Locale.US, "%,.0f", Math.abs(catDiff.difference))}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isUp) WarningOrange else SuccessGreen
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Screen 09: Spending Insights & Patterns
 */
@Composable
private fun SpendingInsightsContent(
    patternData: SpendingPatternData,
    smartInsights: List<com.example.ui.SpendingInsight>,
    onOpenAiAdvisor: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 9.2 Key Statistics (4 small cards)
        Text(
            text = "মূল পরিসংখ্যান (Key Statistics)",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = HeadingDark)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ReportMiniMetricCard(
                title = "Daily Average",
                value = "৳ ${String.format(Locale.US, "%,.0f", patternData.dailyAverage)}",
                modifier = Modifier.weight(1f)
            )
            ReportMiniMetricCard(
                title = "Weekly Average",
                value = "৳ ${String.format(Locale.US, "%,.0f", patternData.weeklyAverage)}",
                modifier = Modifier.weight(1f)
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ReportMiniMetricCard(
                title = "Peak Day",
                value = patternData.highestSpendingDayName,
                modifier = Modifier.weight(1f),
                isHighlighted = true
            )
            ReportMiniMetricCard(
                title = "Top Category",
                value = patternData.highestCategoryName,
                modifier = Modifier.weight(1f)
            )
        }

        // 9.4 Spending Pattern Analysis (Weekday vs Weekend, Morning vs Evening)
        Text(
            text = "ব্যয়ের ধরণ (Spending Patterns)",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = HeadingDark),
            modifier = Modifier.padding(top = 6.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = BorderStroke(1.dp, BorderLight)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(text = "কর্মদিবস (Weekday)", style = MaterialTheme.typography.labelSmall, color = SecondaryText)
                        Text(text = "৳ ${String.format(Locale.US, "%,.0f", patternData.weekdayTotal)}", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = HeadingDark))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "ছুটির দিন (Weekend)", style = MaterialTheme.typography.labelSmall, color = SecondaryText)
                        Text(text = "৳ ${String.format(Locale.US, "%,.0f", patternData.weekendTotal)}", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue))
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderLight))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(text = "সকাল ও দুপুর (Morning)", style = MaterialTheme.typography.labelSmall, color = SecondaryText)
                        Text(text = "৳ ${String.format(Locale.US, "%,.0f", patternData.morningTotal)}", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = HeadingDark))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "সন্ধ্যা ও রাত (Evening)", style = MaterialTheme.typography.labelSmall, color = SecondaryText)
                        Text(text = "৳ ${String.format(Locale.US, "%,.0f", patternData.eveningTotal)}", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue))
                    }
                }
            }
        }

        // 9.5 Budget Saving Suggestions
        Text(
            text = "সাশ্রয়ী পরামর্শ (Budget Saving Suggestions)",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = HeadingDark),
            modifier = Modifier.padding(top = 6.dp)
        )
        patternData.recurringTips.forEach { tip ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LightBlue),
                border = BorderStroke(1.dp, BorderLight)
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = tip, style = MaterialTheme.typography.bodySmall.copy(color = HeadingDark, fontWeight = FontWeight.Medium))
                }
            }
        }

        // Gemini High Thinking AI Advisor CTA
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = BorderStroke(1.5.dp, PrimaryBlue),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Gemini High Thinking AI Advisor",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "আপনার সম্পূর্ণ খরচের ইতিহাস পর্যালোচনা করে কৃত্রিম বুদ্ধিমত্তা আপনাকে আরও বিশদ ও কাস্টমাইজড আর্থিক নিরীক্ষা প্রদান করবে।",
                    style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onOpenAiAdvisor,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("AI Advisor চালু করুন")
                }
            }
        }
    }
}

@Composable
private fun ReportMiniMetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isHighlighted) LightBlue else SurfaceWhite),
        border = BorderStroke(1.dp, if (isHighlighted) PrimaryBlue.copy(alpha = 0.5f) else BorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(color = SecondaryText),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isHighlighted) DeepBlue else HeadingDark
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CategoryProgressCard(catSummary: CategorySummary) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(28.dp).clip(CircleShape).background(catSummary.category.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = catSummary.category.icon, contentDescription = null, tint = catSummary.category.color, modifier = Modifier.size(15.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = catSummary.category.nameBn, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, color = HeadingDark))
                }
                Text(
                    text = "৳ ${String.format(Locale.US, "%,.0f", catSummary.totalAmount)} (${(catSummary.percentage * 100).toInt()}%)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = HeadingDark)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { catSummary.percentage.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = catSummary.category.color,
                trackColor = LightBlue
            )
        }
    }
}
