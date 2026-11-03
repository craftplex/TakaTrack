package com.example.util

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreException
import org.json.JSONObject

enum class OperationType {
    CREATE,
    READ,
    UPDATE,
    DELETE,
    LIST
}

private const val TAG = "FirestoreError"

fun handleFirestoreError(exception: Exception, operationType: OperationType, path: String) {
    val errorReport = JSONObject().apply {
        put("operation", operationType.name)
        put("targetPath", path)
        put("errorType", exception.javaClass.simpleName)
        put("message", exception.localizedMessage ?: "Unknown error")
        if (exception is FirebaseFirestoreException) {
            put("firestoreErrorCode", exception.code.name)
        }
    }
    Log.e(TAG, "Firestore error encountered: $errorReport", exception)
}
