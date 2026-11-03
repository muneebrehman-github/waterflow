package com.spgg.app.ui.shopowner.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import com.spgg.app.domain.Product
import com.spgg.app.ui.auth.LocalShopCapabilities
import com.spgg.app.ui.components.EmptyState
import com.spgg.app.ui.components.SpggScaffold
import com.spgg.app.ui.components.SpggSurfaceCard
import com.spgg.app.ui.components.StatusChip

private sealed interface ActiveDialog {
    data object None : ActiveDialog
    data object AddProduct : ActiveDialog
    data class EditProduct(val product: Product) : ActiveDialog
    data class ChangePrice(val product: Product) : ActiveDialog
    data class ViewHistory(val product: Product) : ActiveDialog
    data class ConfirmArchive(val product: Product) : ActiveDialog
}

@Composable
fun ProductListScreen(onBack: () -> Unit, viewModel: ProductListViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val canManage = LocalShopCapabilities.current.canManageProducts
    var activeDialog by remember { mutableStateOf<ActiveDialog>(ActiveDialog.None) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    SpggScaffold(
        title = "Products",
        onBack = onBack,
        floatingActionButton = {
            if (canManage) {
                FloatingActionButton(
                    onClick = { activeDialog = ActiveDialog.AddProduct },
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add product")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(it) } }
    ) { padding ->
        when {
            uiState.isLoading && uiState.products.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.products.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    EmptyState(
                        title = "No products yet",
                        message = if (canManage) "Tap + to add your first bottle category." else "Your shop hasn't published products yet.",
                        icon = Icons.Default.Inventory2
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.products, key = { it.id }) { product ->
                        ProductRow(
                            product = product,
                            canManage = canManage,
                            onEdit = { activeDialog = ActiveDialog.EditProduct(product) },
                            onChangePrice = { activeDialog = ActiveDialog.ChangePrice(product) },
                            onViewHistory = { activeDialog = ActiveDialog.ViewHistory(product) },
                            onToggleAvailability = { viewModel.toggleAvailability(product.id) },
                            onArchive = { activeDialog = ActiveDialog.ConfirmArchive(product) }
                        )
                    }
                }
            }
        }

        when (val dialog = activeDialog) {
            ActiveDialog.None -> {}
            ActiveDialog.AddProduct -> ProductFormDialog(
                existingProduct = null,
                onDismiss = { activeDialog = ActiveDialog.None },
                onSave = { name, unitPrice, deposit, order ->
                    viewModel.createProduct(name, unitPrice ?: 0.0, deposit, order)
                    activeDialog = ActiveDialog.None
                }
            )
            is ActiveDialog.EditProduct -> ProductFormDialog(
                existingProduct = dialog.product,
                onDismiss = { activeDialog = ActiveDialog.None },
                onSave = { name, _, deposit, order ->
                    viewModel.updateProduct(dialog.product.id, name, deposit, order)
                    activeDialog = ActiveDialog.None
                }
            )
            is ActiveDialog.ChangePrice -> ChangePriceDialog(
                product = dialog.product,
                onDismiss = { activeDialog = ActiveDialog.None },
                onSave = { newPrice ->
                    viewModel.updatePrice(dialog.product.id, newPrice)
                    activeDialog = ActiveDialog.None
                }
            )
            is ActiveDialog.ViewHistory -> PriceHistoryDialog(
                product = dialog.product,
                onDismiss = { activeDialog = ActiveDialog.None },
                loadHistory = { viewModel.loadPriceHistory(dialog.product.id) }
            )
            is ActiveDialog.ConfirmArchive -> AlertDialog(
                onDismissRequest = { activeDialog = ActiveDialog.None },
                title = { Text("Archive ${dialog.product.name}?") },
                text = { Text("This hides it from your catalog. It can't be un-archived from the app yet.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.archive(dialog.product.id)
                        activeDialog = ActiveDialog.None
                    }) { Text("Archive") }
                },
                dismissButton = {
                    TextButton(onClick = { activeDialog = ActiveDialog.None }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
private fun ProductRow(
    product: Product,
    canManage: Boolean,
    onEdit: () -> Unit,
    onChangePrice: () -> Unit,
    onViewHistory: () -> Unit,
    onToggleAvailability: () -> Unit,
    onArchive: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    SpggSurfaceCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Price ${product.unitPrice}  ·  Deposit ${product.depositAmount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                StatusChip(if (product.isAvailable) "AVAILABLE" else "UNAVAILABLE")
            }
            if (canManage) {
                Switch(checked = product.isAvailable, onCheckedChange = { onToggleAvailability() })
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More actions")
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = { menuExpanded = false; onEdit() }
                        )
                        DropdownMenuItem(
                            text = { Text("Change price") },
                            onClick = { menuExpanded = false; onChangePrice() }
                        )
                        DropdownMenuItem(
                            text = { Text("Price history") },
                            leadingIcon = { Icon(Icons.Default.History, contentDescription = null) },
                            onClick = { menuExpanded = false; onViewHistory() }
                        )
                        DropdownMenuItem(
                            text = { Text("Archive") },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                            onClick = { menuExpanded = false; onArchive() }
                        )
                    }
                }
            }
        }
    }
}
