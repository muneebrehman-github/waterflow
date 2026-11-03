package com.spgg.app.ui.navigation

import com.spgg.app.domain.UserRole

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SHOP_OWNER_HOME = "home/shop_owner"
    const val SHOP_PROFILE = "shop_owner/profile"
    const val PRODUCTS = "shop_owner/products"
    const val SHOP_ORDERS = "shop_owner/orders"
    const val SHOP_ORDER_DETAIL = "shop_owner/orders/{orderId}"
    const val SHOP_CUSTOMERS = "shop_owner/customers"
    const val SHOP_CUSTOMER_LEDGER = "shop_owner/customers/{customerId}/ledger"
    const val SHOP_TEAM = "shop_owner/team"
    const val DELIVERY_DETAIL = "delivery_boy/orders/{orderId}"
    const val STAFF_HOME = "home/staff"
    const val DELIVERY_BOY_HOME = "home/delivery_boy"
    const val CUSTOMER_HOME = "home/customer"
    const val REGISTER = "register"
    const val SHOP_REGISTER = "shop_register"
    const val PENDING_APPROVAL = "pending_approval"
    const val CUSTOMER_PROFILE = "customer/profile"
    const val CUSTOMER_ADDRESSES = "customer/addresses"
    const val NEW_ORDER = "customer/new_order"
    const val ORDER_HISTORY = "customer/orders"
    const val ORDER_DETAIL = "customer/orders/{orderId}"
    const val CUSTOMER_LEDGER = "customer/ledger"
    const val CUSTOMER_BOTTLES = "customer/bottles"
    const val SHOP_BOTTLE_SUMMARY = "shop_owner/bottles/summary"

    fun shopOrderDetail(orderId: Long) = "shop_owner/orders/$orderId"

    fun shopCustomerLedger(customerId: Long) = "shop_owner/customers/$customerId/ledger"

    fun orderDetail(orderId: Long) = "customer/orders/$orderId"

    fun deliveryDetail(orderId: Long) = "delivery_boy/orders/$orderId"

    fun pendingApproval(shopName: String) = "pending_approval?shopName=${java.net.URLEncoder.encode(shopName, "UTF-8")}"
}

/** SUPER_ADMIN never reaches here — AuthViewModel rejects that role's login
 *  before the UI state ever becomes Authenticated (that role uses the web
 *  admin portal instead). Falling back to LOGIN keeps this function total. */
fun homeRouteFor(role: UserRole): String = when (role) {
    UserRole.SHOP_OWNER -> Routes.SHOP_OWNER_HOME
    UserRole.STAFF -> Routes.STAFF_HOME
    UserRole.DELIVERY_BOY -> Routes.DELIVERY_BOY_HOME
    UserRole.CUSTOMER -> Routes.CUSTOMER_HOME
    UserRole.SUPER_ADMIN -> Routes.LOGIN
}
