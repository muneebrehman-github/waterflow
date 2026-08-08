package com.spgg.backend.customer;

import com.spgg.backend.bottle.BottleBalanceResponse;
import com.spgg.backend.bottle.BottleBalanceService;
import com.spgg.backend.common.ApiResponse;
import com.spgg.backend.ledger.LedgerService;
import com.spgg.backend.ledger.LedgerStatementResponse;
import com.spgg.backend.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final LedgerService ledgerService;
    private final BottleBalanceService bottleBalanceService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CustomerProfileResponse> register(@Valid @RequestBody CustomerRegistrationRequest request) {
        return ApiResponse.ok("Registration successful", customerService.register(request));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<CustomerProfileResponse> getMyProfile() {
        return ApiResponse.ok(customerService.getMyProfile());
    }

    @GetMapping("/me/ledger")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<LedgerStatementResponse> getMyLedger() {
        return ApiResponse.ok(ledgerService.getStatement(CurrentUser.id()));
    }

    @GetMapping("/me/bottle-balance")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<BottleBalanceResponse> getMyBottleBalance() {
        return ApiResponse.ok(bottleBalanceService.getBalance(CurrentUser.id()));
    }
}
