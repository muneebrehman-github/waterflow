package com.spgg.backend.bottle;

import com.spgg.backend.common.BadRequestException;
import com.spgg.backend.common.TenantContext;
import com.spgg.backend.order.Order;
import com.spgg.backend.order.OrderRepository;
import com.spgg.backend.order.OrderStatus;
import com.spgg.backend.security.CurrentUser;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * FR-BOT-005/006/008: a customer's bottle balance is "bottles physically
 * delivered minus bottles returned minus bottles closed out as lost/damaged"
 * — derived from DELIVERED orders and {@link BottleAdjustment} rows rather
 * than tracked as its own running counter, since the source events (order
 * delivery, adjustment) are already the system of record.
 */
@Service
@RequiredArgsConstructor
public class BottleBalanceService {

    private final OrderRepository orderRepository;
    private final BottleAdjustmentRepository bottleAdjustmentRepository;

    public BottleBalanceResponse getBalance(Long customerId) {
        List<Order> delivered = orderRepository.findByCustomerIdAndStatusAndDeletedFalse(customerId, OrderStatus.DELIVERED);
        List<BottleAdjustment> adjustments = bottleAdjustmentRepository.findByCustomerIdAndDeletedFalse(customerId);
        return toBalance(customerId, delivered, adjustments);
    }

    public ShopBottleSummaryResponse getShopSummary() {
        List<Order> delivered = orderRepository.findByStatusAndDeletedFalse(OrderStatus.DELIVERED);
        List<BottleAdjustment> adjustments = bottleAdjustmentRepository.findByDeletedFalse();

        int totalDelivered = delivered.stream().mapToInt(Order::getTotalBottleQuantity).sum();
        int totalReturned = delivered.stream().mapToInt(o -> o.getEmptyBottlesCollected() == null ? 0 : o.getEmptyBottlesCollected()).sum();
        int totalLost = sumByType(adjustments, BottleAdjustmentType.LOST);
        int totalDamaged = sumByType(adjustments, BottleAdjustmentType.DAMAGED);
        int totalOutstanding = totalDelivered - totalReturned - totalLost - totalDamaged;

        BigDecimal totalDepositValue = delivered.stream()
                .filter(o -> o.getTotalBottleQuantity() > 0)
                .map(Order::getTotalDeposit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal averageDepositPerBottle = totalDelivered == 0
                ? BigDecimal.ZERO
                : totalDepositValue.divide(BigDecimal.valueOf(totalDelivered), 2, RoundingMode.HALF_UP);
        BigDecimal estimatedOutstandingValue = averageDepositPerBottle.multiply(BigDecimal.valueOf(Math.max(totalOutstanding, 0)));

        return new ShopBottleSummaryResponse(totalDelivered, totalReturned, totalLost, totalDamaged, totalOutstanding, estimatedOutstandingValue);
    }

    @Transactional
    public BottleBalanceResponse markAdjustment(Long customerId, BottleAdjustmentRequest request) {
        BottleBalanceResponse current = getBalance(customerId);
        if (request.quantity() > current.outstanding()) {
            throw new BadRequestException(
                    "Cannot mark " + request.quantity() + " bottles as " + request.type()
                            + " — only " + current.outstanding() + " are currently outstanding");
        }

        BottleAdjustment adjustment = new BottleAdjustment();
        adjustment.setTenantId(TenantContext.getTenantId());
        adjustment.setCustomerId(customerId);
        adjustment.setQuantity(request.quantity());
        adjustment.setType(request.type());
        adjustment.setReason(request.reason());
        adjustment.setCreatedByUserId(CurrentUser.id());
        bottleAdjustmentRepository.save(adjustment);

        return getBalance(customerId);
    }

    private BottleBalanceResponse toBalance(Long customerId, List<Order> delivered, List<BottleAdjustment> adjustments) {
        int deliveredCount = delivered.stream().mapToInt(Order::getTotalBottleQuantity).sum();
        int returnedCount = delivered.stream().mapToInt(o -> o.getEmptyBottlesCollected() == null ? 0 : o.getEmptyBottlesCollected()).sum();
        int lostCount = sumByType(adjustments, BottleAdjustmentType.LOST);
        int damagedCount = sumByType(adjustments, BottleAdjustmentType.DAMAGED);
        int outstanding = deliveredCount - returnedCount - lostCount - damagedCount;
        return new BottleBalanceResponse(customerId, deliveredCount, returnedCount, lostCount, damagedCount, outstanding);
    }

    private int sumByType(List<BottleAdjustment> adjustments, BottleAdjustmentType type) {
        return adjustments.stream().filter(a -> a.getType() == type).mapToInt(BottleAdjustment::getQuantity).sum();
    }
}
