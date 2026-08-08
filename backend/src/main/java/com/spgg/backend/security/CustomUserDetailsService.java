package com.spgg.backend.security;

import com.spgg.backend.user.User;
import com.spgg.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService {

    private final UserRepository userRepository;

    /**
     * Deliberately unfiltered by tenant: at login time we don't yet know
     * which tenant the caller belongs to, so email must be looked up
     * across all shops.
     */
    public UserPrincipal loadByEmail(String email) {
        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new UsernameNotFoundException("No account for email: " + email));
        return new UserPrincipal(user);
    }
}
