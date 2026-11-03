package com.kora.mobile.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.kora.mobile.core.contracts.EngineContractDecoder
import com.kora.mobile.core.database.KoraDatabase
import com.kora.mobile.core.network.KoraHttpClient
import com.kora.mobile.core.security.PayloadCipher
import com.kora.mobile.core.security.SessionSecretStore
import com.kora.mobile.domain.model.BusinessScope
import com.kora.mobile.domain.model.Scope
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.URI
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
class WorkspaceRepositoryPublishedContractsTest {
    private class MemorySecrets : SessionSecretStore {
        private val values = mutableMapOf<String, String>()
        override fun put(key: String, value: String) { values[key] = value }
        override fun get(key: String): String? = values[key]
        override fun remove(key: String) { values.remove(key) }
    }

    private class TestCipher : PayloadCipher {
        override fun encrypt(value: String) = value
        override fun decrypt(encoded: String) = encoded
    }

    @Test
    fun catalogQueryProjectionAndRegisteredActionStayWithinActorScope() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val queryPath = "/api/v1/modules/sales.orders/queries/list"
        val projectionPath = "/api/v1/modules/sales.orders/projections/history"
        val operationRef = "sales.orders~sales.orders~accept"
        val queryRequest = AtomicReference<JSONObject>()
        val operationRequest = AtomicReference<JSONObject>()
        val lookupRequest = AtomicReference<JSONObject>()
        val entityHeader = AtomicReference<String>()
        val idempotencyHeader = AtomicReference<String>()
        val executionCount = AtomicInteger()
        val server = ServerSocket().apply { bind(InetSocketAddress("127.0.0.1", 0)) }
        val serverThread = Thread {
            while (!server.isClosed) runCatching {
                server.accept().use { socket ->
                    val request = readRequest(socket.getInputStream())
                    val path = URI(request.first).path
                    if (path != "/api/v2/modules/catalog") entityHeader.set(request.second["x-kora-legal-entity-id"])
                    if (path.endsWith("/execute")) idempotencyHeader.set(request.second["idempotency-key"])
                    val result = when {
                        path == "/api/v2/modules/catalog" -> catalogJson()
                        path == queryPath -> { queryRequest.set(JSONObject(request.third)); queryResult("row-1") }
                        path == projectionPath -> queryResult("history-1")
                        path == "/api/v2/modules/operations/$operationRef/execute" -> { executionCount.incrementAndGet(); operationRequest.set(JSONObject(request.third)); receiptJson(operationRef, "completed") }
                        path == "/api/v2/module-executions/lookup" -> { lookupRequest.set(JSONObject(request.third)); receiptJson(operationRef, "completed") }
                        else -> JSONObject().put("error", JSONObject().put("code", "not_found").put("message", "unexpected path: $path"))
                    }
                    val status = if (path == "/api/v2/modules/catalog" || path == queryPath || path == projectionPath || path.endsWith("/execute") || path == "/api/v2/module-executions/lookup") 200 else 404
                    val body = JSONObject().put("data", result).toString().toByteArray()
                    val statusText = if (status == 200) "OK" else "Not Found"
                    socket.getOutputStream().apply {
                        write("HTTP/1.1 $status $statusText\r\nContent-Type: application/json\r\nContent-Length: ${body.size}\r\nConnection: close\r\n\r\n".toByteArray())
                        write(body)
                        flush()
                    }
                }
            }
        }.apply { isDaemon = true; start() }
        val db = Room.inMemoryDatabaseBuilder(context, KoraDatabase::class.java).allowMainThreadQueries().build()
        try {
            val repository = WorkspaceRepository(KoraHttpClient(MemorySecrets()), db, TestCipher(), EngineContractDecoder.fromAssets(context))
            val scope = Scope("test-instance", "http://127.0.0.1:${server.localPort}", null, "alice", BusinessScope("tenant-a", "legal_entity", "entity-a"))

            val catalog = repository.publishedCatalog(scope)
            val query = catalog.queries.single()
            val projection = catalog.projections.single()
            val operation = catalog.operations.single()
            val page = repository.publishedQuery(scope, catalog, query, JSONObject())
            val historicalPage = repository.publishedProjection(scope, catalog, projection)
            val receipt = repository.submitPublishedOperation(scope, catalog, operation, JSONObject().put("id", "row-1"), "intent-stable-1")
            val recovered = repository.lookupPublishedOperation(scope, catalog, operation, "intent-stable-1")

            assertEquals("fresh", page.freshness)
            assertEquals("cursor-next", page.nextCursor)
            assertEquals("source-rev-9", page.sourceRevision)
            assertEquals("row-1", page.data.getJSONObject(0).getJSONObject("business_ref").getString("id"))
            assertEquals("stale", historicalPage.freshness)
            assertEquals("intent-stable-1", operationRequest.get().getJSONObject("meta").getString("idempotency_key"))
            assertEquals("intent-stable-1", idempotencyHeader.get())
            assertEquals("intent-stable-1", lookupRequest.get().getString("idempotency_key"))
            assertEquals(1, executionCount.get())
            assertEquals(0, queryRequest.get().getJSONObject("input").length())
            assertEquals(25, queryRequest.get().getInt("limit"))
            assertNotNull(operationRequest.get().getJSONObject("input_schema"))
            assertEquals("entity-a", entityHeader.get())
            assertEquals("completed", receipt.status)
            assertEquals("exec-9", recovered?.executionId)
            assertEquals("tenant-a", receipt.businessScope.tenantId)
            assertNull(db.localDao.getEntry(scope.key, "published-catalog"))
            assertTrue(db.localDao.pendingIntents(scope.key).isEmpty())
            assertTrue(query.route.startsWith("/api/v1/"))
        } finally {
            db.close()
            server.close()
            serverThread.join(1000)
        }
    }

    private fun catalogJson(): JSONObject {
        val scope = JSONObject().put("tenant_id", "tenant-a").put("kind", "legal_entity").put("legal_entity_id", "entity-a")
        val input = JSONObject().put("type", "object")
        val queryOutput = JSONObject().put("type", "array").put("items", JSONObject().put("type", "object"))
        val actionInput = JSONObject().put("type", "object").put("required", org.json.JSONArray().put("id"))
            .put("properties", JSONObject().put("id", JSONObject().put("type", "string")))
        val actionOutput = JSONObject().put("type", "object").put("required", org.json.JSONArray().put("receipt_id"))
            .put("properties", JSONObject().put("receipt_id", JSONObject().put("type", "string")))
        return JSONObject().put("revision", 9).put("business_scope", scope)
            .put("modules", org.json.JSONArray().put(JSONObject().put("id", "sales.orders").put("version", "1.0.0").put("state", "active")))
            .put("published_queries", org.json.JSONArray().put(JSONObject()
                .put("owner_module_id", "sales.orders").put("name", "list").put("route", "/api/v1/modules/sales.orders/queries/list")
                .put("input_schema", schemaRef("orders.input")).put("output_schema", schemaRef("orders.output")).put("scope_policy", "entity_owned")
                .put("allowed_fields", org.json.JSONArray().put("id")).put("page_limit", 25).put("presentation", JSONObject().put("title", "Orders"))))
            .put("published_projections", org.json.JSONArray().put(JSONObject()
                .put("owner_module_id", "sales.orders").put("id", "history").put("route", "/api/v1/modules/sales.orders/projections/history")
                .put("output_schema", schemaRef("orders.output")).put("scope_policy", "entity_owned").put("allowed_fields", org.json.JSONArray().put("id"))))
            .put("published_operations", org.json.JSONArray().put(JSONObject()
                .put("operation_ref", "sales.orders~sales.orders~accept").put("owner_module_id", "sales.orders").put("capability", "sales.orders")
                .put("operation", "accept").put("input_schema", schemaRef("action.input")).put("output_schema", schemaRef("action.output"))
                .put("availability", "available").put("permissions", org.json.JSONArray().put("write")).put("presentation", JSONObject().put("title", "Accept order"))))
            .put("schemas", org.json.JSONArray()
                .put(JSONObject().put("ref", schemaRef("orders.input")).put("schema", input))
                .put(JSONObject().put("ref", schemaRef("orders.output")).put("schema", queryOutput))
                .put(JSONObject().put("ref", schemaRef("action.input")).put("schema", actionInput))
                .put(JSONObject().put("ref", schemaRef("action.output")).put("schema", actionOutput)))
            .put("allowed_entity_choices", org.json.JSONArray()).put("available_scopes", org.json.JSONArray()).put("blockers", org.json.JSONArray())
    }

    private fun queryResult(id: String): JSONObject {
        val data = org.json.JSONArray().put(JSONObject().put("id", id).put("business_ref", JSONObject()
            .put("provider_id", "billing").put("resource_type", "invoice").put("id", id)
            .put("scope", JSONObject().put("tenant_id", "tenant-a").put("kind", "legal_entity").put("legal_entity_id", "entity-a"))))
        return JSONObject().put("data", data).put("next_cursor", "cursor-next").put("snapshot_token", "snapshot-4")
            .put("source_cursor", "cursor-source").put("source_revision", "source-rev-9").put("as_of", "2026-11-03T09:00:00Z")
            .put("freshness", if (id.startsWith("history")) "stale" else "fresh")
    }

    private fun receiptJson(operationRef: String, status: String): JSONObject = JSONObject()
        .put("execution_id", "exec-9").put("operation_ref", operationRef)
        .put("business_scope", JSONObject().put("tenant_id", "tenant-a").put("kind", "legal_entity").put("legal_entity_id", "entity-a"))
        .put("status", status).put("output_schema", schemaRef("action.output"))
        .put("output", JSONObject().put("receipt_id", "receipt-9")).put("revision", "10")

    private fun schemaRef(id: String) = JSONObject().put("id", id).put("version", 1)

    private fun readRequest(input: java.io.InputStream): Triple<String, Map<String, String>, String> {
        val stream = BufferedInputStream(input)
        val headerBytes = ByteArrayOutputStream()
        val marker = byteArrayOf(13, 10, 13, 10)
        var matched = 0
        while (matched < marker.size) {
            val next = stream.read()
            if (next < 0) error("incomplete HTTP request headers")
            headerBytes.write(next)
            matched = if (next == marker[matched].toInt()) matched + 1 else if (next == marker[0].toInt()) 1 else 0
        }
        val lines = headerBytes.toString(Charsets.ISO_8859_1.name()).split("\r\n")
        val headers = lines.drop(1).mapNotNull { line ->
            val separator = line.indexOf(':')
            if (separator < 0) null else line.substring(0, separator).lowercase() to line.substring(separator + 1).trim()
        }.toMap()
        val length = headers["content-length"]?.toIntOrNull() ?: 0
        val body = ByteArray(length)
        var offset = 0
        while (offset < body.size) {
            val read = stream.read(body, offset, body.size - offset)
            if (read < 0) error("incomplete HTTP request body")
            offset += read
        }
        return Triple(lines.first().split(' ').getOrElse(1) { "" }, headers, body.toString(Charsets.UTF_8))
    }
}
