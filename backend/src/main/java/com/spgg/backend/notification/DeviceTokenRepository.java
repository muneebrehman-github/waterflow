package com.spgg.backend.notification;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    Optional<DeviceToken> findByTokenAndDeletedFalse(String token);

    List<DeviceToken> findByUserIdAndDeletedFalse(Long userId);
}
