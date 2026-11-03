package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Expense
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ExpenseRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun addExpense_authenticatedUser_createsSuccessfully() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = ExpenseRepository(firestore)

        val expense = Expense(
            userId = uid,
            amount = 1200.0,
            title = "Monthly Grocery",
            categoryId = "grocery",
            categoryName = "Grocery",
            paymentMethod = "Cash",
            expenseDate = "2026-11-03",
            expenseTime = "14:00"
        )

        val result = withTimeout(TIMEOUT_MS) {
            repository.addExpense(expense)
        }
        assertTrue(result.isSuccess)
        val expenseId = result.getOrThrow()
        assertNotNull(expenseId)

        val expenses = withTimeout(TIMEOUT_MS) {
            repository.observeExpenses().first { list -> list.any { it.id == expenseId } }
        }
        assertTrue(expenses.any { it.id == expenseId && it.amount == 1200.0 })
    }

    @Test
    fun updateExpense_authenticatedOwner_updatesSuccessfully() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = ExpenseRepository(firestore)

        val expense = Expense(
            userId = uid,
            amount = 350.0,
            title = "Lunch",
            categoryId = "food",
            categoryName = "Food & Dining",
            paymentMethod = "Cash",
            expenseDate = "2026-11-03"
        )
        val id = repository.addExpense(expense).getOrThrow()

        val updatedExpense = expense.copy(id = id, amount = 420.0, title = "Lunch with Team")
        val updateResult = repository.updateExpense(updatedExpense)
        assertTrue(updateResult.isSuccess)

        val observed = withTimeout(TIMEOUT_MS) {
            repository.observeExpenses().first { list -> list.any { it.id == id && it.amount == 420.0 } }
        }
        assertEquals(420.0, observed.first { it.id == id }.amount, 0.01)
    }

    @Test
    fun deleteExpense_authenticatedOwner_removesDocument() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = ExpenseRepository(firestore)

        val expense = Expense(
            userId = uid,
            amount = 50.0,
            title = "Tea",
            categoryId = "food",
            categoryName = "Food & Dining",
            paymentMethod = "Cash",
            expenseDate = "2026-11-03"
        )
        val id = repository.addExpense(expense).getOrThrow()

        val deleteResult = repository.deleteExpense(id)
        assertTrue(deleteResult.isSuccess)

        val listAfter = withTimeout(TIMEOUT_MS) {
            repository.observeExpenses().first { list -> list.none { it.id == id } }
        }
        assertTrue(listAfter.none { it.id == id })
    }

    @Test
    fun observeExpenses_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = ExpenseRepository(firestore)

        try {
            withTimeout(3000L) {
                repository.observeExpenses().first()
            }
            fail("Expected exception when unauthenticated")
        } catch (e: Exception) {
            assertTrue(e is IllegalStateException || e.unwrapFirestoreException()?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice_rules@example.com"
        const val TIMEOUT_MS = 6000L
    }
}
