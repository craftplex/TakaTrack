package com.example.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.graphics.Color
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.model.CategoryBudgetInfo
import com.example.data.model.ExpenseCategory
import com.example.ui.ExpenseScreenState
import com.example.ui.ExpenseViewModel
import com.example.ui.auth.AuthViewModel
import com.example.ui.theme.DangerRed
import com.example.ui.theme.LightBlue
import com.example.ui.theme.PrimaryBlue
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
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

    // Category budget editing state
    var selectedCategoryForBudget by remember { mutableStateOf<ExpenseCategory?>(null) }
    var categoryBudgetInputText by remember { mutableStateOf("") }

    // Custom Tag creation state
    var isAddTagDialogOpen by remember { mutableStateOf(false) }
    var newTagInputText by remember { mutableStateOf("") }

    // Phase 4 PIN Lock state
    var isPinDialogOpen by remember { mutableStateOf(false) }
    var pinInputText by remember { mutableStateOf("") }

    // Phase 4 Clear Data state
    var isClearAllConfirmOpen by remember { mutableStateOf(false) }

    // Phase 4 Quick Template creation state
    var isNewTemplateDialogOpen by remember { mutableStateOf(false) }
    var newTemplateTitle by remember { mutableStateOf("") }
    var newTemplateAmountText by remember { mutableStateOf("") }
    var newTemplateCatId by remember { mutableStateOf("food") }

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

        // Monthly Overall Spending Limit
        item {
            SettingsCardItem(
                icon = Icons.Default.Tune,
                title = "Monthly Spending Limit (মাসিক মোট সীমা)",
                value = "৳ ${String.format(Locale.US, "%,.0f", state.monthlyLimit)}",
                subtitle = "বাজেটের অতিরিক্ত খরচ হলে সতর্কতা পাবেন",
                onClick = {
                    editLimitText = state.monthlyLimit.toInt().toString()
                    isEditLimitOpen = true
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Category-wise Budget Management
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Category Budgets (খাতভিত্তিক বাজেট)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    state.categoryBudgetsInfo.forEach { budgetInfo ->
                        val cat = budgetInfo.category
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable {
                                    selectedCategoryForBudget = cat
                                    categoryBudgetInputText = if (budgetInfo.limitAmount > 0) budgetInfo.limitAmount.toInt().toString() else ""
                                }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = cat.icon,
                                        contentDescription = null,
                                        tint = cat.color,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = cat.nameBn,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (budgetInfo.limitAmount > 0) {
                                        Text(
                                            text = "৳${String.format(Locale.US, "%.0f", budgetInfo.spentAmount)} / ৳${String.format(Locale.US, "%.0f", budgetInfo.limitAmount)}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = budgetInfo.getStatusColor()
                                            )
                                        )
                                    } else {
                                        Text(
                                            text = "সীমা নির্ধারণ করুন",
                                            style = MaterialTheme.typography.labelSmall.copy(color = PrimaryBlue)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                                }
                            }

                            if (budgetInfo.limitAmount > 0) {
                                Spacer(modifier = Modifier.height(6.dp))
                                val progress = (budgetInfo.usagePercent / 100f).coerceIn(0f, 1f)
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = budgetInfo.getStatusColor(),
                                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = budgetInfo.getStatusLabelBn(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = budgetInfo.getStatusColor()
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Custom Tags Management
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Sell,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Custom Tags (কাস্টম ট্যাগসমূহ)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        TextButton(onClick = { isAddTagDialogOpen = true }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Add Tag", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        state.customTags.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(LightBlue)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = tag,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = PrimaryBlue,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { viewModel.deleteCustomTag(tag) },
                                        modifier = Modifier.size(16.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Delete Tag",
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Budget Warning Notifications Settings
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Budget Alerts (সতর্কবার্তা নোটিফিকেশন)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    NotificationToggleRow(
                        label = "৫০% বাজেট পূর্ণ হলে নোটিফিকেশন",
                        checked = state.budgetWarning50,
                        onCheckedChange = { chk ->
                            viewModel.updateBudgetNotificationSettings(chk, state.budgetWarning75, state.budgetWarning90, state.budgetWarning100)
                        }
                    )
                    NotificationToggleRow(
                        label = "৭৫% বাজেট পূর্ণ হলে সতর্কতা",
                        checked = state.budgetWarning75,
                        onCheckedChange = { chk ->
                            viewModel.updateBudgetNotificationSettings(state.budgetWarning50, chk, state.budgetWarning90, state.budgetWarning100)
                        }
                    )
                    NotificationToggleRow(
                        label = "৯০% বাজেট পূর্ণ হলে জরুরি অ্যালার্ট",
                        checked = state.budgetWarning90,
                        onCheckedChange = { chk ->
                            viewModel.updateBudgetNotificationSettings(state.budgetWarning50, state.budgetWarning75, chk, state.budgetWarning100)
                        }
                    )
                    NotificationToggleRow(
                        label = "১০০% সীমা অতিক্রম অ্যালার্ট",
                        checked = state.budgetWarning100,
                        onCheckedChange = { chk ->
                            viewModel.updateBudgetNotificationSettings(state.budgetWarning50, state.budgetWarning75, state.budgetWarning90, chk)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
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
            Spacer(modifier = Modifier.height(12.dp))
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
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Quick Expense Templates Management Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Quick Templates (১-ট্যাপ টেমপ্লেট)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        TextButton(onClick = { isNewTemplateDialogOpen = true }) {
                            Text("+ New", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "সহজে বার বার ব্যবহারের জন্য খরচের টেমপ্লেটসমূহ:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    state.quickTemplates.take(6).forEach { tmpl ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "• ${tmpl.title} (৳${tmpl.amount.toInt()})",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (!tmpl.id.startsWith("default_")) {
                                IconButton(
                                    onClick = { viewModel.deleteTemplate(tmpl.id) },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Delete Template", tint = DangerRed, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // App Security & PIN Lock Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "App Security (পিন লক সুরক্ষা)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (profile?.isPinEnabled == true) "PIN Enabled (${profile.pinCode})" else "গোপনীয়তা রক্ষায় ৪ ডিজিটের পিন ব্যবহার করুন",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Switch(
                            checked = profile?.isPinEnabled ?: false,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    isPinDialogOpen = true
                                } else {
                                    viewModel.updatePinLock(false, "")
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = PrimaryBlue)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Data Export & Backup Options
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Data Export & Backup (ডেটা ব্যাকআপ)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "আপনার সমস্ত রেকর্ডিং CSV ফাইল বা লিখিত রিপোর্ট হিসেবে ব্যাকআপ রাখুন:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val csvStr = com.example.util.ExpenseExporter.exportToCsv(state.allExpenses)
                                com.example.util.ExpenseExporter.shareText(context, csvStr, "Export Expenses (CSV)")
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = LightBlue, contentColor = PrimaryBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Share CSV File", style = MaterialTheme.typography.labelSmall)
                        }

                        Button(
                            onClick = {
                                val summaryStr = com.example.util.ExpenseExporter.generateFormattedSummary(state.allExpenses, "Total Records")
                                com.example.util.ExpenseExporter.copyToClipboard(context, summaryStr)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue, contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Copy Summary", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Data Reset Action (Clear All Expenses)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F1)),
                border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Data Management (ডেটা রিসেট)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DangerRed)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "সমস্ত খরচের তালিকা স্থায়ীভাবে মুছে ফেলতে চান?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { isClearAllConfirmOpen = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Clear All Expense Records")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
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

    // Category Budget Edit Dialog
    if (selectedCategoryForBudget != null) {
        val cat = selectedCategoryForBudget!!
        AlertDialog(
            onDismissRequest = { selectedCategoryForBudget = null },
            title = { Text("${cat.nameBn}-এর বাজেট সীমা (৳)") },
            text = {
                Column {
                    Text(
                        text = "এই খাতের জন্য প্রতি মাসে সর্বাধিক কত টাকা খরচ করতে চান? (খালি রাখলে সীমা মুছে যাবে)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = categoryBudgetInputText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) categoryBudgetInputText = input
                        },
                        label = { Text("Monthly Limit (BDT ৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limit = categoryBudgetInputText.toDoubleOrNull() ?: 0.0
                        viewModel.setCategoryBudget(cat.id, limit)
                        selectedCategoryForBudget = null
                    }
                ) {
                    Text("Save Limit")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedCategoryForBudget = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Custom Tag Dialog
    if (isAddTagDialogOpen) {
        AlertDialog(
            onDismissRequest = { isAddTagDialogOpen = false },
            title = { Text("Add Custom Tag (নতুন কাস্টম ট্যাগ)") },
            text = {
                OutlinedTextField(
                    value = newTagInputText,
                    onValueChange = { newTagInputText = it },
                    label = { Text("Tag Name (e.g. Vacation, Medical)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTagInputText.isNotBlank()) {
                            viewModel.addCustomTag(newTagInputText.trim())
                            newTagInputText = ""
                        }
                        isAddTagDialogOpen = false
                    }
                ) {
                    Text("Add Tag")
                }
            },
            dismissButton = {
                TextButton(onClick = { isAddTagDialogOpen = false }) {
                    Text("Cancel")
                }
            }
        )
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

    // Set PIN Dialog
    if (isPinDialogOpen) {
        AlertDialog(
            onDismissRequest = { isPinDialogOpen = false },
            title = { Text("Set 4-Digit Security PIN") },
            text = {
                Column {
                    Text(
                        text = "অ্যাপের গোপনীয়তা রক্ষা করার জন্য ৪ সংখ্যার পিন কোড প্রদান করুন:",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = pinInputText,
                        onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) pinInputText = it },
                        label = { Text("4-Digit PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInputText.length == 4) {
                            viewModel.updatePinLock(true, pinInputText)
                            isPinDialogOpen = false
                            pinInputText = ""
                        }
                    }
                ) {
                    Text("Enable PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { isPinDialogOpen = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Quick Template Dialog
    if (isNewTemplateDialogOpen) {
        AlertDialog(
            onDismissRequest = { isNewTemplateDialogOpen = false },
            title = { Text("Create Quick Template (নতুন টেমপ্লেট)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newTemplateTitle,
                        onValueChange = { newTemplateTitle = it },
                        label = { Text("Title (e.g. Daily Rickshaw)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newTemplateAmountText,
                        onValueChange = { if (it.all { ch -> ch.isDigit() || ch == '.' }) newTemplateAmountText = it },
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
                        val amt = newTemplateAmountText.toDoubleOrNull() ?: 0.0
                        if (newTemplateTitle.isNotBlank() && amt > 0) {
                            viewModel.addCustomTemplate(
                                title = newTemplateTitle.trim(),
                                amount = amt,
                                categoryId = "food",
                                paymentMethod = "Cash (নগদ)",
                                shopName = "",
                                notes = "Template",
                                tags = listOf("Quick")
                            )
                            newTemplateTitle = ""
                            newTemplateAmountText = ""
                            isNewTemplateDialogOpen = false
                        }
                    }
                ) {
                    Text("Save Template")
                }
            },
            dismissButton = {
                TextButton(onClick = { isNewTemplateDialogOpen = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear All Expense Data Double-Confirmation Dialog
    if (isClearAllConfirmOpen) {
        AlertDialog(
            onDismissRequest = { isClearAllConfirmOpen = false },
            title = { Text("⚠️ রিসেট নিশ্চিতকরণ (Data Reset)") },
            text = {
                Text(
                    text = "আপনি কি নিশ্চিত যে আপনার সমস্ত খরচের হিসাব মুছে ফেলতে চান? এই পদক্ষেপটি আর ফিরিয়ে নেওয়া যাবে না।",
                    color = DangerRed
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        isClearAllConfirmOpen = false
                        viewModel.clearAllExpenseData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Yes, Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { isClearAllConfirmOpen = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun NotificationToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = PrimaryBlue)
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
