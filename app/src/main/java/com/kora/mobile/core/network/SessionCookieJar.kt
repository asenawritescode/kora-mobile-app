package com.kora.mobile.core.network

import com.kora.mobile.core.security.SecretVault
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpCookie
import java.net.URI

class SessionCookieJar(private val vault: SecretVault) {
    fun header(origin: String, siteRef: String?, path: String): String? = load(key(origin, siteRef)).filter { cookie ->
        val req = path.ifBlank { "/" }
        (cookie.path.isNullOrBlank() || req.startsWith(cookie.path)) && (!cookie.secure || origin.startsWith("https://")) && (cookie.maxAge < 0 || cookie.maxAge > 0)
    }.joinToString("; ") { "${it.name}=${it.value}" }.ifBlank { null }

    fun save(origin: String, siteRef: String?, setCookies: List<String>) {
        val jarKey = key(origin, siteRef)
        val cookies = load(jarKey).associateBy { it.name }.toMutableMap()
        val host = URI(origin).host
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

    fun csrf(origin: String, siteRef: String?): String? = load(key(origin, siteRef)).firstOrNull { it.name == "kora_csrf" }?.value
    fun clear(origin: String, siteRef: String?) = vault.remove(key(origin, siteRef))

    fun copySession(fromOrigin: String, toOrigin: String, siteRef: String?) {
        val source = load(key(fromOrigin, siteRef)).filter { it.name == "kora_sid" || it.name == "kora_csrf" }
        val host = URI(toOrigin).host
        val rows = JSONArray()
        source.forEach { cookie -> rows.put(JSONObject().put("name", cookie.name).put("value", cookie.value).put("domain", host).put("path", cookie.path ?: "/").put("secure", cookie.secure).put("httpOnly", cookie.isHttpOnly).put("maxAge", cookie.maxAge)) }
        if (source.isNotEmpty()) vault.put(key(toOrigin, siteRef), rows.toString())
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

    private fun key(origin: String, siteRef: String?): String {
        val uri = URI(origin)
        val path = uri.path.trimEnd('/') + (siteRef?.takeIf { it.isNotBlank() }?.let { "/s/$it" } ?: "")
        return "cookies_${uri.host}_${path.replace('/', '_')}"
    }
}
