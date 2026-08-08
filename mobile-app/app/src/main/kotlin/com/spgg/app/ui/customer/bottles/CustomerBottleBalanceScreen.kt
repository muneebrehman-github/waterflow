package com.spgg.app.ui.customer.bottles

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
fun CustomerBottleBalanceScreen(onBack: () -> Unit, viewModel: CustomerBottleBalanceViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Bottles") },
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
            uiState.balance == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(uiState.error ?: "Couldn't load bottle balance")
            }
            else -> {
                val balance = uiState.balance!!
                Column(Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Bottles with you", style = MaterialTheme.typography.labelMedium)
                            Text(
                                balance.outstanding.toString(),
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Please return these to your delivery boy", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    BottleStatRow("Delivered to you", balance.delivered)
                    BottleStatRow("Returned so far", balance.returned)
                    if (balance.lost > 0) BottleStatRow("Marked lost", balance.lost)
                    if (balance.damaged > 0) BottleStatRow("Marked damaged", balance.damaged)
                }
            }
        }
    }
}

@Composable
private fun BottleStatRow(label: String, value: Int) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}
