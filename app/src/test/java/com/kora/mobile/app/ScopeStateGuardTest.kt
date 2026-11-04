package com.kora.mobile.app

import com.kora.mobile.domain.model.BusinessScope
import com.kora.mobile.domain.model.DocTypeNavItem
import com.kora.mobile.domain.model.Scope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.junit.Assert.assertEquals
import org.junit.Test

class ScopeStateGuardTest {
    @Test
    fun lateResponseCannotReplaceStateAfterActorSwitch() = runBlocking {
        val alice = Scope("instance", "https://example.test", "tenant-a", "alice")
        val bob = Scope("instance", "https://example.test", "tenant-a", "bob")
        val state = MutableStateFlow(AppState(message = "bob-state"))
        var activeScope: Scope? = alice
        var activeGeneration = 1L
        val response = CompletableDeferred<String>()
        val request = launch {
            val oldResult = response.await()
            state.update { current -> current.updateForRequestScope(ScopeRequestContext(alice, 1), activeScope, activeGeneration) { it.copy(message = oldResult) } }
        }
        activeScope = bob
        activeGeneration++
        response.complete("alice-state")
        request.join()
        assertEquals("bob-state", state.value.message)
    }

    @Test
    fun lateResponseCannotReplaceStateAfterEntitySwitch() = runBlocking {
        val tenant = Scope("instance", "https://example.test", "tenant-a", "alice")
        val entityA = tenant.copy(businessScope = BusinessScope("tenant-a", "legal_entity", "entity-a"))
        val entityB = tenant.copy(businessScope = BusinessScope("tenant-a", "legal_entity", "entity-b"))
        val state = MutableStateFlow(AppState(message = "entity-b-state"))
        var activeScope: Scope? = entityA
        var activeGeneration = 1L
        val response = CompletableDeferred<String>()
        val request = launch {
            val oldResult = response.await()
            state.update { current -> current.updateForRequestScope(ScopeRequestContext(entityA, 1), activeScope, activeGeneration) { it.copy(message = oldResult) } }
        }
        activeScope = entityB
        activeGeneration++
        response.complete("entity-a-state")
        request.join()
        assertEquals("entity-b-state", state.value.message)
    }

    @Test
    fun lateResponseCannotReplaceStateAfterSameActorRelogin() {
        val scope = Scope("instance", "https://example.test", "tenant-a", "alice")
        val state = AppState(message = "new-session-state")
        val updated = state.updateForRequestScope(ScopeRequestContext(scope, 7), scope, 8) { it.copy(message = "previous-session-state") }
        assertEquals("new-session-state", updated.message)
    }

    @Test
    fun lateResponseCannotReplaceASelectionMadeByANewerRequest() {
        val scope = Scope("instance", "https://example.test", "tenant-a", "alice")
        val oldSelection = DocTypeNavItem("Product", "Product", "Products")
        val newSelection = DocTypeNavItem("Customer", "Customer", "Customers")
        val state = AppState(selectedDoctype = newSelection, message = "customer-state")
        val updated = state.updateForRequestScope(ScopeRequestContext(scope, 1), scope, 1, oldSelection.resourceName) { it.copy(message = "product-state") }
        assertEquals("Customer", updated.selectedDoctype?.resourceName)
        assertEquals("customer-state", updated.message)
    }
}
