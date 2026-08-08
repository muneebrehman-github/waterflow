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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Addresses") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { dialog = AddressDialog.Add }) {
                Icon(Icons.Default.Add, contentDescription = "Add address")
            }
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
                    Text("No addresses yet — tap + to add one")
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(address.label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                if (address.isDefault) {
                    AssistChip(onClick = {}, label = { Text("Default") })
                }
            }
            Text(address.addressText, style = MaterialTheme.typography.bodyMedium)
            address.area?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!address.isDefault) {
                    OutlinedButton(onClick = onSetDefault) { Text("Set default") }
                }
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit") }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Remove") }
            }
        }
    }
}
