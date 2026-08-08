package com.spgg.app.ui.shopowner.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spgg.app.ui.auth.AuthUiState
import com.spgg.app.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopOwnerHomeScreen(
    onOpenShopProfile: () -> Unit,
    onOpenProducts: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenCustomers: () -> Unit,
    onOpenTeam: () -> Unit,
    onOpenBottleTracking: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val fullName = (uiState as? AuthUiState.Authenticated)?.session?.fullName.orEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Welcome, $fullName") },
                actions = {
                    IconButton(onClick = { authViewModel.logout() }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Log out")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Card(onClick = onOpenShopProfile, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    ListItem(
                        headlineContent = { Text("Shop Profile & Settings") },
                        supportingContent = { Text("Business hours, delivery hours, delivery charges") },
                        leadingContent = { Icon(Icons.Default.Storefront, contentDescription = null) }
                    )
                }
            }
            item {
                Card(onClick = onOpenProducts, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    ListItem(
                        headlineContent = { Text("Products") },
                        supportingContent = { Text("Manage bottle categories, prices, and availability") },
                        leadingContent = { Icon(Icons.Default.Inventory2, contentDescription = null) }
                    )
                }
            }
            item {
                Card(onClick = onOpenOrders, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    ListItem(
                        headlineContent = { Text("Orders") },
                        supportingContent = { Text("Accept, reject, prepare, and cancel incoming orders") },
                        leadingContent = { Icon(Icons.Default.ReceiptLong, contentDescription = null) }
                    )
                }
            }
            item {
                Card(onClick = onOpenCustomers, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    ListItem(
                        headlineContent = { Text("Customers") },
                        supportingContent = { Text("View customer ledgers and balances") },
                        leadingContent = { Icon(Icons.Default.People, contentDescription = null) }
                    )
                }
            }
            item {
                Card(onClick = onOpenTeam, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    ListItem(
                        headlineContent = { Text("Team") },
                        supportingContent = { Text("Manage staff and delivery boy accounts") },
                        leadingContent = { Icon(Icons.Default.Groups, contentDescription = null) }
                    )
                }
            }
            item {
                Card(onClick = onOpenBottleTracking, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    ListItem(
                        headlineContent = { Text("Bottle Tracking") },
                        supportingContent = { Text("Outstanding bottles across all customers") },
                        leadingContent = { Icon(Icons.Default.LocalDrink, contentDescription = null) }
                    )
                }
            }
        }
    }
}
