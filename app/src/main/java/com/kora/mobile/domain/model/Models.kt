package com.kora.mobile.domain.model

import org.json.JSONArray
import org.json.JSONObject

/** Resolve a component's declared data binding to its manifest resource. */
fun resolveComponentResourceDoctype(spec: JSONObject, component: JSONObject?): String? {
    component?.optJSONObject("props")?.optString("source_doctype")
        ?.takeIf(String::isNotBlank)?.let { return it }

    val resources = spec.optJSONArray("resources") ?: JSONArray()
    val dataBinding = component?.optString("data").orEmpty()
    val resourceId = dataBinding.substringBefore('.').takeIf { dataBinding.contains('.') && it.isNotBlank() }
    if (resourceId != null) {
        (0 until resources.length()).asSequence()
            .mapNotNull(resources::optJSONObject)
            .firstOrNull { it.optString("id") == resourceId }
            ?.optJSONObject("params")?.optString("doctype")
            ?.takeIf(String::isNotBlank)?.let { return it }
    }

    return resources.optJSONObject(0)?.optJSONObject("params")?.optString("doctype")
        ?.takeIf(String::isNotBlank)
}

data class InstanceConfig(val id: String, val origin: String, val siteRef: String? = null, val label: String)
data class CurrentUser(val name: String, val email: String = "", val fullName: String = "", val roles: List<String> = emptyList())
data class AuthProvider(val name: String, val label: String)
data class ModuleGroup(
    val module: String,
    val label: String,
    val doctypes: List<DocTypeNavItem> = emptyList(),
    val icon: String? = null,
    val order: Int? = null,
    val description: String? = null,
)
data class DocTypeNavItem(val name: String, val resourceName: String = name, val label: String, val icon: String? = null, val isChild: Boolean = false)
data class NavigationView(val name: String, val label: String, val route: String, val type: String? = null, val module: String? = null)
data class NavigationConfig(val modules: List<ModuleGroup> = emptyList(), val branding: Branding? = null, val user: CurrentUser? = null, val adminCapabilities: List<String> = emptyList(), val supportedCapabilities: List<String> = emptyList(), val views: List<NavigationView> = emptyList())
data class Branding(
    val appName: String = "Kora",
    val shortName: String? = null,
    val logoUrl: String? = null,
    val logoDarkUrl: String? = null,
    val faviconUrl: String? = null,
    val primaryColor: String? = null,
    val accentColor: String? = null,
    val fontFamily: String? = null,
    val headingFont: String? = null,
    val defaultMode: String? = null,
)
data class LoginRequest(val email: String, val password: String)
data class DocTypeSchema(val name: String = "", val label: String = "", val fields: List<FieldSchema> = emptyList())
data class FieldSchema(
    val fieldname: String,
    val label: String = fieldname,
    val fieldtype: String = "Data",
    val options: String? = null,
    val reqd: Boolean = false,
    val readOnly: Boolean = false,
    val editableOnCreate: Boolean = false,
    val computed: String? = null,
    val hidden: Boolean = false,
    val defaultValue: String? = null,
    val dependsOn: String? = null,
    val mandatoryDependsOn: String? = null,
    val description: String? = null,
    val inListView: Boolean = false,
    val inStandardFilter: Boolean = false,
    val linkedField: String? = null,
    val bold: Boolean = false,
    val accept: String? = null,
    val constraints: List<FieldConstraint> = emptyList(),
    val collapsible: Boolean = false,
    val collapsed: Boolean = false,
)
data class FieldConstraint(
    val type: String,
    val value: String? = null,
    val values: List<String> = emptyList(),
    val pattern: String? = null,
    val message: String = "",
    val condition: String? = null,
)
data class Scope(val instanceId: String, val origin: String, val siteRef: String?, val userId: String) {
    val key: String get() = listOf(origin.trimEnd('/'), siteRef.orEmpty(), userId).joinToString("|")
}
data class KoraError(val code: String, override val message: String, val status: Int? = null, val field: String? = null, val retryable: Boolean = false) : Exception(message)
