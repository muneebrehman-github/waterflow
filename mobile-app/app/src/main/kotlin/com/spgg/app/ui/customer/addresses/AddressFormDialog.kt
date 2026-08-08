package com.spgg.app.ui.customer.addresses

import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spgg.app.domain.CustomerAddress

@Composable
fun AddressFormDialog(
    existing: CustomerAddress?,
    onDismiss: () -> Unit,
    onSave: (label: String, addressText: String, area: String?, isDefault: Boolean) -> Unit
) {
    var label by remember { mutableStateOf(existing?.label ?: "") }
    var addressText by remember { mutableStateOf(existing?.addressText ?: "") }
    var area by remember { mutableStateOf(existing?.area ?: "") }
    var isDefault by remember { mutableStateOf(existing?.isDefault ?: false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add address" else "Edit address") },
        text = {
            Column {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label (Home, Work, ...)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = addressText,
                    onValueChange = { addressText = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("Area / zone (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isDefault, onCheckedChange = { isDefault = it })
                    Text("Set as default delivery address")
                }
                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (label.isBlank() || addressText.isBlank()) {
                    error = "Label and address are required"
                    return@TextButton
                }
                onSave(label.trim(), addressText.trim(), area.trim().ifBlank { null }, isDefault)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
