> Historical proposal. For current behavior see README.md and docs/offline-implementation-status.md.

# Kora Mobile: implementation plan and contracts

**Status:** planning document
**Reference UI:** the standalone Kora UI project
**Project:** standalone native client in this directory; no runtime imports from the web UI.

## 1. Goal and product boundary

Build a native Android app in Kotlin that mirrors the day-to-day Kora MVP workspace, uses the configured Kora server by default, and lets a person change or add a server before signing in. It must support offline access to previously synchronized workspace data and drafts. Offline writes are a supported product behavior only for operations that have a defined replay contract and server acceptance path. This plan targets Android; iOS would require a separate Swift client or a future Kotlin Multiplatform product decision.

The reference MVP is a config-driven React app. Its navigation is received from `GET /api/v1/system/navigation`; DocType schemas are loaded from `/api/v1/system/doctype/:doctype`; list/detail/create/edit screens use generic resources; page manifests can add configured screens. The native client mirrors that data model and adapts it to phone interaction rather than cloning desktop layouts.

### In scope for initial release

- Server selection, connection check, site/tenant selection where required, password and magic-link login.
- Workspace home, module navigation/search, records list/detail/create/edit, workflow actions, requests, and approvals.
- Persistent local cache for navigation, schemas, selected lists, and records; local drafts.
- Clear connection and sync state, pending-operation queue UI, and conflict UI.
- Secure session storage, tenant/user/server isolation, logout and account switching.

### Later phases

Reports/analytics, manifest-driven views, notifications, AI chat, Studio, marketplace, and administration (users, permissions, DocTypes, workflows, scripts, extensions, secrets, agents). These have broader or higher-risk interaction models and should not delay the field-work workspace.

## 2. Stack and project shape

Use **Kotlin + Jetpack Compose** for a native Android app. Keep a single-activity Compose UI with Navigation Compose, a ViewModel per feature/screen flow, coroutines and Flow for async/state streams, Retrofit + OkHttp for HTTP, Kotlin serialization for JSON, Room for durable offline data, DataStore for non-secret preferences, and Android Keystore-backed encrypted storage for session credentials. Use WorkManager for constrained background sync once the server sync contract exists. Pin versions when scaffolding against the current Android Gradle Plugin and SDK.

```text
app/src/main/java/com/kora/mobile/
  MainActivity.kt
  app/                       KoraApplication, AppRoot, NavGraph, AppViewModel
  core/
    config/                  BuildConfig defaults, InstanceConfig, Scope
    network/                 HttpClient, auth/csrf interceptor, URL resolver, error decoder
    database/                KoraDatabase, migrations, type converters
    security/                SessionVault (Android Keystore), redaction
    design/                  Theme, typography, shared Compose controls
    connectivity/            ConnectivityMonitor
  data/
    auth/                    AuthRemoteDataSource, AuthRepository
    workspace/               WorkspaceRemoteDataSource, WorkspaceRepository
    records/                 ResourceRemoteDataSource, RecordRepository, DraftRepository
    requests/                RequestRemoteDataSource, RequestRepository
    sync/                    SyncRemoteDataSource, OperationDao, SyncRepository, SyncWorker
  domain/
    model/                   Domain types and normalized errors
    usecase/                 SignIn, LoadWorkspace, SaveDraft, SyncPendingOperations
  feature/
    server/                  ServerSetupScreen + ServerSetupViewModel
    auth/                    LoginScreen + LoginViewModel
    home/                    DashboardScreen + DashboardViewModel
    records/                 RecordList/Detail/Form screens + ViewModels
    requests/                Requests and RequestDetail screens + ViewModels
    approvals/               Approval inbox and online decision UI
    sync/                    SyncStatusScreen, QueueItem, ConflictResolver
    settings/                Server, cache, account and logout screens
```

Use constructor-injected repositories/view models (Hilt is recommended if dependency graph grows; keep initial setup simple). UI emits actions to ViewModels; ViewModels call use cases/repositories; repositories coordinate Retrofit data sources with Room cache. Compose screens never access Retrofit or Room directly. Keep API DTOs separate from domain models and generate/validate DTO fixtures against the canonical server contracts. Do not import web UI source files.

Primary Android libraries by responsibility:

- UI/navigation: Jetpack Compose, Material 3, Navigation Compose, Lifecycle ViewModel Compose.
- Async: Kotlin coroutines, Flow.
- Networking: Retrofit, OkHttp, Kotlin serialization converter.
- Persistence: Room and DataStore Preferences.
- Secrets: Android Keystore-backed encrypted preferences or an audited Keystore wrapper; never persist passwords.
- Background work: WorkManager for retryable sync jobs, not for user-visible immediate interactions.


## 3. Screen and component plan

| Screen | Main components | Data and behavior |
|---|---|---|
| Server setup | `ServerSetupScreen`, `ServerUrlField`, `ConnectionCheck`, `SitePicker`, `SavedServerList` | Default URL from app configuration; accept HTTPS URLs and loopback HTTP for development; validate and persist server separately from credentials. Discover site context through the server's supported public mechanism. |
| Login | `LoginScreen`, `LoginForm`, `AuthModePicker`, `MagicLinkSent` | Fetch providers, support configured password and magic-link methods, show site/server identity. Never claim offline login validates a new user. |
| Home | `DashboardScreen`, `WelcomeCard`, `ModuleGrid`, `QuickSearch`, `RecentRecords`, `DraftsCard`, `SyncBanner` | Use cached navigation and local data immediately, then refresh. Include connection timestamp and cached/offline labeling. |
| Module/work list | `ModulePicker`, `RecordListScreen`, `RecordCard`, `FilterSheet`, `SortMenu`, `PaginationFooter` | Read configured navigation and DocType list metadata; search/filter against supported server semantics; cache fetched pages. |
| Record detail | `RecordDetailScreen`, `RecordSummary`, `RelatedRecords`, `WorkflowState`, `WorkflowActionSheet` | Render fields based on schema, show local pending edits distinctly, perform workflow transitions online unless replay is explicitly safe. |
| Create/edit | `RecordFormScreen`, `FieldRenderer`, `FormSection`, `ChildTableEditor`, `AttachmentPicker`, `SaveBar` | Local draft autosave. Initial native field support: Data, Text, Small Text, Int, Float, Currency, Check, Select, Date, Datetime, Link, Read Only, Table. Unsupported field types render read-only explanation, not a broken input. |
| Requests | `RequestsScreen`, `InboxTabs`, `RequestCard`, `RequestDetailScreen`, `RequestTimeline`, `ResponseComposer` | Use existing v2 request contracts. Read cached inbox/details; submit/respond/reassign/cancel only online in first release. |
| Approvals | `ApprovalsScreen`, `ApprovalCard`, `ApprovalDetail`, `ApprovalDecisionSheet` | Show pending AI approvals with freshness and expiry; decisions are online-only and must not be queued for delayed replay. |
| Sync center | `SyncStatusScreen`, `QueueItem`, `ConflictResolver`, `RetryAction` | Show queued, syncing, accepted, rejected, conflict, discarded; provide safe retry/discard and refresh paths. |
| Settings | `SettingsScreen`, `ServerManagement`, `StorageUsage`, `LogoutAction` | Switch server/site/account deliberately, show cache scope and clear-cache controls, logout and remove credentials. |

Android interaction intent: bottom navigation for primary areas, stacked Compose cards for records, full-screen forms, ModalBottomSheet for filters/actions, and persistent offline/sync status. Preserve Kora branding supplied by `/api/v1/system/branding` while respecting system font scaling, TalkBack semantics, back navigation, and platform edge-to-edge behavior.

## 4. Existing MVP API contracts to consume

All paths below are relative to the selected server and must include the tenant/site routing context required by that server (currently path-based `/s/:site` is supported by the web UI). The mobile client must not assume all installations use the same routing style.

| Purpose | Method and path | Request/response contract |
|---|---|---|
| Server health probe | `GET /api/ping` | Use only for reachability/version probe if endpoint is available; do not send credentials. |
| Auth providers | `GET /api/auth/providers` | `{data:{providers: AuthProvider[]}}` per standard response envelope. |
| Password login | `POST /api/auth/login` | `LoginRequest {email,password}` → `CurrentUser {name,email,full_name,roles}`; server sets `kora_sid` session cookie today. |
| Magic-link request | `POST /api/auth/magic-link/request` | `{email}` → message; native deep-link handling/verification needs server URL allow-listing. |
| Current user/logout | `GET /api/auth/me`, `POST /api/auth/logout` | CurrentUser and empty success respectively. |
| Navigation | `GET /api/v1/system/navigation` | `NavigationConfig {modules,views?,branding,user,admin_capabilities,supported_capabilities?,capabilities_version?}`. |
| Branding | `GET /api/v1/system/branding` | Tenant experience and branding. |
| DocType schema | `GET /api/v1/system/doctype/:doctype` | Schema-driven native form and list configuration. |
| Resource list/detail | `GET /api/v1/resource/:doctype[/:name]` | Standard data envelope; list supports limit/offset/order_by/filters/search/fields and meta total/doctype. |
| Resource create/update/delete | `POST /api/v1/resource/:doctype`, `PUT/DELETE /api/v1/resource/:doctype/:name` | Existing resource mutations; first release sends online only unless specific endpoint gains offline operation guarantees. |
| Workflow action | `POST /api/v1/resource/:doctype/:name/workflow_action` | `{action}`; online only at first. |
| Requests | `GET/POST /api/v2/requests`, `GET /api/v2/requests/:id`, response/reassignment/cancel/history subpaths | Use generated Engine v2 contracts already consumed in `src/lib/api/requests.ts`; online mutations only initially. |
| AI approvals | `GET /api/v1/ai/approvals`, `POST /api/v1/ai/approvals/:id/grant` | Approval decision is time-sensitive and explicitly online-only. |
| Analytics | `/api/v1/analytics/...` | Later phase; cached results must display their last-updated time. |

### Authentication decision required during implementation

The server's current browser flow uses `kora_sid` HttpOnly cookie plus double-submit `kora_csrf` cookie and `X-Kora-CSRF-Token`; `AuthMiddleware` also accepts `Authorization: Bearer ...`. Before app auth implementation, confirm whether session cookies can be held and replayed by the Android networking stack and whether cookie path/domain behavior works with path-routed sites. Preferred contract is a supported native token/session exchange with refresh/revocation and scoped audience; fallback is cookie jar persistence plus CSRF bootstrap. Do not store passwords. Store only revocable session material using Android Keystore-backed storage.

## 5. Offline data and sync contracts

### 5.1 Local persistence

SQLite entities must be keyed by the full scope:

`scope = normalized_server_origin + site_id_or_route + user_id`

Minimum tables:

- `server_instances(id, origin, label, site_ref, last_used_at)`
- `sessions(scope_id, encrypted_secret_reference, expires_at, user_id)`; the secret itself remains in SecureStore.
- `navigation(scope_id, payload_json, config_version, fetched_at)`
- `doctype_schemas(scope_id, doctype, schema_json, schema_version, fetched_at)`
- `records(scope_id, doctype, record_id, payload_json, server_version, fetched_at, deleted_at)`
- `record_pages(scope_id, query_hash, record_ids_json, next_cursor, fetched_at)`
- `drafts(scope_id, doctype, record_id_nullable, local_id, base_version, payload_json, created_at, updated_at)`
- `operations(scope_id, operation_id, device_id, branch_id, entity_type, entity_id, operation_type, base_version, schema_version, payload_json, occurred_at, status, attempts, last_error)`
- `conflicts(scope_id, conflict_id, operation_id, reason_code, server_version, snapshot_json, resolution_mode, recorded_at)`

Use Android Keystore-backed encryption for session secrets and assess SQLCipher or equivalent database encryption for cached sensitive records before production. Logout must remove session secrets; cache retention must be an explicit setting. Switching scope must never query or display another scope's rows.

### 5.2 Read/offline semantics

- Offline mode allows only data already cached for the selected server/site/user.
- Every cached screen shows its last refresh time and a stale indicator when network unavailable.
- A cache miss is an honest empty/offline state, never a fabricated empty server result.
- Refresh is network-first with cache fallback; use schema/config generation metadata to invalidate incompatible data.
- Do not cache secrets, auth challenge responses, or approval grant material.
- Cached sensitive records should be cleared on logout by default; a later product decision may allow encrypted multi-account retention.

### 5.3 Mutation policy

| Operation | Offline policy in first native release | Reason |
|---|---|---|
| Save an unfinished new/edit form | Allowed as local draft | No server side effect until user submits. |
| Create/update ordinary records | Queue only after sync endpoint, idempotency, authorization, base-version conflict, and schema-gate contracts are live; otherwise disabled offline with “save draft” option. | A local queue alone does not ensure durable/replay-safe server execution. |
| Delete record | Online only initially | Requires tombstone, authorization recheck, and conflict/recovery behavior. |
| Workflow transition | Online only initially | Business side effects and current state/role must be revalidated. |
| Submit/respond/reassign/cancel request | Online only initially | Assignment and response validity can change while offline. |
| Approve AI action / payment / external action | Always online; never automatically replay | Time-sensitive and potentially irreversible. |
| Upload attachment | Draft metadata can be local; bytes upload online, queue later only with resumable upload support. | Avoid silent loss and excessive local storage. |

### 5.4 Server sync API proposal (required for queued writes)

The repository currently defines offline wire models in `contract/offline.go` (`OfflineOperation`, `OfflineConflict`, `OfflineSyncBatch`, `SyncCursor`, generation gates) but no HTTP sync routes were found in the API router. Add and publish a tenant-authenticated API before claiming full offline writes.

**Register device / get sync capabilities**

`POST /api/v1/offline/devices`

```json
{
  "device_id": "uuid",
  "platform": "ios|android",
  "app_version": "semver",
  "client_capabilities": ["resource.create", "resource.update"],
  "site": "site-id"
}
```

Response: device identity, accepted capabilities, `GenerationInfo {site,config_version,projection_hash}`, current schema version, supported offline mode, cursor, and server retention window. Device registration must be authenticated and bound to current user/site.

**Push operation batch**

`POST /api/v1/offline/sync/push`

```json
{
  "device_id": "uuid",
  "branch_id": "uuid",
  "site": "site-id",
  "schema_version": "hash-or-version",
  "cursor": {"token":"opaque","branch_id":"uuid","device_id":"uuid","version":12,"at":"RFC3339"},
  "operations": [{
    "id":"uuid",
    "device_id":"uuid",
    "branch_id":"uuid",
    "site":"site-id",
    "entity_type":"Customer",
    "entity_id":"CUST-123",
    "operation_type":"create|update|delete",
    "base_version":4,
    "occurred_at":"RFC3339",
    "schema_version":"hash-or-version",
    "status":"queued",
    "payload":{},
    "metadata":{"idempotency_key":"uuid"}
  }]
}
```

Response per operation: operation ID, status (`acked|conflict|rejected`), canonical server record or typed error, conflict snapshot/resolution mode, server version; plus next cursor. Enforce exact duplicate replay idempotency (same ID + same payload returns prior receipt; same ID + different payload is a conflict), validate user/site/device scope, revalidate permissions and workflow/schema constraints at commit time, and preserve an audit trail. A batch should report each result independently and must not silently drop rejected work.

**Pull changes**

`GET /api/v1/offline/sync/pull?device_id=...&branch_id=...&cursor=...&limit=...`

Response: changed records and tombstones visible to the user, deletion/version metadata, next cursor, current generation/schema gate, and required invalidations. Cursor must be opaque and bound to device, site, and user. Define retention and full-resync response when cursor expires.

**Conflict handling**

Conflict response follows `OfflineConflict` fields from `contract/offline.go`; include the current server record snapshot and allowed resolution modes (`retry`, `merge`, `manual_review`, `discard`). A resolved operation receives a new operation ID unless server contract explicitly supports safe retry of the original; UI must preserve both original and resolution audit references. Never overwrite a newer record silently.

**Schema/capability gate**

Before push, server compares client `GenerationInfo` and capabilities with current site generation. Reject stale projections with typed `stale_generation` or `capability_missing`; app pauses writes, refreshes schema, keeps queued payloads intact, and asks for user review when form meaning changed.

### 5.5 Conflict resolution policy

- Create collision: offer server record and local draft; user can copy into a new draft, not overwrite.
- Update stale base: three-way compare base snapshot, local edits, and server snapshot; field-level auto-merge only for non-overlapping fields with unchanged schema; otherwise manual review.
- Permission revoked, required field/config changed, workflow state changed: mark rejected/conflict and require online user action.
- Delete collision or record already deleted: present final server state and allow local queue item discard.
- Preserve failed/conflicted operations until user discards them; do not clear queue on logout or server switch.

## 6. Component/API boundaries

```kotlin
data class InstanceConfig(
    val id: String,
    val origin: String,
    val siteRef: String? = null,
    val label: String,
)

data class Scope(
    val instanceId: String,
    val normalizedOrigin: String,
    val siteRef: String?,
    val userId: String,
)

interface AuthRepository {
    suspend fun providers(scope: InstanceScope): List<AuthProvider>
    suspend fun login(scope: InstanceScope, input: LoginRequest): SessionSummary
    suspend fun currentUser(scope: Scope): CurrentUser
    suspend fun logout(scope: Scope)
}

interface WorkspaceRepository {
    fun observeNavigation(scope: Scope): Flow<CachedValue<NavigationConfig>?>
    suspend fun refreshNavigation(scope: Scope): NavigationConfig
    fun observeSchema(scope: Scope, doctype: String): Flow<CachedValue<DocTypeSchema>?>
    fun observeRecords(scope: Scope, query: RecordQuery): Flow<CachedPage<Document>>
    suspend fun refreshRecords(scope: Scope, query: RecordQuery): CachedPage<Document>
}

interface DraftRepository {
    fun observeDrafts(scope: Scope): Flow<List<DocumentDraft>>
    suspend fun saveDraft(scope: Scope, draft: DocumentDraft)
    suspend fun deleteDraft(scope: Scope, draftId: String)
}

interface SyncRepository {
    fun observeQueue(scope: Scope): Flow<List<QueueItem>>
    suspend fun enqueue(scope: Scope, operation: OfflineOperation)
    suspend fun syncNow(scope: Scope): SyncSummary
    suspend fun resolveConflict(scope: Scope, conflictId: String, resolution: ConflictResolution)
}
```

`KoraHttpClient` (Retrofit/OkHttp) owns URL composition, site routing, cookie/token handling, JSON envelopes, typed error decoding, timeouts, and request IDs through interceptors/converters. Remote data sources own endpoint paths and DTO decoding. Repositories own local read/write and cache freshness. Compose UI calls ViewModels/use cases only; no screen touches Room or Retrofit directly.

### Error contract

Normalize all API errors to:

```kotlin
data class KoraError(
    val code: String,
    val message: String,
    val status: Int? = null,
    val field: String? = null,
    val details: Map<String, JsonElement> = emptyMap(),
    val retryable: Boolean,
    val requestId: String? = null,
)
```

Network/timeouts are retryable; validation/auth/permission/schema errors are not automatically retried. HTTP 409 maps to conflict, 401 triggers session refresh/sign-in, and 403 is not reinterpreted as offline.

## 7. Delivery phases and acceptance gates

### Phase 0 — contract spike

- Verify default production server origin and public probe behavior.
- Confirm tenant/site discovery and path-routed requests from native clients.
- Verify auth cookie vs bearer behavior, CSRF, logout, token revocation, and Android app-link handling.
- Inventory supported MVP field types and API response envelope fixtures.
- Agree which record operations can be replay-safe; design sync endpoints above if needed.

**Gate:** an Android prototype can select server/site, log in, call `/me`, retrieve navigation/schema/list, and log out against both hosted and local Kora instances.

### Phase 1 — connected workspace

- Scaffold Android Gradle project, Compose navigation/theme/branding, server setup, auth, dashboard, record list/detail/forms, requests and approvals.
- Add API DTO decoders and contract fixtures based on MVP responses.
- No offline mutation replay; drafts persist locally.

**Gate:** core screens work on supported Android API levels, account scope and server switching are correct, unsupported fields are explicit.

### Phase 2 — offline reads and drafts

- SQLite migrations/repositories, cache policy, scope isolation, draft list/resume/delete, offline banners and cache timestamps.
- Cache navigation/schema and bounded recent pages/records.

**Gate:** airplane-mode test demonstrates cached navigation/details/drafts, empty cache messaging, and no cross-account/server leakage.

### Phase 3 — durable sync

- Implement server registration/push/pull/generation endpoints or adapt to an existing formal endpoint if one is added.
- Build queue runner, retry policy, per-operation receipt handling, conflict screens, and schema-gate behavior.
- Enable only explicitly allow-listed create/update operations.

**Gate:** duplicate push is idempotent; stale writes return visible conflicts; auth loss pauses sync; process kill/restart retains queued work and receipt state.

### Phase 4 — broader MVP parity

- Add reports, manifest-defined pages where mobile-compatible, notifications, AI chat, then admin tools and marketplace.
- Gate each feature by advertised server capability and define its offline policy independently.

**Gate:** no feature claims offline support without repository, sync, authorization, conflict, and retention behavior defined.

## 8. Operational and quality requirements

- Accessibility: screen reader labels, dynamic type, touch targets, keyboard navigation for tablets, color-independent status labels.
- Security: HTTPS by default; allow HTTP only for loopback/development unless explicitly overridden; redact tokens and sensitive payloads from logs; secure-store secrets; define cache TTL and logout clearing.
- Reliability: bounded retries with backoff and jitter; cancellation on account switch; idempotent sync; durable queue; migration tests; visible last-sync time.
- Performance: paginated data, bounded local cache, list virtualization, schema cache keyed by config generation.
- Observability: request correlation IDs and non-sensitive sync diagnostics; opt-in diagnostic export that redacts record data and credentials.
- Contract compatibility: decode defensively, preserve unknown fields where safe, and block queued writes when schema/capability generation is unknown or stale.

## 9. Decisions to finalize before implementation

1. Exact default server origin for production builds and whether “server URL” includes a site route or asks for site separately.
2. Auth mechanism supported for native app: bearer session/token exchange preferred, cookie jar fallback.
3. Whether server offline sync endpoints are part of this project or a prerequisite from the Kora engine.
4. Offline data retention/encryption policy, especially behavior after logout and on shared devices.
5. Initial offline write slice: recommended first eligible slice is ordinary DocType create/update only, with workflow, requests, deletes, payments, approvals, and external actions online-only.
6. Branding/product name: Kora vs Kigae labels in Play Store and server-provided tenant branding.
7. iOS is outside this Kotlin-native Android scope; decide later whether to build a separate Swift app or adopt Kotlin Multiplatform.
