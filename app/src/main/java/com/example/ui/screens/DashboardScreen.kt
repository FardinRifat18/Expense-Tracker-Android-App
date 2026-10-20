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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TransactionType
import com.example.ui.components.DarkInsightPill
import com.example.ui.components.ReferenceHeaderCard
import com.example.ui.components.ReferenceTransactionItem
import com.example.ui.components.YourMoneySection
import com.example.ui.theme.CleanBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletPrimary
import com.example.viewmodel.ExpenseViewModel
import com.example.viewmodel.PeriodFilter
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: ExpenseViewModel,
    onNavigateToTransactions: () -> Unit,
    onNavigateToAddTransaction: (TransactionType) -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToSavings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val summary by viewModel.financialSummary.collectAsState()
    val currency by viewModel.currentCurrency.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val allTransactions by viewModel.filteredTransactions.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    val recentTransactions = allTransactions.take(8)

    // Current date label matching reference: "Monday, 12 January, 2026"
    val todayDateFormat = SimpleDateFormat("EEEE, d MMMM, yyyy", Locale.ENGLISH)
    val todayFormatted = todayDateFormat.format(Date())

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CleanBackground)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Reference Soft Lavender Curved Gradient Header
        item {
            ReferenceHeaderCard(
                userName = userProfile.name,
                summary = summary,
                currency = currency,
                selectedPeriod = selectedPeriod,
                onPeriodClick = {
                    val nextPeriod = when (selectedPeriod) {
                        PeriodFilter.THIS_MONTH -> PeriodFilter.LAST_MONTH
                        PeriodFilter.LAST_MONTH -> PeriodFilter.ALL_TIME
                        PeriodFilter.ALL_TIME -> PeriodFilter.THIS_MONTH
                    }
                    viewModel.setPeriodFilter(nextPeriod)
                },
                onProfileClick = onNavigateToProfile
            )
        }

        // 2. "Your Money ⓘ" side-by-side cards
        item {
            Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                YourMoneySection(
                    summary = summary,
                    currency = currency,
                    onDetailsClick = onNavigateToTransactions
                )
            }
        }

        // 3. Dark Insight Banner: "✦ Your insight is ready"
        item {
            Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                DarkInsightPill(
                    onInsightClick = onNavigateToBudgets
                )
            }
        }

        // 4. "Transactions" Section Header with filter tools
        item {
            Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transactions",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = TextPrimary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable { onNavigateToTransactions() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable { onNavigateToTransactions() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "History",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // "For the Period" chip
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEEEBFC),
                            modifier = Modifier.clickable { onNavigateToTransactions() }
                        ) {
                            Text(
                                text = "For the Period",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = VioletPrimary
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Date Group header matching reference: "Monday, 12 January, 2026" ... "Total ৳2,95,776"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = todayFormatted,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        ),
                        color = TextSecondary
                    )

                    Text(
                        text = "Total ${currency.formatCompact(summary.totalExpenses)}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = TextPrimary
                    )
                }
            }
        }

        // 5. Transaction items
        if (recentTransactions.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No transactions found in this period",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(recentTransactions, key = { it.id }) { tx ->
                Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                    ReferenceTransactionItem(
                        transaction = tx,
                        currency = currency,
                        onClick = { viewModel.selectTransaction(tx) }
                    )
                }
            }
        }
    }
}
