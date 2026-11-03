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

        val catalog = decoder.decodeApplicationCatalog("""{"revision":7,"business_scope":{"tenant_id":"tenant-a","kind":"legal_entity","legal_entity_id":"entity-a"},"modules":[],"published_queries":[],"published_operations":[],"schemas":[],"allowed_entity_choices":[],"available_scopes":[],"blockers":[]}""")
        assertEquals("entity-a", catalog.businessScope.legalEntityId)
        assertEquals(7L, catalog.revision)
    }
}
