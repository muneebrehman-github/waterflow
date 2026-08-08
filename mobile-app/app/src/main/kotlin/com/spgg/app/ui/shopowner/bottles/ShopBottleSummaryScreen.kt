package com.spgg.app.ui.shopowner.bottles

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopBottleSummaryScreen(onBack: () -> Unit, viewModel: ShopBottleSummaryViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bottle Tracking") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            uiState.summary == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(uiState.error ?: "Couldn't load summary")
            }
            else -> {
                val summary = uiState.summary!!
                Column(Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Outstanding Bottles", style = MaterialTheme.typography.labelMedium)
                            Text(
                                summary.totalOutstanding.toString(),
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Estimated value: ${summary.estimatedOutstandingValue}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    SummaryRow("Total delivered", summary.totalDelivered)
                    SummaryRow("Total returned", summary.totalReturned)
                    SummaryRow("Marked lost", summary.totalLost)
                    SummaryRow("Marked damaged", summary.totalDamaged)
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: Int) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}
