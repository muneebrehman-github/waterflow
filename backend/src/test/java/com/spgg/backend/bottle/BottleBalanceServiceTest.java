package com.spgg.backend.bottle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.spgg.backend.common.BadRequestException;
import com.spgg.backend.common.TenantContext;
import com.spgg.backend.order.Order;
import com.spgg.backend.order.OrderRepository;
import com.spgg.backend.order.OrderStatus;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class BottleBalanceServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private BottleAdjustmentRepository bottleAdjustmentRepository;

    private BottleBalanceService bottleBalanceService;

    @BeforeEach
    void setUp() {
        bottleBalanceService = new BottleBalanceService(orderRepository, bottleAdjustmentRepository);
        TenantContext.setTenantId(7L);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(99L, null, List.of()));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    private Order deliveredOrder(int totalBottleQuantity, Integer emptyBottlesCollected, double totalDeposit) {
        Order order = new Order();
        order.setCustomerId(42L);
        order.setStatus(OrderStatus.DELIVERED);
        order.setTotalBottleQuantity(totalBottleQuantity);
        order.setEmptyBottlesCollected(emptyBottlesCollected);
        order.setTotalDeposit(BigDecimal.valueOf(totalDeposit));
        return order;
    }

    private BottleAdjustment adjustment(int quantity, BottleAdjustmentType type) {
        BottleAdjustment adjustment = new BottleAdjustment();
        adjustment.setCustomerId(42L);
        adjustment.setQuantity(quantity);
        adjustment.setType(type);
        adjustment.setReason("test");
        return adjustment;
    }

    @Test
    void getBalance_computesOutstandingFromDeliveredMinusReturned() {
        when(orderRepository.findByCustomerIdAndStatusAndDeletedFalse(42L, OrderStatus.DELIVERED))
                .thenReturn(List.of(deliveredOrder(2, 1, 1000)));
        when(bottleAdjustmentRepository.findByCustomerIdAndDeletedFalse(42L)).thenReturn(List.of());

        BottleBalanceResponse balance = bottleBalanceService.getBalance(42L);

        assertThat(balance.delivered()).isEqualTo(2);
        assertThat(balance.returned()).isEqualTo(1);
        assertThat(balance.outstanding()).isEqualTo(1);
    }

    @Test
    void getBalance_treatsNullEmptyBottlesCollectedAsZero() {
        when(orderRepository.findByCustomerIdAndStatusAndDeletedFalse(42L, OrderStatus.DELIVERED))
                .thenReturn(List.of(deliveredOrder(2, null, 1000)));
        when(bottleAdjustmentRepository.findByCustomerIdAndDeletedFalse(42L)).thenReturn(List.of());

        BottleBalanceResponse balance = bottleBalanceService.getBalance(42L);

        assertThat(balance.returned()).isEqualTo(0);
        assertThat(balance.outstanding()).isEqualTo(2);
    }

    @Test
    void getBalance_subtractsLostAndDamagedFromOutstanding() {
        when(orderRepository.findByCustomerIdAndStatusAndDeletedFalse(42L, OrderStatus.DELIVERED))
                .thenReturn(List.of(deliveredOrder(4, 1, 2000)));
        when(bottleAdjustmentRepository.findByCustomerIdAndDeletedFalse(42L))
                .thenReturn(List.of(adjustment(1, BottleAdjustmentType.LOST), adjustment(1, BottleAdjustmentType.DAMAGED)));

        BottleBalanceResponse balance = bottleBalanceService.getBalance(42L);

        assertThat(balance.lost()).isEqualTo(1);
        assertThat(balance.damaged()).isEqualTo(1);
        assertThat(balance.outstanding()).isEqualTo(1);
    }

    @Test
    void markAdjustment_rejectsWhenQuantityExceedsOutstanding() {
        when(orderRepository.findByCustomerIdAndStatusAndDeletedFalse(42L, OrderStatus.DELIVERED))
                .thenReturn(List.of(deliveredOrder(2, 1, 1000)));
        when(bottleAdjustmentRepository.findByCustomerIdAndDeletedFalse(42L)).thenReturn(List.of());

        BottleAdjustmentRequest request = new BottleAdjustmentRequest(5, BottleAdjustmentType.LOST, "broke it");

        assertThatThrownBy(() -> bottleBalanceService.markAdjustment(42L, request)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void markAdjustment_savesAndReturnsUpdatedBalance() {
        when(orderRepository.findByCustomerIdAndStatusAndDeletedFalse(42L, OrderStatus.DELIVERED))
                .thenReturn(List.of(deliveredOrder(2, 0, 1000)));
        when(bottleAdjustmentRepository.findByCustomerIdAndDeletedFalse(42L))
                .thenReturn(List.of())
                .thenReturn(List.of(adjustment(1, BottleAdjustmentType.LOST)));

        BottleAdjustmentRequest request = new BottleAdjustmentRequest(1, BottleAdjustmentType.LOST, "broke it");
        BottleBalanceResponse balance = bottleBalanceService.markAdjustment(42L, request);

        assertThat(balance.lost()).isEqualTo(1);
        assertThat(balance.outstanding()).isEqualTo(1);
    }

    @Test
    void getShopSummary_aggregatesAcrossAllCustomers() {
        when(orderRepository.findByStatusAndDeletedFalse(OrderStatus.DELIVERED))
                .thenReturn(List.of(deliveredOrder(2, 1, 1000), deliveredOrder(2, 2, 1000)));
        when(bottleAdjustmentRepository.findByDeletedFalse()).thenReturn(List.of());

        ShopBottleSummaryResponse summary = bottleBalanceService.getShopSummary();

        assertThat(summary.totalDelivered()).isEqualTo(4);
        assertThat(summary.totalReturned()).isEqualTo(3);
        assertThat(summary.totalOutstanding()).isEqualTo(1);
        assertThat(summary.estimatedOutstandingValue()).isEqualByComparingTo(BigDecimal.valueOf(500));
    }
}
