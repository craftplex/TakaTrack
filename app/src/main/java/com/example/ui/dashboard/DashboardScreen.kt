package com.example.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.CategoryConstants
import com.example.data.model.DateFilterOption
import com.example.data.model.Expense
import com.example.ui.CategorySummary
import com.example.ui.ExpenseScreenState
import com.example.ui.ExpenseViewModel
import com.example.ui.SpendingInsight
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DeepBlue
import com.example.ui.theme.LightBlue
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningOrange
import java.util.Calendar
import java.util.Locale

import androidx.compose.material.icons.filled.BarChart

@Composable
fun DashboardScreen(
    state: ExpenseScreenState,
    viewModel: ExpenseViewModel,
    onNavigateToExpenses: () -> Unit,
    onNavigateToDaily: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val greeting = getGreetingMessage()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // 1. Top Header
        item {
            DashboardHeader(
                greeting = greeting,
                userName = state.userProfile?.displayName?.ifBlank { "User" } ?: "User",
                onNotificationClick = { viewModel.openAddExpenseDialog() },
                onProfileClick = onNavigateToProfile
            )
        }

        // 2. Date Filter
        item {
            DateFilterChips(
                selectedFilter = state.selectedDateFilter,
                onFilterSelected = { viewModel.setDateFilter(it) }
            )
        }

        // 3. Total Spending Card
        item {
            TotalSpendingCard(
                totalSpending = state.totalSpending,
                percentageChange = state.percentageChange,
                transactionCount = state.transactionCount,
                dailyAverage = state.dailyAverageExpense,
                periodLabel = state.selectedDateFilter.labelBn
            )
        }

        // 3.5 Quick Expense Templates (1-Tap Logging)
        if (state.quickTemplates.isNotEmpty()) {
            item {
                SectionHeader(title = "দ্রুত খরচ রেকর্ড (1-Tap Quick Log)")
                QuickTemplatesRow(
                    templates = state.quickTemplates,
                    onTemplateClick = { template ->
                        viewModel.logExpenseFromTemplate(template)
                    }
                )
            }
        }

        // 4. Spending Trend Visualizer
        if (state.filteredExpenses.isNotEmpty()) {
            item {
                SpendingTrendCard(expenses = state.filteredExpenses)
            }
        }

        // 5. Spending Limit Overview (Monthly Budget)
        item {
            SpendingLimitCard(
                monthlyLimit = state.monthlyLimit,
                usedAmount = state.totalSpending,
                budgetUsagePercent = state.budgetUsagePercent,
                remainingBudget = state.remainingBudget,
                onSetLimitClick = onNavigateToProfile
            )
        }

        // 6. Top Expense Categories
        if (state.categoryBreakdowns.isNotEmpty()) {
            item {
                SectionHeader(title = "শীর্ষ খরচের খাত (Top Categories)")
                TopCategoriesSection(categories = state.categoryBreakdowns)
            }
        }

        // 7. Spending Insights Preview
        if (state.smartInsights.isNotEmpty()) {
            item {
                SectionHeader(title = "খরচের বিশ্লেষণ ও পরামর্শ (Insights)")
                InsightsPreviewSection(insights = state.smartInsights)
            }
        }

        // 8. Recent Expenses
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "সর্বশেষ খরচ (Recent Expenses)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "সব দেখুন",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .clickable { onNavigateToExpenses() }
                        .padding(4.dp)
                )
            }
        }

        if (state.recentExpenses.isEmpty()) {
            item {
                EmptyExpensesPlaceholder(onAddClick = { viewModel.openAddExpenseDialog() })
            }
        } else {
            items(state.recentExpenses, key = { it.id.ifBlank { it.hashCode().toString() } }) { expense ->
                RecentExpenseRow(
                    expense = expense,
                    onClick = { viewModel.showExpenseDetails(expense) }
                )
            }
        }

        // 9. Quick Actions
        item {
            QuickActionsBar(
                onAddExpense = { viewModel.openAddExpenseDialog() },
                onViewDaily = onNavigateToDaily,
                onViewReports = onNavigateToReports,
                onSetLimit = onNavigateToProfile
            )
        }
    }
}

@Composable
private fun DashboardHeader(
    greeting: String,
    userName: String,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$greeting, $userName",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Here's your spending overview",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.secondary)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            BadgedBox(
                badge = {
                    Badge(containerColor = PrimaryBlue) {
                        Text("1", color = Color.White, fontSize = 10.sp)
                    }
                }
            ) {
                IconButton(onClick = onNotificationClick) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(LightBlue)
                    .clickable { onProfileClick() }
                    .border(1.5.dp, PrimaryBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun DateFilterChips(
    selectedFilter: DateFilterOption,
    onFilterSelected: (DateFilterOption) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(DateFilterOption.values()) { option ->
            FilterChip(
                selected = selectedFilter == option,
                onClick = { onFilterSelected(option) },
                label = {
                    Text(
                        text = "${option.labelBn} (${option.labelEn})",
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryBlue,
                    selectedLabelColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
private fun TotalSpendingCard(
    totalSpending: Double,
    percentageChange: Double?,
    transactionCount: Int,
    dailyAverage: Double,
    periodLabel: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Spending ($periodLabel)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.secondary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(LightBlue)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Expense Only",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = DeepBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Large Amount formatted in BDT ৳
            Text(
                text = "৳ ${String.format(Locale.US, "%,.2f", totalSpending)}",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryBlue,
                    letterSpacing = (-0.5).sp
                )
            )

            // Percentage change badge
            if (percentageChange != null) {
                Spacer(modifier = Modifier.height(6.dp))
                val isIncrease = percentageChange >= 0
                val tagColor = if (isIncrease) WarningOrange else SuccessGreen
                val sign = if (isIncrease) "↑ +" else "↓ "
                Text(
                    text = "$sign${String.format(Locale.US, "%.1f", percentageChange)}% Compared to previous period",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = tagColor,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Sub-metrics: Transaction Count & Daily Average
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Recorded Expenses",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "$transactionCount Transactions",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Daily Average",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "৳ ${String.format(Locale.US, "%,.0f", dailyAverage)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun SpendingTrendCard(expenses: List<Expense>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "ব্যয় চিত্র (Spending Overview Chart)",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Bar chart canvas
            val dailySums = expenses.groupBy { it.expenseDate }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
                .toList()
                .sortedBy { it.first }
                .takeLast(7)

            if (dailySums.isNotEmpty()) {
                val maxVal = (dailySums.maxOfOrNull { it.second } ?: 1.0).coerceAtLeast(1.0)
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    val count = dailySums.size
                    val spacing = size.width / (count + 1)
                    val barWidth = (spacing * 0.5f).coerceIn(12f, 36f)

                    dailySums.forEachIndexed { index, pair ->
                        val x = spacing * (index + 1)
                        val barHeight = ((pair.second / maxVal) * (size.height - 24f)).toFloat()
                        val isPeak = pair.second == maxVal
                        val barColor = if (isPeak) DeepBlue else PrimaryBlue.copy(alpha = 0.55f)

                        drawRect(
                            color = barColor,
                            topLeft = Offset(x - barWidth / 2, size.height - barHeight),
                            size = Size(barWidth, barHeight)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dailySums.forEach { pair ->
                        val label = pair.first.takeLast(5)
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpendingLimitCard(
    monthlyLimit: Double,
    usedAmount: Double,
    budgetUsagePercent: Float,
    remainingBudget: Double,
    onSetLimitClick: () -> Unit
) {
    val progressColor = when {
        budgetUsagePercent >= 100f -> DangerRed
        budgetUsagePercent >= 75f -> WarningOrange
        else -> PrimaryBlue
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monthly Spending Limit",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${String.format(Locale.US, "%.1f", budgetUsagePercent)}% Used",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = progressColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "৳ ${String.format(Locale.US, "%,.0f", usedAmount)}",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = "৳ ${String.format(Locale.US, "%,.0f", monthlyLimit)} monthly limit",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.secondary)
            )

            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { (budgetUsagePercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = progressColor,
                trackColor = LightBlue
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Used: ৳${String.format(Locale.US, "%,.0f", usedAmount)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Remaining Limit: ৳${String.format(Locale.US, "%,.0f", remainingBudget)}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (remainingBudget > 0) SuccessGreen else DangerRed
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "এটি Income বা Account Balance নয়। এটি শুধু Spending Limit থেকে কতটুকু খরচ হয়েছে তার হিসাব।",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
    )
}

@Composable
private fun TopCategoriesSection(categories: List<CategorySummary>) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories.take(6)) { catSummary ->
            Card(
                modifier = Modifier
                    .width(160.dp)
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(catSummary.category.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = catSummary.category.icon,
                            contentDescription = catSummary.category.nameEn,
                            tint = catSummary.category.color,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = catSummary.category.nameBn,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "৳ ${String.format(Locale.US, "%,.0f", catSummary.totalAmount)}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { catSummary.percentage.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = catSummary.category.color,
                        trackColor = LightBlue
                    )
                    Text(
                        text = "${(catSummary.percentage * 100).toInt()}% of total",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightsPreviewSection(insights: List<SpendingInsight>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        insights.take(2).forEach { insight ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (insight.isWarning) Color(0xFFFFFBEB) else LightBlue
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = if (insight.isWarning) Icons.Default.Warning else Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = if (insight.isWarning) WarningOrange else PrimaryBlue,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = insight.title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (insight.isWarning) WarningOrange else DeepBlue
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = insight.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecentExpenseRow(
    expense: Expense,
    onClick: () -> Unit
) {
    val category = CategoryConstants.getCategoryById(expense.categoryId)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(category.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = category.nameEn,
                    tint = category.color,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.title.ifBlank { category.nameBn },
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val subtitle = buildString {
                    if (expense.shopName.isNotBlank()) append("${expense.shopName} • ")
                    append(expense.expenseDate)
                    if (expense.expenseTime.isNotBlank()) append(" ${expense.expenseTime}")
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "৳ ${String.format(Locale.US, "%,.0f", expense.amount)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = expense.paymentMethod,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun EmptyExpensesPlaceholder(onAddClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "কোনো খরচ পাওয়া যায়নি",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = "আপনার প্রথম খরচ যোগ করে ট্র্যাকিং শুরু করুন",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
            OutlinedButton(onClick = onAddClick) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("নতুন খরচ রেকর্ড করুন")
            }
        }
    }
}

@Composable
private fun QuickActionsBar(
    onAddExpense: () -> Unit,
    onViewDaily: () -> Unit,
    onViewReports: () -> Unit,
    onSetLimit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "দ্রুত অপশন (Quick Actions)",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                QuickActionItem(
                    icon = Icons.Default.Add,
                    label = "Add Expense",
                    onClick = onAddExpense
                )
                QuickActionItem(
                    icon = Icons.Default.CalendarMonth,
                    label = "Daily View",
                    onClick = onViewDaily
                )
                QuickActionItem(
                    icon = Icons.Default.BarChart,
                    label = "Reports",
                    onClick = onViewReports
                )
                QuickActionItem(
                    icon = Icons.Default.Tune,
                    label = "Set Limit",
                    onClick = onSetLimit
                )
            }
        }
    }
}

@Composable
private fun QuickActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(LightBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = PrimaryBlue,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun getGreetingMessage(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        in 17..20 -> "Good Evening"
        else -> "Hello"
    }
}

@Composable
private fun QuickTemplatesRow(
    templates: List<com.example.data.model.ExpenseTemplate>,
    onTemplateClick: (com.example.data.model.ExpenseTemplate) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(templates) { template ->
            val cat = CategoryConstants.getCategoryById(template.categoryId)
            Card(
                modifier = Modifier
                    .width(150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onTemplateClick(template) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(cat.color.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = cat.icon,
                                contentDescription = null,
                                tint = cat.color,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(LightBlue)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "1-Tap",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = template.title,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "৳ ${String.format(Locale.US, "%,.0f", template.amount)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    )
                    Text(
                        text = template.paymentMethod.substringBefore(" "),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}
