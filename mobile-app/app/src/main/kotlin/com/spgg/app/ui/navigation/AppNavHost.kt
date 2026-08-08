package com.spgg.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.spgg.app.ui.auth.AuthUiState
import com.spgg.app.ui.auth.AuthViewModel
import com.spgg.app.ui.auth.LoginScreen
import com.spgg.app.ui.auth.SplashScreen
import com.spgg.app.ui.customer.addresses.AddressListScreen
import com.spgg.app.ui.customer.bottles.CustomerBottleBalanceScreen
import com.spgg.app.ui.customer.home.CustomerHomeScreen
import com.spgg.app.ui.customer.ledger.CustomerLedgerScreen
import com.spgg.app.ui.customer.orders.NewOrderScreen
import com.spgg.app.ui.customer.orders.OrderDetailScreen
import com.spgg.app.ui.customer.orders.OrderHistoryScreen
import com.spgg.app.ui.customer.profile.CustomerProfileScreen
import com.spgg.app.ui.customer.register.RegisterScreen
import com.spgg.app.ui.home.RoleHomeScreen
import com.spgg.app.ui.shopowner.customers.ShopCustomerLedgerScreen
import com.spgg.app.ui.shopowner.customers.ShopCustomersScreen
import com.spgg.app.ui.shopowner.home.ShopOwnerHomeScreen
import com.spgg.app.ui.shopowner.orders.ShopOrderDetailScreen
import com.spgg.app.ui.shopowner.orders.ShopOrdersScreen
import com.spgg.app.ui.shopowner.products.ProductListScreen
import com.spgg.app.ui.shopowner.profile.ShopProfileScreen
import com.spgg.app.ui.shopowner.bottles.ShopBottleSummaryScreen
import com.spgg.app.ui.shopowner.team.TeamScreen
import com.spgg.app.ui.deliveryboy.DeliveryDetailScreen
import com.spgg.app.ui.deliveryboy.DeliveryListScreen

@Composable
fun AppNavHost(authViewModel: AuthViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        val destination = when (val state = uiState) {
            is AuthUiState.Unknown -> null
            is AuthUiState.Unauthenticated -> Routes.LOGIN
            is AuthUiState.Authenticated -> homeRouteFor(state.session.role)
        } ?: return@LaunchedEffect

        navController.navigate(destination) {
            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) { SplashScreen() }
        composable(Routes.LOGIN) {
            LoginScreen(authViewModel, onNavigateToRegister = { navController.navigate(Routes.REGISTER) })
        }
        composable(Routes.REGISTER) {
            RegisterScreen(authViewModel = authViewModel, onBackToLogin = { navController.popBackStack() })
        }
        composable(Routes.SHOP_OWNER_HOME) {
            ShopOwnerHomeScreen(
                onOpenShopProfile = { navController.navigate(Routes.SHOP_PROFILE) },
                onOpenProducts = { navController.navigate(Routes.PRODUCTS) },
                onOpenOrders = { navController.navigate(Routes.SHOP_ORDERS) },
                onOpenCustomers = { navController.navigate(Routes.SHOP_CUSTOMERS) },
                onOpenTeam = { navController.navigate(Routes.SHOP_TEAM) },
                onOpenBottleTracking = { navController.navigate(Routes.SHOP_BOTTLE_SUMMARY) },
                authViewModel = authViewModel
            )
        }
        composable(Routes.SHOP_TEAM) { TeamScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.SHOP_BOTTLE_SUMMARY) { ShopBottleSummaryScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.SHOP_PROFILE) { ShopProfileScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.PRODUCTS) { ProductListScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.SHOP_ORDERS) {
            ShopOrdersScreen(
                onBack = { navController.popBackStack() },
                onOpenOrder = { orderId -> navController.navigate(Routes.shopOrderDetail(orderId)) }
            )
        }
        composable(
            Routes.SHOP_ORDER_DETAIL,
            arguments = listOf(navArgument("orderId") { type = NavType.LongType })
        ) {
            ShopOrderDetailScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SHOP_CUSTOMERS) {
            ShopCustomersScreen(
                onBack = { navController.popBackStack() },
                onOpenCustomerLedger = { customerId -> navController.navigate(Routes.shopCustomerLedger(customerId)) }
            )
        }
        composable(
            Routes.SHOP_CUSTOMER_LEDGER,
            arguments = listOf(navArgument("customerId") { type = NavType.LongType })
        ) {
            ShopCustomerLedgerScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.STAFF_HOME) { RoleHomeScreen(title = "Staff", authViewModel = authViewModel) }
        composable(Routes.DELIVERY_BOY_HOME) {
            DeliveryListScreen(
                onOpenOrder = { orderId -> navController.navigate(Routes.deliveryDetail(orderId)) },
                authViewModel = authViewModel
            )
        }
        composable(
            Routes.DELIVERY_DETAIL,
            arguments = listOf(navArgument("orderId") { type = NavType.LongType })
        ) {
            DeliveryDetailScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.CUSTOMER_HOME) {
            CustomerHomeScreen(
                onOpenProfile = { navController.navigate(Routes.CUSTOMER_PROFILE) },
                onOpenAddresses = { navController.navigate(Routes.CUSTOMER_ADDRESSES) },
                onOpenNewOrder = { navController.navigate(Routes.NEW_ORDER) },
                onOpenOrderHistory = { navController.navigate(Routes.ORDER_HISTORY) },
                onOpenLedger = { navController.navigate(Routes.CUSTOMER_LEDGER) },
                onOpenBottles = { navController.navigate(Routes.CUSTOMER_BOTTLES) },
                authViewModel = authViewModel
            )
        }
        composable(Routes.CUSTOMER_PROFILE) { CustomerProfileScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.CUSTOMER_ADDRESSES) { AddressListScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.CUSTOMER_LEDGER) { CustomerLedgerScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.CUSTOMER_BOTTLES) { CustomerBottleBalanceScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.NEW_ORDER) {
            NewOrderScreen(
                onBack = { navController.popBackStack() },
                onOrderPlaced = { orderId ->
                    navController.navigate(Routes.orderDetail(orderId)) {
                        popUpTo(Routes.CUSTOMER_HOME)
                    }
                }
            )
        }
        composable(Routes.ORDER_HISTORY) {
            OrderHistoryScreen(
                onBack = { navController.popBackStack() },
                onOpenOrder = { orderId -> navController.navigate(Routes.orderDetail(orderId)) }
            )
        }
        composable(
            Routes.ORDER_DETAIL,
            arguments = listOf(navArgument("orderId") { type = NavType.LongType })
        ) {
            OrderDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}
