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
@OptIn(ExperimentalMaterial3Api::class)
internal fun WorkspaceShell(state: AppState, vm: AppViewModel) {
    val tabs = buildList {
        add("Home" to Icons.Filled.Home)
        add("Browse" to Icons.Filled.Inventory2)
        add("Requests" to Icons.Filled.ListAlt)
        add("Reports" to Icons.Filled.BarChart)
    }
    Scaffold(
        contentWindowInsets = WindowInsets.navigationBars,
        topBar = {
            if (state.formOpen || state.selectedRecord != null || state.selectedDoctype != null || state.selectedPublishedQuery != null || state.selectedProjection != null || state.selectedPublishedOperation != null || state.selectedPublishedRow != null) {
                TopAppBar(
                    title = { Text(when { state.formOpen -> if (state.editingRecord == null) "New record" else "Edit record"; state.selectedRecord != null -> state.selectedDoctype?.label ?: "Record"; state.selectedPublishedQuery != null -> state.selectedPublishedQuery.presentation.optString("title").ifBlank { state.selectedPublishedQuery.name }; state.selectedProjection != null -> state.selectedProjection.id; state.selectedPublishedOperation != null -> state.selectedPublishedOperation.presentation.optString("title").ifBlank { state.selectedPublishedOperation.operationRef }; else -> "Published data" }, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    navigationIcon = { IconButton(onClick = { if (state.formOpen) vm.backFromForm() else if (state.selectedRecord != null) vm.closeRecordDetail() else if (state.selectedPublishedRow != null) vm.selectPublishedRow(null) else if (state.selectedPublishedQuery != null || state.selectedProjection != null || state.selectedPublishedOperation != null) vm.closePublishedSurface() else vm.selectSection("Work") }) { Icon(Icons.Outlined.ArrowBack, "Back") } },
                    actions = {
                        if (state.formOpen) {
                            if (state.offline) Icon(Icons.Filled.CloudOff, "Offline", Modifier.padding(12.dp).size(24.dp), tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                            else Icon(Icons.Outlined.CloudDone, "Connected", Modifier.padding(12.dp).size(24.dp), tint = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                        } else IconButton(onClick = { vm.selectSection("Sync") }) {
                            if (state.offline) Icon(Icons.Filled.CloudOff, "Offline. Open sync", tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                            else Icon(Icons.Outlined.CloudDone, "Connected. Open sync", tint = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background),
                )
            } else {
                TopAppBar(
                    title = { Column { Text(if (state.section in setOf("Reports", "Requests", "Sync")) state.section else "Kora", fontWeight = FontWeight.Bold); Text(state.navigation?.branding?.appName ?: "Workspace", fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) } },
                    actions = {
                        ScopePicker(state, vm)
                        IconButton(onClick = { vm.selectSection("Sync") }) { if (state.offline) Icon(Icons.Filled.CloudOff, "Offline. Open sync", tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) else Icon(Icons.Outlined.CloudDone, "Connected. Open sync", tint = androidx.compose.material3.MaterialTheme.colorScheme.primary) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background),
                )
            }
        },
        bottomBar = {
            if (state.selectedDoctype == null && !state.formOpen && state.selectedPublishedQuery == null && state.selectedProjection == null && state.selectedPublishedOperation == null && state.selectedPublishedRow == null) {
                NavigationBar(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface) {
                    tabs.forEach { (name, icon) ->
                        val section = if (name == "Browse") "Work" else name
                        NavigationBarItem(selected = state.section == section, onClick = { vm.selectSection(section) }, icon = { Icon(icon, contentDescription = name) }, label = { Text(name) })
                    }
                }
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.formOpen -> RecordForm(state, vm)
                state.selectedPublishedRow != null -> PublishedRowDetail(state.selectedPublishedRow)
                state.selectedPublishedQuery != null || state.selectedProjection != null -> PublishedReadDetail(state, vm)
                state.selectedPublishedOperation != null -> PublishedOperationDetail(state, vm)
                state.selectedRecord != null -> RecordDetailScreen(state, vm)
                state.selectedDoctype != null -> RecordListScreen(state, vm)
                state.section == "Home" -> DashboardScreen(state, vm)
                state.section == "Work" -> WorkScreen(state, vm)
                state.section == "POS" -> PointOfSaleScreen(state, vm)
                state.section == "Requests" -> RequestsScreen(state, vm)
                state.section == "Reports" -> ReportsScreen(state, vm)
                else -> SyncScreen(state, vm)
            }
        }
    }
}

@Composable
internal fun ScopePicker(state: AppState, vm: AppViewModel) {
    val catalog = state.publishedCatalog ?: return
    if (state.availableEntityChoices.isEmpty()) return
    var expanded by remember { mutableStateOf(false) }
    val current = catalog.businessScope
    Box {
        TextButton(onClick = { expanded = true }, modifier = Modifier.semantics { contentDescription = "Scope selector" }) {
            Text(if (current.kind == "legal_entity") current.legalEntityId ?: "Legal entity" else "Tenant", maxLines = 1, overflow = TextOverflow.Ellipsis)
            Icon(Icons.Filled.ArrowDropDown, null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            state.availableEntityChoices.forEach { choice ->
                DropdownMenuItem(
                    text = { Text("Legal entity ${choice.legalEntityId}") },
                    onClick = { expanded = false; vm.switchBusinessScope(choice) },
                )
            }
        }
    }
}
