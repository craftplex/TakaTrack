package com.example.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ExpenseScreenState
import com.example.ui.ExpenseViewModel
import com.example.ui.auth.AuthViewModel
import com.example.ui.theme.DangerRed
import com.example.ui.theme.LightBlue
import com.example.ui.theme.PrimaryBlue
import java.util.Locale

@Composable
fun ProfileScreen(
    state: ExpenseScreenState,
    viewModel: ExpenseViewModel,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isEditNameOpen by remember { mutableStateOf(false) }
    var isEditLimitOpen by remember { mutableStateOf(false) }
    var editNameText by remember { mutableStateOf("") }
    var editLimitText by remember { mutableStateOf("") }
    var isSignOutConfirmOpen by remember { mutableStateOf(false) }

    val user = authViewModel.uiState.value.user
    val profile = state.userProfile

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp)
    ) {
        // Header
        item {
            Text(
                text = "Profile & Settings (প্রোফাইল সেটিংস)",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Profile Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(LightBlue)
                            .border(2.dp, PrimaryBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Avatar",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = profile?.displayName?.ifBlank { user?.displayName ?: "User" } ?: "User",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = profile?.email?.ifBlank { user?.email ?: "" } ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            editNameText = profile?.displayName ?: user?.displayName ?: ""
                            isEditNameOpen = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LightBlue, contentColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit Name (নাম পরিবর্তন)")
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Settings Section Header
        item {
            Text(
                text = "অ্যাপ ও আর্থিক সেটিংস",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Monthly Spending Limit
        item {
            SettingsCardItem(
                icon = Icons.Default.Tune,
                title = "Monthly Spending Limit (মাসিক বাজেট সীমা)",
                value = "৳ ${String.format(Locale.US, "%,.0f", state.monthlyLimit)}",
                subtitle = "বাজেটের অতিরিক্ত খরচ হলে সতর্কতা পাবেন",
                onClick = {
                    editLimitText = state.monthlyLimit.toInt().toString()
                    isEditLimitOpen = true
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Currency Setting
        item {
            SettingsCardItem(
                icon = Icons.Default.Payments,
                title = "মুদ্রা ও ফরম্যাট (Currency)",
                value = "BDT (৳)",
                subtitle = "বাংলাদেশি টাকা (ডিফল্ট)",
                onClick = {}
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Language Setting
        item {
            SettingsCardItem(
                icon = Icons.Default.Language,
                title = "ভাষা (Language)",
                value = if (profile?.language == "en") "English" else "বাংলা (Bangla)",
                subtitle = "Bengali & English Support",
                onClick = {
                    val nextLang = if (profile?.language == "en") "bn" else "en"
                    viewModel.updateLanguage(nextLang)
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Business Rule / About App
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TakaTrack দর্শন (Philosophy)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "এটি একটি ১০০% Expense-Only ট্র্যাকার। এখানে কোনো জটিল আয়, ওয়ালেট ব্যালেন্স বা ক্যাশফ্লো নেই। আপনার প্রতিটি খরচের হিসাব পরিষ্কারভাবে তুলে ধরাই এই অ্যাপের মূল লক্ষ্য।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Logout Button
        item {
            Button(
                onClick = { isSignOutConfirmOpen = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("logout_button"),
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out (লগআউট করুন)", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Edit Name Dialog
    if (isEditNameOpen) {
        AlertDialog(
            onDismissRequest = { isEditNameOpen = false },
            title = { Text("Display Name") },
            text = {
                OutlinedTextField(
                    value = editNameText,
                    onValueChange = { editNameText = it },
                    label = { Text("Your Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editNameText.isNotBlank()) {
                            viewModel.updateDisplayName(editNameText.trim())
                        }
                        isEditNameOpen = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { isEditNameOpen = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Spending Limit Dialog
    if (isEditLimitOpen) {
        AlertDialog(
            onDismissRequest = { isEditLimitOpen = false },
            title = { Text("Set Monthly Spending Limit (৳)") },
            text = {
                Column {
                    Text(
                        text = "মাসিক কত টাকা খরচের সীমা নির্ধারণ করতে চান?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = editLimitText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) editLimitText = input
                        },
                        label = { Text("Amount in BDT (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limit = editLimitText.toDoubleOrNull()
                        if (limit != null && limit > 0) {
                            viewModel.updateMonthlyLimit(limit)
                        }
                        isEditLimitOpen = false
                    }
                ) {
                    Text("Set Limit")
                }
            },
            dismissButton = {
                TextButton(onClick = { isEditLimitOpen = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Logout confirmation
    if (isSignOutConfirmOpen) {
        AlertDialog(
            onDismissRequest = { isSignOutConfirmOpen = false },
            title = { Text("লগআউট করতে চান?") },
            text = { Text("Are you sure you want to log out? পুনরায় লগইন করতে যেকোনো সময় Google অ্যাকাউন্ট ব্যবহার করতে পারবেন।") },
            confirmButton = {
                Button(
                    onClick = {
                        isSignOutConfirmOpen = false
                        authViewModel.signOut(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Log Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { isSignOutConfirmOpen = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsCardItem(
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(LightBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
            )
        }
    }
}
