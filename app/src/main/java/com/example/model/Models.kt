package com.example.model

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER
}

enum class PaymentMethod(val displayName: String, val iconName: String) {
    CASH("Cash", "payments"),
    BKASH("bKash", "mobile_friendly"),
    NAGAD("Nagad", "account_balance_wallet"),
    ROCKET("Rocket", "send_to_mobile"),
    BANK_CARD("Bank Card", "credit_card"),
    BANK_TRANSFER("Bank Transfer", "account_balance"),
    UPAY("Upay", "phone_android"),
    OTHER("Other", "more_horiz");

    companion object {
        fun fromString(value: String): PaymentMethod {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) } ?: CASH
        }
    }
}

data class CurrencyConfig(
    val code: String = "BDT",
    val symbol: String = "৳",
    val name: String = "Bangladeshi Taka",
    val flag: String = "🇧🇩"
) {
    fun format(amount: BigDecimal): String {
        val formatter = DecimalFormat("#,##0.00")
        return "$symbol ${formatter.format(amount)}"
    }

    fun formatCompact(amount: BigDecimal): String {
        val formatter = DecimalFormat("#,##0")
        return "$symbol ${formatter.format(amount)}"
    }

    companion object {
        val DEFAULT = CurrencyConfig("BDT", "৳", "Bangladeshi Taka", "🇧🇩")

        val SUPPORTED_CURRENCIES = listOf(
            CurrencyConfig("BDT", "৳", "Bangladeshi Taka", "🇧🇩"),
            CurrencyConfig("USD", "$", "US Dollar", "🇺🇸"),
            CurrencyConfig("EUR", "€", "Euro", "🇪🇺"),
            CurrencyConfig("GBP", "£", "British Pound", "🇬🇧"),
            CurrencyConfig("INR", "₹", "Indian Rupee", "🇮🇳"),
            CurrencyConfig("SAR", "﷼", "Saudi Riyal", "🇸🇦"),
            CurrencyConfig("AED", "د.إ", "UAE Dirham", "🇦🇪"),
            CurrencyConfig("CAD", "C$", "Canadian Dollar", "🇨🇦"),
            CurrencyConfig("AUD", "A$", "Australian Dollar", "🇦🇺"),
            CurrencyConfig("JPY", "¥", "Japanese Yen", "🇯🇵")
        )
    }
}

data class CategoryItem(
    val id: String,
    val name: String,
    val type: TransactionType,
    val icon: String,
    val colorHex: String,
    val isDefault: Boolean = true
)

data class TransactionItem(
    val id: String,
    val title: String,
    val amount: BigDecimal,
    val type: TransactionType,
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColorHex: String,
    val paymentMethod: PaymentMethod,
    val dateEpochMillis: Long,
    val description: String = "",
    val notes: String = "",
    val currencyCode: String = "BDT",
    val isSynced: Boolean = true
)

data class BudgetItem(
    val id: String,
    val categoryId: String? = null, // null means overall monthly budget
    val categoryName: String = "Overall Monthly",
    val limitAmount: BigDecimal,
    val spentAmount: BigDecimal = BigDecimal.ZERO,
    val monthYear: String, // format "YYYY-MM"
    val currencyCode: String = "BDT"
) {
    val remainingAmount: BigDecimal get() = (limitAmount - spentAmount).max(BigDecimal.ZERO)
    val percentageUsed: Float get() = if (limitAmount > BigDecimal.ZERO) {
        ((spentAmount.toDouble() / limitAmount.toDouble()) * 100).toFloat().coerceIn(0f, 200f)
    } else 0f
    val isNearLimit: Boolean get() = percentageUsed in 80f..99.99f
    val isOverBudget: Boolean get() = percentageUsed >= 100f
}

data class SavingsGoalItem(
    val id: String,
    val title: String,
    val targetAmount: BigDecimal,
    val currentAmount: BigDecimal,
    val targetDateEpochMillis: Long,
    val currencyCode: String = "BDT",
    val isArchived: Boolean = false,
    val colorHex: String = "#00A86B"
) {
    val remainingAmount: BigDecimal get() = (targetAmount - currentAmount).max(BigDecimal.ZERO)
    val progressFraction: Float get() = if (targetAmount > BigDecimal.ZERO) {
        (currentAmount.toDouble() / targetAmount.toDouble()).toFloat().coerceIn(0f, 1f)
    } else 0f
    val isReached: Boolean get() = currentAmount >= targetAmount
}

data class UserProfile(
    val id: String = "user_default",
    val name: String = "Fardin Hasan",
    val email: String = "ewrfardin123xyz@gmail.com",
    val phone: String = "+880 1712-345678",
    val occupation: String = "Software Engineer",
    val bio: String = "Tracking expenses and building financial freedom",
    val preferredCurrency: String = "BDT",
    val openingBalance: BigDecimal = BigDecimal("50000.00"),
    val serverBaseUrl: String = "http://10.0.2.2:8000/api/v1/",
    val isDarkTheme: Boolean? = null, // null means system default
    val isNotificationsEnabled: Boolean = true
)

data class FinancialSummary(
    val totalBalance: BigDecimal,
    val totalIncome: BigDecimal,
    val totalExpenses: BigDecimal,
    val netSavings: BigDecimal,
    val savingsRatePercentage: Float,
    val monthlyBudgetLimit: BigDecimal,
    val monthlyBudgetSpent: BigDecimal,
    val monthlyBudgetRemaining: BigDecimal
)

data class CategorySpendSummary(
    val categoryName: String,
    val categoryIcon: String,
    val categoryColorHex: String,
    val totalSpent: BigDecimal,
    val percentageOfTotal: Float,
    val transactionCount: Int
)

data class MonthlyTrendItem(
    val monthLabel: String,
    val income: BigDecimal,
    val expense: BigDecimal
)
