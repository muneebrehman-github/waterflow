package com.spgg.backend.customer;

import com.spgg.backend.bottle.BottleAdjustmentRequest;
import com.spgg.backend.bottle.BottleBalanceResponse;
import com.spgg.backend.bottle.BottleBalanceService;
import com.spgg.backend.common.ApiResponse;
import com.spgg.backend.ledger.LedgerService;
import com.spgg.backend.ledger.LedgerStatementResponse;
import com.spgg.backend.payment.PaymentRequest;
import com.spgg.backend.payment.PaymentResponse;
import com.spgg.backend.payment.PaymentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Shop Owner/Staff visibility into their own shop's customers (FR-CST-006/007/008). */
@RestController
@RequestMapping("/api/v1/shop/customers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SHOP_OWNER','STAFF')")
public class CustomerManagementController {

    private final CustomerService customerService;
    private final LedgerService ledgerService;
    private final PaymentService paymentService;
    private final BottleBalanceService bottleBalanceService;

    @GetMapping
    public ApiResponse<List<CustomerSummaryResponse>> list() {
        return ApiResponse.ok(customerService.listForMyShop());
    }

    @GetMapping("/{id}")
    public ApiResponse<CustomerSummaryResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(customerService.getForMyShop(id));
    }

    @PostMapping("/{id}/block")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public ApiResponse<CustomerSummaryResponse> block(@PathVariable Long id) {
        return ApiResponse.ok("Customer blocked", customerService.setBlocked(id, true));
    }

    @PostMapping("/{id}/unblock")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public ApiResponse<CustomerSummaryResponse> unblock(@PathVariable Long id) {
        return ApiResponse.ok("Customer unblocked", customerService.setBlocked(id, false));
    }

    @GetMapping("/{id}/ledger")
    public ApiResponse<LedgerStatementResponse> getLedger(@PathVariable Long id) {
        customerService.getForMyShop(id);
        return ApiResponse.ok(ledgerService.getStatement(id));
    }

    @GetMapping("/{id}/payments")
    public ApiResponse<List<PaymentResponse>> listPayments(@PathVariable Long id) {
        customerService.getForMyShop(id);
        return ApiResponse.ok(paymentService.listForCustomer(id));
    }

    @PostMapping("/{id}/payments")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PaymentResponse> recordPayment(@PathVariable Long id, @Valid @RequestBody PaymentRequest request) {
        customerService.getForMyShop(id);
        return ApiResponse.ok("Payment recorded", paymentService.record(id, request));
    }

    @GetMapping("/{id}/bottle-balance")
    public ApiResponse<BottleBalanceResponse> getBottleBalance(@PathVariable Long id) {
        customerService.getForMyShop(id);
        return ApiResponse.ok(bottleBalanceService.getBalance(id));
    }

    @PostMapping("/{id}/bottle-adjustments")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public ApiResponse<BottleBalanceResponse> markBottleAdjustment(
            @PathVariable Long id, @Valid @RequestBody BottleAdjustmentRequest request) {
        customerService.getForMyShop(id);
        return ApiResponse.ok("Bottles marked " + request.type(), bottleBalanceService.markAdjustment(id, request));
    }
}
