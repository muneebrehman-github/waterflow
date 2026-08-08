package com.spgg.backend.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.spgg.backend.common.BadRequestException;
import com.spgg.backend.common.TenantContext;
import com.spgg.backend.customer.CustomerAddress;
import com.spgg.backend.customer.CustomerAddressRepository;
import com.spgg.backend.ledger.LedgerService;
import com.spgg.backend.notification.NotificationService;
import com.spgg.backend.product.Product;
import com.spgg.backend.product.ProductRepository;
import com.spgg.backend.product.ProductStatus;
import com.spgg.backend.shop.Shop;
import com.spgg.backend.shop.ShopRepository;
import com.spgg.backend.shop.ShopStatus;
import com.spgg.backend.user.Role;
import com.spgg.backend.user.User;
import com.spgg.backend.user.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CustomerAddressRepository addressRepository;
    @Mock
    private ShopRepository shopRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LedgerService ledgerService;
    @Mock
    private NotificationService notificationService;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        orderService = new OrderService(
                orderRepository, orderItemRepository, productRepository, addressRepository, shopRepository,
                userRepository, ledgerService, notificationService, objectMapper);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(42L, null, List.of()));
        TenantContext.setTenantId(1L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    private Shop activeShop() {
        Shop shop = new Shop();
        shop.setId(1L);
        shop.setStatus(ShopStatus.ACTIVE);
        shop.setMinOrderValue(BigDecimal.valueOf(200));
        // No hours/tiers configured — assertWithinDeliveryHours short-circuits, calculateDeliveryCharge falls back to 0.
        return shop;
    }

    private Product availableProduct(long id, double price, double deposit) {
        Product product = new Product();
        product.setId(id);
        product.setName("19L Bottle");
        product.setUnitPrice(BigDecimal.valueOf(price));
        product.setDepositAmount(BigDecimal.valueOf(deposit));
        product.setStatus(ProductStatus.AVAILABLE);
        return product;
    }

    private CustomerAddress ownAddress() {
        CustomerAddress address = new CustomerAddress();
        address.setId(9L);
        address.setUserId(42L);
        address.setAddressText("123 Main St");
        return address;
    }

    private PlaceOrderRequest requestFor(long productId, int quantity) {
        return new PlaceOrderRequest(9L, List.of(new OrderItemRequest(productId, quantity)), null);
    }

    @Test
    void placeOrder_computesTotalsAndRecordsBothLedgerEntries() {
        when(shopRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(activeShop()));
        when(addressRepository.findByIdAndUserIdAndDeletedFalse(9L, 42L)).thenReturn(Optional.of(ownAddress()));
        when(productRepository.findByIdAndDeletedFalse(5L)).thenReturn(Optional.of(availableProduct(5L, 300, 500)));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(100L);
            return o;
        });
        when(orderItemRepository.findByOrderIdAndDeletedFalse(100L)).thenReturn(List.of());

        // 2 x (price 300, deposit 500) = subtotal 600, deposit 1000; min order 200 satisfied; no delivery tiers -> charge 0.
        OrderResponse response = orderService.placeOrder(requestFor(5L, 2));

        assertThat(response.subtotal()).isEqualByComparingTo(BigDecimal.valueOf(600));
        assertThat(response.deliveryCharge()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.orderAmount()).isEqualByComparingTo(BigDecimal.valueOf(600));
        assertThat(response.totalDeposit()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(response.totalBottleQuantity()).isEqualTo(2);
        assertThat(response.totalPayable()).isEqualByComparingTo(BigDecimal.valueOf(1600));

        org.mockito.Mockito.verify(ledgerService).recordEntry(
                eq(42L), eq(com.spgg.backend.ledger.LedgerEntryType.DEBIT), eqBigDecimal(BigDecimal.valueOf(600)),
                eq("ORDER"), eq(100L), eq("Order #100"));
        org.mockito.Mockito.verify(ledgerService).recordEntry(
                eq(42L), eq(com.spgg.backend.ledger.LedgerEntryType.DEPOSIT_DEBIT), eqBigDecimal(BigDecimal.valueOf(1000)),
                eq("ORDER"), eq(100L), eq("Bottle deposit for order #100"));
    }

    @Test
    void placeOrder_rejectsWhenBelowMinimumOrderValue() {
        when(shopRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(activeShop()));
        when(addressRepository.findByIdAndUserIdAndDeletedFalse(9L, 42L)).thenReturn(Optional.of(ownAddress()));
        when(productRepository.findByIdAndDeletedFalse(5L)).thenReturn(Optional.of(availableProduct(5L, 50, 0)));

        assertThatThrownBy(() -> orderService.placeOrder(requestFor(5L, 1))).isInstanceOf(BadRequestException.class);
    }

    @Test
    void placeOrder_rejectsUnavailableProduct() {
        when(shopRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(activeShop()));
        when(addressRepository.findByIdAndUserIdAndDeletedFalse(9L, 42L)).thenReturn(Optional.of(ownAddress()));
        Product unavailable = availableProduct(5L, 300, 0);
        unavailable.setStatus(ProductStatus.UNAVAILABLE);
        when(productRepository.findByIdAndDeletedFalse(5L)).thenReturn(Optional.of(unavailable));

        assertThatThrownBy(() -> orderService.placeOrder(requestFor(5L, 1))).isInstanceOf(BadRequestException.class);
    }

    @Test
    void placeOrder_rejectsWhenShopNotActive() {
        Shop suspended = activeShop();
        suspended.setStatus(ShopStatus.SUSPENDED);
        when(shopRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(suspended));

        assertThatThrownBy(() -> orderService.placeOrder(requestFor(5L, 1))).isInstanceOf(BadRequestException.class);
    }

    private Order pendingOrder() {
        Order order = new Order();
        order.setId(100L);
        order.setCustomerId(42L);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderAmount(BigDecimal.valueOf(600));
        order.setTotalDeposit(BigDecimal.valueOf(1000));
        return order;
    }

    @Test
    void cancelByCustomer_onlyAllowedWhilePending_andReversesLedger() {
        Order order = pendingOrder();
        when(orderRepository.findByIdAndCustomerIdAndDeletedFalse(100L, 42L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderIdAndDeletedFalse(100L)).thenReturn(List.of());

        orderService.cancelByCustomer(100L);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        org.mockito.Mockito.verify(ledgerService).recordEntry(
                eq(42L), eq(com.spgg.backend.ledger.LedgerEntryType.CREDIT), eqBigDecimal(BigDecimal.valueOf(-600)),
                eq("ORDER"), eq(100L), any());
        org.mockito.Mockito.verify(ledgerService).recordEntry(
                eq(42L), eq(com.spgg.backend.ledger.LedgerEntryType.DEPOSIT_CREDIT), eqBigDecimal(BigDecimal.valueOf(-1000)),
                eq("ORDER"), eq(100L), any());
    }

    @Test
    void cancelByCustomer_rejectsWhenNotPending() {
        Order order = pendingOrder();
        order.setStatus(OrderStatus.ACCEPTED);
        when(orderRepository.findByIdAndCustomerIdAndDeletedFalse(100L, 42L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelByCustomer(100L)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void accept_rejectsWhenNotPending() {
        Order order = pendingOrder();
        order.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.findByIdAndDeletedFalse(100L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.accept(100L)).isInstanceOf(BadRequestException.class);
    }

    private Order preparingOrder() {
        Order order = new Order();
        order.setId(100L);
        order.setCustomerId(42L);
        order.setStatus(OrderStatus.PREPARING);
        order.setOrderAmount(BigDecimal.valueOf(600));
        order.setTotalDeposit(BigDecimal.valueOf(1000));
        order.setTotalBottleQuantity(2);
        return order;
    }

    private User deliveryBoy(long id) {
        User user = new User();
        user.setId(id);
        user.setTenantId(1L);
        user.setRole(Role.DELIVERY_BOY);
        return user;
    }

    @Test
    void assignDelivery_movesToOutForDelivery() {
        Order order = preparingOrder();
        when(orderRepository.findByIdAndDeletedFalse(100L)).thenReturn(Optional.of(order));
        when(userRepository.findByIdAndTenantIdAndDeletedFalse(7L, 1L)).thenReturn(Optional.of(deliveryBoy(7L)));
        when(orderItemRepository.findByOrderIdAndDeletedFalse(100L)).thenReturn(List.of());

        OrderResponse response = orderService.assignDelivery(100L, 7L);

        assertThat(response.status()).isEqualTo(OrderStatus.OUT_FOR_DELIVERY);
        assertThat(response.deliveryBoyId()).isEqualTo(7L);
    }

    @Test
    void assignDelivery_rejectsNonDeliveryBoyAccount() {
        Order order = preparingOrder();
        when(orderRepository.findByIdAndDeletedFalse(100L)).thenReturn(Optional.of(order));
        User staff = deliveryBoy(7L);
        staff.setRole(Role.STAFF);
        when(userRepository.findByIdAndTenantIdAndDeletedFalse(7L, 1L)).thenReturn(Optional.of(staff));

        assertThatThrownBy(() -> orderService.assignDelivery(100L, 7L)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void assignDelivery_rejectsWhenNotPreparing() {
        Order order = preparingOrder();
        order.setStatus(OrderStatus.ACCEPTED);
        when(orderRepository.findByIdAndDeletedFalse(100L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.assignDelivery(100L, 7L)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void completeDelivery_fullReturn_refundsEntireDeposit() {
        Order order = preparingOrder();
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        order.setDeliveryBoyId(42L);
        when(orderRepository.findByIdAndDeliveryBoyIdAndDeletedFalse(100L, 42L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderIdAndDeletedFalse(100L)).thenReturn(List.of());

        OrderResponse response = orderService.completeDelivery(100L, 2);

        assertThat(response.status()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(response.emptyBottlesCollected()).isEqualTo(2);
        org.mockito.Mockito.verify(ledgerService).recordEntry(
                eq(42L), eq(com.spgg.backend.ledger.LedgerEntryType.DEPOSIT_CREDIT), eqBigDecimal(BigDecimal.valueOf(-1000)),
                eq("ORDER"), eq(100L), any());
    }

    @Test
    void completeDelivery_partialReturn_refundsProportionally() {
        Order order = preparingOrder();
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        order.setDeliveryBoyId(42L);
        when(orderRepository.findByIdAndDeliveryBoyIdAndDeletedFalse(100L, 42L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderIdAndDeletedFalse(100L)).thenReturn(List.of());

        orderService.completeDelivery(100L, 1);

        org.mockito.Mockito.verify(ledgerService).recordEntry(
                eq(42L), eq(com.spgg.backend.ledger.LedgerEntryType.DEPOSIT_CREDIT), eqBigDecimal(BigDecimal.valueOf(-500)),
                eq("ORDER"), eq(100L), any());
    }

    @Test
    void completeDelivery_noBottlesReturned_recordsNoRefund() {
        Order order = preparingOrder();
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        order.setDeliveryBoyId(42L);
        when(orderRepository.findByIdAndDeliveryBoyIdAndDeletedFalse(100L, 42L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderIdAndDeletedFalse(100L)).thenReturn(List.of());

        orderService.completeDelivery(100L, 0);

        org.mockito.Mockito.verify(ledgerService, org.mockito.Mockito.never()).recordEntry(
                any(), eq(com.spgg.backend.ledger.LedgerEntryType.DEPOSIT_CREDIT), any(), any(), any(), any());
    }

    private static <T> T eq(T value) {
        return org.mockito.ArgumentMatchers.eq(value);
    }

    private static BigDecimal eqBigDecimal(BigDecimal value) {
        return org.mockito.ArgumentMatchers.argThat(actual -> actual != null && actual.compareTo(value) == 0);
    }
}
