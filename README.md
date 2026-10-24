# Kora Mobile

Standalone Android client for Kora, built with Kotlin and Jetpack Compose. This
repository contains the mobile app; the Kora Engine is a separate project.

## What it supports

- Connect to `https://api.kora.mradiafrica.com` by default, or select another
  instance. A Site ID is required before sign-in.
- Download an authorized workspace for offline access.
- Browse cached records and create or edit supported records while disconnected.
- Use the configured retail POS and Stock Adjustment action offline. Submitted
  changes are saved on the device, then sent in order when the server is reachable.
- Review pending, rejected, and conflicting changes in **Offline & sync**. The
  server remains authoritative for permissions, scripts, validation, and final
  acceptance.

Initial sign-in and workspace download require a connection. An unsaved form is
discarded when you leave it; **Save** commits a durable local change even while
offline. Server reporting requires reachable server data. See
[offline implementation status](docs/offline-implementation-status.md) for the
tested paths and remaining protocol limits.

## Build and check

Use JDK 17 and Android SDK platform 35:

```sh
./gradlew :app:lintDebug :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.
Override the default server URL for a local build with
`-PkoraDefaultServerUrl=https://your-kora-host.example`.

For a USB-connected development phone using a local Engine on port 8000:

```sh
adb reverse tcp:8000 tcp:8000
```

Select `http://localhost:8000` in the app and choose the site before signing in.
USB reverse is a development connection; reconnecting to ordinary Wi-Fi alone
does not make that laptop's localhost reachable.

## Project workflow

Pull requests and pushes to `main` run Android lint, JVM tests, and debug and
release builds in GitHub Actions. Use Conventional Commit subjects, for example
`fix(sync): preserve queued product references`. See [releasing](docs/RELEASING.md)
for versioning, signing, and release checks. Device smoke tests live in
[maestro](maestro/README.md).
