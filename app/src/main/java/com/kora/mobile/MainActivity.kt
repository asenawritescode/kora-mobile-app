package com.kora.mobile

import android.os.Bundle
import android.app.TimePickerDialog
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.kora.mobile.app.AppStage
import com.kora.mobile.app.AppViewModel
import com.kora.mobile.app.AppState
import com.kora.mobile.domain.model.DocTypeNavItem
import com.kora.mobile.domain.model.FieldSchema
import org.json.JSONObject
import org.json.JSONArray
import com.kora.mobile.ui.theme.KoraTheme
import com.kora.mobile.ui.theme.KoraTokens
import kotlinx.coroutines.launch
import com.kora.mobile.ui.screens.*
import com.kora.mobile.ui.components.*

class MainActivity : ComponentActivity() {
    @Suppress("UNCHECKED_CAST")
    private val factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AppViewModel(application) as T
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: AppViewModel = viewModel(factory = factory)
            LaunchedEffect(intent?.dataString) { vm.acceptMagicLink(intent?.data) }
            val state by vm.state.collectAsState()
            KoraTheme(
                primaryColor = state.tenantBranding?.primaryColor ?: state.navigation?.branding?.primaryColor,
                accentColor = state.tenantBranding?.accentColor,
                mode = state.themeMode,
            ) {
                KoraApp(state, vm)
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        ViewModelProvider(this, factory)[AppViewModel::class.java].acceptMagicLink(intent.data)
    }
}

@Composable
internal fun KoraApp(state: AppState, vm: AppViewModel) {
    Surface(modifier = Modifier.fillMaxSize(), color = androidx.compose.material3.MaterialTheme.colorScheme.background) {
        when (state.stage) {
            AppStage.SERVER -> ServerSetup(state, onNext = vm::goToLogin, onConnect = vm::connect)
            AppStage.LOGIN -> LoginScreen(state, onBack = { vm.logout() }, onMagicLink = vm::requestMagicLink, onChangeServer = { vm.logout() })
            AppStage.WORKSPACE -> WorkspaceShell(state, vm)
        }
    }
}

@Composable
internal fun RequestsScreen(state: AppState, vm: AppViewModel) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        item { Spacer(Modifier.height(8.dp)); SectionHeading("Requests", "Needs action") }
        item { OutlinedButton(onClick = vm::refreshRequests, enabled = !state.workflowBusy, modifier = Modifier.fillMaxWidth()) { Text("Refresh status") } }
        if (state.requests.isEmpty()) item { EmptyCard("Nothing needs your attention", if (state.offline) "No cached requests are available. Reconnect to refresh." else "Requests assigned to you will show up here.") }
        items(state.requests) { request ->
            val title = request.optString("title", "Request")
            val message = request.optString("message")
            val status = request.optString("status", "Open")
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Text(title, Modifier.weight(1f), fontWeight = FontWeight.SemiBold); Text(status.replace('_', ' '), color = androidx.compose.material3.MaterialTheme.colorScheme.primary, fontSize = 11.sp) }
                    if (message.isNotBlank()) Text(message, Modifier.padding(top = 5.dp), color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    val assignments = request.optJSONArray("assignments")
                    val assignment = assignments?.let { rows -> (0 until rows.length()).mapNotNull(rows::optJSONObject).firstOrNull { it.optString("status") == "pending" } }
                    val responses = request.optJSONArray("allowed_responses")
                    if (assignment != null && responses != null && responses.length() > 0) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 10.dp)) {
                            (0 until responses.length()).forEach { index ->
                                val answer = responses.optString(index)
                                if (answer == "provide_information") Text("Information response requires its published schema form.", fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                                else OutlinedButton(onClick = { vm.respondToRequest(request, answer) }, enabled = !state.workflowBusy) { Text(answer.replace('_', ' ').replaceFirstChar { it.uppercase() }) }
                            }
                        }
                    }
                }
            }
        }
        item { SectionHeading("Workflow status", "Recent activity") }
        item { OutlinedButton(onClick = vm::refreshWorkflowDefinitions, enabled = !state.workflowBusy, modifier = Modifier.fillMaxWidth()) { Text("Load available workflows") } }
        if (state.workflowError != null) item { MessageBanner(state.workflowError, state.offline) }
        items(state.workflowDefinitions, key = { "definition:${it.id}:${it.version}" }) { definition ->
            var subjectId by rememberSaveable(definition.id, definition.version) { mutableStateOf("") }
            var startKey by rememberSaveable(definition.id, definition.version, subjectId) { mutableStateOf(java.util.UUID.randomUUID().toString()) }
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(definition.id, fontWeight = FontWeight.SemiBold)
                    Text("v${definition.version} · ${definition.digest.take(12)}", fontSize = 11.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(subjectId, { subjectId = it }, label = { Text("Subject ID") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedButton(onClick = { vm.startWorkflow(definition, "record", subjectId.trim(), startKey) { startKey = java.util.UUID.randomUUID().toString() } }, enabled = !state.workflowBusy && subjectId.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Start workflow") }
                }
            }
        }
        if (state.workflowInstances.isEmpty()) item { EmptyCard("No recent workflows", "Workflow status will appear here when this workspace has activity.") }
        items(state.workflowInstances, key = { it.id }) { workflow ->
            var reason by remember(workflow.id) { mutableStateOf("") }
            var evidence by remember(workflow.id) { mutableStateOf("") }
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Text(workflow.definitionId, Modifier.weight(1f), fontWeight = FontWeight.SemiBold); Text(workflow.status.replace('_', ' '), color = androidx.compose.material3.MaterialTheme.colorScheme.primary, fontSize = 11.sp) }
                    Text("${workflow.currentStep} · ${workflow.subjectType} ${workflow.subjectId}", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    if (workflow.status == "outcome_unknown") Text("Outcome needs review. Reconcile it with the workflow owner before retrying.", color = androidx.compose.material3.MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    if (workflow.status in setOf("waiting", "retrying", "outcome_unknown")) Text("Check the workflow history before taking another action.", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    OutlinedButton(onClick = { vm.loadWorkflowHistory(workflow.id) }) { Text("Load history") }
                    state.workflowHistory[workflow.id].orEmpty().forEach { event ->
                        Text("${event.optString("step_id")} · ${event.optString("kind")} · ${event.optString("status")}", fontSize = 11.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                        if (event.optString("detail").isNotBlank()) Text(event.optString("detail"), fontSize = 11.sp)
                    }
                    if (workflow.status == "outcome_unknown") {
                        OutlinedTextField(evidence, { evidence = it }, label = { Text("Evidence reference") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(reason, { reason = it }, label = { Text("Reconciliation reason") }, modifier = Modifier.fillMaxWidth())
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("confirmed_succeeded", "confirmed_not_applied", "abandon").forEach { outcome ->
                                OutlinedButton(onClick = { vm.reconcileWorkflow(workflow.id, workflow.currentStep, outcome, evidence.trim(), reason.trim()) }, enabled = !state.workflowBusy && reason.isNotBlank() && evidence.isNotBlank()) { Text(outcome.removePrefix("confirmed_").replace('_', ' '), fontSize = 10.sp) }
                            }
                        }
                    }
                    if (workflow.status in setOf("ready", "running", "waiting", "retrying")) {
                        OutlinedTextField(reason, { reason = it }, label = { Text("Cancellation reason") }, modifier = Modifier.fillMaxWidth())
                        OutlinedButton(onClick = { vm.cancelWorkflow(workflow.id, reason.trim()) }, enabled = !state.workflowBusy && reason.isNotBlank()) { Text("Cancel workflow") }
                    }
                }
            }
        }
    }
}

@Composable
internal fun SyncScreen(state: AppState, vm: AppViewModel) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        item { Spacer(Modifier.height(8.dp)); Text("Connection", fontSize = 26.sp, fontWeight = FontWeight.Bold); Text(if (state.offline) "Offline" else "Connected", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) }
        item {
            EmptyCard(
                if (state.offline) "Reconnect to Kora" else "Published workspace is online",
                "Saved actions stay scoped to this signed-in actor and are only complete when Engine returns a completed receipt.",
            )
        }
        if (state.offlineIntents.isNotEmpty()) {
            item { SectionHeading("Saved actions", "${state.offlineIntents.size} total") }
            items(state.offlineIntents, key = { it.localOperationId }) { intent ->
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface), border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant.copy(alpha = .7f))) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(intent.operationRef, fontWeight = FontWeight.SemiBold)
                        Text(intent.status.replace('_', ' ').replaceFirstChar { it.uppercase() }, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                        intent.lastError?.let { Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
                    }
                }
            }
            item { OutlinedButton(onClick = vm::syncOfflineIntents, enabled = !state.offline && !state.busy, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Icon(Icons.Filled.Sync, null); Spacer(Modifier.width(8.dp)); Text("Check saved actions") } }
        }
        if (state.message != null) item { MessageBanner(state.message, state.offline && state.message.contains("waiting", true)) }
        item { OutlinedButton(onClick = { vm.logout() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Icon(Icons.Outlined.Logout, null); Spacer(Modifier.width(8.dp)); Text("Sign out") } }
    }
}

