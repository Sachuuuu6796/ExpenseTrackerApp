package com.sachin.expensetracker

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ExpenseRepository(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("ExpenseTrackerPrefs", Context.MODE_PRIVATE)

    private val gson = Gson()
    private val KEY_EXPENSES = "expenses"

    // Get all expenses
    fun getAllExpenses(): List<Expense> {
        val json = sharedPreferences.getString(KEY_EXPENSES, null) ?: return emptyList()
        val type = object : TypeToken<List<Expense>>() {}.type
        return gson.fromJson(json, type)
    }

    // Save expense
    fun saveExpense(expense: Expense) {
        val expenses = getAllExpenses().toMutableList()
        expenses.add(expense)
        saveExpenses(expenses)
    }

    // Delete expense
    fun deleteExpense(expense: Expense) {
        val expenses = getAllExpenses().toMutableList()
        expenses.removeAll { it.id == expense.id }
        saveExpenses(expenses)
    }

    // Delete all expenses
    fun deleteAllExpenses() {
        saveExpenses(emptyList())
    }

    // Private helper to save list
    private fun saveExpenses(expenses: List<Expense>) {
        val json = gson.toJson(expenses)
        sharedPreferences.edit().putString(KEY_EXPENSES, json).apply()
    }

    // Update existing expense
    fun updateExpense(expense: Expense) {
        val expenses = getAllExpenses().toMutableList()
        val index = expenses.indexOfFirst { it.id == expense.id }
        if (index != -1) {
            expenses[index] = expense
            saveExpenses(expenses)
        }
    }
}