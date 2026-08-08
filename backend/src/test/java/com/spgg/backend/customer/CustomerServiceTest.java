package com.spgg.backend.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.spgg.backend.common.BadRequestException;
import com.spgg.backend.common.TenantContext;
import com.spgg.backend.shop.Shop;
import com.spgg.backend.shop.ShopRepository;
import com.spgg.backend.shop.ShopStatus;
import com.spgg.backend.user.Role;
import com.spgg.backend.user.User;
import com.spgg.backend.user.UserRepository;
import com.spgg.backend.user.UserStatus;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ShopRepository shopRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(userRepository, shopRepository, passwordEncoder);
    }

    private Shop activeShop() {
        Shop shop = new Shop();
        shop.setId(1L);
        shop.setName("Clear Springs Water");
        shop.setStatus(ShopStatus.ACTIVE);
        return shop;
    }

    private CustomerRegistrationRequest request() {
        return new CustomerRegistrationRequest(1L, "Zara Ahmed", "03001112222", "zara@example.com", "SecurePass1");
    }

    @Test
    void register_createsActiveCustomerScopedToTheShop_noApprovalNeeded() {
        when(shopRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(activeShop()));
        when(userRepository.existsByEmailAndDeletedFalse("zara@example.com")).thenReturn(false);
        when(passwordEncoder.encode("SecurePass1")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(5L);
            return u;
        });

        CustomerProfileResponse response = customerService.register(request());

        assertThat(response.status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(response.shopId()).isEqualTo(1L);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        org.mockito.Mockito.verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(captor.getValue().getTenantId()).isEqualTo(1L);
        assertThat(captor.getValue().getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void register_rejectsWhenShopIsNotActive() {
        Shop pendingShop = activeShop();
        pendingShop.setStatus(ShopStatus.PENDING_APPROVAL);
        when(shopRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(pendingShop));

        assertThatThrownBy(() -> customerService.register(request())).isInstanceOf(BadRequestException.class);
    }

    @Test
    void register_rejectsDuplicateEmail() {
        when(shopRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(activeShop()));
        when(userRepository.existsByEmailAndDeletedFalse("zara@example.com")).thenReturn(true);

        assertThatThrownBy(() -> customerService.register(request())).isInstanceOf(BadRequestException.class);
    }

    @Test
    void setBlocked_flipsStatusForACustomerInMyShop() {
        User customer = new User();
        customer.setId(9L);
        customer.setTenantId(1L);
        customer.setRole(Role.CUSTOMER);
        customer.setStatus(UserStatus.ACTIVE);

        TenantContext.setTenantId(1L);
        try {
            when(userRepository.findByIdAndTenantIdAndDeletedFalse(9L, 1L)).thenReturn(Optional.of(customer));

            customerService.setBlocked(9L, true);

            assertThat(customer.getStatus()).isEqualTo(UserStatus.BLOCKED);
        } finally {
            TenantContext.clear();
        }
    }
}
