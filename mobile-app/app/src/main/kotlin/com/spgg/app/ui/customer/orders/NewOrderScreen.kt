package com.spgg.app.ui.customer.orders

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spgg.app.domain.CustomerAddress
import com.spgg.app.domain.Product
import com.spgg.app.ui.components.AmountRow
import com.spgg.app.ui.components.EmptyState
import com.spgg.app.ui.components.IconBadge
import com.spgg.app.ui.components.SectionHeader
import com.spgg.app.ui.components.SpggScaffold
import com.spgg.app.ui.components.SpggSurfaceCard
import com.spgg.app.ui.components.formatRs
import com.spgg.app.ui.theme.Lagoon
import com.spgg.app.ui.theme.LagoonMist

@Composable
fun NewOrderScreen(
    onBack: () -> Unit,
    onOrderPlaced: (Long) -> Unit,
    viewModel: NewOrderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    LaunchedEffect(uiState.placedOrder) {
        uiState.placedOrder?.let { onOrderPlaced(it.id) }
    }

    SpggScaffold(
        title = "Order water",
        onBack = onBack,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@SpggScaffold
        }

        Column(Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.shopName.isNotBlank()) {
                    item {
                        Text(
                            "From ${uiState.shopName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                item { SectionHeader("Deliver to") }
                item {
                    if (uiState.addresses.isEmpty()) {
                        SpggSurfaceCard {
                            Text(
                                "Add a delivery address from your Account tab before ordering.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(uiState.addresses, key = { it.id }) { address ->
                                AddressOption(
                                    address = address,
                                    selected = address.id == uiState.selectedAddressId,
                                    onClick = { viewModel.selectAddress(address.id) }
                                )
                            }
                        }
                    }
                }

                item { SectionHeader("Choose products", modifier = Modifier.padding(top = 4.dp)) }
                if (uiState.products.isEmpty()) {
                    item {
                        SpggSurfaceCard {
                            EmptyState(
                                title = "No products yet",
                                message = if (uiState.shopName.isBlank()) {
                                    "Your shop hasn't listed any products yet. Please check back soon."
                                } else {
                                    "${uiState.shopName} hasn't listed any products for your account yet. " +
                                        "Make sure you registered with the right shop (see Account), or ask the shop to add products."
                                },
                                icon = Icons.Outlined.Inventory2
                            )
                        }
                    }
                } else {
                    items(uiState.products, key = { it.id }) { product ->
                        ProductOrderRow(
                            product = product,
                            quantity = uiState.quantities[product.id] ?: 0,
                            onQuantityChange = { viewModel.setQuantity(product.id, it) }
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = uiState.notes,
                        onValueChange = viewModel::setNotes,
                        label = { Text("Note for the rider (optional)") },
                        placeholder = { Text("e.g. Ring the bell twice") },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    )
                }
            }

            CheckoutBar(
                subtotal = uiState.subtotal,
                deposit = uiState.totalDeposit,
                itemCount = uiState.itemCount,
                isPlacing = uiState.isPlacingOrder,
                onPlaceOrder = viewModel::placeOrder
            )
        }
    }
}

@Composable
private fun AddressOption(address: CustomerAddress, selected: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = if (selected) scheme.primaryContainer else scheme.surface,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) scheme.primary else scheme.outlineVariant),
        modifier = Modifier.width(220.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(address.label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                if (selected) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = scheme.primary, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                address.addressText,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ProductOrderRow(product: Product, quantity: Int, onQuantityChange: (Int) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = MaterialTheme.shapes.large,
        color = scheme.surface,
        border = BorderStroke(if (quantity > 0) 2.dp else 1.dp, if (quantity > 0) scheme.primary else scheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(icon = Icons.Default.WaterDrop, tint = Lagoon, background = LagoonMist, size = 52)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium)
                Text(formatRs(product.unitPrice), style = MaterialTheme.typography.titleSmall, color = scheme.primary)
                if (product.depositAmount > 0) {
                    Text(
                        "+ ${formatRs(product.depositAmount)} bottle deposit",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }
            AnimatedContent(targetState = quantity > 0, label = "stepper") { inCart ->
                if (inCart) {
                    QuantityStepper(quantity = quantity, onQuantityChange = onQuantityChange)
                } else {
                    FilledTonalButton(onClick = { onQuantityChange(1) }, contentPadding = PaddingValues(horizontal = 16.dp)) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add")
                    }
                }
            }
        }
    }
}

@Composable
private fun QuantityStepper(quantity: Int, onQuantityChange: (Int) -> Unit) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onQuantityChange((quantity - 1).coerceAtLeast(0)) }, modifier = Modifier.size(38.dp)) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = MaterialTheme.colorScheme.onPrimary)
            }
            Text(
                quantity.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            IconButton(onClick = { onQuantityChange(quantity + 1) }, modifier = Modifier.size(38.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Increase", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
private fun CheckoutBar(
    subtotal: Double,
    deposit: Double,
    itemCount: Int,
    isPlacing: Boolean,
    onPlaceOrder: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp,
        shape = MaterialTheme.shapes.extraLarge.copy(
            bottomStart = androidx.compose.foundation.shape.CornerSize(0.dp),
            bottomEnd = androidx.compose.foundation.shape.CornerSize(0.dp)
        )
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
            AmountRow("Water", subtotal)
            if (deposit > 0) AmountRow("Bottle deposit", deposit)
            AmountRow("Estimated total", subtotal + deposit, emphasize = true)
            Text(
                "Delivery charges or discounts, if any, are applied by the shop.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onPlaceOrder,
                enabled = !isPlacing && itemCount > 0,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                if (isPlacing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else if (itemCount == 0) {
                    Text("Add items to continue")
                } else {
                    Text("Place order · $itemCount item${if (itemCount == 1) "" else "s"}")
                }
            }
        }
    }
}
