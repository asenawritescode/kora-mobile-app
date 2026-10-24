package com.kora.mobile.app

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
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
import com.kora.mobile.domain.model.Scope
import kotlinx.coroutines.Job
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
import java.time.LocalDate

data class AppState(
    val stage: AppStage = AppStage.SERVER,
    val serverUrl: String = "",
    val siteRef: String = "",
    val message: String? = null,
    val busy: Boolean = false,
    val user: CurrentUser? = null,
    val navigation: NavigationConfig? = null,
    val tenantBranding: Branding? = null,
    val themeMode: String = "system",
    val section: String = "Home",
    val selectedDoctype: DocTypeNavItem? = null,
    val selectedRecord: JSONObject? = null,
    val detailLoading: Boolean = false,
    val detailLinkLabels: Map<String, String> = emptyMap(),
    val records: List<JSONObject> = emptyList(),
    val schemaFields: List<FieldSchema> = emptyList(),
    val childSchemas: Map<String, List<FieldSchema>> = emptyMap(),
    val titleField: String = "name",
    val doctypeManifest: JSONObject? = null,
    val listSearch: String = "",
    val linkOptions: Map<String, List<JSONObject>> = emptyMap(),
    val linkTitleFields: Map<String, String> = emptyMap(),
    val linkLoading: Set<String> = emptySet(),
    val linkErrors: Map<String, String> = emptyMap(),
    val userLabels: Map<String, String> = emptyMap(),
    val editingRecord: JSONObject? = null,
    val formValues: Map<String, Any?> = emptyMap(),
    val formOpen: Boolean = false,
    val resolvingMutationId: String? = null,
    val requests: List<JSONObject> = emptyList(),
    val queuedMutations: List<LocalMutation> = emptyList(),
    val syncingMutations: Boolean = false,
    val offline: Boolean = false,
    val offlineCoverage: com.kora.mobile.data.offline.OfflineCoverage = com.kora.mobile.data.offline.OfflineCoverage(),
    val posManifest: JSONObject? = null,
    val posLoading: Boolean = false,
    val posProducts: List<JSONObject> = emptyList(),
    val posCategories: Map<String, String> = emptyMap(),
    val posCart: Map<String, PosCartLine> = emptyMap(),
    val posSaving: Boolean = false,
    val posPaymentMethod: String = "cash",
    val analyticsCatalog: JSONObject? = null,
    val savedReports: List<JSONObject> = emptyList(),
    val reportResult: JSONObject? = null,
    val reportLoading: Boolean = false,
    val reportError: String? = null,
    val selectedReportName: String = "",
    val selectedAnalyticsModel: String = "",
    val selectedAnalyticsMeasure: String = "",
    val reportWindowDays: Int = 30,
)
data class PosCartLine(val product: JSONObject, val quantity: Int)
enum class AppStage { SERVER, LOGIN, WORKSPACE }

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as KoraApplication
    private val _state = MutableStateFlow(AppState(serverUrl = app.instanceStore.current().origin, siteRef = app.instanceStore.current().siteRef.orEmpty()))
    val state: StateFlow<AppState> = _state.asStateFlow()
    private var scope: Scope? = null
    private var outboxJob: Job? = null
    private var outboxSyncJob: Job? = null
    private var recordRowsJob: Job? = null
    private var scheduledMutationIds: Set<String> = emptySet()
    private val connectivityManager = app.getSystemService(ConnectivityManager::class.java)
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            app.workspace.networkAvailable = true
            syncQueuedMutations()
        }
        override fun onLost(network: Network) { app.workspace.markNetworkUnavailable() }
    }

    init {
        val connected = connectivityManager.activeNetwork?.let { connectivityManager.getNetworkCapabilities(it)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } == true
        if (!connected) app.workspace.markNetworkUnavailable()
        viewModelScope.launch { app.workspace.offline.collect { offline -> _state.update { it.copy(offline = offline) } } }
        viewModelScope.launch { app.workspace.coverage.collect { coverage -> _state.update { it.copy(offlineCoverage = coverage) } } }
        restoreCachedSession()
        runCatching { connectivityManager.registerDefaultNetworkCallback(networkCallback) }
    }

    override fun onCleared() {
        runCatching { connectivityManager.unregisterNetworkCallback(networkCallback) }
        super.onCleared()
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
            loadWorkspace(); observeLocalWork(); if (!_state.value.offline) syncQueuedMutations()
            downloadForOffline()
        } catch (error: Exception) { _state.update { it.copy(busy = false, message = error.message ?: "Sign in failed") } }
    }

    fun selectSection(section: String) {
        _state.update { it.copy(section = section, selectedDoctype = null, selectedRecord = null, editingRecord = null, formOpen = false, formValues = emptyMap()) }
        when (section) { "Requests" -> loadRequests(); "Work" -> loadWorkspace(); "POS" -> loadPosCatalog(); "Reports" -> loadAnalytics(); "Sync" -> if (!_state.value.offline) syncQueuedMutations(showMessage = true) }
    }

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
            val resources = spec.optJSONArray("resources") ?: JSONArray()
            val sourceDoctype = resources.optJSONObject(0)?.optJSONObject("params")?.optString("doctype").orEmpty().ifBlank { "Product" }
            val components = spec.optJSONObject("layout")?.optJSONArray("children") ?: JSONArray()
            val componentRows = (0 until components.length()).mapNotNull { components.optJSONObject(it) }
            val productComponent = componentRows.firstOrNull { it.optString("component") == "product_grid" }
            val categoryComponent = (0 until components.length()).mapNotNull { components.optJSONObject(it) }.firstOrNull { it.optString("component") == "category_tabs" }
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
                val titleField = categorySchema.optJSONObject("doctype")?.optString("title_field").orEmpty().ifBlank { "name" }
                categoryNames = (0 until categoryRows.length()).mapNotNull { categoryRows.optJSONObject(it) }
                    .filterNot { it.has("is_active") && (it.opt("is_active") == false || it.optString("is_active") == "0" || it.optString("is_active").equals("false", true)) }
                    .associate { category -> category.optString("name") to category.optString(titleField).ifBlank { category.optString("name") } }
            } else {
                categoryNames = productRows.mapNotNull { row -> row.optString(groupField).takeIf(String::isNotBlank) }.distinct().associateWith { it }
            }
            _state.update { it.copy(posManifest = manifest, posProducts = productRows, posCategories = categoryNames, posLoading = false, offline = productsCached || productSchemaCached || categoriesCached || categorySchemaCached, message = null) }
        } catch (error: Exception) {
            _state.update { it.copy(posLoading = false, offline = true, message = error.message ?: "Could not load the product catalogue") }
        }
    }

    fun refreshPosCatalog() = loadPosCatalog()

    fun addPosProduct(product: JSONObject) {
        val key = product.optString("name")
        val component = posComponent("product_grid")
        val bindings = component?.optJSONObject("props")?.optJSONObject("bindings") ?: JSONObject()
        val stockField = bindings.optString("badge").ifBlank { "stock_qty" }
        if (key.isBlank() || product.optInt(stockField, 1) <= 0) return
        _state.update { current ->
            val old = current.posCart[key]
            val maxStock = product.optInt(stockField, Int.MAX_VALUE).coerceAtLeast(1)
            val quantity = ((old?.quantity ?: 0) + 1).coerceAtMost(maxStock)
            current.copy(posCart = current.posCart + (key to PosCartLine(product, quantity)), message = null)
        }
    }

    fun changePosQuantity(productName: String, delta: Int) {
        _state.update { current ->
            val line = current.posCart[productName] ?: return@update current
            val next = line.quantity + delta
            if (next <= 0) current.copy(posCart = current.posCart - productName)
            else current.copy(posCart = current.posCart + (productName to line.copy(quantity = next)))
        }
    }

    fun clearPosCart() { _state.update { it.copy(posCart = emptyMap(), message = null) } }

    fun setPosPaymentMethod(method: String) { _state.update { it.copy(posPaymentMethod = method) } }

    fun completePosSale() = viewModelScope.launch {
        val active = scope ?: return@launch
        val current = _state.value
        val lines = current.posCart.values.toList()
        if (lines.isEmpty()) return@launch
        val productComponent = posComponent("product_grid")
        val priceField = productComponent?.optJSONObject("props")?.optJSONObject("bindings")?.optString("price").orEmpty().ifBlank { "selling_price" }
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
        val saleType = resourceRoute(actionConfig.optString("target_doctype").ifBlank { "Sale" })
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
        try {
            app.workspace.enqueueViewAction(active, actionId, request, idempotencyKey, saleType, projection, effect)
            val products = effect?.optString("doctype")?.takeIf(String::isNotBlank)
                ?.let { type -> runCatching { app.workspace.records(active, type) }.getOrNull() }
            _state.update { current ->
                current.copy(
                    posSaving = false,
                    posCart = emptyMap(),
                    posProducts = products?.let { rows -> (0 until rows.length()).mapNotNull(rows::optJSONObject) } ?: current.posProducts,
                    message = "Sale saved",
                )
            }
            observeLocalWork()
            if (app.workspace.networkAvailable) syncQueuedMutations()
        } catch (error: Exception) {
            _state.update { it.copy(posSaving = false, message = error.message ?: "Could not save sale on this device") }
        }
    }

    private fun resourceRoute(doctype: String): String = _state.value.navigation?.modules?.flatMap { it.doctypes }
        ?.firstOrNull { it.name.equals(doctype, true) }?.resourceName
        ?: doctype.lowercase().replace(' ', '-')

    private fun posComponent(type: String): JSONObject? {
        val children = _state.value.posManifest?.optJSONObject("spec")?.optJSONObject("layout")?.optJSONArray("children") ?: return null
        return (0 until children.length()).mapNotNull { children.optJSONObject(it) }.firstOrNull { it.optString("component") == type }
    }

    fun openDoctype(item: DocTypeNavItem) = viewModelScope.launch {
        recordRowsJob?.cancel()
        _state.update { it.copy(selectedDoctype = item, selectedRecord = null, section = "Work", formOpen = false, formValues = emptyMap(), listSearch = "", busy = true, message = null) }
        val active = scope ?: return@launch
        try {
            val records = app.workspace.records(active, item.resourceName)
            val recordsCached = app.workspace.lastResponseWasCached
            val schema = app.workspace.schema(active, item.resourceName)
            val schemaCached = app.workspace.lastResponseWasCached
            val fields = parseFields(schema)
            val doctype = schema.optJSONObject("doctype")
            val route = "/" + item.name.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
            val manifest = runCatching { app.workspace.pageManifest(active, route) }.getOrNull()
            val manifestCached = app.workspace.lastResponseWasCached
            val recordList = (0 until records.length()).mapNotNull { i -> records.optJSONObject(i) }
            _state.update { it.copy(
                records = recordList,
                schemaFields = fields,
                titleField = doctype?.optString("title_field").orEmpty().ifBlank { "name" },
                doctypeManifest = manifest?.takeIf { it.has("spec") },
                busy = false,
                offline = recordsCached || schemaCached || manifestCached,
            ) }
            observeRecordRows(active, item)
            resolveUserLabels(fields, recordList)
        } catch (error: Exception) { _state.update { it.copy(busy = false, offline = true, message = error.message) } }
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
        _state.update { it.copy(selectedRecord = record, editingRecord = null, formOpen = false, detailLoading = true, detailLinkLabels = emptyMap(), message = null) }
        viewModelScope.launch {
            val active = scope ?: return@launch
            try {
                val fullRecord = app.workspace.record(active, doctype.resourceName, name)
                _state.update { current ->
                    if (current.selectedRecord?.optString("name") != name) current
                    else current.copy(selectedRecord = fullRecord, detailLoading = false, offline = current.offline || app.workspace.lastResponseWasCached)
                }
                val detailLabels = resolveDetailLinkLabels(active, fullRecord)
                _state.update { current -> if (current.selectedRecord?.optString("name") == name) current.copy(detailLinkLabels = detailLabels) else current }
                resolveUserLabels(_state.value.schemaFields, listOf(fullRecord))
            } catch (error: Exception) {
                _state.update { current ->
                    if (current.selectedRecord?.optString("name") != name) current
                    else current.copy(detailLoading = false, offline = true, message = error.message ?: "Showing the saved list details")
                }
            }
        }
    }

    private suspend fun resolveDetailLinkLabels(active: Scope, record: JSONObject): Map<String, String> {
        val labels = mutableMapOf<String, String>()
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
                val titleField = targetSchema?.optJSONObject("doctype")?.optString("title_field").orEmpty().ifBlank { "name" }
                references.forEach { reference ->
                    val related = runCatching { app.workspace.record(active, resourceRoute(targetType), reference) }.getOrNull()
                    val label = related?.optString(titleField).orEmpty().ifBlank { reference }
                    labels["$targetType:$reference"] = label
                }
            }
        }
        return labels
    }
    fun closeRecordDetail() { _state.update { it.copy(selectedRecord = null, message = null) } }

    fun startNewRecord() {
        val defaults = _state.value.schemaFields.mapNotNull { field -> field.defaultValue?.let { field.fieldname to parseDefault(field.fieldtype, it) } }.toMap()
        _state.update { it.copy(selectedRecord = null, editingRecord = null, formValues = defaults, formOpen = true, resolvingMutationId = null, message = null) }
    }
    fun editRecord(record: JSONObject) {
        val values = _state.value.schemaFields.mapNotNull { field ->
            if (!record.has(field.fieldname) || record.isNull(field.fieldname)) null else field.fieldname to record.opt(field.fieldname)
        }.toMap()
        _state.update { it.copy(selectedRecord = record, editingRecord = record, formValues = values, formOpen = true, resolvingMutationId = null) }
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
            val titleField = schema.optJSONObject("doctype")?.optString("title_field").orEmpty().ifBlank { "name" }
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

    fun submitRecord() = viewModelScope.launch {
        val active = scope ?: return@launch
        val doctype = _state.value.selectedDoctype ?: return@launch
        val values = _state.value.formValues
        val missing = _state.value.schemaFields.firstOrNull { field ->
            !field.hidden && isFieldVisible(field, values) && isFieldRequired(field, values) && (values[field.fieldname] == null || values[field.fieldname] == JSONObject.NULL || values[field.fieldname].toString().isBlank())
        }
        if (missing != null) { _state.update { it.copy(message = "${missing.label} is required") }; return@launch }
        val name = _state.value.editingRecord?.optString("name")?.takeIf(String::isNotBlank)
        val payload = FormPayloadSerializer.serialize(_state.value.schemaFields, values, _state.value.childSchemas, isNew = name == null)
        _state.update { it.copy(busy = true, message = null) }
        val idempotencyKey = java.util.UUID.randomUUID().toString()
        queueRecordForSync(active, doctype.resourceName, name, payload, idempotencyKey)
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
        loadWorkspace(); observeLocalWork()
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
            val brandingResponse = runCatching { app.workspace.branding(active) }.getOrNull()
            val brandingJson = brandingResponse?.optJSONObject("branding") ?: brandingResponse
            val tenantBranding = brandingJson?.let { parseBranding(it) } ?: config.branding
            val savedTheme = app.getSharedPreferences("kora_preferences", 0).getString("theme_mode", null)
            val themeMode = (savedTheme ?: tenantBranding?.defaultMode ?: "system").lowercase()
            val hasProducts = config.modules.flatMap { it.doctypes }.any { it.name.equals("Product", true) }
            val posManifest = if (hasProducts) runCatching { app.workspace.pageManifest(active, "/pos") }.getOrNull() else null
            _state.update { it.copy(navigation = config.copy(branding = tenantBranding), tenantBranding = tenantBranding, themeMode = themeMode, posManifest = posManifest, offline = navCached || app.workspace.lastResponseWasCached, message = null) }
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
            ModuleGroup(module.optString("module"), module.optString("label", module.optString("module")), items)
        }
        val brandingObj = raw.optJSONObject("branding")
        val user = raw.optJSONObject("user")?.let { obj -> CurrentUser(obj.optString("name"), obj.optString("email"), obj.optString("full_name"), jsonStringArray(obj.optJSONArray("roles"))) }
        return NavigationConfig(modules, brandingObj?.let(::parseBranding), user,
            jsonStringArray(raw.optJSONArray("admin_capabilities")), jsonStringArray(raw.optJSONArray("supported_capabilities")))
    }

    private fun parseBranding(raw: JSONObject) = Branding(
        appName = raw.optString("app_name", "Kora"),
        primaryColor = raw.optString("primary_color").ifBlank { null },
        accentColor = raw.optString("accent_color").ifBlank { null },
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
                editableOnCreate = field.optBoolean("editable_on_create") ||
                    (raw.optJSONObject("doctype")?.optString("name") == "Product" && name == "stock_qty"),
                hidden = field.optBoolean("hidden"),
                collapsible = field.optBoolean("collapsible"),
                collapsed = field.optBoolean("collapsed"),
                dependsOn = field.optString("depends_on").ifBlank { null },
                mandatoryDependsOn = field.optString("mandatory_depends_on").ifBlank { null },
                description = field.optString("description").ifBlank { null },
                inListView = field.optBoolean("in_list_view"),
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

    private fun parseDefault(fieldType: String, raw: String): Any? = when (fieldType) {
        "Check" -> raw == "1" || raw.equals("true", true)
        "Int" -> raw.toIntOrNull() ?: raw
        "Float", "Currency", "Percent" -> raw.toDoubleOrNull() ?: raw
        "Date" -> if (raw == "today" || raw == "__today__") java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date()) else raw
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
