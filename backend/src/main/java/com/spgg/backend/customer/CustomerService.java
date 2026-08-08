package com.spgg.backend.customer;

import com.spgg.backend.common.BadRequestException;
import com.spgg.backend.common.ForbiddenException;
import com.spgg.backend.common.ResourceNotFoundException;
import com.spgg.backend.common.TenantContext;
import com.spgg.backend.security.CurrentUser;
import com.spgg.backend.shop.Shop;
import com.spgg.backend.shop.ShopRepository;
import com.spgg.backend.shop.ShopStatus;
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
public class CustomerService {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public CustomerProfileResponse register(CustomerRegistrationRequest request) {
        Shop shop = shopRepository.findByIdAndDeletedFalse(request.shopId())
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found: " + request.shopId()));
        if (shop.getStatus() != ShopStatus.ACTIVE) {
            throw new BadRequestException("This shop isn't accepting new customers right now");
        }
        if (userRepository.existsByEmailAndDeletedFalse(request.email())) {
            throw new BadRequestException("An account with this email already exists");
        }

        User customer = new User();
        customer.setTenantId(shop.getId());
        customer.setFullName(request.fullName());
        customer.setPhone(request.phone());
        customer.setEmail(request.email());
        customer.setPasswordHash(passwordEncoder.encode(request.password()));
        customer.setRole(Role.CUSTOMER);
        customer.setStatus(UserStatus.ACTIVE);
        customer = userRepository.save(customer);

        return toProfileResponse(customer, shop);
    }

    public CustomerProfileResponse getMyProfile() {
        User customer = userRepository.findById(CurrentUser.id())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        Shop shop = shopRepository.findByIdAndDeletedFalse(customer.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found: " + customer.getTenantId()));
        return toProfileResponse(customer, shop);
    }

    /** Shop Owner/Staff view of their own shop's customers. */
    public List<CustomerSummaryResponse> listForMyShop() {
        Long tenantId = TenantContext.getTenantId();
        return userRepository.findByTenantIdAndRoleAndDeletedFalse(tenantId, Role.CUSTOMER).stream()
                .map(CustomerSummaryResponse::from)
                .toList();
    }

    public CustomerSummaryResponse getForMyShop(Long customerId) {
        User customer = requireCustomerInMyShop(customerId);
        return CustomerSummaryResponse.from(customer);
    }

    @Transactional
    public CustomerSummaryResponse setBlocked(Long customerId, boolean blocked) {
        User customer = requireCustomerInMyShop(customerId);
        customer.setStatus(blocked ? UserStatus.BLOCKED : UserStatus.ACTIVE);
        userRepository.save(customer);
        return CustomerSummaryResponse.from(customer);
    }

    private User requireCustomerInMyShop(Long customerId) {
        Long tenantId = TenantContext.getTenantId();
        User customer = userRepository.findByIdAndTenantIdAndDeletedFalse(customerId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));
        if (customer.getRole() != Role.CUSTOMER) {
            throw new ForbiddenException("Not a customer account");
        }
        return customer;
    }

    private CustomerProfileResponse toProfileResponse(User customer, Shop shop) {
        return new CustomerProfileResponse(
                customer.getId(),
                customer.getFullName(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getStatus(),
                shop.getId(),
                shop.getName());
    }
}
