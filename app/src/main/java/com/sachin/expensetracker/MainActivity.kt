package com.sachin.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.graphics.Color
import android.app.Application
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.CircleShape

// Custom colors - Robinhood Premium Theme
val DeepBlack = Color(0xFF0B0B0D)
val SoftBlack = Color(0xFF121214)
val CharcoalBorder = Color(0xFF1C1C1E)
val GoldAccent = Color(0xFFD4AF37)
val MutedGold = Color(0xFFC9A227)
val HighlightGold = Color(0xFFE6C75A)
val WhiteText = Color(0xFFFFFFFF)
val SecondaryGray = Color(0xFFA1A1A6)
val MutedGray = Color(0xFF6E6E73)

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

@Composable
fun ExpenseTrackerApp(
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val viewModel: ExpenseViewModel = remember {
        ExpenseViewModel(context.applicationContext as Application)
    }

    var currentScreen by remember { mutableStateOf("home") }

    when (currentScreen) {
        "home" -> HomeScreen(
            viewModel = viewModel,
            authViewModel = authViewModel,
            onNavigateToReports = { currentScreen = "reports" },
            onNavigateToSettings = { currentScreen = "settings" }
        )
        "reports" -> ReportsScreen(
            viewModel = viewModel,
            onBackClick = { currentScreen = "home" }
        )
        "settings" -> SettingsScreen(
            onBackClick = { currentScreen = "home" },
            authViewModel = authViewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ExpenseViewModel,
    authViewModel: AuthViewModel,
    onNavigateToReports: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var expenseToEdit by remember { mutableStateOf<Expense?>(null) }

    val filteredExpenses = viewModel.getFilteredExpenses()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()

    Scaffold(
        containerColor = DeepBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text("ExT", color = WhiteText)
                },
                navigationIcon = {
                    Spacer(modifier = Modifier.width(8.dp))
                    UserProfileIcon(
                        onNavigateToReports = onNavigateToReports,
                        onNavigateToSettings = onNavigateToSettings,
                        onLogout = {
                            authViewModel.signOut()
                        }
                    )
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
            MonthYearSelector(
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
                onMonthChange = { viewModel.setMonth(it) },
                onYearChange = { viewModel.setYear(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            BalanceSummaryCard(
                credit = viewModel.getTotalCredit(),
                debit = viewModel.getTotalDebit(),
                balance = viewModel.getNetBalance()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredExpenses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No transactions for this month.\nTap + to add one!",
                        color = SecondaryGray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn {
                    items(filteredExpenses.sortedByDescending { it.date }) { expense ->
                        ExpenseItem(
                            expense = expense,
                            onDelete = { viewModel.deleteExpense(expense) },
                            onEdit = {
                                expenseToEdit = expense
                                showEditDialog = true
                            }
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

    if (showEditDialog && expenseToEdit != null) {
        EditExpenseDialog(
            expense = expenseToEdit!!,
            onDismiss = {
                showEditDialog = false
                expenseToEdit = null
            },
            onUpdate = { updatedExpense ->
                viewModel.updateExpense(updatedExpense)
                showEditDialog = false
                expenseToEdit = null
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

        Text(
            text = "${months[selectedMonth]} $selectedYear",
            style = MaterialTheme.typography.titleLarge,
            color = WhiteText,
            fontWeight = FontWeight.Bold
        )

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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
fun ExpenseItem(
    expense: Expense,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
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
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (expense.type == ExpenseType.CREDIT)
                                GoldAccent.copy(alpha = 0.2f)
                            else
                                CharcoalBorder,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = ExpenseCategories.getIconForCategory(expense.category),
                        contentDescription = null,
                        tint = if (expense.type == ExpenseType.CREDIT)
                            GoldAccent
                        else
                            WhiteText,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = expense.category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = WhiteText
                    )
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
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (expense.type == ExpenseType.CREDIT) "+" else "-"}${
                            formatCurrency(
                                expense.amount
                            )
                        }",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (expense.type == ExpenseType.CREDIT) GoldAccent else WhiteText
                    )
                }

                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = GoldAccent
                    )
                }

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
        mutableStateOf(ExpenseCategories.debitCategories[0].name)
    }
    var description by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    val categories = if (selectedType == ExpenseType.DEBIT)
        ExpenseCategories.debitCategories
    else
        ExpenseCategories.creditCategories

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDateMillis
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SoftBlack,
        title = { Text("Add Transaction", color = WhiteText) },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            selectedType = ExpenseType.DEBIT
                            selectedCategory = ExpenseCategories.debitCategories[0].name
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

                    Button(
                        onClick = {
                            selectedType = ExpenseType.CREDIT
                            selectedCategory = ExpenseCategories.creditCategories[0].name
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
                        val categoryItems = if (selectedType == ExpenseType.DEBIT)
                            ExpenseCategories.debitCategories
                        else
                            ExpenseCategories.creditCategories

                        categoryItems.forEach { categoryItem ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = categoryItem.icon,
                                            contentDescription = null,
                                            tint = GoldAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(categoryItem.name, color = WhiteText)
                                    }
                                },
                                onClick = {
                                    selectedCategory = categoryItem.name
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseDialog(
    expense: Expense,
    onDismiss: () -> Unit,
    onUpdate: (Expense) -> Unit
) {
    var amount by remember { mutableStateOf(expense.amount.toString()) }
    var selectedType by remember { mutableStateOf(expense.type) }
    var selectedCategory by remember { mutableStateOf(expense.category) }
    var description by remember { mutableStateOf(expense.description) }
    var expanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf(expense.date) }

    val categories = if (selectedType == ExpenseType.DEBIT)
        ExpenseCategories.debitCategories
    else
        ExpenseCategories.creditCategories

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDateMillis
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SoftBlack,
        title = { Text("Edit Transaction", color = WhiteText) },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            selectedType = ExpenseType.DEBIT
                            selectedCategory = ExpenseCategories.debitCategories[0].name
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

                    Button(
                        onClick = {
                            selectedType = ExpenseType.CREDIT
                            selectedCategory = ExpenseCategories.creditCategories[0].name
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
                        val categoryItems = if (selectedType == ExpenseType.DEBIT)
                            ExpenseCategories.debitCategories
                        else
                            ExpenseCategories.creditCategories

                        categoryItems.forEach { categoryItem ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = categoryItem.icon,
                                            contentDescription = null,
                                            tint = GoldAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(categoryItem.name, color = WhiteText)
                                    }
                                },
                                onClick = {
                                    selectedCategory = categoryItem.name
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
                        val updatedExpense = expense.copy(
                            amount = amountValue,
                            category = selectedCategory,
                            description = description,
                            type = selectedType,
                            date = selectedDateMillis
                        )
                        onUpdate(updatedExpense)
                    }
                }
            ) {
                Text("Update", color = GoldAccent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SecondaryGray)
            }
        }
    )

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