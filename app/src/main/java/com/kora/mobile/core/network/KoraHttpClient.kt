package com.kora.mobile.core.network

import com.kora.mobile.core.security.SessionSecretStore
import com.kora.mobile.domain.model.KoraError
import com.kora.mobile.domain.model.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLEncoder

data class KoraHttpResponse(val status: Int, val headers: Map<String, List<String>>, val body: Any, val scope: Scope)

internal fun requestScopeHeaders(scope: Scope): Map<String, String> = if (scope.businessScope.kind == "legal_entity") {
    mapOf("X-Kora-Legal-Entity-ID" to requireNotNull(scope.businessScope.legalEntityId))
} else emptyMap()

class KoraHttpClient(vault: SessionSecretStore) {
    private val cookies = SessionCookieJar(vault)

    suspend fun get(scope: Scope, path: String, query: Map<String, String> = emptyMap()): Any = request(scope, "GET", path, null, query)
    suspend fun post(scope: Scope, path: String, body: JSONObject? = null, idempotencyKey: String? = null): Any = request(scope, "POST", path, body, idempotencyKey = idempotencyKey)
    suspend fun put(scope: Scope, path: String, body: JSONObject? = null, idempotencyKey: String? = null): Any = request(scope, "PUT", path, body, idempotencyKey = idempotencyKey)
    suspend fun delete(scope: Scope, path: String): Any = request(scope, "DELETE", path)
    suspend fun getResponse(scope: Scope, path: String, query: Map<String, String> = emptyMap()): KoraHttpResponse = requestResponse(scope, "GET", path, null, query)
    suspend fun postResponse(scope: Scope, path: String, body: JSONObject? = null, idempotencyKey: String? = null): KoraHttpResponse = requestResponse(scope, "POST", path, body, idempotencyKey = idempotencyKey)
    fun clearSession(scope: Scope) = cookies.clear(scope)
    fun bindAnonymousSession(from: Scope, to: Scope) = cookies.bindAnonymousSession(from, to)

    private suspend fun request(scope: Scope, method: String, path: String, body: JSONObject? = null, query: Map<String, String> = emptyMap(), idempotencyKey: String? = null): Any = requestResponse(scope, method, path, body, query, idempotencyKey).body

    private suspend fun requestResponse(scope: Scope, method: String, path: String, body: JSONObject? = null, query: Map<String, String> = emptyMap(), idempotencyKey: String? = null): KoraHttpResponse = withContext(Dispatchers.IO) {
        val uri = URI(scope.origin)
        val sitePath = scope.siteRef?.takeIf { it.isNotBlank() }?.let { "/s/${it.trim('/')}" }.orEmpty()
        val basePath = (uri.path.trimEnd('/') + sitePath).replace("//", "/").trimEnd('/')
        val encodedPath = (basePath + path).split('/').filter(String::isNotBlank).joinToString("/", prefix = "/") { segment ->
            if (segment.contains('%')) segment else URLEncoder.encode(segment, "UTF-8").replace("+", "%20")
        }
        val queryString = query.entries.joinToString("&") { (k, v) -> "${URLEncoder.encode(k, "UTF-8")}=${URLEncoder.encode(v, "UTF-8")}" }
        val address = URL("${uri.scheme}://${uri.rawAuthority}$encodedPath" + if (queryString.isBlank()) "" else "?$queryString")
        val connection = (address.openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15000
            readTimeout = 25000
            setRequestProperty("Accept", "application/json")
            requestScopeHeaders(scope).forEach { (name, value) -> setRequestProperty(name, value) }
            idempotencyKey?.takeIf(String::isNotBlank)?.let { setRequestProperty("Idempotency-Key", it) }
            cookies.header(scope, encodedPath)?.let { setRequestProperty("Cookie", it) }
            if (method !in setOf("GET", "HEAD")) {
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                cookies.csrf(scope)?.let { setRequestProperty("X-Kora-CSRF-Token", it) }
                doOutput = true
            }
        }
        try {
            if (method !in setOf("GET", "HEAD")) connection.outputStream.use { it.write((body?.toString() ?: "{}").toByteArray(Charsets.UTF_8)) }
            val status = connection.responseCode
            val setCookies = connection.headerFields.entries.filter { it.key.equals("Set-Cookie", true) }.flatMap { it.value.orEmpty() }
            cookies.save(scope, setCookies)
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val raw = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            val parsed: Any? = raw.takeIf(String::isNotBlank)?.let { runCatching { JSONTokener(it).nextValue() }.getOrNull() }
            if (status !in 200..299) throw decodeError(status, parsed)
            val data = when (parsed) { is JSONObject -> parsed.opt("data") ?: parsed; is JSONArray -> parsed; else -> parsed ?: JSONObject() }
            KoraHttpResponse(status, connection.headerFields.filterKeys { it != null }.mapKeys { it.key!! }, data, scope)
        } catch (error: KoraError) { throw error }
        catch (error: IOException) { throw KoraError("network_error", "Could not reach ${uri.host}. Check your connection.", retryable = true) }
        finally { connection.disconnect() }
    }

    private fun decodeError(status: Int, payload: Any?): KoraError {
        val error = (payload as? JSONObject)?.opt("error")
        val obj = error as? JSONObject
        val rawMessage = when (error) { is String -> error; is JSONObject -> error.optString("message", "Request failed ($status)"); else -> "Request failed ($status)" }
        val message = when {
            rawMessage.contains("uq_tabProduct_reference", ignoreCase = true) ||
                (rawMessage.contains("duplicate", ignoreCase = true) && rawMessage.contains("product_reference", ignoreCase = true)) ->
                "A product with this reference already exists. Change the reference and try again."
            else -> rawMessage
        }
        val code = obj?.optString("code")?.takeIf(String::isNotBlank) ?: "http_$status"
        return KoraError(code, message, status, obj?.optString("field")?.takeIf(String::isNotBlank), status >= 500 || status == 429)
    }
}
