# Releasing Kora Mobile

The repository currently builds a development preview. Do not tag or distribute
a release until a signed artifact and the target server compatibility are verified.

## Release checklist

1. Review `docs/offline-implementation-status.md` and update the supported
   offline paths and known limits. Verify the target Kora Engine exposes the
   required sync and view action contracts.
2. Update `versionName` and increment `versionCode` in `app/build.gradle.kts`.
   Move user-visible changes from **Unreleased** into a dated version section in
   `CHANGELOG.md`.
3. Run `./gradlew :app:lintDebug :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease` and
   wait for the same GitHub Actions check to pass on the release commit.
4. On a test device, run `maestro/smoke_workspace.yaml`, then verify offline
   Product creation, POS sale, app restart, reconnection, and zero pending changes.
   On a clean install, verify the default API server and required Site ID.
   Check a rejected server validation separately; it must remain reviewable.
5. Build an Android App Bundle or release APK with a dedicated release signing
   key. Keep the keystore and passwords outside Git; use Android Studio's
   **Generate Signed Bundle / APK** flow or a secure CI signing setup. Verify the
   package ID, version, signature, and installed upgrade before distribution.
6. Tag the verified commit `v<versionName>` and publish a GitHub release with
   the signed artifact, checksum, change notes, and known limits. Do not attach
   a debug APK as a production release.

Conventional Commit subjects are required for source changes:
`feat(scope): summary`, `fix(scope): summary`, `docs(scope): summary`, or
`chore(scope): summary`. Keep the subject imperative and under about 72
characters. Use a body when the reason or migration impact is not obvious.

Do not store tenant credentials, signing files, local database copies, or
Maestro recordings in the repository.
