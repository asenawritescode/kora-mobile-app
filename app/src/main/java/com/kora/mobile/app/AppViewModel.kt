package com.kora.mobile.app

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.Uri
import com.kora.mobile.data.offline.FormPayloadSerializer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kora.mobile.KoraApplication
import com.kora.mobile.OutboxSyncWorker
import com.kora.mobile.core.database.LocalMutation
import com.kora.mobile.domain.model.Branding
import com.kora.mobile.domain.model.CurrentUser
import com.kora.mobile.domain.model.DocTypeNavItem
import com.kora.mobile.domain.model.FieldSchema
import com.kora.mobile.domain.model.FieldConstraint
import com.kora.mobile.domain.model.ModuleGroup
import com.kora.mobile.domain.model.NavigationConfig
import com.kora.mobile.domain.model.NavigationView
import com.kora.mobile.domain.model.resolveComponentResourceDoctype
import com.kora.mobile.domain.model.Scope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.LocalDate

data class AppState(
    val stage: AppStage = AppStage.SERVER,
    val serverUrl: String = "",
    val siteRef: String = "",
    val message: String? = null,
    val messageIsError: Boolean = true,
    val busy: Boolean = false,
    val user: CurrentUser? = null,
    val navigation: NavigationConfig? = null,
    val tenantBranding: Branding? = null,
    val brandingDraft: Branding? = null,
    val savedBranding: Branding? = null,
    val currency: String = "",
    val savedCurrency: String = "",
    val settingsPage: String? = null,
    val settingsDoctypes: List<JSONObject> = emptyList(),
    val settingsDoctypesLoading: Boolean = false,
    val settingsDoctypesError: String? = null,
    val settingsDoctypeSaving: Boolean = false,
    val settingsDoctypeActionError: String? = null,
    val settingsDoctypeActionMessage: String? = null,
    val settingsResources: Map<String, List<JSONObject>> = emptyMap(),
    val settingsResourcesLoading: String? = null,
    val settingsResourcesError: Map<String, String> = emptyMap(),
    val workflowDefinitions: List<JSONObject> = emptyList(),
    val workflowDefinitionsLoading: Boolean = false,
    val workflowDefinitionsError: String? = null,
    val selectedWorkflow: JSONObject? = null,
    val selectedWorkflowLoading: Boolean = false,
    val selectedWorkflowError: String? = null,
    val agentPolicies: List<JSONObject> = emptyList(),
    val agentPoliciesLoading: Boolean = false,
    val agentPoliciesError: String? = null,
    val selectedAgentPolicy: JSONObject? = null,
    val savedAgentPolicySnapshot: String? = null,
    val agentPolicyLoading: Boolean = false,
    val agentPolicySaving: Boolean = false,
    val agentPolicyError: String? = null,
    val agentEffectivePermissions: List<JSONObject> = emptyList(),
    val agentRuns: List<JSONObject> = emptyList(),
    val agentPermissionCatalog: List<JSONObject> = emptyList(),
    val koriEnabledInApp: Boolean = true,
    val settingsSaving: Boolean = false,
    val configVersions: List<JSONObject> = emptyList(),
    val configVersionsLoading: Boolean = false,
    val configVersionsError: String? = null,
    val configVersionDetail: JSONObject? = null,
    val configVersionDetailLoading: Boolean = false,
    val settingsUsers: List<JSONObject> = emptyList(),
    val settingsUsersLoading: Boolean = false,
    val settingsUsersError: String? = null,
    val accessRoles: List<JSONObject> = emptyList(),
    val accessPermissions: List<JSONObject> = emptyList(),
    val savedAccessPermissions: List<JSONObject> = emptyList(),
    val selectedAccessRole: String = "",
    val accessLoading: Boolean = false,
    val accessError: String? = null,
    val accessSaving: Boolean = false,
    val themeMode: String = "light",
    val themePreference: String = "site",
    val section: String = "Home",
    val selectedModule: String? = null,
    val selectedDoctype: DocTypeNavItem? = null,
    val selectedRecord: JSONObject? = null,
    val detailLoading: Boolean = false,
    val detailLinkLabels: Map<String, String> = emptyMap(),
    val detailLinkedRecords: Map<String, JSONObject> = emptyMap(),
    val detailLinkedSchemas: Map<String, List<FieldSchema>> = emptyMap(),
    val recordLinkLabels: Map<String, String> = emptyMap(),
    val records: List<JSONObject> = emptyList(),
    val schemaFields: List<FieldSchema> = emptyList(),
    val childSchemas: Map<String, List<FieldSchema>> = emptyMap(),
    val titleField: String = "name",
    val doctypeManifest: JSONObject? = null,
    val listSearch: String = "",
    val listSelectionActive: Boolean = false,
    val listSelectionCount: Int = 0,
    val linkOptions: Map<String, List<JSONObject>> = emptyMap(),
    val linkTitleFields: Map<String, String> = emptyMap(),
    val linkLoading: Set<String> = emptySet(),
    val linkErrors: Map<String, String> = emptyMap(),
    val userLabels: Map<String, String> = emptyMap(),
    val userChoices: Map<String, String> = emptyMap(),
    val editingRecord: JSONObject? = null,
    val formValues: Map<String, Any?> = emptyMap(),
    val formOpen: Boolean = false,
    val resolvingMutationId: String? = null,
    val requests: List<JSONObject> = emptyList(),
    val notifications: List<JSONObject> = emptyList(),
    val selectedNotification: JSONObject? = null,
    val notificationToast: JSONObject? = null,
    val queuedMutations: List<LocalMutation> = emptyList(),
    val syncingMutations: Boolean = false,
    val discardingMutations: Boolean = false,
    val offline: Boolean = false,
    val reconnecting: Boolean = false,
    val offlineCoverage: com.kora.mobile.data.offline.OfflineCoverage = com.kora.mobile.data.offline.OfflineCoverage(),
    val posManifest: JSONObject? = null,
    val posLoading: Boolean = false,
    val posProducts: List<JSONObject> = emptyList(),
    val posCategories: Map<String, String> = emptyMap(),
    val posCart: Map<String, PosCartLine> = emptyMap(),
    val posLineLabel: String = "Lines",
    val posSaving: Boolean = false,
    val posPaymentMethod: String = "",
    val posPrerequisiteRecords: Map<String, String> = emptyMap(),
    val posPrerequisiteInputs: Map<String, String> = emptyMap(),
    val posPrerequisitesInProgress: Boolean = false,
    val analyticsCatalog: JSONObject? = null,
    val savedReports: List<JSONObject> = emptyList(),
    val reportResult: JSONObject? = null,
    val reportLoading: Boolean = false,
    val reportError: String? = null,
    val selectedReportName: String = "",
    val selectedAnalyticsModel: String = "",
    val selectedAnalyticsMeasure: String = "",
    val reportWindowDays: Int = 30,
    val assistantTurns: List<AssistantTurn> = emptyList(),
    val assistantBusy: Boolean = false,
    val assistantError: String? = null,
) {
    /** One presentation state keeps the shell status dot and connection banner in sync. */
    val connectionState: ConnectionState
        get() = when {
            syncingMutations -> ConnectionState.Syncing(queuedMutations.size)
            reconnecting -> ConnectionState.Reconnecting
            offline -> ConnectionState.Offline(queuedMutations.size)
            else -> ConnectionState.Live
        }
}

sealed interface ConnectionState {
    data object Live : ConnectionState
    data object Reconnecting : ConnectionState
    data class Offline(val pendingChanges: Int) : ConnectionState
    data class Syncing(val remaining: Int) : ConnectionState
}

data class PosCartLine(val product: JSONObject, val quantity: Int)
data class AssistantTurn(val role: String, val content: String)
enum class AppStage { SERVER, LOGIN, WORKSPACE }

private fun Branding.toJson() = JSONObject()
    .put("app_name", appName)
    .put("short_name", shortName.orEmpty())
    .put("logo_url", logoUrl.orEmpty())
    .put("logo_dark_url", logoDarkUrl.orEmpty())
    .put("favicon_url", faviconUrl.orEmpty())
    .put("primary_color", primaryColor.orEmpty())
    .put("accent_color", accentColor.orEmpty())
    .put("font_family", fontFamily.orEmpty())
    .put("heading_font", headingFont.orEmpty())
    .put("default_mode", defaultMode.orEmpty())

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as KoraApplication
    private val _state = MutableStateFlow(AppState(
        serverUrl = app.instanceStore.current().origin,
        siteRef = app.instanceStore.current().siteRef.orEmpty(),
        koriEnabledInApp = app.getSharedPreferences("kora_mobile_navigation", 0).getBoolean("show_kori", true),
    ))
    val state: StateFlow<AppState> = _state.asStateFlow()
    private var scope: Scope? = null
    private var experienceCopy: JSONObject? = null
    private var outboxJob: Job? = null
    private var outboxSyncJob: Job? = null
    private var recordRowsJob: Job? = null
    private var liveUpdatesJob: Job? = null
    private var notificationStreamJob: Job? = null
    private var appForeground = false
    private var scheduledMutationIds: Set<String> = emptySet()
    private val connectivityManager = app.getSystemService(ConnectivityManager::class.java)
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            app.workspace.networkAvailable = true
            _state.update { it.copy(reconnecting = it.offline) }
            syncQueuedMutations()
            refreshConnectedWorkspace()
        }
        override fun onLost(network: Network) { app.workspace.markNetworkUnavailable(); _state.update { it.copy(reconnecting = false) } }
    }

    init {
        val connected = connectivityManager.activeNetwork?.let { connectivityManager.getNetworkCapabilities(it)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } == true
        if (!connected) app.workspace.markNetworkUnavailable()
        viewModelScope.launch { app.workspace.offline.collect { offline -> _state.update { it.copy(offline = offline, reconnecting = offline && app.workspace.networkAvailable) } } }
        viewModelScope.launch { app.workspace.coverage.collect { coverage -> _state.update { it.copy(offlineCoverage = coverage) } } }
        restoreCachedSession()
        runCatching { connectivityManager.registerDefaultNetworkCallback(networkCallback) }
    }

    override fun onCleared() {
        runCatching { connectivityManager.unregisterNetworkCallback(networkCallback) }
        liveUpdatesJob?.cancel()
        notificationStreamJob?.cancel()
        super.onCleared()
    }

    fun onAppResumed() {
        appForeground = true
        if (_state.value.stage != AppStage.WORKSPACE) return
        refreshConnectedWorkspace()
        startLiveUpdates()
        startNotificationStream()
    }

    private fun startLiveUpdates() {
        if (!appForeground || _state.value.stage != AppStage.WORKSPACE) return
        if (liveUpdatesJob?.isActive == true) return
        val active = scope ?: return
        liveUpdatesJob = viewModelScope.launch {
            var metadataRefreshedAt = System.currentTimeMillis()
            while (_state.value.stage == AppStage.WORKSPACE && scope?.key == active.key) {
                delay(5_000)
                if (!app.workspace.networkAvailable) continue
                runCatching { app.workspace.refreshLatest(active) }
                val now = System.currentTimeMillis()
                if (now - metadataRefreshedAt >= 15_000) {
                    loadWorkspace()
                    loadRequests()
                    if (_state.value.formOpen) loadUserChoices(_state.value.schemaFields)
                    metadataRefreshedAt = now
                }
            }
        }
    }

    private fun startNotificationStream() {
        if (!appForeground || _state.value.stage != AppStage.WORKSPACE || notificationStreamJob?.isActive == true) return
        val active = scope ?: return
        notificationStreamJob = viewModelScope.launch {
            _state.update { it.copy(notifications = app.workspace.notifications(active).toJsonObjects()) }
            var cursor = app.workspace.realtimeCursor(active)
            while (appForeground && _state.value.stage == AppStage.WORKSPACE && scope?.key == active.key) {
                try {
                    app.workspace.streamRealtime(active, cursor) { event ->
                        cursor = event.optString("stream_id").takeIf(String::isNotBlank) ?: cursor
                        viewModelScope.launch {
                            cursor?.let { nextCursor -> runCatching { app.workspace.saveRealtimeCursor(active, nextCursor) } }
                            if (event.optString("type") != "change" || event.optString("transport") == "replay") return@launch
                            if (event.optString("operation") != "notification") {
                                runCatching { app.workspace.refreshLatest(active) }
                                return@launch
                            }
                            val payload = event.optJSONObject("payload") ?: JSONObject()
                            val recipient = payload.optString("recipient")
                            val user = _state.value.user
                            if (recipient.isNotBlank() && user != null && !recipient.equals(user.name, true) && !recipient.equals(user.email, true)) return@launch
                            val requestId = payload.optString("request_id")
                            val notification = if (requestId.isNotBlank()) {
                                val request = runCatching { app.workspace.request(active, requestId) }.getOrNull()
                                JSONObject()
                                    .put("id", event.optString("id").ifBlank { event.optString("stream_id") })
                                    .put("stream_id", event.optString("stream_id"))
                                    .put("request_id", requestId)
                                    .put("title", request?.optString("title").orEmpty().ifBlank { "Request needs attention" })
                                    .put("message", request?.optString("message").orEmpty())
                                    .put("created_at", request?.optString("created_at").orEmpty())
                                    .put("subject", request?.optJSONObject("subject") ?: JSONObject())
                            } else {
                                JSONObject()
                                    .put("id", event.optString("id").ifBlank { event.optString("stream_id") })
                                    .put("stream_id", event.optString("stream_id"))
                                    .put("title", payload.optString("title").ifBlank { "Workspace update" })
                                    .put("message", payload.optString("message"))
                                    .put("created_at", event.optString("occurred_at").ifBlank { event.optString("timestamp") })
                                    .put("source", payload.optString("source"))
                            }
                            // Notification actions are optional, generic metadata. Keep them intact
                            // so the UI can navigate to any accessible DocType record without
                            // knowing about a particular module or business domain.
                            payload.optJSONObject("action")?.let { notification.put("action", JSONObject(it.toString())) }
                            listOf("action_label", "target_doctype", "target_name", "category").forEach { key ->
                                payload.optString(key).takeIf(String::isNotBlank)?.let { notification.put(key, it) }
                            }
                            if (notification.optString("id").isBlank()) return@launch
                            runCatching { app.workspace.storeNotification(active, notification) }
                            _state.update { state ->
                                val merged = (listOf(notification) + state.notifications).distinctBy { item -> item.optString("id") }.take(100)
                                state.copy(notifications = merged, notificationToast = notification)
                            }
                            delay(5000)
                            _state.update { state -> if (state.notificationToast?.optString("id") == notification.optString("id")) state.copy(notificationToast = null) else state }
                        }
                    }
                    delay(1500)
                } catch (cancelled: kotlinx.coroutines.CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    delay(3000)
                }
            }
        }
    }

    fun onAppPaused() {
        appForeground = false
        liveUpdatesJob?.cancel()
        liveUpdatesJob = null
        notificationStreamJob?.cancel()
        notificationStreamJob = null
    }

    private fun refreshConnectedWorkspace() {
        if (_state.value.stage != AppStage.WORKSPACE) return
        val active = scope ?: return
        loadWorkspace()
        loadRequests()
        viewModelScope.launch {
            runCatching { app.workspace.refreshLatest(active) }
            if (_state.value.section == "Requests") loadRequests()
            val selected = _state.value.selectedDoctype
            if (_state.value.section == "Work" && selected != null && !app.workspace.localRecords.isCovered(active, selected.resourceName)) {
                runCatching { app.workspace.records(active, selected.resourceName) }
            }
        }
    }

    fun connect(url: String, site: String) = viewModelScope.launch {
        _state.update { it.copy(busy = true, message = null) }
        try {
            val instance = app.instanceStore.save(url, site)
            val temporaryScope = Scope(instance.id, instance.origin, instance.siteRef, "")
            app.workspace.probe(temporaryScope)
            _state.update { it.copy(serverUrl = instance.origin, siteRef = instance.siteRef.orEmpty(), stage = AppStage.LOGIN, message = "Server is reachable. Sign in to continue.", busy = false) }
        } catch (error: Exception) {
            if (error is IllegalArgumentException) {
                _state.update { it.copy(stage = AppStage.SERVER, busy = false, message = error.message) }
                return@launch
            }
            val unauthorized = (error as? com.kora.mobile.domain.model.KoraError)?.status == 401
            _state.update { it.copy(serverUrl = url, siteRef = site, stage = AppStage.LOGIN, busy = false, message = if (unauthorized) null else error.message ?: "Could not reach this server") }
        }
    }

    fun goToLogin(url: String, site: String) {
        try {
            val instance = app.instanceStore.save(url, site)
            _state.update { it.copy(serverUrl = instance.origin, siteRef = instance.siteRef.orEmpty(), stage = AppStage.LOGIN, message = null) }
        } catch (error: Exception) { _state.update { it.copy(message = error.message) } }
    }

    fun requestMagicLink(email: String, cloudUrl: String) = viewModelScope.launch {
        _state.update { it.copy(busy = true, message = null, messageIsError = true) }
        try {
            val cloudOrigin = normalizeCloudOrigin(cloudUrl)
            require(email.trim().matches(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))) { "Enter a valid email address" }
            val random = ByteArray(32).also(SecureRandom()::nextBytes)
            val mobileState = random.joinToString("") { "%02x".format(it.toInt() and 0xff) }
            app.workspace.requestCloudMagicLink(cloudOrigin, email.trim(), mobileState)
            app.getSharedPreferences("kora_session", 0).edit()
                .putString("pending_mobile_state", mobileState)
                .putLong("pending_mobile_state_at", System.currentTimeMillis())
                .apply()
            _state.update { it.copy(busy = false, message = "If this email belongs to a Kora Cloud account, a sign-in link is on its way. Open it on this phone.", messageIsError = false) }
        } catch (error: Exception) {
            _state.update { it.copy(busy = false, message = error.message ?: "Could not request a sign-in link", messageIsError = true) }
        }
    }

    fun acceptCloudSignInIntent(intentUri: Uri?) {
        if (intentUri?.scheme != "kora" || intentUri.host != "auth" || intentUri.path != "/cloud") return
        val fragment = intentUri.fragment ?: return
        val values = Uri.parse("https://mobile.invalid/?$fragment")
        val mobileState = values.getQueryParameter("state") ?: return
        val hostname = values.getQueryParameter("hostname") ?: return
        val tenantId = values.getQueryParameter("tenant_id") ?: return
        val code = values.getQueryParameter("code") ?: return
        completeCloudSignIn(mobileState, hostname, tenantId, code)
    }

    private fun completeCloudSignIn(mobileState: String, hostname: String, tenantId: String, code: String) = viewModelScope.launch {
        val prefs = app.getSharedPreferences("kora_session", 0)
        val expected = prefs.getString("pending_mobile_state", null).orEmpty()
        val issuedAt = prefs.getLong("pending_mobile_state_at", 0L)
        val validState = expected.isNotEmpty() && MessageDigest.isEqual(expected.toByteArray(), mobileState.toByteArray()) &&
            System.currentTimeMillis() - issuedAt in 0..(20 * 60 * 1000L)
        prefs.edit().remove("pending_mobile_state").remove("pending_mobile_state_at").apply()
        if (!validState) {
            _state.update { it.copy(message = "This sign-in link was not requested by this app or has expired. Request a new link.", messageIsError = true) }
            return@launch
        }
        _state.update { it.copy(busy = true, message = null, messageIsError = true) }
        try {
            val instance = app.instanceStore.current()
            val configuredSite = instance.siteRef.orEmpty()
            require(configuredSite == tenantId || configuredSite.equals(hostname, ignoreCase = true)) {
                "This Cloud account is not linked to the selected site (${configuredSite})."
            }
            require(code.isNotBlank() && hostname.isNotBlank()) { "The sign-in link is incomplete. Request a new one." }
            val loginScope = Scope(instance.id, instance.origin, configuredSite, "")
            app.workspace.exchangeCloudHandoff(loginScope, code)
            val user = parseUser(app.workspace.currentUser(loginScope))
            val activeScope = Scope(instance.id, instance.origin, configuredSite, user.name)
            scope = activeScope
            app.activateScope(activeScope)
            prefs.edit().putString("last_user", user.name).putString("last_email", user.email).putString("last_full_name", user.fullName).apply()
            _state.update { it.copy(stage = AppStage.WORKSPACE, user = user, busy = false, message = null, messageIsError = false, offline = false) }
            loadWorkspace(); loadRequests()
            observeLocalWork()
            syncQueuedMutations()
            downloadForOffline()
            startLiveUpdates()
            startNotificationStream()
        } catch (error: Exception) {
            _state.update { it.copy(busy = false, message = error.message ?: "Could not complete Cloud sign-in", messageIsError = true) }
        }
    }

    private fun normalizeCloudOrigin(raw: String): String {
        val value = raw.trim().removeSuffix("/")
        val uri = runCatching { URI(value) }.getOrElse { throw IllegalArgumentException("Enter a valid Kora Cloud URL") }
        require(uri.scheme in setOf("http", "https") && !uri.host.isNullOrBlank() && uri.userInfo == null && uri.query == null && uri.fragment == null && uri.path.isNullOrBlank()) {
            "Use a Kora Cloud origin such as https://cloud.kora.mradiafrica.com"
        }
        val localHost = uri.host.equals("localhost", true) || uri.host == "127.0.0.1"
        require(uri.scheme == "https" || localHost) { "Use HTTPS except for localhost development" }
        return value
    }

    fun signIn(email: String, password: String) = viewModelScope.launch {
        _state.update { it.copy(busy = true, message = null) }
        try {
            val instance = app.instanceStore.current()
            require(!instance.siteRef.isNullOrBlank()) { "Enter a Site ID before signing in" }
            val loginScope = Scope(instance.id, instance.origin, instance.siteRef, "")
            app.workspace.login(loginScope, email, password)
            val user = parseUser(app.workspace.currentUser(loginScope))
            scope = Scope(instance.id, instance.origin, instance.siteRef, user.name)
            val activeScope = scope!!
            app.activateScope(activeScope)
            app.workspace.clearDrafts(activeScope)
            app.getSharedPreferences("kora_session", 0).edit().putString("last_user", user.name).putString("last_email", user.email).putString("last_full_name", user.fullName).apply()
            _state.update { it.copy(stage = AppStage.WORKSPACE, user = user, busy = false, message = null, offline = false) }
            loadWorkspace(); loadRequests(); observeLocalWork(); if (!_state.value.offline) syncQueuedMutations()
            downloadForOffline()
            startLiveUpdates()
            startNotificationStream()
        } catch (error: Exception) { _state.update { it.copy(busy = false, message = error.message ?: "Sign in failed") } }
    }

    fun selectSection(section: String) {
        _state.update { it.copy(section = section, selectedModule = null, selectedDoctype = null, selectedRecord = null, editingRecord = null, formOpen = false, formValues = emptyMap(), settingsPage = null, listSelectionActive = false, listSelectionCount = 0) }
        when (section) { "Requests" -> loadRequests(); "Notifications" -> loadNotifications(); "Work" -> loadWorkspace(); "POS" -> loadPosCatalog(); "Reports" -> loadAnalytics(); "Sync" -> if (!_state.value.offline) syncQueuedMutations(showMessage = true) }
    }

    fun setThemePreference(preference: String) {
        val normalized = preference.lowercase().takeIf { it in setOf("light", "dark", "site") } ?: "site"
        val active = scope
        val key = "appearance:${active?.siteRef.orEmpty()}"
        app.getSharedPreferences("kora_mobile_navigation", 0).edit().apply {
            if (normalized == "site") remove(key) else putString(key, normalized)
        }.apply()
        val siteMode = (_state.value.tenantBranding?.defaultMode ?: "light").lowercase()
        _state.update { it.copy(themePreference = normalized, themeMode = if (normalized == "site") siteMode else normalized) }
    }

    fun openSettingsPage(page: String? = null) {
        _state.update { it.copy(section = "Settings", settingsPage = page, selectedModule = null, selectedDoctype = null, selectedRecord = null, editingRecord = null, formOpen = false, formValues = emptyMap(), listSelectionActive = false, listSelectionCount = 0, message = null, selectedAgentPolicy = null, savedAgentPolicySnapshot = null, agentPolicyError = null) }
        if (page == "Change history") loadConfigVersions()
        if (page == "Team") loadSettingsUsers()
        if (page == "Access") loadAccessSettings()
        if (page == "Insights") loadAnalytics()
        if (page == "Tables") loadSettingsDoctypes()
        if (page == "Automations") loadWorkflowDefinitions()
        if (page == "AI agents") loadAgentPolicies()
        if (page != null && page in setOf("Scripts", "Integrations", "Credentials", "Templates")) loadSettingsResource(page)
    }

    fun loadSettingsDoctypes() = viewModelScope.launch {
        val active = scope ?: return@launch
        _state.update { it.copy(settingsDoctypesLoading = true, settingsDoctypesError = null) }
        try {
            val result = app.workspace.administrativeDoctypes(active)
            _state.update { it.copy(settingsDoctypes = (0 until result.length()).mapNotNull(result::optJSONObject), settingsDoctypesLoading = false) }
        } catch (error: Exception) {
            _state.update { it.copy(settingsDoctypesLoading = false, settingsDoctypesError = error.message ?: "Could not load record types") }
        }
    }

    fun createSettingsDoctype(name: String, module: String) = saveSettingsDoctype(name, module, null)

    fun duplicateSettingsDoctype(source: JSONObject, name: String) = saveSettingsDoctype(
        name = name,
        module = source.optString("module"),
        source = source,
    )

    private fun saveSettingsDoctype(name: String, module: String, source: JSONObject?) = viewModelScope.launch {
        val active = scope ?: return@launch
        val cleanName = name.trim()
        if (cleanName.isBlank() || module.isBlank()) {
            _state.update { it.copy(settingsDoctypeActionError = "Enter a table name and choose a module.") }
            return@launch
        }
        _state.update { it.copy(settingsDoctypeSaving = true, settingsDoctypeActionError = null, settingsDoctypeActionMessage = null) }
        try {
            val payload = source?.let { JSONObject(it.toString()) } ?: JSONObject()
            val resource = cleanName.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
            payload.put("name", cleanName).put("resource_name", resource).put("module", module)
            if (source == null) {
                payload.put("title_field", "title")
                    .put("sort_field", "modified").put("sort_order", "DESC")
                    .put("search_fields", "title").put("description", "")
                    .put("is_submittable", false).put("is_child_table", false).put("is_single", false)
                    .put("track_changes", true)
                    .put("fields", JSONArray().put(JSONObject()
                        .put("fieldname", "title").put("fieldtype", "Data").put("label", "Title")
                        .put("reqd", true).put("in_list_view", true).put("search_index", true)
                        .put("options", "").put("unique", false).put("default", "").put("hidden", false)
                        .put("read_only", false).put("bold", false).put("in_standard_filter", false)
                        .put("description", "").put("depends_on", "").put("mandatory_depends_on", "")))
                    .put("doc_constraints", JSONArray())
            }
            app.workspace.createAdministrativeDoctype(active, payload)
            _state.update { it.copy(settingsDoctypeSaving = false, settingsDoctypeActionMessage = if (source == null) "Table created" else "Table duplicated") }
            loadSettingsDoctypes()
            loadWorkspace()
        } catch (error: Exception) {
            _state.update { it.copy(settingsDoctypeSaving = false, settingsDoctypeActionError = error.message ?: "Could not save table") }
        }
    }

    fun deleteSettingsDoctype(name: String) = viewModelScope.launch {
        val active = scope ?: return@launch
        _state.update { it.copy(settingsDoctypeSaving = true, settingsDoctypeActionError = null, settingsDoctypeActionMessage = null) }
        try {
            app.workspace.deleteAdministrativeDoctype(active, name)
            _state.update { it.copy(settingsDoctypeSaving = false, settingsDoctypeActionMessage = "Table deleted") }
            loadSettingsDoctypes()
            loadWorkspace()
        } catch (error: Exception) {
            _state.update { it.copy(settingsDoctypeSaving = false, settingsDoctypeActionError = error.message ?: "Could not delete table") }
        }
    }

    fun loadSettingsResource(page: String) = viewModelScope.launch {
        val active = scope ?: return@launch
        _state.update { it.copy(settingsResourcesLoading = page, settingsResourcesError = it.settingsResourcesError - page) }
        try {
            val result = when (page) {
                "Scripts" -> app.workspace.administrativeScripts(active)
                "Integrations" -> app.workspace.administrativeExtensions(active)
                "Credentials" -> app.workspace.administrativeSecrets(active)
                "Templates" -> app.workspace.administrativePageManifests(active)
                else -> JSONArray()
            }
            _state.update {
                it.copy(
                    settingsResources = it.settingsResources + (page to (0 until result.length()).mapNotNull(result::optJSONObject)),
                    settingsResourcesLoading = null,
                )
            }
        } catch (error: Exception) {
            _state.update { it.copy(settingsResourcesLoading = null, settingsResourcesError = it.settingsResourcesError + (page to (error.message ?: "Could not load $page"))) }
        }
    }

    fun loadWorkflowDefinitions() = viewModelScope.launch {
        val active = scope ?: return@launch
        _state.update { it.copy(workflowDefinitionsLoading = true, workflowDefinitionsError = null) }
        try {
            val definitions = app.workspace.workflowDefinitions(active)
            _state.update { it.copy(workflowDefinitions = (0 until definitions.length()).mapNotNull(definitions::optJSONObject), workflowDefinitionsLoading = false) }
        } catch (error: Exception) {
            _state.update { it.copy(workflowDefinitionsLoading = false, workflowDefinitionsError = error.message ?: "Could not load automations") }
        }
    }

    fun loadAgentPolicies() = viewModelScope.launch {
        val active = scope ?: return@launch
        _state.update { it.copy(agentPoliciesLoading = true, agentPoliciesError = null) }
        try {
            val policies = app.workspace.agentPolicies(active)
            _state.update { it.copy(agentPolicies = (0 until policies.length()).mapNotNull(policies::optJSONObject), agentPoliciesLoading = false) }
        } catch (error: Exception) {
            _state.update { it.copy(agentPoliciesLoading = false, agentPoliciesError = error.message ?: "Could not load agent policies") }
        }
    }

    fun openAgentPolicy(reference: JSONObject) = viewModelScope.launch {
        val active = scope ?: return@launch
        val id = reference.optString("id")
        if (id.isBlank()) return@launch
        _state.update { it.copy(agentPolicyLoading = true, agentPolicyError = null, selectedAgentPolicy = JSONObject(reference.toString())) }
        try {
            val policy = app.workspace.agentPolicy(active, id)
            _state.update { it.copy(selectedAgentPolicy = policy, savedAgentPolicySnapshot = policy.toString(), agentPolicyLoading = false) }
            loadAgentPolicyResources(active, id)
        } catch (error: Exception) {
            _state.update { it.copy(agentPolicyLoading = false, agentPolicyError = error.message ?: "Could not load agent policy") }
        }
    }

    fun createAgentPolicy(name: String) = viewModelScope.launch {
        val active = scope ?: return@launch
        val cleanName = name.trim()
        if (cleanName.isBlank()) return@launch
        val id = java.util.UUID.randomUUID().toString()
        val manifest = JSONObject()
            .put("id", id)
            .put("name", cleanName)
            .put("roles", JSONArray())
            .put("direct_permissions", JSONArray())
            .put("denied_permissions", JSONArray())
            .put("approval_rules", JSONArray())
            .put("granted_capabilities", JSONArray())
        _state.update {
            it.copy(
                selectedAgentPolicy = manifest,
                savedAgentPolicySnapshot = null,
                agentEffectivePermissions = emptyList(),
                agentRuns = emptyList(),
                agentPolicyError = null,
                agentPolicyLoading = true,
            )
        }
        try {
            val roles = app.workspace.accessRoles(active)
            val permissions = app.workspace.agentPermissionCatalog(active)
            _state.update { current ->
                if (current.selectedAgentPolicy?.optString("id") != id) current
                else current.copy(
                    agentPolicyLoading = false,
                    accessRoles = (0 until roles.length()).mapNotNull(roles::optJSONObject),
                    agentPermissionCatalog = (0 until permissions.length()).mapNotNull(permissions::optJSONObject),
                )
            }
        } catch (error: Exception) {
            _state.update { it.copy(agentPolicyLoading = false, agentPolicyError = error.message ?: "Could not load agent policy options") }
        }
    }

    fun updateAgentRoles(roles: List<String>) = _state.update { current ->
        val selected = current.selectedAgentPolicy ?: return@update current
        val updated = JSONObject(selected.toString()).put("roles", JSONArray(roles.distinct()))
        current.copy(selectedAgentPolicy = updated)
    }

    fun setKoriEnabledInApp(enabled: Boolean) {
        app.getSharedPreferences("kora_mobile_navigation", 0).edit().putBoolean("show_kori", enabled).apply()
        _state.update { it.copy(koriEnabledInApp = enabled) }
    }

    fun saveAgentPolicy() = viewModelScope.launch {
        val active = scope ?: return@launch
        val policy = _state.value.selectedAgentPolicy ?: return@launch
        val id = policy.optString("id")
        if (id.isBlank() || policy.optString("name").isBlank()) {
            _state.update { it.copy(agentPolicyError = "Agent name is required") }
            return@launch
        }
        _state.update { it.copy(agentPolicySaving = true, agentPolicyError = null) }
        try {
            val saved = app.workspace.saveAgentPolicy(active, id, JSONObject(policy.toString()))
            _state.update { current ->
                current.copy(
                    agentPolicySaving = false,
                    selectedAgentPolicy = saved,
                    savedAgentPolicySnapshot = saved.toString(),
                    agentPolicies = (current.agentPolicies.filterNot { it.optString("id") == id } + saved),
                )
            }
            loadAgentPolicyResources(active, id)
        } catch (error: Exception) {
            _state.update { it.copy(agentPolicySaving = false, agentPolicyError = error.message ?: "Could not save agent policy") }
        }
    }

    fun closeAgentPolicy() = _state.update {
        it.copy(selectedAgentPolicy = null, savedAgentPolicySnapshot = null, agentEffectivePermissions = emptyList(), agentRuns = emptyList(), agentPolicyError = null)
    }

    fun discardAgentPolicyChanges() = _state.update { current ->
        val saved = current.savedAgentPolicySnapshot ?: return@update current.copy(
            selectedAgentPolicy = null,
            agentPolicyError = null,
        )
        val restored = runCatching { JSONObject(saved) }.getOrNull() ?: return@update current
        current.copy(selectedAgentPolicy = restored, agentPolicyError = null)
    }

    private suspend fun loadAgentPolicyResources(active: Scope, id: String) {
        val results = kotlinx.coroutines.coroutineScope {
            val roles = async { app.workspace.accessRoles(active) }
            val permissions = async { app.workspace.agentPermissionCatalog(active) }
            val effective = async { app.workspace.agentEffectivePermissions(active, id) }
            val runs = async { app.workspace.agentRuns(active, id) }
            listOf(roles.await(), permissions.await(), effective.await(), runs.await())
        }
        _state.update { current ->
            if (current.selectedAgentPolicy?.optString("id") != id) current
            else current.copy(
                accessRoles = (0 until results[0].length()).mapNotNull(results[0]::optJSONObject),
                agentPermissionCatalog = (0 until results[1].length()).mapNotNull(results[1]::optJSONObject),
                agentEffectivePermissions = (0 until results[2].length()).mapNotNull(results[2]::optJSONObject),
                agentRuns = (0 until results[3].length()).mapNotNull(results[3]::optJSONObject),
            )
        }
    }

    fun openWorkflowDefinition(reference: JSONObject) = viewModelScope.launch {
        val active = scope ?: return@launch
        _state.update { it.copy(selectedWorkflowLoading = true, selectedWorkflowError = null, selectedWorkflow = null) }
        try {
            val id = reference.optString("definition_id")
            val version = reference.optInt("version")
            val detail = app.workspace.workflowDefinition(active, id, version)
            _state.update { it.copy(selectedWorkflow = detail.optJSONObject("definition") ?: detail, selectedWorkflowLoading = false) }
        } catch (error: Exception) {
            _state.update { it.copy(selectedWorkflowLoading = false, selectedWorkflowError = error.message ?: "Could not load automation") }
        }
    }

    fun closeWorkflowDefinition() { _state.update { it.copy(selectedWorkflow = null, selectedWorkflowError = null) } }

    fun loadAccessSettings() = viewModelScope.launch {
        val active = scope ?: return@launch
        _state.update { it.copy(accessLoading = true, accessError = null) }
        try {
            val roles = app.workspace.accessRoles(active)
            val permissions = app.workspace.accessPermissions(active)
            val roleRows = (0 until roles.length()).mapNotNull(roles::optJSONObject)
            val permissionRows = (0 until permissions.length()).mapNotNull(permissions::optJSONObject)
            val preferredRole = _state.value.selectedAccessRole.takeIf { selected -> roleRows.any { it.optString("name") == selected } }
                ?: _state.value.user?.roles?.firstOrNull()?.takeIf { selected -> roleRows.any { it.optString("name") == selected } }
                ?: roleRows.firstOrNull()?.optString("name").orEmpty()
            _state.update { it.copy(accessRoles = roleRows, accessPermissions = permissionRows, savedAccessPermissions = permissionRows.map { row -> JSONObject(row.toString()) }, selectedAccessRole = preferredRole, accessLoading = false) }
        } catch (error: Exception) {
            _state.update { it.copy(accessLoading = false, accessError = error.message ?: "Could not load access policies") }
        }
    }

    fun selectAccessRole(role: String) { _state.update { it.copy(selectedAccessRole = role) } }

    fun createAccessRole(name: String, workspaceAccess: Boolean, description: String = "") = viewModelScope.launch {
        val active = scope ?: return@launch
        _state.update { it.copy(accessSaving = true, message = null) }
        try {
            app.workspace.createAccessRole(active, name.trim(), workspaceAccess, description.trim())
            _state.update { it.copy(accessSaving = false, selectedAccessRole = name.trim(), message = "Role created") }
            loadAccessSettings()
        } catch (error: Exception) {
            _state.update { it.copy(accessSaving = false, message = error.message ?: "Could not create role") }
        }
    }

    fun updateAccessPermission(doctype: String, operation: String, allowed: Boolean) {
        val current = _state.value
        val existing = current.accessPermissions.firstOrNull { it.optString("doctype") == doctype && it.optString("role") == current.selectedAccessRole }
        val updated = (existing?.let { JSONObject(it.toString()) } ?: JSONObject().put("doctype", doctype).put("role", current.selectedAccessRole))
            .put(operation, allowed)
        val next = current.accessPermissions.filterNot { it.optString("doctype") == doctype && it.optString("role") == current.selectedAccessRole } + updated
        _state.update { it.copy(accessPermissions = next, message = null) }
    }

    fun discardAccessChanges() {
        _state.update { it.copy(accessPermissions = it.savedAccessPermissions.map { row -> JSONObject(row.toString()) }, message = null) }
    }

    fun saveAccessChanges() = viewModelScope.launch {
        val active = scope ?: return@launch
        _state.update { it.copy(accessSaving = true, message = null) }
        try {
            val rows = JSONArray()
            _state.value.accessPermissions.forEach { row -> rows.put(JSONObject(row.toString())) }
            app.workspace.saveAccessPermissions(active, rows)
            val saved = _state.value.accessPermissions.map { row -> JSONObject(row.toString()) }
            _state.update { it.copy(savedAccessPermissions = saved, accessSaving = false, message = "Access saved") }
        } catch (error: Exception) {
            _state.update { it.copy(accessSaving = false, message = error.message ?: "Could not save access") }
        }
    }

    fun loadSettingsUsers() = viewModelScope.launch {
        val active = scope ?: return@launch
        _state.update { it.copy(settingsUsersLoading = true, settingsUsersError = null) }
        try {
            val users = app.workspace.administrativeUsers(active)
            _state.update { it.copy(settingsUsers = (0 until users.length()).mapNotNull(users::optJSONObject), settingsUsersLoading = false) }
        } catch (error: Exception) {
            _state.update { it.copy(settingsUsersLoading = false, settingsUsersError = error.message ?: "Could not load workspace users") }
        }
    }

    fun loadConfigVersions() = viewModelScope.launch {
        val active = scope ?: return@launch
        _state.update { it.copy(configVersionsLoading = true, configVersionsError = null) }
        try {
            val result = app.workspace.configVersions(active)
            _state.update { it.copy(configVersions = (0 until result.length()).mapNotNull(result::optJSONObject), configVersionsLoading = false) }
        } catch (error: Exception) {
            _state.update { it.copy(configVersionsLoading = false, configVersionsError = error.message ?: "Could not load configuration history") }
        }
    }

    fun reviewConfigVersion(version: JSONObject) = viewModelScope.launch {
        val active = scope ?: return@launch
        _state.update { it.copy(configVersionDetailLoading = true, configVersionDetail = null) }
        try {
            val result = app.workspace.configVersion(active, version.optString("id"))
            _state.update { it.copy(configVersionDetailLoading = false, configVersionDetail = result) }
        } catch (error: Exception) {
            _state.update { it.copy(configVersionDetailLoading = false, message = error.message ?: "Could not load this version") }
        }
    }

    fun closeConfigVersionReview() { _state.update { it.copy(configVersionDetail = null, message = null) } }

    fun updateSettingsCurrency(value: String) { _state.update { it.copy(currency = value.trim().uppercase(), message = null) } }

    fun updateSettingsBranding(transform: (Branding) -> Branding) {
        _state.update { state ->
            val current = state.brandingDraft ?: state.tenantBranding ?: Branding()
            state.copy(brandingDraft = transform(current), message = null)
        }
    }

    fun discardSettingsChanges() {
        _state.update { state ->
            state.copy(currency = state.savedCurrency, brandingDraft = state.savedBranding ?: state.tenantBranding, message = null)
        }
    }

    fun saveSettings() = viewModelScope.launch {
        val active = scope ?: return@launch
        val snapshot = _state.value
        val currency = snapshot.currency
        val branding = snapshot.brandingDraft ?: snapshot.tenantBranding
        _state.update { it.copy(settingsSaving = true, message = null) }
        try {
            if (currency != snapshot.savedCurrency) {
                val saved = app.workspace.updateSettings(active, currency)
                val data = saved.optJSONObject("data") ?: saved
                val canonicalCurrency = data.optString("currency").ifBlank { currency }
                _state.update { it.copy(currency = canonicalCurrency, savedCurrency = canonicalCurrency) }
            }
            if (branding != null && branding != snapshot.savedBranding) {
                val copy = experienceCopy ?: throw IllegalStateException("Workspace experience settings are unavailable. Reload and try again.")
                val saved = app.workspace.updateExperience(active, branding.toJson(), JSONObject(copy.toString()))
                val data = saved.optJSONObject("data") ?: saved
                val brandingJson = data.optJSONObject("branding") ?: data
                val canonicalBranding = parseBranding(brandingJson)
                data.optJSONObject("copy")?.let { experienceCopy = JSONObject(it.toString()) }
                _state.update { state ->
                    state.copy(
                        tenantBranding = canonicalBranding,
                        brandingDraft = canonicalBranding,
                        savedBranding = canonicalBranding,
                        navigation = state.navigation?.copy(branding = canonicalBranding),
                        themeMode = if (state.themePreference == "site") canonicalBranding.defaultMode ?: state.themeMode else state.themeMode,
                    )
                }
            }
            _state.update { it.copy(settingsSaving = false, message = "Settings saved") }
        } catch (error: Exception) {
            _state.update { it.copy(settingsSaving = false, message = error.message ?: "Could not save settings") }
        }
    }

    fun sendAssistantMessage(message: String) {
        val clean = message.trim()
        val active = scope ?: return
        if (clean.isBlank() || _state.value.assistantBusy) return
        val snapshot = _state.value
        _state.update { it.copy(assistantTurns = it.assistantTurns + AssistantTurn("user", clean), assistantBusy = true, assistantError = null) }
        viewModelScope.launch {
            try {
                val path = when {
                    snapshot.selectedDoctype != null -> "/workspace/${snapshot.selectedDoctype.resourceName}"
                    snapshot.section == "POS" -> "/workspace/pos"
                    else -> "/workspace/${snapshot.section.lowercase()}"
                }
                val response = app.workspace.aiChat(active, clean, path, snapshot.selectedDoctype?.name)
                val reply = response.optString("reply").ifBlank { "Kori could not return a response for this message." }
                _state.update { it.copy(assistantTurns = it.assistantTurns + AssistantTurn("assistant", reply), assistantBusy = false) }
            } catch (error: Exception) {
                _state.update { it.copy(assistantBusy = false, assistantError = error.message ?: "Kori could not connect right now.") }
            }
        }
    }

    fun openModule(module: String) {
        if (_state.value.navigation?.modules?.any { it.module == module } != true) return
        _state.update { it.copy(section = "Home", selectedModule = module, selectedDoctype = null, selectedRecord = null, editingRecord = null, formOpen = false, formValues = emptyMap(), message = null) }
    }

    fun closeModule() = _state.update { it.copy(selectedModule = null) }

    fun downloadForOffline() = viewModelScope.launch {
        val active = scope ?: return@launch
        if (!_state.value.offlineCoverage.downloading) app.workspace.prepareOffline(active)
    }

    fun reviewSyncIssue(mutation: LocalMutation) = viewModelScope.launch {
        val active = scope ?: return@launch
        val item = _state.value.navigation?.modules?.flatMap { it.doctypes }?.firstOrNull { it.resourceName == mutation.doctype } ?: return@launch
        openDoctype(item).join()
        val record = (mutation.localId ?: mutation.recordName)?.let { app.workspace.record(active, mutation.doctype, it) } ?: return@launch
        editRecord(record)
        _state.update { it.copy(resolvingMutationId = mutation.id, message = mutation.lastError ?: "Review and save your correction") }
    }

    fun acceptServerVersion(mutation: LocalMutation) = viewModelScope.launch {
        val active = scope ?: return@launch
        app.workspace.localRecords.acceptServer(active, mutation.id)
        syncQueuedMutations()
    }

    fun discardAllPendingChanges() = viewModelScope.launch {
        val active = scope ?: return@launch
        _state.update { it.copy(discardingMutations = true, message = null) }
        try {
            val discarded = app.workspace.localRecords.discardAllPending(active)
            observeLocalWork()
            _state.update {
                it.copy(
                    discardingMutations = false,
                    offline = app.workspace.offline.value,
                    message = if (discarded == 0) "There were no pending changes to discard."
                    else "Discarded $discarded unsynced change${if (discarded == 1) "" else "s"}. Server records were not changed.",
                )
            }
            if (_state.value.posManifest != null) loadPosCatalog()
        } catch (error: Exception) {
            _state.update { it.copy(discardingMutations = false, message = error.message ?: "Could not clear pending changes") }
        }
    }

    fun selectSavedReport(name: String) {
        _state.update { it.copy(selectedReportName = name, reportError = null) }
        runAnalyticsQuery()
    }

    fun selectAnalyticsMeasure(model: String, measure: String) {
        _state.update { it.copy(selectedReportName = "", selectedAnalyticsModel = model, selectedAnalyticsMeasure = measure, reportError = null) }
        runAnalyticsQuery()
    }

    fun setReportWindow(days: Int) {
        _state.update { it.copy(reportWindowDays = days, reportError = null) }
        runAnalyticsQuery()
    }

    fun refreshAnalytics() = runAnalyticsQuery()

    private fun loadAnalytics() = viewModelScope.launch {
        val active = scope ?: return@launch
        _state.update { it.copy(reportLoading = true, reportError = null) }
        try {
            val catalog = app.workspace.analyticsCatalog(active)
            val catalogCached = app.workspace.lastResponseWasCached
            val saved = app.workspace.analyticsReports(active)
            val reportsCached = app.workspace.lastResponseWasCached
            val models = catalog.optJSONArray("models") ?: JSONArray()
            val firstModel = models.optJSONObject(0) ?: JSONObject()
            val firstMeasure = firstModel.optJSONArray("measures")?.optJSONObject(0) ?: JSONObject()
            val defaultReport = saved.optJSONObject(0)?.optString("name").orEmpty()
            _state.update { it.copy(
                analyticsCatalog = catalog,
                savedReports = (0 until saved.length()).mapNotNull(saved::optJSONObject),
                selectedReportName = it.selectedReportName.ifBlank { defaultReport },
                selectedAnalyticsModel = it.selectedAnalyticsModel.ifBlank { firstModel.optString("name") },
                selectedAnalyticsMeasure = it.selectedAnalyticsMeasure.ifBlank { firstMeasure.optString("name") },
                offline = it.offline || catalogCached || reportsCached,
            ) }
            runAnalyticsQuery(active)
        } catch (error: Exception) {
            _state.update { it.copy(reportLoading = false, reportError = error.message ?: "Could not load reporting") }
        }
    }

    private fun runAnalyticsQuery() = viewModelScope.launch { runAnalyticsQuery(scope ?: return@launch) }

    private suspend fun runAnalyticsQuery(active: Scope) {
        val current = _state.value
        _state.update { it.copy(reportLoading = true, reportError = null) }
        try {
            val end = LocalDate.now()
            val start = end.minusDays((current.reportWindowDays - 1).toLong())
            val time = JSONObject().put("from", start.toString()).put("to", end.toString()).put("granularity", "day")
            val saved = current.savedReports.firstOrNull { it.optString("name") == current.selectedReportName }
            val result = if (saved != null) {
                app.workspace.executeReport(active, saved.optString("name"), JSONObject().put("time", time).put("queries", JSONArray()))
                    .optJSONObject("result") ?: JSONObject()
            } else {
                val query = JSONObject()
                    .put("model", current.selectedAnalyticsModel)
                    .put("measures", JSONArray().put(current.selectedAnalyticsMeasure))
                val model = current.analyticsCatalog?.optJSONArray("models")?.let { models -> (0 until models.length()).mapNotNull(models::optJSONObject).firstOrNull { it.optString("name") == current.selectedAnalyticsModel } }
                model?.optString("time_dimension")?.takeIf(String::isNotBlank)?.let { query.put("dimensions", JSONArray().put(it)) }
                app.workspace.queryAnalytics(active, JSONObject().put("queries", JSONArray().put(query)).put("time", time).put("limit", 100))
            }
            val cached = app.workspace.lastResponseWasCached
            _state.update { it.copy(reportResult = result, reportLoading = false, reportError = null, offline = it.offline || cached) }
        } catch (error: Exception) {
            _state.update { it.copy(reportLoading = false, reportError = error.message ?: "Could not run this report") }
        }
    }

    private fun loadPosCatalog() = viewModelScope.launch {
        val active = scope ?: return@launch
        _state.update { it.copy(message = null, posLoading = true) }
        try {
            val manifest = _state.value.posManifest ?: app.workspace.pageManifest(active, "/pos")
            val spec = manifest.optJSONObject("spec") ?: JSONObject()
            val components = spec.optJSONObject("layout")?.optJSONArray("children") ?: JSONArray()
            val componentRows = (0 until components.length()).mapNotNull { components.optJSONObject(it) }
            val productComponent = componentRows.firstOrNull { it.optString("component") == "product_grid" }
            val sourceDoctype = resolveComponentResourceDoctype(spec, productComponent).orEmpty()
            if (sourceDoctype.isBlank()) {
                _state.update { it.copy(posLoading = false, message = "This view has no configured record source.") }
                return@launch
            }
            val categoryComponent = (0 until components.length()).mapNotNull { components.optJSONObject(it) }.firstOrNull { it.optString("component") == "category_tabs" }
            val paymentComponent = componentRows.firstOrNull { it.optString("component") == "payment_panel" }
            val paymentActions = paymentComponent?.optJSONObject("props")?.optJSONArray("actions") ?: JSONArray()
            val sessionPrefs = app.getSharedPreferences("kora_pos_sessions", 0)
            val sessionKey = posSessionKey(active)
            val savedRecords = runCatching { JSONObject(sessionPrefs.getString("$sessionKey:records", "{}")) }.getOrDefault(JSONObject())
            val prerequisiteRecords = buildMap {
                for (index in 0 until paymentActions.length()) {
                    val setupAction = paymentActions.optJSONObject(index) ?: continue
                    val contextField = setupAction.optJSONObject("config")?.optString("context_field").orEmpty()
                    if (setupAction.optString("type") == "create_record" && contextField.isNotBlank()) {
                        savedRecords.optString(contextField).takeIf(String::isNotBlank)?.let { put(contextField, it) }
                    }
                }
            }
            val prerequisiteInputs = runCatching { JSONObject(sessionPrefs.getString("$sessionKey:inputs", "{}")) }.getOrDefault(JSONObject())
            val restoredInputs = buildMap {
                val keys = prerequisiteInputs.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    prerequisiteInputs.optString(key).takeIf(String::isNotBlank)?.let { put(key, it) }
                }
            }
            val componentTransactionConfig = paymentComponent?.optJSONObject("props")?.optJSONArray("actions")?.let { actions ->
                (0 until actions.length()).mapNotNull { actions.optJSONObject(it) }
                    .firstOrNull { it.optString("type") == "create_transaction" }
                    ?.optJSONObject("config")
            }
            val manifestTransactionConfig = spec.optJSONArray("actions")?.let { actions ->
                (0 until actions.length()).mapNotNull { actions.optJSONObject(it) }
                    .firstOrNull { it.optString("command") == "create_transaction" }
                    ?.optJSONObject("input")
            }
            val transactionConfig = componentTransactionConfig ?: manifestTransactionConfig
            val lineTable = transactionConfig?.optString("child_table").orEmpty()
            val targetDoctype = transactionConfig?.optString("target_doctype").orEmpty()
            var lineLabel = "Lines"
            var lineSchemaCached = false
            if (lineTable.isNotBlank() && targetDoctype.isNotBlank()) {
                val targetSchema = app.workspace.schema(active, resourceRoute(targetDoctype))
                lineSchemaCached = app.workspace.lastResponseWasCached
                lineLabel = parseFields(targetSchema)
                    .firstOrNull { it.fieldtype == "Table" && it.options == lineTable }
                    ?.label?.takeIf(String::isNotBlank) ?: lineLabel
            }
            val configuredMethods = paymentComponent?.optJSONObject("props")?.optJSONObject("bindings")
                ?.optString("methods").orEmpty().removePrefix("literal:")
                .split(',').map { it.trim().lowercase() }.filter(String::isNotBlank)
            val existingMethod = _state.value.posPaymentMethod
            val selectedMethod = existingMethod.takeIf { it in configuredMethods }
                ?: configuredMethods.firstOrNull().orEmpty()
            val productRoute = resourceRoute(sourceDoctype)
            val products = app.workspace.records(active, productRoute)
            val productsCached = app.workspace.lastResponseWasCached
            val productSchema = app.workspace.schema(active, productRoute)
            val productSchemaCached = app.workspace.lastResponseWasCached
            val productRows = (0 until products.length()).mapNotNull { products.optJSONObject(it) }
            val productFields = parseFields(productSchema)
            val groupField = categoryComponent?.optJSONObject("props")?.optJSONObject("bindings")?.optString("group_field").orEmpty()
                .ifBlank { productComponent?.optJSONObject("props")?.optJSONObject("bindings")?.optString("subtitle").orEmpty() }
            val groupSchema = productFields.firstOrNull { it.fieldname == groupField }
            val categoryDoctype = groupSchema?.takeIf { it.fieldtype == "Link" }?.options.orEmpty()
            val categoryNames: Map<String, String>
            var categoriesCached = false
            var categorySchemaCached = false
            if (categoryDoctype.isNotBlank()) {
                val categoryRoute = resourceRoute(categoryDoctype)
                val categoryRows = app.workspace.records(active, categoryRoute)
                categoriesCached = app.workspace.lastResponseWasCached
                val categorySchema = app.workspace.schema(active, categoryRoute)
                categorySchemaCached = app.workspace.lastResponseWasCached
                val titleField = resolveTitleField(categorySchema)
                categoryNames = (0 until categoryRows.length()).mapNotNull { categoryRows.optJSONObject(it) }
                    .associate { category -> category.optString("name") to category.optString(titleField).ifBlank { category.optString("name") } }
            } else {
                categoryNames = productRows.mapNotNull { row -> row.optString(groupField).takeIf(String::isNotBlank) }.distinct().associateWith { it }
            }
            _state.update { it.copy(posManifest = manifest, posProducts = productRows, posCategories = categoryNames, posLineLabel = lineLabel, posPaymentMethod = selectedMethod, posPrerequisiteRecords = prerequisiteRecords, posPrerequisiteInputs = restoredInputs, posLoading = false, offline = productsCached || productSchemaCached || categoriesCached || categorySchemaCached || lineSchemaCached, message = null) }
        } catch (error: Exception) {
            _state.update { it.copy(posLoading = false, offline = true, message = error.message ?: "Could not load the configured records") }
        }
    }

    fun refreshPosCatalog() = loadPosCatalog()

    fun addPosProduct(product: JSONObject) {
        val key = product.optString("name")
        val component = posComponent("product_grid")
        val bindings = component?.optJSONObject("props")?.optJSONObject("bindings") ?: JSONObject()
        val stockField = bindings.optString("badge")
        if (key.isBlank() || (stockField.isNotBlank() && product.optInt(stockField, 1) <= 0)) return
        _state.update { current ->
            val old = current.posCart[key]
            val maxStock = if (stockField.isBlank()) Int.MAX_VALUE else product.optInt(stockField, Int.MAX_VALUE).coerceAtLeast(1)
            val quantity = ((old?.quantity ?: 0) + 1).coerceAtMost(maxStock)
            current.copy(posCart = current.posCart + (key to PosCartLine(product, quantity)), message = null)
        }
    }

    fun changePosQuantity(productName: String, delta: Int) {
        _state.update { current ->
            val line = current.posCart[productName] ?: return@update current
            val next = line.quantity + delta
            if (next <= 0) current.copy(posCart = current.posCart - productName)
            else {
                val bindings = posComponent("product_grid")?.optJSONObject("props")?.optJSONObject("bindings") ?: JSONObject()
                val stockField = bindings.optString("badge")
                val maxStock = if (stockField.isBlank()) Int.MAX_VALUE else line.product.optInt(stockField, Int.MAX_VALUE).coerceAtLeast(0)
                if (next > maxStock) current.copy(message = "Only $maxStock available for ${line.product.optString(bindings.optString("title").ifBlank { "name" }, "this record")}")
                else current.copy(posCart = current.posCart + (productName to line.copy(quantity = next)), message = null)
            }
        }
    }

    fun clearPosCart() { _state.update { it.copy(posCart = emptyMap(), message = null) } }

    fun setPosPaymentMethod(method: String) { _state.update { it.copy(posPaymentMethod = method) } }

    fun setPosPrerequisiteInput(field: String, value: String) {
        val normalized = value
        val active = scope
        if (active != null) {
            val key = posSessionKey(active)
            val inputs = runCatching { JSONObject(app.getSharedPreferences("kora_pos_sessions", 0).getString("$key:inputs", "{}")) }.getOrDefault(JSONObject())
            inputs.put(field, normalized)
            app.getSharedPreferences("kora_pos_sessions", 0).edit().putString("$key:inputs", inputs.toString()).apply()
        }
        _state.update { it.copy(posPrerequisiteInputs = it.posPrerequisiteInputs + (field to normalized)) }
    }

    fun openPosPrerequisites() = viewModelScope.launch {
        val active = scope ?: return@launch
        val current = _state.value
        if (current.offline || !app.workspace.networkAvailable) {
            _state.update { it.copy(message = "Connect to Kora to create the required records. You can continue offline after setup is complete.") }
            return@launch
        }
        val manifest = current.posManifest ?: return@launch _state.update { it.copy(message = "The POS configuration is not loaded") }
        val spec = manifest.optJSONObject("spec") ?: JSONObject()
        val components = spec.optJSONObject("layout")?.optJSONArray("children") ?: JSONArray()
        val payment = (0 until components.length()).mapNotNull(components::optJSONObject).firstOrNull { it.optString("component") == "payment_panel" }
            ?: return@launch _state.update { it.copy(message = "This POS has no payment panel") }
        val componentId = payment.optString("id")
        val viewName = manifest.optJSONObject("metadata")?.optString("name").orEmpty()
        val actions = payment.optJSONObject("props")?.optJSONArray("actions") ?: JSONArray()
        val setupActions = (0 until actions.length()).mapNotNull(actions::optJSONObject)
            .filter { it.optString("type") == "create_record" && it.optJSONObject("config")?.optString("context_field").orEmpty().isNotBlank() }
        if (setupActions.isEmpty()) return@launch _state.update { it.copy(message = "No prerequisite records are configured for this workflow") }
        _state.update { it.copy(posPrerequisitesInProgress = true, message = null) }
        try {
            val prefs = app.getSharedPreferences("kora_pos_sessions", 0)
            val sessionKey = posSessionKey(active)
            val bindings = payment.optJSONObject("props")?.optJSONObject("bindings") ?: JSONObject()
            val records = _state.value.posPrerequisiteRecords.toMutableMap()
            for (setupAction in setupActions) {
                val actionId = setupAction.optString("id")
                val config = setupAction.optJSONObject("config") ?: JSONObject()
                val contextField = config.optString("context_field")
                if (records[contextField].isNullOrBlank()) {
                    val context = JSONObject()
                    val inputs = config.optJSONArray("inputs") ?: JSONArray()
                    val inputValues = JSONObject()
                    for (index in 0 until inputs.length()) {
                        val input = inputs.optJSONObject(index) ?: continue
                        val inputName = input.optString("field")
                        if (inputName.isBlank()) continue
                        val rawInput = _state.value.posPrerequisiteInputs[inputName].orEmpty().ifBlank { input.optString("default") }
                        if (input.optBoolean("required", false) && rawInput.isBlank()) error("${input.optString("label").ifBlank { inputName }} is required")
                        val value: Any? = if (input.optString("fieldtype") in setOf("Int", "Float", "Currency")) rawInput.toBigDecimalOrNull() else rawInput.takeIf(String::isNotBlank)
                        if (value != null) inputValues.put(inputName, value)
                    }
                    context.put("_bindings", JSONObject().apply {
                        val keys = bindings.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            put(key, bindings.optString(key).removePrefix("literal:"))
                        }
                    })
                    context.put("_prerequisites", JSONObject().apply { records.forEach { (key, value) -> put(key, value) } })
                    context.put("_inputs", inputValues)
                    val inputKeys = inputValues.keys()
                    while (inputKeys.hasNext()) {
                        val key = inputKeys.next()
                        context.put(key, inputValues.opt(key))
                    }
                    val response = app.workspace.executeViewAction(active, actionId, viewName, componentId, context, "${actionId}:${context.optString("reference")}")
                    val record = response.optJSONObject("data") ?: response
                    val recordName = record.optString("name").ifBlank { record.optString("reference") }
                    if (recordName.isBlank()) error("Kora did not return the created ${config.optString("label", "record")}")
                    records[contextField] = recordName
                    prefs.edit().putString("$sessionKey:records", JSONObject().apply { records.forEach { (key, value) -> put(key, value) } }.toString()).apply()
                }
            }
            _state.update { it.copy(posPrerequisitesInProgress = false, posPrerequisiteRecords = records, message = "Required records are ready") }
        } catch (error: Exception) {
            _state.update { it.copy(posPrerequisitesInProgress = false, message = error.message ?: "Could not prepare this transaction") }
        }
    }

    fun completePosSale() = viewModelScope.launch {
        val active = scope ?: return@launch
        val current = _state.value
        val lines = current.posCart.values.toList()
        if (lines.isEmpty()) return@launch
        val productComponent = posComponent("product_grid")
        val priceField = productComponent?.optJSONObject("props")?.optJSONObject("bindings")?.optString("price").orEmpty()
        if (priceField.isBlank()) return@launch _state.update { it.copy(message = "This view does not configure an item price field.") }
        val subtotal = lines.fold(java.math.BigDecimal.ZERO) { total, line ->
            total + line.product.optString(priceField, "0").toBigDecimalOrNull().orZero() * line.quantity.toBigDecimal()
        }
        val manifest = current.posManifest ?: return@launch
        val spec = manifest.optJSONObject("spec") ?: JSONObject()
        val components = spec.optJSONObject("layout")?.optJSONArray("children") ?: JSONArray()
        val paymentComponent = (0 until components.length()).mapNotNull { components.optJSONObject(it) }.firstOrNull { it.optString("component") == "payment_panel" }
            ?: return@launch _state.update { it.copy(message = "This POS view has no payment panel configured") }
        val componentId = paymentComponent.optString("id")
        val actions = paymentComponent.optJSONObject("props")?.optJSONArray("actions") ?: JSONArray()
        val saleAction = (0 until actions.length()).mapNotNull { actions.optJSONObject(it) }.firstOrNull {
            it.optString("type") == "create_transaction" && it.optJSONObject("config")?.optString("requires_operation_status").isNullOrBlank()
        } ?: return@launch _state.update { it.copy(message = "This POS view has no direct sale action configured") }
        val actionConfig = saleAction.optJSONObject("config") ?: JSONObject()
        val requiredRelatedRecords = actionConfig.optJSONArray("required_related_status") ?: JSONArray()
        val missingRelatedField = (0 until requiredRelatedRecords.length()).mapNotNull { requiredRelatedRecords.optJSONObject(it)?.optString("record_field") }
            .firstOrNull { current.posPrerequisiteRecords[it].isNullOrBlank() }
        if (missingRelatedField != null) {
            return@launch _state.update { it.copy(message = "Create the required related records before completing this transaction") }
        }
        val bindings = paymentComponent.optJSONObject("props")?.optJSONObject("bindings") ?: JSONObject()
        val methods = bindings.optString("methods").removePrefix("literal:").split(',').map { it.trim().lowercase() }.filter(String::isNotBlank)
        if (current.posPaymentMethod !in methods) return@launch _state.update { it.copy(message = "Choose a payment method configured for this register") }
        val lineFields = actionConfig.optJSONObject("line_fields") ?: JSONObject()
        val lineDefaults = actionConfig.optJSONObject("line_defaults") ?: JSONObject()
        val projectedItems = JSONArray()
        val cartItems = JSONArray().apply {
            lines.forEach { line ->
                val sourceValues = mapOf(
                    "product" to line.product.optString("name"),
                    "rate" to line.product.optString(priceField, "0"),
                    "unit_price" to line.product.optString(priceField, "0"),
                    "quantity" to line.quantity,
                    "discount_amount" to 0,
                    "tax_amount" to 0,
                )
                val projected = JSONObject()
                put(JSONObject().apply {
                    val keys = lineFields.keys()
                    while (keys.hasNext()) {
                        val destination = keys.next()
                        val source = lineFields.optString(destination, destination)
                        sourceValues[source]?.let { value -> put(source, value); projected.put(destination, value) }
                    }
                    val defaults = lineDefaults.keys()
                    while (defaults.hasNext()) {
                        val field = defaults.next()
                        if (!has(field)) put(field, lineDefaults.opt(field))
                        if (!projected.has(field)) projected.put(field, lineDefaults.opt(field))
                    }
                })
                val lineTotal = line.product.optString(priceField, "0").toBigDecimalOrNull().orZero() * line.quantity.toBigDecimal()
                val totalField = actionConfig.optJSONObject("totals")?.optJSONObject("lines")?.optString("line_total_field").orEmpty()
                if (totalField.isNotBlank()) projected.put(totalField, lineTotal.toPlainString())
                projectedItems.put(projected)
            }
        }
        val register = bindings.optString("register").removePrefix("literal:")
        val context = JSONObject()
            .put("cart", cartItems)
            .put("cashier", current.user?.email.orEmpty())
            .put("register", register)
            .put("sale_date", java.time.LocalDate.now().toString())
            .put("entry_source", "POS")
            .put("payment_method", when (current.posPaymentMethod) {
                "cash" -> "Cash"
                "card" -> "Card"
                "mpesa" -> "Mpesa"
                "mobile_money", "mobile" -> "Mobile Money"
                else -> current.posPaymentMethod.replaceFirstChar { it.uppercase() }
            })
            .put("total", subtotal.toPlainString())
            .put("reference", "POS-${java.util.UUID.randomUUID().toString().take(12).uppercase()}")
        for (index in 0 until requiredRelatedRecords.length()) {
            val recordField = requiredRelatedRecords.optJSONObject(index)?.optString("record_field").orEmpty()
            current.posPrerequisiteRecords[recordField]?.let { context.put(recordField, it) }
        }
        actionConfig.optJSONObject("allowed_context_values")?.let { allowed ->
            val fields = allowed.keys()
            while (fields.hasNext()) {
                val field = fields.next()
                val choices = allowed.optJSONArray(field) ?: return@launch _state.update { it.copy(message = "The register rules need to be refreshed") }
                if ((0 until choices.length()).none { choices.optString(it) == context.optString(field) }) {
                    return@launch _state.update { it.copy(message = "This checkout option is not available for this register") }
                }
            }
        }
        val viewName = manifest.optJSONObject("metadata")?.optString("name").orEmpty()
        if (viewName.isBlank() || componentId.isBlank() || saleAction.optString("id").isBlank()) return@launch _state.update { it.copy(message = "The POS view action configuration is incomplete") }
        val actionId = saleAction.optString("id")
        val idempotencyPrefix = actionConfig.optString("idempotency_prefix").ifBlank { "view-action:$actionId" }
        val idempotencyKey = "$idempotencyPrefix:${context.optString("reference")}"
        val actionRequest = JSONObject().put("view", viewName).put("component", componentId).put("context", context)
        _state.update { it.copy(posSaving = true, message = null) }
        val targetDoctype = actionConfig.optString("target_doctype")
        if (targetDoctype.isBlank()) return@launch _state.update { it.copy(message = "This view action does not configure a target record type.") }
        val saleType = resourceRoute(targetDoctype)
        val projection = JSONObject(context.toString()).apply {
            remove("cart")
            put("items", projectedItems)
            put("_payment_state", "Pending confirmation")
            put("_payment_intent", JSONObject().put("amount", subtotal.toPlainString()).put("method", context.optString("payment_method")).put("register", register))
        }
        val quantityConfig = actionConfig.optJSONObject("related_quantity_updates")
            val stockField = quantityConfig?.optString("target_quantity_field")
                ?: productComponent?.optJSONObject("props")?.optJSONObject("bindings")?.optString("badge").orEmpty()
        val stockType = quantityConfig?.optString("target_doctype") ?: productComponent?.optString("source_doctype").orEmpty()
        val effect = if ((quantityConfig != null || actionConfig.optBoolean("decrement_stock")) && stockField.isNotBlank() && stockType.isNotBlank()) {
            JSONObject().put("doctype", resourceRoute(stockType)).put("quantity_field", stockField)
                .put("lines", JSONArray(lines.map { line -> JSONObject().put("name", line.product.optString("name")).put("quantity", line.quantity) }))
        } else null
        queueViewActionForSync(active, actionId, actionRequest, idempotencyKey, saleType, projection, effect)
    }

    private fun java.math.BigDecimal?.orZero() = this ?: java.math.BigDecimal.ZERO

    private suspend fun queueViewActionForSync(active: Scope, actionId: String, request: JSONObject, idempotencyKey: String, saleType: String, projection: JSONObject, effect: JSONObject?) {
        var mutationId: String? = null
        try {
            mutationId = app.workspace.enqueueViewAction(active, actionId, request, idempotencyKey, saleType, projection, effect)
            val online = app.workspace.networkAvailable
            val syncError = if (online) app.workspace.syncQueuedRecords(active, force = true).second else null
            val mutation = app.workspace.localRecords.mutation(active, mutationId)
            val localRecord = app.workspace.localRecords.record(active, saleType, "local:$mutationId")
            val serverName = localRecord?.optString("name")?.takeIf { it.isNotBlank() && !it.startsWith("local:") }
            val products = effect?.optString("doctype")?.takeIf(String::isNotBlank)
                ?.let { type -> runCatching { app.workspace.records(active, type) }.getOrNull() }
            val outcome = when {
                mutation?.status == "acked" && serverName != null -> {
                    val verified = verifyServerSale(active, saleType, serverName)
                    if (verified != null) "Sale confirmed in Kora · ${verified.optString("reference").ifBlank { serverName }}"
                    else "Kora acknowledged the sale, but I could not verify its record just now."
                }
                mutation?.status in setOf("rejected", "conflict", "blocked") ->
                    "Sale was not saved in Kora · ${mutation?.lastError ?: syncError?.message ?: "Review the sync issue"}"
                !online -> "Sale saved on this device · waiting to sync."
                syncError != null -> "Sale saved on this device · waiting to sync. ${syncError.message.orEmpty()}".trim()
                else -> "Sale saved on this device · waiting for Kora confirmation."
            }
            _state.update { current ->
                current.copy(
                    posSaving = false,
                    posCart = emptyMap(),
                    posProducts = products?.let { rows -> (0 until rows.length()).mapNotNull(rows::optJSONObject) } ?: current.posProducts,
                    message = outcome,
                )
            }
            observeLocalWork()
        } catch (error: Exception) {
            observeLocalWork()
            val savedLocally = mutationId != null
            _state.update {
                it.copy(
                    posSaving = false,
                    posCart = if (savedLocally) emptyMap() else it.posCart,
                    message = if (savedLocally) "Sale saved on this device, but Kora has not confirmed it · ${error.message ?: "sync pending"}"
                    else error.message ?: "Could not save sale on this device",
                )
            }
        }
    }

    private fun posSessionKey(active: Scope): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(active.key.toByteArray())
        return digest.take(12).joinToString("") { "%02x".format(it) }
    }

    private suspend fun verifyServerSale(active: Scope, saleType: String, serverName: String): JSONObject? {
        repeat(5) { attempt ->
            if (attempt > 0) delay(attempt * 500L)
            try {
                return app.workspace.fetchServerRecord(active, saleType, serverName)
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // Retry briefly in case the server's list/read projection is catching up.
            }
        }
        return null
    }

    private fun resourceRoute(doctype: String): String = _state.value.navigation?.modules?.flatMap { it.doctypes }
        ?.firstOrNull { it.name.equals(doctype, true) }?.resourceName
        ?: doctype.lowercase().replace(' ', '-')

    private fun posComponent(type: String): JSONObject? {
        val children = _state.value.posManifest?.optJSONObject("spec")?.optJSONObject("layout")?.optJSONArray("children") ?: return null
        return (0 until children.length()).mapNotNull { children.optJSONObject(it) }.firstOrNull { it.optString("component") == type }
    }

    fun openDoctype(item: DocTypeNavItem) = viewModelScope.launch {
        loadDoctype(item)
    }

    private suspend fun loadDoctype(item: DocTypeNavItem) {
        recordRowsJob?.cancel()
        _state.update { it.copy(selectedModule = null, selectedDoctype = item, selectedRecord = null, recordLinkLabels = emptyMap(), section = "Work", formOpen = false, formValues = emptyMap(), listSearch = "", listSelectionActive = false, listSelectionCount = 0, busy = true, message = null) }
        val active = scope ?: return
        try {
            val records = app.workspace.records(active, item.resourceName)
            val recordsCached = app.workspace.lastResponseWasCached
            val schema = app.workspace.schema(active, item.resourceName)
            val schemaCached = app.workspace.lastResponseWasCached
            val fields = parseFields(schema)
            val route = "/" + item.name.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
            val manifest = runCatching { app.workspace.pageManifest(active, route) }.getOrNull()
            val manifestCached = app.workspace.lastResponseWasCached
            val recordList = (0 until records.length()).mapNotNull { i -> records.optJSONObject(i) }
            _state.update { it.copy(
                records = recordList,
                schemaFields = fields,
                titleField = resolveTitleField(schema, manifest),
                doctypeManifest = manifest?.takeIf { it.has("spec") },
                busy = false,
                offline = recordsCached || schemaCached || manifestCached,
            ) }
            observeRecordRows(active, item)
            resolveUserLabels(fields, recordList)
            resolveRecordLinkLabels(active, fields, recordList)
        } catch (error: Exception) { _state.update { it.copy(busy = false, offline = true, message = error.message) } }
    }

    private suspend fun resolveRecordLinkLabels(active: Scope, fields: List<FieldSchema>, rows: List<JSONObject>) {
        val labels = linkedMapOf<String, String>()
        suspend fun resolve(target: String, references: Set<String>) {
            if (target.isBlank() || references.isEmpty()) return
            val route = resourceRoute(target)
            val titleField = runCatching { resolveTitleField(app.workspace.schema(active, route)) }.getOrDefault("name")
            val records = runCatching { app.workspace.records(active, route) }.getOrNull()
            val byName = records?.let { values -> (0 until values.length()).mapNotNull(values::optJSONObject).associateBy { it.optString("name") } }.orEmpty()
            references.take(100).forEach { reference ->
                val related = byName[reference] ?: runCatching { app.workspace.record(active, route, reference) }.getOrNull()
                val label = related?.optString(titleField).orEmpty().ifBlank { reference }
                labels["$target:$reference"] = label
            }
        }
        fields.filter { it.fieldtype == "Link" && !it.options.isNullOrBlank() }.forEach { field ->
            val references = rows.mapNotNull { row -> row.optString(field.fieldname).takeIf(String::isNotBlank) }.toSet()
            resolve(field.options.orEmpty(), references)
        }
        fields.filter { it.fieldtype == "Table" && !it.options.isNullOrBlank() }.forEach { table ->
            val childType = table.options.orEmpty()
            val childFields = runCatching { parseFields(app.workspace.schema(active, resourceRoute(childType))) }.getOrDefault(emptyList())
            _state.update { current -> if (scope?.key == active.key) current.copy(childSchemas = current.childSchemas + (childType to childFields)) else current }
            val childRows = rows.flatMap { row ->
                val values = row.optJSONArray(table.fieldname) ?: JSONArray()
                (0 until values.length()).mapNotNull(values::optJSONObject)
            }
            childFields.filter { it.fieldtype == "Link" && !it.options.isNullOrBlank() }.forEach { field ->
                val references = childRows.mapNotNull { row -> row.optString(field.fieldname).takeIf(String::isNotBlank) }.toSet()
                resolve(field.options.orEmpty(), references)
            }
        }
        _state.update { current -> if (scope?.key == active.key && current.selectedDoctype != null) current.copy(recordLinkLabels = current.recordLinkLabels + labels) else current }
    }

    private fun observeRecordRows(active: Scope, item: DocTypeNavItem) {
        recordRowsJob?.cancel()
        recordRowsJob = viewModelScope.launch {
            app.workspace.observeRecords(active, item.resourceName).collect { rows ->
                _state.update { current ->
                    if (scope?.key != active.key || current.selectedDoctype?.resourceName != item.resourceName) current
                    else current.copy(records = rows, selectedRecord = current.selectedRecord?.let { selected ->
                        rows.firstOrNull { it.optString("_local_id") == selected.optString("_local_id") && selected.has("_local_id") || it.optString("name") == selected.optString("name") } ?: selected
                    })
                }
            }
        }
    }

    fun viewRecord(record: JSONObject) {
        val doctype = _state.value.selectedDoctype ?: return
        val name = record.optString("name")
        _state.update { it.copy(selectedRecord = record, editingRecord = null, formOpen = false, detailLoading = true, detailLinkLabels = emptyMap(), detailLinkedRecords = emptyMap(), detailLinkedSchemas = emptyMap(), message = null, listSelectionActive = false, listSelectionCount = 0) }
        viewModelScope.launch {
            val active = scope ?: return@launch
            try {
                val fullRecord = app.workspace.record(active, doctype.resourceName, name)
                _state.update { current ->
                    if (current.selectedRecord?.optString("name") != name) current
                    else current.copy(selectedRecord = fullRecord, detailLoading = false, offline = current.offline || app.workspace.lastResponseWasCached)
                }
                val detailLinks = resolveDetailLinkLabels(active, fullRecord)
                _state.update { current -> if (current.selectedRecord?.optString("name") == name) current.copy(detailLinkLabels = detailLinks.first, detailLinkedRecords = detailLinks.second, detailLinkedSchemas = detailLinks.third) else current }
                resolveUserLabels(_state.value.schemaFields, listOf(fullRecord))
            } catch (error: Exception) {
                _state.update { current ->
                    if (current.selectedRecord?.optString("name") != name) current
                    else current.copy(detailLoading = false, offline = true, message = error.message ?: "Showing the saved list details")
                }
            }
        }
    }

    private suspend fun resolveDetailLinkLabels(active: Scope, record: JSONObject): Triple<Map<String, String>, Map<String, JSONObject>, Map<String, List<FieldSchema>>> {
        val labels = mutableMapOf<String, String>()
        val linkedRecords = mutableMapOf<String, JSONObject>()
        val linkedSchemas = mutableMapOf<String, List<FieldSchema>>()
        _state.value.schemaFields.filter { it.fieldtype == "Table" }.forEach { table ->
            val childType = table.options ?: return@forEach
            val childSchema = runCatching { app.workspace.schema(active, resourceRoute(childType)) }.getOrNull() ?: return@forEach
            val childFields = parseFields(childSchema)
            _state.update { it.copy(childSchemas = it.childSchemas + (childType to childFields)) }
            val rows = record.optJSONArray(table.fieldname) ?: return@forEach
            childFields.filter { it.fieldtype == "Link" && !it.options.isNullOrBlank() }.forEach { linkField ->
                val targetType = linkField.options ?: return@forEach
                val references = (0 until rows.length()).mapNotNull { index ->
                    rows.optJSONObject(index)?.optString(linkField.fieldname)?.takeIf(String::isNotBlank)
                }.distinct()
                if (references.isEmpty()) return@forEach
                val targetSchema = runCatching { app.workspace.schema(active, resourceRoute(targetType)) }.getOrNull()
                targetSchema?.let { linkedSchemas[targetType] = parseFields(it) }
                val titleField = targetSchema?.let(::resolveTitleField) ?: "name"
                references.forEach { reference ->
                    val related = runCatching { app.workspace.record(active, resourceRoute(targetType), reference) }.getOrNull()
                    val label = related?.optString(titleField).orEmpty().ifBlank { reference }
                    labels["$targetType:$reference"] = label
                    if (related != null) linkedRecords["$targetType:$reference"] = related
                }
            }
        }
        return Triple(labels, linkedRecords, linkedSchemas)
    }
    fun closeRecordDetail() { _state.update { it.copy(selectedRecord = null, message = null) } }

    fun updateListSelection(active: Boolean, count: Int) {
        _state.update { it.copy(listSelectionActive = active, listSelectionCount = count.coerceAtLeast(0)) }
    }

    fun startNewRecord() {
        val defaults = _state.value.schemaFields.mapNotNull { field ->
            val configured = field.defaultValue?.let { parseDefault(field.fieldtype, it) }
            configured?.let { field.fieldname to it }
        }.toMap()
        _state.update { it.copy(selectedRecord = null, editingRecord = null, formValues = defaults, formOpen = true, resolvingMutationId = null, message = null, listSelectionActive = false, listSelectionCount = 0) }
        loadUserChoices(_state.value.schemaFields)
    }

    fun duplicateRecord(record: JSONObject) {
        val current = _state.value
        val defaults = current.schemaFields.mapNotNull { field -> field.defaultValue?.let { parseDefault(field.fieldtype, it) }?.let { field.fieldname to it } }.toMap()
        val excluded = setOf("name", "_version", "doc_status", "creation", "modified", "modified_by", "owner", "idx", "docstatus")
        val copied = current.schemaFields.mapNotNull { field ->
            if (excluded.contains(field.fieldname) || (field.readOnly && !field.editableOnCreate) || !record.has(field.fieldname) || record.isNull(field.fieldname)) return@mapNotNull null
            val raw = record.opt(field.fieldname)
            if (field.fieldtype == "Table" && raw is JSONArray) {
                val childFields = current.childSchemas[field.options].orEmpty()
                val cleanRows = JSONArray()
                for (index in 0 until raw.length()) {
                    val child = raw.optJSONObject(index) ?: continue
                    val clean = JSONObject()
                    childFields.filterNot { it.fieldname in excluded || (it.readOnly && !it.editableOnCreate) }.forEach { childField ->
                        if (child.has(childField.fieldname) && !child.isNull(childField.fieldname)) clean.put(childField.fieldname, child.opt(childField.fieldname))
                    }
                    cleanRows.put(clean)
                }
                field.fieldname to cleanRows
            } else field.fieldname to raw
        }.toMap()
        _state.update { it.copy(selectedRecord = null, editingRecord = null, formValues = defaults + copied, formOpen = true, resolvingMutationId = null, message = null, listSelectionActive = false, listSelectionCount = 0) }
        loadUserChoices(current.schemaFields)
    }

    fun deleteRecords(recordNames: List<String>) = viewModelScope.launch {
        val active = scope ?: return@launch
        val doctype = _state.value.selectedDoctype ?: return@launch
        val names = recordNames.distinct().filter(String::isNotBlank)
        if (names.isEmpty()) return@launch
        _state.update { it.copy(busy = true, message = null) }
        val deleted = mutableSetOf<String>()
        try {
            names.forEach { name ->
                app.workspace.deleteRecord(active, doctype.resourceName, name)
                deleted += name
            }
            _state.update { current -> current.copy(
                records = current.records.filterNot { it.optString("name") in deleted },
                busy = false,
                listSelectionActive = false,
                listSelectionCount = 0,
                message = "${deleted.size} ${if (deleted.size == 1) "record deleted" else "records deleted"}",
            ) }
        } catch (error: Exception) {
            _state.update { current -> current.copy(
                records = current.records.filterNot { it.optString("name") in deleted },
                busy = false,
                listSelectionActive = false,
                listSelectionCount = 0,
                message = if (deleted.isEmpty()) (error.message ?: "Could not delete records") else "Deleted ${deleted.size}; remaining records were not deleted: ${error.message ?: "request failed"}",
            ) }
        }
    }
    fun editRecord(record: JSONObject) {
        val values = _state.value.schemaFields.mapNotNull { field ->
            if (!record.has(field.fieldname) || record.isNull(field.fieldname)) null else field.fieldname to record.opt(field.fieldname)
        }.toMap()
        _state.update { it.copy(selectedRecord = record, editingRecord = record, formValues = values, formOpen = true, resolvingMutationId = null, listSelectionActive = false, listSelectionCount = 0) }
        loadUserChoices(_state.value.schemaFields)
    }
    fun saveChildRows(record: JSONObject, table: FieldSchema, rows: List<Map<String, Any?>>) = viewModelScope.launch {
        val active = scope ?: return@launch
        val current = _state.value
        val doctype = current.selectedDoctype ?: return@launch
        val values = current.schemaFields.mapNotNull { field ->
            if (!record.has(field.fieldname) || record.isNull(field.fieldname)) null else field.fieldname to record.opt(field.fieldname)
        }.toMap() + (table.fieldname to rows)
        val calculated = calculateDerivedAmounts(current.schemaFields, current.childSchemas, values)
        val name = record.optString("name").takeIf(String::isNotBlank) ?: return@launch
        val payload = FormPayloadSerializer.serialize(current.schemaFields, calculated, current.childSchemas, isNew = false)
        _state.update { it.copy(busy = true, message = null) }
        try {
            queueRecordForSync(active, doctype.resourceName, name, payload, java.util.UUID.randomUUID().toString())
            val updated = JSONObject(record.toString())
            updated.put(table.fieldname, JSONArray().apply {
                rows.forEach { row -> put(JSONObject().apply { row.forEach { (key, value) -> put(key, value ?: JSONObject.NULL) } }) }
            })
            calculated.forEach { (field, value) -> if (value != null) updated.put(field, value) }
            _state.update { state ->
                state.copy(
                    selectedRecord = updated,
                    records = state.records.map { if (it.optString("name") == name) updated else it },
                    message = "Saved",
                )
            }
        } catch (error: Exception) {
            _state.update { it.copy(busy = false, message = error.message ?: "Could not save item changes") }
        }
    }
    fun updateField(name: String, value: Any?) = _state.update { it.copy(formValues = it.formValues + (name to value), message = null) }
    fun updateLinkedField(name: String, value: Any?) = _state.update { current ->
        val changed = current.schemaFields.firstOrNull { it.fieldname == name }
        var values = current.formValues + (name to value)
        if (changed?.fieldtype != "Link" && changed?.fieldtype != "Dynamic Link") {
            current.schemaFields.filter { it.fieldtype == "Dynamic Link" && it.options == name }.forEach { dynamic -> values = values + (dynamic.fieldname to null) }
            return@update current.copy(formValues = values, message = null)
        }
        val target = if (changed.fieldtype == "Dynamic Link") values[changed.options.orEmpty()]?.toString().orEmpty() else changed.options.orEmpty()
        val record = current.linkOptions[target].orEmpty().firstOrNull { it.optString("name") == value?.toString() }
        if (record == null) return@update current.copy(formValues = values, message = null)
        val linked = current.schemaFields.filter { it.linkedField?.startsWith("$name.") == true }.mapNotNull { field ->
            val source = field.linkedField?.substringAfter('.') ?: return@mapNotNull null
            if (!record.has(source) || record.isNull(source)) null else field.fieldname to record.opt(source)
        }.toMap()
        current.copy(formValues = values + linked, message = null)
    }
    fun loadChildSchema(doctype: String) = viewModelScope.launch {
        if (doctype.isBlank() || _state.value.childSchemas.containsKey(doctype)) return@launch
        val active = scope ?: return@launch
        try {
            val schema = app.workspace.schema(active, resourceRoute(doctype))
            _state.update { it.copy(childSchemas = it.childSchemas + (doctype to parseFields(schema))) }
        } catch (error: Exception) {
            _state.update { it.copy(message = error.message ?: "Could not load $doctype fields") }
        }
    }
    fun updateChildLinkedField(tableField: String, childDoctype: String, name: String, value: Any?) = _state.update { current ->
        val rows = (current.formValues[tableField] as? List<*>)?.mapNotNull { row ->
            when (row) {
                is JSONObject -> buildMap { val keys = row.keys(); while (keys.hasNext()) { val key = keys.next(); put(key, row.opt(key)) } }
                is Map<*, *> -> row.entries.associate { it.key.toString() to it.value }
                else -> null
            }
        }.orEmpty()
        val childFields = current.childSchemas[childDoctype].orEmpty()
        val changed = childFields.firstOrNull { it.fieldname == name }
        val target = changed?.options.orEmpty()
        val linkedFields = childFields.filter { it.linkedField?.startsWith("$name.") == true }
        val updatedRows = rows.map { row ->
            if (changed?.fieldtype != "Link" && changed?.fieldtype != "Dynamic Link") row
            else {
                var updated = row + (name to value)
                if (changed.fieldtype == "Dynamic Link") childFields.filter { it.fieldtype == "Dynamic Link" && it.options == name }.forEach { updated = updated + (it.fieldname to null) }
                val matchingLink = current.linkOptions[target].orEmpty().firstOrNull { it.optString("name") == value?.toString() }
                if (matchingLink != null) linkedFields.forEach { field ->
                    val source = field.linkedField?.substringAfter('.') ?: return@forEach
                    if (matchingLink.has(source) && !matchingLink.isNull(source)) updated = updated + (field.fieldname to matchingLink.opt(source))
                }
                updated
            }
        }
        current.copy(formValues = current.formValues + (tableField to updatedRows), message = null)
    }
    fun setListSearch(value: String) = _state.update { it.copy(listSearch = value) }

    fun loadLinkOptions(doctype: String) = viewModelScope.launch {
        if (doctype.isBlank() || _state.value.linkOptions.containsKey(doctype)) return@launch
        val active = scope ?: return@launch
        _state.update { it.copy(linkLoading = it.linkLoading + doctype, linkErrors = it.linkErrors - doctype) }
        try {
            val route = resourceRoute(doctype)
            val records = app.workspace.records(active, route)
            val schema = app.workspace.schema(active, route)
            val titleField = resolveTitleField(schema)
            val options = (0 until records.length()).mapNotNull { records.optJSONObject(it) }
            _state.update { it.copy(linkOptions = it.linkOptions + (doctype to options), linkTitleFields = it.linkTitleFields + (doctype to titleField), linkLoading = it.linkLoading - doctype) }
        } catch (error: Exception) {
            _state.update { it.copy(linkLoading = it.linkLoading - doctype, linkErrors = it.linkErrors + (doctype to (error.message ?: "Could not load choices."))) }
        }
    }

    private suspend fun resolveUserLabels(fields: List<FieldSchema>, records: List<JSONObject>) {
        val names = fields.filter { it.fieldtype == "User" || (it.fieldtype == "Link" && it.options.equals("User", true)) }
            .flatMap { field -> records.mapNotNull { record -> record.optString(field.fieldname).takeIf(String::isNotBlank) } }
            .distinct().take(100)
        if (names.isEmpty()) return
        val active = scope ?: return
        runCatching { app.workspace.userDirectory(active, names) }.onSuccess { users ->
            val labels = (0 until users.length()).mapNotNull { users.optJSONObject(it) }.associate { user ->
                val id = user.optString("name")
                id to user.optString("full_name").ifBlank { user.optString("email").ifBlank { id } }
            }
            _state.update { it.copy(userLabels = it.userLabels + labels, offline = it.offline || app.workspace.lastResponseWasCached) }
        }
    }

    private fun loadUserChoices(fields: List<FieldSchema>) {
        if (fields.none { it.fieldtype == "User" || (it.fieldtype == "Link" && it.options.equals("User", true)) }) return
        val active = scope ?: return
        viewModelScope.launch {
            runCatching { app.workspace.userDirectory(active, emptyList()) }.onSuccess { users ->
                val labels = (0 until users.length()).mapNotNull { users.optJSONObject(it) }.associate { user ->
                    val id = user.optString("name")
                    id to user.optString("full_name").ifBlank { user.optString("email").ifBlank { id } }
                }
                _state.update { current ->
                    if (scope?.key != active.key) current
                    else current.copy(userChoices = labels, offline = current.offline || app.workspace.lastResponseWasCached)
                }
            }
        }
    }

    fun submitRecord() = viewModelScope.launch {
        val active = scope ?: return@launch
        val doctype = _state.value.selectedDoctype ?: return@launch
        val current = _state.value
        val values = calculateDerivedAmounts(current.schemaFields, current.childSchemas, current.formValues)
        val missing = _state.value.schemaFields.firstOrNull { field ->
            !field.hidden && isFieldVisible(field, values) && isFieldRequired(field, values) && (values[field.fieldname] == null || values[field.fieldname] == JSONObject.NULL || values[field.fieldname].toString().isBlank())
        }
        if (missing != null) { _state.update { it.copy(message = "${missing.label} is required") }; return@launch }
        val name = _state.value.editingRecord?.optString("name")?.takeIf(String::isNotBlank)
        val payload = FormPayloadSerializer.serialize(current.schemaFields, values, current.childSchemas, isNew = name == null)
        _state.update { it.copy(busy = true, message = null, formValues = values) }
        val idempotencyKey = java.util.UUID.randomUUID().toString()
        queueRecordForSync(active, doctype.resourceName, name, payload, idempotencyKey)
    }

    private fun calculateDerivedAmounts(
        fields: List<FieldSchema>,
        childSchemas: Map<String, List<FieldSchema>>,
        source: Map<String, Any?>,
    ): Map<String, Any?> {
        var values = source
        var grandTotal = 0.0
        var foundLineAmounts = false
        fields.filter { it.fieldtype == "Table" }.forEach { table ->
            val childFields = childSchemas[table.options].orEmpty()
            val quantity = childFields.firstOrNull { it.matchesSemanticRole("quantity", "qty", "units") } ?: return@forEach
            val price = childFields.firstOrNull { it.matchesSemanticRole("unit price", "price", "rate", "unit cost") } ?: return@forEach
            val amount = childFields.firstOrNull { it.computed != null || it.matchesSemanticRole("line amount", "line total", "amount", "total") } ?: return@forEach
            val rows = childRows(values[table.fieldname])
            val normalized = rows.map { original ->
                val row = original.toMutableMap()
                val qty = row[quantity.fieldname].numberOrNull()
                val rate = row[price.fieldname].numberOrNull()
                if (qty != null && rate != null && qty.isFinite() && rate.isFinite()) {
                    val lineTotal = qty * rate
                    row[amount.fieldname] = lineTotal
                    grandTotal += lineTotal
                    foundLineAmounts = true
                }
                row
            }
            values = values + (table.fieldname to normalized)
        }
        if (foundLineAmounts) {
            fields.firstOrNull { field ->
                field.fieldtype.equals("Currency", true) &&
                    (field.computed != null || field.readOnly || field.matchesSemanticRole("total", "subtotal", "grand total"))
            }?.let { total -> values = values + (total.fieldname to grandTotal) }
        }
        return values
    }

    private fun childRows(value: Any?): List<Map<String, Any?>> = when (value) {
        is JSONArray -> (0 until value.length()).mapNotNull { index ->
            val item = value.optJSONObject(index) ?: return@mapNotNull null
            buildMap { val keys = item.keys(); while (keys.hasNext()) { val key = keys.next(); put(key, item.opt(key).takeUnless { it == JSONObject.NULL }) } }
        }
        is List<*> -> value.mapNotNull { item ->
            when (item) {
                is JSONObject -> buildMap { val keys = item.keys(); while (keys.hasNext()) { val key = keys.next(); put(key, item.opt(key).takeUnless { it == JSONObject.NULL }) } }
                is Map<*, *> -> item.entries.associate { it.key.toString() to it.value }
                else -> null
            }
        }
        else -> emptyList()
    }

    private fun Any?.numberOrNull(): Double? = when (this) {
        null, JSONObject.NULL -> null
        is Number -> toDouble().takeIf(Double::isFinite)
        else -> toString().trim().toDoubleOrNull()?.takeIf(Double::isFinite)
    }

    private suspend fun queueRecordForSync(active: Scope, doctype: String, name: String?, payload: JSONObject, idempotencyKey: String) {
        try {
            val titleField = _state.value.titleField
            val displayName = payload.optString(titleField).ifBlank { payload.optString("reference") }.ifBlank { null }
            val correction = _state.value.resolvingMutationId
            if (correction != null) app.workspace.localRecords.correct(active, correction, payload, null)
            else {
                val schema = app.workspace.schema(active, doctype).optJSONObject("doctype")
                    ?: throw IllegalStateException("Download this record definition before saving")
                val binding = if (name == null) schema.optJSONObject("quantity_effect") else null
                if (binding == null) app.workspace.enqueueRecord(active, doctype, name, payload, idempotencyKey, displayName)
                else {
                    val recordField = binding.optString("source_record_field")
                    val deltaField = binding.optString("delta_field")
                    val reasonField = binding.optString("reason_field")
                    val targetType = binding.optString("target_doctype")
                    val quantityField = binding.optString("target_quantity_field")
                    val actionId = binding.optString("action")
                    val delta = payload.optString(deltaField).toBigDecimalOrNull()
                    val target = payload.optString(recordField)
                    if (delta == null || delta.signum() == 0 || target.isBlank() || payload.optString(reasonField).isBlank() || targetType.isBlank() || quantityField.isBlank() || actionId.isBlank()) {
                        throw IllegalArgumentException("Choose a record, enter a signed quantity, and give a reason")
                    }
                    val effect = JSONObject().put("doctype", resourceRoute(targetType)).put("quantity_field", quantityField)
                        .put("lines", JSONArray().put(JSONObject().put("name", target).put("quantity", delta.negate().toPlainString())))
                    val request = JSONObject().put("view", binding.optString("view"))
                        .put("component", binding.optString("component")).put("context", payload)
                    app.workspace.enqueueViewAction(active, actionId, request, idempotencyKey, doctype, payload, effect)
                }
            }
            _state.update {
                it.copy(busy = false, editingRecord = null, formOpen = false, formValues = emptyMap(),
                    message = "Saved")
            }
            observeLocalWork()
            if (app.workspace.networkAvailable) syncQueuedMutations()
        } catch (error: Exception) {
            _state.update { it.copy(busy = false, message = error.message ?: "Could not save on this device") }
        }
    }

    fun syncQueuedMutations(showMessage: Boolean = false) {
        if (outboxSyncJob?.isActive == true) return
        outboxSyncJob = viewModelScope.launch {
            val active = scope ?: return@launch
            _state.update { it.copy(syncingMutations = true) }
            val (synced, error) = app.workspace.syncQueuedRecords(active, force = showMessage)
            _state.update { current ->
                val remaining = current.queuedMutations.size - synced
                current.copy(
                    syncingMutations = false,
                    offline = app.workspace.offline.value,
                    message = when {
                        error != null && showMessage -> "${remaining.coerceAtLeast(0)} change${if (remaining == 1) "" else "s"} waiting to sync · ${error.message ?: "Could not sync"}"
                        error != null -> null
                        showMessage && synced > 0 -> "Synced $synced saved record${if (synced == 1) "" else "s"}"
                        else -> current.message
                    },
                )
            }
            observeLocalWork()
            if (error == null && _state.value.posManifest != null) {
                val productType = _state.value.posManifest?.optJSONObject("spec")?.optJSONArray("resources")
                    ?.optJSONObject(0)?.optJSONObject("params")?.optString("doctype").orEmpty()
                if (productType.isNotBlank()) {
                    runCatching { app.workspace.records(active, resourceRoute(productType)) }.getOrNull()?.let { rows ->
                        _state.update { it.copy(posProducts = (0 until rows.length()).mapNotNull(rows::optJSONObject)) }
                    }
                }
            }
        }
    }

    fun backFromForm() = _state.update { it.copy(selectedRecord = it.editingRecord, editingRecord = null, formOpen = false, formValues = emptyMap(), message = null) }
    fun logout() = viewModelScope.launch {
        val previous = scope
        scope = null
        liveUpdatesJob?.cancel(); liveUpdatesJob = null
        recordRowsJob?.cancel(); outboxJob?.cancel(); outboxSyncJob?.cancel()
        app.getSharedPreferences("kora_session", 0).edit().clear().apply()
        _state.update { AppState(stage = AppStage.SERVER, serverUrl = app.instanceStore.current().origin, siteRef = app.instanceStore.current().siteRef.orEmpty()) }
        previous?.let { app.workspace.logout(it) }
    }

    fun changeServer(url: String) = viewModelScope.launch {
        try {
            val old = app.instanceStore.current()
            val instance = app.instanceStore.save(url, old.siteRef)
            scope = null
            recordRowsJob?.cancel(); outboxJob?.cancel(); outboxSyncJob?.cancel()
            app.getSharedPreferences("kora_session", 0).edit().clear().apply()
            _state.update { AppState(stage = AppStage.LOGIN, serverUrl = instance.origin, siteRef = instance.siteRef.orEmpty(), message = "Sign in to this server. Saved work stays with its original workspace.") }
        } catch (error: Exception) {
            _state.update { it.copy(message = error.message ?: "Could not update server address") }
        }
    }

    fun restoreUsbServer() = viewModelScope.launch {
        if (scope?.origin == "http://localhost:8000") syncQueuedMutations(showMessage = true)
        else changeServer("http://localhost:8000")
    }

    private fun restoreCachedSession() = viewModelScope.launch {
        val saved = app.getSharedPreferences("kora_session", 0)
        val userId = saved.getString("last_user", null) ?: return@launch
        val instance = app.instanceStore.current()
        if (instance.siteRef.isNullOrBlank()) {
            _state.update { it.copy(stage = AppStage.SERVER, message = "Enter a Site ID before signing in") }
            return@launch
        }
        val restored = Scope(instance.id, instance.origin, instance.siteRef, userId)
        scope = restored
        app.activateScope(restored)
        app.workspace.clearDrafts(restored)
        app.workspace.restoreCoverage(restored)
        // Restore the locally authenticated workspace immediately. Network validation runs separately.
        val cachedUser = CurrentUser(userId, saved.getString("last_email", "").orEmpty(), saved.getString("last_full_name", "Your workspace").orEmpty())
        _state.update { it.copy(stage = AppStage.WORKSPACE, user = cachedUser, offline = app.workspace.offline.value) }
        startLiveUpdates()
        startNotificationStream()
        loadWorkspace(); loadRequests(); loadNotifications(); observeLocalWork()
        if (!app.workspace.networkAvailable) return@launch
        try {
            val user = parseUser(app.workspace.currentUser(restored))
            if (user.name != userId) throw IllegalStateException("Sign in again to sync this workspace")
            _state.update { it.copy(user = user) }
            syncQueuedMutations()
            if (_state.value.offlineCoverage.completedAt == null || !app.workspace.localRecords.hasPullCursor(restored)) downloadForOffline()
        } catch (error: Exception) {
            _state.update { it.copy(message = if ((error as? com.kora.mobile.domain.model.KoraError)?.status == 401) "Sign in again to sync. Your saved work is safe on this device." else null) }
        }
    }

    private fun loadWorkspace() = viewModelScope.launch {
        val active = scope ?: return@launch
        try {
            val config = parseNavigation(app.workspace.navigation(active))
            val navCached = app.workspace.lastResponseWasCached
            experienceCopy = null
            val brandingResponse = runCatching { app.workspace.branding(active) }.getOrNull()
            val brandingJson = brandingResponse?.optJSONObject("data")?.optJSONObject("branding")
                ?: brandingResponse?.optJSONObject("branding")
                ?: brandingResponse
            val tenantBranding = brandingJson?.let { parseBranding(it) } ?: config.branding
            brandingResponse?.optJSONObject("data")?.optJSONObject("copy")?.let { experienceCopy = JSONObject(it.toString()) }
            val settingsResponse = runCatching { app.workspace.settings(active) }.getOrNull()
            val settingsJson = settingsResponse?.optJSONObject("data") ?: settingsResponse
            val currency = settingsJson?.optString("currency")?.takeIf { it.isNotBlank() && it != "null" } ?: _state.value.currency
            // The site configuration is the source of truth until the app exposes
            // a user-level appearance override. Ignore stale local values from
            // older builds so a tenant change takes effect immediately.
            val themePreference = app.getSharedPreferences("kora_mobile_navigation", 0)
                .getString("appearance:${active.siteRef.orEmpty()}", "site") ?: "site"
            val siteTheme = (tenantBranding?.defaultMode ?: "light").lowercase()
            val themeMode = if (themePreference == "site") siteTheme else themePreference
            val workflowManifests = config.views.mapNotNull { view ->
                runCatching { app.workspace.pageManifest(active, view.route) }.getOrNull()
            }
            val posManifest = workflowManifests.firstOrNull { manifest ->
                val children = manifest.optJSONObject("spec")?.optJSONObject("layout")?.optJSONArray("children") ?: JSONArray()
                val componentTypes = (0 until children.length()).mapNotNull { children.optJSONObject(it)?.optString("component") }.toSet()
                "product_grid" in componentTypes && "payment_panel" in componentTypes
            }
            _state.update { it.copy(navigation = config.copy(branding = tenantBranding), tenantBranding = tenantBranding, brandingDraft = tenantBranding, savedBranding = tenantBranding, currency = currency, savedCurrency = currency, themeMode = themeMode, themePreference = themePreference, posManifest = posManifest, offline = navCached || app.workspace.lastResponseWasCached, message = null) }
        } catch (_: Exception) { _state.update { it.copy(offline = true, message = null) } }
    }

    private fun loadRequests() = viewModelScope.launch {
        val active = scope ?: return@launch
        try {
            val response = app.workspace.requests(active)
            val items = response.optJSONArray("items") ?: JSONArray()
            _state.update { it.copy(requests = (0 until items.length()).mapNotNull { i -> items.optJSONObject(i) }, offline = app.workspace.lastResponseWasCached, message = null) }
        } catch (error: Exception) { _state.update { it.copy(offline = true, message = error.message ?: "Could not load requests") } }
    }

    fun loadNotifications() = viewModelScope.launch {
        val active = scope ?: return@launch
        _state.update { it.copy(notifications = app.workspace.notifications(active).toJsonObjects()) }
    }

    fun openNotification(notification: JSONObject) = viewModelScope.launch {
        _state.update { it.copy(selectedNotification = notification) }
        markNotificationRead(notification)
    }

    fun activateNotification(notification: JSONObject) = viewModelScope.launch {
        val action = notification.optJSONObject("action")
        val target = action?.optJSONObject("target") ?: action ?: JSONObject()
        val targetDoctype = target.optString("doctype").ifBlank { notification.optString("target_doctype") }
        val targetName = target.optString("name")
            .ifBlank { target.optString("id") }
            .ifBlank { notification.optString("target_name") }
        val targetType = _state.value.navigation?.modules?.flatMap { it.doctypes }
            ?.firstOrNull { !it.isChild && (it.name.equals(targetDoctype, true) || it.resourceName.equals(targetDoctype, true)) }

        closeNotification()
        if (targetType != null && targetName.isNotBlank()) {
            openDoctype(targetType).join()
            if (_state.value.selectedDoctype?.resourceName == targetType.resourceName) {
                viewRecord(JSONObject().put("name", targetName))
            }
            return@launch
        }

        // Requests keep their existing inbox navigation. Other actions can open
        // records through the generic target contract above.
        if (notification.optString("request_id").isNotBlank()) selectSection("Requests")
    }

    fun closeNotification() = _state.update { it.copy(selectedNotification = null) }

    fun markNotificationRead(notification: JSONObject) = viewModelScope.launch {
        val active = scope ?: return@launch
        val requestId = notification.optString("request_id")
        if (requestId.isNotBlank()) runCatching { app.workspace.markRequestRead(active, requestId) }
        app.workspace.updateNotifications(active) { item -> if (item.optString("id") == notification.optString("id")) item.put("read", true) }
        _state.update { state -> state.copy(notifications = state.notifications.map { item -> if (item.optString("id") == notification.optString("id")) JSONObject(item.toString()).put("read", true) else item }) }
    }

    fun markAllNotificationsRead() = viewModelScope.launch {
        val active = scope ?: return@launch
        _state.value.notifications.filter { !it.optBoolean("read") }.forEach { notification ->
            val requestId = notification.optString("request_id")
            if (requestId.isNotBlank()) runCatching { app.workspace.markRequestRead(active, requestId) }
        }
        app.workspace.updateNotifications(active) { it.put("read", true) }
        _state.update { it.copy(notifications = it.notifications.map { item -> JSONObject(item.toString()).put("read", true) }) }
    }

    fun archiveNotification(notification: JSONObject) = viewModelScope.launch {
        val active = scope ?: return@launch
        app.workspace.updateNotifications(active) { item -> if (item.optString("id") == notification.optString("id")) item.put("archived", true) }
        _state.update { it.copy(notifications = it.notifications.map { item -> if (item.optString("id") == notification.optString("id")) JSONObject(item.toString()).put("archived", true) else item }) }
    }

    fun snoozeNotification(notification: JSONObject) = viewModelScope.launch {
        val active = scope ?: return@launch
        val until = System.currentTimeMillis() + 24 * 60 * 60 * 1000L
        app.workspace.updateNotifications(active) { item -> if (item.optString("id") == notification.optString("id")) item.put("snoozed_until", until) }
        _state.update { state -> state.copy(notifications = state.notifications.map { item -> if (item.optString("id") == notification.optString("id")) JSONObject(item.toString()).put("snoozed_until", until) else item }, selectedNotification = null) }
    }

    fun dismissNotificationToast() = _state.update { it.copy(notificationToast = null) }

    private fun observeLocalWork() {
        val active = scope ?: return
        outboxJob?.cancel()
        outboxJob = viewModelScope.launch {
            app.workspace.queuedMutations(active).collect { list ->
                _state.update { it.copy(queuedMutations = list) }
                val ids = list.map { it.id }.toSet()
                if (ids.isEmpty()) scheduledMutationIds = emptySet()
                else if (!scheduledMutationIds.containsAll(ids)) {
                    scheduledMutationIds = ids
                    OutboxSyncWorker.schedule(app, active)
                }
            }
        }
    }

    private fun parseUser(raw: JSONObject): CurrentUser {
        val obj = raw.optJSONObject("data") ?: raw
        val roles = obj.optJSONArray("roles") ?: JSONArray()
        return CurrentUser(obj.optString("name"), obj.optString("email"), obj.optString("full_name"), (0 until roles.length()).map { roles.optString(it) })
    }

    private fun parseNavigation(raw: JSONObject): NavigationConfig {
        val moduleArray = raw.optJSONArray("modules") ?: JSONArray()
        val modules = (0 until moduleArray.length()).mapNotNull { moduleArray.optJSONObject(it) }.map { module ->
            val docs = module.optJSONArray("doctypes") ?: JSONArray()
            val items = (0 until docs.length()).mapNotNull { docs.optJSONObject(it) }.map { item ->
                val name = item.optString("name")
                DocTypeNavItem(name, item.optString("resource_name", name), item.optString("label", name), item.optString("icon").ifBlank { null }, item.optBoolean("is_child"))
            }
            ModuleGroup(
                module = module.optString("module"),
                label = module.optString("label", module.optString("module")),
                doctypes = items,
                icon = module.optString("icon").ifBlank { null },
                order = module.takeIf { it.has("order") }?.optInt("order"),
                description = module.optString("description").ifBlank { null },
            )
        }
        val brandingObj = raw.optJSONObject("branding")
        val user = raw.optJSONObject("user")?.let { obj -> CurrentUser(obj.optString("name"), obj.optString("email"), obj.optString("full_name"), jsonStringArray(obj.optJSONArray("roles"))) }
        val views = raw.optJSONArray("views")?.let { array ->
            (0 until array.length()).mapNotNull { index ->
                array.optJSONObject(index)?.let { view ->
                    NavigationView(
                        name = view.optString("name"),
                        label = view.optString("label").ifBlank { view.optString("name") },
                        route = view.optString("route"),
                        type = view.optString("type").ifBlank { null },
                        module = view.optString("module").ifBlank { null },
                    )
                }
            }.filter { it.route.isNotBlank() }
        }.orEmpty()
        return NavigationConfig(modules, brandingObj?.let(::parseBranding), user,
            jsonStringArray(raw.optJSONArray("admin_capabilities")), jsonStringArray(raw.optJSONArray("supported_capabilities")), views)
    }

    private fun parseBranding(raw: JSONObject) = Branding(
        appName = raw.optString("app_name", "Kora"),
        shortName = raw.optString("short_name").ifBlank { null },
        logoUrl = raw.optString("logo_url").ifBlank { null },
        logoDarkUrl = raw.optString("logo_dark_url").ifBlank { null },
        faviconUrl = raw.optString("favicon_url").ifBlank { null },
        primaryColor = raw.optString("primary_color").ifBlank { null },
        accentColor = raw.optString("accent_color").ifBlank { null },
        fontFamily = raw.optString("font_family").ifBlank { null },
        headingFont = raw.optString("heading_font").ifBlank { null },
        defaultMode = raw.optString("default_mode").ifBlank { null },
    )

    private fun parseFields(raw: JSONObject): List<FieldSchema> {
        val rows = raw.optJSONArray("fields") ?: raw.optJSONObject("doctype")?.optJSONArray("fields") ?: JSONArray()
        return (0 until rows.length()).mapNotNull { index -> rows.optJSONObject(index)?.let { index to it } }.mapNotNull { (index, field) ->
            val name = field.optString("fieldname").ifBlank { "_layout_$index" }
            FieldSchema(
                fieldname = name,
                label = field.optString("label", name),
                fieldtype = field.optString("fieldtype", "Data"),
                options = field.optString("options").ifBlank { null },
                reqd = field.optBoolean("reqd"),
                readOnly = field.optBoolean("read_only"),
                editableOnCreate = field.optBoolean("editable_on_create"),
                computed = field.optString("computed").ifBlank { null },
                hidden = field.optBoolean("hidden"),
                collapsible = field.optBoolean("collapsible"),
                collapsed = field.optBoolean("collapsed"),
                dependsOn = field.optString("depends_on").ifBlank { null },
                mandatoryDependsOn = field.optString("mandatory_depends_on").ifBlank { null },
                description = field.optString("description").ifBlank { null },
                inListView = field.optBoolean("in_list_view"),
                inStandardFilter = field.optBoolean("in_standard_filter"),
                linkedField = field.optString("linked_field").ifBlank { null },
                bold = field.optBoolean("bold"),
                accept = field.optString("accept").ifBlank { null },
                defaultValue = field.optString("default").ifBlank { null },
                constraints = field.optJSONArray("constraints")?.let { constraints ->
                    (0 until constraints.length()).mapNotNull { i -> constraints.optJSONObject(i)?.let { item ->
                        FieldConstraint(
                            type = item.optString("type"),
                            value = if (item.has("value") && !item.isNull("value")) item.opt("value")?.toString() else null,
                            values = item.optJSONArray("values")?.let { values -> (0 until values.length()).map(values::optString) }.orEmpty(),
                            pattern = item.optString("pattern").ifBlank { null },
                            message = item.optString("message"),
                            condition = item.optString("condition").ifBlank { null },
                        )
                    } }
                }.orEmpty(),
            )
        }
    }

    /** Uses the configured title field, falling back to the first visible list field instead of exposing an internal record ID. */
    private fun resolveTitleField(schema: JSONObject, pageManifest: JSONObject? = null): String {
        val configured = schema.optJSONObject("doctype")?.optString("title_field").orEmpty().trim()
        val fields = parseFields(schema)
        if (configured.isNotBlank() && !configured.equals("name", ignoreCase = true) && fields.any { it.fieldname == configured && !it.hidden }) {
            return configured
        }
        val children = pageManifest?.optJSONObject("spec")?.optJSONObject("layout")?.optJSONArray("children") ?: JSONArray()
        val listProps = (0 until children.length()).mapNotNull(children::optJSONObject)
            .firstOrNull { it.optString("component") in setOf("record_table", "record_list", "record_cards") }
            ?.optJSONObject("props")
        val declaredTitle = listProps?.optString("mobile_title_field").orEmpty()
            .ifBlank { listProps?.optString("title_field").orEmpty() }
        if (declaredTitle.isNotBlank() && fields.any { it.fieldname == declaredTitle && !it.hidden }) return declaredTitle
        val mobileColumns = listProps?.optJSONArray("mobile_columns")
        val configuredListFields = mobileColumns?.let { columns -> (0 until columns.length()).map(columns::optString) }.orEmpty()
        val titleCandidates = configuredListFields + fields.filter { it.inListView }.map { it.fieldname }
        titleCandidates.firstOrNull { candidate ->
            val field = fields.firstOrNull { it.fieldname == candidate }
            field != null && !field.hidden && !candidate.equals("name", ignoreCase = true) &&
                field.fieldtype in setOf("Data", "Text", "Link", "Dynamic Link", "Select", "Date", "Datetime", "DateTime")
        }?.let { return it }
        fields.firstOrNull { field ->
            !field.hidden && !field.fieldname.equals("name", ignoreCase = true) &&
                field.fieldtype in setOf("Data", "Text", "Link", "Dynamic Link", "Select", "Date", "Datetime", "DateTime")
        }?.let { return it.fieldname }
        return fields.firstOrNull { field ->
            field.inListView && !field.hidden && !field.fieldname.equals("name", ignoreCase = true) &&
                field.fieldtype !in setOf("Table", "Section Break", "Column Break", "Heading", "HTML", "Button")
        }?.fieldname ?: configured.takeIf(String::isNotBlank) ?: "name"
    }

    private fun parseDefault(fieldType: String, raw: String): Any? = when (fieldType) {
        "Check" -> raw == "1" || raw.equals("true", true)
        "Int" -> raw.toIntOrNull() ?: raw
        "Float", "Currency", "Percent" -> raw.toDoubleOrNull() ?: raw
        "Date" -> if (raw == "today" || raw == "__today__") java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date()) else raw
        "Datetime", "DateTime" -> if (raw.equals("now", true) || raw == "__now__" || raw == "\$now") java.time.OffsetDateTime.now().toString() else raw
        else -> raw
    }

    private fun isFieldVisible(field: FieldSchema, values: Map<String, Any?>): Boolean =
        !field.hidden && evaluateFieldCondition(field.dependsOn, values, fallback = true)

    private fun isFieldRequired(field: FieldSchema, values: Map<String, Any?>): Boolean =
        field.reqd || evaluateFieldCondition(field.mandatoryDependsOn, values, fallback = false)

    private fun evaluateFieldCondition(condition: String?, values: Map<String, Any?>, fallback: Boolean): Boolean {
        if (condition.isNullOrBlank()) return fallback
        return condition.split(',').map(String::trim).filter(String::isNotBlank).all { token ->
            val expr = token.removePrefix("eval:").removePrefix("doc.")
            if (expr.startsWith('!')) return@all !isTruthy(values[expr.drop(1)])
            val comparator = listOf("==", "!=", ">=", "<=", ">", "<").firstOrNull(expr::contains)
            if (comparator == null) return@all isTruthy(values[expr])
            val parts = expr.split(comparator, limit = 2)
            val left = values[parts[0].trim()]
            val rawRight = parts[1].trim()
            val right: Any? = when {
                rawRight.equals("true", true) -> true
                rawRight.equals("false", true) -> false
                rawRight == "null" -> null
                rawRight.trim('"', '\'').toDoubleOrNull() != null -> rawRight.trim('"', '\'').toDouble()
                else -> rawRight.trim('"', '\'')
            }
            val leftNumber = left.toString().toDoubleOrNull()
            val rightNumber = right?.toString()?.toDoubleOrNull()
            val comparison = if (leftNumber != null && rightNumber != null) leftNumber.compareTo(rightNumber) else left.toString().compareTo(right?.toString().orEmpty(), ignoreCase = true)
            when (comparator) {
                "==" -> left == right || left?.toString() == right?.toString()
                "!=" -> !(left == right || left?.toString() == right?.toString())
                ">" -> comparison > 0
                "<" -> comparison < 0
                ">=" -> comparison >= 0
                "<=" -> comparison <= 0
                else -> false
            }
        }
    }

    private fun isTruthy(value: Any?): Boolean = when (value) {
        null -> false
        is Boolean -> value
        is Number -> value.toDouble() != 0.0
        is List<*> -> value.isNotEmpty()
        else -> value.toString().isNotBlank() && value.toString() != "0" && !value.toString().equals("false", true)
    }

    private fun jsonStringArray(array: JSONArray?): List<String> = array?.let { (0 until it.length()).map(it::optString) }.orEmpty()
}

private fun JSONArray.toJsonObjects(): List<JSONObject> = (0 until length()).mapNotNull { optJSONObject(it) }

private fun FieldSchema.matchesSemanticRole(vararg terms: String): Boolean {
    val metadata = "$label ${fieldname.replace('_', ' ')}"
    return terms.any { term ->
        Regex("(^|[^a-z0-9])${Regex.escape(term)}([^a-z0-9]|$)", RegexOption.IGNORE_CASE).containsMatchIn(metadata)
    }
}
