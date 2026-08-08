package com.spgg.backend.user;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAndDeletedFalse(String email);

    boolean existsByEmailAndDeletedFalse(String email);

    /** Explicit tenant scoping (User does not extend TenantAwareEntity — see its javadoc). */
    List<User> findByTenantIdAndRoleAndDeletedFalse(Long tenantId, Role role);

    Optional<User> findByIdAndTenantIdAndDeletedFalse(Long id, Long tenantId);
}
