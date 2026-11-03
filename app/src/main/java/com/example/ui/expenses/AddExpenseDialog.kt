package com.example.ui.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CategoryConstants
import com.example.data.model.Expense
import com.example.data.model.SplitParticipant
import com.example.ui.ExpenseViewModel
import com.example.ui.theme.LightBlue
import com.example.ui.theme.PrimaryBlue
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddExpenseDialog(
    initialExpense: Expense? = null,
    availableTags: List<String> = listOf("Personal", "Family", "Office", "Travel", "Emergency"),
    onAddCustomTag: ((String) -> Unit)? = null,
    onDismiss: () -> Unit,
    onSave: (
        amount: Double,
        title: String,
        categoryId: String,
        shopName: String,
        paymentMethod: String,
        date: String,
        time: String,
        notes: String,
        tags: List<String>,
        isSplit: Boolean,
        splitParticipants: List<SplitParticipant>,
        splitType: String,
        splitTotalBill: Double
    ) -> Unit
) {
    var isSplitMode by remember { mutableStateOf(initialExpense?.isSplit ?: false) }
    var totalBillText by remember {
        mutableStateOf(
            if (initialExpense?.isSplit == true) initialExpense.splitTotalBill.toString()
            else if (initialExpense != null) initialExpense.amount.toString()
            else ""
        )
    }
    var userAmountText by remember { mutableStateOf(if (initialExpense != null) initialExpense.amount.toString() else "") }
    var title by remember { mutableStateOf(initialExpense?.title ?: "") }
    var selectedCategoryId by remember { mutableStateOf(initialExpense?.categoryId ?: "grocery") }
    var shopName by remember { mutableStateOf(initialExpense?.shopName ?: "") }
    var paymentMethod by remember { mutableStateOf(initialExpense?.paymentMethod ?: "Cash") }
    var dateText by remember {
        mutableStateOf(initialExpense?.expenseDate ?: ExpenseViewModel.getTodayDateString())
    }
    var timeText by remember {
        mutableStateOf(initialExpense?.expenseTime ?: ExpenseViewModel.getCurrentTimeString())
    }
    var notes by remember { mutableStateOf(initialExpense?.notes ?: "") }
    var selectedTags by remember { mutableStateOf(initialExpense?.tags?.toSet() ?: emptySet()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Split Expense states
    var splitType by remember { mutableStateOf(initialExpense?.splitType ?: "Equal Split") }
    var participantList by remember {
        mutableStateOf(initialExpense?.splitParticipants ?: listOf(SplitParticipant(name = "Friend 1", amount = 0.0)))
    }
    var newParticipantName by remember { mutableStateOf("") }
    var newTagText by remember { mutableStateOf("") }
    var isAddTagDialogOpen by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val paymentMethods = listOf("Cash", "bKash", "Nagad", "Bank Card", "Other")
    val shopSuggestions = listOf("Madina Store", "Agora", "Shwapno", "Local Market", "Pharmacy")

    // Recalculate amounts if Equal Split is active
    val totalBillVal = totalBillText.toDoubleOrNull() ?: 0.0
    val totalParticipantsCount = participantList.size + 1 // friends + user
    val equalShare = if (isSplitMode && splitType == "Equal Split" && totalParticipantsCount > 0) {
        totalBillVal / totalParticipantsCount
    } else null

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("add_expense_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialExpense == null) "Add Expense (নতুন খরচ)" else "Edit Expense (খরচ পরিবর্তন)",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Mode Selector Card: Personal vs Split Expense
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSplitMode) LightBlue.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isSplitMode) Icons.Default.Group else Icons.Default.Person,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isSplitMode) "Split Expense Mode (বিল ভাগাভাগি)" else "Personal Expense (ব্যক্তিগত খরচ)",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isSplitMode) "বন্ধুদের সাথে মোট বিল ভাগ করুন" else "একক খরচের হিসাব রাখুন",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                        Switch(
                            checked = isSplitMode,
                            onCheckedChange = { isSplitMode = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = PrimaryBlue)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Amount Input Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LightBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isSplitMode) "TOTAL BILL AMOUNT (মোট বিল)" else "YOUR EXPENSE AMOUNT (আপনার খরচ)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "৳ ",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                            OutlinedTextField(
                                value = if (isSplitMode) totalBillText else userAmountText,
                                onValueChange = { input ->
                                    if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d{0,2}$"""))) {
                                        if (isSplitMode) totalBillText = input
                                        else userAmountText = input
                                    }
                                },
                                placeholder = { Text("0.00", fontSize = 28.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("expense_amount_input")
                            )
                        }

                        if (isSplitMode) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val myShare = equalShare ?: (userAmountText.toDoubleOrNull() ?: 0.0)
                            Text(
                                text = "আপনার নিজস্ব ভাগের হিসাব: ৳ ${String.format(Locale.US, "%,.2f", myShare)}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                            )
                        }
                    }
                }

                // Split Details UI (shown if Split Mode is ON)
                if (isSplitMode) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "ভাগাভাগির ধরন (Split Type)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = splitType == "Equal Split",
                                    onClick = { splitType = "Equal Split" },
                                    label = { Text("Equal Split (সমান ভাগ)") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryBlue,
                                        selectedLabelColor = Color.White
                                    )
                                )
                                FilterChip(
                                    selected = splitType == "Custom Split",
                                    onClick = { splitType = "Custom Split" },
                                    label = { Text("Custom Split (কাস্টম)") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryBlue,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "অংশীদারদের তালিকা (Participants)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // You (User)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "আপনি (You)",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                val calculatedShare = equalShare ?: (userAmountText.toDoubleOrNull() ?: 0.0)
                                Text(
                                    text = "৳ ${String.format(Locale.US, "%.2f", calculatedShare)}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlue
                                    )
                                )
                            }

                            // Participants List
                            participantList.forEachIndexed { index, participant ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                participantList = participantList.toMutableList().apply { removeAt(index) }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Close, contentDescription = "Remove", tint = Color.Red)
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = participant.name, style = MaterialTheme.typography.bodyMedium)
                                    }

                                    if (splitType == "Equal Split") {
                                        Text(
                                            text = "৳ ${String.format(Locale.US, "%.2f", equalShare ?: 0.0)}",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                        )
                                    } else {
                                        OutlinedTextField(
                                            value = participant.amount.let { if (it > 0) it.toString() else "" },
                                            onValueChange = { valAmt ->
                                                val parsed = valAmt.toDoubleOrNull() ?: 0.0
                                                participantList = participantList.toMutableList().apply {
                                                    this[index] = this[index].copy(amount = parsed)
                                                }
                                            },
                                            placeholder = { Text("0.00") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            singleLine = true,
                                            modifier = Modifier.width(100.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Add Participant Field
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newParticipantName,
                                    onValueChange = { newParticipantName = it },
                                    label = { Text("Friend's Name") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (newParticipantName.isNotBlank()) {
                                            val initialAmt = if (splitType == "Equal Split") 0.0 else 0.0
                                            participantList = participantList + SplitParticipant(
                                                name = newParticipantName.trim(),
                                                amount = initialAmt,
                                                avatarInitial = newParticipantName.trim().take(1).uppercase()
                                            )
                                            newParticipantName = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title / Description
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Expense Title / Note (খরচের নাম)") },
                    placeholder = { Text("e.g. Grocery Shopping, Dinner, Taxi Fare") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_title_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Category Selection Grid
                Text(
                    text = "খাত নির্বাচন করুন (Select Category)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryConstants.Categories.forEach { category ->
                        val isSelected = selectedCategoryId == category.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) LightBlue else MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedCategoryId = category.id }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = category.icon,
                                    contentDescription = category.nameEn,
                                    tint = if (isSelected) PrimaryBlue else category.color,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = category.nameBn,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Date & Time row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dateText,
                        onValueChange = { dateText = it },
                        label = { Text("Date (তারিখ)") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = timeText,
                        onValueChange = { timeText = it },
                        label = { Text("Time (সময়)") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Shop / Merchant Name
                OutlinedTextField(
                    value = shopName,
                    onValueChange = { shopName = it },
                    label = { Text("Shop / Merchant (দোকান বা স্থানের নাম - ঐচ্ছিক)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Store, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Quick Shop Suggestions
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    shopSuggestions.forEach { suggestion ->
                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.labelSmall.copy(color = PrimaryBlue),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(LightBlue)
                                .clickable { shopName = suggestion }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment Method
                Text(
                    text = "Payment Method (পদ্ধতি)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    paymentMethods.forEach { method ->
                        FilterChip(
                            selected = paymentMethod == method,
                            onClick = { paymentMethod = method },
                            label = { Text(method) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Tags
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ট্যাগ (Custom Tags)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    TextButton(onClick = { isAddTagDialogOpen = true }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Add Tag", style = MaterialTheme.typography.labelSmall)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    availableTags.forEach { tag ->
                        val isSelected = selectedTags.contains(tag)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedTags = if (isSelected) {
                                    selectedTags - tag
                                } else {
                                    selectedTags + tag
                                }
                            },
                            label = { Text(tag) },
                            leadingIcon = if (isSelected) {
                                { Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LightBlue,
                                selectedLabelColor = PrimaryBlue
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Save Expense CTA Button
                Button(
                    onClick = {
                        val finalUserAmount = if (isSplitMode) {
                            if (splitType == "Equal Split") equalShare ?: 0.0
                            else (totalBillVal - participantList.sumOf { it.amount })
                        } else {
                            userAmountText.toDoubleOrNull() ?: 0.0
                        }

                        if (finalUserAmount <= 0 && !isSplitMode) {
                            errorMessage = "টাকার সঠিক পরিমাণ দিন (Amount must be > 0)"
                            return@Button
                        }
                        if (isSplitMode && totalBillVal <= 0) {
                            errorMessage = "মোট বিলে সঠিক পরিমাণ দিন"
                            return@Button
                        }

                        if (title.isBlank()) {
                            val cat = CategoryConstants.getCategoryById(selectedCategoryId)
                            title = if (isSplitMode) "Split Bill - ${cat.nameBn}" else cat.nameBn
                        }
                        errorMessage = null

                        // Final participants list with shares
                        val finalParticipants = if (isSplitMode && splitType == "Equal Split") {
                            participantList.map { it.copy(amount = equalShare ?: 0.0) }
                        } else {
                            participantList
                        }

                        onSave(
                            finalUserAmount,
                            title,
                            selectedCategoryId,
                            shopName,
                            paymentMethod,
                            dateText,
                            timeText,
                            notes,
                            selectedTags.toList(),
                            isSplitMode,
                            finalParticipants,
                            splitType,
                            if (isSplitMode) totalBillVal else finalUserAmount
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_expense_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text(
                        text = if (initialExpense == null) "Save Expense (সংরক্ষণ করুন)" else "Update Expense (হালনাগাদ করুন)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Add Custom Tag Dialog
    if (isAddTagDialogOpen) {
        AlertDialog(
            onDismissRequest = { isAddTagDialogOpen = false },
            title = { Text("Create New Tag (নতুন ট্যাগ)") },
            text = {
                OutlinedTextField(
                    value = newTagText,
                    onValueChange = { newTagText = it },
                    label = { Text("Tag Name (e.g. Vacation, Medical)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTagText.isNotBlank()) {
                            val clean = newTagText.trim()
                            onAddCustomTag?.invoke(clean)
                            selectedTags = selectedTags + clean
                            newTagText = ""
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
}
