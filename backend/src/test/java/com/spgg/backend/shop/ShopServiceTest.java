package com.spgg.backend.shop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.spgg.backend.common.BadRequestException;
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
class ShopServiceTest {

    @Mock
    private ShopRepository shopRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private ShopService shopService;

    @BeforeEach
    void setUp() {
        shopService = new ShopService(shopRepository, userRepository, passwordEncoder);
    }

    private ShopRegistrationRequest sampleRequest() {
        return new ShopRegistrationRequest(
                "Clear Springs Water", "Ali Khan", "ali@clearsprings.test", "03001234567",
                "SecurePass1", "Purified water delivery", "Model Town, Lahore");
    }

    @Test
    void register_createsPendingOwnerAndPendingShop_andLinksOwnerTenantIdBackToTheShop() {
        when(userRepository.existsByEmailAndDeletedFalse(any())).thenReturn(false);
        when(shopRepository.existsByEmailAndDeletedFalse(any())).thenReturn(false);
        when(passwordEncoder.encode("SecurePass1")).thenReturn("hashed");

        User savedOwner = new User();
        savedOwner.setId(7L);
        when(userRepository.save(any(User.class))).thenReturn(savedOwner);

        Shop savedShop = new Shop();
        savedShop.setId(99L);
        when(shopRepository.save(any(Shop.class))).thenReturn(savedShop);

        ShopResponse response = shopService.register(sampleRequest());

        assertThat(response.id()).isEqualTo(99L);
        assertThat(response.status()).isEqualTo(ShopStatus.PENDING_APPROVAL);

        ArgumentCaptor<Shop> shopCaptor = ArgumentCaptor.forClass(Shop.class);
        verify(shopRepository).save(shopCaptor.capture());
        assertThat(shopCaptor.getValue().getOwnerUserId()).isEqualTo(7L);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, org.mockito.Mockito.times(2)).save(userCaptor.capture());
        assertThat(userCaptor.getAllValues().get(0).getStatus()).isEqualTo(UserStatus.PENDING);
        // Second save links the owner back to the now-created shop's id (tenant_id).
        assertThat(userCaptor.getAllValues().get(1).getTenantId()).isEqualTo(99L);
    }

    @Test
    void register_rejectsDuplicateEmail() {
        when(userRepository.existsByEmailAndDeletedFalse(any())).thenReturn(true);

        assertThatThrownBy(() -> shopService.register(sampleRequest()))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void approve_activatesShopAndOwner_whenPendingApproval() {
        Shop shop = new Shop();
        shop.setId(1L);
        shop.setOwnerUserId(7L);
        shop.setStatus(ShopStatus.PENDING_APPROVAL);
        when(shopRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(shop));

        User owner = new User();
        owner.setId(7L);
        owner.setStatus(UserStatus.PENDING);
        when(userRepository.findById(7L)).thenReturn(Optional.of(owner));

        ShopResponse response = shopService.approve(1L);

        assertThat(response.status()).isEqualTo(ShopStatus.ACTIVE);
        assertThat(owner.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void approve_rejectsAlreadyActiveShop() {
        Shop shop = new Shop();
        shop.setId(1L);
        shop.setStatus(ShopStatus.ACTIVE);
        when(shopRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(shop));

        assertThatThrownBy(() -> shopService.approve(1L)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void suspend_onlyAllowedFromActive() {
        Shop shop = new Shop();
        shop.setId(1L);
        shop.setStatus(ShopStatus.PENDING_APPROVAL);
        when(shopRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(shop));

        assertThatThrownBy(() -> shopService.suspend(1L, "policy violation"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void reinstate_onlyAllowedFromSuspended() {
        Shop shop = new Shop();
        shop.setId(1L);
        shop.setStatus(ShopStatus.ACTIVE);
        when(shopRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(shop));

        assertThatThrownBy(() -> shopService.reinstate(1L)).isInstanceOf(BadRequestException.class);
    }
}
