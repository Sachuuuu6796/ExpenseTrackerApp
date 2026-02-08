package com.sachin.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions


// Robinhood-inspired Premium Black & Gold Theme
val DeepBlack = Color(0xFF0B0B0D)           // Background
val SoftBlack = Color(0xFF121214)           // Cards/Surfaces
val CharcoalBorder = Color(0xFF1C1C1E)      // Borders

val GoldAccent = Color(0xFFD4AF37)          // Primary Gold
val MutedGold = Color(0xFFC9A227)           // Secondary Gold
val HighlightGold = Color(0xFFE6C75A)       // Bright highlights

val WhiteText = Color(0xFFFFFFFF)           // Primary text
val SecondaryGray = Color(0xFFA1A1A6)       // Secondary text
val MutedGray = Color(0xFF6E6E73)           // Tertiary text

// Custom color scheme
private val DarkGoldColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = DeepBlack,
    primaryContainer = SoftBlack,
    onPrimaryContainer = GoldAccent,

    secondary = MutedGold,
    onSecondary = DeepBlack,
    secondaryContainer = CharcoalBorder,
    onSecondaryContainer = HighlightGold,

    background = DeepBlack,
    onBackground = WhiteText,

    surface = SoftBlack,
    onSurface = WhiteText,
    surfaceVariant = CharcoalBorder,
    onSurfaceVariant = SecondaryGray,

    outline = CharcoalBorder,

    error = Color(0xFFCF6679),
    onError = DeepBlack
)

@Composable
fun ExTTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkGoldColorScheme,
        content = content
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExTTheme {
                MainNavigation()
            }
        }
    }
}

@Composable
fun MainNavigation(authViewModel: AuthViewModel = viewModel()) {
    val authState by authViewModel.authState.collectAsState()

    when (authState) {
        is AuthState.Success -> ExpenseTrackerApp(authViewModel)
        else -> LoginScreen(onLoginSuccess = {})
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerApp(
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val viewModel: ExpenseViewModel = remember {
        ExpenseViewModel(context.applicationContext as Application)
    }

    var showAddDialog by remember { mutableStateOf(false) }
    val filteredExpenses = viewModel.getFilteredExpenses()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()

    Scaffold(
        containerColor = DeepBlack,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppLogo()
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("ExT", color = WhiteText)
                    }
                },
                actions = {
                    IconButton(onClick = { authViewModel.signOut() }) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = GoldAccent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SoftBlack,
                    titleContentColor = WhiteText
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = GoldAccent,
                contentColor = DeepBlack
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Month/Year Selector
            MonthYearSelector(
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
                onMonthChange = { viewModel.setMonth(it) },
                onYearChange = { viewModel.setYear(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Balance Summary Card
            BalanceSummaryCard(
                credit = viewModel.getTotalCredit(),
                debit = viewModel.getTotalDebit(),
                balance = viewModel.getNetBalance()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Transaction list
            if (filteredExpenses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No transactions for this month.\nTap + to add one!",
                        color = SecondaryGray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn {
                    items(filteredExpenses.sortedByDescending { it.date }) { expense ->
                        ExpenseItem(
                            expense = expense,
                            onDelete = { viewModel.deleteExpense(expense) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddExpenseDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { amount, category, description, type, date ->
                viewModel.addExpense(amount, category, description, type, date)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun MonthYearSelector(
    selectedMonth: Int,
    selectedYear: Int,
    onMonthChange: (Int) -> Unit,
    onYearChange: (Int) -> Unit
) {
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous month button
        IconButton(onClick = {
            if (selectedMonth == 0) {
                onMonthChange(11)
                onYearChange(selectedYear - 1)
            } else {
                onMonthChange(selectedMonth - 1)
            }
        }) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Previous Month",
                tint = GoldAccent
            )
        }

        // Month and Year display
        Text(
            text = "${months[selectedMonth]} $selectedYear",
            style = MaterialTheme.typography.titleLarge,
            color = WhiteText,
            fontWeight = FontWeight.Bold
        )

        // Next month button
        IconButton(onClick = {
            if (selectedMonth == 11) {
                onMonthChange(0)
                onYearChange(selectedYear + 1)
            } else {
                onMonthChange(selectedMonth + 1)
            }
        }) {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Next Month",
                tint = GoldAccent
            )
        }
    }
}

@Composable
fun BalanceSummaryCard(
    credit: Double,
    debit: Double,
    balance: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SoftBlack
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Net Balance
            Text(
                "Net Balance",
                style = MaterialTheme.typography.labelMedium,
                color = SecondaryGray
            )
            Text(
                formatCurrency(balance),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = if (balance >= 0) GoldAccent else Color(0xFFCF6679)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Credit and Debit Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Credit
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Income",
                            style = MaterialTheme.typography.labelSmall,
                            color = SecondaryGray
                        )
                    }
                    Text(
                        formatCurrency(credit),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                }

                // Debit
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color(0xFFCF6679),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Expenses",
                            style = MaterialTheme.typography.labelSmall,
                            color = SecondaryGray
                        )
                    }
                    Text(
                        formatCurrency(debit),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = WhiteText
                    )
                }
            }
        }
    }
}

@Composable
fun ExpenseItem(expense: Expense, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SoftBlack
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Credit/Debit indicator
                    Icon(
                        imageVector = if (expense.type == ExpenseType.CREDIT)
                            Icons.Default.Add else Icons.Default.Delete,
                        contentDescription = null,
                        tint = if (expense.type == ExpenseType.CREDIT)
                            GoldAccent else Color(0xFFCF6679),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = expense.category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = WhiteText
                    )
                }
                if (expense.description.isNotEmpty()) {
                    Text(
                        text = expense.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryGray
                    )
                }
                Text(
                    text = formatDate(expense.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedGray
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${if (expense.type == ExpenseType.CREDIT) "+" else "-"}${formatCurrency(expense.amount)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (expense.type == ExpenseType.CREDIT) GoldAccent else WhiteText
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFCF6679)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onAdd: (Double, String, String, ExpenseType, Long) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ExpenseType.DEBIT) }
    var selectedCategory by remember {
        mutableStateOf(ExpenseCategories.debitCategories[0])
    }
    var description by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Selected date (default to current date)
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    val categories = if (selectedType == ExpenseType.DEBIT)
        ExpenseCategories.debitCategories
    else
        ExpenseCategories.creditCategories

    // Date picker state
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDateMillis
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SoftBlack,
        title = { Text("Add Transaction", color = WhiteText) },
        text = {
            Column {
                // Type Selector (Credit/Debit)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Debit Button
                    Button(
                        onClick = {
                            selectedType = ExpenseType.DEBIT
                            selectedCategory = ExpenseCategories.debitCategories[0]
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == ExpenseType.DEBIT)
                                CharcoalBorder else DeepBlack,
                            contentColor = if (selectedType == ExpenseType.DEBIT)
                                GoldAccent else SecondaryGray
                        )
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Expense")
                    }

                    // Credit Button
                    Button(
                        onClick = {
                            selectedType = ExpenseType.CREDIT
                            selectedCategory = ExpenseCategories.creditCategories[0]
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == ExpenseType.CREDIT)
                                CharcoalBorder else DeepBlack,
                            contentColor = if (selectedType == ExpenseType.CREDIT)
                                GoldAccent else SecondaryGray
                        )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Income")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date Selector
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = WhiteText
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CharcoalBorder)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        formatDateShort(selectedDateMillis),
                        color = WhiteText
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount", color = SecondaryGray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WhiteText,
                        unfocusedTextColor = WhiteText,
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = CharcoalBorder,
                        cursorColor = GoldAccent
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category", color = SecondaryGray) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WhiteText,
                            unfocusedTextColor = WhiteText,
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = CharcoalBorder
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(SoftBlack)
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category, color = WhiteText) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)", color = SecondaryGray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WhiteText,
                        unfocusedTextColor = WhiteText,
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = CharcoalBorder,
                        cursorColor = GoldAccent
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    if (amountValue != null && amountValue > 0) {
                        onAdd(amountValue, selectedCategory, description, selectedType, selectedDateMillis)
                    }
                }
            ) {
                Text("Add", color = GoldAccent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SecondaryGray)
            }
        }
    )

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDateMillis = it
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", color = GoldAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = SecondaryGray)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = SoftBlack
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = SoftBlack,
                    titleContentColor = WhiteText,
                    headlineContentColor = WhiteText,
                    weekdayContentColor = SecondaryGray,
                    subheadContentColor = SecondaryGray,
                    yearContentColor = WhiteText,
                    currentYearContentColor = GoldAccent,
                    selectedYearContentColor = DeepBlack,
                    selectedYearContainerColor = GoldAccent,
                    dayContentColor = WhiteText,
                    selectedDayContentColor = DeepBlack,
                    selectedDayContainerColor = GoldAccent,
                    todayContentColor = GoldAccent,
                    todayDateBorderColor = GoldAccent
                )
            )
        }
    }
}

// Helper functions
fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    return format.format(amount)
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun formatDateShort(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}