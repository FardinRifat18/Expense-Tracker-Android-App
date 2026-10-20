package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TransactionType
import com.example.ui.components.StatusBanner
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletPrimary
import com.example.viewmodel.ExpenseViewModel

enum class ReferenceNavigationTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    REPORT("Report", Icons.Default.BarChart),
    PLAN("Plan", Icons.Default.Savings),
    SETTINGS("Settings", Icons.Default.Settings)
}

enum class ActiveSubScreen {
    NONE,
    ADD_TRANSACTION,
    CATEGORIES,
    TRANSACTIONS_LIST
}

@Composable
fun MainContainerScreen(
    viewModel: ExpenseViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(ReferenceNavigationTab.HOME) }
    var subScreen by remember { mutableStateOf(ActiveSubScreen.NONE) }
    var initialAddType by remember { mutableStateOf(TransactionType.EXPENSE) }

    val statusMessage by viewModel.statusMessage.collectAsState()

    BackHandler(enabled = subScreen != ActiveSubScreen.NONE || selectedTab != ReferenceNavigationTab.HOME) {
        if (subScreen != ActiveSubScreen.NONE) {
            subScreen = ActiveSubScreen.NONE
        } else if (selectedTab != ReferenceNavigationTab.HOME) {
            selectedTab = ReferenceNavigationTab.HOME
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (subScreen == ActiveSubScreen.NONE) {
                    ReferenceBottomBar(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        onFabClick = {
                            initialAddType = TransactionType.EXPENSE
                            subScreen = ActiveSubScreen.ADD_TRANSACTION
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = subScreen to selectedTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "screen_flow"
                ) { (activeSub, tab) ->
                    when (activeSub) {
                        ActiveSubScreen.ADD_TRANSACTION -> {
                            AddTransactionScreen(
                                viewModel = viewModel,
                                initialType = initialAddType,
                                onDismiss = { subScreen = ActiveSubScreen.NONE }
                            )
                        }
                        ActiveSubScreen.CATEGORIES -> {
                            CategoriesScreen(
                                viewModel = viewModel
                            )
                        }
                        ActiveSubScreen.TRANSACTIONS_LIST -> {
                            TransactionsScreen(
                                viewModel = viewModel
                            )
                        }
                        ActiveSubScreen.NONE -> {
                            when (tab) {
                                ReferenceNavigationTab.HOME -> DashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToTransactions = { subScreen = ActiveSubScreen.TRANSACTIONS_LIST },
                                    onNavigateToAddTransaction = { type ->
                                        initialAddType = type
                                        subScreen = ActiveSubScreen.ADD_TRANSACTION
                                    },
                                    onNavigateToBudgets = { selectedTab = ReferenceNavigationTab.PLAN },
                                    onNavigateToSavings = { selectedTab = ReferenceNavigationTab.PLAN },
                                    onNavigateToProfile = { selectedTab = ReferenceNavigationTab.SETTINGS }
                                )
                                ReferenceNavigationTab.REPORT -> AnalyticsScreen(
                                    viewModel = viewModel
                                )
                                ReferenceNavigationTab.PLAN -> BudgetsScreen(
                                    viewModel = viewModel
                                )
                                ReferenceNavigationTab.SETTINGS -> ProfileScreen(
                                    viewModel = viewModel,
                                    onNavigateToCategories = { subScreen = ActiveSubScreen.CATEGORIES },
                                    onLogout = onLogout
                                )
                            }
                        }
                    }
                }

                StatusBanner(
                    message = statusMessage,
                    onDismiss = { viewModel.clearStatusMessage() },
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}

@Composable
fun ReferenceBottomBar(
    selectedTab: ReferenceNavigationTab,
    onTabSelected: (ReferenceNavigationTab) -> Unit,
    onFabClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // White elevated dock
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = Color.White,
            shadowElevation = 10.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 1: Home
                NavigationItem(
                    tab = ReferenceNavigationTab.HOME,
                    isSelected = selectedTab == ReferenceNavigationTab.HOME,
                    onClick = { onTabSelected(ReferenceNavigationTab.HOME) }
                )

                // Tab 2: Report
                NavigationItem(
                    tab = ReferenceNavigationTab.REPORT,
                    isSelected = selectedTab == ReferenceNavigationTab.REPORT,
                    onClick = { onTabSelected(ReferenceNavigationTab.REPORT) }
                )

                // Center Gap for FAB
                Spacer(modifier = Modifier.width(52.dp))

                // Tab 3: Plan
                NavigationItem(
                    tab = ReferenceNavigationTab.PLAN,
                    isSelected = selectedTab == ReferenceNavigationTab.PLAN,
                    onClick = { onTabSelected(ReferenceNavigationTab.PLAN) }
                )

                // Tab 4: Settings
                NavigationItem(
                    tab = ReferenceNavigationTab.SETTINGS,
                    isSelected = selectedTab == ReferenceNavigationTab.SETTINGS,
                    onClick = { onTabSelected(ReferenceNavigationTab.SETTINGS) }
                )
            }
        }

        // Floating Center (+) Button in Lavender / Soft Purple
        Box(
            modifier = Modifier
                .offset(y = (-18).dp)
                .size(54.dp)
                .shadow(elevation = 12.dp, shape = CircleShape, spotColor = Color(0xFF7C5CFC).copy(alpha = 0.5f))
                .clip(CircleShape)
                .background(Color(0xFF9477FA))
                .clickable { onFabClick() }
                .testTag("reference_fab_plus"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Transaction",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun NavigationItem(
    tab: ReferenceNavigationTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.title,
            tint = if (isSelected) TextPrimary else TextMuted,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = tab.title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 11.sp
            ),
            color = if (isSelected) TextPrimary else TextMuted
        )
    }
}
