package com.spgg.app.ui.shopowner.customers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spgg.app.domain.CustomerSummary
import com.spgg.app.ui.components.EmptyState
import com.spgg.app.ui.components.SpggScaffold
import com.spgg.app.ui.components.SpggSurfaceCard
import com.spgg.app.ui.components.StatusChip

@Composable
fun ShopCustomersScreen(
    onBack: () -> Unit,
    onOpenCustomerLedger: (Long) -> Unit,
    viewModel: ShopCustomersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SpggScaffold(title = "Customers", onBack = onBack) { padding ->
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            uiState.customers.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(
                    title = "No customers yet",
                    message = "Customers who register for your shop will appear here.",
                    icon = Icons.Default.People
                )
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.customers, key = { it.id }) { customer ->
                    CustomerSummaryCard(customer = customer, onClick = { onOpenCustomerLedger(customer.id) })
                }
            }
        }
    }
}

@Composable
private fun CustomerSummaryCard(customer: CustomerSummary, onClick: () -> Unit) {
    SpggSurfaceCard(onClick = onClick) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(customer.fullName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            StatusChip(status = customer.status)
        }
        Spacer(Modifier.height(8.dp))
        Text(customer.phone, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(customer.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
