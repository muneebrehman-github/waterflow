package com.spgg.backend.notification;

import com.spgg.backend.common.ApiResponse;
import com.spgg.backend.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** FR-NOT-007: any authenticated role registers/refreshes its FCM device token. */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/device-token")
    public ApiResponse<Void> registerDeviceToken(@Valid @RequestBody RegisterDeviceTokenRequest request) {
        notificationService.registerToken(CurrentUser.id(), request.token());
        return ApiResponse.ok("Device token registered", null);
    }
}
