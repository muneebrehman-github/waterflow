package com.spgg.app.ui.shopowner.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.spgg.app.domain.Product

/** Create mode when [existingProduct] is null (asks for an initial price too);
 *  edit mode otherwise (price is changed separately, via its own dedicated flow —
 *  matches the backend keeping a price-change history). */
@Composable
fun ProductFormDialog(
    existingProduct: Product?,
    onDismiss: () -> Unit,
    onSave: (name: String, unitPrice: Double?, depositAmount: Double, displayOrder: Int?) -> Unit
) {
    var name by remember { mutableStateOf(existingProduct?.name ?: "") }
    var unitPrice by remember { mutableStateOf(existingProduct?.unitPrice?.toString() ?: "") }
    var depositAmount by remember { mutableStateOf(existingProduct?.depositAmount?.toString() ?: "0") }
    var displayOrder by remember { mutableStateOf(existingProduct?.displayOrder?.toString() ?: "0") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingProduct == null) "Add product" else "Edit product") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category name (e.g. 19L Bottle)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                if (existingProduct == null) {
                    OutlinedTextField(
                        value = unitPrice,
                        onValueChange = { unitPrice = it },
                        label = { Text("Unit price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                }
                OutlinedTextField(
                    value = depositAmount,
                    onValueChange = { depositAmount = it },
                    label = { Text("Refundable deposit per unit") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = displayOrder,
                    onValueChange = { displayOrder = it },
                    label = { Text("Display order") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val deposit = depositAmount.toDoubleOrNull()
                val order = displayOrder.toIntOrNull()
                val price = if (existingProduct == null) unitPrice.toDoubleOrNull() else null

                if (name.isBlank() || deposit == null || (existingProduct == null && price == null)) {
                    error = "Enter a name, a valid deposit amount, and a valid price"
                    return@TextButton
                }
                onSave(name.trim(), price, deposit, order)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
