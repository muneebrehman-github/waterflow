package com.spgg.app.ui.customer.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.spgg.app.domain.Order
import com.spgg.app.ui.components.AmountRow
import com.spgg.app.ui.components.EmptyState
import com.spgg.app.ui.components.IconBadge
import com.spgg.app.ui.components.OrderStatusTracker
import com.spgg.app.ui.components.SectionHeader
import com.spgg.app.ui.components.SpggScaffold
import com.spgg.app.ui.components.SpggSurfaceCard
import com.spgg.app.ui.components.StatusChip
import com.spgg.app.ui.components.formatRs
import com.spgg.app.ui.components.formatTimestamp
import com.spgg.app.ui.theme.DangerRed
import com.spgg.app.ui.theme.Lagoon
import com.spgg.app.ui.theme.LagoonMist

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

    SpggScaffold(
        title = uiState.order?.let { "Order #${it.id}" } ?: "Order",
        onBack = onBack,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        val order = uiState.order
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            order == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(title = "Couldn't load order", message = "Pull back and try again in a moment.")
            }
            else -> OrderDetailContent(
                order = order,
                isMutating = uiState.isMutating,
                onCancel = { confirmCancel = true },
                modifier = Modifier.padding(padding)
            )
        }

        if (confirmCancel) {
            AlertDialog(
                onDismissRequest = { confirmCancel = false },
                title = { Text("Cancel this order?") },
                text = { Text("The shop will be notified and nothing will be delivered.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.cancel()
                        confirmCancel = false
                    }) { Text("Yes, cancel", color = DangerRed) }
                },
                dismissButton = {
                    TextButton(onClick = { confirmCancel = false }) { Text("Keep order") }
                }
            )
        }
    }
}

@Composable
private fun OrderDetailContent(order: Order, isMutating: Boolean, onCancel: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SpggSurfaceCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Status", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatTimestamp(order.createdAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusChip(order.status)
            }
            Spacer(Modifier.height(16.dp))
            OrderStatusTracker(order.status)
            order.statusReason?.let { reason ->
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(reason, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        if (!order.addressText.isNullOrBlank()) {
            SpggSurfaceCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(Icons.Default.LocationOn, tint = MaterialTheme.colorScheme.tertiary, background = MaterialTheme.colorScheme.tertiaryContainer, size = 40)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(order.addressLabel ?: "Delivery address", style = MaterialTheme.typography.titleSmall)
                        Text(
                            listOfNotNull(order.addressText, order.addressArea).joinToString(", "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                order.deliveryNotes?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(10.dp))
                    Text("Note: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        SpggSurfaceCard {
            SectionHeader("Items")
            Spacer(Modifier.height(6.dp))
            order.items.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                    IconBadge(Icons.Default.WaterDrop, tint = Lagoon, background = LagoonMist, size = 36)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.productName, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "${item.quantity} × ${formatRs(item.unitPrice)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(formatRs(item.lineTotal), style = MaterialTheme.typography.titleSmall)
                }
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(8.dp))
            AmountRow("Subtotal", order.subtotal)
            if (order.deliveryCharge != 0.0) AmountRow("Delivery charge", order.deliveryCharge)
            if (order.discountAmount != 0.0) AmountRow("Discount", -order.discountAmount)
            if (order.totalDeposit != 0.0) AmountRow("Bottle deposit", order.totalDeposit)
            Spacer(Modifier.height(4.dp))
            AmountRow("Total payable", order.totalPayable, emphasize = true)
        }

        if (order.status == "PENDING") {
            OutlinedButton(
                onClick = onCancel,
                enabled = !isMutating,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Cancel order")
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}
