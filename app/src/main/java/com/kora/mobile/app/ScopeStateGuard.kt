package com.kora.mobile.app

import com.kora.mobile.domain.model.Scope

internal data class ScopeRequestContext(val scope: Scope, val generation: Long)

/** Applies an asynchronous result only while its originating scope is active. */
internal inline fun AppState.updateForRequestScope(
    request: ScopeRequestContext,
    activeScope: Scope?,
    activeGeneration: Long,
    requestedDoctype: String? = null,
    transform: (AppState) -> AppState,
): AppState {
    if (activeScope?.key != request.scope.key || activeGeneration != request.generation) return this
    if (requestedDoctype != null && selectedDoctype?.resourceName != requestedDoctype) return this
    return transform(this)
}
