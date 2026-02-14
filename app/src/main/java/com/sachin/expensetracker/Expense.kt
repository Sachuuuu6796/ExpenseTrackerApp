package com.sachin.expensetracker

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class Expense(
    val id: Long = System.currentTimeMillis(),
    val amount: Double,
    val category: String,
    val description: String,
    val date: Long = System.currentTimeMillis(),
    val type: ExpenseType = ExpenseType.DEBIT
)

enum class ExpenseType {
    CREDIT,
    DEBIT
}

data class CategoryItem(
    val name: String,
    val icon: ImageVector
)

object ExpenseCategories {
    val debitCategories = listOf(
        CategoryItem("Food", Icons.Default.Restaurant),
        CategoryItem("Transport", Icons.Default.DirectionsCar),
        CategoryItem("Shopping", Icons.Default.ShoppingCart),
        CategoryItem("Bills", Icons.Default.Receipt),
        CategoryItem("Entertainment", Icons.Default.Movie),
        CategoryItem("Health", Icons.Default.LocalHospital),
        CategoryItem("Education", Icons.Default.School),
        CategoryItem("Travel", Icons.Default.Flight),
        CategoryItem("Groceries", Icons.Default.ShoppingBag),
        CategoryItem("Other", Icons.Default.MoreHoriz)
    )

    val creditCategories = listOf(
        CategoryItem("Salary", Icons.Default.AccountBalance),
        CategoryItem("Freelance", Icons.Default.Work),
        CategoryItem("Investment", Icons.Default.TrendingUp),
        CategoryItem("Gift", Icons.Default.CardGiftcard),
        CategoryItem("Refund", Icons.Default.MoneyOff),
        CategoryItem("Bonus", Icons.Default.Stars),
        CategoryItem("Other Income", Icons.Default.AttachMoney)
    )

    // Helper function to get icon for a category name
    fun getIconForCategory(categoryName: String): ImageVector {
        val allCategories = debitCategories + creditCategories
        return allCategories.find { it.name == categoryName }?.icon ?: Icons.Default.MoreHoriz
    }
}