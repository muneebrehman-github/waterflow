package com.spgg.backend.customer;

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
@RequestMapping("/api/v1/customers/me/addresses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerAddressController {

    private final CustomerAddressService addressService;

    @GetMapping
    public ApiResponse<List<AddressResponse>> list() {
        return ApiResponse.ok(addressService.list());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AddressResponse> create(@Valid @RequestBody AddressRequest request) {
        return ApiResponse.ok("Address added", addressService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<AddressResponse> update(@PathVariable Long id, @Valid @RequestBody AddressRequest request) {
        return ApiResponse.ok("Address updated", addressService.update(id, request));
    }

    @PutMapping("/{id}/default")
    public ApiResponse<AddressResponse> setDefault(@PathVariable Long id) {
        return ApiResponse.ok("Default address updated", addressService.setDefault(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        addressService.delete(id);
        return ApiResponse.ok("Address removed", null);
    }
}
