package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequestDto(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class RegisterRequestDto(
    @Json(name = "name") val name: String,
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "preferred_currency") val preferredCurrency: String = "BDT"
)

@JsonClass(generateAdapter = true)
data class AuthResponseDto(
    @Json(name = "access") val access: String,
    @Json(name = "refresh") val refresh: String?,
    @Json(name = "user") val user: UserProfileDto?
)

@JsonClass(generateAdapter = true)
data class UserProfileDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "email") val email: String,
    @Json(name = "preferred_currency") val preferredCurrency: String = "BDT",
    @Json(name = "opening_balance") val openingBalance: String = "0.00"
)

@JsonClass(generateAdapter = true)
data class TransactionDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "amount") val amount: String,
    @Json(name = "type") val type: String, // EXPENSE, INCOME, TRANSFER
    @Json(name = "category") val categoryId: String,
    @Json(name = "category_name") val categoryName: String? = null,
    @Json(name = "payment_method") val paymentMethod: String,
    @Json(name = "transaction_date") val transactionDate: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "currency") val currency: String = "BDT"
)

@JsonClass(generateAdapter = true)
data class CategoryDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "type") val type: String,
    @Json(name = "icon") val icon: String,
    @Json(name = "color") val color: String,
    @Json(name = "is_default") val isDefault: Boolean = true
)

@JsonClass(generateAdapter = true)
data class BudgetDto(
    @Json(name = "id") val id: String,
    @Json(name = "category") val categoryId: String?,
    @Json(name = "category_name") val categoryName: String? = null,
    @Json(name = "amount") val amount: String,
    @Json(name = "period") val period: String,
    @Json(name = "currency") val currency: String = "BDT"
)

@JsonClass(generateAdapter = true)
data class SavingsGoalDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "target_amount") val targetAmount: String,
    @Json(name = "current_amount") val currentAmount: String,
    @Json(name = "target_date") val targetDate: String,
    @Json(name = "status") val status: String = "active"
)

@JsonClass(generateAdapter = true)
data class AnalyticsSummaryDto(
    @Json(name = "total_balance") val totalBalance: String,
    @Json(name = "total_income") val totalIncome: String,
    @Json(name = "total_expenses") val totalExpenses: String,
    @Json(name = "net_savings") val netSavings: String,
    @Json(name = "currency") val currency: String = "BDT"
)
