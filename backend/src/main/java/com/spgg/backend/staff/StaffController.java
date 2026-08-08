package com.spgg.backend.staff;

import com.spgg.backend.common.ApiResponse;
import com.spgg.backend.user.Role;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** FR-STF-001: Shop Owner manages Staff/Delivery Boy accounts for their shop. */
@RestController
@RequestMapping("/api/v1/shop/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @PostMapping
    @PreAuthorize("hasRole('SHOP_OWNER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StaffSummaryResponse> create(@Valid @RequestBody StaffCreateRequest request) {
        return ApiResponse.ok("Account created", staffService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SHOP_OWNER','STAFF')")
    public ApiResponse<List<StaffSummaryResponse>> list(@RequestParam(required = false) Role role) {
        return ApiResponse.ok(staffService.listForMyShop(role));
    }
}
