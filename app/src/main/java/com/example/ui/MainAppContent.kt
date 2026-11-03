package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import com.example.ui.reports.ReportsScreen
import com.example.ui.theme.LightBlue
import com.example.ui.theme.PrimaryBlue

enum class NavTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    EXPENSES("Expenses", Icons.AutoMirrored.Filled.ReceiptLong),
    DAILY("Daily View", Icons.Default.CalendarMonth),
    REPORTS("Reports", Icons.Default.BarChart),
    SETTINGS("Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
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
    var showAiModal by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            expenseViewModel.clearUserMessage()
        }
    }

    val showFab = selectedTabIndex in 0..3

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
                    onNavigateToReports = { selectedTabIndex = 3 },
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
                3 -> ReportsScreen(
                    state = state,
                    viewModel = expenseViewModel,
                    aiViewModel = aiViewModel,
                    onNavigateToAiAdvisor = { showAiModal = true }
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
                    availableTags = state.customTags,
                    onAddCustomTag = { tag -> expenseViewModel.addCustomTag(tag) },
                    onDismiss = { expenseViewModel.closeAddExpenseDialog() },
                    onSave = { amount, title, catId, shop, payment, date, time, notes, tags, isSplit, participants, splitType, totalBill ->
                        expenseViewModel.addExpense(
                            amount = amount,
                            title = title,
                            categoryId = catId,
                            shopName = shop,
                            paymentMethod = payment,
                            date = date,
                            time = time,
                            notes = notes,
                            tags = tags,
                            isSplit = isSplit,
                            splitParticipants = participants,
                            splitType = splitType,
                            splitTotalBill = totalBill
                        )
                    }
                )
            }

            // Edit Expense Dialog
            if (state.isEditDialogOpen && state.selectedExpenseForDetails != null) {
                AddExpenseDialog(
                    initialExpense = state.selectedExpenseForDetails,
                    availableTags = state.customTags,
                    onAddCustomTag = { tag -> expenseViewModel.addCustomTag(tag) },
                    onDismiss = { expenseViewModel.closeEditDialog() },
                    onSave = { amount, title, catId, shop, payment, date, time, notes, tags, isSplit, participants, splitType, totalBill ->
                        val updated = state.selectedExpenseForDetails!!.copy(
                            amount = amount,
                            title = title,
                            categoryId = catId,
                            shopName = shop,
                            paymentMethod = payment,
                            expenseDate = date,
                            expenseTime = time,
                            notes = notes,
                            tags = tags,
                            isSplit = isSplit,
                            splitParticipants = participants,
                            splitType = splitType,
                            splitTotalBill = totalBill
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

            // AI Advisor Modal Screen
            if (showAiModal) {
                Dialog(
                    onDismissRequest = { showAiModal = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        "Gemini AI Financial Advisor",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                },
                                navigationIcon = {
                                    IconButton(onClick = { showAiModal = false }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Close AI Advisor"
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    ) { modalPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(modalPadding)
                        ) {
                            AiInsightsScreen(
                                state = state,
                                aiViewModel = aiViewModel
                            )
                        }
                    }
                }
            }

            // PIN Lock Overlay
            if (state.userProfile?.isPinEnabled == true && !state.isPinUnlocked) {
                PinUnlockOverlay(
                    onUnlockSubmit = { pin -> expenseViewModel.unlockPin(pin) }
                )
            }
        }
    }
}

@Composable
private fun PinUnlockOverlay(
    onUnlockSubmit: (String) -> Boolean
) {
    var pinText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false, usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Card(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(LightBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Lock",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.size(16.dp))
                    Text(
                        text = "TakaTrack Protected",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "অনুগ্রহ করে ৪ সংখ্যার পিন কোড প্রবেশ করুন",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    androidx.compose.material3.OutlinedTextField(
                        value = pinText,
                        onValueChange = { input ->
                            if (input.length <= 4 && input.all { it.isDigit() }) {
                                pinText = input
                                isError = false
                            }
                        },
                        label = { Text("Enter PIN") },
                        isError = isError,
                        supportingText = { if (isError) Text("ভুল পিন কোড! পুনরায় চেষ্টা করুন", color = com.example.ui.theme.DangerRed) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.size(16.dp))
                    androidx.compose.material3.Button(
                        onClick = {
                            val success = onUnlockSubmit(pinText)
                            if (!success) {
                                isError = true
                                pinText = ""
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .size(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Unlock App", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
