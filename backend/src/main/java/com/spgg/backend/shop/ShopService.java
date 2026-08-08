package com.spgg.backend.shop;

import com.spgg.backend.common.BadRequestException;
import com.spgg.backend.common.ResourceNotFoundException;
import com.spgg.backend.user.Role;
import com.spgg.backend.user.User;
import com.spgg.backend.user.UserRepository;
import com.spgg.backend.user.UserStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ShopResponse register(ShopRegistrationRequest request) {
        if (userRepository.existsByEmailAndDeletedFalse(request.email())
                || shopRepository.existsByEmailAndDeletedFalse(request.email())) {
            throw new BadRequestException("An account with this email already exists");
        }

        User owner = new User();
        owner.setFullName(request.ownerName());
        owner.setEmail(request.email());
        owner.setPhone(request.phone());
        owner.setPasswordHash(passwordEncoder.encode(request.password()));
        owner.setRole(Role.SHOP_OWNER);
        owner.setStatus(UserStatus.PENDING);
        owner = userRepository.save(owner);

        Shop shop = new Shop();
        shop.setOwnerUserId(owner.getId());
        shop.setName(request.shopName());
        shop.setDescription(request.description());
        shop.setOwnerName(request.ownerName());
        shop.setEmail(request.email());
        shop.setPhone(request.phone());
        shop.setAddress(request.address());
        shop.setStatus(ShopStatus.PENDING_APPROVAL);
        shop = shopRepository.save(shop);

        owner.setTenantId(shop.getId());
        userRepository.save(owner);

        return ShopResponse.from(shop);
    }

    public List<ShopResponse> list(ShopStatus statusFilter) {
        List<Shop> shops = statusFilter != null
                ? shopRepository.findByStatusAndDeletedFalse(statusFilter)
                : shopRepository.findByDeletedFalse();
        return shops.stream().map(ShopResponse::from).toList();
    }

    @Transactional
    public ShopResponse approve(Long shopId) {
        Shop shop = requireShop(shopId);
        if (shop.getStatus() != ShopStatus.PENDING_APPROVAL) {
            throw new BadRequestException("Only shops pending approval can be approved");
        }
        shop.setStatus(ShopStatus.ACTIVE);
        shopRepository.save(shop);

        User owner = requireOwner(shop);
        owner.setStatus(UserStatus.ACTIVE);
        userRepository.save(owner);

        return ShopResponse.from(shop);
    }

    @Transactional
    public ShopResponse reject(Long shopId, String reason) {
        Shop shop = requireShop(shopId);
        if (shop.getStatus() != ShopStatus.PENDING_APPROVAL) {
            throw new BadRequestException("Only shops pending approval can be rejected");
        }
        shop.setStatus(ShopStatus.REJECTED);
        shop.setRejectionReason(reason);
        shopRepository.save(shop);

        User owner = requireOwner(shop);
        owner.setStatus(UserStatus.BLOCKED);
        userRepository.save(owner);

        return ShopResponse.from(shop);
    }

    @Transactional
    public ShopResponse suspend(Long shopId, String reason) {
        Shop shop = requireShop(shopId);
        if (shop.getStatus() != ShopStatus.ACTIVE) {
            throw new BadRequestException("Only active shops can be suspended");
        }
        shop.setStatus(ShopStatus.SUSPENDED);
        shop.setSuspensionReason(reason);
        shopRepository.save(shop);
        // Owner keeps ACTIVE status: suspension blocks customer-facing operations
        // (Phase 2), not the owner's own portal/app access — they still need to
        // be able to log in to resolve the suspension.
        return ShopResponse.from(shop);
    }

    @Transactional
    public ShopResponse reinstate(Long shopId) {
        Shop shop = requireShop(shopId);
        if (shop.getStatus() != ShopStatus.SUSPENDED) {
            throw new BadRequestException("Only suspended shops can be reinstated");
        }
        shop.setStatus(ShopStatus.ACTIVE);
        shop.setSuspensionReason(null);
        shopRepository.save(shop);
        return ShopResponse.from(shop);
    }

    private Shop requireShop(Long shopId) {
        return shopRepository.findByIdAndDeletedFalse(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found: " + shopId));
    }

    private User requireOwner(Shop shop) {
        return userRepository.findById(shop.getOwnerUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Owner user not found for shop: " + shop.getId()));
    }
}
