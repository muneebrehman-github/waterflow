package com.spgg.studentbeans.controller;

import com.spgg.studentbeans.common.ApiResponse;
import com.spgg.studentbeans.dto.ApplyDiscountRequest;
import com.spgg.studentbeans.dto.ApplyDiscountResponse;
import com.spgg.studentbeans.dto.ConnectUrlResponse;
import com.spgg.studentbeans.dto.VerificationCallbackRequest;
import com.spgg.studentbeans.dto.VerificationResponse;
import com.spgg.studentbeans.service.StudentDiscountService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student-discount")
public class StudentDiscountController {

    private final StudentDiscountService studentDiscountService;

    public StudentDiscountController(StudentDiscountService studentDiscountService) {
        this.studentDiscountService = studentDiscountService;
    }

    @GetMapping("/connect-url")
    public ApiResponse<ConnectUrlResponse> getConnectUrl(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String offerSlug) {
        return ApiResponse.ok(studentDiscountService.getConnectUrl(country, offerSlug));
    }

    @PostMapping("/verification-callback")
    public ApiResponse<VerificationResponse> verificationCallback(
            @Valid @RequestBody VerificationCallbackRequest request) {
        return ApiResponse.ok(
                "Verification recorded",
                studentDiscountService.handleVerificationCallback(request));
    }

    @PostMapping("/apply")
    public ApiResponse<ApplyDiscountResponse> applyDiscount(@Valid @RequestBody ApplyDiscountRequest request) {
        return ApiResponse.ok(studentDiscountService.applyDiscount(request));
    }

    @GetMapping("/verifications/{verificationId}")
    public ApiResponse<VerificationResponse> getVerification(@PathVariable String verificationId) {
        return ApiResponse.ok(studentDiscountService.getVerification(verificationId));
    }

    @GetMapping("/verifications")
    public ApiResponse<List<VerificationResponse>> listVerifications() {
        return ApiResponse.ok(studentDiscountService.listVerifications());
    }

    @GetMapping("/cart/{cartId}")
    public ApiResponse<Map<String, String>> getCartDiscount(@PathVariable String cartId) {
        var discountCode = studentDiscountService.getCartDiscountCode(cartId);
        return ApiResponse.ok(Map.of(
                "cartId", cartId,
                "discountCode", discountCode == null ? "" : discountCode));
    }
}
