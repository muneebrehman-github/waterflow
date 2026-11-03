package com.spgg.app.ui.shopowner.bottles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spgg.app.ui.components.EmptyState
import com.spgg.app.ui.components.SpggScaffold
import com.spgg.app.ui.components.SpggSurfaceCard
import com.spgg.app.ui.components.StatHeroCard

@Composable
fun ShopBottleSummaryScreen(onBack: () -> Unit, viewModel: ShopBottleSummaryViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SpggScaffold(title = "Bottle tracking", onBack = onBack) { padding ->
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            uiState.summary == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(
                    title = "Couldn't load summary",
                    message = uiState.error ?: "Try again in a moment."
                )
            }
            else -> {
                val summary = uiState.summary!!
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    StatHeroCard(
                        label = "Outstanding bottles",
                        value = summary.totalOutstanding.toString(),
                        supporting = "Estimated value  ${summary.estimatedOutstandingValue}"
                    )
                    Spacer(Modifier.height(16.dp))
                    SpggSurfaceCard {
                        SummaryRow("Total delivered", summary.totalDelivered)
                        SummaryRow("Total returned", summary.totalReturned)
                        SummaryRow("Marked lost", summary.totalLost)
                        SummaryRow("Marked damaged", summary.totalDamaged)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: Int) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}
