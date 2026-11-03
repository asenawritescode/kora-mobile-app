package com.kora.mobile

import android.app.Application
import com.kora.mobile.core.config.InstanceStore
import com.kora.mobile.core.contracts.EngineContractDecoder
import com.kora.mobile.core.database.KoraDatabase
import com.kora.mobile.core.network.KoraHttpClient
import com.kora.mobile.core.security.SecretVault
import com.kora.mobile.data.WorkspaceRepository
import com.kora.mobile.domain.model.Scope

class KoraApplication : Application() {
    lateinit var instanceStore: InstanceStore
        private set
    lateinit var vault: SecretVault
        private set
    lateinit var workspace: WorkspaceRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instanceStore = InstanceStore(this)
        vault = SecretVault(this)
        workspace = WorkspaceRepository(KoraHttpClient(vault), KoraDatabase.get(this), vault, EngineContractDecoder.fromAssets(this), ::isActiveScope)
    }

    fun activateScope(scope: Scope) {
        getSharedPreferences("kora_session", MODE_PRIVATE).edit()
            .putString("active_scope", scope.key)
            .putString("active_tenant_id", scope.businessScope.tenantId)
            .putString("active_scope_kind", scope.businessScope.kind)
            .putString("active_legal_entity_id", scope.businessScope.legalEntityId)
            .apply()
    }

    fun isActiveScope(scope: Scope): Boolean = getSharedPreferences("kora_session", MODE_PRIVATE).getString("active_scope", null) == scope.key
}
