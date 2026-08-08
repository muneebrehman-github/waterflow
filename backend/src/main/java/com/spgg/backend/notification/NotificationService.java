package com.spgg.backend.notification;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * FR-NOT-001/002/003: sends push notifications via FCM. Deliberately never
 * lets a notification failure propagate to the caller — placing an order or
 * recording a payment must succeed even if push delivery (or Firebase
 * configuration itself) is broken. Until a real Firebase project's
 * service-account key replaces the placeholder configured by default,
 * {@link #firebaseMessaging} stays null and every send is a logged no-op.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final DeviceTokenRepository deviceTokenRepository;

    @Value("${waterflow.fcm.service-account-path}")
    private String serviceAccountPath;

    private FirebaseMessaging firebaseMessaging;

    @PostConstruct
    void init() {
        try {
            InputStream credentialsStream = openStream(serviceAccountPath);
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                    .build();
            FirebaseApp app = FirebaseApp.getApps().isEmpty() ? FirebaseApp.initializeApp(options) : FirebaseApp.getInstance();
            firebaseMessaging = FirebaseMessaging.getInstance(app);
            log.info("FCM initialized from {}", serviceAccountPath);
        } catch (Exception e) {
            log.warn("FCM not configured (using placeholder or invalid credentials at '{}') — "
                    + "push notifications are disabled until a real Firebase service account is provided: {}",
                    serviceAccountPath, e.getMessage());
        }
    }

    @Transactional
    public void registerToken(Long userId, String token) {
        DeviceToken deviceToken = deviceTokenRepository.findByTokenAndDeletedFalse(token).orElseGet(DeviceToken::new);
        deviceToken.setUserId(userId);
        deviceToken.setToken(token);
        deviceTokenRepository.save(deviceToken);
    }

    public void notifyUser(Long userId, String title, String body, Map<String, String> data) {
        if (firebaseMessaging == null) {
            log.debug("Skipping push to user {} ('{}') — FCM not configured", userId, title);
            return;
        }
        for (DeviceToken deviceToken : deviceTokenRepository.findByUserIdAndDeletedFalse(userId)) {
            try {
                Message message = Message.builder()
                        .setToken(deviceToken.getToken())
                        .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                        .putAllData(data)
                        .build();
                firebaseMessaging.send(message);
            } catch (FirebaseMessagingException e) {
                log.warn("Push to user {} failed for token {}: {}", userId, deviceToken.getId(), e.getMessage());
            }
        }
    }

    private InputStream openStream(String path) throws IOException {
        if (path.startsWith("classpath:")) {
            String resourcePath = path.substring("classpath:".length());
            InputStream stream = getClass().getClassLoader().getResourceAsStream(resourcePath);
            if (stream == null) {
                throw new IOException("Classpath resource not found: " + resourcePath);
            }
            return stream;
        }
        return new FileInputStream(path);
    }
}
