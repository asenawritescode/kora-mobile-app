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
internal fun RecordDetailScreen(state: AppState, vm: AppViewModel) {
    val record = state.selectedRecord ?: return
    val colors = androidx.compose.material3.MaterialTheme.colorScheme
    val visibleFields = state.schemaFields.filterNot { it.hidden }
    val title = record.optString(state.titleField).ifBlank { record.optString("name", "Record") }
    val status = state.schemaFields.firstOrNull { it.fieldname == "status" }?.let { record.optString(it.fieldname) }.orEmpty()
    val tableFields = visibleFields.filter { it.fieldtype == "Table" }
    val regularFields = visibleFields.filterNot { it.fieldtype in setOf("Section Break", "Column Break", "Heading", "Table") || it.fieldname == state.titleField || it.fieldname == "status" }
    val summaryFieldNames = setOf("grand_total", "total_amount", "total", "amount", "transaction_date", "posting_date", "sale_date", "date", "payment_method", "customer", "supplier")
    val summaryFields = regularFields.filter { it.fieldname.lowercase() in summaryFieldNames && record.opt(it.fieldname) != null && record.opt(it.fieldname) != JSONObject.NULL && record.optString(it.fieldname).isNotBlank() }
    val primaryMetric = summaryFields.firstOrNull { it.fieldname.lowercase() in setOf("grand_total", "total_amount", "total", "amount") }
    val quickFacts = summaryFields.filterNot { it == primaryMetric }.take(2)
    val detailFields = regularFields.filterNot { it in summaryFields }
    val detailSections = buildList {
        var sectionTitle = "Details"
        var sectionFields = mutableListOf<FieldSchema>()
        detailFields.forEach { field ->
            when (field.fieldtype) {
                "Section Break" -> {
                    if (sectionFields.isNotEmpty()) add(sectionTitle to sectionFields.toList())
                    sectionTitle = field.label.ifBlank { "Details" }
                    sectionFields = mutableListOf()
                }
                "Heading" -> {
                    if (sectionFields.isNotEmpty()) add(sectionTitle to sectionFields.toList())
                    sectionTitle = field.label.ifBlank { "Details" }
                    sectionFields = mutableListOf()
                }
                "Column Break" -> Unit
                else -> sectionFields.add(field)
            }
        }
        if (sectionFields.isNotEmpty()) add(sectionTitle to sectionFields.toList())
    }
    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(key = "record-summary") {
                val recordName = record.optString("name").let { if (it.startsWith("local:")) "Saved on this device" else it }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.outlineVariant.copy(alpha = .65f)),
                ) {
                    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(verticalAlignment = Alignment.Top) {
                            Column(Modifier.weight(1f).padding(end = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(title, style = androidx.compose.material3.MaterialTheme.typography.headlineSmall.copy(fontSize = 23.sp, lineHeight = 29.sp), fontWeight = FontWeight.SemiBold, color = colors.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                if (recordName.isNotBlank() && recordName != title) {
                                    Text(recordName, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                                }
                                if (status.isNotBlank()) {
                                    Surface(color = colors.surfaceVariant, shape = RoundedCornerShape(8.dp)) {
                                        Text(status, Modifier.padding(horizontal = 13.dp, vertical = 7.dp), style = androidx.compose.material3.MaterialTheme.typography.labelLarge, color = colors.primary, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                            OutlinedButton(
                                onClick = { vm.editRecord(record) },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.onSurface),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp),
                            ) {
                                Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(7.dp))
                                Text("Edit")
                            }
                        }
                        if (tableFields.isNotEmpty() && tableFields.first().let { record.optJSONArray(it.fieldname)?.length() ?: 0 } > 0) {
                            val primaryTable = tableFields.first()
                            val count = record.optJSONArray(primaryTable.fieldname)?.length() ?: 0
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(primaryTable.label, style = androidx.compose.material3.MaterialTheme.typography.labelLarge, color = colors.onPrimaryContainer.copy(alpha = .76f))
                                Text("$count ${if (count == 1) "item" else "items"}", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = colors.onSurface)
                            }
                        }
                        if (state.detailLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
                    }
                }
            }
            if (primaryMetric != null || quickFacts.isNotEmpty()) {
                item(key = "record-highlights") {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        primaryMetric?.let { field ->
                                    val value = if (field.fieldtype == "Currency") formatCurrency(record.opt(field.fieldname)) else record.opt(field.fieldname)?.toString().orEmpty()
                                    Surface(Modifier.weight(1.15f), color = colors.surface, shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, colors.outlineVariant.copy(alpha = .65f))) {
                                Column(Modifier.padding(horizontal = 18.dp, vertical = 15.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(field.label, style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                                    Text(value, style = androidx.compose.material3.MaterialTheme.typography.headlineSmall, color = colors.primary, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                        if (quickFacts.isNotEmpty()) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                quickFacts.forEach { field ->
                                    val raw = record.opt(field.fieldname)
                                    val value = when {
                                        field.fieldtype == "Date" -> formatHumanDate(raw.toString(), dateOnly = true)
                                        field.fieldtype == "Datetime" -> formatHumanDate(raw.toString(), dateOnly = false)
                                        field.fieldtype == "Link" -> state.detailLinkLabels["${field.options}:$raw"].orEmpty().ifBlank { raw.toString() }
                                        else -> raw.toString()
                                    }
                                    Surface(Modifier.fillMaxWidth(), color = colors.surfaceContainerLow, shape = RoundedCornerShape(14.dp)) {
                                        Column(Modifier.padding(horizontal = 13.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(field.label, style = androidx.compose.material3.MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                                            Text(value, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            state.message?.takeUnless(::isPassiveConnectionMessage)?.let { message -> item(key = "record-message") { MessageBanner(message, false) } }

            tableFields.forEach { tableField ->
                val childRows = record.optJSONArray(tableField.fieldname) ?: JSONArray()
                val childFields = state.childSchemas[tableField.options].orEmpty()
                    .filterNot { it.hidden || it.fieldtype in setOf("Section Break", "Column Break", "Heading", "Table", "JSON") }
                item(key = "table-heading-${tableField.fieldname}") {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(tableField.label, style = androidx.compose.material3.MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        Text("${childRows.length()} ${if (childRows.length() == 1) "item" else "items"}", style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    }
                }
                if (childRows.length() == 0) {
                    item(key = "table-empty-${tableField.fieldname}") {
                        Text(
                            if (state.detailLoading) "Loading items…" else "No items in this record",
                            Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                        )
                    }
                } else {
                    items((0 until childRows.length()).toList(), key = { index -> "${tableField.fieldname}-$index" }) { index ->
                        val child = childRows.optJSONObject(index) ?: JSONObject()
                        val primaryField = childFields.firstOrNull { it.fieldname in setOf("product", "item", "title", "name") && it.fieldtype in setOf("Link", "Data") }
                            ?: childFields.firstOrNull { it.fieldtype == "Link" }
                            ?: childFields.firstOrNull { it.fieldtype == "Data" }
                        val primaryId = primaryField?.let { child.optString(it.fieldname) }.orEmpty()
                        val primaryLabel = primaryField?.let { field ->
                            if (field.fieldtype == "Link") state.detailLinkLabels["${field.options}:$primaryId"].orEmpty().ifBlank { primaryId } else primaryId
                        }.orEmpty()
                        val visibleChildFields = childFields.filter { it.fieldname != primaryField?.fieldname }
                            Surface(color = colors.surface, shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, colors.outlineVariant.copy(alpha = .65f))) {
                            Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        if (primaryLabel.isNotBlank()) Text(primaryLabel, style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                        val secondary = childFields.firstOrNull { it.fieldname in setOf("sku", "code", "item_code") }
                                            ?.let { child.optString(it.fieldname) }.orEmpty()
                                        if (secondary.isNotBlank()) Text(secondary, style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                                    }
                                    visibleChildFields.firstOrNull { it.fieldname in setOf("line_total", "amount", "total") }?.let { totalField ->
                                        Text(child.opt(totalField.fieldname)?.toString().orEmpty(), style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.primary)
                                    }
                                }
                                val summaryFields = visibleChildFields.filterNot { it.fieldname in setOf("line_total", "amount", "total", "sku", "code", "item_code") }
                                if (summaryFields.isNotEmpty()) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        summaryFields.forEach { childField ->
                                    val raw = child.opt(childField.fieldname)
                                    val value = when {
                                        raw == null || raw == JSONObject.NULL || raw == "" -> "—"
                                        childField.fieldtype == "Link" -> state.detailLinkLabels["${childField.options}:$raw"].orEmpty().ifBlank { raw.toString() }
                                        childField.fieldtype == "Date" -> formatHumanDate(raw.toString(), dateOnly = true)
                                        childField.fieldtype == "Datetime" -> formatHumanDate(raw.toString(), dateOnly = false)
                                        else -> raw.toString()
                                    }
                                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Text(childField.label, style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text(value, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (detailFields.isNotEmpty()) {
                    detailSections.forEachIndexed { sectionIndex, (sectionTitle, sectionFields) ->
                        item(key = "detail-section-$sectionIndex") {
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(sectionTitle, style = androidx.compose.material3.MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                                if (sectionTitle == "Details") Text("Record information", style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                            }
                        }
                        item(key = "detail-table-$sectionIndex") {
                    Surface(color = colors.surface, shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, colors.outlineVariant.copy(alpha = .65f))) {
                        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                            sectionFields.forEachIndexed { index, field ->
                    val raw = record.opt(field.fieldname)
                    val value = when {
                        raw == null || raw == JSONObject.NULL || raw == "" -> "—"
                        raw is Boolean -> if (raw) "Yes" else "No"
                        field.fieldtype == "User" || (field.fieldtype == "Link" && field.options.equals("User", true)) -> state.userLabels[raw.toString()] ?: "Workspace user unavailable"
                    field.fieldtype == "Link" -> state.detailLinkLabels["${field.options}:$raw"].orEmpty().ifBlank { raw.toString() }
                    field.fieldtype == "Currency" -> formatCurrency(raw)
                        field.fieldtype == "Date" -> formatHumanDate(raw.toString(), dateOnly = true)
                        field.fieldtype == "Datetime" -> formatHumanDate(raw.toString(), dateOnly = false)
                        else -> raw.toString()
                    }
                                Row(Modifier.fillMaxWidth().padding(vertical = 13.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Text(field.label, Modifier.weight(.9f), style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                                    Text(value, Modifier.weight(1.1f), style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, textAlign = androidx.compose.ui.text.style.TextAlign.End)
                                }
                                if (index < sectionFields.lastIndex) Divider(color = colors.outlineVariant.copy(alpha = .55f))
                            }
                        }
                    }
                        }
                    }
            }
            if (detailFields.isEmpty() && tableFields.isEmpty()) {
                item(key = "no-detail-fields") { EmptyCard("No additional details", "This record has no other fields to display.") }
            }
        }
    }
}

internal data class MobileReportMetric(val label: String, val value: String)
internal data class MobileReportChart(val title: String, val rows: JSONArray, val indexField: String, val seriesField: String, val mode: String, val format: String = "number")

@Composable
internal fun ReportChartCard(chart: MobileReportChart) {
    val colors = androidx.compose.material3.MaterialTheme.colorScheme
    val values = (0 until chart.rows.length()).mapNotNull { index -> (chart.rows.optJSONObject(index)?.opt(chart.seriesField) as? Number)?.toFloat() }
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(chart.title, style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (values.isEmpty()) Text("No time series is available for this report.", color = colors.onSurfaceVariant)
            else if (values.size == 1) {
                val row = chart.rows.optJSONObject(0) ?: JSONObject()
                val raw = row.opt(chart.seriesField)
                val number = (raw as? Number)?.toDouble()
                val value = when {
                    number == null -> raw?.toString() ?: "—"
                    chart.format == "currency" -> "KES ${java.text.NumberFormat.getNumberInstance().format(number)}"
                    chart.format == "percent" -> "${"%.1f".format(number)}%"
                    else -> java.text.NumberFormat.getNumberInstance().format(number)
                }
                val rawCategory = row.optString(chart.indexField)
                val category = when {
                    rawCategory.isBlank() -> "This period"
                    chart.indexField.contains("date", ignoreCase = true) || chart.indexField.contains("time", ignoreCase = true) -> formatHumanDate(rawCategory, dateOnly = true)
                    else -> rawCategory
                }
                Surface(color = colors.surfaceVariant, shape = RoundedCornerShape(10.dp)) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 13.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(category, color = colors.onSurfaceVariant, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                        Text(value, color = colors.tertiary, style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            else {
                Canvas(Modifier.fillMaxWidth().height(164.dp)) {
                    for (line in 1..3) {
                        val y = size.height * line / 4f
                        drawLine(colors.outlineVariant.copy(alpha = .35f), Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
                    }
                    val max = values.maxOrNull() ?: 1f
                    val min = values.minOrNull() ?: 0f
                    if (chart.mode == "line" || chart.mode == "area") {
                        val points = values.mapIndexed { index, value ->
                            val x = if (values.size == 1) size.width / 2f else 8.dp.toPx() + index.toFloat() / (values.size - 1) * (size.width - 16.dp.toPx())
                            val ratio = if (max == min) .58f else (value - min) / (max - min)
                            Offset(x, size.height - 12.dp.toPx() - ratio * (size.height - 28.dp.toPx()))
                        }
                        if (points.size > 1) {
                            val area = Path().apply {
                                moveTo(points.first().x, size.height)
                                lineTo(points.first().x, points.first().y)
                                points.zipWithNext().forEach { (start, end) ->
                                    val midX = (start.x + end.x) / 2f
                                    cubicTo(midX, start.y, midX, end.y, end.x, end.y)
                                }
                                lineTo(points.last().x, size.height)
                                close()
                            }
                            drawPath(area, Brush.verticalGradient(listOf(colors.tertiary.copy(alpha = .22f), colors.tertiary.copy(alpha = .015f)), 0f, size.height))
                            val linePath = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                points.zipWithNext().forEach { (start, end) ->
                                    val midX = (start.x + end.x) / 2f
                                    cubicTo(midX, start.y, midX, end.y, end.x, end.y)
                                }
                            }
                            drawPath(linePath, colors.tertiary, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
                        }
                        points.lastOrNull()?.let { point ->
                            drawCircle(colors.surface, 6.dp.toPx(), point)
                            drawCircle(colors.tertiary, 4.dp.toPx(), point)
                        }
                    } else {
                        val cellWidth = size.width / values.size
                        val barWidth = cellWidth * .58f
                        values.forEachIndexed { index, value ->
                            val ratio = if (max == min) .72f else ((value - min) / (max - min)).coerceAtLeast(.06f)
                            val height = ratio * (size.height - 12.dp.toPx())
                            val x = index * cellWidth + (cellWidth - barWidth) / 2f
                            drawRoundRect(colors.tertiary, Offset(x, size.height - height), androidx.compose.ui.geometry.Size(barWidth, height), androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()))
                        }
                    }
                }
                val first = chart.rows.optJSONObject(0)?.optString(chart.indexField).orEmpty()
                val last = chart.rows.optJSONObject(chart.rows.length() - 1)?.optString(chart.indexField).orEmpty()
                val dateAxis = chart.indexField.contains("date", ignoreCase = true) || chart.indexField.contains("time", ignoreCase = true)
                val firstLabel = if (dateAxis) formatHumanDate(first, dateOnly = true) else first
                val lastLabel = if (dateAxis) formatHumanDate(last, dateOnly = true) else last
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(firstLabel, style = androidx.compose.material3.MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant, maxLines = 1)
                    Text(lastLabel, style = androidx.compose.material3.MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant, maxLines = 1)
                }
            }
        }
    }
}

@Composable
internal fun ReportsScreen(state: AppState, vm: AppViewModel) {
    var showReportData by remember(state.selectedReportName) { mutableStateOf(false) }
    val colors = androidx.compose.material3.MaterialTheme.colorScheme
    val catalogModels = state.analyticsCatalog?.optJSONArray("models") ?: JSONArray()
    val models = (0 until catalogModels.length()).mapNotNull(catalogModels::optJSONObject)
    val selectedReport = state.savedReports.firstOrNull { it.optString("name") == state.selectedReportName }
    val reportQueries = selectedReport?.optJSONArray("queries") ?: JSONArray()
    val results = state.reportResult?.optJSONArray("queries") ?: JSONArray()
    val timeRange = listOf(7 to "7d", 30 to "30d", 90 to "90d")
    val metricKeys = mutableSetOf<String>()
    val metrics = buildList {
        for (queryIndex in 0 until results.length()) {
            val result = results.optJSONObject(queryIndex) ?: continue
            val queryDef = reportQueries.optJSONObject(queryIndex)
            val modelName = queryDef?.optString("model")?.ifBlank { result.optString("model") } ?: result.optString("model")
            val model = models.firstOrNull { it.optString("name") == modelName }
            val queryMeasures = queryDef?.optJSONArray("measures") ?: JSONArray()
            val rows = result.optJSONArray("rows") ?: JSONArray()
            for (measureIndex in 0 until queryMeasures.length()) {
                val measureName = queryMeasures.optString(measureIndex)
                if (measureName.isBlank()) continue
                if (!metricKeys.add("$modelName:$measureName")) continue
                val measure = model?.optJSONArray("measures")?.let { list -> (0 until list.length()).mapNotNull(list::optJSONObject).firstOrNull { it.optString("name") == measureName } }
                val total = (0 until rows.length()).sumOf { rowIndex -> (rows.optJSONObject(rowIndex)?.opt(measureName) as? Number)?.toDouble() ?: 0.0 }
                val format = measure?.optString("format").orEmpty()
                val display = if (format == "currency") "KES ${java.text.NumberFormat.getNumberInstance().format(total)}" else java.text.NumberFormat.getNumberInstance().format(total)
                add(MobileReportMetric(measure?.optString("label")?.ifBlank { measureName } ?: measureName, display))
            }
        }
    }
    val visuals = selectedReport?.optJSONArray("visuals") ?: JSONArray()
    val reportCharts = buildList {
        if (visuals.length() > 0) {
            for (visualIndex in 0 until visuals.length()) {
                val visual = visuals.optJSONObject(visualIndex) ?: continue
                val source = visual.optString("source")
                val queryIndex = (0 until reportQueries.length()).firstOrNull { reportQueries.optJSONObject(it)?.optString("id") == source } ?: continue
                val query = reportQueries.optJSONObject(queryIndex) ?: continue
                val result = results.optJSONObject(queryIndex) ?: continue
                val series = visual.optJSONArray("series")?.optJSONObject(0)
                val field = series?.optString("field").orEmpty().ifBlank { query.optJSONArray("measures")?.optString(0).orEmpty() }
                val index = visual.optString("x_field").ifBlank { query.optJSONArray("dimensions")?.optString(0).orEmpty() }
                if (field.isNotBlank() && index.isNotBlank()) add(MobileReportChart(
                    visual.optString("title").ifBlank { series?.optString("label")?.ifBlank { field } ?: field },
                    result.optJSONArray("rows") ?: JSONArray(), index, field,
                    series?.optString("type").orEmpty().ifBlank { visual.optString("type", "bar") },
                    series?.optString("format").orEmpty().ifBlank { "number" },
                ))
            }
        }
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (state.savedReports.isNotEmpty()) item(key = "saved-report-picker") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Choose a report", style = androidx.compose.material3.MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.savedReports.forEach { report ->
                        val name = report.optString("name")
                        FilterChip(selected = state.selectedReportName == name, onClick = { vm.selectSavedReport(name) }, label = { Text(report.optString("label").ifBlank { name }) })
                    }
                }
            }
        }
        item(key = "report-period") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Time period", style = androidx.compose.material3.MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    timeRange.forEach { (days, label) -> FilterChip(selected = state.reportWindowDays == days, onClick = { vm.setReportWindow(days) }, label = { Text(label, style = androidx.compose.material3.MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium) }) }
                    IconButton(onClick = vm::refreshAnalytics, enabled = !state.reportLoading) { Icon(Icons.Filled.Refresh, "Refresh report") }
                }
            }
        }
        if (state.reportLoading) item(key = "report-loading") { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        state.reportError?.let { error -> item(key = "report-error") { MessageBanner("Reporting unavailable: $error", state.offline) } }
        if (state.analyticsCatalog == null && state.reportLoading) {
            item(key = "report-skeleton") { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } } }
        } else if (!state.reportLoading && state.analyticsCatalog == null) {
            item(key = "report-empty") { EmptyCard("Reports aren’t available", "Connect to the workspace or ask an administrator to enable an approved analytics model.") }
        } else if (state.savedReports.isEmpty()) {
            item(key = "report-not-configured") { EmptyCard("No reports are set up", "Ask your workspace administrator to add the reports your team needs.") }
        } else if (state.reportResult != null && metrics.isEmpty()) {
            item(key = "report-no-data") { EmptyCard("No data for this period", "Choose a longer time period or check again later.") }
        } else if (metrics.isNotEmpty()) {
            item(key = "report-metrics") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("At a glance", style = androidx.compose.material3.MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        metrics.take(2).forEach { metric ->
                            Surface(Modifier.weight(1f), color = colors.surfaceContainerLow, shape = RoundedCornerShape(12.dp)) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                    Text(metric.label, style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                                    Text(metric.value, style = androidx.compose.material3.MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colors.tertiary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                        if (metrics.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
            reportCharts.firstOrNull()?.let { chart ->
                item(key = "report-chart") { ReportChartCard(chart) }
            }
            val rowCount = (0 until results.length()).sumOf { results.optJSONObject(it)?.optJSONArray("rows")?.length() ?: 0 }
            if (rowCount > 0) item(key = "report-data-toggle") {
                TextButton(onClick = { showReportData = !showReportData }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (showReportData) "Hide report data" else "View report data ($rowCount)")
                }
            }
            if (showReportData) for (queryIndex in 0 until results.length()) {
                val result = results.optJSONObject(queryIndex) ?: continue
                val rows = result.optJSONArray("rows") ?: JSONArray()
                val columns = result.optJSONArray("columns") ?: JSONArray()
                val queryDef = reportQueries.optJSONObject(queryIndex)
                val rowIndexField = queryDef?.optJSONArray("dimensions")?.optString(0).orEmpty()
                    .ifBlank { models.firstOrNull { it.optString("name") == result.optString("model") }?.optString("time_dimension").orEmpty() }
                    .ifBlank { columns.optString(0) }
                val queryModel = models.firstOrNull { it.optString("name") == result.optString("model") }
                item(key = "report-result-${result.optString("model")}-$queryIndex") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(queryModel?.optString("label")?.ifBlank { result.optString("model") } ?: result.optString("model"), style = androidx.compose.material3.MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        if (rows.length() == 0) Text("No rows in this period.", color = colors.onSurfaceVariant)
                        else for (rowIndex in 0 until minOf(rows.length(), 100)) {
                            val row = rows.optJSONObject(rowIndex) ?: continue
                            val primary = row.optString(rowIndexField).ifBlank { row.optString(columns.optString(0)) }
                            val dataColumns = (0 until columns.length()).map(columns::optString).filterNot { it == rowIndexField }
                            Surface(color = colors.surfaceContainerLow, shape = RoundedCornerShape(16.dp)) {
                                Column(Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 13.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(primary.ifBlank { "Result ${rowIndex + 1}" }, style = androidx.compose.material3.MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                    dataColumns.forEach { column ->
                                        val label = queryModel?.optJSONArray("measures")?.let { list -> (0 until list.length()).mapNotNull(list::optJSONObject).firstOrNull { it.optString("name") == column }?.optString("label") }?.ifBlank { column } ?: column.replace('_', ' ')
                                        val value = row.opt(column)?.toString() ?: "—"
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(label, color = colors.onSurfaceVariant, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                                            Text(value, fontWeight = FontWeight.Medium, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (showReportData) state.reportResult?.let { result ->
            val source = result.optString("data_source")
            val generated = result.optString("generated_at")
            item(key = "report-source") {
                Text(listOfNotNull(source.takeIf(String::isNotBlank), generated.takeIf(String::isNotBlank)).joinToString(" · "), style = androidx.compose.material3.MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecordForm(state: AppState, vm: AppViewModel) {
    val context = LocalContext.current
    val colors = androidx.compose.material3.MaterialTheme.colorScheme
    val visibleFields = state.schemaFields.filter { fieldIsVisible(it, state.formValues) }
    val requiredFields = visibleFields.filter { fieldIsRequired(it, state.formValues) }
    val filledRequired = requiredFields.count { field -> state.formValues[field.fieldname]?.toString()?.isNotBlank() == true }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(if (state.editingRecord == null) "New ${state.selectedDoctype?.label ?: "record"}" else "Edit ${state.selectedDoctype?.label ?: "record"}", fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Text("$filledRequired of ${requiredFields.size} required fields complete", fontSize = 12.sp, color = colors.onSurfaceVariant)
            }
        }
        state.message?.takeUnless(::isPassiveConnectionMessage)?.let { MessageBanner(it, false) }
        if (requiredFields.isNotEmpty()) LinearProgressIndicator(
            progress = { filledRequired.toFloat() / requiredFields.size },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        )
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(visibleFields, key = { it.fieldname }) { field ->
                val value = state.formValues[field.fieldname]
                val editable = !field.readOnly || (state.editingRecord == null && field.editableOnCreate)
                val required = fieldIsRequired(field, state.formValues)
                val label = field.label + if (required) " *" else ""
                val constraintHint = fieldConstraintHint(field, value, state.formValues)
                when (field.fieldtype) {
                    "Section Break" -> {
                        Column(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 2.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Text(field.label, style = androidx.compose.material3.MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = colors.onSurface)
                            Divider(color = colors.outlineVariant)
                        }
                    }
                    "Heading" -> Text(field.label, Modifier.padding(top = 8.dp), fontWeight = FontWeight.SemiBold, color = colors.onSurfaceVariant)
                    "Column Break" -> Spacer(Modifier.height(1.dp))
                    "Check" -> Row(Modifier.fillMaxWidth().clickable(enabled = editable) { vm.updateField(field.fieldname, value != true) }, verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = value == true || value.toString() == "1", onCheckedChange = { vm.updateField(field.fieldname, it) }, enabled = editable)
                        Column(Modifier.weight(1f)) {
                            Text(label, fontWeight = if (field.bold) FontWeight.SemiBold else FontWeight.Medium)
                            field.description?.let { Text(it, fontSize = 12.sp, color = colors.onSurfaceVariant) }
                        }
                    }
                    "Select", "Data" -> {
                        val choices = field.options.orEmpty().lineSequence().map(String::trim).filter(String::isNotBlank).distinct().toList()
                        val isChoiceField = field.fieldtype == "Select" || (field.fieldtype == "Data" && choices.size > 1)
                        if (isChoiceField) {
                            ChoiceField(
                                label = label,
                                value = value?.toString().orEmpty(),
                                placeholder = "Choose ${field.label.lowercase()}",
                                choices = choices,
                                allowEmpty = !required,
                                enabled = editable,
                                onSelect = { vm.updateField(field.fieldname, it) },
                            )
                        } else {
                            KoraTextField(
                                value = value?.toString().orEmpty(), onValueChange = { vm.updateField(field.fieldname, it) }, modifier = Modifier.fillMaxWidth(),
                                label = label,
                                enabled = editable, readOnly = !editable, singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = when (field.options) { "Email" -> KeyboardType.Email; "Phone" -> KeyboardType.Phone; "URL" -> KeyboardType.Uri; else -> KeyboardType.Text })
                            )
                        }
                    }
                    "Int", "Float", "Currency", "Percent" -> KoraTextField(
                        value = value?.toString().orEmpty(), onValueChange = { raw ->
                            val parsed: Any? = when (field.fieldtype) { "Int" -> raw.toLongOrNull() ?: raw; else -> raw.toDoubleOrNull() ?: raw }
                            vm.updateField(field.fieldname, if (raw.isEmpty()) null else parsed)
                        }, modifier = Modifier.fillMaxWidth(), label = label,
                        enabled = editable,
                        keyboardOptions = KeyboardOptions(keyboardType = if (field.fieldtype == "Int") KeyboardType.Number else KeyboardType.Decimal),
                        singleLine = true
                    )
                    "Date" -> {
                        var pickerOpen by remember(field.fieldname) { mutableStateOf(false) }
                        val chosenDate = runCatching { LocalDate.parse(value?.toString()?.take(10)) }.getOrDefault(LocalDate.now())
                        PickerField(label, value?.toString()?.take(10)?.let { runCatching { LocalDate.parse(it).format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.getDefault())) }.getOrNull() }.orEmpty(), "Choose a date", enabled = editable, onClick = { pickerOpen = true })
                        if (pickerOpen) {
                            val dateState = rememberDatePickerState(initialSelectedDateMillis = chosenDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
                            DatePickerDialog(
                                onDismissRequest = { pickerOpen = false },
                                confirmButton = {
                                    TextButton(onClick = {
                                        dateState.selectedDateMillis?.let { millis ->
                                            vm.updateField(field.fieldname, Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString())
                                        }
                                        pickerOpen = false
                                    }) { Text("Apply") }
                                },
                                dismissButton = {
                                    Row {
                                        TextButton(onClick = {
                                            dateState.selectedDateMillis = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                                        }) { Text("Today") }
                                        TextButton(onClick = { pickerOpen = false }) { Text("Cancel") }
                                    }
                                },
                                colors = DatePickerDefaults.colors(containerColor = colors.surface),
                            ) {
                                DatePicker(state = dateState, title = { Text("Choose ${field.label.lowercase()}", Modifier.padding(start = 24.dp, top = 20.dp), style = androidx.compose.material3.MaterialTheme.typography.titleMedium) }, showModeToggle = true)
                            }
                        }
                    }
                    "Time" -> PickerField(label, value?.toString().orEmpty(), "Choose time", enabled = editable, onClick = {
                            val pieces = value?.toString().orEmpty().split(':')
                            TimePickerDialog(context, { _, h, m -> vm.updateField(field.fieldname, "%02d:%02d".format(h, m)) }, pieces.getOrNull(0)?.toIntOrNull() ?: 9, pieces.getOrNull(1)?.toIntOrNull() ?: 0, true).show()
                        })
                    "Link", "Dynamic Link" -> {
                        val targetField = field.options.orEmpty()
                        val target = if (field.fieldtype == "Dynamic Link") state.formValues[targetField]?.toString().orEmpty() else targetField
                        var pickerOpen by remember(field.fieldname) { mutableStateOf(false) }
                        var pickerSearch by remember(field.fieldname) { mutableStateOf("") }
                        val options = state.linkOptions[target].orEmpty()
                        val targetTitleField = state.linkTitleFields[target] ?: "name"
                        val selectedLabel = options.firstOrNull { it.optString("name") == value?.toString() }
                            ?.let { it.optString(targetTitleField).ifBlank { it.optString("name") } }
                            ?: value?.toString().orEmpty()
                        PickerField(label, selectedLabel, if (target.isBlank()) "Choose a link type first" else "Choose ${field.label.lowercase()}", enabled = editable && target.isNotBlank(), onClick = {
                            pickerSearch = ""
                            pickerOpen = true
                            vm.loadLinkOptions(target)
                        })
                        if (pickerOpen) {
                            ModalBottomSheet(onDismissRequest = { pickerOpen = false }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = colors.surface) {
                                Column(Modifier.fillMaxWidth().heightIn(min = 450.dp, max = 720.dp).imePadding().padding(horizontal = 20.dp)) {
                                    Text("Choose ${field.label.lowercase()}", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                                    Text("Select a ${target.lowercase()} record to link to this form.", Modifier.padding(top = 5.dp, bottom = 16.dp), color = colors.onSurfaceVariant, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                                    KoraTextField(
                                        pickerSearch, { pickerSearch = it }, Modifier.fillMaxWidth(), singleLine = true,
                                        leadingIcon = { Icon(Icons.Outlined.Search, null) }, placeholder = { Text("Search ${target.lowercase()}") }
                                    )
                                    val matches = options.filter { item -> item.optString(targetTitleField).contains(pickerSearch, true) || item.optString("name").contains(pickerSearch, true) }
                                    Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(if (pickerSearch.isBlank()) "${options.size} available" else "${matches.size} matches", style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                                        Spacer(Modifier.weight(1f))
                                        if (selectedLabel.isNotBlank()) Text("Selected: $selectedLabel", style = androidx.compose.material3.MaterialTheme.typography.labelSmall, color = colors.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    when {
                                        target in state.linkLoading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { CircularProgressIndicator(); Text("Loading ${target.lowercase()}…", Modifier.padding(top = 12.dp), color = colors.onSurfaceVariant) } }
                                        state.linkErrors[target] != null -> Column(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Filled.CloudOff, null, tint = colors.error, modifier = Modifier.size(32.dp))
                                            Text("Couldn’t load records", Modifier.padding(top = 10.dp), fontWeight = FontWeight.SemiBold)
                                            Text(state.linkErrors[target].orEmpty(), Modifier.padding(top = 4.dp), color = colors.onSurfaceVariant)
                                            TextButton(onClick = { vm.loadLinkOptions(target) }) { Text("Try again") }
                                        }
                                        options.isEmpty() -> EmptyCard("No records available", "There are no ${target.lowercase()} records to choose from.")
                                        matches.isEmpty() -> EmptyCard("No matches", "Try a different name or reference.")
                                        else -> LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(bottom = 18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            items(matches, key = { it.optString("name") }) { item ->
                                                val itemName = item.optString("name")
                                                val itemLabel = item.optString(targetTitleField).ifBlank { itemName }
                                                val isSelected = value?.toString() == itemName
                                                Card(Modifier.fillMaxWidth().clickable {
                                                    vm.updateLinkedField(field.fieldname, itemName)
                                                    pickerOpen = false
                                                    pickerSearch = ""
                                                }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (isSelected) colors.secondaryContainer else colors.surfaceVariant.copy(alpha = .45f))) {
                                                    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                                                        Box(Modifier.size(38.dp).background(if (isSelected) colors.primary.copy(alpha = .12f) else colors.surfaceVariant, CircleShape), contentAlignment = Alignment.Center) {
                                                            Icon(Icons.Filled.Inventory2, null, tint = if (isSelected) colors.primary else colors.onSurfaceVariant, modifier = Modifier.size(19.dp))
                                                        }
                                                        Column(Modifier.weight(1f).padding(start = 12.dp, end = 8.dp)) {
                                                            Text(itemLabel, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                            if (itemLabel != itemName) Text("Ref · $itemName", Modifier.padding(top = 2.dp), style = androidx.compose.material3.MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                        }
                                                        if (isSelected) Icon(Icons.Filled.CheckCircle, null, tint = colors.primary) else Icon(Icons.Outlined.ChevronRight, null, tint = colors.onSurfaceVariant)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(16.dp))
                                }
                            }
                        }
                    }
                    "Table" -> ChildTableField(
                        field = field,
                        value = value,
                        enabled = editable,
                        onChange = { vm.updateField(field.fieldname, it) },
                        onLinkedChange = { childDoctype, childField, picked -> vm.updateChildLinkedField(field.fieldname, childDoctype, childField, picked) },
                        onLoadOptions = vm::loadLinkOptions,
                        state = state,
                        onLoadSchema = vm::loadChildSchema,
                    )
                    "Attach", "Attach Image", "Attach Audio" -> Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant)) {
                        Column(Modifier.fillMaxWidth().padding(14.dp)) {
                            Text(label, fontWeight = if (field.bold) FontWeight.SemiBold else FontWeight.Medium)
                            Text(value?.toString()?.takeIf(String::isNotBlank) ?: "File uploads are not available in this app yet. Use the website to attach a file.", Modifier.padding(top = 4.dp), fontSize = 12.sp, color = colors.onSurfaceVariant)
                        }
                    }
                    else -> KoraTextField(
                        value = value?.toString().orEmpty(), onValueChange = { raw ->
                            val next: Any? = if (field.fieldtype == "JSON") runCatching { org.json.JSONTokener(raw).nextValue() }.getOrElse { raw } else raw
                            vm.updateField(field.fieldname, next)
                        }, modifier = Modifier.fillMaxWidth(), label = label, enabled = editable,
                        visualTransformation = if (field.fieldtype == "Password") PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
                        minLines = if (field.fieldtype in setOf("Text", "Text Editor", "Small Text", "Long Text", "JSON")) 3 else 1,
                        keyboardOptions = KeyboardOptions(keyboardType = if (field.fieldtype == "Password") KeyboardType.Password else KeyboardType.Text)
                    )
                }
                if (!field.description.isNullOrBlank() && field.fieldtype !in setOf("Check", "Attach", "Attach Image", "Attach Audio", "Table")) {
                    Text(field.description, Modifier.padding(start = 4.dp, top = 4.dp), fontSize = 11.sp, color = colors.onSurfaceVariant)
                }
                constraintHint?.let { Text(it, Modifier.padding(start = 4.dp, top = 4.dp), fontSize = 12.sp, color = colors.error) }
            }
            if (state.schemaFields.isEmpty()) item { EmptyCard("Form schema unavailable", "Connect to load the field layout for this record type.") }
        }
        Button(onClick = { vm.submitRecord() }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 12.dp).height(50.dp), enabled = !state.busy, shape = RoundedCornerShape(14.dp)) { if (state.busy) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) else Text("Save") }
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

