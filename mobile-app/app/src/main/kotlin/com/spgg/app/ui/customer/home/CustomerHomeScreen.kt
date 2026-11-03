@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.spgg.app.ui.customer.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spgg.app.domain.Order
import com.spgg.app.ui.auth.AuthUiState
import com.spgg.app.ui.auth.AuthViewModel
import com.spgg.app.ui.components.ActionRow
import com.spgg.app.ui.components.ActiveOrderStatuses
import com.spgg.app.ui.components.BrandMark
import com.spgg.app.ui.components.EmptyState
import com.spgg.app.ui.components.IconBadge
import com.spgg.app.ui.components.InitialsAvatar
import com.spgg.app.ui.components.MistBackground
import com.spgg.app.ui.components.OrderStatusTracker
import com.spgg.app.ui.components.SectionHeader
import com.spgg.app.ui.components.SpggSurfaceCard
import com.spgg.app.ui.components.StatTile
import com.spgg.app.ui.components.StatusChip
import com.spgg.app.ui.components.formatRs
import com.spgg.app.ui.customer.orders.OrderSummaryCard
import com.spgg.app.ui.ledger.LedgerStatementView
import com.spgg.app.ui.theme.CoralPulse
import com.spgg.app.ui.theme.CoralSoft
import com.spgg.app.ui.theme.DangerRed
import com.spgg.app.ui.theme.DangerSoft
import com.spgg.app.ui.theme.Lagoon
import com.spgg.app.ui.theme.LagoonBright
import com.spgg.app.ui.theme.LagoonMist
import com.spgg.app.ui.theme.SuccessGreen
import com.spgg.app.ui.theme.SuccessSoft
import com.spgg.app.ui.theme.TideTeal
import com.spgg.app.ui.theme.TideTealSoft
import com.spgg.app.ui.theme.Violet
import com.spgg.app.ui.theme.VioletSoft
import java.time.LocalTime

private enum class CustomerTab(val label: String, val selectedIcon: ImageVector, val icon: ImageVector) {
    Home("Home", Icons.Filled.Home, Icons.Outlined.Home),
    Orders("Orders", Icons.AutoMirrored.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
    Wallet("Wallet", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
    Account("Account", Icons.Filled.Person, Icons.Outlined.Person)
}

/** Customer home shell: bottom-navigation tabs over one shared view model. */
@Composable
fun CustomerHomeScreen(
    onOpenNewOrder: () -> Unit,
    onOpenOrder: (Long) -> Unit,
    onOpenAddresses: () -> Unit,
    onOpenBottles: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    viewModel: CustomerHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val sessionName = (authState as? AuthUiState.Authenticated)?.session?.fullName.orEmpty()
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tab = CustomerTab.entries[tabIndex]
    val snackbarHostState = remember { SnackbarHostState() }

    // Reload whenever we come back (e.g. after placing or cancelling an order).
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    BackHandler(enabled = tab != CustomerTab.Home) { tabIndex = CustomerTab.Home.ordinal }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (tab == CustomerTab.Orders) {
                ExtendedFloatingActionButton(
                    onClick = onOpenNewOrder,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("New order") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                CustomerTab.entries.forEach { item ->
                    val selected = item == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { tabIndex = item.ordinal },
                        icon = { Icon(if (selected) item.selectedIcon else item.icon, contentDescription = null) },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { padding ->
        MistBackground(Modifier.padding(padding)) {
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refresh(userInitiated = true) },
                modifier = Modifier.fillMaxSize()
            ) {
                if (uiState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                } else {
                    AnimatedContent(
                        targetState = tab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "customerTab"
                    ) { current ->
                        when (current) {
                            CustomerTab.Home -> HomeTab(
                                state = uiState,
                                fallbackName = sessionName,
                                onOpenNewOrder = onOpenNewOrder,
                                onOpenOrder = onOpenOrder,
                                onOpenAddresses = onOpenAddresses,
                                onOpenBottles = onOpenBottles,
                                onShowOrders = { tabIndex = CustomerTab.Orders.ordinal },
                                onShowWallet = { tabIndex = CustomerTab.Wallet.ordinal },
                                onShowAccount = { tabIndex = CustomerTab.Account.ordinal }
                            )
                            CustomerTab.Orders -> OrdersTab(orders = uiState.orders, onOpenOrder = onOpenOrder)
                            CustomerTab.Wallet -> LedgerStatementView(
                                balance = uiState.ledgerBalance,
                                entries = uiState.ledgerEntries,
                                title = "Wallet"
                            )
                            CustomerTab.Account -> AccountTab(
                                state = uiState,
                                fallbackName = sessionName,
                                onOpenAddresses = onOpenAddresses,
                                onOpenBottles = onOpenBottles,
                                onShowWallet = { tabIndex = CustomerTab.Wallet.ordinal },
                                onLogout = { authViewModel.logout() }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- Home tab

@Composable
private fun HomeTab(
    state: CustomerHomeUiState,
    fallbackName: String,
    onOpenNewOrder: () -> Unit,
    onOpenOrder: (Long) -> Unit,
    onOpenAddresses: () -> Unit,
    onOpenBottles: () -> Unit,
    onShowOrders: () -> Unit,
    onShowWallet: () -> Unit,
    onShowAccount: () -> Unit
) {
    val name = state.profile?.fullName ?: fallbackName
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item { GreetingHeader(name = name, onAvatarClick = onShowAccount) }
        item {
            DeliverToPill(
                label = state.defaultAddress?.let { "${it.label} · ${it.addressText}" } ?: "Add a delivery address",
                onClick = onOpenAddresses
            )
        }
        item { OrderHeroCard(shopName = state.profile?.shopName.orEmpty(), onOrder = onOpenNewOrder) }

        state.activeOrder?.let { order ->
            item {
                Column {
                    SectionHeader("Current order", actionLabel = "Details", onAction = { onOpenOrder(order.id) })
                    ActiveOrderCard(order = order, onClick = { onOpenOrder(order.id) })
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val balance = state.ledgerBalance
                StatTile(
                    label = "Balance due",
                    value = formatRs(balance),
                    caption = if (balance > 0) "Pay on next delivery" else "All settled",
                    icon = Icons.Default.AccountBalanceWallet,
                    accent = if (balance > 0) CoralPulse else SuccessGreen,
                    soft = if (balance > 0) CoralSoft else SuccessSoft,
                    onClick = onShowWallet,
                    modifier = Modifier.weight(1f)
                )
                val bottles = state.bottles?.outstanding ?: 0
                StatTile(
                    label = "Bottles with you",
                    value = bottles.toString(),
                    caption = if (bottles > 0) "Return on next visit" else "Nothing to return",
                    icon = Icons.Default.LocalDrink,
                    accent = TideTeal,
                    soft = TideTealSoft,
                    onClick = onOpenBottles,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Column {
                SectionHeader("Quick actions")
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    QuickAction("Order", Icons.Default.WaterDrop, Lagoon, LagoonMist, onOpenNewOrder)
                    QuickAction("History", Icons.AutoMirrored.Filled.ReceiptLong, Violet, VioletSoft, onShowOrders)
                    QuickAction("Bottles", Icons.Default.LocalDrink, TideTeal, TideTealSoft, onOpenBottles)
                    QuickAction("Addresses", Icons.Default.LocationOn, CoralPulse, CoralSoft, onOpenAddresses)
                }
            }
        }

        item {
            SectionHeader(
                "Recent orders",
                actionLabel = if (state.orders.isNotEmpty()) "See all" else null,
                onAction = onShowOrders
            )
        }
        val recent = state.orders.filter { it.id != state.activeOrder?.id }.take(3)
        if (state.orders.isEmpty()) {
            item {
                SpggSurfaceCard {
                    EmptyState(
                        title = "No orders yet",
                        message = "Your first delivery is just a tap away.",
                        modifier = Modifier.padding(0.dp)
                    )
                }
            }
        } else {
            items(recent, key = { it.id }) { order ->
                OrderSummaryCard(order = order, onClick = { onOpenOrder(order.id) })
            }
        }
    }
}

private fun greetingFor(hour: Int): String = when (hour) {
    in 5..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    else -> "Good evening"
}

@Composable
private fun GreetingHeader(name: String, onAvatarClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                greetingFor(LocalTime.now().hour) + " 👋",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                name.substringBefore(' ').ifBlank { "there" },
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Surface(onClick = onAvatarClick, shape = CircleShape, color = Color.Transparent) {
            InitialsAvatar(name = name, size = 46)
        }
    }
}

@Composable
private fun DeliverToPill(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Deliver to ", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun OrderHeroCard(shopName: String, onOrder: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(Brush.linearGradient(listOf(LagoonBright, Lagoon)))
    ) {
        // Decorative bubbles
        Box(
            Modifier
                .size(160.dp)
                .offset(x = 220.dp, y = (-50).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
        )
        Box(
            Modifier
                .size(90.dp)
                .offset(x = 280.dp, y = 90.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.10f))
        )
        Column(Modifier.padding(22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    shopName.ifBlank { "Your water shop" },
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(10.dp))
            Text("Fresh, clean water\ndelivered to your door", style = MaterialTheme.typography.headlineSmall, color = Color.White)
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onOrder,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Lagoon),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Default.WaterDrop, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Order water")
            }
        }
    }
}

@Composable
private fun ActiveOrderCard(order: Order, onClick: () -> Unit) {
    SpggSurfaceCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Default.WaterDrop, tint = Lagoon, background = LagoonMist)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Order #${order.id}", style = MaterialTheme.typography.titleMedium)
                Text(
                    "${order.totalBottleQuantity} bottle${if (order.totalBottleQuantity == 1) "" else "s"} · ${formatRs(order.totalPayable)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusChip(order.status)
        }
        Spacer(Modifier.height(16.dp))
        OrderStatusTracker(order.status)
    }
}

@Composable
private fun QuickAction(label: String, icon: ImageVector, accent: Color, soft: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(onClick = onClick, shape = MaterialTheme.shapes.large, color = soft, modifier = Modifier.size(64.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(28.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

// -------------------------------------------------------------- Orders tab

private enum class OrderFilter(val label: String) { All("All"), Active("Active"), Past("Past") }

@Composable
private fun OrdersTab(orders: List<Order>, onOpenOrder: (Long) -> Unit) {
    var filter by rememberSaveable { mutableStateOf(OrderFilter.All) }
    val visible = when (filter) {
        OrderFilter.All -> orders
        OrderFilter.Active -> orders.filter { it.status in ActiveOrderStatuses }
        OrderFilter.Past -> orders.filter { it.status !in ActiveOrderStatuses }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("My orders", style = MaterialTheme.typography.headlineMedium) }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(OrderFilter.entries) { option ->
                    FilterChip(
                        selected = filter == option,
                        onClick = { filter = option },
                        label = { Text(option.label) },
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }
        if (visible.isEmpty()) {
            item {
                EmptyState(
                    title = if (filter == OrderFilter.Active) "Nothing on the way" else "No orders here",
                    message = "Tap \"New order\" to get fresh water delivered.",
                    icon = Icons.AutoMirrored.Filled.ReceiptLong
                )
            }
        } else {
            items(visible, key = { it.id }) { order ->
                OrderSummaryCard(order = order, onClick = { onOpenOrder(order.id) })
            }
        }
    }
}

// ------------------------------------------------------------- Account tab

@Composable
private fun AccountTab(
    state: CustomerHomeUiState,
    fallbackName: String,
    onOpenAddresses: () -> Unit,
    onOpenBottles: () -> Unit,
    onShowWallet: () -> Unit,
    onLogout: () -> Unit
) {
    val profile = state.profile
    val name = profile?.fullName ?: fallbackName
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Text("Account", style = MaterialTheme.typography.headlineMedium) }
        item {
            SpggSurfaceCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InitialsAvatar(name = name, size = 64)
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(name, style = MaterialTheme.typography.titleLarge)
                        profile?.phone?.let {
                            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        profile?.email?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                profile?.let {
                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(it.shopName, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        StatusChip(it.status)
                    }
                }
            }
        }
        item {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(Modifier.padding(vertical = 4.dp)) {
                    ActionRow(
                        title = "Delivery addresses",
                        subtitle = "${state.addresses.size} saved",
                        icon = Icons.Default.LocationOn,
                        accent = CoralPulse,
                        soft = CoralSoft,
                        onClick = onOpenAddresses
                    )
                    ActionRow(
                        title = "My bottles",
                        subtitle = "${state.bottles?.outstanding ?: 0} with you",
                        icon = Icons.Default.LocalDrink,
                        accent = TideTeal,
                        soft = TideTealSoft,
                        onClick = onOpenBottles
                    )
                    ActionRow(
                        title = "Wallet & statement",
                        subtitle = "Balance ${formatRs(state.ledgerBalance)}",
                        icon = Icons.Default.AccountBalanceWallet,
                        accent = Lagoon,
                        soft = LagoonMist,
                        onClick = onShowWallet
                    )
                }
            }
        }
        item {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                ActionRow(
                    title = "Log out",
                    icon = Icons.AutoMirrored.Filled.Logout,
                    accent = DangerRed,
                    soft = DangerSoft,
                    titleColor = DangerRed,
                    onClick = onLogout
                )
            }
        }
        item {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                BrandMark(size = 36)
                Spacer(Modifier.height(6.dp))
                Text(
                    "SaafPaniGharGhar · Clean water, door to door",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
