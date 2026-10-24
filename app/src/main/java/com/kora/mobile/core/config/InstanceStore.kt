package com.kora.mobile.core.config

import android.content.Context
import com.kora.mobile.BuildConfig
import com.kora.mobile.domain.model.InstanceConfig
import java.net.URI
import java.util.UUID

class InstanceStore(context: Context) {
    private val prefs = context.getSharedPreferences("kora_instances", Context.MODE_PRIVATE)

    fun current(): InstanceConfig {
        val origin = prefs.getString("origin", BuildConfig.DEFAULT_SERVER_URL) ?: BuildConfig.DEFAULT_SERVER_URL
        val site = prefs.getString("site", null)
        val label = prefs.getString("label", "Kora server") ?: "Kora server"
        val id = prefs.getString("id", origin) ?: origin
        return InstanceConfig(id, origin, site, label)
    }

    fun save(rawUrl: String, siteRef: String? = null, label: String = "Kora server"): InstanceConfig {
        val normalized = normalize(rawUrl)
        val parsedSite = Regex("/s/([^/]+)$").find(normalized)?.groupValues?.get(1)
        val parsedOrigin = normalized.replace(Regex("/s/[^/]+$"), "")
        val selectedSite = siteRef?.trim()?.ifBlank { null } ?: parsedSite
        require(!selectedSite.isNullOrBlank()) { "Enter a Site ID before signing in" }
        val config = InstanceConfig(UUID.nameUUIDFromBytes((parsedOrigin + selectedSite.orEmpty()).toByteArray()).toString(), parsedOrigin, selectedSite, label.trim().ifBlank { "Kora server" })
        prefs.edit().putString("id", config.id).putString("origin", config.origin).putString("site", config.siteRef).putString("label", config.label).apply()
        return config
    }

    fun defaultUrl(): String = BuildConfig.DEFAULT_SERVER_URL

    private fun normalize(raw: String): String {
        val value = raw.trim().removeSuffix("/")
        val uri = runCatching { URI(value) }.getOrElse { throw IllegalArgumentException("Enter a valid server URL") }
        if (uri.scheme !in setOf("https", "http") || uri.host.isNullOrBlank() || uri.userInfo != null || uri.query != null || uri.fragment != null) {
            throw IllegalArgumentException("Use a server origin such as https://api.kora.mradiafrica.com")
        }
        val localDevelopmentHost = uri.host.equals("localhost", true) || uri.host == "10.0.2.2" || uri.host == "127.0.0.1"
        if (uri.scheme == "http" && !localDevelopmentHost) {
            throw IllegalArgumentException("Use HTTPS except for localhost development servers")
        }
        val path = uri.path.trimEnd('/')
        val site = Regex("/s/([^/]+)$").find(path)
        return URI(uri.scheme, null, uri.host, uri.port, if (site != null) "/s/${site.groupValues[1]}" else "", null, null).toString().trimEnd('/')
    }
}
