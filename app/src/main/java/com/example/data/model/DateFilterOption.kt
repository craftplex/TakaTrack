package com.example.data.model

enum class DateFilterOption(val labelEn: String, val labelBn: String) {
    TODAY("Today", "আজ"),
    YESTERDAY("Yesterday", "গতকাল"),
    THIS_WEEK("This Week", "এই সপ্তাহ"),
    THIS_MONTH("This Month", "এই মাস"),
    CUSTOM("Custom", "কাস্টম")
}

enum class SortOption(val labelEn: String, val labelBn: String) {
    NEWEST_FIRST("Newest First", "নতুন আগে"),
    OLDEST_FIRST("Oldest First", "পুরনো আগে"),
    HIGHEST_AMOUNT("Highest Amount", "সর্বোচ্চ খরচ"),
    LOWEST_AMOUNT("Lowest Amount", "সর্বনিম্ন খরচ")
}
