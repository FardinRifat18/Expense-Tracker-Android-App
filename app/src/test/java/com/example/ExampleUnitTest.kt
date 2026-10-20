package com.example

import com.example.model.BudgetItem
import com.example.model.CurrencyConfig
import com.example.model.PaymentMethod
import com.example.model.SavingsGoalItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class ExampleUnitTest {

    @Test
    fun testCurrencyFormatting_BangladeshiTaka() {
        val bdt = CurrencyConfig.DEFAULT
        assertEquals("BDT", bdt.code)
        assertEquals("৳", bdt.symbol)

        val formatted = bdt.format(BigDecimal("15450.50"))
        assertTrue("Must include Taka symbol ৳", formatted.contains("৳"))
        assertTrue("Must format comma separated thousands", formatted.contains("15,450.50"))
    }

    @Test
    fun testFinancialBalanceCalculation() {
        val openingBalance = BigDecimal("50000.00")
        val salaryIncome = BigDecimal("85000.00")
        val freelanceIncome = BigDecimal("25000.00")
        val rentExpense = BigDecimal("22000.00")
        val groceriesExpense = BigDecimal("8500.00")

        val totalIncome = salaryIncome + freelanceIncome
        val totalExpenses = rentExpense + groceriesExpense
        val netBalance = openingBalance + totalIncome - totalExpenses

        assertEquals(BigDecimal("110000.00"), totalIncome)
        assertEquals(BigDecimal("30500.00"), totalExpenses)
        assertEquals(BigDecimal("129500.00"), netBalance)
    }

    @Test
    fun testBudgetThresholds_Normal_Warning_OverLimit() {
        // Normal budget (50% spent)
        val normalBudget = BudgetItem(
            id = "bgt_1",
            categoryName = "Food & Dining",
            limitAmount = BigDecimal("10000.00"),
            spentAmount = BigDecimal("5000.00"),
            monthYear = "2026-10"
        )
        assertEquals(50f, normalBudget.percentageUsed, 0.01f)
        assertFalse(normalBudget.isNearLimit)
        assertFalse(normalBudget.isOverBudget)
        assertEquals(BigDecimal("5000.00"), normalBudget.remainingAmount)

        // 80% Warning threshold
        val warningBudget = BudgetItem(
            id = "bgt_2",
            categoryName = "Food & Dining",
            limitAmount = BigDecimal("10000.00"),
            spentAmount = BigDecimal("8500.00"),
            monthYear = "2026-10"
        )
        assertEquals(85f, warningBudget.percentageUsed, 0.01f)
        assertTrue(warningBudget.isNearLimit)
        assertFalse(warningBudget.isOverBudget)

        // Over 100% threshold
        val overBudget = BudgetItem(
            id = "bgt_3",
            categoryName = "Food & Dining",
            limitAmount = BigDecimal("10000.00"),
            spentAmount = BigDecimal("12000.00"),
            monthYear = "2026-10"
        )
        assertTrue(overBudget.isOverBudget)
        assertEquals(BigDecimal.ZERO, overBudget.remainingAmount)
    }

    @Test
    fun testSavingsGoalProgress() {
        val goal = SavingsGoalItem(
            id = "goal_test",
            title = "Sajek Tour",
            targetAmount = BigDecimal("30000.00"),
            currentAmount = BigDecimal("15000.00"),
            targetDateEpochMillis = System.currentTimeMillis() + 864000000L
        )

        assertEquals(0.5f, goal.progressFraction, 0.01f)
        assertEquals(0, BigDecimal("15000.00").compareTo(goal.remainingAmount))
        assertFalse(goal.isReached)

        val completedGoal = goal.copy(currentAmount = BigDecimal("30000.00"))
        assertTrue(completedGoal.isReached)
        assertEquals(0, BigDecimal.ZERO.compareTo(completedGoal.remainingAmount))
    }

    @Test
    fun testPaymentMethodParsing() {
        assertEquals(PaymentMethod.BKASH, PaymentMethod.fromString("bKash"))
        assertEquals(PaymentMethod.NAGAD, PaymentMethod.fromString("NAGAD"))
        assertEquals(PaymentMethod.ROCKET, PaymentMethod.fromString("rocket"))
        assertEquals(PaymentMethod.CASH, PaymentMethod.fromString("Cash"))
        assertEquals(PaymentMethod.BANK_TRANSFER, PaymentMethod.fromString("Bank Transfer"))
    }

    @Test
    fun testUserProfileDetailsUpdate() {
        val initialProfile = com.example.model.UserProfile(
            name = "Fardin Hasan",
            email = "ewrfardin123xyz@gmail.com",
            phone = "+880 1712-345678",
            occupation = "Software Engineer",
            bio = "Tracking expenses and building financial freedom"
        )
        assertEquals("Fardin Hasan", initialProfile.name)
        assertEquals("Software Engineer", initialProfile.occupation)

        val updated = initialProfile.copy(
            name = "Fardin H. Chowdhury",
            occupation = "Lead Engineer",
            phone = "+880 1819-000111",
            bio = "Building long term wealth with smart budgeting"
        )
        assertEquals("Fardin H. Chowdhury", updated.name)
        assertEquals("Lead Engineer", updated.occupation)
        assertEquals("+880 1819-000111", updated.phone)
        assertEquals("Building long term wealth with smart budgeting", updated.bio)
    }
}
