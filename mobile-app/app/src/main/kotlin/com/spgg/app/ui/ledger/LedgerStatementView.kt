package com.spgg.app.ui.ledger

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spgg.app.domain.LedgerEntry
import com.spgg.app.ui.components.EmptyState
import com.spgg.app.ui.components.IconBadge
import com.spgg.app.ui.components.SectionHeader
import com.spgg.app.ui.components.SpggSurfaceCard
import com.spgg.app.ui.components.formatRs
import com.spgg.app.ui.components.formatTimestamp
import com.spgg.app.ui.theme.CoralPulse
import com.spgg.app.ui.theme.CoralSoft
import com.spgg.app.ui.theme.Lagoon
import com.spgg.app.ui.theme.LagoonBright
import com.spgg.app.ui.theme.SuccessGreen
import com.spgg.app.ui.theme.SuccessSoft
import com.spgg.app.ui.theme.TideTeal
import com.spgg.app.ui.theme.TideTealSoft
import com.spgg.app.ui.theme.Violet
import com.spgg.app.ui.theme.VioletSoft

@Composable
fun LedgerStatementView(
    balance: Double,
    entries: List<LedgerEntry>,
    modifier: Modifier = Modifier,
    title: String? = null
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (title != null) {
            item { Text(title, style = MaterialTheme.typography.headlineMedium) }
        }
        item { BalanceCard(balance) }
        item { SectionHeader("Activity", modifier = Modifier.padding(top = 8.dp)) }
        if (entries.isEmpty()) {
            item {
                EmptyState(
                    title = "No activity yet",
                    message = "Charges and payments will show up here.",
                    icon = Icons.AutoMirrored.Filled.ReceiptLong
                )
            }
        } else {
            items(entries, key = { it.id }) { entry -> LedgerEntryRow(entry) }
        }
    }
}

@Composable
private fun BalanceCard(balance: Double) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(Brush.linearGradient(listOf(LagoonBright, Lagoon)))
            .padding(22.dp)
    ) {
        Column {
            Text("Current balance", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.85f))
            Spacer(Modifier.height(6.dp))
            Text(formatRs(balance), style = MaterialTheme.typography.displayMedium, color = Color.White)
            Spacer(Modifier.height(6.dp))
            Text(
                when {
                    balance > 0 -> "Amount owed to the shop"
                    balance < 0 -> "You have credit with the shop"
                    else -> "You're all settled up"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun LedgerEntryRow(entry: LedgerEntry) {
    val (icon, accent, soft) = when (entry.type.uppercase()) {
        "CREDIT" -> Triple(Icons.Default.ArrowDownward, SuccessGreen, SuccessSoft)
        "DEBIT" -> Triple(Icons.Default.ArrowUpward, CoralPulse, CoralSoft)
        "DEPOSIT_DEBIT", "DEPOSIT_CREDIT" -> Triple(Icons.Default.LocalDrink, TideTeal, TideTealSoft)
        else -> Triple(Icons.Default.Tune, Violet, VioletSoft)
    }
    SpggSurfaceCard {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            IconBadge(icon = icon, tint = accent, background = soft, size = 40)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    entry.description ?: entry.type.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2
                )
                Text(
                    formatTimestamp(entry.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                Text(
                    (if (entry.amount > 0) "+" else "") + formatRs(entry.amount),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (entry.amount < 0) SuccessGreen else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Bal ${formatRs(entry.runningBalanceSnapshot)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
