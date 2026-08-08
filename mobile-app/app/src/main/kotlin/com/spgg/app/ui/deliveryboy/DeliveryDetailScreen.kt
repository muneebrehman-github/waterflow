package com.spgg.app.ui.deliveryboy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryDetailScreen(onBack: () -> Unit, viewModel: DeliveryDetailViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCompleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Delivery Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(it) } }
    ) { padding ->
        val order = uiState.order
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            order == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Couldn't load delivery")
            }
            else -> Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text("Order #${order.id}", style = MaterialTheme.typography.headlineSmall)
                Text(order.status, style = MaterialTheme.typography.titleMedium)

                Spacer(Modifier.height(16.dp))
                order.customerName?.let { Text(it, style = MaterialTheme.typography.titleSmall) }
                order.customerPhone?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                order.addressLabel?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                order.addressText?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                order.addressArea?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                order.deliveryNotes?.let { Text("Notes: $it", style = MaterialTheme.typography.bodySmall) }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))
                order.items.forEach { item ->
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("${item.productName} x${item.quantity}")
                        Text(item.lineTotal.toString())
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("Bottles to collect: ${order.totalBottleQuantity}", style = MaterialTheme.typography.bodyMedium)
                Text("Total payable: ${order.totalPayable}", style = MaterialTheme.typography.titleMedium)

                Spacer(Modifier.height(24.dp))
                if (order.status == "OUT_FOR_DELIVERY") {
                    Button(
                        onClick = { showCompleteDialog = true },
                        enabled = !uiState.isMutating,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Complete Delivery") }
                } else if (order.status == "DELIVERED") {
                    Text(
                        "Delivered — ${order.emptyBottlesCollected ?: 0}/${order.totalBottleQuantity} bottles collected",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    if (showCompleteDialog) {
        CompleteDeliveryDialog(
            maxBottles = uiState.order?.totalBottleQuantity ?: 0,
            onDismiss = { showCompleteDialog = false },
            onConfirm = { count ->
                viewModel.completeDelivery(count)
                showCompleteDialog = false
            }
        )
    }
}

@Composable
private fun CompleteDeliveryDialog(maxBottles: Int, onDismiss: () -> Unit, onConfirm: (Int?) -> Unit) {
    var countText by remember { mutableStateOf(maxBottles.toString()) }
    val count = countText.toIntOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Complete Delivery") },
        text = {
            Column {
                Text("How many empty bottles did the customer return? (max $maxBottles)")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = countText,
                    onValueChange = { countText = it },
                    label = { Text("Empty bottles collected") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(count) }, enabled = count != null && count >= 0) { Text("Confirm") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
