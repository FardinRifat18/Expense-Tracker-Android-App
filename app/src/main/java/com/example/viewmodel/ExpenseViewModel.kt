package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ExpenseDatabase
import com.example.data.repository.ExpenseRepository
import com.example.model.BudgetItem
import com.example.model.CategoryItem
import com.example.model.CategorySpendSummary
import com.example.model.CurrencyConfig
import com.example.model.FinancialSummary
import com.example.model.MonthlyTrendItem
import com.example.model.PaymentMethod
import com.example.model.SavingsGoalItem
import com.example.model.TransactionItem
import com.example.model.TransactionType
import com.example.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class PeriodFilter(val displayName: String) {
    THIS_MONTH("This Month"),
    LAST_MONTH("Last Month"),
    ALL_TIME("All Time")
}

enum class SortOrder(val displayName: String) {
    NEWEST("Newest"),
    OLDEST("Oldest"),
    HIGHEST_AMOUNT("Highest Amount"),
    LOWEST_AMOUNT("Lowest Amount")
}

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ExpenseDatabase.getDatabase(application, viewModelScope)
    val repository = ExpenseRepository(database)

    // Current month identifier
    private val calendar = Calendar.getInstance()
    private val currentMonthYear: String
        get() {
            val cal = Calendar.getInstance()
            return String.format("%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
        }

    // Selected period filter
    private val _selectedPeriod = MutableStateFlow(PeriodFilter.THIS_MONTH)
    val selectedPeriod: StateFlow<PeriodFilter> = _selectedPeriod.asStateFlow()

    // Transaction list filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _typeFilter = MutableStateFlow<TransactionType?>(null)
    val typeFilter: StateFlow<TransactionType?> = _typeFilter.asStateFlow()

    private val _categoryFilter = MutableStateFlow<String?>(null)
    val categoryFilter: StateFlow<String?> = _categoryFilter.asStateFlow()

    private val _paymentMethodFilter = MutableStateFlow<PaymentMethod?>(null)
    val paymentMethodFilter: StateFlow<PaymentMethod?> = _paymentMethodFilter.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.NEWEST)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    // Status banner
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _isBackendSyncing = MutableStateFlow(false)
    val isBackendSyncing: StateFlow<Boolean> = _isBackendSyncing.asStateFlow()

    // Selected transaction for view/edit
    private val _selectedTransaction = MutableStateFlow<TransactionItem?>(null)
    val selectedTransaction: StateFlow<TransactionItem?> = _selectedTransaction.asStateFlow()

    // Base flows from repository
    val allTransactions: StateFlow<List<TransactionItem>> = repository.transactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<CategoryItem>> = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    val rawBudgets: StateFlow<List<BudgetItem>> = repository.getBudgetsForMonth(currentMonthYear)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savingsGoals: StateFlow<List<SavingsGoalItem>> = repository.savingsGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Currency Config (BDT ৳ as default)
    val currentCurrency: StateFlow<CurrencyConfig> = userProfile.combine(_selectedPeriod) { profile, _ ->
        CurrencyConfig.SUPPORTED_CURRENCIES.find { it.code == profile.preferredCurrency }
            ?: CurrencyConfig.DEFAULT
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CurrencyConfig.DEFAULT)

    // Filtered Transactions according to search, period, type, category, sort
    val filteredTransactions: StateFlow<List<TransactionItem>> = combine(
        allTransactions,
        _selectedPeriod,
        _searchQuery,
        _typeFilter,
        _categoryFilter,
        _paymentMethodFilter,
        _sortOrder
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val txs = args[0] as List<TransactionItem>
        val period = args[1] as PeriodFilter
        val query = (args[2] as String).trim().lowercase()
        val type = args[3] as TransactionType?
        val catId = args[4] as String?
        val paymentMethod = args[5] as PaymentMethod?
        val sort = args[6] as SortOrder

        val (startTime, endTime) = getPeriodTimeRange(period)

        txs.filter { tx ->
            val matchesPeriod = (tx.dateEpochMillis in startTime..endTime)
            val matchesQuery = query.isEmpty() ||
                    tx.title.lowercase().contains(query) ||
                    tx.categoryName.lowercase().contains(query) ||
                    tx.notes.lowercase().contains(query)
            val matchesType = type == null || tx.type == type
            val matchesCat = catId == null || tx.categoryId == catId
            val matchesPayment = paymentMethod == null || tx.paymentMethod == paymentMethod

            matchesPeriod && matchesQuery && matchesType && matchesCat && matchesPayment
        }.let { list ->
            when (sort) {
                SortOrder.NEWEST -> list.sortedByDescending { it.dateEpochMillis }
                SortOrder.OLDEST -> list.sortedBy { it.dateEpochMillis }
                SortOrder.HIGHEST_AMOUNT -> list.sortedByDescending { it.amount }
                SortOrder.LOWEST_AMOUNT -> list.sortedBy { it.amount }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Derived Financial Summary with precise Opening Balance accounting
    val financialSummary: StateFlow<FinancialSummary> = combine(
        allTransactions,
        _selectedPeriod,
        userProfile,
        rawBudgets
    ) { txs, period, profile, budgets ->
        val (startTime, endTime) = getPeriodTimeRange(period)

        // 1. Overall Balance: openingBalance + (ALL income ever) - (ALL expense ever)
        val allTimeIncome = txs.filter { it.type == TransactionType.INCOME }
            .fold(BigDecimal.ZERO) { acc, item -> acc + item.amount }
        val allTimeExpenses = txs.filter { it.type == TransactionType.EXPENSE }
            .fold(BigDecimal.ZERO) { acc, item -> acc + item.amount }
        val totalBalance = profile.openingBalance + allTimeIncome - allTimeExpenses

        // 2. Period specific Income & Expenses
        val periodTxs = txs.filter { it.dateEpochMillis in startTime..endTime }
        val periodIncome = periodTxs.filter { it.type == TransactionType.INCOME }
            .fold(BigDecimal.ZERO) { acc, item -> acc + item.amount }
        val periodExpenses = periodTxs.filter { it.type == TransactionType.EXPENSE }
            .fold(BigDecimal.ZERO) { acc, item -> acc + item.amount }
        val netSavings = periodIncome - periodExpenses

        val savingsRate = if (periodIncome > BigDecimal.ZERO && netSavings > BigDecimal.ZERO) {
            ((netSavings.toDouble() / periodIncome.toDouble()) * 100).toFloat().coerceIn(0f, 100f)
        } else 0f

        // 3. Budgets spent calculation for this period
        val overallBudget = budgets.find { it.categoryId == null }
        val budgetLimit = overallBudget?.limitAmount ?: BigDecimal("50000.00")
        val currentMonthTxs = txs.filter {
            val (mStart, mEnd) = getPeriodTimeRange(PeriodFilter.THIS_MONTH)
            it.dateEpochMillis in mStart..mEnd && it.type == TransactionType.EXPENSE
        }
        val monthlyBudgetSpent = currentMonthTxs.fold(BigDecimal.ZERO) { acc, tx -> acc + tx.amount }
        val monthlyBudgetRemaining = (budgetLimit - monthlyBudgetSpent).max(BigDecimal.ZERO)

        FinancialSummary(
            totalBalance = totalBalance,
            totalIncome = periodIncome,
            totalExpenses = periodExpenses,
            netSavings = netSavings,
            savingsRatePercentage = savingsRate,
            monthlyBudgetLimit = budgetLimit,
            monthlyBudgetSpent = monthlyBudgetSpent,
            monthlyBudgetRemaining = monthlyBudgetRemaining
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary(
        totalBalance = BigDecimal("55000.00"),
        totalIncome = BigDecimal.ZERO,
        totalExpenses = BigDecimal.ZERO,
        netSavings = BigDecimal.ZERO,
        savingsRatePercentage = 0f,
        monthlyBudgetLimit = BigDecimal("50000.00"),
        monthlyBudgetSpent = BigDecimal.ZERO,
        monthlyBudgetRemaining = BigDecimal("50000.00")
    ))

    // Budgets enriched with real calculated spend for the current month
    val enrichedBudgets: StateFlow<List<BudgetItem>> = combine(
        rawBudgets,
        allTransactions
    ) { budgets, txs ->
        val (mStart, mEnd) = getPeriodTimeRange(PeriodFilter.THIS_MONTH)
        val monthExpenses = txs.filter { it.dateEpochMillis in mStart..mEnd && it.type == TransactionType.EXPENSE }

        budgets.map { bgt ->
            val spent = if (bgt.categoryId == null) {
                monthExpenses.fold(BigDecimal.ZERO) { acc, tx -> acc + tx.amount }
            } else {
                monthExpenses.filter { it.categoryId == bgt.categoryId }
                    .fold(BigDecimal.ZERO) { acc, tx -> acc + tx.amount }
            }
            bgt.copy(spentAmount = spent)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Category Spend Breakdown for active period
    val categorySpendSummary: StateFlow<List<CategorySpendSummary>> = combine(
        allTransactions,
        _selectedPeriod
    ) { txs, period ->
        val (startTime, endTime) = getPeriodTimeRange(period)
        val periodExpenses = txs.filter { it.dateEpochMillis in startTime..endTime && it.type == TransactionType.EXPENSE }
        val totalExpenses = periodExpenses.fold(BigDecimal.ZERO) { acc, tx -> acc + tx.amount }

        if (totalExpenses == BigDecimal.ZERO) {
            emptyList()
        } else {
            periodExpenses.groupBy { it.categoryName }
                .map { (catName, items) ->
                    val catTotal = items.fold(BigDecimal.ZERO) { acc, tx -> acc + tx.amount }
                    val percent = ((catTotal.toDouble() / totalExpenses.toDouble()) * 100).toFloat()
                    val first = items.first()
                    CategorySpendSummary(
                        categoryName = catName,
                        categoryIcon = first.categoryIcon,
                        categoryColorHex = first.categoryColorHex,
                        totalSpent = catTotal,
                        percentageOfTotal = percent,
                        transactionCount = items.size
                    )
                }.sortedByDescending { it.totalSpent }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 6-Month Trend Data
    val monthlyTrends: StateFlow<List<MonthlyTrendItem>> = allTransactions.map { txs ->
        val cal = Calendar.getInstance()
        val formatMonth = SimpleDateFormat("MMM", Locale.getDefault())
        val result = mutableListOf<MonthlyTrendItem>()

        for (i in 5 downTo 0) {
            val c = Calendar.getInstance().apply {
                add(Calendar.MONTH, -i)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val start = c.timeInMillis
            val end = c.apply {
                set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            val monthTxs = txs.filter { it.dateEpochMillis in start..end }
            val inc = monthTxs.filter { it.type == TransactionType.INCOME }.fold(BigDecimal.ZERO) { a, b -> a + b.amount }
            val exp = monthTxs.filter { it.type == TransactionType.EXPENSE }.fold(BigDecimal.ZERO) { a, b -> a + b.amount }

            result.add(MonthlyTrendItem(formatMonth.format(Date(start)), inc, exp))
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Actions
    fun setPeriodFilter(period: PeriodFilter) {
        _selectedPeriod.value = period
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setTypeFilter(type: TransactionType?) {
        _typeFilter.value = type
    }

    fun setCategoryFilter(categoryId: String?) {
        _categoryFilter.value = categoryId
    }

    fun setPaymentMethodFilter(method: PaymentMethod?) {
        _paymentMethodFilter.value = method
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun selectTransaction(transaction: TransactionItem?) {
        _selectedTransaction.value = transaction
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun addTransaction(
        title: String,
        amount: BigDecimal,
        type: TransactionType,
        category: CategoryItem,
        paymentMethod: PaymentMethod,
        dateEpochMillis: Long = System.currentTimeMillis(),
        description: String = "",
        notes: String = ""
    ) {
        viewModelScope.launch {
            try {
                val currency = currentCurrency.value.code
                repository.addTransaction(
                    title = title,
                    amount = amount,
                    type = type,
                    category = category,
                    paymentMethod = paymentMethod,
                    dateEpochMillis = dateEpochMillis,
                    description = description,
                    notes = notes,
                    currencyCode = currency
                )
                val typeLabel = if (type == TransactionType.INCOME) "Income" else "Expense"
                _statusMessage.value = "✓ $typeLabel ${currentCurrency.value.symbol} ${amount.setScale(2, RoundingMode.HALF_UP)} added!"
            } catch (e: Exception) {
                _statusMessage.value = "Failed to add transaction: ${e.message}"
            }
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteTransaction(id)
                _statusMessage.value = "Transaction deleted successfully"
                _selectedTransaction.value = null
            } catch (e: Exception) {
                _statusMessage.value = "Error deleting transaction: ${e.message}"
            }
        }
    }

    fun addCategory(name: String, type: TransactionType, icon: String, colorHex: String) {
        viewModelScope.launch {
            try {
                repository.addCategory(name, type, icon, colorHex)
                _statusMessage.value = "Category \"$name\" created"
            } catch (e: Exception) {
                _statusMessage.value = "Error creating category: ${e.message}"
            }
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            try {
                // Safeguard: Check if used by any transaction
                val isUsed = allTransactions.value.any { it.categoryId == id }
                if (isUsed) {
                    _statusMessage.value = "Cannot delete: Category is used by existing transactions"
                    return@launch
                }
                repository.deleteCategory(id)
                _statusMessage.value = "Category removed"
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun saveBudget(categoryId: String?, categoryName: String, limit: BigDecimal) {
        viewModelScope.launch {
            try {
                repository.saveBudget(
                    categoryId = categoryId,
                    categoryName = categoryName,
                    limitAmount = limit,
                    monthYear = currentMonthYear,
                    currencyCode = currentCurrency.value.code
                )
                _statusMessage.value = "Budget for \"$categoryName\" set to ${currentCurrency.value.symbol} $limit"
            } catch (e: Exception) {
                _statusMessage.value = "Error saving budget: ${e.message}"
            }
        }
    }

    fun deleteBudget(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteBudget(id)
                _statusMessage.value = "Budget removed"
            } catch (e: Exception) {
                _statusMessage.value = "Error deleting budget"
            }
        }
    }

    fun addSavingsGoal(title: String, targetAmount: BigDecimal, initialAmount: BigDecimal, targetDateEpoch: Long, colorHex: String) {
        viewModelScope.launch {
            try {
                repository.addSavingsGoal(
                    title = title,
                    targetAmount = targetAmount,
                    initialAmount = initialAmount,
                    targetDateEpochMillis = targetDateEpoch,
                    currencyCode = currentCurrency.value.code,
                    colorHex = colorHex
                )
                _statusMessage.value = "Savings goal \"$title\" created!"
            } catch (e: Exception) {
                _statusMessage.value = "Error creating goal: ${e.message}"
            }
        }
    }

    fun contributeToSavingsGoal(goalId: String, amount: BigDecimal, goalTitle: String) {
        viewModelScope.launch {
            try {
                repository.contributeToSavingsGoal(goalId, amount)
                _statusMessage.value = "Deposited ${currentCurrency.value.symbol} $amount to $goalTitle!"
            } catch (e: Exception) {
                _statusMessage.value = "Error contributing to goal: ${e.message}"
            }
        }
    }

    fun deleteSavingsGoal(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteSavingsGoal(id)
                _statusMessage.value = "Savings goal deleted"
            } catch (e: Exception) {
                _statusMessage.value = "Error removing goal"
            }
        }
    }

    fun updateCurrency(currencyCode: String) {
        viewModelScope.launch {
            val current = userProfile.value
            repository.updateUserProfile(current.copy(preferredCurrency = currencyCode))
            val selected = CurrencyConfig.SUPPORTED_CURRENCIES.find { it.code == currencyCode }
            _statusMessage.value = "Currency switched to ${selected?.name} (${selected?.symbol})"
        }
    }

    fun updateProfileDetails(
        name: String,
        email: String,
        phone: String = "",
        occupation: String = "",
        bio: String = ""
    ) {
        viewModelScope.launch {
            val current = userProfile.value
            val updated = current.copy(
                name = name.trim().ifBlank { current.name },
                email = email.trim().ifBlank { current.email },
                phone = phone.trim(),
                occupation = occupation.trim(),
                bio = bio.trim()
            )
            repository.updateUserProfile(updated)
            _statusMessage.value = "✓ Profile details updated successfully!"
        }
    }

    fun updateOpeningBalance(balance: BigDecimal) {
        viewModelScope.launch {
            val current = userProfile.value
            repository.updateUserProfile(current.copy(openingBalance = balance))
            _statusMessage.value = "Opening Balance set to ${currentCurrency.value.symbol} $balance"
        }
    }

    fun updateServerUrl(url: String) {
        viewModelScope.launch {
            val current = userProfile.value
            repository.updateUserProfile(current.copy(serverBaseUrl = url))
            _statusMessage.value = "Backend Server URL updated"
        }
    }

    fun testBackendSync() {
        viewModelScope.launch {
            _isBackendSyncing.value = true
            val result = repository.testBackendConnection()
            _isBackendSyncing.value = false
            result.onSuccess { msg ->
                _statusMessage.value = msg
            }.onFailure { err ->
                _statusMessage.value = err.message
            }
        }
    }

    fun exportTransactionsCsv(): String {
        val txs = filteredTransactions.value
        val csv = repository.generateCsvExport(txs, currentCurrency.value)
        _statusMessage.value = "Exported ${txs.size} transactions to CSV"
        return csv
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllTransactions()
            _statusMessage.value = "All transactions cleared"
        }
    }

    private fun getPeriodTimeRange(period: PeriodFilter): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        return when (period) {
            PeriodFilter.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            PeriodFilter.LAST_MONTH -> {
                cal.add(Calendar.MONTH, -1)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            PeriodFilter.ALL_TIME -> {
                Pair(0L, Long.MAX_VALUE)
            }
        }
    }
}
