# Kora Mobile

Standalone Android client for Kora, built with Kotlin and Jetpack Compose. This
repository contains the mobile app; the Kora Engine is a separate project.

## What it supports

- Connect to `https://api.kora.mradiafrica.com` by default, or select another
  instance. A Site ID is required before sign-in.
- Load actor authorized published queries, projections, operations, requests and
  workflow definitions from Engine.
- Queue an offline action only when Engine publishes it as offline eligible.
  Online only actions require a connection and a completed Engine receipt before
  the app reports success.
- Review saved domain intents in **Offline & sync**. Uncertain operations retain
  their idempotency key for receipt lookup; never submit a second key to repeat
  an action whose outcome is unknown.
- Browse previously synchronized data with its freshness state. Writes through
  legacy DocType record screens and POS view actions remain unavailable until
  their modules publish supported queries and operations.

Initial sign-in, catalog refresh, workflow/request actions and server reporting
require a connection. Do not treat a timeout as proof that a payment or other
business operation did not happen; check its Engine receipt before retrying.

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
unsigned release builds in GitHub Actions. Use Conventional Commit subjects, for example
`fix(sync): preserve queued product references`. See [contributor guidance](CONTRIBUTING.md)
for the project structure and code patterns. Device smoke tests live in
[maestro](maestro/README.md).
