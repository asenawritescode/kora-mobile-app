package com.kora.mobile.ui.screens

import android.app.TimePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kora.mobile.*
import com.kora.mobile.app.AppStage
import com.kora.mobile.app.AppState
import com.kora.mobile.app.AppViewModel
import com.kora.mobile.domain.model.DocTypeNavItem
import com.kora.mobile.domain.model.FieldSchema
import com.kora.mobile.ui.components.*
import com.kora.mobile.ui.theme.KoraTheme
import com.kora.mobile.ui.theme.KoraTokens
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@Composable
internal fun WorkScreen(state: AppState, vm: AppViewModel) {
    val catalog = state.publishedCatalog
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Published workspace", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(catalog?.let { "${it.queries.size} queries · ${it.projections.size} historical views · ${it.operations.size} actions" } ?: "Actor-authorized data and actions", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { vm.selectSection("Work") }) { Icon(Icons.Filled.Refresh, "Refresh published catalog") }
        }
        state.surfaceError?.let { Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp)) }
        if (state.surfaceLoading && catalog == null) LinearProgressIndicator(Modifier.fillMaxWidth())
        val modules = catalog?.modules.orEmpty()
        if (catalog == null) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { EmptyCard("Published workspace unavailable", state.surfaceError ?: "Connect to Kora to load actor-authorized queries and actions.") }
        } else if (modules.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { EmptyCard("No published modules", "This actor has no module surfaces in the current business scope.") }
        } else LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
            items(modules, key = { it.id }) { module ->
                val queries = catalog.queries.filter { it.ownerModuleId == module.id }
                val projections = catalog.projections.filter { it.ownerModuleId == module.id }
                val operations = catalog.operations.filter { it.ownerModuleId == module.id }
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(34.dp).background(androidx.compose.material3.MaterialTheme.colorScheme.secondary, RoundedCornerShape(11.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Inventory2, null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) }
                            Spacer(Modifier.width(11.dp)); Text(module.id, Modifier.weight(1f), fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(module.state, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
                        }
                        queries.forEach { item -> PublishedSurfaceRow(item.presentation.optString("title").ifBlank { item.name }, "Query · ${item.scopePolicy}") { vm.openPublishedQuery(item) } }
                        projections.forEach { item -> PublishedSurfaceRow(item.id, "Historical view · ${item.scopePolicy}") { vm.openPublishedProjection(item) } }
                        operations.forEach { item ->
                            val title = item.presentation.optString("title").ifBlank { item.operationRef }
                            PublishedSurfaceRow(title, "Action · ${item.availability}", enabled = item.availability == "available") { vm.openPublishedOperation(item) }
                        }
                        if (queries.isEmpty() && projections.isEmpty() && operations.isEmpty()) Text("No actor-authorized surfaces", modifier = Modifier.padding(start = 45.dp, bottom = 12.dp), color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (catalog.blockers.isNotEmpty()) item { Text("Engine reported capability blockers: ${catalog.blockers.joinToString { "${it.code} (${it.count})" }}", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp)) }
        }
    }
}

@Composable
internal fun PublishedSurfaceRow(title: String, subtitle: String, enabled: Boolean = true, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 54.dp).clickable(enabled = enabled, onClick = onClick).padding(start = 45.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(title, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(subtitle, style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) }
        if (enabled) Icon(Icons.Outlined.ChevronRight, null, tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun PublishedReadDetail(state: AppState, vm: AppViewModel) {
    val query = state.selectedPublishedQuery
    val projection = state.selectedProjection
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(query?.presentation?.optString("description").orEmpty().ifBlank { projection?.let { "Historical data from ${it.ownerModuleId}." }.orEmpty() }, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
        if (query != null) {
            OutlinedTextField(value = state.publishedQueryInput, onValueChange = vm::setPublishedQueryInput, label = { Text("Query input JSON") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            Button(onClick = { vm.runPublishedQuery() }, enabled = !state.surfaceLoading, modifier = Modifier.fillMaxWidth()) { Text(if (state.surfaceLoading) "Loading…" else "Load published data") }
        }
        state.surfaceError?.let { Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
        state.publishedPage?.let { page ->
            Card { Column(Modifier.fillMaxWidth().padding(14.dp)) {
                Text("${page.freshness.replaceFirstChar { it.uppercase() }} · as of ${page.asOf}", fontWeight = FontWeight.SemiBold)
                Text("Source revision: ${page.sourceRevision ?: "not supplied"}", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                page.snapshotToken?.let { Text("Snapshot: $it", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) }
                if (page.freshness in setOf("stale", "incomplete", "unknown")) Text("This result is ${page.freshness}; do not treat it as complete current data.", color = androidx.compose.material3.MaterialTheme.colorScheme.error)
            } }
            page.data.let { rows ->
                if (rows.length() == 0) Text("No rows were returned by this published surface.", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                for (index in 0 until rows.length()) rows.optJSONObject(index)?.let { row ->
                    Card(Modifier.fillMaxWidth().clickable { vm.selectPublishedRow(row) }) { Text(row.toString(), modifier = Modifier.padding(14.dp), maxLines = 4, overflow = TextOverflow.Ellipsis) }
                }
            }
            page.nextCursor?.let { cursor -> OutlinedButton(onClick = { vm.runPublishedQuery(cursor) }, enabled = query != null && !state.surfaceLoading) { Text("Next page") } }
        }
    }
}

@Composable
internal fun PublishedRowDetail(row: JSONObject) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        val ref = row.optJSONObject("business_ref")
        if (ref != null) {
            Text("${ref.optString("provider_id")} · ${ref.optString("resource_type")} · ${ref.optString("id")}", fontWeight = FontWeight.SemiBold)
            Text("Scope: ${ref.optJSONObject("scope")?.toString() ?: "not supplied"}", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(row.toString())
    }
}

@Composable
internal fun PublishedOperationDetail(state: AppState, vm: AppViewModel) {
    val operation = state.selectedPublishedOperation ?: return
    val inputSchema = state.publishedCatalog?.schemas?.singleOrNull { it.ref == operation.inputSchema }?.schema
    val properties = inputSchema?.optJSONObject("properties")
    val required = inputSchema?.optJSONArray("required")?.let { array -> (0 until array.length()).map { array.optString(it) }.toSet() }.orEmpty()
    var formValues by remember(operation.operationRef) { mutableStateOf(emptyMap<String, String>()) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(operation.presentation.optString("description").ifBlank { "Engine validates this action against its published input schema and current permissions." }, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Input schema ${operation.inputSchema.id} v${operation.inputSchema.version}", style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
        if (properties != null && properties.length() > 0) {
            val names = properties.keys().asSequence().toList()
            names.forEach { name ->
                val definition = properties.optJSONObject(name) ?: JSONObject()
                val label = definition.optString("title").ifBlank { name.replace('_', ' ').replaceFirstChar { it.uppercase() } } + if (name in required) " *" else ""
                val value = formValues[name].orEmpty()
                val enumValues = definition.optJSONArray("enum")?.let { values -> (0 until values.length()).map { values.optString(it) } }.orEmpty()
                when {
                    enumValues.isNotEmpty() -> ChoiceField(label, value, "Choose ${label.removeSuffix(" *").lowercase()}", enumValues, true, !state.operationMayHaveRun) { selected ->
                        formValues = formValues + (name to selected)
                        vm.setOperationInput(operationFormJson(formValues + (name to selected), properties))
                    }
                    definition.optString("type") == "boolean" -> Row(Modifier.fillMaxWidth().clickable(enabled = !state.operationMayHaveRun) {
                        val selected = if (value == "true") "false" else "true"
                        formValues = formValues + (name to selected)
                        vm.setOperationInput(operationFormJson(formValues + (name to selected), properties))
                    }, verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(value == "true", null, enabled = !state.operationMayHaveRun)
                        Text(label)
                    }
                    else -> KoraTextField(
                        value = value,
                        onValueChange = { typed ->
                            formValues = formValues + (name to typed)
                            vm.setOperationInput(operationFormJson(formValues + (name to typed), properties))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.operationMayHaveRun,
                        label = label,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = if (definition.optString("type") in setOf("number", "integer")) KeyboardType.Decimal else KeyboardType.Text),
                    )
                }
                definition.optString("description").takeIf(String::isNotBlank)?.let { Text(it, fontSize = 11.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        } else {
            OutlinedTextField(value = state.operationInput, onValueChange = vm::setOperationInput, enabled = !state.operationMayHaveRun, label = { Text("Action input JSON") }, modifier = Modifier.fillMaxWidth(), minLines = 4)
        }
        Button(onClick = { vm.executePublishedOperation(operation) }, enabled = operation.availability == "available" && !state.surfaceLoading, modifier = Modifier.fillMaxWidth()) { Text(if (state.surfaceLoading) "Submitting…" else if (operation.offlineEligible) "Submit or save offline" else "Submit action") }
        if (state.operationMayHaveRun) OutlinedButton(onClick = vm::recoverPublishedOperation, enabled = !state.surfaceLoading, modifier = Modifier.fillMaxWidth()) { Text("Check operation receipt") }
        state.surfaceError?.let { Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
        state.operationReceipt?.let { receipt ->
            Card { Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("${receipt.status.replace('_', ' ').replaceFirstChar { it.uppercase() }} · execution ${receipt.executionId}", fontWeight = FontWeight.SemiBold)
                Text("Revision ${receipt.revision} · scope ${receipt.businessScope.kind}", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                receipt.output?.let { Text(it.toString()) }
                receipt.errorMessage?.let { Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
            } }
        }
        if (state.operationReceipt != null && !state.operationMayHaveRun) OutlinedButton(onClick = vm::startNewPublishedOperation, modifier = Modifier.fillMaxWidth()) { Text("Start another action") }
    }
}

internal fun operationFormJson(values: Map<String, String>, properties: JSONObject): String {
    val payload = JSONObject()
    values.forEach { (name, raw) ->
        if (raw.isBlank()) return@forEach
        val type = properties.optJSONObject(name)?.optString("type")
        val value: Any = when (type) {
            "boolean" -> raw == "true"
            "integer" -> raw.toLongOrNull() ?: raw
            "number" -> raw.toDoubleOrNull() ?: raw
            else -> raw
        }
        payload.put(name, value)
    }
    return payload.toString()
}
