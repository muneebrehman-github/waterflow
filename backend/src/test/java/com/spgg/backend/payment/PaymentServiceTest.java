package com.spgg.backend.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.spgg.backend.common.BadRequestException;
import com.spgg.backend.common.TenantContext;
import com.spgg.backend.ledger.LedgerEntryType;
import com.spgg.backend.ledger.LedgerService;
import com.spgg.backend.notification.NotificationService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private LedgerService ledgerService;
    @Mock
    private NotificationService notificationService;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, ledgerService, notificationService);
        TenantContext.setTenantId(7L);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(99L, null, List.of()));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    private Payment paymentWithId(long id) {
        Payment payment = new Payment();
        payment.setId(id);
        payment.setTenantId(7L);
        payment.setCustomerId(42L);
        payment.setAmount(BigDecimal.valueOf(500));
        payment.setMethod(PaymentMethod.CASH);
        payment.setReceiptNumber("PMT-" + id);
        payment.setRecordedByUserId(99L);
        return payment;
    }

    @Test
    void record_savesPaymentAndCreatesCreditLedgerEntry() {
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            if (p.getId() == null) {
                p.setId(1L);
            }
            return p;
        });

        PaymentRequest request = new PaymentRequest(BigDecimal.valueOf(500), PaymentMethod.CASH, "receipt-123");
        PaymentResponse response = paymentService.record(42L, request);

        assertThat(response.receiptNumber()).isEqualTo("PMT-1");
        assertThat(response.status()).isEqualTo(PaymentStatus.ACTIVE);
        verify(paymentRepository, times(2)).save(any(Payment.class));
        verify(ledgerService).recordEntry(
                42L, LedgerEntryType.CREDIT, BigDecimal.valueOf(-500), "PAYMENT", 1L, "Payment received (CASH) - PMT-1");
    }

    @Test
    void voidPayment_marksVoidedAndCreatesOffsettingDebit() {
        when(paymentRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(paymentWithId(1L)));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<BigDecimal> amountCaptor = ArgumentCaptor.forClass(BigDecimal.class);

        PaymentResponse response = paymentService.voidPayment(1L, "Entered by mistake");

        assertThat(response.status()).isEqualTo(PaymentStatus.VOIDED);
        assertThat(response.voidReason()).isEqualTo("Entered by mistake");
        verify(ledgerService).recordEntry(eq(42L), eq(LedgerEntryType.DEBIT), amountCaptor.capture(), eq("PAYMENT"), eq(1L), any());
        assertThat(amountCaptor.getValue()).isEqualByComparingTo(BigDecimal.valueOf(500));
    }

    @Test
    void voidPayment_whenAlreadyVoided_throws() {
        Payment payment = paymentWithId(1L);
        payment.setStatus(PaymentStatus.VOIDED);
        when(paymentRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.voidPayment(1L, "again"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void listForCustomer_returnsAllPaymentsForCustomer() {
        when(paymentRepository.findByCustomerIdAndDeletedFalseOrderByCreatedAtDesc(42L))
                .thenReturn(List.of(paymentWithId(1L), paymentWithId(2L)));

        List<PaymentResponse> payments = paymentService.listForCustomer(42L);

        assertThat(payments).hasSize(2);
    }
}
