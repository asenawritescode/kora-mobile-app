package com.kora.mobile.core.network

import com.kora.mobile.core.security.SessionSecretStore
import com.kora.mobile.domain.model.BusinessScope
import com.kora.mobile.domain.model.Scope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ScopedTransportTest {
    private class MemorySecrets : SessionSecretStore {
        private val values = mutableMapOf<String, String>()
        override fun put(key: String, value: String) { values[key] = value }
        override fun get(key: String): String? = values[key]
        override fun remove(key: String) { values.remove(key) }
    }

    @Test fun scopeKeySeparatesActorAndEntityButSessionCookieIsActorScoped() {
        val tenant = Scope("instance", "https://example.test", "tenant-a", "alice")
        val entity = tenant.copy(businessScope = BusinessScope("tenant-a", "legal_entity", "entity-a"))
        val anotherEntity = tenant.copy(businessScope = BusinessScope("tenant-a", "legal_entity", "entity-b"))
        val bob = tenant.copy(userId = "bob")
        assertTrue(tenant.key != entity.key)
        assertTrue(entity.key != anotherEntity.key)
        assertTrue(tenant.key != bob.key)

        val secrets = MemorySecrets()
        val jar = SessionCookieJar(secrets)
        jar.save(tenant, listOf("kora_sid=alice-session; Path=/; HttpOnly", "kora_csrf=alice-csrf; Path=/"))
        assertTrue(jar.header(tenant, "/api/v2/offline/intents")!!.contains("kora_sid=alice-session"))
        assertTrue(jar.header(entity, "/api/v2/offline/intents")!!.contains("kora_sid=alice-session"))
        assertNull(jar.header(bob, "/api/v2/offline/intents"))

        val anonymous = Scope("instance", "https://example.test", "tenant-a", "")
        jar.save(anonymous, listOf("kora_sid=new-session; Path=/; HttpOnly"))
        jar.bindAnonymousSession(anonymous, bob)
        assertNull(jar.header(anonymous, "/api/v1"))
        assertTrue(jar.header(bob, "/api/v1")!!.contains("kora_sid=new-session"))
    }

    @Test fun entityHeaderIsDerivedFromRequestLocalBusinessScope() {
        val tenant = Scope("instance", "https://example.test", "tenant-a", "alice")
        val entity = tenant.copy(businessScope = BusinessScope("tenant-a", "legal_entity", "entity-a"))
        assertTrue(requestScopeHeaders(entity).containsKey("X-Kora-Legal-Entity-ID"))
        assertEquals("entity-a", requestScopeHeaders(entity)["X-Kora-Legal-Entity-ID"])
        assertFalse(requestScopeHeaders(tenant).containsKey("X-Kora-Legal-Entity-ID"))
    }
}
