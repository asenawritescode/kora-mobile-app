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
internal fun RecordListScreen(state: AppState, vm: AppViewModel) {
    val doctype = state.selectedDoctype ?: return
    val components = state.doctypeManifest?.optJSONObject("spec")?.optJSONObject("layout")?.optJSONArray("children")
    val tableComponent = components?.let { rows ->
        (0 until rows.length()).mapNotNull { rows.optJSONObject(it) }
            .firstOrNull { it.optString("component") in setOf("record_table", "record_list", "record_cards") }
    }
    val tableProps = tableComponent?.optJSONObject("props") ?: JSONObject()
    val configuredMobileColumns = tableProps.optJSONArray("mobile_columns")?.let { rows ->
        (0 until rows.length()).map { rows.optString(it) }.filter(String::isNotBlank)
    }.orEmpty()
    val displayFields = state.schemaFields.filterNot { field ->
        field.hidden || field.fieldtype in setOf("Section Break", "Column Break", "Heading", "Table", "JSON", "Password")
    }
    val inferredColumns = listOf(state.titleField, "name") + displayFields.filter { it.inListView }.map { it.fieldname }
    val listColumns = (if (displayFields.none { it.inListView }) listOf(state.titleField, "name") + displayFields.take(3).map { it.fieldname } else inferredColumns).distinct()
    val mobileColumns = (configuredMobileColumns.ifEmpty { listColumns.take(4) }).distinct()
    val titleField = state.titleField
    val visibleRecords = state.records.filter { record ->
        state.listSearch.isBlank() || (mobileColumns + titleField + "name").distinct().any { column ->
            record.optString(column).contains(state.listSearch.trim(), ignoreCase = true)
        }
    }
    val statusField = state.schemaFields.firstOrNull { it.fieldname == "status" }?.fieldname
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 7.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(tableProps.optString("title").ifBlank { doctype.label }, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("${visibleRecords.size} records", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = vm::startNewRecord, shape = RoundedCornerShape(13.dp)) { Text("+ New") }
        }
        state.message?.takeUnless(::isPassiveConnectionMessage)?.let { MessageBanner(it, false) }
        KoraTextField(
            value = state.listSearch,
            onValueChange = vm::setListSearch,
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            singleLine = true,
            leadingIcon = { Icon(Icons.Outlined.Search, null) },
            placeholder = { Text("Search ${doctype.label.lowercase()}") }
        )
        if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (state.records.isEmpty() && !state.busy) EmptyCard("No ${doctype.label.lowercase()} yet", if (state.offline) "This list has not been cached yet. Connect to load records." else "Create a record to get started.")
        else if (visibleRecords.isEmpty() && !state.busy) EmptyCard("No matching records", "Try a different search.")
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(visibleRecords, key = { it.optString("_local_id").ifBlank { it.optString("name") } }) { record ->
                RecordCard(record, titleField, mobileColumns, displayFields, statusField, state.userLabels) { vm.viewRecord(record) }
            }
        }
    }
}

@Composable
internal fun RecordCard(
    record: JSONObject,
    titleField: String,
    columns: List<String>,
    fields: List<FieldSchema>,
    statusField: String?,
    userLabels: Map<String, String>,
    onClick: () -> Unit,
) {
    val title = record.optString(titleField).ifBlank { record.optString("name").ifBlank { "Record" } }
    val colors = androidx.compose.material3.MaterialTheme.colorScheme
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(17.dp), colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    if (titleField != "name") Text(if (record.optString("name").startsWith("local:")) "Saved on this device" else record.optString("name"), fontSize = 11.sp, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (!statusField.isNullOrBlank() && record.optString(statusField).isNotBlank()) {
                    Surface(color = colors.secondary, shape = RoundedCornerShape(50)) {
                        Text(record.optString(statusField), Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 11.sp, fontWeight = FontWeight.Medium, color = colors.onSecondary)
                    }
                }
                Icon(Icons.Outlined.ChevronRight, null, Modifier.padding(start = 6.dp), tint = colors.onSurfaceVariant)
            }
            val detailColumns = columns.filter { it != titleField && it != "name" }.take(3)
            if (detailColumns.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                detailColumns.forEach { column ->
                    val label = fields.firstOrNull { it.fieldname == column }?.label ?: column.replace('_', ' ').replaceFirstChar { it.uppercase() }
                    val raw = record.opt(column)
                    val field = fields.firstOrNull { it.fieldname == column }
                    val value = when {
                        raw == null || raw == JSONObject.NULL || raw == "" -> "—"
                        raw is Boolean -> if (raw) "Yes" else "No"
                        field?.fieldtype == "User" || (field?.fieldtype == "Link" && field.options.equals("User", true)) -> userLabels[raw.toString()] ?: "Workspace user unavailable"
                        field?.fieldtype == "Date" -> formatHumanDate(raw.toString(), dateOnly = true)
                        field?.fieldtype == "Datetime" -> formatHumanDate(raw.toString(), dateOnly = false)
                        else -> raw.toString()
                    }
                    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Text(label, Modifier.weight(1f), fontSize = 12.sp, color = colors.onSurfaceVariant)
                        Text(value, Modifier.weight(1.2f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}
