package com.spgg.backend.superadmin;

import com.spgg.backend.common.ApiResponse;
import com.spgg.backend.shop.ReasonRequest;
import com.spgg.backend.shop.ShopResponse;
import com.spgg.backend.shop.ShopService;
import com.spgg.backend.shop.ShopStatus;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Machine API for FTS Control Center (header {@code X-FTS-CC-Key}). */
@RestController
@RequestMapping("/api/v1/internal")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class InternalControlCenterController {

    private final ShopService shopService;

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("product", "waterflow");
        out.put("status", "ok");
        return out;
    }

    @GetMapping("/shops")
    public ApiResponse<List<ShopResponse>> list(@RequestParam(required = false) ShopStatus status) {
        return ApiResponse.ok(shopService.list(status));
    }

    @PostMapping("/shops/{id}/approve")
    public ApiResponse<ShopResponse> approve(@PathVariable Long id) {
        return ApiResponse.ok("Shop approved", shopService.approve(id));
    }

    @PostMapping("/shops/{id}/reject")
    public ApiResponse<ShopResponse> reject(@PathVariable Long id, @Valid @RequestBody ReasonRequest request) {
        return ApiResponse.ok("Shop rejected", shopService.reject(id, request.reason()));
    }

    @PostMapping("/shops/{id}/suspend")
    public ApiResponse<ShopResponse> suspend(@PathVariable Long id, @Valid @RequestBody ReasonRequest request) {
        return ApiResponse.ok("Shop suspended", shopService.suspend(id, request.reason()));
    }

    @PostMapping("/shops/{id}/reinstate")
    public ApiResponse<ShopResponse> reinstate(@PathVariable Long id) {
        return ApiResponse.ok("Shop reinstated", shopService.reinstate(id));
    }
}
