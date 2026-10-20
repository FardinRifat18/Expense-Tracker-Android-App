package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        SavingsGoalEntity::class,
        UserProfileEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: ExpenseDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): ExpenseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExpenseDatabase::class.java,
                    "expenseflow_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            suspend fun populateInitialData(db: ExpenseDatabase) {
                // 1. Categories
                val defaultCategories = listOf(
                    // Expense Categories
                    CategoryEntity("cat_exp_food", "Food & Dining", "EXPENSE", "restaurant", "#EF4444", true),
                    CategoryEntity("cat_exp_transport", "Transportation", "EXPENSE", "directions_car", "#F59E0B", true),
                    CategoryEntity("cat_exp_shopping", "Shopping", "EXPENSE", "shopping_bag", "#EC4899", true),
                    CategoryEntity("cat_exp_bills", "Bills & Utilities", "EXPENSE", "receipt_long", "#8B5CF6", true),
                    CategoryEntity("cat_exp_entertainment", "Entertainment", "EXPENSE", "movie", "#3B82F6", true),
                    CategoryEntity("cat_exp_health", "Health & Medical", "EXPENSE", "local_hospital", "#10B981", true),
                    CategoryEntity("cat_exp_education", "Education", "EXPENSE", "school", "#6366F1", true),
                    CategoryEntity("cat_exp_rent", "Housing & Rent", "EXPENSE", "home", "#14B8A6", true),
                    CategoryEntity("cat_exp_personal", "Personal Care", "EXPENSE", "face", "#F43F5E", true),
                    CategoryEntity("cat_exp_other", "Other Expenses", "EXPENSE", "category", "#64748B", true),

                    // Income Categories
                    CategoryEntity("cat_inc_salary", "Monthly Salary", "INCOME", "payments", "#10B981", true),
                    CategoryEntity("cat_inc_freelance", "Freelancing", "INCOME", "laptop_mac", "#06B6D4", true),
                    CategoryEntity("cat_inc_business", "Business Revenue", "INCOME", "business_center", "#3B82F6", true),
                    CategoryEntity("cat_inc_investment", "Investments & Dividends", "INCOME", "trending_up", "#8B5CF6", true),
                    CategoryEntity("cat_inc_gift", "Gifts & Bonus", "INCOME", "card_giftcard", "#F59E0B", true),
                    CategoryEntity("cat_inc_other", "Other Income", "INCOME", "account_balance", "#64748B", true)
                )
                db.categoryDao().insertCategories(defaultCategories)

                // 2. User Profile with BDT (৳) default
                val defaultProfile = UserProfileEntity(
                    id = "user_default",
                    name = "Fardin Hasan",
                    email = "ewrfardin123xyz@gmail.com",
                    phone = "+880 1712-345678",
                    occupation = "Software Engineer",
                    bio = "Tracking expenses and building financial freedom",
                    preferredCurrency = "BDT",
                    openingBalance = "55000.00",
                    serverBaseUrl = "http://10.0.2.2:8000/api/v1/",
                    isDarkTheme = null,
                    isNotificationsEnabled = true
                )
                db.userProfileDao().saveUserProfile(defaultProfile)

                // 3. Transactions in Bangladeshi Taka (৳)
                val now = System.currentTimeMillis()
                val oneDay = 86_400_000L
                val calendar = Calendar.getInstance()
                val currentYearMonth = String.format("%04d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)

                val sampleTransactions = listOf(
                    TransactionEntity(
                        id = "tx_1",
                        title = "Monthly Software Engineering Salary",
                        amount = "85000.00",
                        type = "INCOME",
                        categoryId = "cat_inc_salary",
                        categoryName = "Monthly Salary",
                        categoryIcon = "payments",
                        categoryColorHex = "#10B981",
                        paymentMethod = "Bank Transfer",
                        dateEpochMillis = now - (oneDay * 2),
                        description = "Direct payroll deposit from Dhaka Tech",
                        notes = "Salary for current cycle",
                        currencyCode = "BDT"
                    ),
                    TransactionEntity(
                        id = "tx_2",
                        title = "Shwapno Supermarket Grocery",
                        amount = "4250.00",
                        type = "EXPENSE",
                        categoryId = "cat_exp_food",
                        categoryName = "Food & Dining",
                        categoryIcon = "restaurant",
                        categoryColorHex = "#EF4444",
                        paymentMethod = "bKash",
                        dateEpochMillis = now - (oneDay * 1),
                        description = "Monthly essentials and fresh groceries",
                        notes = "Cashback received in bKash",
                        currencyCode = "BDT"
                    ),
                    TransactionEntity(
                        id = "tx_3",
                        title = "Apartment Rent (Gulshan / Banani)",
                        amount = "22000.00",
                        type = "EXPENSE",
                        categoryId = "cat_exp_rent",
                        categoryName = "Housing & Rent",
                        categoryIcon = "home",
                        categoryColorHex = "#14B8A6",
                        paymentMethod = "Bank Transfer",
                        dateEpochMillis = now - (oneDay * 5),
                        description = "Monthly apartment rental maintenance",
                        notes = "Paid through City Bank",
                        currencyCode = "BDT"
                    ),
                    TransactionEntity(
                        id = "tx_4",
                        title = "Freelance Mobile App Design Client",
                        amount = "32000.00",
                        type = "INCOME",
                        categoryId = "cat_inc_freelance",
                        categoryName = "Freelancing",
                        categoryIcon = "laptop_mac",
                        categoryColorHex = "#06B6D4",
                        paymentMethod = "Bank Transfer",
                        dateEpochMillis = now - (oneDay * 7),
                        description = "UI/UX sprint milestone delivery",
                        notes = "Remote client payout",
                        currencyCode = "BDT"
                    ),
                    TransactionEntity(
                        id = "tx_5",
                        title = "DESCO Electricity & Fiber Internet Bill",
                        amount = "3850.00",
                        type = "EXPENSE",
                        categoryId = "cat_exp_bills",
                        categoryName = "Bills & Utilities",
                        categoryIcon = "receipt_long",
                        categoryColorHex = "#8B5CF6",
                        paymentMethod = "Nagad",
                        dateEpochMillis = now - (oneDay * 3),
                        description = "Prepaid smart electricity card & WiFi",
                        notes = "Nagad utility bill pay",
                        currencyCode = "BDT"
                    ),
                    TransactionEntity(
                        id = "tx_6",
                        title = "Uber & CNG Commute",
                        amount = "920.00",
                        type = "EXPENSE",
                        categoryId = "cat_exp_transport",
                        categoryName = "Transportation",
                        categoryIcon = "directions_car",
                        categoryColorHex = "#F59E0B",
                        paymentMethod = "Cash",
                        dateEpochMillis = now - (oneDay * 1),
                        description = "Office commute and weekend errands",
                        notes = "City transport",
                        currencyCode = "BDT"
                    ),
                    TransactionEntity(
                        id = "tx_7",
                        title = "Dine Out at Kacchi Bhai",
                        amount = "1650.00",
                        type = "EXPENSE",
                        categoryId = "cat_exp_food",
                        categoryName = "Food & Dining",
                        categoryIcon = "restaurant",
                        categoryColorHex = "#EF4444",
                        paymentMethod = "bKash",
                        dateEpochMillis = now,
                        description = "Dinner with university friends",
                        notes = "Splitted dinner bill",
                        currencyCode = "BDT"
                    ),
                    TransactionEntity(
                        id = "tx_8",
                        title = "Stock Market Dividend (CSE / DSE)",
                        amount = "7500.00",
                        type = "INCOME",
                        categoryId = "cat_inc_investment",
                        categoryName = "Investments & Dividends",
                        categoryIcon = "trending_up",
                        categoryColorHex = "#8B5CF6",
                        paymentMethod = "Bank Transfer",
                        dateEpochMillis = now - (oneDay * 9),
                        description = "Annual mutual fund dividend distribution",
                        notes = "Reinvested into savings",
                        currencyCode = "BDT"
                    )
                )
                db.transactionDao().insertTransactions(sampleTransactions)

                // 4. Budgets for this month
                val sampleBudgets = listOf(
                    BudgetEntity(
                        id = "bgt_overall",
                        categoryId = null,
                        categoryName = "Overall Monthly",
                        limitAmount = "45000.00",
                        monthYear = currentYearMonth,
                        currencyCode = "BDT"
                    ),
                    BudgetEntity(
                        id = "bgt_food",
                        categoryId = "cat_exp_food",
                        categoryName = "Food & Dining",
                        limitAmount = "12000.00",
                        monthYear = currentYearMonth,
                        currencyCode = "BDT"
                    ),
                    BudgetEntity(
                        id = "bgt_transport",
                        categoryId = "cat_exp_transport",
                        categoryName = "Transportation",
                        limitAmount = "5000.00",
                        monthYear = currentYearMonth,
                        currencyCode = "BDT"
                    ),
                    BudgetEntity(
                        id = "bgt_bills",
                        categoryId = "cat_exp_bills",
                        categoryName = "Bills & Utilities",
                        limitAmount = "6000.00",
                        monthYear = currentYearMonth,
                        currencyCode = "BDT"
                    )
                )
                db.budgetDao().insertBudgets(sampleBudgets)

                // 5. Savings Goals
                val targetDate1 = calendar.apply { add(Calendar.MONTH, 6) }.timeInMillis
                val targetDate2 = calendar.apply { add(Calendar.MONTH, 12) }.timeInMillis
                val sampleGoals = listOf(
                    SavingsGoalEntity(
                        id = "goal_1",
                        title = "Emergency Fund (6 Months Buffer)",
                        targetAmount = "200000.00",
                        currentAmount = "125000.00",
                        targetDateEpochMillis = targetDate1,
                        currencyCode = "BDT",
                        colorHex = "#00A86B"
                    ),
                    SavingsGoalEntity(
                        id = "goal_2",
                        title = "Cox's Bazar & Sajek Family Vacation",
                        targetAmount = "45000.00",
                        currentAmount = "28000.00",
                        targetDateEpochMillis = targetDate2,
                        currencyCode = "BDT",
                        colorHex = "#3B82F6"
                    ),
                    SavingsGoalEntity(
                        id = "goal_3",
                        title = "New MacBook Pro M-Series",
                        targetAmount = "180000.00",
                        currentAmount = "95000.00",
                        targetDateEpochMillis = targetDate2,
                        currencyCode = "BDT",
                        colorHex = "#8B5CF6"
                    )
                )
                db.savingsGoalDao().insertGoals(sampleGoals)
            }
        }
    }
}
