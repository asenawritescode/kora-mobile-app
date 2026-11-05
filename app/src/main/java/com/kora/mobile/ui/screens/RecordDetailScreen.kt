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
