package com.kora.mobile.core.contracts

import com.kora.mobile.generated.EngineContractManifest
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.security.MessageDigest

@RunWith(RobolectricTestRunner::class)
class EngineContractDecoderTest {
    private fun resource(path: String): ByteArray =
        javaClass.classLoader!!.getResourceAsStream(path)!!.use { it.readBytes() }

    @Test
    fun sharedProducerFixturesAcceptValidAndRejectMalformedContracts() {
        val schemaBytes = resource("contracts/engine-v2.json")
        val schemaHash = MessageDigest.getInstance("SHA-256").digest(schemaBytes).joinToString("") { "%02x".format(it) }
        assertEquals(EngineContractManifest.schemaSHA256, schemaHash)
        val decoder = EngineContractDecoder(JSONObject(schemaBytes.toString(Charsets.UTF_8)))
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
}
