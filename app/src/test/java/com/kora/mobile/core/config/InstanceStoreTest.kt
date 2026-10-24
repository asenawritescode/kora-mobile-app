package com.kora.mobile.core.config

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class InstanceStoreTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before fun clearInstances() {
        context.getSharedPreferences("kora_instances", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test fun requiresSiteIdBeforeSavingServer() {
        val store = InstanceStore(context)
        assertThrows(IllegalArgumentException::class.java) {
            store.save("https://api.kora.mradiafrica.com", "")
        }
        assertEquals("https://api.kora.mradiafrica.com", store.current().origin)
    }

    @Test fun acceptsSiteIdEmbeddedInServerUrl() {
        val instance = InstanceStore(context).save("https://api.kora.mradiafrica.com/s/shop-one")
        assertEquals("https://api.kora.mradiafrica.com", instance.origin)
        assertEquals("shop-one", instance.siteRef)
    }
}
