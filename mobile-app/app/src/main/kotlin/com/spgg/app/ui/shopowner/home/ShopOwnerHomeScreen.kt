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
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spgg.app.ui.auth.AuthUiState
import com.spgg.app.ui.auth.AuthViewModel
import com.spgg.app.ui.components.DestinationTile
import com.spgg.app.ui.components.HomeHeroHeader
import com.spgg.app.ui.components.MistBackground
import com.spgg.app.ui.theme.CoralPulse
import com.spgg.app.ui.theme.CoralSoft
import com.spgg.app.ui.theme.Lagoon
import com.spgg.app.ui.theme.LagoonMist
import com.spgg.app.ui.theme.SuccessGreen
import com.spgg.app.ui.theme.SuccessSoft
import com.spgg.app.ui.theme.TideTeal
import com.spgg.app.ui.theme.TideTealSoft
import com.spgg.app.ui.theme.WarningAmber
import com.spgg.app.ui.theme.WarningSoft
import kotlinx.coroutines.delay

private data class HomeDestination(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accent: Color,
    val soft: Color,
    val onClick: () -> Unit
)

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
    var visible by remember { mutableStateOf(false) }
    val enterTransition = remember { fadeIn() + slideInVertically { height -> height / 6 } }

    LaunchedEffect(Unit) {
        delay(40)
        visible = true
    }

    val destinations = listOf(
        HomeDestination("Orders", "Accept, prepare, assign deliveries", Icons.Default.ReceiptLong, WarningAmber, WarningSoft, onOpenOrders),
        HomeDestination("Customers", "Ledgers, balances, bottle returns", Icons.Default.People, Lagoon, LagoonMist, onOpenCustomers),
        HomeDestination("Products", "Bottles, prices, availability", Icons.Default.Inventory2, TideTeal, TideTealSoft, onOpenProducts),
        HomeDestination("Team", "Staff & delivery boy accounts", Icons.Default.Groups, CoralPulse, CoralSoft, onOpenTeam),
        HomeDestination("Bottles", "Outstanding bottles shop-wide", Icons.Default.LocalDrink, SuccessGreen, SuccessSoft, onOpenBottleTracking),
        HomeDestination("Shop profile", "Hours, radius, delivery charges", Icons.Default.Storefront, Lagoon, LagoonMist, onOpenShopProfile)
    )

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
                            greeting = "Good to see you,",
                            name = fullName,
                            roleLabel = "Shop owner",
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
                            destinations.chunked(2).forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    rowItems.forEach { dest ->
                                        DestinationTile(
                                            title = dest.title,
                                            subtitle = dest.subtitle,
                                            icon = dest.icon,
                                            accent = dest.accent,
                                            soft = dest.soft,
                                            onClick = dest.onClick,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (rowItems.size == 1) {
                                        Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
