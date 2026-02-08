package com.sachin.expensetracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ExpenseRepository(application)

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _selectedMonth = MutableStateFlow(getCurrentMonth())
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _selectedYear = MutableStateFlow(getCurrentYear())
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    init {
        loadExpenses()
    }

    private fun loadExpenses() {
        _expenses.value = repository.getAllExpenses()
    }

    fun addExpense(amount: Double, category: String, description: String, type: ExpenseType,date: Long) {
        val newExpense = Expense(
            amount = amount,
            category = category,
            description = description,
            type = type,
            date = date
        )
        repository.saveExpense(newExpense)
        loadExpenses()
    }

    fun deleteExpense(expense: Expense) {
        repository.deleteExpense(expense)
        loadExpenses()
    }

    // Filter expenses by selected month and year
    fun getFilteredExpenses(): List<Expense> {
        return _expenses.value.filter { expense ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = expense.date
            val expenseMonth = calendar.get(Calendar.MONTH)
            val expenseYear = calendar.get(Calendar.YEAR)

            expenseMonth == _selectedMonth.value && expenseYear == _selectedYear.value
        }
    }

    // Get total credit (income)
    fun getTotalCredit(): Double {
        return getFilteredExpenses()
            .filter { it.type == ExpenseType.CREDIT }
            .sumOf { it.amount }
    }

    // Get total debit (expenses)
    fun getTotalDebit(): Double {
        return getFilteredExpenses()
            .filter { it.type == ExpenseType.DEBIT }
            .sumOf { it.amount }
    }

    // Get net balance (credit - debit)
    fun getNetBalance(): Double {
        return getTotalCredit() - getTotalDebit()
    }

    fun getTotalSpending(): Double {
        return getTotalDebit()
    }

    fun getSpendingByCategory(): Map<String, Double> {
        return getFilteredExpenses()
            .filter { it.type == ExpenseType.DEBIT }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    fun setMonth(month: Int) {
        _selectedMonth.value = month
    }

    fun setYear(year: Int) {
        _selectedYear.value = year
    }

    fun deleteAllExpenses() {
        repository.deleteAllExpenses()
        loadExpenses()
    }

    private fun getCurrentMonth(): Int {
        return Calendar.getInstance().get(Calendar.MONTH)
    }

    private fun getCurrentYear(): Int {
        return Calendar.getInstance().get(Calendar.YEAR)
    }
}