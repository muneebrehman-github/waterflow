package com.spgg.backend.payment;

import com.spgg.backend.common.BadRequestException;
import com.spgg.backend.common.ResourceNotFoundException;
import com.spgg.backend.common.TenantContext;
import com.spgg.backend.ledger.LedgerEntryType;
import com.spgg.backend.ledger.LedgerService;
import com.spgg.backend.notification.NotificationService;
import com.spgg.backend.security.CurrentUser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * FR-PAY-001/002: recording a manual payment is a general credit against
 * the customer's ledger balance — not linked to a specific order.
 * FR-PAY-005/BR-009: voiding never edits/deletes the original row; it posts
 * an equal offsetting debit so the net ledger effect is zero.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final LedgerService ledgerService;
    private final NotificationService notificationService;

    @Transactional
    public PaymentResponse record(Long customerId, PaymentRequest request) {
        Payment payment = new Payment();
        payment.setTenantId(TenantContext.getTenantId());
        payment.setCustomerId(customerId);
        payment.setAmount(request.amount());
        payment.setMethod(request.method());
        payment.setReference(request.reference());
        payment.setRecordedByUserId(CurrentUser.id());
        payment.setReceiptNumber("PENDING");
        payment = paymentRepository.save(payment);

        payment.setReceiptNumber("PMT-" + payment.getId());
        payment = paymentRepository.save(payment);

        ledgerService.recordEntry(customerId, LedgerEntryType.CREDIT, request.amount().negate(), "PAYMENT",
                payment.getId(), "Payment received (" + request.method() + ") - " + payment.getReceiptNumber());

        notificationService.notifyUser(customerId, "Payment Received",
                "We received your payment of " + request.amount() + " (" + payment.getReceiptNumber() + ")",
                java.util.Map.of("paymentId", String.valueOf(payment.getId())));

        return PaymentResponse.from(payment);
    }

    @Transactional
    public PaymentResponse voidPayment(Long paymentId, String reason) {
        Payment payment = paymentRepository.findByIdAndDeletedFalse(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));
        if (payment.getStatus() != PaymentStatus.ACTIVE) {
            throw new BadRequestException("This payment has already been voided");
        }
        payment.setStatus(PaymentStatus.VOIDED);
        payment.setVoidReason(reason);
        paymentRepository.save(payment);

        ledgerService.recordEntry(payment.getCustomerId(), LedgerEntryType.DEBIT, payment.getAmount(), "PAYMENT",
                payment.getId(), "Voided payment " + payment.getReceiptNumber() + " - " + reason);

        return PaymentResponse.from(payment);
    }

    public List<PaymentResponse> listForCustomer(Long customerId) {
        return paymentRepository.findByCustomerIdAndDeletedFalseOrderByCreatedAtDesc(customerId).stream()
                .map(PaymentResponse::from)
                .toList();
    }
}
