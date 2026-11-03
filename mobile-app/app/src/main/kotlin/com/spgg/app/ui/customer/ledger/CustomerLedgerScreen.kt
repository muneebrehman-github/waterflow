package com.spgg.app.ui.customer.ledger

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spgg.app.ui.components.EmptyState
import com.spgg.app.ui.components.SpggScaffold
import com.spgg.app.ui.ledger.LedgerStatementView

@Composable
fun CustomerLedgerScreen(onBack: () -> Unit, viewModel: CustomerLedgerViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SpggScaffold(title = "My Wallet", onBack = onBack) { padding ->
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            uiState.error != null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(title = "Something went wrong", message = uiState.error ?: "Please try again shortly.")
            }
            else -> LedgerStatementView(
                balance = uiState.balance,
                entries = uiState.entries,
                modifier = Modifier.padding(padding)
            )
        }
    }
}
