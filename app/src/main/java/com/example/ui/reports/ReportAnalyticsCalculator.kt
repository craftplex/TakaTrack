package com.example.ui.reports

import com.example.data.model.CategoryConstants
import com.example.data.model.Expense
import com.example.ui.CategorySummary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object ReportAnalyticsCalculator {

    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun calculateDailyReport(allExpenses: List<Expense>): DailyReportData {
        val today = sdf.format(Date())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = sdf.format(cal.time)

        val todayExpenses = allExpenses.filter { it.expenseDate == today }
        val yesterdayExpenses = allExpenses.filter { it.expenseDate == yesterday }

        val todayTotal = todayExpenses.sumOf { it.amount }
        val yesterdayTotal = yesterdayExpenses.sumOf { it.amount }
        val diff = todayTotal - yesterdayTotal
        val pctChange = if (yesterdayTotal > 0) ((diff / yesterdayTotal) * 100.0) else null

        val count = todayExpenses.size
        val avg = if (count > 0) todayTotal / count else 0.0

        val categoryGroups = todayExpenses.groupBy { it.categoryId }
        val topCategory = categoryGroups.maxByOrNull { entry -> entry.value.sumOf { it.amount } }?.let { (catId, list) ->
            val cat = CategoryConstants.getCategoryById(catId)
            val sum = list.sumOf { it.amount }
            CategorySummary(
                category = cat,
                totalAmount = sum,
                percentage = if (todayTotal > 0) (sum / todayTotal).toFloat() else 0f,
                count = list.size
            )
        }

        return DailyReportData(
            todayTotal = todayTotal,
            yesterdayTotal = yesterdayTotal,
            difference = diff,
            percentageChange = pctChange,
            transactionCount = count,
            averageExpense = avg,
            highestCategory = topCategory,
            expenses = todayExpenses
        )
    }

    fun calculateWeeklyReport(allExpenses: List<Expense>): WeeklyReportData {
        val dayNamesEn = listOf("Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri")
        val dayNamesBn = listOf("শনিবার", "রবিবার", "সোমবার", "মঙ্গলবার", "বুধবার", "বৃহস্পতিবার", "শুক্রবার")

        // Last 7 days
        val daysList = mutableListOf<DayBarData>()
        val calIter = Calendar.getInstance()
        calIter.time = Date()
        calIter.add(Calendar.DAY_OF_YEAR, -6)

        for (i in 0..6) {
            val dateStr = sdf.format(calIter.time)
            val dayOfWeek = calIter.get(Calendar.DAY_OF_WEEK) // 1=Sun, 7=Sat
            val dayNameIndex = when (dayOfWeek) {
                Calendar.SATURDAY -> 0
                Calendar.SUNDAY -> 1
                Calendar.MONDAY -> 2
                Calendar.TUESDAY -> 3
                Calendar.WEDNESDAY -> 4
                Calendar.THURSDAY -> 5
                Calendar.FRIDAY -> 6
                else -> 0
            }

            val amount = allExpenses.filter { it.expenseDate == dateStr }.sumOf { it.amount }
            daysList.add(
                DayBarData(
                    dayNameEn = dayNamesEn[dayNameIndex],
                    dayNameBn = dayNamesBn[dayNameIndex],
                    dateString = dateStr,
                    amount = amount
                )
            )
            calIter.add(Calendar.DAY_OF_YEAR, 1)
        }

        val maxAmount = daysList.maxOfOrNull { it.amount } ?: 0.0
        val dayBars = daysList.map {
            it.copy(isPeak = maxAmount > 0 && it.amount == maxAmount)
        }

        val total = dayBars.sumOf { it.amount }
        val peakDay = dayBars.firstOrNull { it.isPeak }
        val lowestDay = dayBars.filter { it.amount > 0 }.minByOrNull { it.amount } ?: dayBars.firstOrNull()

        val weekDates = dayBars.map { it.dateString }.toSet()
        val weekExpenses = allExpenses.filter { it.expenseDate in weekDates }
        val catMap = weekExpenses.groupBy { it.categoryId }
        val topCategories = catMap.map { (catId, list) ->
            val cat = CategoryConstants.getCategoryById(catId)
            val sum = list.sumOf { it.amount }
            CategorySummary(
                category = cat,
                totalAmount = sum,
                percentage = if (total > 0) (sum / total).toFloat() else 0f,
                count = list.size
            )
        }.sortedByDescending { it.totalAmount }

        val payMap = weekExpenses.groupBy { it.paymentMethod }
        val paymentBreakdowns = payMap.map { (method, list) ->
            val sum = list.sumOf { it.amount }
            PaymentMethodSummary(
                paymentMethod = method,
                totalAmount = sum,
                percentage = if (total > 0) (sum / total).toFloat() else 0f,
                count = list.size
            )
        }.sortedByDescending { it.totalAmount }

        return WeeklyReportData(
            weeklyTotal = total,
            dayBars = dayBars,
            mostExpensiveDay = peakDay,
            lowestSpendingDay = lowestDay,
            weeklyAverage = total / 7.0,
            topCategories = topCategories,
            paymentBreakdowns = paymentBreakdowns
        )
    }

    fun calculateMonthlyReport(allExpenses: List<Expense>, monthlyLimit: Double): MonthlyReportData {
        val cal = Calendar.getInstance()
        cal.time = Date()
        val currentYear = cal.get(Calendar.YEAR)
        val currentMonth = cal.get(Calendar.MONTH) // 0-based
        val currentMonthPrefix = String.format(Locale.US, "%04d-%02d", currentYear, currentMonth + 1)

        val prevMonthPrefix = if (currentMonth == 0) {
            String.format(Locale.US, "%04d-12", currentYear - 1)
        } else {
            String.format(Locale.US, "%04d-%02d", currentYear, currentMonth)
        }

        val currentMonthExpenses = allExpenses.filter { it.expenseDate.startsWith(currentMonthPrefix) }
        val prevMonthExpenses = allExpenses.filter { it.expenseDate.startsWith(prevMonthPrefix) }

        val currentTotal = currentMonthExpenses.sumOf { it.amount }
        val prevTotal = prevMonthExpenses.sumOf { it.amount }
        val diff = currentTotal - prevTotal
        val pctChange = if (prevTotal > 0) ((diff / prevTotal) * 100.0) else null

        val daysInMonth = cal.get(Calendar.DAY_OF_MONTH)
        val dailyAvg = if (daysInMonth > 0) currentTotal / daysInMonth else 0.0

        val unusedBudget = maxOf(0.0, monthlyLimit - currentTotal)
        val budgetUsagePct = if (monthlyLimit > 0) ((currentTotal / monthlyLimit) * 100).toFloat() else 0f

        // Category breakdown
        val catMap = currentMonthExpenses.groupBy { it.categoryId }
        val categories = catMap.map { (catId, list) ->
            val cat = CategoryConstants.getCategoryById(catId)
            val s = list.sumOf { it.amount }
            CategorySummary(
                category = cat,
                totalAmount = s,
                percentage = if (currentTotal > 0) (s / currentTotal).toFloat() else 0f,
                count = list.size
            )
        }.sortedByDescending { it.totalAmount }

        // Shop-wise breakdown
        val shopMap = currentMonthExpenses.filter { it.shopName.isNotBlank() }.groupBy { it.shopName }
        val shops = shopMap.map { (name, list) ->
            ShopExpenseSummary(
                shopName = name,
                totalAmount = list.sumOf { it.amount },
                count = list.size
            )
        }.sortedByDescending { it.totalAmount }

        // Payment method breakdown
        val payMap = currentMonthExpenses.groupBy { it.paymentMethod }
        val paymentBreakdowns = payMap.map { (method, list) ->
            val sum = list.sumOf { it.amount }
            PaymentMethodSummary(
                paymentMethod = method,
                totalAmount = sum,
                percentage = if (currentTotal > 0) (sum / currentTotal).toFloat() else 0f,
                count = list.size
            )
        }.sortedByDescending { it.totalAmount }

        // Unusual / Unnecessary spending detection
        val unusualItems = mutableListOf<UnusualSpendingItem>()
        val largeExpenses = currentMonthExpenses.filter { it.amount >= 3000 || (currentTotal > 0 && it.amount > currentTotal * 0.3) }
        for (item in largeExpenses.take(2)) {
            unusualItems.add(
                UnusualSpendingItem(
                    title = "বড় অংকের একক খরচ (${item.title})",
                    reason = "একক লেনদেনে ৳${item.amount.toInt()} খরচ হয়েছে। অপ্রয়োজনীয় হলে পর্যালোচনা করুন।",
                    amount = item.amount,
                    category = CategoryConstants.getCategoryById(item.categoryId)
                )
            )
        }

        val shopDayGroups = currentMonthExpenses.filter { it.shopName.isNotBlank() }
            .groupBy { "${it.shopName}_${it.expenseDate}" }
            .filter { it.value.size >= 2 }
        for ((_, group) in shopDayGroups.entries.take(1)) {
            val s = group.first().shopName
            val sum = group.sumOf { it.amount }
            unusualItems.add(
                UnusualSpendingItem(
                    title = "একই দিনে একই দোকানে একাধিক খরচ ($s)",
                    reason = "একই দিনে $s-এ ${group.size}টি লেনদেনে মোট ৳${sum.toInt()} ব্যয় হয়েছে।",
                    amount = sum
                )
            )
        }

        val entSummary = categories.firstOrNull { it.category.id == "entertainment" }
        if (entSummary != null && entSummary.percentage >= 0.25f) {
            unusualItems.add(
                UnusualSpendingItem(
                    title = "বিনোদন খরচ স্বাভাবিকের চেয়ে বেশি",
                    reason = "আপনার মোট ব্যয়ের ${(entSummary.percentage * 100).toInt()}% খরচ হয়েছে বিনোদনে।",
                    amount = entSummary.totalAmount,
                    category = entSummary.category
                )
            )
        }

        return MonthlyReportData(
            currentMonthTotal = currentTotal,
            previousMonthTotal = prevTotal,
            difference = diff,
            percentageChange = pctChange,
            transactionCount = currentMonthExpenses.size,
            dailyAverage = dailyAvg,
            monthlyLimit = monthlyLimit,
            unusedBudget = unusedBudget,
            budgetUsagePercent = budgetUsagePct,
            categoryBreakdowns = categories,
            shopBreakdowns = shops,
            paymentBreakdowns = paymentBreakdowns,
            unusualSpendings = unusualItems
        )
    }

    fun calculateYearlyReport(allExpenses: List<Expense>, monthlyLimit: Double): YearlyReportData {
        val cal = Calendar.getInstance()
        val currentYear = cal.get(Calendar.YEAR)
        val yearPrefix = "$currentYear"

        val yearExpenses = allExpenses.filter { it.expenseDate.startsWith(yearPrefix) }
        val annualTotal = yearExpenses.sumOf { it.amount }

        val monthNamesEn = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val monthNamesBn = listOf("জানু", "ফেব্রু", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টে", "অক্টো", "নভে", "ডিসে")

        val monthBars = (1..12).map { month ->
            val mStr = String.format(Locale.US, "%04d-%02d", currentYear, month)
            val sum = yearExpenses.filter { it.expenseDate.startsWith(mStr) }.sumOf { it.amount }
            MonthBarData(
                monthNumber = month,
                monthNameEn = monthNamesEn[month - 1],
                monthNameBn = monthNamesBn[month - 1],
                amount = sum
            )
        }

        val maxMonthAmount = monthBars.maxOfOrNull { it.amount } ?: 0.0
        val updatedBars = monthBars.map {
            it.copy(isPeak = maxMonthAmount > 0 && it.amount == maxMonthAmount)
        }

        val highestMonth = updatedBars.firstOrNull { it.isPeak }
        val lowestMonth = updatedBars.filter { it.amount > 0 }.minByOrNull { it.amount } ?: updatedBars.firstOrNull()

        val catMap = yearExpenses.groupBy { it.categoryId }
        val topCategory = catMap.map { (catId, list) ->
            val cat = CategoryConstants.getCategoryById(catId)
            val sum = list.sumOf { it.amount }
            CategorySummary(
                category = cat,
                totalAmount = sum,
                percentage = if (annualTotal > 0) (sum / annualTotal).toFloat() else 0f,
                count = list.size
            )
        }.maxByOrNull { it.totalAmount }

        val mostFrequentShop = yearExpenses.filter { it.shopName.isNotBlank() }
            .groupBy { it.shopName }
            .maxByOrNull { it.value.size }
            ?.key

        val payMap = yearExpenses.groupBy { it.paymentMethod }
        val paymentBreakdowns = payMap.map { (method, list) ->
            val sum = list.sumOf { it.amount }
            PaymentMethodSummary(
                paymentMethod = method,
                totalAmount = sum,
                percentage = if (annualTotal > 0) (sum / annualTotal).toFloat() else 0f,
                count = list.size
            )
        }.sortedByDescending { it.totalAmount }

        val trends = mutableListOf<String>()
        if (topCategory != null) {
            trends.add("এই বছর আপনার প্রধান ব্যয়ের খাত ছিল ${topCategory.category.nameBn} (৳${topCategory.totalAmount.toInt()})।")
        }
        if (highestMonth != null && highestMonth.amount > 0) {
            trends.add("${highestMonth.monthNameBn} মাসে আপনার সর্বোচ্চ খরচ হয়েছিল (৳${highestMonth.amount.toInt()})।")
        }
        trends.add("কোনো আয় বা ব্যাংকিং হিসাব ছাড়াই TakaTrack আপনার প্রতিটি খরচের স্বচ্ছ রেকর্ড রক্ষা করেছে।")

        return YearlyReportData(
            annualTotal = annualTotal,
            monthBars = updatedBars,
            highestMonth = highestMonth,
            lowestMonth = lowestMonth,
            annualLimit = monthlyLimit * 12,
            highestCategory = topCategory,
            mostFrequentShop = mostFrequentShop,
            paymentBreakdowns = paymentBreakdowns,
            trends = trends
        )
    }

    fun calculateComparison(
        allExpenses: List<Expense>,
        preset: String = "last_7_days"
    ): ComparisonReportData {
        val cal = Calendar.getInstance()
        cal.time = Date()

        val periodAExpenses: List<Expense>
        val periodBExpenses: List<Expense>
        val labelA: String
        val labelB: String

        when (preset) {
            "this_month_vs_last" -> {
                val currentYear = cal.get(Calendar.YEAR)
                val currentMonth = cal.get(Calendar.MONTH)
                val curPrefix = String.format(Locale.US, "%04d-%02d", currentYear, currentMonth + 1)
                val prevPrefix = if (currentMonth == 0) {
                    String.format(Locale.US, "%04d-12", currentYear - 1)
                } else {
                    String.format(Locale.US, "%04d-%02d", currentYear, currentMonth)
                }
                periodAExpenses = allExpenses.filter { it.expenseDate.startsWith(curPrefix) }
                periodBExpenses = allExpenses.filter { it.expenseDate.startsWith(prevPrefix) }
                labelA = "This Month (এই মাস)"
                labelB = "Previous Month (গত মাস)"
            }
            "last_30_days" -> {
                val cal30 = Calendar.getInstance()
                cal30.add(Calendar.DAY_OF_YEAR, -30)
                val d30 = sdf.format(cal30.time)
                cal30.add(Calendar.DAY_OF_YEAR, -30)
                val d60 = sdf.format(cal30.time)

                periodAExpenses = allExpenses.filter { it.expenseDate >= d30 }
                periodBExpenses = allExpenses.filter { it.expenseDate in d60..<d30 }
                labelA = "Last 30 Days (গত ৩০ দিন)"
                labelB = "Previous 30 Days (পূর্বের ৩০ দিন)"
            }
            else -> {
                val cal7 = Calendar.getInstance()
                cal7.add(Calendar.DAY_OF_YEAR, -7)
                val d7 = sdf.format(cal7.time)
                cal7.add(Calendar.DAY_OF_YEAR, -7)
                val d14 = sdf.format(cal7.time)

                periodAExpenses = allExpenses.filter { it.expenseDate >= d7 }
                periodBExpenses = allExpenses.filter { it.expenseDate in d14..<d7 }
                labelA = "Last 7 Days (গত ৭ দিন)"
                labelB = "Previous 7 Days (পূর্বের ৭ দিন)"
            }
        }

        val totalA = periodAExpenses.sumOf { it.amount }
        val totalB = periodBExpenses.sumOf { it.amount }
        val diff = totalA - totalB
        val pctChange = if (totalB > 0) ((diff / totalB) * 100.0) else null

        val days = if (preset == "last_30_days" || preset == "this_month_vs_last") 30 else 7
        val avgA = totalA / days
        val avgB = totalB / days

        val allCatIds = (periodAExpenses.map { it.categoryId } + periodBExpenses.map { it.categoryId }).distinct()
        val catDiffs = allCatIds.map { catId ->
            val cat = CategoryConstants.getCategoryById(catId)
            val sumA = periodAExpenses.filter { it.categoryId == catId }.sumOf { it.amount }
            val sumB = periodBExpenses.filter { it.categoryId == catId }.sumOf { it.amount }
            ComparisonCategoryDiff(
                category = cat,
                periodAAmount = sumA,
                periodBAmount = sumB,
                difference = sumA - sumB
            )
        }.sortedByDescending { it.difference }

        val topIncreased = catDiffs.firstOrNull { it.difference > 0 }?.category

        return ComparisonReportData(
            periodALabel = labelA,
            periodBLabel = labelB,
            periodATotal = totalA,
            periodBTotal = totalB,
            difference = diff,
            percentageChange = pctChange,
            periodADailyAvg = avgA,
            periodBDailyAvg = avgB,
            dailyAvgDifference = avgA - avgB,
            categoryDiffs = catDiffs,
            topIncreasedCategory = topIncreased
        )
    }

    fun calculateSpendingPatterns(allExpenses: List<Expense>): SpendingPatternData {
        val total = allExpenses.sumOf { it.amount }
        val days = allExpenses.map { it.expenseDate }.distinct().size.coerceAtLeast(1)
        val dailyAvg = total / days
        val weeklyAvg = dailyAvg * 7.0

        var weekdaySum = 0.0
        var weekendSum = 0.0
        var morningSum = 0.0
        var eveningSum = 0.0

        for (exp in allExpenses) {
            try {
                val d = sdf.parse(exp.expenseDate)
                if (d != null) {
                    val cal = Calendar.getInstance()
                    cal.time = d
                    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                    if (dayOfWeek == Calendar.FRIDAY || dayOfWeek == Calendar.SATURDAY) {
                        weekendSum += exp.amount
                    } else {
                        weekdaySum += exp.amount
                    }
                }
            } catch (e: Exception) {}

            val hour = try { exp.expenseTime.substringBefore(":").toInt() } catch (e: Exception) { 12 }
            if (hour in 5..14) {
                morningSum += exp.amount
            } else {
                eveningSum += exp.amount
            }
        }

        val dayGroup = allExpenses.groupBy {
            try {
                val d = sdf.parse(it.expenseDate)
                val c = Calendar.getInstance().apply { time = d!! }
                when (c.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.FRIDAY -> "Friday (শুক্রবার)"
                    Calendar.SATURDAY -> "Saturday (শনিবার)"
                    Calendar.SUNDAY -> "Sunday (রবিবার)"
                    Calendar.MONDAY -> "Monday (সোমবার)"
                    Calendar.TUESDAY -> "Tuesday (মঙ্গলবার)"
                    Calendar.WEDNESDAY -> "Wednesday (বুধবার)"
                    else -> "Thursday (বৃহস্পতিবার)"
                }
            } catch (e: Exception) {
                "Friday (শুক্রবার)"
            }
        }
        val peakDay = dayGroup.maxByOrNull { it.value.sumOf { exp -> exp.amount } }?.key ?: "Friday (শুক্রবার)"

        val topCat = allExpenses.groupBy { it.categoryId }.maxByOrNull { it.value.sumOf { exp -> exp.amount } }
        val topCatName = if (topCat != null) CategoryConstants.getCategoryById(topCat.key).nameBn else "Grocery (মুদি)"

        val tips = listOf(
            "Grocery কেনাকাটার জন্য একটি নির্দিষ্ট মাসিক লক্ষ্য ঠিক করুন।",
            "বাইরে খাওয়ার খরচ কমাতে সাপ্তাহিক বাজেট নির্ধারণ করুন।",
            "একই দোকানে ঘন ঘন বড় খরচের ক্ষেত্রে পূর্ববর্তী বিল পর্যালোচনা করুন।"
        )

        return SpendingPatternData(
            dailyAverage = dailyAvg,
            weeklyAverage = weeklyAvg,
            highestSpendingDayName = peakDay,
            highestCategoryName = topCatName,
            weekdayTotal = weekdaySum,
            weekendTotal = weekendSum,
            morningTotal = morningSum,
            eveningTotal = eveningSum,
            recurringTips = tips
        )
    }
}
