package com.spgg.backend.payment;

import com.spgg.backend.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** FR-PAY-005: voiding a payment is Owner-only (Staff cannot reverse money already recorded). */
@RestController
@RequestMapping("/api/v1/shop/payments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SHOP_OWNER')")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{id}/void")
    public ApiResponse<PaymentResponse> voidPayment(@PathVariable Long id, @Valid @RequestBody VoidPaymentRequest request) {
        return ApiResponse.ok("Payment voided", paymentService.voidPayment(id, request.reason()));
    }
}
