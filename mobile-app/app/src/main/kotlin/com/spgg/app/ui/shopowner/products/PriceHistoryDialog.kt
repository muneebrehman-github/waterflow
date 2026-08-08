package com.spgg.app.ui.shopowner.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spgg.app.domain.Product
import com.spgg.app.domain.ProductPriceHistoryEntry

@Composable
fun PriceHistoryDialog(
    product: Product,
    onDismiss: () -> Unit,
    loadHistory: suspend () -> Result<List<ProductPriceHistoryEntry>>
) {
    var entries by remember { mutableStateOf<List<ProductPriceHistoryEntry>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(product.id) {
        loadHistory()
            .onSuccess { entries = it }
            .onFailure { error = it.message }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Price history — ${product.name}") },
        text = {
            when {
                error != null -> Text(error!!, color = MaterialTheme.colorScheme.error)
                entries == null -> CircularProgressIndicator()
                entries!!.isEmpty() -> Text("No price history yet")
                else -> Column(Modifier.verticalScroll(rememberScrollState())) {
                    entries!!.forEachIndexed { index, entry ->
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(entry.price.toString())
                            Text(entry.changedAt, style = MaterialTheme.typography.bodySmall)
                        }
                        if (index != entries!!.lastIndex) HorizontalDivider(Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
