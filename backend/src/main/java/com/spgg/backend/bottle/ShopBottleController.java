package com.spgg.backend.bottle;

import com.spgg.backend.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** FR-BOT-008: shop-wide total unreturned bottle count and estimated value. */
@RestController
@RequestMapping("/api/v1/shop/bottles")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SHOP_OWNER','STAFF')")
public class ShopBottleController {

    private final BottleBalanceService bottleBalanceService;

    @GetMapping("/summary")
    public ApiResponse<ShopBottleSummaryResponse> summary() {
        return ApiResponse.ok(bottleBalanceService.getShopSummary());
    }
}
