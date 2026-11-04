package com.kora.mobile.core.network

import com.kora.mobile.core.security.SessionSecretStore
import org.json.JSONArray
import org.json.JSONObject
import com.kora.mobile.domain.model.Scope
import java.net.HttpCookie
import java.net.URI

class SessionCookieJar(private val vault: SessionSecretStore) {
    fun header(scope: Scope, path: String): String? = load(key(scope)).filter { cookie ->
        val req = path.ifBlank { "/" }
        (cookie.path.isNullOrBlank() || req.startsWith(cookie.path)) && (!cookie.secure || scope.origin.startsWith("https://")) && (cookie.maxAge < 0 || cookie.maxAge > 0)
    }.joinToString("; ") { "${it.name}=${it.value}" }.ifBlank { null }

    fun save(scope: Scope, setCookies: List<String>) {
        val jarKey = key(scope)
        val cookies = load(jarKey).associateBy { it.name }.toMutableMap()
        val host = URI(scope.origin).host
        setCookies.forEach { raw ->
            runCatching { HttpCookie.parse(raw) }.getOrDefault(emptyList()).forEach { cookie ->
                if (cookie.name == "kora_sid" || cookie.name == "kora_csrf") {
                    if (cookie.maxAge == 0L) cookies.remove(cookie.name) else cookies[cookie.name] = cookie
                }
            }
        }
        val rows = JSONArray()
        cookies.values.forEach { cookie -> rows.put(JSONObject().put("name", cookie.name).put("value", cookie.value).put("domain", cookie.domain ?: host).put("path", cookie.path ?: "/").put("secure", cookie.secure).put("httpOnly", cookie.isHttpOnly).put("maxAge", cookie.maxAge)) }
        vault.put(jarKey, rows.toString())
    }

    fun csrf(scope: Scope): String? = load(key(scope)).firstOrNull { it.name == "kora_csrf" }?.value
    fun clear(scope: Scope) = vault.remove(key(scope))

    fun bindAnonymousSession(from: Scope, to: Scope) {
        require(from.userId.isBlank() && to.userId.isNotBlank() && from.origin == to.origin && from.siteRef == to.siteRef && from.businessScope.tenantId == to.businessScope.tenantId)
        val source = load(key(from))
        if (source.isNotEmpty()) {
            val rows = JSONArray()
            source.forEach { cookie -> rows.put(JSONObject().put("name", cookie.name).put("value", cookie.value).put("domain", cookie.domain ?: URI(to.origin).host).put("path", cookie.path ?: "/").put("secure", cookie.secure).put("httpOnly", cookie.isHttpOnly).put("maxAge", cookie.maxAge)) }
            vault.put(key(to), rows.toString())
        }
        clear(from)
    }

    private fun load(key: String): List<HttpCookie> {
        val raw = vault.get(key) ?: return emptyList()
        val rows = runCatching { JSONArray(raw) }.getOrNull() ?: return emptyList()
        return (0 until rows.length()).mapNotNull { i -> runCatching {
            val row = rows.getJSONObject(i)
            HttpCookie(row.getString("name"), row.getString("value")).apply {
                domain = row.optString("domain"); path = row.optString("path", "/"); secure = row.optBoolean("secure"); isHttpOnly = row.optBoolean("httpOnly"); maxAge = row.optLong("maxAge", -1)
            }
        }.getOrNull() }
    }

    private fun key(scope: Scope): String = "cookies_${scope.instanceId}_${scope.businessScope.tenantId}_${scope.userId}"
}
