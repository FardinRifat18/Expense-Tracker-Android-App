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
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.example.model.BudgetItem
import com.example.model.CategoryItem
import com.example.model.SavingsGoalItem
import com.example.model.TransactionType
import com.example.ui.components.CategoryIconHelper
import com.example.ui.theme.CardBlueBg
import com.example.ui.theme.CardBlueIcon
import com.example.ui.theme.CardMintBg
import com.example.ui.theme.CardMintIcon
import com.example.ui.theme.CardOrangeBg
import com.example.ui.theme.CardOrangeIcon
import com.example.ui.theme.CardPinkBg
import com.example.ui.theme.CardPinkIcon
import com.example.ui.theme.CardPurpleBg
import com.example.ui.theme.CardPurpleIcon
import com.example.ui.theme.CleanBackground
import com.example.ui.theme.CleanSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletPrimary
import com.example.viewmodel.ExpenseViewModel
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val budgets by viewModel.enrichedBudgets.collectAsState()
    val goals by viewModel.savingsGoals.collectAsState()
    val currency by viewModel.currentCurrency.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()
    val expenseCategories = allCategories.filter { it.type == TransactionType.EXPENSE }

    var showCreateDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var selectedGoalForDeposit by remember { mutableStateOf<SavingsGoalItem?>(null) }

    val primaryGoal = goals.firstOrNull() ?: SavingsGoalItem(
        id = "goal_demo",
        title = "House by the Sea",
        targetAmount = BigDecimal("150000.00"),
        currentAmount = BigDecimal("87500.00"),
        targetDateEpochMillis = System.currentTimeMillis() + 864000000L
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CleanBackground)
            .testTag("budgets_screen"),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Header: "My Plan" with (+) and (↗) buttons
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Plan",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = TextPrimary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // (+) Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF14131D))
                            .clickable { showCreateDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // (↗) Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { showGoalDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NorthEast,
                            contentDescription = "Goals",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 2. "Goals" Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Goals",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = TextPrimary
                )

                Text(
                    text = "View All",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.clickable { showGoalDialog = true }
                )
            }
        }

        // 3. Featured Hero Goal Card matching reference Screen 3
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CleanSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Header of card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Orange hexagon dot icon
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CardOrangeBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(CardOrangeIcon)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = primaryGoal.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Tap to deposit",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = TextSecondary
                                )
                            }
                        }

                        IconButton(
                            onClick = { selectedGoalForDeposit = primaryGoal },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Amount row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = currency.format(primaryGoal.currentAmount),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp
                            ),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Out of ${currency.format(primaryGoal.targetAmount)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress Bar
                    val fraction = primaryGoal.progressFraction
                    LinearProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFFFF6442),
                        trackColor = Color(0xFFF0EBE9)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Your Progress",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = TextSecondary
                        )
                        Text(
                            text = "${currency.formatCompact(primaryGoal.remainingAmount)} Left",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Coral Alert Banner inside card
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { selectedGoalForDeposit = primaryGoal },
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFF5252)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Keep depositing to reach your milestone on schedule.",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        // 4. "Budgets" Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Budgets",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = TextPrimary
                )

                Text(
                    text = "View All",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.clickable { showCreateDialog = true }
                )
            }
        }

        // 5. Budgets List matching reference cards
        items(budgets, key = { it.id }) { budget ->
            val percent = budget.percentageUsed.toInt()
            val (bgColor, iconColor) = when {
                budget.categoryName.contains("Food", ignoreCase = true) -> Pair(CardPinkBg, CardPinkIcon)
                budget.categoryName.contains("Transport", ignoreCase = true) || budget.categoryName.contains("Car", ignoreCase = true) -> Pair(CardOrangeBg, CardOrangeIcon)
                budget.categoryName.contains("Education", ignoreCase = true) -> Pair(CardPurpleBg, CardPurpleIcon)
                budget.categoryName.contains("Vacation", ignoreCase = true) || budget.categoryName.contains("Travel", ignoreCase = true) -> Pair(CardBlueBg, CardBlueIcon)
                else -> Pair(CardMintBg, CardMintIcon)
            }

            val icon = when {
                budget.categoryName.contains("Transport", ignoreCase = true) || budget.categoryName.contains("Car", ignoreCase = true) -> Icons.Default.DirectionsCar
                budget.categoryName.contains("Education", ignoreCase = true) -> Icons.Default.School
                budget.categoryName.contains("Vacation", ignoreCase = true) -> Icons.Default.Umbrella
                budget.categoryName.contains("Health", ignoreCase = true) -> Icons.Default.LocalHospital
                else -> Icons.Default.Home
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CleanSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Category pill icon
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(bgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = budget.categoryName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                ),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${currency.symbol}${budget.spentAmount} of ${currency.symbol}${budget.limitAmount}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = TextSecondary
                            )
                        }
                    }

                    // Circular percentage badge matching reference
                    Surface(
                        shape = CircleShape,
                        color = bgColor,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$percent%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                color = iconColor
                            )
                        }
                    }
                }
            }
        }
    }

    // Create Budget Dialog
    if (showCreateDialog) {
        var selectedCat by remember { mutableStateOf<CategoryItem?>(null) }
        var limitInput by remember { mutableStateOf("") }
        var isMenuExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Set Category Budget") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = isMenuExpanded,
                        onExpandedChange = { isMenuExpanded = !isMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCat?.name ?: "Overall Monthly Budget",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isMenuExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Overall Monthly Budget") },
                                onClick = {
                                    selectedCat = null
                                    isMenuExpanded = false
                                }
                            )
                            expenseCategories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name) },
                                    onClick = {
                                        selectedCat = cat
                                        isMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = limitInput,
                        onValueChange = { limitInput = it },
                        label = { Text("Limit Amount (${currency.symbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limit = limitInput.toBigDecimalOrNull()
                        if (limit != null && limit > BigDecimal.ZERO) {
                            viewModel.saveBudget(
                                categoryId = selectedCat?.id,
                                categoryName = selectedCat?.name ?: "Overall Monthly",
                                limit = limit
                            )
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Deposit to Goal Dialog
    selectedGoalForDeposit?.let { goal ->
        var depositAmountText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { selectedGoalForDeposit = null },
            title = { Text("Deposit to ${goal.title}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Saved: ${currency.symbol}${goal.currentAmount} of ${currency.symbol}${goal.targetAmount}")
                    OutlinedTextField(
                        value = depositAmountText,
                        onValueChange = { depositAmountText = it },
                        label = { Text("Deposit Amount (${currency.symbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = depositAmountText.toBigDecimalOrNull()
                        if (amount != null && amount > BigDecimal.ZERO) {
                            viewModel.contributeToSavingsGoal(goal.id, amount, goal.title)
                            selectedGoalForDeposit = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary)
                ) {
                    Text("Deposit", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedGoalForDeposit = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
