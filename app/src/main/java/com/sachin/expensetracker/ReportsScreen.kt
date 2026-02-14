package com.sachin.expensetracker

import android.app.Application
import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.components.Legend
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ExpenseViewModel,  // Add this parameter
    onBackClick: () -> Unit
) {

    val filteredExpenses = viewModel.getFilteredExpenses()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()

    Scaffold(
        containerColor = DeepBlack,
        topBar = {
            TopAppBar(
                title = { Text("Reports", color = WhiteText) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = GoldAccent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SoftBlack,
                    titleContentColor = WhiteText
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Month/Year Header
            item {
                MonthYearSelector(
                    selectedMonth = selectedMonth,
                    selectedYear = selectedYear,
                    onMonthChange = { viewModel.setMonth(it) },
                    onYearChange = { viewModel.setYear(it) }
                )
            }

            // Summary Cards
            item {
                ReportsSummaryCards(
                    totalCredit = viewModel.getTotalCredit(),
                    totalDebit = viewModel.getTotalDebit(),
                    netBalance = viewModel.getNetBalance(),
                    transactionCount = filteredExpenses.size
                )
            }

            // Spending by Category - Pie Chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftBlack)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Spending by Category",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = WhiteText
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        if (viewModel.getTotalDebit() > 0) {
                            CategoryPieChart(
                                spendingByCategory = viewModel.getSpendingByCategory()
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No expenses this month",
                                    color = SecondaryGray
                                )
                            }
                        }
                    }
                }
            }

            // Category Breakdown List
            item {
                CategoryBreakdownList(
                    spendingByCategory = viewModel.getSpendingByCategory(),
                    totalSpending = viewModel.getTotalDebit()
                )
            }

            // Top Expenses
            item {
                TopExpensesList(
                    expenses = filteredExpenses
                        .filter { it.type == ExpenseType.DEBIT }
                        .sortedByDescending { it.amount }
                        .take(5)
                )
            }
        }
    }
}

@Composable
fun ReportsSummaryCards(
    totalCredit: Double,
    totalDebit: Double,
    netBalance: Double,
    transactionCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Income
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = SoftBlack)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Income", fontSize = 12.sp, color = SecondaryGray)
                Text(
                    formatCurrency(totalCredit),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )
            }
        }

        // Expenses
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = SoftBlack)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Expenses", fontSize = 12.sp, color = SecondaryGray)
                Text(
                    formatCurrency(totalDebit),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = WhiteText
                )
            }
        }

        // Transactions
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = SoftBlack)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Count", fontSize = 12.sp, color = SecondaryGray)
                Text(
                    "$transactionCount",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = WhiteText
                )
            }
        }
    }
}

@Composable
fun CategoryPieChart(spendingByCategory: Map<String, Double>) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                setDrawEntryLabels(false)
                legend.apply {
                    isEnabled = true
                    textColor = android.graphics.Color.parseColor("#FFFFFF")
                    textSize = 12f
                    verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                    horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                    orientation = Legend.LegendOrientation.HORIZONTAL
                    setDrawInside(false)
                }
                setHoleColor(android.graphics.Color.parseColor("#0B0B0D"))
                holeRadius = 60f
                transparentCircleRadius = 65f
                setDrawCenterText(true)
                centerText = "Categories"
                setCenterTextColor(android.graphics.Color.parseColor("#D4AF37"))
                setCenterTextSize(16f)
            }
        },
        update = { chart ->
            val entries = spendingByCategory.entries.mapIndexed { index, entry ->
                PieEntry(entry.value.toFloat(), entry.key)
            }

            val colors = listOf(
                android.graphics.Color.parseColor("#D4AF37"), // Gold
                android.graphics.Color.parseColor("#E6C75A"), // Highlight Gold
                android.graphics.Color.parseColor("#C9A227"), // Muted Gold
                android.graphics.Color.parseColor("#B8860B"), // Dark Gold
                android.graphics.Color.parseColor("#FFD700"), // Bright Gold
                android.graphics.Color.parseColor("#DAA520"), // Goldenrod
                android.graphics.Color.parseColor("#F0E68C")  // Khaki
            )

            val dataSet = PieDataSet(entries, "").apply {
                setColors(colors)
                valueTextColor = android.graphics.Color.parseColor("#0B0B0D")
                valueTextSize = 12f
                sliceSpace = 2f
            }

            chart.data = PieData(dataSet).apply {
                setValueFormatter(object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "₹${value.toInt()}"
                    }
                })
            }

            chart.animateY(1000)
            chart.invalidate()
        }
    )
}

@Composable
fun CategoryBreakdownList(
    spendingByCategory: Map<String, Double>,
    totalSpending: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SoftBlack)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Category Breakdown",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = WhiteText
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (spendingByCategory.isEmpty()) {
                Text(
                    "No data available",
                    color = SecondaryGray,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                spendingByCategory.entries
                    .sortedByDescending { it.value }
                    .forEach { (category, amount) ->
                        val percentage = if (totalSpending > 0) {
                            (amount / totalSpending * 100).toInt()
                        } else 0

                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(category, color = WhiteText)
                                Text(
                                    formatCurrency(amount),
                                    color = GoldAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))

                            // Progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .background(CharcoalBorder, RoundedCornerShape(4.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(percentage / 100f)
                                        .height(8.dp)
                                        .background(GoldAccent, RoundedCornerShape(4.dp))
                                )
                            }

                            Text(
                                "$percentage%",
                                fontSize = 12.sp,
                                color = SecondaryGray,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        if (category != spendingByCategory.keys.last()) {
                            Divider(
                                color = CharcoalBorder,
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
            }
        }
    }
}

@Composable
fun TopExpensesList(expenses: List<Expense>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SoftBlack)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Top 5 Expenses",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = WhiteText
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (expenses.isEmpty()) {
                Text(
                    "No expenses yet",
                    color = SecondaryGray,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                expenses.forEachIndexed { index, expense ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rank badge
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        if (index == 0) GoldAccent else CharcoalBorder,
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${index + 1}",
                                    color = if (index == 0) DeepBlack else SecondaryGray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    expense.category,
                                    color = WhiteText,
                                    fontWeight = FontWeight.Medium
                                )
                                if (expense.description.isNotEmpty()) {
                                    Text(
                                        expense.description,
                                        fontSize = 12.sp,
                                        color = SecondaryGray
                                    )
                                }
                            }
                        }

                        Text(
                            formatCurrency(expense.amount),
                            color = if (index == 0) GoldAccent else WhiteText,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (index < expenses.size - 1) {
                        Divider(color = CharcoalBorder, thickness = 1.dp)
                    }
                }
            }
        }
    }
}