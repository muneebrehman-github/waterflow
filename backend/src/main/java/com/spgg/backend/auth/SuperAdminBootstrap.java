package com.spgg.backend.auth;

import com.spgg.backend.user.Role;
import com.spgg.backend.user.User;
import com.spgg.backend.user.UserRepository;
import com.spgg.backend.user.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates a default Super Admin on first boot if none exists yet — there's no
 * registration endpoint for this role (SUPER_ADMIN is platform-scoped and
 * provisioned out-of-band, not self-registered like Shop Owners).
 * Credentials come from {@code waterflow.bootstrap.*} — override them via
 * env vars for anything beyond local dev.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SuperAdminBootstrap implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${waterflow.bootstrap.super-admin-email}")
    private String superAdminEmail;

    @Value("${waterflow.bootstrap.super-admin-password}")
    private String superAdminPassword;

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmailAndDeletedFalse(superAdminEmail)) {
            return;
        }

        User superAdmin = new User();
        superAdmin.setFullName("Super Admin");
        superAdmin.setEmail(superAdminEmail);
        superAdmin.setPasswordHash(passwordEncoder.encode(superAdminPassword));
        superAdmin.setRole(Role.SUPER_ADMIN);
        superAdmin.setStatus(UserStatus.ACTIVE);
        superAdmin.setTenantId(null);
        userRepository.save(superAdmin);

        log.info("Bootstrapped default Super Admin account: {}", superAdminEmail);
    }
}
