package com.spgg.app.ui.customer.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.spgg.app.domain.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewOrderScreen(
    onBack: () -> Unit,
    onOrderPlaced: (Long) -> Unit,
    viewModel: NewOrderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var addressMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    LaunchedEffect(uiState.placedOrder) {
        uiState.placedOrder?.let { onOrderPlaced(it.id) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Order") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(it) } },
        bottomBar = {
            if (!uiState.isLoading) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Subtotal: ${uiState.subtotal}  ·  Deposit: ${uiState.totalDeposit}")
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = viewModel::placeOrder,
                        enabled = !uiState.isPlacingOrder && uiState.itemCount > 0,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isPlacingOrder) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Place Order (${uiState.itemCount} item${if (uiState.itemCount == 1) "" else "s"})")
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Box(Modifier.fillMaxWidth()) {
                    val selectedAddress = uiState.addresses.find { it.id == uiState.selectedAddressId }
                    OutlinedTextField(
                        value = selectedAddress?.let { "${it.label} — ${it.addressText}" } ?: "Choose an address",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Delivery address") },
                        trailingIcon = {
                            IconButton(onClick = { addressMenuExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Choose address")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(expanded = addressMenuExpanded, onDismissRequest = { addressMenuExpanded = false }) {
                        uiState.addresses.forEach { address ->
                            DropdownMenuItem(
                                text = { Text("${address.label} — ${address.addressText}") },
                                onClick = {
                                    viewModel.selectAddress(address.id)
                                    addressMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::setNotes,
                    label = { Text("Delivery notes (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }
            items(uiState.products) { product ->
                ProductOrderRow(
                    product = product,
                    quantity = uiState.quantities[product.id] ?: 0,
                    onQuantityChange = { viewModel.setQuantity(product.id, it) }
                )
            }
        }
    }
}

@Composable
private fun ProductOrderRow(product: Product, quantity: Int, onQuantityChange: (Int) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Price: ${product.unitPrice}  ·  Deposit: ${product.depositAmount}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = { onQuantityChange((quantity - 1).coerceAtLeast(0)) }, enabled = quantity > 0) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease")
            }
            Text(quantity.toString(), modifier = Modifier.padding(horizontal = 8.dp))
            IconButton(onClick = { onQuantityChange(quantity + 1) }) {
                Icon(Icons.Default.Add, contentDescription = "Increase")
            }
        }
    }
}
