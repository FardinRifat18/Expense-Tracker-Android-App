package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.model.SavingsGoalItem
import com.example.ui.components.CategoryIconHelper
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MintAccent
import com.example.ui.theme.PurpleSavings
import com.example.viewmodel.ExpenseViewModel
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun SavingsGoalsScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val goals by viewModel.savingsGoals.collectAsState()
    val currency by viewModel.currentCurrency.collectAsState()

    var showCreateGoalDialog by remember { mutableStateOf(false) }
    var goalToContribute by remember { mutableStateOf<SavingsGoalItem?>(null) }
    var goalToDelete by remember { mutableStateOf<SavingsGoalItem?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("savings_goals_screen")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Savings Goals",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Grow wealth & fund milestones in ${currency.symbol}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { showCreateGoalDialog = true },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                modifier = Modifier.testTag("new_savings_goal_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Goal", color = Color.White)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (goals.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(PurpleSavings.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Savings,
                                    contentDescription = null,
                                    tint = PurpleSavings,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "No Savings Goals Yet",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Create a target for vacation, emergency fund, or electronics in ${currency.symbol}.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(goals, key = { it.id }) { goal ->
                    SavingsGoalCard(
                        goal = goal,
                        currencySymbol = currency.symbol,
                        onContributeClick = { goalToContribute = goal },
                        onDeleteClick = { goalToDelete = goal }
                    )
                }
            }
        }
    }

    // New Goal Dialog
    if (showCreateGoalDialog) {
        var title by remember { mutableStateOf("") }
        var targetAmountText by remember { mutableStateOf("") }
        var initialAmountText by remember { mutableStateOf("0") }
        var targetMonths by remember { mutableStateOf("6") }
        var dialogError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showCreateGoalDialog = false },
            title = { Text("Create Savings Goal") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it; dialogError = null },
                        label = { Text("Goal Title (e.g. Sajek Trip, Laptop)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = targetAmountText,
                        onValueChange = {
                            if (it.isEmpty() || it.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) {
                                targetAmountText = it
                                dialogError = null
                            }
                        },
                        label = { Text("Target Amount (${currency.symbol})") },
                        placeholder = { Text("e.g. 50000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = initialAmountText,
                        onValueChange = {
                            if (it.isEmpty() || it.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) {
                                initialAmountText = it
                            }
                        },
                        label = { Text("Starting Deposit (${currency.symbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = targetMonths,
                        onValueChange = { targetMonths = it.filter { char -> char.isDigit() } },
                        label = { Text("Target Timeframe (Months)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    dialogError?.let { err ->
                        Text(text = err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = targetAmountText.toBigDecimalOrNull()
                        val initial = initialAmountText.toBigDecimalOrNull() ?: BigDecimal.ZERO
                        val months = targetMonths.toIntOrNull() ?: 6

                        if (title.isBlank()) {
                            dialogError = "Please enter a title for this goal"
                            return@Button
                        }
                        if (target == null || target <= BigDecimal.ZERO) {
                            dialogError = "Target amount must be greater than 0"
                            return@Button
                        }

                        val cal = Calendar.getInstance().apply { add(Calendar.MONTH, months) }
                        viewModel.addSavingsGoal(
                            title = title,
                            targetAmount = target,
                            initialAmount = initial,
                            targetDateEpoch = cal.timeInMillis,
                            colorHex = "#00A86B"
                        )
                        showCreateGoalDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Create Goal", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateGoalDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Contribute / Deposit Dialog
    goalToContribute?.let { goal ->
        var depositAmountText by remember { mutableStateOf("") }
        var depositError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { goalToContribute = null },
            title = { Text("Deposit to ${goal.title}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Current: ${currency.symbol} ${goal.currentAmount} / ${currency.symbol} ${goal.targetAmount}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    OutlinedTextField(
                        value = depositAmountText,
                        onValueChange = {
                            if (it.isEmpty() || it.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) {
                                depositAmountText = it
                                depositError = null
                            }
                        },
                        label = { Text("Deposit Amount (${currency.symbol})") },
                        placeholder = { Text("e.g. 5000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    depositError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = depositAmountText.toBigDecimalOrNull()
                        if (amount == null || amount <= BigDecimal.ZERO) {
                            depositError = "Please enter an amount greater than 0"
                            return@Button
                        }
                        viewModel.contributeToSavingsGoal(goal.id, amount, goal.title)
                        goalToContribute = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Add Deposit", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { goalToContribute = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Goal Dialog
    goalToDelete?.let { goal ->
        AlertDialog(
            onDismissRequest = { goalToDelete = null },
            title = { Text("Delete Goal") },
            text = { Text("Are you sure you want to remove \"${goal.title}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSavingsGoal(goal.id)
                        goalToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { goalToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SavingsGoalCard(
    goal: SavingsGoalItem,
    currencySymbol: String,
    onContributeClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val progress = goal.progressFraction
    val percentInt = (progress * 100).toInt()
    val isReached = goal.isReached
    val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
    val dateStr = dateFormat.format(Date(goal.targetDateEpochMillis))
    val goalColor = CategoryIconHelper.parseColor(goal.colorHex, EmeraldPrimary)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(goalColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isReached) Icons.Default.CheckCircle else Icons.Default.Savings,
                            contentDescription = null,
                            tint = if (isReached) MintAccent else goalColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Target by $dateStr",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDeleteClick, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = if (isReached) MintAccent else goalColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Saved ($percentInt%)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$currencySymbol ${goal.currentAmount}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Remaining: $currencySymbol ${goal.remainingAmount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Goal: $currencySymbol ${goal.targetAmount}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = EmeraldLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Add Deposit Action Button
            OutlinedButton(
                onClick = onContributeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Contribution / Deposit", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
