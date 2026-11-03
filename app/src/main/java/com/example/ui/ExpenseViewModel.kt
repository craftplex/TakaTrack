package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CategoryConstants
import com.example.data.model.DateFilterOption
import com.example.data.model.Expense
import com.example.data.model.ExpenseCategory
import com.example.data.model.SortOption
import com.example.data.model.UserProfile
import com.example.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CategorySummary(
    val category: ExpenseCategory,
    val totalAmount: Double,
    val percentage: Float,
    val count: Int
)

data class SpendingInsight(
    val title: String,
    val description: String,
    val category: ExpenseCategory? = null,
    val isWarning: Boolean = false
)

data class DailyTimelineGroup(
    val periodNameEn: String,
    val periodNameBn: String,
    val expenses: List<Expense>
)

data class DialogState(
    val isAddOpen: Boolean = false,
    val detailsExpense: Expense? = null,
    val isEdit: Boolean = false,
    val isDelete: Boolean = false,
    val message: String? = null
)

data class FilterParams(
    val dateFilter: DateFilterOption = DateFilterOption.THIS_MONTH,
    val searchQuery: String = "",
    val categoryFilter: String? = null,
    val sortOption: SortOption = SortOption.NEWEST_FIRST,
    val dailyDate: String = ExpenseViewModel.getTodayDateString(),
    val dialogState: DialogState = DialogState()
)

data class ExpenseScreenState(
    val isLoading: Boolean = false,
    val userProfile: UserProfile? = null,
    val allExpenses: List<Expense> = emptyList(),
    val filteredExpenses: List<Expense> = emptyList(),
    val searchedAndSortedExpenses: List<Expense> = emptyList(),
    val selectedDateFilter: DateFilterOption = DateFilterOption.THIS_MONTH,
    val searchQuery: String = "",
    val selectedCategoryFilter: String? = null,
    val selectedSortOption: SortOption = SortOption.NEWEST_FIRST,
    val totalSpending: Double = 0.0,
    val previousPeriodSpending: Double = 0.0,
    val percentageChange: Double? = null,
    val transactionCount: Int = 0,
    val dailyAverageExpense: Double = 0.0,
    val categoryBreakdowns: List<CategorySummary> = emptyList(),
    val monthlyLimit: Double = 20000.0,
    val budgetUsagePercent: Float = 0f,
    val remainingBudget: Double = 20000.0,
    val recentExpenses: List<Expense> = emptyList(),
    val smartInsights: List<SpendingInsight> = emptyList(),
    val selectedDailyDate: String = ExpenseViewModel.getTodayDateString(),
    val dailyTotalSpending: Double = 0.0,
    val dailyExpenses: List<Expense> = emptyList(),
    val dailyCategoryBreakdowns: List<CategorySummary> = emptyList(),
    val dailyTimeline: List<DailyTimelineGroup> = emptyList(),
    val isAddExpenseDialogOpen: Boolean = false,
    val selectedExpenseForDetails: Expense? = null,
    val isEditDialogOpen: Boolean = false,
    val isDeleteDialogOpen: Boolean = false,
    val userMessage: String? = null
)

class ExpenseViewModel(private val repository: ExpenseRepository) : ViewModel() {

    private val _filterParams = MutableStateFlow(FilterParams())

    val uiState: StateFlow<ExpenseScreenState> = combine(
        repository.observeExpenses().catch { emit(emptyList()) },
        repository.observeUserProfile().catch { emit(null) },
        _filterParams
    ) { rawExpenses, profile, params ->
        calculateUiState(rawExpenses, profile, params)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = ExpenseScreenState(isLoading = true)
    )

    fun setDateFilter(filter: DateFilterOption) {
        _filterParams.value = _filterParams.value.copy(dateFilter = filter)
    }

    fun setSearchQuery(query: String) {
        _filterParams.value = _filterParams.value.copy(searchQuery = query)
    }

    fun setCategoryFilter(categoryId: String?) {
        _filterParams.value = _filterParams.value.copy(categoryFilter = categoryId)
    }

    fun setSortOption(sort: SortOption) {
        _filterParams.value = _filterParams.value.copy(sortOption = sort)
    }

    fun setDailyDate(date: String) {
        _filterParams.value = _filterParams.value.copy(dailyDate = date)
    }

    fun shiftDailyDate(days: Int) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        try {
            val d = sdf.parse(_filterParams.value.dailyDate)
            if (d != null) {
                cal.time = d
                cal.add(Calendar.DAY_OF_YEAR, days)
                _filterParams.value = _filterParams.value.copy(dailyDate = sdf.format(cal.time))
            }
        } catch (e: Exception) {
            _filterParams.value = _filterParams.value.copy(dailyDate = getTodayDateString())
        }
    }

    fun openAddExpenseDialog() {
        val currentDialog = _filterParams.value.dialogState
        _filterParams.value = _filterParams.value.copy(dialogState = currentDialog.copy(isAddOpen = true))
    }

    fun closeAddExpenseDialog() {
        val currentDialog = _filterParams.value.dialogState
        _filterParams.value = _filterParams.value.copy(dialogState = currentDialog.copy(isAddOpen = false))
    }

    fun showExpenseDetails(expense: Expense) {
        val currentDialog = _filterParams.value.dialogState
        _filterParams.value = _filterParams.value.copy(dialogState = currentDialog.copy(detailsExpense = expense))
    }

    fun closeExpenseDetails() {
        val currentDialog = _filterParams.value.dialogState
        _filterParams.value = _filterParams.value.copy(
            dialogState = currentDialog.copy(detailsExpense = null, isEdit = false, isDelete = false)
        )
    }

    fun openEditDialog(expense: Expense) {
        val currentDialog = _filterParams.value.dialogState
        _filterParams.value = _filterParams.value.copy(
            dialogState = currentDialog.copy(detailsExpense = expense, isEdit = true)
        )
    }

    fun closeEditDialog() {
        val currentDialog = _filterParams.value.dialogState
        _filterParams.value = _filterParams.value.copy(dialogState = currentDialog.copy(isEdit = false))
    }

    fun requestDeleteConfirmation(expense: Expense) {
        val currentDialog = _filterParams.value.dialogState
        _filterParams.value = _filterParams.value.copy(
            dialogState = currentDialog.copy(detailsExpense = expense, isDelete = true)
        )
    }

    fun cancelDeleteConfirmation() {
        val currentDialog = _filterParams.value.dialogState
        _filterParams.value = _filterParams.value.copy(dialogState = currentDialog.copy(isDelete = false))
    }

    fun clearUserMessage() {
        val currentDialog = _filterParams.value.dialogState
        _filterParams.value = _filterParams.value.copy(dialogState = currentDialog.copy(message = null))
    }

    fun addExpense(
        amount: Double,
        title: String,
        categoryId: String,
        shopName: String,
        paymentMethod: String,
        date: String,
        time: String,
        notes: String,
        tags: List<String>
    ) {
        val category = CategoryConstants.getCategoryById(categoryId)
        val expense = Expense(
            amount = amount,
            title = title,
            categoryId = category.id,
            categoryName = category.nameEn,
            shopName = shopName,
            paymentMethod = paymentMethod,
            expenseDate = date.ifBlank { getTodayDateString() },
            expenseTime = time.ifBlank { getCurrentTimeString() },
            notes = notes,
            tags = tags
        )
        viewModelScope.launch {
            val result = repository.addExpense(expense)
            val currentDialog = _filterParams.value.dialogState
            if (result.isSuccess) {
                _filterParams.value = _filterParams.value.copy(
                    dialogState = currentDialog.copy(
                        isAddOpen = false,
                        message = "Expense added successfully! (৳${String.format(Locale.US, "%.0f", amount)})"
                    )
                )
            } else {
                _filterParams.value = _filterParams.value.copy(
                    dialogState = currentDialog.copy(
                        message = result.exceptionOrNull()?.localizedMessage ?: "Failed to save expense"
                    )
                )
            }
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            val result = repository.updateExpense(expense)
            val currentDialog = _filterParams.value.dialogState
            if (result.isSuccess) {
                _filterParams.value = _filterParams.value.copy(
                    dialogState = currentDialog.copy(
                        isEdit = false,
                        detailsExpense = null,
                        message = "Expense updated successfully!"
                    )
                )
            } else {
                _filterParams.value = _filterParams.value.copy(
                    dialogState = currentDialog.copy(
                        message = result.exceptionOrNull()?.localizedMessage ?: "Failed to update expense"
                    )
                )
            }
        }
    }

    fun confirmDeleteExpense(expenseId: String) {
        viewModelScope.launch {
            val result = repository.deleteExpense(expenseId)
            val currentDialog = _filterParams.value.dialogState
            if (result.isSuccess) {
                _filterParams.value = _filterParams.value.copy(
                    dialogState = currentDialog.copy(
                        isDelete = false,
                        detailsExpense = null,
                        message = "Expense deleted successfully!"
                    )
                )
            } else {
                _filterParams.value = _filterParams.value.copy(
                    dialogState = currentDialog.copy(
                        message = result.exceptionOrNull()?.localizedMessage ?: "Failed to delete expense"
                    )
                )
            }
        }
    }

    fun updateMonthlyLimit(limit: Double) {
        viewModelScope.launch {
            val result = repository.updateUserProfile(monthlyLimit = limit)
            if (result.isSuccess) {
                val currentDialog = _filterParams.value.dialogState
                _filterParams.value = _filterParams.value.copy(
                    dialogState = currentDialog.copy(
                        message = "Monthly spending limit updated to ৳${String.format(Locale.US, "%.0f", limit)}"
                    )
                )
            }
        }
    }

    fun updateDisplayName(name: String) {
        viewModelScope.launch {
            repository.updateUserProfile(displayName = name)
        }
    }

    fun updateLanguage(lang: String) {
        viewModelScope.launch {
            repository.updateUserProfile(language = lang)
        }
    }

    companion object {
        fun getTodayDateString(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return sdf.format(Date())
        }

        fun getCurrentTimeString(): String {
            val sdf = SimpleDateFormat("HH:mm", Locale.US)
            return sdf.format(Date())
        }
    }
}

private fun calculateUiState(
    allExpenses: List<Expense>,
    profile: UserProfile?,
    params: FilterParams
): ExpenseScreenState {
    val today = ExpenseViewModel.getTodayDateString()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    val cal = Calendar.getInstance()
    cal.time = Date()
    val currentYear = cal.get(Calendar.YEAR)
    val currentMonth = cal.get(Calendar.MONTH)

    cal.add(Calendar.DAY_OF_YEAR, -1)
    val yesterday = sdf.format(cal.time)

    cal.time = Date()
    cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
    val startOfWeek = sdf.format(cal.time)

    val currentMonthPrefix = String.format(Locale.US, "%04d-%02d", currentYear, currentMonth + 1)
    val prevMonthPrefix = if (currentMonth == 0) {
        String.format(Locale.US, "%04d-12", currentYear - 1)
    } else {
        String.format(Locale.US, "%04d-%02d", currentYear, currentMonth)
    }

    val filtered = when (params.dateFilter) {
        DateFilterOption.TODAY -> allExpenses.filter { it.expenseDate == today }
        DateFilterOption.YESTERDAY -> allExpenses.filter { it.expenseDate == yesterday }
        DateFilterOption.THIS_WEEK -> allExpenses.filter { it.expenseDate >= startOfWeek }
        DateFilterOption.THIS_MONTH -> allExpenses.filter { it.expenseDate.startsWith(currentMonthPrefix) }
        DateFilterOption.CUSTOM -> allExpenses
    }

    val previousPeriodExpenses = when (params.dateFilter) {
        DateFilterOption.TODAY -> allExpenses.filter { it.expenseDate == yesterday }
        DateFilterOption.THIS_MONTH -> allExpenses.filter { it.expenseDate.startsWith(prevMonthPrefix) }
        else -> emptyList()
    }

    val totalSpending = filtered.sumOf { it.amount }
    val previousSpending = previousPeriodExpenses.sumOf { it.amount }
    val percentageChange = if (previousSpending > 0) {
        ((totalSpending - previousSpending) / previousSpending) * 100.0
    } else null

    val transactionCount = filtered.size

    val daysCount = when (params.dateFilter) {
        DateFilterOption.TODAY, DateFilterOption.YESTERDAY -> 1
        DateFilterOption.THIS_WEEK -> Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        DateFilterOption.THIS_MONTH -> Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        DateFilterOption.CUSTOM -> {
            val distinctDates = filtered.map { it.expenseDate }.distinct().size
            if (distinctDates > 0) distinctDates else 1
        }
    }
    val dailyAverage = if (daysCount > 0) totalSpending / daysCount else 0.0

    val categoryMap = filtered.groupBy { it.categoryId }
    val breakdowns = categoryMap.map { (catId, items) ->
        val cat = CategoryConstants.getCategoryById(catId)
        val sum = items.sumOf { it.amount }
        val pct = if (totalSpending > 0) (sum / totalSpending).toFloat() else 0f
        CategorySummary(
            category = cat,
            totalAmount = sum,
            percentage = pct,
            count = items.size
        )
    }.sortedByDescending { it.totalAmount }

    var queryList = filtered
    if (params.searchQuery.isNotBlank()) {
        val q = params.searchQuery.trim().lowercase(Locale.ROOT)
        queryList = queryList.filter {
            it.title.lowercase(Locale.ROOT).contains(q) ||
                it.shopName.lowercase(Locale.ROOT).contains(q) ||
                it.categoryName.lowercase(Locale.ROOT).contains(q) ||
                it.notes.lowercase(Locale.ROOT).contains(q) ||
                it.tags.any { tag -> tag.lowercase(Locale.ROOT).contains(q) }
        }
    }
    if (params.categoryFilter != null) {
        queryList = queryList.filter { it.categoryId.equals(params.categoryFilter, ignoreCase = true) }
    }

    val searchedAndSorted = when (params.sortOption) {
        SortOption.NEWEST_FIRST -> queryList.sortedWith(
            compareByDescending<Expense> { it.expenseDate }
                .thenByDescending { it.expenseTime }
                .thenByDescending { it.createdAt?.toDate()?.time ?: 0L }
        )
        SortOption.OLDEST_FIRST -> queryList.sortedWith(
            compareBy<Expense> { it.expenseDate }
                .thenBy { it.expenseTime }
                .thenBy { it.createdAt?.toDate()?.time ?: 0L }
        )
        SortOption.HIGHEST_AMOUNT -> queryList.sortedByDescending { it.amount }
        SortOption.LOWEST_AMOUNT -> queryList.sortedBy { it.amount }
    }

    val monthlyLimit = profile?.monthlyLimit ?: 20000.0
    val currentMonthTotal = allExpenses
        .filter { it.expenseDate.startsWith(currentMonthPrefix) }
        .sumOf { it.amount }
    val budgetUsagePercent = if (monthlyLimit > 0) ((currentMonthTotal / monthlyLimit) * 100).toFloat() else 0f
    val remainingBudget = maxOf(0.0, monthlyLimit - currentMonthTotal)

    val recent = allExpenses.take(5)

    val insights = mutableListOf<SpendingInsight>()
    if (breakdowns.isNotEmpty()) {
        val topCategory = breakdowns.first()
        val topPct = (topCategory.percentage * 100).toInt()
        insights.add(
            SpendingInsight(
                title = "সর্বোচ্চ খরচের খাত (${topCategory.category.nameBn})",
                description = "আপনার মোট খরচের $topPct% (${String.format(Locale.US, "৳%.0f", topCategory.totalAmount)}) খরচ হয়েছে ${topCategory.category.nameBn}-এ।",
                category = topCategory.category,
                isWarning = topPct >= 40
            )
        )
    }
    if (budgetUsagePercent >= 90) {
        insights.add(
            SpendingInsight(
                title = "বাজেট সীমা সতর্কবার্তা!",
                description = "আপনার চলতি মাসের Spending Limit-এর ${String.format(Locale.US, "%.1f", budgetUsagePercent)}% খরচ হয়ে গেছে। অপ্রয়োজনীয় খরচ পর্যালোচনা করুন।",
                isWarning = true
            )
        )
    } else if (budgetUsagePercent >= 60) {
        insights.add(
            SpendingInsight(
                title = "বাজেট অগ্রগতির তথ্য",
                description = "চলতি মাসে Spending Limit-এর ${String.format(Locale.US, "%.1f", budgetUsagePercent)}% খরচ হয়েছে। বাকি রয়েছে ৳${String.format(Locale.US, "%.0f", remainingBudget)}।",
                isWarning = false
            )
        )
    }

    if (percentageChange != null && percentageChange > 15) {
        insights.add(
            SpendingInsight(
                title = "ব্যয় বৃদ্ধি পেয়েছে",
                description = "পূর্ববর্তী সময়ের তুলনায় আপনার খরচ ${String.format(Locale.US, "%.1f", percentageChange)}% বৃদ্ধি পেয়েছে।",
                isWarning = true
            )
        )
    } else if (percentageChange != null && percentageChange < -10) {
        insights.add(
            SpendingInsight(
                title = "ব্যয় নিয়ন্ত্রণে রয়েছে",
                description = "পূর্ববর্তী সময়ের তুলনায় আপনার খরচ ${String.format(Locale.US, "%.1f", Math.abs(percentageChange))}% কমেছে। দুর্দান্ত অগ্রগতি!",
                isWarning = false
            )
        )
    }

    if (insights.isEmpty()) {
        insights.add(
            SpendingInsight(
                title = "ব্যয় ট্র্যাকিং শুরু হয়েছে",
                description = "নিয়মিত প্রতিটি খরচ রেকর্ড করুন। আপনার খরচের ধরণ অনুযায়ী এখানে বিশ্লেষণ প্রদর্শিত হবে।",
                isWarning = false
            )
        )
    }

    val dailyItems = allExpenses.filter { it.expenseDate == params.dailyDate }
    val dailySum = dailyItems.sumOf { it.amount }
    val dailyCatMap = dailyItems.groupBy { it.categoryId }
    val dailyBreakdowns = dailyCatMap.map { (catId, items) ->
        val cat = CategoryConstants.getCategoryById(catId)
        val s = items.sumOf { it.amount }
        val p = if (dailySum > 0) (s / dailySum).toFloat() else 0f
        CategorySummary(category = cat, totalAmount = s, percentage = p, count = items.size)
    }.sortedByDescending { it.totalAmount }

    val morningList = mutableListOf<Expense>()
    val afternoonList = mutableListOf<Expense>()
    val eveningList = mutableListOf<Expense>()
    val nightList = mutableListOf<Expense>()

    for (exp in dailyItems) {
        val hour = try {
            exp.expenseTime.substringBefore(":").toInt()
        } catch (e: Exception) {
            12
        }
        when (hour) {
            in 5..11 -> morningList.add(exp)
            in 12..16 -> afternoonList.add(exp)
            in 17..20 -> eveningList.add(exp)
            else -> nightList.add(exp)
        }
    }

    val dailyTimeline = mutableListOf<DailyTimelineGroup>()
    if (morningList.isNotEmpty()) dailyTimeline.add(DailyTimelineGroup("Morning", "সকাল", morningList))
    if (afternoonList.isNotEmpty()) dailyTimeline.add(DailyTimelineGroup("Afternoon", "দুপুর", afternoonList))
    if (eveningList.isNotEmpty()) dailyTimeline.add(DailyTimelineGroup("Evening", "সন্ধ্যা", eveningList))
    if (nightList.isNotEmpty()) dailyTimeline.add(DailyTimelineGroup("Night", "রাত", nightList))

    return ExpenseScreenState(
        isLoading = false,
        userProfile = profile,
        allExpenses = allExpenses,
        filteredExpenses = filtered,
        searchedAndSortedExpenses = searchedAndSorted,
        selectedDateFilter = params.dateFilter,
        searchQuery = params.searchQuery,
        selectedCategoryFilter = params.categoryFilter,
        selectedSortOption = params.sortOption,
        totalSpending = totalSpending,
        previousPeriodSpending = previousSpending,
        percentageChange = percentageChange,
        transactionCount = transactionCount,
        dailyAverageExpense = dailyAverage,
        categoryBreakdowns = breakdowns,
        monthlyLimit = monthlyLimit,
        budgetUsagePercent = budgetUsagePercent,
        remainingBudget = remainingBudget,
        recentExpenses = recent,
        smartInsights = insights,
        selectedDailyDate = params.dailyDate,
        dailyTotalSpending = dailySum,
        dailyExpenses = dailyItems,
        dailyCategoryBreakdowns = dailyBreakdowns,
        dailyTimeline = dailyTimeline,
        isAddExpenseDialogOpen = params.dialogState.isAddOpen,
        selectedExpenseForDetails = params.dialogState.detailsExpense,
        isEditDialogOpen = params.dialogState.isEdit,
        isDeleteDialogOpen = params.dialogState.isDelete,
        userMessage = params.dialogState.message
    )
}
