package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.ExpenseRepository
import com.example.ui.MainAppContent
import com.example.ui.ExpenseViewModel
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.AuthViewModel
import com.example.ui.insights.AiInsightsViewModel
import com.example.ui.theme.TakaTrackTheme
import com.example.util.configureAppCheck
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Step 1: Configure App Check with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            TakaTrackTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(activity = this)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(activity: ComponentActivity) {
    val authViewModel: AuthViewModel = viewModel()
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    // Attempt silent auto-sign in on launch
    LaunchedEffect(Unit) {
        authViewModel.attemptSilentSignIn(activity)
    }

    // Observe auth state changes
    androidx.compose.runtime.DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    val repository = remember { ExpenseRepository(activity) }

    if (currentUser == null) {
        AuthScreen(
            authViewModel = authViewModel,
            repository = repository
        )
    } else {
        // Authenticated user session: instantiate feature ViewModels
        val expenseViewModel = remember(currentUser!!.uid) {
            ExpenseViewModel(repository)
        }
        val aiViewModel: AiInsightsViewModel = viewModel()

        MainAppContent(
            repository = repository,
            authViewModel = authViewModel,
            expenseViewModel = expenseViewModel,
            aiViewModel = aiViewModel
        )
    }
}
