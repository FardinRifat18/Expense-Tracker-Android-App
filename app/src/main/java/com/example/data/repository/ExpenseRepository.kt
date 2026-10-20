package com.example.data.repository

import com.example.data.local.BudgetEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.ExpenseDatabase
import com.example.data.local.SavingsGoalEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UserProfileEntity
import com.example.data.remote.ApiClient
import com.example.model.BudgetItem
import com.example.model.CategoryItem
import com.example.model.CurrencyConfig
import com.example.model.PaymentMethod
import com.example.model.SavingsGoalItem
import com.example.model.TransactionItem
import com.example.model.TransactionType
import com.example.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ExpenseRepository(private val database: ExpenseDatabase) {

    private val transactionDao = database.transactionDao()
    private val categoryDao = database.categoryDao()
    private val budgetDao = database.budgetDao()
    private val savingsGoalDao = database.savingsGoalDao()
    private val userProfileDao = database.userProfileDao()

    val transactions: Flow<List<TransactionItem>> = transactionDao.getAllTransactions()
        .map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)

    val categories: Flow<List<CategoryItem>> = categoryDao.getAllCategories()
        .map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)

    fun getBudgetsForMonth(monthYear: String): Flow<List<BudgetItem>> =
        budgetDao.getBudgetsForMonth(monthYear)
            .map { entities ->
                entities.map { it.toDomain() }
            }.flowOn(Dispatchers.IO)

    val savingsGoals: Flow<List<SavingsGoalItem>> = savingsGoalDao.getAllGoals()
        .map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)

    val userProfile: Flow<UserProfile> = userProfileDao.getUserProfile()
        .map { entity ->
            entity?.toDomain() ?: UserProfile()
        }.flowOn(Dispatchers.IO)

    suspend fun addTransaction(
        title: String,
        amount: BigDecimal,
        type: TransactionType,
        category: CategoryItem,
        paymentMethod: PaymentMethod,
        dateEpochMillis: Long,
        description: String = "",
        notes: String = "",
        currencyCode: String = "BDT"
    ): String = withContext(Dispatchers.IO) {
        val id = "tx_" + UUID.randomUUID().toString().take(8)
        val entity = TransactionEntity(
            id = id,
            title = title,
            amount = amount.toPlainString(),
            type = type.name,
            categoryId = category.id,
            categoryName = category.name,
            categoryIcon = category.icon,
            categoryColorHex = category.colorHex,
            paymentMethod = paymentMethod.displayName,
            dateEpochMillis = dateEpochMillis,
            description = description,
            notes = notes,
            currencyCode = currencyCode,
            isSynced = false
        )
        transactionDao.insertTransaction(entity)
        id
    }

    suspend fun updateTransaction(transaction: TransactionItem) = withContext(Dispatchers.IO) {
        transactionDao.updateTransaction(transaction.toEntity())
    }

    suspend fun deleteTransaction(id: String) = withContext(Dispatchers.IO) {
        transactionDao.deleteById(id)
    }

    suspend fun addCategory(
        name: String,
        type: TransactionType,
        icon: String,
        colorHex: String
    ): String = withContext(Dispatchers.IO) {
        val id = "cat_custom_" + UUID.randomUUID().toString().take(6)
        val entity = CategoryEntity(
            id = id,
            name = name,
            type = type.name,
            icon = icon,
            colorHex = colorHex,
            isDefault = false
        )
        categoryDao.insertCategory(entity)
        id
    }

    suspend fun deleteCategory(id: String) = withContext(Dispatchers.IO) {
        categoryDao.deleteById(id)
    }

    suspend fun saveBudget(
        categoryId: String?,
        categoryName: String,
        limitAmount: BigDecimal,
        monthYear: String,
        currencyCode: String = "BDT"
    ) = withContext(Dispatchers.IO) {
        val id = "bgt_${monthYear}_${categoryId ?: "overall"}"
        val entity = BudgetEntity(
            id = id,
            categoryId = categoryId,
            categoryName = categoryName,
            limitAmount = limitAmount.toPlainString(),
            monthYear = monthYear,
            currencyCode = currencyCode
        )
        budgetDao.insertBudget(entity)
    }

    suspend fun deleteBudget(id: String) = withContext(Dispatchers.IO) {
        budgetDao.deleteById(id)
    }

    suspend fun addSavingsGoal(
        title: String,
        targetAmount: BigDecimal,
        initialAmount: BigDecimal,
        targetDateEpochMillis: Long,
        currencyCode: String = "BDT",
        colorHex: String = "#00A86B"
    ): String = withContext(Dispatchers.IO) {
        val id = "goal_" + UUID.randomUUID().toString().take(6)
        val entity = SavingsGoalEntity(
            id = id,
            title = title,
            targetAmount = targetAmount.toPlainString(),
            currentAmount = initialAmount.toPlainString(),
            targetDateEpochMillis = targetDateEpochMillis,
            currencyCode = currencyCode,
            colorHex = colorHex
        )
        savingsGoalDao.insertGoal(entity)
        id
    }

    suspend fun contributeToSavingsGoal(
        goalId: String,
        contributionAmount: BigDecimal
    ) = withContext(Dispatchers.IO) {
        val goal = savingsGoalDao.getGoalById(goalId) ?: return@withContext
        val current = BigDecimal(goal.currentAmount)
        val updated = current + contributionAmount
        savingsGoalDao.updateGoal(goal.copy(currentAmount = updated.toPlainString()))
    }

    suspend fun deleteSavingsGoal(id: String) = withContext(Dispatchers.IO) {
        savingsGoalDao.deleteById(id)
    }

    suspend fun updateUserProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        userProfileDao.saveUserProfile(profile.toEntity())
        ApiClient.setBaseUrl(profile.serverBaseUrl)
    }

    suspend fun clearAllTransactions() = withContext(Dispatchers.IO) {
        transactionDao.deleteAll()
    }

    suspend fun resetDatabaseToDefaults() = withContext(Dispatchers.IO) {
        database.clearAllTables()
        // Re-seed initial data
        val callback = database.openHelper.writableDatabase
        val defaultProfile = UserProfileEntity(
            id = "user_default",
            name = "Fardin Hasan",
            email = "ewrfardin123xyz@gmail.com",
            phone = "+880 1712-345678",
            occupation = "Software Engineer",
            bio = "Tracking expenses and building financial freedom",
            preferredCurrency = "BDT",
            openingBalance = "50000.00",
            serverBaseUrl = "http://10.0.2.2:8000/api/v1/",
            isDarkTheme = null,
            isNotificationsEnabled = true
        )
        userProfileDao.saveUserProfile(defaultProfile)
    }

    suspend fun testBackendConnection(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val service = ApiClient.getService()
            val response = service.getProfile()
            if (response.isSuccessful) {
                Result.success("Connected to Django REST backend successfully (Status ${response.code()})")
            } else {
                Result.success("Django server reached (HTTP ${response.code()}: ${response.message()})")
            }
        } catch (e: Exception) {
            Result.failure(Exception("Could not connect to Django server at ${ApiClient.getBaseUrl()}: ${e.localizedMessage ?: "Connection refused"}"))
        }
    }

    fun generateCsvExport(transactions: List<TransactionItem>, currency: CurrencyConfig): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val sb = StringBuilder()
        sb.append("ID,Date,Title,Type,Category,Amount (${currency.code}),Currency,Payment Method,Notes\n")
        transactions.forEach { tx ->
            val dateStr = dateFormat.format(Date(tx.dateEpochMillis))
            val cleanTitle = tx.title.replace(",", ";").replace("\"", "'")
            val cleanNotes = tx.notes.replace(",", ";").replace("\"", "'")
            sb.append("${tx.id},\"$dateStr\",\"$cleanTitle\",${tx.type.name},\"${tx.categoryName}\",${tx.amount},${currency.symbol},${tx.paymentMethod.displayName},\"$cleanNotes\"\n")
        }
        return sb.toString()
    }

    // Mapping extensions
    private fun TransactionEntity.toDomain() = TransactionItem(
        id = id,
        title = title,
        amount = BigDecimal(amount),
        type = runCatching { TransactionType.valueOf(type) }.getOrDefault(TransactionType.EXPENSE),
        categoryId = categoryId,
        categoryName = categoryName,
        categoryIcon = categoryIcon,
        categoryColorHex = categoryColorHex,
        paymentMethod = PaymentMethod.fromString(paymentMethod),
        dateEpochMillis = dateEpochMillis,
        description = description,
        notes = notes,
        currencyCode = currencyCode,
        isSynced = isSynced
    )

    private fun TransactionItem.toEntity() = TransactionEntity(
        id = id,
        title = title,
        amount = amount.toPlainString(),
        type = type.name,
        categoryId = categoryId,
        categoryName = categoryName,
        categoryIcon = categoryIcon,
        categoryColorHex = categoryColorHex,
        paymentMethod = paymentMethod.displayName,
        dateEpochMillis = dateEpochMillis,
        description = description,
        notes = notes,
        currencyCode = currencyCode,
        isSynced = isSynced
    )

    private fun CategoryEntity.toDomain() = CategoryItem(
        id = id,
        name = name,
        type = runCatching { TransactionType.valueOf(type) }.getOrDefault(TransactionType.EXPENSE),
        icon = icon,
        colorHex = colorHex,
        isDefault = isDefault
    )

    private fun BudgetEntity.toDomain() = BudgetItem(
        id = id,
        categoryId = categoryId,
        categoryName = categoryName,
        limitAmount = BigDecimal(limitAmount),
        monthYear = monthYear,
        currencyCode = currencyCode
    )

    private fun SavingsGoalEntity.toDomain() = SavingsGoalItem(
        id = id,
        title = title,
        targetAmount = BigDecimal(targetAmount),
        currentAmount = BigDecimal(currentAmount),
        targetDateEpochMillis = targetDateEpochMillis,
        currencyCode = currencyCode,
        isArchived = isArchived,
        colorHex = colorHex
    )

    private fun UserProfileEntity.toDomain() = UserProfile(
        id = id,
        name = name,
        email = email,
        phone = phone,
        occupation = occupation,
        bio = bio,
        preferredCurrency = preferredCurrency,
        openingBalance = BigDecimal(openingBalance),
        serverBaseUrl = serverBaseUrl,
        isDarkTheme = isDarkTheme,
        isNotificationsEnabled = isNotificationsEnabled
    )

    private fun UserProfile.toEntity() = UserProfileEntity(
        id = id,
        name = name,
        email = email,
        phone = phone,
        occupation = occupation,
        bio = bio,
        preferredCurrency = preferredCurrency,
        openingBalance = openingBalance.toPlainString(),
        serverBaseUrl = serverBaseUrl,
        isDarkTheme = isDarkTheme,
        isNotificationsEnabled = isNotificationsEnabled
    )
}
