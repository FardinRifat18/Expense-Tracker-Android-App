package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val amount: String, // String representation for precise BigDecimal storage
    val type: String, // EXPENSE, INCOME, TRANSFER
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColorHex: String,
    val paymentMethod: String,
    val dateEpochMillis: Long,
    val description: String = "",
    val notes: String = "",
    val currencyCode: String = "BDT",
    val isSynced: Boolean = true
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val icon: String,
    val colorHex: String,
    val isDefault: Boolean = true
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val id: String,
    val categoryId: String?,
    val categoryName: String,
    val limitAmount: String,
    val monthYear: String, // "YYYY-MM"
    val currencyCode: String = "BDT"
)

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey val id: String,
    val title: String,
    val targetAmount: String,
    val currentAmount: String,
    val targetDateEpochMillis: Long,
    val currencyCode: String = "BDT",
    val isArchived: Boolean = false,
    val colorHex: String = "#00A86B"
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String = "user_default",
    val name: String,
    val email: String,
    val phone: String = "+880 1712-345678",
    val occupation: String = "Software Engineer",
    val bio: String = "Tracking expenses and building financial freedom",
    val preferredCurrency: String = "BDT",
    val openingBalance: String = "50000.00",
    val serverBaseUrl: String = "http://10.0.2.2:8000/api/v1/",
    val isDarkTheme: Boolean? = null,
    val isNotificationsEnabled: Boolean = true
)
