package com.spgg.app.ui.shopowner.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spgg.app.ui.auth.AuthUiState
import com.spgg.app.ui.auth.AuthViewModel
import com.spgg.app.ui.components.DestinationTile
import com.spgg.app.ui.components.HomeHeroHeader
import com.spgg.app.ui.components.MistBackground
import com.spgg.app.ui.theme.Lagoon
import com.spgg.app.ui.theme.LagoonMist
import com.spgg.app.ui.theme.SuccessGreen
import com.spgg.app.ui.theme.SuccessSoft
import com.spgg.app.ui.theme.TideTeal
import com.spgg.app.ui.theme.TideTealSoft
import com.spgg.app.ui.theme.WarningAmber
import com.spgg.app.ui.theme.WarningSoft
import kotlinx.coroutines.delay

@Composable
fun StaffHomeScreen(
    onOpenProducts: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenCustomers: () -> Unit,
    onOpenBottleTracking: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val fullName = (uiState as? AuthUiState.Authenticated)?.session?.fullName.orEmpty()
    var visible by remember { mutableStateOf(false) }
    val enterTransition = remember { fadeIn() + slideInVertically { height -> height / 6 } }

    LaunchedEffect(Unit) {
        delay(40)
        visible = true
    }

    Scaffold(containerColor = Color.Transparent) { padding ->
        MistBackground {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 28.dp)
            ) {
                item {
                    Spacer(Modifier.height(12.dp))
                    AnimatedVisibility(visible = visible, enter = enterTransition) {
                        HomeHeroHeader(
                            greeting = "Ready for the shift,",
                            name = fullName,
                            roleLabel = "Shop staff",
                            onLogout = { authViewModel.logout() },
                            logoutIcon = Icons.AutoMirrored.Filled.Logout
                        )
                    }
                }
                item { Spacer(Modifier.height(22.dp)) }
                item {
                    AnimatedVisibility(visible = visible, enter = enterTransition) {
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                DestinationTile(
                                    title = "Orders",
                                    subtitle = "Accept, prepare, manage",
                                    icon = Icons.Default.ReceiptLong,
                                    accent = WarningAmber,
                                    soft = WarningSoft,
                                    onClick = onOpenOrders,
                                    modifier = Modifier.weight(1f)
                                )
                                DestinationTile(
                                    title = "Customers",
                                    subtitle = "Ledgers & balances",
                                    icon = Icons.Default.People,
                                    accent = Lagoon,
                                    soft = LagoonMist,
                                    onClick = onOpenCustomers,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                DestinationTile(
                                    title = "Products",
                                    subtitle = "View catalog & prices",
                                    icon = Icons.Default.Inventory2,
                                    accent = TideTeal,
                                    soft = TideTealSoft,
                                    onClick = onOpenProducts,
                                    modifier = Modifier.weight(1f)
                                )
                                DestinationTile(
                                    title = "Bottles",
                                    subtitle = "Outstanding summary",
                                    icon = Icons.Default.LocalDrink,
                                    accent = SuccessGreen,
                                    soft = SuccessSoft,
                                    onClick = onOpenBottleTracking,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
