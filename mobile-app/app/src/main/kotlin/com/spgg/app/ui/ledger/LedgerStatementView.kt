package com.spgg.app.ui.ledger

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spgg.app.domain.LedgerEntry

@Composable
fun LedgerStatementView(balance: Double, entries: List<LedgerEntry>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Current Balance", style = MaterialTheme.typography.labelMedium)
                    Text(
                        balance.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (balance > 0) "Amount owed to shop" else "No outstanding balance",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        if (entries.isEmpty()) {
            item { Text("No ledger entries yet") }
        } else {
            items(entries, key = { it.id }) { entry -> LedgerEntryRow(entry) }
        }
    }
}

@Composable
private fun LedgerEntryRow(entry: LedgerEntry) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(entry.type.replace('_', ' '), style = MaterialTheme.typography.titleSmall)
                Text(
                    (if (entry.amount >= 0) "+" else "") + entry.amount.toString(),
                    style = MaterialTheme.typography.titleSmall
                )
            }
            entry.description?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            Spacer(Modifier.height(4.dp))
            HorizontalDivider()
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Balance: ${entry.runningBalanceSnapshot}", style = MaterialTheme.typography.bodySmall)
                Text(entry.createdAt, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
