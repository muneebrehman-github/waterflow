package com.spgg.backend.notification;

import com.spgg.backend.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** FR-NOT-007: not tenant-scoped — mirrors {@code User}, a device token belongs to an account. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "device_tokens")
public class DeviceToken extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token", nullable = false, unique = true)
    private String token;
}
