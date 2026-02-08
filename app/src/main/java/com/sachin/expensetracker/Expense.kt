package com.sachin.expensetracker

data class Expense(
    val id: Long = System.currentTimeMillis(),
    val amount: Double,
    val category: String,
    val description: String,
    val date: Long = System.currentTimeMillis(),
    val type: ExpenseType = ExpenseType.DEBIT  // New field
)

enum class ExpenseType {
    CREDIT,  // Money received/income
    DEBIT    // Money spent/expense
}

object ExpenseCategories {
    val debitCategories = listOf(
        "Food",
        "Transport",
        "Shopping",
        "Bills",
        "Entertainment",
        "Health",
        "Other"
    )

    val creditCategories = listOf(
        "Salary",
        "Freelance",
        "Investment",
        "Gift",
        "Refund",
        "Other Income"
    )

    // Keep for backwards compatibility
    val categories = debitCategories
}