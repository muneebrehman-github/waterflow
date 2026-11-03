package com.spgg.app.ui.customer.addresses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import com.spgg.app.domain.CustomerAddress
import com.spgg.app.ui.components.EmptyState
import com.spgg.app.ui.components.IconBadge
import com.spgg.app.ui.components.SpggScaffold
import com.spgg.app.ui.components.SpggSurfaceCard
import com.spgg.app.ui.components.StatusChip
import com.spgg.app.ui.theme.CoralPulse
import com.spgg.app.ui.theme.CoralSoft
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.ExtendedFloatingActionButton

private sealed interface AddressDialog {
    data object None : AddressDialog
    data object Add : AddressDialog
    data class Edit(val address: CustomerAddress) : AddressDialog
    data class ConfirmDelete(val address: CustomerAddress) : AddressDialog
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressListScreen(onBack: () -> Unit, viewModel: AddressListViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var dialog by remember { mutableStateOf<AddressDialog>(AddressDialog.None) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    SpggScaffold(
        title = "My Addresses",
        onBack = onBack,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { dialog = AddressDialog.Add },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add address") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(it) } }
    ) { padding ->
        when {
            uiState.isLoading && uiState.addresses.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.addresses.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    EmptyState(
                        title = "No addresses yet",
                        message = "Add where you'd like your water delivered.",
                        icon = Icons.Default.LocationOn
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.addresses, key = { it.id }) { address ->
                        AddressRow(
                            address = address,
                            onEdit = { dialog = AddressDialog.Edit(address) },
                            onSetDefault = { viewModel.setDefault(address.id) },
                            onDelete = { dialog = AddressDialog.ConfirmDelete(address) }
                        )
                    }
                }
            }
        }

        when (val d = dialog) {
            AddressDialog.None -> {}
            AddressDialog.Add -> AddressFormDialog(
                existing = null,
                onDismiss = { dialog = AddressDialog.None },
                onSave = { label, addressText, area, isDefault ->
                    viewModel.createAddress(label, addressText, area, isDefault)
                    dialog = AddressDialog.None
                }
            )
            is AddressDialog.Edit -> AddressFormDialog(
                existing = d.address,
                onDismiss = { dialog = AddressDialog.None },
                onSave = { label, addressText, area, isDefault ->
                    viewModel.updateAddress(d.address.id, label, addressText, area, isDefault)
                    dialog = AddressDialog.None
                }
            )
            is AddressDialog.ConfirmDelete -> AlertDialog(
                onDismissRequest = { dialog = AddressDialog.None },
                title = { Text("Remove ${d.address.label}?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.delete(d.address.id)
                        dialog = AddressDialog.None
                    }) { Text("Remove") }
                },
                dismissButton = {
                    TextButton(onClick = { dialog = AddressDialog.None }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
private fun AddressRow(
    address: CustomerAddress,
    onEdit: () -> Unit,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit
) {
    SpggSurfaceCard {
        Row(verticalAlignment = Alignment.Top) {
            IconBadge(
                icon = if (address.label.contains("work", ignoreCase = true) || address.label.contains("office", ignoreCase = true)) {
                    Icons.Default.Work
                } else {
                    Icons.Default.Home
                },
                tint = CoralPulse,
                background = CoralSoft,
                size = 42
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(address.label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f, fill = false))
                    if (address.isDefault) {
                        Spacer(Modifier.width(8.dp))
                        StatusChip("DEFAULT")
                    }
                }
                Text(address.addressText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                address.area?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!address.isDefault) {
                TextButton(onClick = onSetDefault) { Text("Set as default") }
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error) }
        }
    }
}
