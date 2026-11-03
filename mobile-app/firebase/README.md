# Firebase setup for push notifications

1. Create a Firebase project at https://console.firebase.google.com
2. Add an Android app with package name `com.spgg.app`
3. Download `google-services.json` and replace `app/google-services.json`
4. Put the same project's service-account key on the backend (see `backend/src/main/resources/firebase/README.md`)

Push notifications require both the mobile `google-services.json` and the backend service account to reference the **same** Firebase project.

The committed `google-services.json` is a placeholder so the app builds out of the box. Replace it before testing FCM on a real device.
