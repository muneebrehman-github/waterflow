package com.spgg.app.ui.shopowner.customers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spgg.app.domain.BottleBalance
import com.spgg.app.domain.Payment
import com.spgg.app.ui.auth.LocalShopCapabilities
import com.spgg.app.ui.ledger.LedgerStatementView

private val PAYMENT_METHODS = listOf("CASH", "BANK_TRANSFER", "OTHER")
private val ADJUSTMENT_TYPES = listOf("LOST", "DAMAGED")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopCustomerLedgerScreen(onBack: () -> Unit, viewModel: ShopCustomerLedgerViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val capabilities = LocalShopCapabilities.current
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableStateOf(0) }
    var showRecordDialog by remember { mutableStateOf(false) }
    var voidTargetId by remember { mutableStateOf<Long?>(null) }
    var showAdjustmentDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.customerName.ifBlank { "Customer Ledger" }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            val showFab = when (selectedTab) {
                1 -> capabilities.canRecordPayments
                2 -> capabilities.canAdjustBottles
                else -> false
            }
            if (showFab) {
                FloatingActionButton(onClick = {
                    if (selectedTab == 2) showAdjustmentDialog = true else showRecordDialog = true
                }) {
                    Icon(Icons.Default.Add, contentDescription = if (selectedTab == 2) "Mark Lost/Damaged" else "Record Payment")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(it) } }
    ) { padding ->
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            else -> Column(Modifier.fillMaxSize().padding(padding)) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Ledger") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Payments") })
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Bottles") })
                }
                when (selectedTab) {
                    0 -> LedgerStatementView(balance = uiState.balance, entries = uiState.entries, modifier = Modifier.weight(1f))
                    1 -> PaymentsList(
                        payments = uiState.payments,
                        modifier = Modifier.weight(1f),
                        canVoid = capabilities.canVoidPayments,
                        onVoid = { paymentId -> voidTargetId = paymentId }
                    )
                    else -> BottleBalanceView(balance = uiState.bottleBalance, modifier = Modifier.weight(1f))
                }
            }
        }
    }

    if (showRecordDialog) {
        RecordPaymentDialog(
            isSubmitting = uiState.isMutating,
            onDismiss = { showRecordDialog = false },
            onConfirm = { amount, method, reference ->
                viewModel.recordPayment(amount, method, reference)
                showRecordDialog = false
            }
        )
    }

    voidTargetId?.let { paymentId ->
        VoidPaymentDialog(
            onDismiss = { voidTargetId = null },
            onConfirm = { reason ->
                viewModel.voidPayment(paymentId, reason)
                voidTargetId = null
            }
        )
    }

    if (showAdjustmentDialog) {
        MarkAdjustmentDialog(
            maxQuantity = uiState.bottleBalance?.outstanding ?: 0,
            isSubmitting = uiState.isMutating,
            onDismiss = { showAdjustmentDialog = false },
            onConfirm = { quantity, type, reason ->
                viewModel.markBottleAdjustment(quantity, type, reason)
                showAdjustmentDialog = false
            }
        )
    }
}

@Composable
private fun BottleBalanceView(balance: BottleBalance?, modifier: Modifier) {
    if (balance == null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Couldn't load bottle balance") }
        return
    }
    Column(modifier.fillMaxSize().padding(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Outstanding bottles", style = MaterialTheme.typography.labelMedium)
                Text(balance.outstanding.toString(), style = MaterialTheme.typography.headlineMedium)
            }
        }
        Spacer(Modifier.height(16.dp))
        BottleStatRow("Delivered", balance.delivered)
        BottleStatRow("Returned", balance.returned)
        BottleStatRow("Lost", balance.lost)
        BottleStatRow("Damaged", balance.damaged)
    }
}

@Composable
private fun BottleStatRow(label: String, value: Int) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value.toString(), style = MaterialTheme.typography.bodyMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MarkAdjustmentDialog(
    maxQuantity: Int,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (quantity: Int, type: String, reason: String) -> Unit
) {
    var quantityText by remember { mutableStateOf(if (maxQuantity > 0) "1" else "0") }
    var type by remember { mutableStateOf(ADJUSTMENT_TYPES.first()) }
    var reason by remember { mutableStateOf("") }
    var typeExpanded by remember { mutableStateOf(false) }
    val quantity = quantityText.toIntOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mark Lost / Damaged") },
        text = {
            Column {
                Text("Currently outstanding: $maxQuantity", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("Quantity") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Box {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = {
                            IconButton(onClick = { typeExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Choose type")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        ADJUSTMENT_TYPES.forEach { option ->
                            DropdownMenuItem(text = { Text(option) }, onClick = { type = option; typeExpanded = false })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { quantity?.let { onConfirm(it, type, reason) } },
                enabled = !isSubmitting && quantity != null && quantity in 1..maxQuantity && reason.isNotBlank()
            ) { Text("Confirm") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun PaymentsList(payments: List<Payment>, modifier: Modifier, canVoid: Boolean, onVoid: (Long) -> Unit) {
    if (payments.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No payments recorded yet")
        }
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(payments, key = { it.id }) { payment -> PaymentCard(payment, canVoid, onVoid) }
    }
}

@Composable
private fun PaymentCard(payment: Payment, canVoid: Boolean, onVoid: (Long) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(payment.receiptNumber, style = MaterialTheme.typography.titleSmall)
                Text(payment.status, style = MaterialTheme.typography.labelMedium)
            }
            Text("${payment.amount} — ${payment.method.replace('_', ' ')}", style = MaterialTheme.typography.bodyMedium)
            payment.reference?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            payment.voidReason?.let { Text("Void reason: $it", style = MaterialTheme.typography.bodySmall) }
            Text(payment.createdAt, style = MaterialTheme.typography.bodySmall)
            if (payment.status == "ACTIVE" && canVoid) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { onVoid(payment.id) }) { Text("Void") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordPaymentDialog(
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, method: String, reference: String?) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var method by remember { mutableStateOf(PAYMENT_METHODS.first()) }
    var reference by remember { mutableStateOf("") }
    var methodExpanded by remember { mutableStateOf(false) }
    val amount = amountText.toDoubleOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Payment") },
        text = {
            Column {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Box {
                    OutlinedTextField(
                        value = method,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Method") },
                        trailingIcon = {
                            IconButton(onClick = { methodExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Choose method")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(expanded = methodExpanded, onDismissRequest = { methodExpanded = false }) {
                        PAYMENT_METHODS.forEach { option ->
                            DropdownMenuItem(text = { Text(option) }, onClick = { method = option; methodExpanded = false })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = reference,
                    onValueChange = { reference = it },
                    label = { Text("Reference (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { amount?.let { onConfirm(it, method, reference.ifBlank { null }) } },
                enabled = !isSubmitting && amount != null && amount > 0
            ) { Text("Record") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun VoidPaymentDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Void payment") },
        text = {
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("Reason") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(reason) }, enabled = reason.isNotBlank()) { Text("Confirm") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
