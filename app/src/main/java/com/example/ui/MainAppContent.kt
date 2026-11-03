package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.ExpenseRepository
import com.example.ui.auth.AuthViewModel
import com.example.ui.daily.DailyViewScreen
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.expenses.AddExpenseDialog
import com.example.ui.expenses.ExpenseDetailsDialog
import com.example.ui.expenses.ExpensesListScreen
import com.example.ui.insights.AiInsightsScreen
import com.example.ui.insights.AiInsightsViewModel
import com.example.ui.profile.ProfileScreen
import com.example.ui.theme.LightBlue
import com.example.ui.theme.PrimaryBlue

enum class NavTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    EXPENSES("Expenses", Icons.AutoMirrored.Filled.ReceiptLong),
    DAILY("Daily View", Icons.Default.CalendarMonth),
    AI_ADVISOR("AI Advisor", Icons.Default.AutoAwesome),
    SETTINGS("Settings", Icons.Default.Settings)
}

@Composable
fun MainAppContent(
    repository: ExpenseRepository,
    authViewModel: AuthViewModel,
    expenseViewModel: ExpenseViewModel,
    aiViewModel: AiInsightsViewModel,
    modifier: Modifier = Modifier
) {
    val state by expenseViewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            expenseViewModel.clearUserMessage()
        }
    }

    val showFab = selectedTabIndex == 0 || selectedTabIndex == 1 || selectedTabIndex == 2

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                NavTab.values().forEachIndexed { index, tab ->
                    val isSelected = selectedTabIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTabIndex = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryBlue,
                            selectedTextColor = PrimaryBlue,
                            indicatorColor = LightBlue,
                            unselectedIconColor = MaterialTheme.colorScheme.secondary,
                            unselectedTextColor = MaterialTheme.colorScheme.secondary
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = { expenseViewModel.openAddExpenseDialog() },
                    shape = CircleShape,
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                    modifier = Modifier.testTag("add_expense_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Expense",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTabIndex) {
                0 -> DashboardScreen(
                    state = state,
                    viewModel = expenseViewModel,
                    onNavigateToExpenses = { selectedTabIndex = 1 },
                    onNavigateToDaily = { selectedTabIndex = 2 },
                    onNavigateToProfile = { selectedTabIndex = 4 }
                )
                1 -> ExpensesListScreen(
                    state = state,
                    viewModel = expenseViewModel
                )
                2 -> DailyViewScreen(
                    state = state,
                    viewModel = expenseViewModel
                )
                3 -> AiInsightsScreen(
                    state = state,
                    aiViewModel = aiViewModel
                )
                4 -> ProfileScreen(
                    state = state,
                    viewModel = expenseViewModel,
                    authViewModel = authViewModel
                )
            }

            // Add Expense Dialog
            if (state.isAddExpenseDialogOpen) {
                AddExpenseDialog(
                    initialExpense = null,
                    onDismiss = { expenseViewModel.closeAddExpenseDialog() },
                    onSave = { amount, title, catId, shop, payment, date, time, notes, tags ->
                        expenseViewModel.addExpense(amount, title, catId, shop, payment, date, time, notes, tags)
                    }
                )
            }

            // Edit Expense Dialog
            if (state.isEditDialogOpen && state.selectedExpenseForDetails != null) {
                AddExpenseDialog(
                    initialExpense = state.selectedExpenseForDetails,
                    onDismiss = { expenseViewModel.closeEditDialog() },
                    onSave = { amount, title, catId, shop, payment, date, time, notes, tags ->
                        val updated = state.selectedExpenseForDetails!!.copy(
                            amount = amount,
                            title = title,
                            categoryId = catId,
                            shopName = shop,
                            paymentMethod = payment,
                            expenseDate = date,
                            expenseTime = time,
                            notes = notes,
                            tags = tags
                        )
                        expenseViewModel.updateExpense(updated)
                    }
                )
            }

            // Expense Details Dialog
            if (state.selectedExpenseForDetails != null && !state.isEditDialogOpen) {
                ExpenseDetailsDialog(
                    expense = state.selectedExpenseForDetails!!,
                    isDeleteConfirmOpen = state.isDeleteDialogOpen,
                    onDismiss = { expenseViewModel.closeExpenseDetails() },
                    onEditClick = { expenseViewModel.openEditDialog(state.selectedExpenseForDetails!!) },
                    onDeleteRequest = { expenseViewModel.requestDeleteConfirmation(state.selectedExpenseForDetails!!) },
                    onDeleteCancel = { expenseViewModel.cancelDeleteConfirmation() },
                    onDeleteConfirm = {
                        expenseViewModel.confirmDeleteExpense(state.selectedExpenseForDetails!!.id)
                    }
                )
            }
        }
    }
}
