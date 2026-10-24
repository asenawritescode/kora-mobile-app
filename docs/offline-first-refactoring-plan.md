# Offline-first refactoring plan and contracts

Status: proposed implementation specification, 2026-10-23. This document supersedes the offline sections of PLAN.md. It defines intended signatures; these APIs are not implemented by this document.

## 1. Required behavior

After a successful workspace download, the app opens and supports enabled record creation/editing, child rows, POS sales, expenses and stock-adjustment capture without contacting the server. Every save commits locally before success is shown, appears immediately in normal lists/details, survives process death, and synchronizes automatically later. Server approval, payment settlement and workflow completion are distinct from a local save. Server-dependent capabilities are explicitly declared; arbitrary server scripts and fresh server reports cannot be promised offline. Local reports must declare their data coverage and pending-operation inclusion.

First sign-in and initial download require the selected Kora instance. A phone configured to reach a laptop via USB localhost can work offline after download, but reconnecting to Wi-Fi alone does not make that laptop reachable. Transport reachability and availability of local data are separate states.

## 2. Exact refactoring scope

| Current component | Refactor |
|---|---|
| data/WorkspaceRepository.kt | Split into WorkspaceRepository, RecordRepository, DraftRepository and SyncRepository. Remove network-first cached(), global lastResponseWasCached and cache clearing to []. |
| core/database/KoraDatabase.kt | Migrate SQLite v3 to transactional Room storage with local records, searchable indexes, schemas, drafts, operations, receipts, conflicts, cursor and bootstrap checkpoints. Preserve encrypted payload storage. |
| app/AppViewModel.kt | Move persistence, form serialization, POS command construction and sync coordination into repositories/use cases. Feature state observes database Flows. Remove direct online-submit/offline-enqueue branching. |
| OutboxSyncWorker.kt | Thin worker using an immutable workspace ID from inputData and the same SyncCoordinator used by foreground triggers. No current-instance or last_user lookup to choose work. |
| core/network/KoraHttpClient.kt | Keep transport responsibility; add typed sync adapter, structured error classification and cancellation propagation. No cache fallback on auth/permission errors. |
| Forms and child-table serializer | One schema-aware serializer used by drafts, local records and sync. Include child arrays and stable row IDs; preserve field types. |
| POS | Local sale aggregate and stock/payment intents committed atomically with its operation. Explicit server action capability and revision; no fabricated settled payment. |
| Instance/session handling | Stable tenant/workspace identity separate from network origin; pin every operation to tenant and actor. USB/LAN URL changes require verified same-server identity before rebinding transport. |
| Server contract and HTTP adapter | Reuse contract/offline.go and offline_service.go vocabulary, add production push/pull/bootstrap contracts and transactional execution. Existing reference intake is not a complete record application service. |

## 3. Core Kotlin types

Signatures below use kotlinx.serialization JsonObject and java.time.Instant. WorkspaceId, LocalRecordId, OperationId and DraftId are opaque UUID-backed value types; ServerRecordId, Doctype and Revision are opaque strings. Local IDs never change after a server ID is assigned. Wire fields use snake_case; Kotlin enums have explicit wire mappings.

```kotlin
data class WorkspaceIdentity(
    val id: WorkspaceId,
    val serverId: String,
    val tenantId: String,
    val site: String,
    val actorId: String,
    val branchId: String,
    val deviceId: String,
)
data class Generation(
    val configVersion: Long,
    val projectionHash: String,
    val schemaVersion: String,
)
data class RecordKey(val workspaceId: WorkspaceId, val doctype: Doctype, val localId: LocalRecordId)
enum class RecordSyncState { CLEAN, PENDING, CONFLICT, REJECTED }
data class LocalRecord(
    val key: RecordKey,
    val serverId: ServerRecordId?,
    val serverRevision: Revision?,
    val serverSnapshot: JsonObject?,
    val effectiveData: JsonObject,
    val syncState: RecordSyncState,
    val deleted: Boolean,
    val localSequence: Long,
)
data class RecordQuery(val search: String = "", val limit: Int = 50, val after: String? = null)
data class LocalPage<T>(val items: List<T>, val next: String?)
data class SaveRecordCommand(
    val workspaceId: WorkspaceId,
    val doctype: Doctype,
    val localId: LocalRecordId?, // null allocates one UUID locally
    val draftId: DraftId?,
    val data: JsonObject, // full editable document, including child rows
)
data class LocalSaveReceipt(val record: RecordKey, val operationId: OperationId)
data class FieldProblem(val path: String, val code: String, val message: String)
sealed interface SaveResult {
    data class Saved(val receipt: LocalSaveReceipt) : SaveResult
    data class Invalid(val fields: List<FieldProblem>) : SaveResult
    data class Failed(val code: String, val message: String) : SaveResult
}
```

## 4. Public repository signatures

```kotlin
interface RecordRepository {
    fun observeRecords(workspace: WorkspaceId, type: Doctype, query: RecordQuery): Flow<LocalPage<LocalRecord>>
    fun observeRecord(key: RecordKey): Flow<LocalRecord?>
    suspend fun save(command: SaveRecordCommand): SaveResult
    suspend fun delete(key: RecordKey): SaveResult // capability checked; tombstone locally
}
interface SchemaRepository {
    fun observeSchema(workspace: WorkspaceId, type: Doctype): Flow<DoctypeSchema?>
    fun observeLinkOptions(workspace: WorkspaceId, type: Doctype, query: RecordQuery): Flow<LocalPage<LinkOption>>
    suspend fun validate(command: SaveRecordCommand): List<FieldProblem>
}
interface DraftRepository {
    fun observeDrafts(workspace: WorkspaceId): Flow<List<RecordDraft>>
    suspend fun create(workspace: WorkspaceId, type: Doctype, target: LocalRecordId?): DraftId
    suspend fun save(id: DraftId, workspace: WorkspaceId, data: JsonObject, expectedEditSequence: Long): Unit
    suspend fun load(workspace: WorkspaceId, id: DraftId): RecordDraft?
    suspend fun discard(workspace: WorkspaceId, id: DraftId): Unit
}
interface PosRepository {
    fun observeCatalog(workspace: WorkspaceId, query: RecordQuery): Flow<LocalPage<PosProduct>>
    suspend fun recordSale(workspace: WorkspaceId, command: RecordSaleCommand): SaveResult
}
interface SyncRepository {
    fun observeStatus(workspace: WorkspaceId): Flow<SyncStatus>
    fun observeIssues(workspace: WorkspaceId): Flow<List<SyncIssue>>
    suspend fun retry(workspace: WorkspaceId, operation: OperationId): Unit
    suspend fun resolve(workspace: WorkspaceId, operation: OperationId, resolution: ConflictResolution): SaveResult
}
interface SyncScheduler {
    fun request(workspace: WorkspaceId, reason: SyncReason): Unit
}
interface SyncCoordinator {
    suspend fun run(workspace: WorkspaceId, budget: SyncBudget): SyncRunResult
}
interface BootstrapRepository {
    fun observeReadiness(workspace: WorkspaceId): Flow<OfflineReadiness>
    suspend fun download(workspace: WorkspaceId, selection: OfflineSelection): BootstrapResult
}
```

Supporting contracts: DoctypeSchema includes generation, fields, child schemas and offline capabilities. LinkOption includes localId, serverId and label. RecordDraft includes ID, target, data and editSequence. RecordSaleCommand includes a caller-generated stable sale ID, line local IDs, quantities/prices, till reference, payment intent and occurredAt; monetary values use decimal strings or minor units with currency, never binary floating point. SyncStatus includes pending/conflict/rejected counts, last successful pull, actual endpoint reachability and phase. OfflineReadiness includes selection, checkpoint, complete flag and missing dependencies. SyncRunResult is Completed, Deferred(retryAfter), AuthenticationRequired, RebootstrapRequired or Failed(code). ConflictResolution is AcceptServer, SubmitCorrection(data, expectedRevision) or DiscardLocal; financial reversals use business commands rather than deletion.

## 5. Database and transaction contracts

Room tables (all tenant/user isolated through workspace_id):

- workspaces: stable identity, endpoint binding, generation, offline authorization policy.
- records: workspace_id + doctype + local_id PK; nullable server_id unique per workspace/type; server_revision, encrypted server_snapshot/effective_payload, sync_state, deleted, local_sequence.
- record_search: explicitly approved searchable projections; protect sensitive fields according to existing encryption requirements. Do not decrypt whole payloads on every UI search.
- schemas/bundles: versioned schemas, manifests, branding, capabilities and linked-option coverage.
- drafts: workspace_id + draft_id PK; target local ID, encrypted payload, edit_sequence. Multiple new drafts per doctype are supported.
- operations: workspace_id + operation_id PK; sequence, immutable wire payload/digest, target local ID, base revision, schema generation, status, attempts, next_attempt_at, lease expiry, last error.
- operation_dependencies and record_links: stable local references and explicit JSON paths for server-ID encoding. Child rows retain stable local row IDs.
- receipts/conflicts: operation identity, server result/snapshot, reason and audit information.
- sync_checkpoints/bootstrap_pages: pull cursor, snapshot token, bootstrap coverage and completion.

Required internal local-store API:

```kotlin
interface LocalSyncStore {
    suspend fun commitLocal(command: PreparedMutation): LocalSaveReceipt
    suspend fun claimReady(workspace: WorkspaceId, owner: String, now: Instant, limit: Int): List<ClaimedOperation>
    suspend fun applyReceipts(workspace: WorkspaceId, owner: String, receipts: List<PushReceipt>): Unit
    suspend fun applyPullPage(workspace: WorkspaceId, page: PullResponse): Unit
    suspend fun releaseLease(workspace: WorkspaceId, owner: String): Unit
}
```

commitLocal atomically validates the schema generation, allocates sequence/IDs, updates effective record plus child/link/search projections, inserts outbox operation, and removes only the exact draft revision being committed. applyReceipts atomically records acknowledgements and canonical IDs/revisions, marks accepted operations, and recomputes effectiveData from the server snapshot plus remaining local operations. It must never erase edits made while a prior operation was in flight. applyPullPage commits record changes, tombstones and the next cursor together. Pending local edits are preserved as overlays; conflicting changes become issues. SQLite transaction rollback leaves all prior state intact.

## 6. Server wire contracts: reuse and extensions

Existing vocabulary is in the Kora Engine offline contracts: OfflineOperation, OfflineReceipt, OfflineConflict, OfflineTombstone, GenerationInfo and SyncCursor. Keep their wire values. Add named versioned DTOs in the authoritative Go contract package and generated consumer schemas; do not silently change BaseVersion int into Revision string. Add expected_revision as a new field and specify legacy compatibility. The current reference OfflineSyncService only stores operations and cursors; production intake must execute validated business commands before returning acked.

Proposed endpoints, all authenticated and site-scoped (not verified existing endpoints):

```text
GET  /api/v1/offline/capabilities
POST /api/v1/offline/bootstrap
POST /api/v1/offline/push
POST /api/v1/offline/pull
GET  /api/v1/offline/receipts/{operation_id}
```

```kotlin
interface SyncRemoteDataSource {
    suspend fun capabilities(workspace: WorkspaceIdentity): OfflineCapabilities
    suspend fun bootstrap(workspace: WorkspaceIdentity, request: BootstrapRequest): BootstrapResponse
    suspend fun push(workspace: WorkspaceIdentity, request: PushRequest): PushResponse
    suspend fun pull(workspace: WorkspaceIdentity, request: PullRequest): PullResponse
    suspend fun receipt(workspace: WorkspaceIdentity, operationId: OperationId): PushReceipt?
}
data class PushRequest(val deviceId: String, val generation: Generation, val operations: List<WireOperation>)
data class WireOperation(
    val id: OperationId,
    val branchId: String,
    val entityType: String,
    val entityId: String?,
    val clientEntityId: String,
    val operationType: String, // create | update | delete | command
    val expectedRevision: Revision?,
    val occurredAt: Instant,
    val schemaVersion: String,
    val dependsOn: List<OperationId>,
    val payload: JsonObject,
)
data class PushResponse(val receipts: List<PushReceipt>)
data class PushReceipt(
    val operationId: OperationId,
    val status: String, // acked | conflict | rejected | deferred
    val recordMappings: List<RecordMapping>,
    val records: List<ServerRecord>,
    val error: SyncProblem?,
    val receivedAt: Instant,
)
data class RecordMapping(val doctype: String, val clientEntityId: String, val serverId: String)
data class ServerRecord(val doctype: String, val serverId: String, val revision: Revision, val data: JsonObject)
data class SyncProblem(val code: String, val message: String, val fields: List<FieldProblem>, val retryAfterSeconds: Long?)
data class PullRequest(val cursor: String?, val limit: Int, val generation: Generation)
data class PullResponse(
    val changes: List<ServerRecord>,
    val tombstones: List<DeleteMarker>,
    val nextCursor: String,
    val hasMore: Boolean,
    val generation: Generation,
)
data class DeleteMarker(val doctype: String, val serverId: String, val revision: Revision)
data class BootstrapRequest(val selection: OfflineSelection, val snapshotToken: String?, val pageToken: String?, val limit: Int)
data class BootstrapResponse(
    val snapshotToken: String,
    val generation: Generation,
    val bundle: WorkspaceBundle?,
    val records: List<ServerRecord>,
    val nextPageToken: String?,
    val pullCursor: String?, // usable only once final snapshot page is committed
    val complete: Boolean,
)
```

OfflineCapabilities declares protocol version, supported schema generations, permitted doctypes and operations, offline-safe action IDs/versions, snapshot limits, authorization duration, cursor/tombstone retention and receipt retention. OfflineSelection names authorized datasets/filters; the server expands required dependencies. WorkspaceBundle contains schemas, child schemas, manifests, branding and local validation rules. DTOs are illustrative Kotlin representations of proposed authoritative wire additions.

Server guarantees:

1. Identity comes from authenticated tenant/site/actor context; never trust a client-supplied site or actor to grant scope.
2. Scope operation deduplication by tenant/site/actor and operation ID. Same ID and digest returns original receipt; different digest returns idempotency_key_reused. Business changes and receipt commit in one transaction, including durable intents for external side effects. Receipt retention must cover the supported offline/retry window; expired keys require explicit reconciliation before replay.
3. Check expected_revision for edits/deletes. Return conflict plus current snapshot for stale writes. Initial workflow state comes from server rules; queued payloads cannot bypass it.
4. Per-operation receipts allow unrelated operations to progress. A dependent operation is deferred until its prerequisite is accepted; rejected prerequisites block only their dependents. Acknowledgement means the business transaction committed.
5. Pull uses a server-ordered change log and opaque cursor, not device timestamps. Changes, delete markers, permission removals and snapshot boundary must be consistent. Expired cursor returns rebootstrap_required, never silent empty results.
6. Bootstrap pages belong to a stable snapshot and final pull cursor; resume safely after interruption. No 50-record implicit completeness. Rebootstrap replaces the server baseline while preserving pending local work.
7. Permission changes revoke local coverage according to a documented authorization policy; authentication expiry pauses sync without removing pending work. Logout locks local data; purge requires an explicit policy for unsynced records.

## 7. Sync execution and error behavior

All foreground, reconnect and worker triggers enqueue unique work for the immutable workspace. One process-wide coordinator plus database leases prevents overlapping drains and recovers claims after process death. Worker input contains workspace ID only; it resolves credentials for that identity and refuses cross-user processing. Foreground refresh observes the same coordinator.

Run: validate session/reachability; refresh capabilities/generation; push bounded ready operations in dependency order; persist receipts; pull all bounded pages within budget; persist each page/cursor; schedule remaining work. Per-record operation chains retain order. Freeze an operation payload before first send; later corrections get new operation IDs. Preserve cancellation and release claims in finally. Network loss, 429 and transient server failures use persisted exponential backoff/jitter. Auth errors pause; schema errors require refresh/migration; validation errors retain a visible rejected operation; conflicts retain both versions. Unrelated work proceeds. Network availability is a scheduling hint; a reachable Kora endpoint determines actual connectivity. WorkManager timing is OS-controlled and does not guarantee immediate background execution.

## 8. Business rules

- Offline POS records a sale and payment intent, not confirmed card/mobile-money settlement. Local till eligibility and cached product/pricing rules are versioned; server validates when syncing.
- Stock adjustments are immutable signed quantity deltas with a required reason. Server applies each operation once. Physical stock availability while disconnected is provisional; define the shop oversell policy explicitly.
- Sales/payments use corrections, reversals and reconciliation, not last-write-wins merging. General editable records use revision conflicts with field-level correction where safe.
- Child rows belong to the parent aggregate for atomic local save and sync. New linked parent records resolve through stable local IDs and returned mappings.
- Reporting exposes downloaded-data coverage and pending effects; server reports remain dated snapshots unless their calculation is deliberately implemented locally.

## 9. Implementation order and completion gates

1. Contract inventory and server capability check: locate actual HTTP handlers, revision semantics and idempotency coverage for resource and POS actions; define authoritative DTOs and endpoint compatibility. Gate: documented supported workflows and wire contract fixtures.
2. Local persistence migration: introduce Room and models/store transactions; import encrypted v3 cache, drafts and outbox without sending anything. Cache entries remain incomplete snapshots. Preserve existing idempotency keys; quarantine ambiguous legacy operations for reconciliation. Keep the old DB until verified migration and recoverability. Gate: no pending operation/draft lost and scope identity preserved.
3. Local record reads and saves: repository Flows, common serializer, child rows, multiple drafts, optimistic records; remove network branches from UI. Gate: save/edit/reopen after process death in airplane mode.
4. Production push and receipts: server transactional deduplication, revision checking, mappings, per-operation results and coordinator leases. Gate: lost response after server commit and concurrent triggers produce one business effect.
5. Pull/bootstrap: stable snapshot, complete datasets, linked dependencies, delete markers, permissions and resumable cursors. Gate: offline readiness means all selected workflows actually have their dependencies.
6. POS, inventory and expenses: dedicated offline domain commands, local projections and server reconciliation. Gate: totals, line items, till association, stock reasons and workflow initial state match accepted server outcomes.
7. Recovery UX and rollout: pending/conflict/rejected states, correct connectivity, auth recovery, workspace switching, coverage and reports. Install on the connected device and run the acceptance matrix. Gate: no silent record disappearance or blocked unrelated queue.

## 10. Required validation

Use repository/database integration tests for atomicity, overlays, migrations, lease recovery and cursor application; server contract/integration tests for authorization, revisions, deduplication, dependencies and snapshot consistency; Maestro for real on-device flows. Required scenarios: airplane-mode launch; more than 50 products; repeated create/edit with child rows; create linked customer then sale; multiple offline sales; process kill before/after local commit and server commit; lost acknowledgement; concurrent worker/foreground triggers; invalid record before valid records; expired login; unreachable USB-only server; tenant/user switching; same record edited on two devices; remote deletion and permission removal; schema update; interrupted bootstrap; expired cursor; stock/payment reconciliation. Check final server effects and local state, not only success banners.

## 11. Research baseline

- Android offline-first guidance: https://developer.android.com/topic/architecture/data-layer/offline-first
- Google Now in Android architecture: https://github.com/android/nowinandroid/blob/main/docs/ArchitectureLearningJourney.md
- PowerSync Kotlin implementation benchmark: https://github.com/powersync-ja/powersync-kotlin

Decision: use Room and Kora-owned sync contracts for the planned refactor. PowerSync remains a benchmark and a separate feasibility option; its adoption needs backend integration evaluation and is not assumed here.
