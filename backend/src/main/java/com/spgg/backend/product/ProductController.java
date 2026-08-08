package com.spgg.backend.product;

import com.spgg.backend.common.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasRole('SHOP_OWNER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductResponse> create(@Valid @RequestBody ProductCreateRequest request) {
        return ApiResponse.ok("Product created", productService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SHOP_OWNER','STAFF')")
    public ApiResponse<List<ProductResponse>> listAll() {
        return ApiResponse.ok(productService.listAll());
    }

    /** Customer-facing catalog — available products only. */
    @GetMapping("/catalog")
    @PreAuthorize("hasAnyRole('SHOP_OWNER','STAFF','CUSTOMER')")
    public ApiResponse<List<ProductResponse>> listAvailable() {
        return ApiResponse.ok(productService.listAvailable());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public ApiResponse<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody ProductUpdateRequest request) {
        return ApiResponse.ok("Product updated", productService.update(id, request));
    }

    @PutMapping("/{id}/price")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public ApiResponse<ProductResponse> updatePrice(
            @PathVariable Long id, @Valid @RequestBody ProductPriceUpdateRequest request) {
        return ApiResponse.ok("Price updated", productService.updatePrice(id, request));
    }

    @PutMapping("/{id}/availability")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public ApiResponse<ProductResponse> toggleAvailability(@PathVariable Long id) {
        return ApiResponse.ok("Availability updated", productService.toggleAvailability(id));
    }

    @GetMapping("/{id}/price-history")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public ApiResponse<List<ProductPriceHistoryResponse>> priceHistory(@PathVariable Long id) {
        return ApiResponse.ok(productService.priceHistory(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public ApiResponse<Void> archive(@PathVariable Long id) {
        productService.archive(id);
        return ApiResponse.ok("Product archived", null);
    }
}
