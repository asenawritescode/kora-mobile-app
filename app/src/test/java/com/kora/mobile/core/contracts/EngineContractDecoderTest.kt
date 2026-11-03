package com.kora.mobile.core.contracts

import androidx.test.core.app.ApplicationProvider
import org.json.JSONObject
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EngineContractDecoderTest {
    private fun resource(path: String): ByteArray =
        javaClass.classLoader!!.getResourceAsStream(path)!!.use { it.readBytes() }

    @Test
    fun sharedProducerFixturesAcceptValidAndRejectMalformedContracts() {
        val decoder = EngineContractDecoder.fromAssets(ApplicationProvider.getApplicationContext())
        val fixture = JSONObject(resource("contract-fixtures/client-contracts-v2.json").toString(Charsets.UTF_8))

        val valid = fixture.getJSONArray("valid")
        for (index in 0 until valid.length()) {
            val example = valid.getJSONObject(index)
            val violations = decoder.validate(example.getString("contract"), example.getJSONObject("value"))
            assertTrue("valid fixture $index rejected: $violations", violations.isEmpty())
        }

        val invalid = fixture.getJSONArray("invalid")
        for (index in 0 until invalid.length()) {
            val example = invalid.getJSONObject(index)
            val violations = decoder.validate(example.getString("contract"), example.getJSONObject("value"))
            assertTrue("invalid fixture $index accepted", violations.isNotEmpty())
        }
    }

    @Test
    fun typedDecodersRetainBusinessScopeSnapshotAndReceiptOutcome() {
        val decoder = EngineContractDecoder.fromAssets(ApplicationProvider.getApplicationContext())
        val receipt = decoder.decodeOperationReceipt("""{"execution_id":"exec-2","operation_ref":"finance.billing~billing.invoice~issue","business_scope":{"tenant_id":"tenant-a","kind":"tenant"},"status":"outcome_unknown","error":{"type":"outcome_unknown","message":"reconciling"},"revision":"8"}""")
        assertEquals("outcome_unknown", receipt.status)
        assertEquals("tenant-a", receipt.businessScope.tenantId)
        assertEquals("outcome_unknown", receipt.errorType)

        val catalog = decoder.decodeApplicationCatalog("""{"revision":7,"business_scope":{"tenant_id":"tenant-a","kind":"legal_entity","legal_entity_id":"entity-a"},"modules":[{"id":"sales.orders","version":"1.0.0","state":"active"}],"published_queries":[{"owner_module_id":"sales.orders","name":"list","route":"/api/v1/modules/sales.orders/queries/list","input_schema":{"id":"orders.input","version":1},"output_schema":{"id":"orders.output","version":1},"scope_policy":"entity_owned","allowed_fields":["id"],"page_limit":25,"presentation":{"title":"Orders"}}],"published_projections":[{"owner_module_id":"sales.orders","id":"history","route":"/api/v1/modules/sales.orders/projections/history","output_schema":{"id":"orders.output","version":1},"scope_policy":"entity_owned","allowed_fields":["id"]}],"published_operations":[],"schemas":[{"ref":{"id":"orders.input","version":1},"schema":{"type":"object"}},{"ref":{"id":"orders.output","version":1},"schema":{"type":"array","items":{"type":"object"}}}],"allowed_entity_choices":[],"available_scopes":[],"blockers":[]}""")
        assertEquals("entity-a", catalog.businessScope.legalEntityId)
        assertEquals(7L, catalog.revision)
        assertEquals("/api/v1/modules/sales.orders/queries/list", catalog.queries.single().route)
        assertEquals("/api/v1/modules/sales.orders/projections/history", catalog.projections.single().route)
        assertEquals("orders.output", catalog.schemas.last().ref.id)
    }

    @Test
    fun typedQueryPagePreservesSnapshotAndRejectsNonArrayData() {
        val decoder = EngineContractDecoder.fromAssets(ApplicationProvider.getApplicationContext())
        val page = decoder.decodeQueryPage("""{"data":[{"business_ref":{"provider_id":"billing","resource_type":"invoice","id":"inv-9","scope":{"tenant_id":"tenant-a","kind":"tenant"}}}],"next_cursor":"cursor-2","snapshot_token":"snap-5","source_cursor":"source-8","source_revision":"8","as_of":"2026-11-03T09:00:00Z","freshness":"stale"}""")
        assertEquals("cursor-2", page.nextCursor)
        assertEquals("snap-5", page.snapshotToken)
        assertEquals("source-8", page.sourceCursor)
        assertEquals("8", page.sourceRevision)
        assertEquals("stale", page.freshness)
        assertTrue(runCatching { decoder.decodeQueryPage("""{"data":{},"as_of":"2026-11-03T09:00:00Z","freshness":"fresh"}""") }.isFailure)
    }

    @Test
    fun moduleOutputSchemaIsAppliedToPublishedRows() {
        val decoder = EngineContractDecoder.fromAssets(ApplicationProvider.getApplicationContext())
        val schema = JSONObject("""{"type":"array","items":{"type":"object","required":["id"],"properties":{"id":{"type":"string"}}}}""")
        assertTrue(decoder.validateSchemaDocument(schema, org.json.JSONArray("""[{"id":"inv-1"}]""")).isEmpty())
        assertTrue(decoder.validateSchemaDocument(schema, org.json.JSONArray("""[{"amount":4}]""")).isNotEmpty())
    }
}
