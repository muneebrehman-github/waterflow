package com.spgg.app.ui.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.spgg.app.domain.ShopCapabilities
import com.spgg.app.domain.UserRole
import com.spgg.app.notification.FcmTokenManager
import com.spgg.app.notification.NotificationNavigation
import com.spgg.app.ui.auth.AuthUiState
import com.spgg.app.ui.auth.AuthViewModel
import com.spgg.app.ui.auth.LocalShopCapabilities
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
import com.spgg.app.ui.shopowner.customers.ShopCustomerLedgerScreen
import com.spgg.app.ui.shopowner.customers.ShopCustomersScreen
import com.spgg.app.ui.shopowner.home.ShopOwnerHomeScreen
import com.spgg.app.ui.shopowner.home.StaffHomeScreen
import com.spgg.app.ui.shopowner.orders.ShopOrderDetailScreen
import com.spgg.app.ui.shopowner.orders.ShopOrdersScreen
import com.spgg.app.ui.shopowner.products.ProductListScreen
import com.spgg.app.ui.shopowner.profile.ShopProfileScreen
import com.spgg.app.ui.shopowner.bottles.ShopBottleSummaryScreen
import com.spgg.app.ui.shopowner.register.PendingApprovalScreen
import com.spgg.app.ui.shopowner.register.ShopRegisterScreen
import com.spgg.app.ui.shopowner.team.TeamScreen
import com.spgg.app.ui.deliveryboy.DeliveryDetailScreen
import com.spgg.app.ui.deliveryboy.DeliveryListScreen
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FcmEntryPoint {
    fun fcmTokenManager(): FcmTokenManager
}

@Composable
fun AppNavHost(authViewModel: AuthViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    val fcmTokenManager = EntryPointAccessors.fromApplication(
        context.applicationContext,
        FcmEntryPoint::class.java
    ).fcmTokenManager()

    val capabilities = when (val state = uiState) {
        is AuthUiState.Authenticated -> ShopCapabilities.forRole(state.session.role)
        else -> ShopCapabilities.forRole(UserRole.CUSTOMER)
    }

    val pendingOrderId by NotificationNavigation.pendingOrderId.collectAsStateWithLifecycle()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            authViewModel.registerFcmToken()
        }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Authenticated -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !fcmTokenManager.canPostNotifications(context)
                ) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    authViewModel.registerFcmToken()
                }
            }
            else -> Unit
        }
    }

    LaunchedEffect(pendingOrderId, uiState) {
        val orderId = pendingOrderId ?: return@LaunchedEffect
        NotificationNavigation.consume()
        val role = (uiState as? AuthUiState.Authenticated)?.session?.role ?: return@LaunchedEffect
        val destination = when (role) {
            UserRole.SHOP_OWNER, UserRole.STAFF -> Routes.shopOrderDetail(orderId)
            UserRole.CUSTOMER -> Routes.orderDetail(orderId)
            UserRole.DELIVERY_BOY -> Routes.deliveryDetail(orderId)
            UserRole.SUPER_ADMIN -> return@LaunchedEffect
        }
        navController.navigate(destination) {
            launchSingleTop = true
        }
    }

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

    CompositionLocalProvider(LocalShopCapabilities provides capabilities) {
        NavHost(navController = navController, startDestination = Routes.SPLASH) {
            composable(Routes.SPLASH) { SplashScreen() }
            composable(Routes.LOGIN) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                    onNavigateToShopRegister = { navController.navigate(Routes.SHOP_REGISTER) }
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(authViewModel = authViewModel, onBackToLogin = { navController.popBackStack() })
            }
            composable(Routes.SHOP_REGISTER) {
                ShopRegisterScreen(
                    onBackToLogin = { navController.popBackStack() },
                    onRegistrationComplete = { shopName ->
                        navController.navigate(Routes.pendingApproval(shopName)) {
                            popUpTo(Routes.LOGIN) { inclusive = false }
                        }
                    }
                )
            }
            composable(
                route = "${Routes.PENDING_APPROVAL}?shopName={shopName}",
                arguments = listOf(navArgument("shopName") { type = NavType.StringType })
            ) { backStackEntry ->
                val shopName = backStackEntry.arguments?.getString("shopName").orEmpty()
                PendingApprovalScreen(
                    shopName = shopName,
                    onBackToLogin = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                        }
                    }
                )
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
            composable(Routes.STAFF_HOME) {
                StaffHomeScreen(
                    onOpenProducts = { navController.navigate(Routes.PRODUCTS) },
                    onOpenOrders = { navController.navigate(Routes.SHOP_ORDERS) },
                    onOpenCustomers = { navController.navigate(Routes.SHOP_CUSTOMERS) },
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
                    onOpenNewOrder = { navController.navigate(Routes.NEW_ORDER) },
                    onOpenOrder = { orderId -> navController.navigate(Routes.orderDetail(orderId)) },
                    onOpenAddresses = { navController.navigate(Routes.CUSTOMER_ADDRESSES) },
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
}
