# Device smoke test

After signing in and downloading a workspace, run:

```sh
maestro --device <android-serial> test maestro/smoke_workspace.yaml
```

The smoke flow verifies basic navigation. Run the offline acceptance scenarios in
`docs/offline-implementation-status.md` before a release candidate. Local,
tenant-specific Maestro experiments are kept under the ignored `maestro/legacy/`
directory and are not part of the release source.
