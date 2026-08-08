package com.spgg.backend.staff;

import com.spgg.backend.common.BadRequestException;
import com.spgg.backend.common.TenantContext;
import com.spgg.backend.user.Role;
import com.spgg.backend.user.User;
import com.spgg.backend.user.UserRepository;
import com.spgg.backend.user.UserStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * FR-STF-001: Shop Owner creates Staff/Delivery Boy accounts for their shop.
 * No email/SMS invite flow exists yet, so this creates the account directly
 * with a password the owner sets and shares with the team member out-of-band
 * — the same direct-creation pattern used for Customer self-registration.
 */
@Service
@RequiredArgsConstructor
public class StaffService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public StaffSummaryResponse create(StaffCreateRequest request) {
        if (request.role() != Role.STAFF && request.role() != Role.DELIVERY_BOY) {
            throw new BadRequestException("Role must be STAFF or DELIVERY_BOY");
        }
        if (userRepository.existsByEmailAndDeletedFalse(request.email())) {
            throw new BadRequestException("An account with this email already exists");
        }

        User user = new User();
        user.setTenantId(TenantContext.getTenantId());
        user.setFullName(request.fullName());
        user.setPhone(request.phone());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setStatus(UserStatus.ACTIVE);
        user = userRepository.save(user);

        return StaffSummaryResponse.from(user);
    }

    public List<StaffSummaryResponse> listForMyShop(Role roleFilter) {
        Long tenantId = TenantContext.getTenantId();
        List<User> results = roleFilter != null
                ? userRepository.findByTenantIdAndRoleAndDeletedFalse(tenantId, roleFilter)
                : concat(userRepository.findByTenantIdAndRoleAndDeletedFalse(tenantId, Role.STAFF),
                        userRepository.findByTenantIdAndRoleAndDeletedFalse(tenantId, Role.DELIVERY_BOY));
        return results.stream().map(StaffSummaryResponse::from).toList();
    }

    private List<User> concat(List<User> a, List<User> b) {
        return java.util.stream.Stream.concat(a.stream(), b.stream()).toList();
    }
}
