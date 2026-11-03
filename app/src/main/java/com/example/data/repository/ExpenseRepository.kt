package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.Expense
import com.example.data.model.UserProfile
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ExpenseRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    /**
     * Realtime flow of all expenses for the current user, ordered by expenseDate descending.
     */
    fun observeExpenses(): Flow<List<Expense>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/expenses"
        emitAll(
            db.collection("users").document(uid).collection("expenses")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Expense::class.java).sortedWith(
                        compareByDescending<Expense> { it.expenseDate }
                            .thenByDescending { it.expenseTime }
                            .thenByDescending { it.createdAt?.toDate()?.time ?: 0L }
                    )
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    /**
     * Realtime flow of user profile information.
     */
    fun observeUserProfile(): Flow<UserProfile?> = flow {
        val uid = requireUserId()
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(UserProfile::class.java)
                    } else {
                        null
                    }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.READ, path)
                    throw error
                }
        )
    }

    /**
     * Creates or updates the user profile on login.
     */
    suspend fun ensureUserProfile(displayName: String, email: String): Result<UserProfile> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        return try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                val profile = snapshot.toObject(UserProfile::class.java)
                    ?: UserProfile(userId = uid, displayName = displayName, email = email)
                Result.success(profile)
            } else {
                val newProfile = UserProfile(
                    userId = uid,
                    displayName = displayName.ifBlank { "User" },
                    email = email,
                    monthlyLimit = 20000.0,
                    currency = "BDT",
                    language = "bn"
                )
                val payload = hashMapOf<String, Any>(
                    "userId" to uid,
                    "displayName" to newProfile.displayName,
                    "email" to newProfile.email,
                    "monthlyLimit" to newProfile.monthlyLimit,
                    "currency" to newProfile.currency,
                    "language" to newProfile.language,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
                Result.success(newProfile)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Updates profile preferences like spending limit, name, or language.
     */
    suspend fun updateUserProfile(
        displayName: String? = null,
        monthlyLimit: Double? = null,
        language: String? = null
    ): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val updates = mutableMapOf<String, Any>(
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (displayName != null) updates["displayName"] = displayName
        if (monthlyLimit != null) updates["monthlyLimit"] = monthlyLimit
        if (language != null) updates["language"] = language

        return try {
            docRef.set(updates, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateCategoryBudgets(categoryBudgets: Map<String, Double>): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        return try {
            val updates = mapOf(
                "categoryBudgets" to categoryBudgets,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(updates, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateCustomTags(tags: List<String>): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        return try {
            val updates = mapOf(
                "customTags" to tags,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(updates, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateBudgetNotificationSettings(
        w50: Boolean,
        w75: Boolean,
        w90: Boolean,
        w100: Boolean
    ): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        return try {
            val updates = mapOf(
                "budgetWarning50" to w50,
                "budgetWarning75" to w75,
                "budgetWarning90" to w90,
                "budgetWarning100" to w100,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(updates, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Adds a new expense record under the authenticated user.
     */
    suspend fun addExpense(expense: Expense): Result<String> {
        val uid = requireUserId()
        val collRef = db.collection("users").document(uid).collection("expenses")
        return try {
            val participantsList = expense.splitParticipants.map {
                hashMapOf<String, Any>(
                    "name" to it.name,
                    "amount" to it.amount,
                    "avatarInitial" to it.avatarInitial
                )
            }
            val payload = hashMapOf<String, Any>(
                "userId" to uid,
                "amount" to expense.amount,
                "title" to expense.title.trim(),
                "categoryId" to expense.categoryId,
                "categoryName" to expense.categoryName,
                "shopName" to expense.shopName.trim(),
                "paymentMethod" to expense.paymentMethod,
                "expenseDate" to expense.expenseDate,
                "expenseTime" to expense.expenseTime,
                "notes" to expense.notes.trim(),
                "tags" to expense.tags,
                "isSplit" to expense.isSplit,
                "splitParticipants" to participantsList,
                "splitType" to expense.splitType,
                "splitTotalBill" to expense.splitTotalBill,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val doc = collRef.add(payload).await()
            Result.success(doc.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, collRef.path)
            Result.failure(e)
        }
    }

    /**
     * Updates an existing expense.
     */
    suspend fun updateExpense(expense: Expense): Result<Unit> {
        val uid = requireUserId()
        require(expense.id.isNotBlank()) { "Expense ID cannot be blank for update" }
        val docRef = db.collection("users").document(uid).collection("expenses").document(expense.id)
        return try {
            val participantsList = expense.splitParticipants.map {
                hashMapOf<String, Any>(
                    "name" to it.name,
                    "amount" to it.amount,
                    "avatarInitial" to it.avatarInitial
                )
            }
            val payload = hashMapOf<String, Any>(
                "userId" to uid,
                "amount" to expense.amount,
                "title" to expense.title.trim(),
                "categoryId" to expense.categoryId,
                "categoryName" to expense.categoryName,
                "shopName" to expense.shopName.trim(),
                "paymentMethod" to expense.paymentMethod,
                "expenseDate" to expense.expenseDate,
                "expenseTime" to expense.expenseTime,
                "notes" to expense.notes.trim(),
                "tags" to expense.tags,
                "isSplit" to expense.isSplit,
                "splitParticipants" to participantsList,
                "splitType" to expense.splitType,
                "splitTotalBill" to expense.splitTotalBill,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.update(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Deletes an expense.
     */
    suspend fun deleteExpense(expenseId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("expenses").document(expenseId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }
}
