package com.spgg.backend.shop;

import com.spgg.backend.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Shop Owner's own shop configuration — Shop Settings is Owner-only per the SRS permission matrix (§7.1). */
@RestController
@RequestMapping("/api/v1/shop/profile")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SHOP_OWNER')")
public class ShopProfileController {

    private final ShopProfileService shopProfileService;

    @GetMapping
    public ApiResponse<ShopProfileResponse> getProfile() {
        return ApiResponse.ok(shopProfileService.getMyProfile());
    }

    @PutMapping
    public ApiResponse<ShopProfileResponse> updateProfile(@Valid @RequestBody ShopProfileUpdateRequest request) {
        return ApiResponse.ok("Profile updated", shopProfileService.updateProfile(request));
    }

    @PutMapping("/business-hours")
    public ApiResponse<ShopProfileResponse> updateBusinessHours(@Valid @RequestBody HoursUpdateRequest request) {
        return ApiResponse.ok("Business hours updated", shopProfileService.updateBusinessHours(request));
    }

    @PutMapping("/delivery-hours")
    public ApiResponse<ShopProfileResponse> updateDeliveryHours(@Valid @RequestBody HoursUpdateRequest request) {
        return ApiResponse.ok("Delivery hours updated", shopProfileService.updateDeliveryHours(request));
    }

    @PutMapping("/delivery-settings")
    public ApiResponse<ShopProfileResponse> updateDeliverySettings(
            @Valid @RequestBody DeliverySettingsRequest request) {
        return ApiResponse.ok("Delivery settings updated", shopProfileService.updateDeliverySettings(request));
    }
}
