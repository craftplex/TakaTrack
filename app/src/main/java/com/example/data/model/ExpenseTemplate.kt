package com.example.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class ExpenseTemplate(
    @DocumentId val id: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val categoryId: String = "food",
    val paymentMethod: String = "Cash (নগদ)",
    val shopName: String = "",
    val notes: String = "",
    val tags: List<String> = emptyList(),
    val iconName: String = "flash"
) {
    companion object {
        val DefaultTemplates = listOf(
            ExpenseTemplate(
                id = "default_1",
                title = "Rickshaw Fare (রিকশা ভাড়া)",
                amount = 50.0,
                categoryId = "transport",
                paymentMethod = "Cash (নগদ)",
                notes = "Daily Rickshaw",
                tags = listOf("Personal")
            ),
            ExpenseTemplate(
                id = "default_2",
                title = "Tea & Toast (চা-বিস্কুট)",
                amount = 35.0,
                categoryId = "food",
                paymentMethod = "Cash (নগদ)",
                notes = "Morning Tea",
                tags = listOf("Personal")
            ),
            ExpenseTemplate(
                id = "default_3",
                title = "Daily Grocery (দৈনিক বাজার)",
                amount = 350.0,
                categoryId = "grocery",
                paymentMethod = "Cash (নগদ)",
                shopName = "Kacha Bazar",
                tags = listOf("Family")
            ),
            ExpenseTemplate(
                id = "default_4",
                title = "Mobile Recharge (মোবাইল রিচার্জ)",
                amount = 100.0,
                categoryId = "bills",
                paymentMethod = "bKash (বিকাশ)",
                tags = listOf("Personal")
            ),
            ExpenseTemplate(
                id = "default_5",
                title = "Lunch Meal (দুপুরের খাবার)",
                amount = 120.0,
                categoryId = "food",
                paymentMethod = "Cash (নগদ)",
                tags = listOf("Office")
            ),
            ExpenseTemplate(
                id = "default_6",
                title = "Monthly House Rent (বাসা ভাড়া)",
                amount = 15000.0,
                categoryId = "bills",
                paymentMethod = "Bank Transfer (ব্যাংক ট্রান্সফার)",
                tags = listOf("Family")
            )
        )
    }
}
