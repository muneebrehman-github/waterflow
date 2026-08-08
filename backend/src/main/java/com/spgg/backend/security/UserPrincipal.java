package com.spgg.backend.security;

import com.spgg.backend.user.Role;
import com.spgg.backend.user.User;
import com.spgg.backend.user.UserStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final Role role;
    private final Long tenantId;
    private final String fullName;
    private final boolean enabled;

    public UserPrincipal(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.role = user.getRole();
        this.tenantId = user.getTenantId();
        this.fullName = user.getFullName();
        this.enabled = user.getStatus() == UserStatus.ACTIVE;
    }

    public Long getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public String getFullName() {
        return fullName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
