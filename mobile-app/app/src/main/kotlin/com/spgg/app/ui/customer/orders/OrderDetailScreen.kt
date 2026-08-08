package com.spgg.app.ui.customer.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.spgg.app.domain.Order

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(onBack: () -> Unit, viewModel: OrderDetailViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var confirmCancel by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Detail") },
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
                Text("Couldn't load order")
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
                order.statusReason?.let {
                    Text("Reason: $it", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))
                order.items.forEach { item ->
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("${item.productName} x${item.quantity}")
                        Text(item.lineTotal.toString())
                    }
                }
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))
                TotalRow("Subtotal", order.subtotal)
                TotalRow("Delivery charge", order.deliveryCharge)
                TotalRow("Discount", -order.discountAmount)
                TotalRow("Order amount", order.orderAmount)
                TotalRow("Bottle deposit", order.totalDeposit)
                TotalRow("Total payable", order.totalPayable, emphasize = true)

                if (order.status == "PENDING") {
                    Spacer(Modifier.height(24.dp))
                    OutlinedButton(
                        onClick = { confirmCancel = true },
                        enabled = !uiState.isMutating,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel Order")
                    }
                }
            }
        }

        if (confirmCancel) {
            AlertDialog(
                onDismissRequest = { confirmCancel = false },
                title = { Text("Cancel this order?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.cancel()
                        confirmCancel = false
                    }) { Text("Yes, cancel") }
                },
                dismissButton = {
                    TextButton(onClick = { confirmCancel = false }) { Text("No") }
                }
            )
        }
    }
}

@Composable
private fun TotalRow(label: String, value: Double, emphasize: Boolean = false) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Text(label, style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium)
        Text(value.toString(), style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium)
    }
}
