Put a real Firebase service-account key here as `service-account.json` to enable push notifications.

Firebase console → Project Settings → Service Accounts → Generate new private key.

This directory is gitignored (except this file) — the key is a secret and must never be committed.
Until it's present, `NotificationService` logs a startup warning and every send is a no-op; it never
blocks the underlying business action (placing an order, recording a payment, etc.).

Alternatively, set `FCM_SERVICE_ACCOUNT_PATH` to an absolute filesystem path outside the repo.
