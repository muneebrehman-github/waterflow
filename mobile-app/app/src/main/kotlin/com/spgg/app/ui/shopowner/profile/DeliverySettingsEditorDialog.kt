package com.spgg.app.ui.shopowner.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.spgg.app.domain.DeliveryChargeTier

private data class EditableTier(val minOrderValue: String, val maxOrderValue: String, val charge: String)

@Composable
fun DeliverySettingsEditorDialog(
    initialRadiusKm: Double?,
    initialMinOrderValue: Double?,
    initialTiers: List<DeliveryChargeTier>,
    onDismiss: () -> Unit,
    onSave: (radiusKm: Double, minOrderValue: Double, tiers: List<DeliveryChargeTier>) -> Unit
) {
    var radiusText by remember { mutableStateOf(initialRadiusKm?.toString() ?: "") }
    var minOrderText by remember { mutableStateOf(initialMinOrderValue?.toString() ?: "") }
    val tiers = remember {
        mutableStateListOf(*(if (initialTiers.isEmpty()) {
            listOf(EditableTier("0", "", "0"))
        } else {
            initialTiers.map { EditableTier(it.minOrderValue.toString(), it.maxOrderValue?.toString() ?: "", it.charge.toString()) }
        }).toTypedArray())
    }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delivery settings") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = radiusText,
                    onValueChange = { radiusText = it },
                    label = { Text("Delivery radius (km)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = minOrderText,
                    onValueChange = { minOrderText = it },
                    label = { Text("Minimum order value") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Text("Delivery charge tiers", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Leave the last tier's \"up to\" empty to mean \"and above\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                tiers.forEachIndexed { index, tier ->
                    Spacer(Modifier.height(8.dp))
                    TierRow(
                        tier = tier,
                        onChanged = { tiers[index] = it },
                        onRemove = { if (tiers.size > 1) tiers.removeAt(index) }
                    )
                    if (index != tiers.lastIndex) HorizontalDivider(Modifier.padding(vertical = 4.dp))
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { tiers.add(EditableTier("", "", "0")) }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Add tier")
                }
                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val radius = radiusText.toDoubleOrNull()
                val minOrder = minOrderText.toDoubleOrNull()
                val parsedTiers = tiers.mapNotNull { tier ->
                    val min = tier.minOrderValue.toDoubleOrNull() ?: return@mapNotNull null
                    val max = tier.maxOrderValue.toDoubleOrNull()
                    val charge = tier.charge.toDoubleOrNull() ?: return@mapNotNull null
                    DeliveryChargeTier(min, max, charge)
                }
                if (radius == null || minOrder == null) {
                    error = "Enter valid numbers for radius and minimum order value"
                    return@TextButton
                }
                if (parsedTiers.size != tiers.size || parsedTiers.isEmpty()) {
                    error = "Every tier needs a valid minimum order value and charge"
                    return@TextButton
                }
                onSave(radius, minOrder, parsedTiers)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun TierRow(tier: EditableTier, onChanged: (EditableTier) -> Unit, onRemove: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = tier.minOrderValue,
            onValueChange = { onChanged(tier.copy(minOrderValue = it)) },
            label = { Text("From") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = tier.maxOrderValue,
            onValueChange = { onChanged(tier.copy(maxOrderValue = it)) },
            label = { Text("Up to") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = tier.charge,
            onValueChange = { onChanged(tier.copy(charge = it)) },
            label = { Text("Charge") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, contentDescription = "Remove tier")
        }
    }
}
