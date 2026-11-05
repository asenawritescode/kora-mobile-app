package com.kora.mobile.core.network

import com.kora.mobile.core.security.SecretVault
import com.kora.mobile.domain.model.KoraError
import com.kora.mobile.domain.model.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLEncoder
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class KoraHttpClient(vault: SecretVault) {
    private val cookies = SessionCookieJar(vault)

    suspend fun get(scope: Scope, path: String, query: Map<String, String> = emptyMap()): Any = request(scope, "GET", path, null, query)
    suspend fun post(scope: Scope, path: String, body: Any? = null, idempotencyKey: String? = null): Any = request(scope, "POST", path, body, idempotencyKey = idempotencyKey)
    suspend fun put(scope: Scope, path: String, body: Any? = null, idempotencyKey: String? = null): Any = request(scope, "PUT", path, body, idempotencyKey = idempotencyKey)
    suspend fun delete(scope: Scope, path: String): Any = request(scope, "DELETE", path)
    suspend fun streamSse(
        scope: Scope,
        path: String,
        lastEventId: String? = null,
        onEvent: (String, JSONObject) -> Unit,
    ) = suspendCancellableCoroutine { continuation ->
        val uri = URI(scope.origin)
        val sitePath = scope.siteRef?.takeIf { it.isNotBlank() }?.let { "/s/${it.trim('/')}" }.orEmpty()
        val basePath = (uri.path.trimEnd('/') + sitePath).replace("//", "/").trimEnd('/')
        val encodedPath = (basePath + path).split('/').filter(String::isNotBlank).joinToString("/", prefix = "/") { segment ->
            if (segment.contains('%')) segment else URLEncoder.encode(segment, "UTF-8").replace("+", "%20")
        }
        val address = URL("${uri.scheme}://${uri.rawAuthority}$encodedPath")
        val connection = (address.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15000
            readTimeout = 0
            setRequestProperty("Accept", "text/event-stream")
            setRequestProperty("Cache-Control", "no-cache")
            lastEventId?.takeIf(String::isNotBlank)?.let { setRequestProperty("Last-Event-ID", it) }
            cookies.header(scope.origin, scope.siteRef, encodedPath)?.let { setRequestProperty("Cookie", it) }
        }
        continuation.invokeOnCancellation { connection.disconnect() }
        Thread({
            try {
                val status = connection.responseCode
                val setCookies = connection.headerFields.entries.filter { it.key.equals("Set-Cookie", true) }.flatMap { it.value.orEmpty() }
                cookies.save(scope.origin, scope.siteRef, setCookies)
                if (status !in 200..299) {
                    val raw = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                    val parsed = raw.takeIf(String::isNotBlank)?.let { runCatching { JSONTokener(it).nextValue() }.getOrNull() }
                    throw decodeError(status, parsed)
                }
                var eventType = "message"
                var eventId: String? = null
                val data = StringBuilder()
                connection.inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                    while (!continuation.isCancelled) {
                        val line = reader.readLine() ?: break
                        when {
                            line.startsWith("event:") -> eventType = line.substringAfter(':').trim()
                            line.startsWith("id:") -> eventId = line.substringAfter(':').trim()
                            line.startsWith("data:") -> {
                                if (data.isNotEmpty()) data.append('\n')
                                data.append(line.substringAfter(':').trimStart())
                            }
                            line.isEmpty() -> {
                                if (data.isNotEmpty()) {
                                    val event = runCatching { JSONTokener(data.toString()).nextValue() as? JSONObject }.getOrNull()
                                    if (event != null && !continuation.isCancelled) {
                                        eventId?.takeIf(String::isNotBlank)?.let { event.put("stream_id", it) }
                                        onEvent(eventType, event)
                                    }
                                }
                                eventType = "message"
                                eventId = null
                                data.clear()
                            }
                            line.startsWith(":") -> Unit
                        }
                    }
                }
                if (!continuation.isCancelled) continuation.resume(Unit)
            } catch (error: Exception) {
                if (!continuation.isCancelled) continuation.resumeWithException(error)
            } finally {
                connection.disconnect()
            }
        }, "kora-mobile-realtime").apply { isDaemon = true }.start()
    }
    fun clearSession(scope: Scope) = cookies.clear(scope.origin, scope.siteRef)
    fun copySession(fromOrigin: String, toOrigin: String, siteRef: String?) = cookies.copySession(fromOrigin, toOrigin, siteRef)

    private suspend fun request(scope: Scope, method: String, path: String, body: Any? = null, query: Map<String, String> = emptyMap(), idempotencyKey: String? = null): Any = withContext(Dispatchers.IO) {
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
            idempotencyKey?.takeIf(String::isNotBlank)?.let { setRequestProperty("Idempotency-Key", it) }
            cookies.header(scope.origin, scope.siteRef, encodedPath)?.let { setRequestProperty("Cookie", it) }
            if (method !in setOf("GET", "HEAD")) {
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                cookies.csrf(scope.origin, scope.siteRef)?.let { setRequestProperty("X-Kora-CSRF-Token", it) }
                doOutput = true
            }
        }
        try {
            if (method !in setOf("GET", "HEAD")) connection.outputStream.use { it.write((body?.toString() ?: "{}").toByteArray(Charsets.UTF_8)) }
            val status = connection.responseCode
            val setCookies = connection.headerFields.entries.filter { it.key.equals("Set-Cookie", true) }.flatMap { it.value.orEmpty() }
            cookies.save(scope.origin, scope.siteRef, setCookies)
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val raw = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            val parsed: Any? = raw.takeIf(String::isNotBlank)?.let { runCatching { JSONTokener(it).nextValue() }.getOrNull() }
            if (status !in 200..299) throw decodeError(status, parsed)
            when (parsed) { is JSONObject -> parsed.opt("data") ?: parsed; is JSONArray -> parsed; else -> parsed ?: JSONObject() }
        } catch (error: KoraError) { throw error }
        catch (error: IOException) { throw KoraError("network_error", "Could not reach ${uri.host}. Check your connection.", retryable = true) }
        finally { connection.disconnect() }
    }

    private fun decodeError(status: Int, payload: Any?): KoraError {
        val error = (payload as? JSONObject)?.opt("error")
        val obj = error as? JSONObject
        val rawMessage = when (error) { is String -> error; is JSONObject -> error.optString("message", "Request failed ($status)"); else -> "Request failed ($status)" }
        val rawDetails = obj?.opt("details")
        val details = rawDetails as? JSONObject
        val rawFields = details?.opt("fields") ?: rawDetails
        val fieldErrors = when (rawFields) {
            is JSONObject -> listOf(rawFields)
            is JSONArray -> (0 until rawFields.length()).mapNotNull(rawFields::optJSONObject)
            else -> emptyList()
        }
        val detailMessage = details?.optString("message")?.takeIf(String::isNotBlank)
            ?: fieldErrors.mapNotNull { it.optString("message").takeIf(String::isNotBlank) }
                .distinct().joinToString(" ").takeIf(String::isNotBlank)
        val message = when {
            rawMessage.equals("Validation failed", ignoreCase = true) && detailMessage != null -> detailMessage
            rawMessage.contains("uq_tabProduct_reference", ignoreCase = true) ||
                (rawMessage.contains("duplicate", ignoreCase = true) && rawMessage.contains("product_reference", ignoreCase = true)) ->
                "A product with this reference already exists. Change the reference and try again."
            else -> rawMessage
        }
        val code = obj?.optString("code")?.takeIf(String::isNotBlank) ?: "http_$status"
        val field = obj?.optString("field")?.takeIf(String::isNotBlank)
            ?: fieldErrors.firstOrNull()?.optString("field")?.takeIf(String::isNotBlank)
        return KoraError(code, message, status, field, status >= 500 || status == 429)
    }
}
