package com.example.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ExpenseFlowApiService {
    // Auth endpoints
    @POST("auth/login/")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthResponseDto>

    @POST("auth/register/")
    suspend fun register(@Body request: RegisterRequestDto): Response<AuthResponseDto>

    @GET("auth/me/")
    suspend fun getProfile(): Response<UserProfileDto>

    // Transactions endpoints
    @GET("transactions/")
    suspend fun getTransactions(
        @Query("type") type: String? = null,
        @Query("category") categoryId: String? = null,
        @Query("ordering") ordering: String? = null
    ): Response<List<TransactionDto>>

    @POST("transactions/")
    suspend fun createTransaction(@Body transaction: TransactionDto): Response<TransactionDto>

    @PATCH("transactions/{id}/")
    suspend fun updateTransaction(
        @Path("id") id: String,
        @Body transaction: TransactionDto
    ): Response<TransactionDto>

    @DELETE("transactions/{id}/")
    suspend fun deleteTransaction(@Path("id") id: String): Response<Unit>

    // Categories endpoints
    @GET("categories/")
    suspend fun getCategories(): Response<List<CategoryDto>>

    @POST("categories/")
    suspend fun createCategory(@Body category: CategoryDto): Response<CategoryDto>

    // Budgets endpoints
    @GET("budgets/")
    suspend fun getBudgets(@Query("period") period: String? = null): Response<List<BudgetDto>>

    @POST("budgets/")
    suspend fun createBudget(@Body budget: BudgetDto): Response<BudgetDto>

    // Savings Goals
    @GET("savings/goals/")
    suspend fun getSavingsGoals(): Response<List<SavingsGoalDto>>

    @POST("savings/goals/")
    suspend fun createSavingsGoal(@Body goal: SavingsGoalDto): Response<SavingsGoalDto>

    // Analytics
    @GET("analytics/summary/")
    suspend fun getAnalyticsSummary(): Response<AnalyticsSummaryDto>
}
