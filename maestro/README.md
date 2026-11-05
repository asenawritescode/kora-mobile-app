# Device smoke test

After signing in and downloading a workspace, run:

```sh
maestro --device <android-serial> test maestro/smoke_workspace.yaml
```

The smoke flow verifies basic navigation. Run the offline acceptance scenarios in
`docs/offline-implementation-status.md` before a release candidate. Local,
tenant-specific Maestro experiments are kept under the ignored `maestro/legacy/`
directory and are not part of the release source.

## Workflow and payment recovery acceptance

The authenticated workspace smoke flow does not prove payment safety or process
recovery. Before marking Android M4 accepted, run these cases against fresh
internal and external module configurations with a test payment provider:

1. Start a workflow, open history, and verify the server revision and current
   step after each transition. Cancel with a reason and verify the terminal state.
2. Use a provider action that can be held pending. Kill the app after submission,
   relaunch, and recover the same Engine execution by its original idempotency
   key. The UI must not report success until an authoritative receipt says
   `completed`.
3. Drop the response after provider acceptance. Verify `outcome_unknown` is
   visible, reconcile only with an authorized actor, expected revision, evidence
   reference and reason, then verify history records the reconciliation.
4. Deliver the same saved batch twice and rotate or revoke the actor credential
   while it is pending. Verify no second charge and no cross actor or entity
   receipt visibility.
5. Switch between two entities and two users. Verify cached rows, pending
   receipts, and workflow history remain scoped, and stale snapshots are labeled.

These cases require producer and provider fixtures that are not currently in this
repository. Passing the navigation smoke flow alone must not be reported as M4
acceptance.
