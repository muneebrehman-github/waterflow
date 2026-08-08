package com.spgg.app.ui.customer.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
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
fun CustomerHomeScreen(
    onOpenProfile: () -> Unit,
    onOpenAddresses: () -> Unit,
    onOpenNewOrder: () -> Unit,
    onOpenOrderHistory: () -> Unit,
    onOpenLedger: () -> Unit,
    onOpenBottles: () -> Unit,
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
                Card(onClick = onOpenNewOrder, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    ListItem(
                        headlineContent = { Text("Place a New Order") },
                        leadingContent = { Icon(Icons.Default.AddShoppingCart, contentDescription = null) }
                    )
                }
            }
            item {
                Card(onClick = onOpenOrderHistory, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    ListItem(
                        headlineContent = { Text("My Orders") },
                        leadingContent = { Icon(Icons.Default.ReceiptLong, contentDescription = null) }
                    )
                }
            }
            item {
                Card(onClick = onOpenLedger, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    ListItem(
                        headlineContent = { Text("My Ledger") },
                        supportingContent = { Text("View your balance and payment history") },
                        leadingContent = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) }
                    )
                }
            }
            item {
                Card(onClick = onOpenBottles, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    ListItem(
                        headlineContent = { Text("My Bottles") },
                        supportingContent = { Text("Bottles with you that need to be returned") },
                        leadingContent = { Icon(Icons.Default.LocalDrink, contentDescription = null) }
                    )
                }
            }
            item {
                Card(onClick = onOpenProfile, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    ListItem(
                        headlineContent = { Text("My Profile") },
                        leadingContent = { Icon(Icons.Default.Person, contentDescription = null) }
                    )
                }
            }
            item {
                Card(onClick = onOpenAddresses, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    ListItem(
                        headlineContent = { Text("My Addresses") },
                        supportingContent = { Text("Manage your delivery addresses") },
                        leadingContent = { Icon(Icons.Default.LocationOn, contentDescription = null) }
                    )
                }
            }
        }
    }
}
