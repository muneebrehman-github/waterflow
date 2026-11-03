package com.spgg.app.ui.customer.bottles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spgg.app.ui.components.EmptyState
import com.spgg.app.ui.components.SpggScaffold
import com.spgg.app.ui.components.StatTile
import com.spgg.app.ui.theme.CoralPulse
import com.spgg.app.ui.theme.CoralSoft
import com.spgg.app.ui.theme.DangerRed
import com.spgg.app.ui.theme.DangerSoft
import com.spgg.app.ui.theme.Lagoon
import com.spgg.app.ui.theme.LagoonMist
import com.spgg.app.ui.theme.SuccessGreen
import com.spgg.app.ui.theme.SuccessSoft
import com.spgg.app.ui.theme.TideTeal
import com.spgg.app.ui.theme.LagoonBright

@Composable
fun CustomerBottleBalanceScreen(onBack: () -> Unit, viewModel: CustomerBottleBalanceViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SpggScaffold(title = "My Bottles", onBack = onBack) { padding ->
        val balance = uiState.balance
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            balance == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(title = "Couldn't load bottles", message = uiState.error ?: "Please try again shortly.")
            }
            else -> Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(Brush.linearGradient(listOf(LagoonBright, TideTeal)))
                        .padding(24.dp)
                ) {
                    Column {
                        Text("Bottles with you", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.85f))
                        Text(balance.outstanding.toString(), style = MaterialTheme.typography.displayLarge, color = Color.White)
                        Text(
                            if (balance.outstanding > 0) "Hand these empties to your rider on the next delivery."
                            else "Nothing to return — thank you!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile("Delivered", balance.delivered.toString(), Icons.Default.LocalShipping, Lagoon, LagoonMist, Modifier.weight(1f))
                    StatTile("Returned", balance.returned.toString(), Icons.Default.Recycling, SuccessGreen, SuccessSoft, Modifier.weight(1f))
                }
                if (balance.lost > 0 || balance.damaged > 0) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatTile("Marked lost", balance.lost.toString(), Icons.Default.SearchOff, CoralPulse, CoralSoft, Modifier.weight(1f))
                        StatTile("Damaged", balance.damaged.toString(), Icons.Default.ReportProblem, DangerRed, DangerSoft, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
