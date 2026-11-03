package com.spgg.studentbeans.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.spgg.studentbeans.config.StudentBeansProperties;
import com.spgg.studentbeans.dto.ApplyDiscountRequest;
import com.spgg.studentbeans.dto.VerificationCallbackRequest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StudentDiscountServiceTest {

    private StudentDiscountService service;

    @BeforeEach
    void setUp() {
        service = new StudentDiscountService(testProperties());
    }

    @Test
    void getConnectUrl_buildsLycaMobileUsUrl() {
        var response = service.getConnectUrl("us", null);

        assertThat(response.connectUrl())
                .isEqualTo("https://connect.studentbeans.com/v4/lyca-mobile/us/");
        assertThat(response.offerSlug()).isEqualTo("lyca-mobile");
    }

    @Test
    void verificationCallbackAndApplyDiscount_workTogether() {
        var verification = service.handleVerificationCallback(new VerificationCallbackRequest(
                "student-1",
                null,
                "token-abc",
                "lyca-mobile",
                "us",
                null,
                "verified",
                null));

        var applied = service.applyDiscount(new ApplyDiscountRequest(
                verification.verificationId(), null, "cart-123"));

        assertThat(applied.applied()).isTrue();
        assertThat(applied.discountCode()).isEqualTo("STUDENT-LYCA-10");
        assertThat(service.getCartDiscountCode("cart-123")).isEqualTo("STUDENT-LYCA-10");
    }

    private StudentBeansProperties testProperties() {
        return new StudentBeansProperties(
                "lyca-mobile",
                "us",
                "https://connect.studentbeans.com/v4",
                "STUDENT-LYCA-10",
                10,
                24,
                List.of("http://localhost:5500"),
                List.of("https://www.lycamobile.us"));
    }
}
