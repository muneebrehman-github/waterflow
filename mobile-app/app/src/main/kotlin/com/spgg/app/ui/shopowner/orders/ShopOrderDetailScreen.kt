package com.spgg.app.ui.shopowner.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

private sealed interface ReasonAction {
    data object None : ReasonAction
    data object Reject : ReasonAction
    data object Cancel : ReasonAction
}

@Composable
fun ShopOrderDetailScreen(onBack: () -> Unit, viewModel: ShopOrderDetailViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var reasonAction by remember { mutableStateOf<ReasonAction>(ReasonAction.None) }
    var showAssignDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    com.spgg.app.ui.components.SpggScaffold(
        title = "Order detail",
        onBack = onBack,
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(it) } }
    ) { padding ->
        val order = uiState.order
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            order == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Couldn't load order")
            }
            else -> Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Order #${order.id}", style = MaterialTheme.typography.headlineSmall)
                    com.spgg.app.ui.components.StatusChip(order.status)
                }
                order.statusReason?.let {
                    Spacer(Modifier.height(8.dp))
                    Text("Reason: $it", style = MaterialTheme.typography.bodyMedium)
                }
                order.deliveryNotes?.let {
                    Spacer(Modifier.height(4.dp))
                    Text("Notes: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(Modifier.height(16.dp))
                com.spgg.app.ui.components.SpggSurfaceCard {
                    order.items.forEach { item ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                        ) {
                            Text("${item.productName} ×${item.quantity}", style = MaterialTheme.typography.bodyMedium)
                            Text(item.lineTotal.toString(), style = MaterialTheme.typography.titleSmall)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Total payable", style = MaterialTheme.typography.titleMedium)
                        Text(order.totalPayable.toString(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(Modifier.height(24.dp))
                when (order.status) {
                    "PENDING" -> {
                        Button(
                            onClick = viewModel::accept,
                            enabled = !uiState.isMutating,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) { Text("Accept order") }
                        Spacer(Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { reasonAction = ReasonAction.Reject },
                            enabled = !uiState.isMutating,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) { Text("Reject") }
                    }
                    "ACCEPTED" -> {
                        Button(
                            onClick = viewModel::prepare,
                            enabled = !uiState.isMutating,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) { Text("Mark preparing") }
                        Spacer(Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { reasonAction = ReasonAction.Cancel },
                            enabled = !uiState.isMutating,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) { Text("Cancel order") }
                    }
                    "PREPARING" -> {
                        Button(
                            onClick = { showAssignDialog = true },
                            enabled = !uiState.isMutating && uiState.deliveryBoys.isNotEmpty(),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) { Text("Assign delivery") }
                        if (uiState.deliveryBoys.isEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "No delivery boys on your team yet — add one from Team",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { reasonAction = ReasonAction.Cancel },
                            enabled = !uiState.isMutating,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) { Text("Cancel order") }
                    }
                    "OUT_FOR_DELIVERY" -> {
                        Text(
                            "Out for delivery — awaiting delivery boy confirmation",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    "DELIVERED" -> {
                        order.emptyBottlesCollected?.let {
                            Text(
                                "Empty bottles collected: $it / ${order.totalBottleQuantity}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        if (reasonAction != ReasonAction.None) {
            OrderReasonDialog(
                title = if (reasonAction == ReasonAction.Reject) "Reject order" else "Cancel order",
                onDismiss = { reasonAction = ReasonAction.None },
                onConfirm = { reason ->
                    if (reasonAction == ReasonAction.Reject) viewModel.reject(reason) else viewModel.cancel(reason)
                    reasonAction = ReasonAction.None
                }
            )
        }

        if (showAssignDialog) {
            AssignDeliveryDialog(
                deliveryBoys = uiState.deliveryBoys,
                onDismiss = { showAssignDialog = false },
                onConfirm = { deliveryBoyId ->
                    viewModel.assignDelivery(deliveryBoyId)
                    showAssignDialog = false
                }
            )
        }
    }
}

@Composable
private fun AssignDeliveryDialog(
    deliveryBoys: List<com.spgg.app.domain.StaffSummary>,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(deliveryBoys.firstOrNull()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign Delivery") },
        text = {
            Box {
                OutlinedTextField(
                    value = selected?.fullName.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Delivery Boy") },
                    trailingIcon = {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Choose delivery boy")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    deliveryBoys.forEach { option ->
                        DropdownMenuItem(text = { Text(option.fullName) }, onClick = { selected = option; expanded = false })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { selected?.let { onConfirm(it.id) } }, enabled = selected != null) { Text("Assign") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun OrderReasonDialog(title: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
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
