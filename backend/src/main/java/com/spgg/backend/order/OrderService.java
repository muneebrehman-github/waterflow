package com.spgg.backend.order;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spgg.backend.common.BadRequestException;
import com.spgg.backend.common.ForbiddenException;
import com.spgg.backend.common.ResourceNotFoundException;
import com.spgg.backend.common.TenantContext;
import com.spgg.backend.customer.CustomerAddress;
import com.spgg.backend.customer.CustomerAddressRepository;
import com.spgg.backend.ledger.LedgerEntryType;
import com.spgg.backend.ledger.LedgerService;
import com.spgg.backend.notification.NotificationService;
import com.spgg.backend.product.Product;
import com.spgg.backend.product.ProductRepository;
import com.spgg.backend.product.ProductStatus;
import com.spgg.backend.security.CurrentUser;
import com.spgg.backend.shop.DayHours;
import com.spgg.backend.shop.DeliveryChargeTier;
import com.spgg.backend.shop.Shop;
import com.spgg.backend.shop.ShopRepository;
import com.spgg.backend.shop.ShopStatus;
import com.spgg.backend.user.Role;
import com.spgg.backend.user.User;
import com.spgg.backend.user.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final CustomerAddressRepository addressRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final LedgerService ledgerService;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        Long customerId = CurrentUser.id();
        Long tenantId = requireTenantId();
        Shop shop = shopRepository.findByIdAndDeletedFalse(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found: " + tenantId));

        if (shop.getStatus() != ShopStatus.ACTIVE) {
            throw new BadRequestException("This shop isn't accepting orders right now");
        }
        assertWithinDeliveryHours(shop);

        CustomerAddress address = addressRepository.findByIdAndUserIdAndDeletedFalse(request.addressId(), customerId)
                .orElseThrow(() -> new BadRequestException("Delivery address not found"));

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDeposit = BigDecimal.ZERO;
        int totalBottleQuantity = 0;
        List<OrderItem> pendingItems = new java.util.ArrayList<>();

        for (OrderItemRequest itemRequest : request.items()) {
            Product product = productRepository.findByIdAndDeletedFalse(itemRequest.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemRequest.productId()));
            if (product.getStatus() != ProductStatus.AVAILABLE) {
                throw new BadRequestException("'" + product.getName() + "' is not currently available");
            }

            BigDecimal quantity = BigDecimal.valueOf(itemRequest.quantity());
            BigDecimal lineTotal = product.getUnitPrice().multiply(quantity);
            BigDecimal lineDeposit = product.getDepositAmount().multiply(quantity);
            subtotal = subtotal.add(lineTotal);
            totalDeposit = totalDeposit.add(lineDeposit);
            if (product.getDepositAmount().compareTo(BigDecimal.ZERO) > 0) {
                totalBottleQuantity += itemRequest.quantity();
            }

            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setQuantity(itemRequest.quantity());
            item.setUnitPrice(product.getUnitPrice());
            item.setDepositAmount(product.getDepositAmount());
            item.setLineTotal(lineTotal);
            pendingItems.add(item);
        }

        if (shop.getMinOrderValue() != null && subtotal.compareTo(shop.getMinOrderValue()) < 0) {
            throw new BadRequestException("Minimum order value is " + shop.getMinOrderValue());
        }

        BigDecimal deliveryCharge = calculateDeliveryCharge(shop, subtotal);
        BigDecimal orderAmount = subtotal.add(deliveryCharge);
        BigDecimal totalPayable = orderAmount.add(totalDeposit);

        Order order = new Order();
        order.setTenantId(tenantId);
        order.setCustomerId(customerId);
        order.setAddressId(request.addressId());
        order.setStatus(OrderStatus.PENDING);
        order.setSubtotal(subtotal);
        order.setDeliveryCharge(deliveryCharge);
        order.setOrderAmount(orderAmount);
        order.setTotalDeposit(totalDeposit);
        order.setTotalBottleQuantity(totalBottleQuantity);
        order.setTotalPayable(totalPayable);
        order.setDeliveryNotes(request.deliveryNotes());
        order = orderRepository.save(order);

        for (OrderItem item : pendingItems) {
            item.setTenantId(tenantId);
            item.setOrderId(order.getId());
            orderItemRepository.save(item);
        }

        ledgerService.recordEntry(customerId, LedgerEntryType.DEBIT, orderAmount, "ORDER", order.getId(),
                "Order #" + order.getId());
        if (totalDeposit.compareTo(BigDecimal.ZERO) > 0) {
            ledgerService.recordEntry(customerId, LedgerEntryType.DEPOSIT_DEBIT, totalDeposit, "ORDER", order.getId(),
                    "Bottle deposit for order #" + order.getId());
        }

        notificationService.notifyUser(shop.getOwnerUserId(), "New Order Received",
                "Order #" + order.getId() + " for " + order.getTotalPayable(), orderData(order));

        return toResponse(order);
    }

    public List<OrderResponse> listForCustomer() {
        return orderRepository.findByCustomerIdAndDeletedFalseOrderByCreatedAtDesc(CurrentUser.id()).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<OrderResponse> listForShop() {
        return orderRepository.findByDeletedFalseOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    public OrderResponse getForCustomer(Long orderId) {
        Order order = orderRepository.findByIdAndCustomerIdAndDeletedFalse(orderId, CurrentUser.id())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        return toResponse(order);
    }

    public OrderResponse getForShop(Long orderId) {
        return toResponse(requireOrder(orderId));
    }

    @Transactional
    public OrderResponse cancelByCustomer(Long orderId) {
        Order order = orderRepository.findByIdAndCustomerIdAndDeletedFalse(orderId, CurrentUser.id())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Only pending orders can be cancelled — ask the shop to cancel it instead");
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        reverseLedgerForOrder(order, "Customer cancelled order #" + order.getId());
        notificationService.notifyUser(order.getCustomerId(), "Order Cancelled",
                "Order #" + order.getId() + " was cancelled", orderData(order));
        return toResponse(order);
    }

    @Transactional
    public OrderResponse accept(Long orderId) {
        Order order = requireOrder(orderId);
        requireStatus(order, OrderStatus.PENDING);
        order.setStatus(OrderStatus.ACCEPTED);
        orderRepository.save(order);
        notificationService.notifyUser(order.getCustomerId(), "Order Accepted",
                "Order #" + order.getId() + " has been accepted", orderData(order));
        return toResponse(order);
    }

    @Transactional
    public OrderResponse reject(Long orderId, String reason) {
        Order order = requireOrder(orderId);
        requireStatus(order, OrderStatus.PENDING);
        order.setStatus(OrderStatus.REJECTED);
        order.setStatusReason(reason);
        orderRepository.save(order);
        reverseLedgerForOrder(order, "Shop rejected order #" + order.getId());
        notificationService.notifyUser(order.getCustomerId(), "Order Rejected",
                "Order #" + order.getId() + " was rejected: " + reason, orderData(order));
        return toResponse(order);
    }

    @Transactional
    public OrderResponse markPreparing(Long orderId) {
        Order order = requireOrder(orderId);
        requireStatus(order, OrderStatus.ACCEPTED);
        order.setStatus(OrderStatus.PREPARING);
        orderRepository.save(order);
        notificationService.notifyUser(order.getCustomerId(), "Order Preparing",
                "Order #" + order.getId() + " is being prepared", orderData(order));
        return toResponse(order);
    }

    @Transactional
    public OrderResponse cancelByShop(Long orderId, String reason) {
        Order order = requireOrder(orderId);
        if (order.getStatus() != OrderStatus.ACCEPTED && order.getStatus() != OrderStatus.PREPARING) {
            throw new BadRequestException("Only accepted or preparing orders can be cancelled here");
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setStatusReason(reason);
        orderRepository.save(order);
        reverseLedgerForOrder(order, "Shop cancelled order #" + order.getId());
        notificationService.notifyUser(order.getCustomerId(), "Order Cancelled",
                "Order #" + order.getId() + " was cancelled: " + reason, orderData(order));
        return toResponse(order);
    }

    @Transactional
    public OrderResponse assignDelivery(Long orderId, Long deliveryBoyId) {
        Order order = requireOrder(orderId);
        requireStatus(order, OrderStatus.PREPARING);

        Long tenantId = requireTenantId();
        User deliveryBoy = userRepository.findByIdAndTenantIdAndDeletedFalse(deliveryBoyId, tenantId)
                .orElseThrow(() -> new BadRequestException("Delivery boy not found: " + deliveryBoyId));
        if (deliveryBoy.getRole() != Role.DELIVERY_BOY) {
            throw new BadRequestException("This account is not a Delivery Boy");
        }

        order.setDeliveryBoyId(deliveryBoyId);
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        orderRepository.save(order);
        notificationService.notifyUser(order.getCustomerId(), "Out for Delivery",
                "Order #" + order.getId() + " is on its way", orderData(order));
        return toResponse(order);
    }

    public List<OrderResponse> listForDeliveryBoy() {
        return orderRepository
                .findByDeliveryBoyIdAndStatusAndDeletedFalseOrderByCreatedAtAsc(CurrentUser.id(), OrderStatus.OUT_FOR_DELIVERY)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public OrderResponse getForDeliveryBoy(Long orderId) {
        Order order = orderRepository.findByIdAndDeliveryBoyIdAndDeletedFalse(orderId, CurrentUser.id())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        return toResponse(order);
    }

    @Transactional
    public OrderResponse completeDelivery(Long orderId, Integer emptyBottlesCollected) {
        Order order = orderRepository.findByIdAndDeliveryBoyIdAndDeletedFalse(orderId, CurrentUser.id())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        requireStatus(order, OrderStatus.OUT_FOR_DELIVERY);

        int collected = emptyBottlesCollected == null ? 0 : Math.min(emptyBottlesCollected, order.getTotalBottleQuantity());
        order.setEmptyBottlesCollected(collected);
        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);

        if (collected > 0 && order.getTotalBottleQuantity() > 0 && order.getTotalDeposit().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal refund = order.getTotalDeposit()
                    .multiply(BigDecimal.valueOf(collected))
                    .divide(BigDecimal.valueOf(order.getTotalBottleQuantity()), 2, RoundingMode.HALF_UP);
            ledgerService.recordEntry(order.getCustomerId(), LedgerEntryType.DEPOSIT_CREDIT, refund.negate(), "ORDER",
                    order.getId(), "Empty bottles returned (" + collected + "/" + order.getTotalBottleQuantity()
                            + ") for order #" + order.getId());
        }

        notificationService.notifyUser(order.getCustomerId(), "Order Delivered",
                "Order #" + order.getId() + " has been delivered", orderData(order));

        return toResponse(order);
    }

    private java.util.Map<String, String> orderData(Order order) {
        return java.util.Map.of("orderId", String.valueOf(order.getId()), "status", order.getStatus().name());
    }

    private void reverseLedgerForOrder(Order order, String description) {
        ledgerService.recordEntry(order.getCustomerId(), LedgerEntryType.CREDIT, order.getOrderAmount().negate(),
                "ORDER", order.getId(), description);
        if (order.getTotalDeposit().compareTo(BigDecimal.ZERO) > 0) {
            ledgerService.recordEntry(order.getCustomerId(), LedgerEntryType.DEPOSIT_CREDIT,
                    order.getTotalDeposit().negate(), "ORDER", order.getId(), description);
        }
    }

    private void assertWithinDeliveryHours(Shop shop) {
        List<DayHours> deliveryHours = readHours(shop.getDeliveryHoursJson());
        if (deliveryHours.isEmpty()) {
            return; // not configured yet — don't block orders on missing Phase-1 setup
        }
        DayOfWeek today = DayOfWeek.from(java.time.LocalDate.now());
        DayHours todayHours = deliveryHours.stream()
                .filter(h -> h.day() == today)
                .findFirst()
                .orElse(null);
        if (todayHours == null || todayHours.closed()) {
            throw new BadRequestException("This shop isn't delivering today");
        }
        LocalTime now = LocalTime.now();
        if (now.isBefore(todayHours.openTime()) || now.isAfter(todayHours.closeTime())) {
            throw new BadRequestException(
                    "Orders can only be placed between " + todayHours.openTime() + " and " + todayHours.closeTime());
        }
    }

    private BigDecimal calculateDeliveryCharge(Shop shop, BigDecimal subtotal) {
        List<DeliveryChargeTier> tiers = readTiers(shop.getDeliveryChargeTiersJson());
        return tiers.stream()
                .filter(tier -> subtotal.compareTo(tier.minOrderValue()) >= 0
                        && (tier.maxOrderValue() == null || subtotal.compareTo(tier.maxOrderValue()) < 0))
                .map(DeliveryChargeTier::charge)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    private List<DayHours> readHours(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<DayHours>>() {
            });
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse stored shop hours", e);
        }
    }

    private List<DeliveryChargeTier> readTiers(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<DeliveryChargeTier>>() {
            });
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse stored delivery charge tiers", e);
        }
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = orderItemRepository.findByOrderIdAndDeletedFalse(order.getId()).stream()
                .map(OrderItemResponse::from)
                .toList();

        User customer = userRepository.findById(order.getCustomerId()).orElse(null);
        CustomerAddress address = addressRepository.findByIdAndDeletedFalse(order.getAddressId()).orElse(null);

        return OrderResponse.from(order, items,
                customer != null ? customer.getFullName() : null,
                customer != null ? customer.getPhone() : null,
                address != null ? address.getLabel() : null,
                address != null ? address.getAddressText() : null,
                address != null ? address.getArea() : null);
    }

    private Order requireOrder(Long orderId) {
        return orderRepository.findByIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    }

    private void requireStatus(Order order, OrderStatus expected) {
        if (order.getStatus() != expected) {
            throw new BadRequestException("Order must be " + expected + " for this action (currently "
                    + order.getStatus() + ")");
        }
    }

    private Long requireTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new ForbiddenException("No shop associated with this account");
        }
        return tenantId;
    }
}
