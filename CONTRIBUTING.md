# Contributor guide

This guide describes the Android app as it exists today and conventions for
changing it. The app is a standalone Kotlin and Jetpack Compose client; the Kora
Engine is a separate project.

## Project map

```text
app/src/main/
├── AndroidManifest.xml
├── java/com/kora/mobile/
│   ├── KoraApplication.kt           # App-scoped dependencies and workspace
│   ├── MainActivity.kt              # Compose entry point and current screens
│   ├── OutboxSyncWorker.kt          # WorkManager background sync
│   ├── app/AppViewModel.kt          # App state and user actions
│   ├── core/
│   │   ├── config/InstanceStore.kt  # Server and site configuration
│   │   ├── database/KoraDatabase.kt # Room entities, DAO, database
│   │   ├── network/                 # HTTP client and session cookies
│   │   └── security/SecretVault.kt  # Local encryption
│   ├── data/
│   │   ├── WorkspaceRepository.kt   # Workspace and cached resource operations
│   │   └── offline/                 # Contracts, local outbox, REST adapter,
│   │                                # payload serialization
│   ├── domain/model/Models.kt       # Shared app and API models
│   └── ui/theme/KoraTheme.kt        # Colors and Compose theme tokens
├── res/                             # Android resources and backup rules
├── debug/                           # Debug-only Android manifest
└── test/                             # JVM and Robolectric tests
```

`feature/` and several `data/` subfolders currently have no Kotlin files; they
are reserved package locations, not implemented modules. Most screens and
reusable Compose components currently live in `MainActivity.kt`. App behavior
and state live in `AppViewModel.kt`. Prefer small, incremental extraction into
focused feature files instead of making either file larger. Keep a component
close to the screen or feature that owns it; move shared presentation
components to `ui/` when they have multiple callers.

Other project areas:

- `.github/workflows/android.yml` runs lint, unit tests, and debug/release builds.
- `maestro/` contains device smoke flows and usage notes. Legacy flows are
  ignored and are not part of the repository.

## Design patterns in use

### Compose UI with state hoisted to the ViewModel

`MainActivity` observes `AppState` from `AppViewModel` and passes state and
callbacks into composables. Keep UI components declarative: render the state
they receive and call an event callback for user actions. Put network, database,
and workflow operations in the ViewModel or repository, not in a composable.

Prefer immutable values in `AppState` and update them through `MutableStateFlow`
operations. Keep transient state in `remember` only when it does not need to
survive navigation or process recreation and does not need to be shared.

### Repository and data source boundaries

`OfflineRecordRepository` implements `RecordRepository` and `SyncCoordinator`.
`RestRecordDataSource` implements the remote transport contract. The repository
coordinates local Room storage and remote calls; the data source maps those
calls to the existing Kora API. Preserve this boundary so UI code does not
become coupled to HTTP or SQL.

`WorkspaceRepository` owns workspace operations, cached navigation and schemas,
record refresh, and offline preparation. `KoraApplication` currently constructs
app-wide dependencies directly; there is no DI framework. Follow this existing
setup for app-scoped dependencies unless a focused change establishes a new
convention.

### Offline-first reads and queued writes

Room is the durable local store. Record screens observe local data; a save
creates a durable mutation in the outbox, then sync sends queued work when the
network and active workspace allow it. `OutboxSyncWorker` provides deferred
background retry. Keep mutations scoped to their instance, site, and user;
preserve ordering, idempotency keys, and local-to-server ID resolution.

The server remains authoritative for permissions, scripts, validation, and
acceptance. A rejected mutation must remain visible and recoverable. Do not add
Kora-specific business rules to the generic mobile sync layer. Update the
offline status doc when changing the contract or supported offline paths.

### Workspace scope and configuration

Use `Scope` to associate local and remote data with the server instance,
required Site ID, and user. The default server is
`https://api.kora.mradiafrica.com`; another server can be selected before
sign-in, and Site ID is mandatory. Avoid unscoped database queries or sharing
session state across server origins.

### Shared styling

Use `KoraTheme` and `KoraTokens` for colors and visual tokens. Support tenant
branding and light/dark theme. Avoid introducing isolated colors, typography,
or spacing values when a theme token or shared component fits.

## Adding a feature

1. Find the owning screen, ViewModel action, repository, and existing tests
   before editing. Keep the event flow explicit: user action → ViewModel →
   repository → local or remote data source → state update.
2. Put reusable Compose UI in focused files as it is extracted from
   `MainActivity.kt`. Keep transport and persistence details out of UI code.
3. For offline writes, persist first, then enqueue sync. Cover retry, duplicate
   delivery, server rejection, and local/server identity resolution where they
apply. Network availability alone does not mean data has synced.
4. Keep server validation errors understandable and tied to the affected
   field or record. Do not silently discard rejected or conflicting work.
5. Add or update a focused unit test for behavior changes. Use the existing
   Robolectric and coroutine test dependencies; tests should not depend on a
   live tenant or credentials.
6. Update this guide and smoke flows when user-visible behavior or supported
   workflows change.

There is not yet a separate use-case/interactor layer, Navigation Compose
graph, or multi-module Gradle structure. Avoid adding those layers for one small
change; introduce them incrementally when a feature boundary needs them.

## Local checks and commits

Use JDK 17 and Android SDK platform 35. On memory-constrained machines, limit
Gradle parallelism and heap:

```sh
./gradlew --max-workers=1 -Dorg.gradle.jvmargs=-Xmx1024m \
  :app:lintDebug :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease
```

Use Conventional Commit subjects, for example:

```text
feat(records): add offline child row editing
fix(sync): retain rejected mutations for review
docs(contributors): explain app structure and patterns
```

Keep subjects concise and descriptive. CI release APKs are unsigned and are not
production releases. A production release needs a protected signing process.
Never commit tenant credentials, local database files, signing keys, or
generated APK/AAB artifacts.
