package com.kora.mobile.core.contracts

import com.kora.mobile.domain.model.BusinessScope
import org.json.JSONArray
import org.json.JSONObject

data class EngineSchemaRef(val id: String, val version: Int)
data class EngineBusinessRef(val providerId: String, val resourceType: String, val id: String, val scope: BusinessScope)
data class EngineOperationReceipt(
    val executionId: String,
    val operationRef: String,
    val businessScope: BusinessScope,
    val status: String,
    val outputSchema: EngineSchemaRef?,
    val output: Any?,
    val errorType: String?,
    val errorMessage: String?,
    val revision: String,
    val correlationId: String?,
)
data class EngineQueryPage(
    val data: JSONArray,
    val nextCursor: String?,
    val snapshotToken: String?,
    val sourceCursor: String?,
    val sourceRevision: String?,
    val asOf: String,
    val freshness: String,
)
data class EnginePublishedQuery(
    val ownerModuleId: String,
    val name: String,
    val route: String,
    val inputSchema: EngineSchemaRef,
    val outputSchema: EngineSchemaRef,
    val scopePolicy: String,
    val pageLimit: Int,
    val offlineEligible: Boolean,
    val allowedFields: List<String>,
    val presentation: JSONObject,
)
data class EnginePublishedProjection(
    val ownerModuleId: String,
    val id: String,
    val route: String,
    val outputSchema: EngineSchemaRef,
    val scopePolicy: String,
    val allowedFields: List<String>,
)
data class EnginePublishedOperation(
    val operationRef: String,
    val ownerModuleId: String,
    val inputSchema: EngineSchemaRef,
    val outputSchema: EngineSchemaRef,
    val availability: String,
    val offlineEligible: Boolean,
    val presentation: JSONObject,
)
data class EngineCatalogModule(val id: String, val version: String, val state: String)
data class EngineSchemaDocument(val ref: EngineSchemaRef, val schema: JSONObject)
data class EngineCatalogBlocker(val code: String, val count: Int)
data class EngineApplicationCatalog(
    val revision: Long,
    val businessScope: BusinessScope,
    val modules: List<EngineCatalogModule>,
    val queries: List<EnginePublishedQuery>,
    val projections: List<EnginePublishedProjection>,
    val operations: List<EnginePublishedOperation>,
    val schemas: List<EngineSchemaDocument>,
    val allowedEntityChoices: List<BusinessScope>,
    val blockers: List<EngineCatalogBlocker>,
)
data class EngineWorkflowInstance(
    val id: String,
    val definitionId: String,
    val definitionVersion: Int,
    val currentStep: String,
    val status: String,
    val revision: String,
    val subjectId: String,
    val subjectType: String,
    val businessScope: BusinessScope?,
)

fun JSONObject.requiredObject(key: String): JSONObject = optJSONObject(key) ?: throw IllegalArgumentException("$key must be an object")
fun JSONObject.requiredString(key: String): String = optString(key).takeIf(String::isNotBlank) ?: throw IllegalArgumentException("$key must be a non-empty string")
fun JSONObject.requiredInt(key: String): Int = if (has(key) && !isNull(key)) getInt(key) else throw IllegalArgumentException("$key must be an integer")
fun JSONObject.optionalSchemaRef(key: String): EngineSchemaRef? = if (!has(key) || isNull(key)) null else requiredObject(key).let { EngineSchemaRef(it.requiredString("id"), it.requiredInt("version")) }
fun JSONObject.requiredBusinessScope(key: String): BusinessScope = requiredObject(key).toBusinessScope()
fun JSONObject.toBusinessScope(): BusinessScope = BusinessScope(
    tenantId = requiredString("tenant_id"),
    kind = requiredString("kind"),
    legalEntityId = optString("legal_entity_id").takeIf(String::isNotBlank),
)

/** Schema validation always runs before values are converted to typed DTOs. */
fun EngineContractDecoder.decodeBusinessScope(json: String): BusinessScope = decode("BusinessScope", json).toBusinessScope()

fun EngineContractDecoder.decodeBusinessRef(json: String): EngineBusinessRef {
    val value = decode("BusinessRef", json)
    return EngineBusinessRef(value.requiredString("provider_id"), value.requiredString("resource_type"), value.requiredString("id"), value.requiredBusinessScope("scope"))
}

fun EngineContractDecoder.decodeOperationReceipt(json: String): EngineOperationReceipt {
    val value = decode("OperationReceipt", json)
    val error = value.optJSONObject("error")
    return EngineOperationReceipt(value.requiredString("execution_id"), value.requiredString("operation_ref"), value.requiredBusinessScope("business_scope"), value.requiredString("status"), value.optionalSchemaRef("output_schema"), value.opt("output").takeUnless { it == JSONObject.NULL }, error?.optString("type"), error?.optString("message"), value.requiredString("revision"), value.optString("correlation_id").takeIf(String::isNotBlank))
}

fun EngineContractDecoder.decodeQueryPage(json: String): EngineQueryPage {
    val value = decode("QueryResult", json)
    return EngineQueryPage(value.optJSONArray("data") ?: throw IllegalArgumentException("data must be an array"), value.optString("next_cursor").takeIf(String::isNotBlank), value.optString("snapshot_token").takeIf(String::isNotBlank), value.optString("source_cursor").takeIf(String::isNotBlank), value.optString("source_revision").takeIf(String::isNotBlank), value.requiredString("as_of"), value.requiredString("freshness"))
}

fun EngineContractDecoder.decodeApplicationCatalog(json: String): EngineApplicationCatalog {
    val value = decode("ApplicationCatalog", json)
    fun <T> array(key: String, parse: (JSONObject) -> T): List<T> {
        val entries = value.optJSONArray(key) ?: throw IllegalArgumentException("$key must be an array")
        return (0 until entries.length()).map { index -> parse(entries.optJSONObject(index) ?: throw IllegalArgumentException("$key[$index] must be an object")) }
    }
    fun JSONObject.strings(key: String): List<String> {
        val entries = optJSONArray(key) ?: throw IllegalArgumentException("$key must be an array")
        return (0 until entries.length()).map { entries.getString(it) }
    }
    val queries = value.optJSONArray("published_queries") ?: JSONArray()
    val projections = value.optJSONArray("published_projections") ?: JSONArray()
    val operations = value.optJSONArray("published_operations") ?: JSONArray()
    val modules = array("modules") { item -> EngineCatalogModule(item.requiredString("id"), item.requiredString("version"), item.requiredString("state")) }
    val schemaDocuments = array("schemas") { item -> EngineSchemaDocument(item.requiredObject("ref").asSchemaRef(), item.requiredObject("schema")) }
    val blockers = array("blockers") { item -> EngineCatalogBlocker(item.requiredString("code"), item.requiredInt("count")) }
    val entityChoices = value.optJSONArray("allowed_entity_choices") ?: throw IllegalArgumentException("allowed_entity_choices must be an array")
    val queryList = (0 until queries.length()).map { index ->
        val item = queries.getJSONObject(index)
        EnginePublishedQuery(item.requiredString("owner_module_id"), item.requiredString("name"), item.requiredString("route"), item.requiredObject("input_schema").asSchemaRef(), item.requiredObject("output_schema").asSchemaRef(), item.requiredString("scope_policy"), item.requiredInt("page_limit"), item.optBoolean("offline_eligible"), item.strings("allowed_fields"), item.requiredObject("presentation"))
    }
    val projectionList = (0 until projections.length()).map { index ->
        val item = projections.getJSONObject(index)
        EnginePublishedProjection(item.requiredString("owner_module_id"), item.requiredString("id"), item.requiredString("route"), item.requiredObject("output_schema").asSchemaRef(), item.requiredString("scope_policy"), item.strings("allowed_fields"))
    }
    val operationList = (0 until operations.length()).map { index ->
        val item = operations.getJSONObject(index)
        EnginePublishedOperation(item.requiredString("operation_ref"), item.requiredString("owner_module_id"), item.requiredObject("input_schema").asSchemaRef(), item.requiredObject("output_schema").asSchemaRef(), item.requiredString("availability"), item.optBoolean("offline_eligible"), item.optJSONObject("presentation") ?: JSONObject())
    }
    return EngineApplicationCatalog(value.getLong("revision"), value.requiredBusinessScope("business_scope"), modules, queryList, projectionList, operationList, schemaDocuments,
        (0 until entityChoices.length()).map { entityChoices.getJSONObject(it).toBusinessScope() }, blockers)
}

fun EngineContractDecoder.decodeWorkflowInstance(json: String): EngineWorkflowInstance {
    val value = decode("WorkflowInstance", json)
    val subject = value.requiredObject("subject")
    return EngineWorkflowInstance(value.requiredString("id"), value.requiredString("definition_id"), value.requiredInt("definition_version"), value.requiredString("current_step"), value.requiredString("status"), value.requiredString("revision"), subject.requiredString("id"), subject.requiredString("kind"), if (value.has("business_scope") && !value.isNull("business_scope")) value.requiredObject("business_scope").toBusinessScope() else null)
}

private fun JSONObject.asSchemaRef() = EngineSchemaRef(requiredString("id"), requiredInt("version"))
