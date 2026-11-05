package com.kora.mobile.ui.components

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
@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
internal fun KoraTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: String? = null,
    placeholder: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = false,
    minLines: Int = 1,
) {
    val colors = androidx.compose.material3.MaterialTheme.colorScheme
    val type = androidx.compose.material3.MaterialTheme.typography
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val shape = RoundedCornerShape(KoraTokens.RadiusField)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (!label.isNullOrBlank()) {
            Text(label, style = type.labelMedium, fontWeight = FontWeight.Medium, color = colors.onSurface)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = if (minLines > 1) 60.dp else 42.dp)
                .background(if (enabled) colors.surfaceContainerLow else colors.surfaceVariant, shape)
                .border(if (focused) 2.dp else 1.dp, if (focused) colors.primary else colors.outlineVariant, shape)
                .semantics { label?.let { contentDescription = it } }
                .padding(horizontal = 12.dp, vertical = 5.dp),
            verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
        ) {
            leadingIcon?.let { icon ->
                Box(Modifier.padding(end = 8.dp), contentAlignment = Alignment.Center) { icon() }
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f).padding(vertical = 0.dp)
                    .semantics { testTagsAsResourceId = true; label?.let { contentDescription = "$it input" } }
                    .testTag("input-" + label.orEmpty().lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')),
                enabled = enabled,
                readOnly = readOnly,
                textStyle = type.bodyMedium.copy(color = if (enabled) colors.onSurface else colors.onSurfaceVariant),
                keyboardOptions = keyboardOptions,
                singleLine = singleLine,
                minLines = minLines,
                visualTransformation = visualTransformation,
                interactionSource = interactionSource,
                decorationBox = { innerTextField ->
                    Box {
                        if (value.isEmpty() && placeholder != null) {
                            placeholder()
                        }
                        innerTextField()
                    }
                },
            )
            trailingIcon?.let { icon ->
                Box(Modifier.padding(start = 8.dp), contentAlignment = Alignment.Center) { icon() }
            }
        }
    }
}

@Composable
internal fun ChoiceField(
    label: String,
    value: String,
    placeholder: String,
    choices: List<String>,
    allowEmpty: Boolean,
    enabled: Boolean,
    onSelect: (String) -> Unit,
) {
    var expanded by remember(label) { mutableStateOf(false) }
    val colors = androidx.compose.material3.MaterialTheme.colorScheme
    Box(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(label, style = androidx.compose.material3.MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = colors.onSurface)
            Surface(
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(enabled = enabled) { expanded = true },
                shape = RoundedCornerShape(KoraTokens.RadiusField),
                color = colors.surfaceContainerLow,
                border = androidx.compose.foundation.BorderStroke(if (expanded) 2.dp else 1.dp, if (expanded) colors.primary else colors.outlineVariant),
            ) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(value.ifBlank { placeholder }, Modifier.weight(1f), style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = if (value.isBlank()) colors.onSurfaceVariant else colors.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Icon(Icons.Filled.ArrowDropDown, null, tint = if (expanded) colors.primary else colors.onSurfaceVariant)
                }
            }
        }
        val menuOptions = buildList {
            if (allowEmpty && value.isNotBlank()) add("" to "None")
            choices.forEach { add(it to it) }
        }
        val menuViewport = (menuOptions.size.coerceAtMost(4).coerceAtLeast(1) * 48).dp
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(.88f),
            shape = RoundedCornerShape(2.dp),
            containerColor = colors.surface,
            tonalElevation = 1.dp,
            shadowElevation = 4.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.outlineVariant),
        ) {
            Column(Modifier.height(menuViewport).verticalScroll(rememberScrollState())) {
                if (menuOptions.isEmpty()) {
                    Text("No options available", Modifier.padding(horizontal = 16.dp, vertical = 14.dp), color = colors.onSurfaceVariant, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                } else menuOptions.forEach { (option, optionLabel) ->
                    val selected = option == value
                    DropdownMenuItem(
                        text = { Text(optionLabel, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal) },
                        onClick = { onSelect(option); expanded = false },
                        modifier = Modifier.fillMaxWidth().height(48.dp).background(if (selected) colors.secondaryContainer else colors.surface),
                        trailingIcon = { if (selected) Icon(Icons.Filled.CheckCircle, null, tint = colors.primary, modifier = Modifier.size(18.dp)) },
                    )
                }
            }
        }
    }
}

@Composable
internal fun PickerField(label: String, value: String, placeholder: String, enabled: Boolean, onClick: () -> Unit) {
    val colors = androidx.compose.material3.MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, style = androidx.compose.material3.MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = colors.onSurface)
        Surface(
            modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp).clickable(enabled = enabled, onClick = onClick),
            shape = RoundedCornerShape(KoraTokens.RadiusField),
            color = colors.surfaceContainerLow,
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.outlineVariant),
        ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(value.ifBlank { placeholder }, Modifier.weight(1f), style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = if (value.isBlank()) colors.onSurfaceVariant else colors.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Icon(Icons.Filled.ArrowDropDown, null, tint = colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ChildTableField(
    field: FieldSchema,
    value: Any?,
    enabled: Boolean,
    onChange: (List<Map<String, Any?>>) -> Unit,
    onLinkedChange: (String, String, Any?) -> Unit,
    onLoadOptions: (String) -> Unit,
    state: AppState,
    onLoadSchema: (String) -> Unit,
) {
    val colors = androidx.compose.material3.MaterialTheme.colorScheme
    val childType = field.options.orEmpty()
    var expandedRow by rememberSaveable(field.fieldname) { mutableStateOf<Int?>(null) }
    val childFields = state.childSchemas[childType].orEmpty().filter { !it.hidden && it.fieldtype !in setOf("Section Break", "Column Break", "Heading", "Table") }
    val rows = (value as? List<*>)?.mapNotNull { row -> (row as? Map<*, *>)?.entries?.associate { it.key.toString() to it.value } }.orEmpty()
    androidx.compose.runtime.LaunchedEffect(childType) { if (childType.isNotBlank()) onLoadSchema(childType) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(field.label, fontWeight = if (field.bold) FontWeight.SemiBold else FontWeight.Medium)
                Text(if (rows.isEmpty()) "Add line items to this record" else "${rows.size} ${if (rows.size == 1) "item" else "items"}", style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
            }
            OutlinedButton(enabled = enabled && childFields.isNotEmpty(), onClick = {
                val defaults = childFields.mapNotNull { f -> f.defaultValue?.let { f.fieldname to parseChildDefault(f.fieldtype, it) } }.toMap()
                onChange(rows + defaults)
                expandedRow = rows.size
            }, shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Outlined.Add, null, modifier = Modifier.size(18.dp))
                Text(" Add row")
            }
        }
        when {
            childType.isBlank() -> EmptyCard("Child type missing", "Set the table field’s child doctype in the website configuration.")
            childType !in state.childSchemas -> Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant.copy(alpha = .5f))) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) { CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp); Text("Loading $childType fields…", color = colors.onSurfaceVariant) }
            }
            childFields.isEmpty() -> EmptyCard("No editable columns", "This child type has no fields available for entry.")
            rows.isEmpty() -> Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant.copy(alpha = .35f))) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(44.dp).background(colors.primary.copy(alpha = .10f), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Inventory2, null, tint = colors.primary) }
                    Text("No ${childType.lowercase()} added", Modifier.padding(top = 12.dp), fontWeight = FontWeight.SemiBold)
                    Text("Add a row to start building this section.", Modifier.padding(top = 4.dp), style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
            else -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                rows.forEachIndexed { index, row ->
                    val primaryField = childFields.firstOrNull { it.fieldtype == "Link" || it.fieldname in setOf("name", "title", "item", "product") } ?: childFields.firstOrNull()
                    val primaryValue = primaryField?.let { row[it.fieldname]?.toString().orEmpty() }.orEmpty()
                    val expanded = expandedRow == index
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow)) {
                        Column(Modifier.fillMaxWidth()) {
                            Row(
                                Modifier.fillMaxWidth().clickable { expandedRow = if (expanded) null else index }.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(primaryValue.ifBlank { "Item ${index + 1}" }, style = androidx.compose.material3.MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    val filledCount = childFields.count { row[it.fieldname]?.toString()?.isNotBlank() == true }
                                    Text(if (expanded) "Editing item ${index + 1}" else "$filledCount details · Tap to edit", style = androidx.compose.material3.MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                                }
                                IconButton(enabled = enabled, onClick = { onChange(rows.filterIndexed { rowIndex, _ -> rowIndex != index }); if (expandedRow == index) expandedRow = null }, modifier = Modifier.size(40.dp)) { Icon(Icons.Outlined.DeleteOutline, "Remove item", tint = colors.error) }
                                Icon(if (expanded) Icons.Filled.ArrowDropDown else Icons.Outlined.ChevronRight, contentDescription = if (expanded) "Collapse item" else "Expand item", tint = colors.onSurfaceVariant)
                            }
                            if (expanded) Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            childFields.forEach { childField ->
                                val childValue = row[childField.fieldname]
                                val childLabel = childField.label + if (childField.reqd) " *" else ""
                                when (childField.fieldtype) {
                                    "Select", "Data" -> {
                                        val choices = childField.options.orEmpty().lineSequence().map(String::trim).filter(String::isNotBlank).distinct().toList()
                                        if (childField.fieldtype == "Select" || (childField.fieldtype == "Data" && choices.size > 1)) ChoiceField(childLabel, childValue?.toString().orEmpty(), "Choose ${childField.label.lowercase()}", choices, !childField.reqd, enabled && !childField.readOnly) { selected -> onChange(rows.mapIndexed { ri, old -> if (ri == index) old + (childField.fieldname to selected.ifBlank { null }) else old }) }
                                        else KoraTextField(childValue?.toString().orEmpty(), { typed -> onChange(rows.mapIndexed { ri, old -> if (ri == index) old + (childField.fieldname to typed) else old }) }, Modifier.fillMaxWidth(), label = childLabel, enabled = enabled && !childField.readOnly, singleLine = true)
                                    }
                                    "Int", "Float", "Currency", "Percent" -> KoraTextField(childValue?.toString().orEmpty(), { typed ->
                                        val parsed: Any? = if (childField.fieldtype == "Int") typed.toLongOrNull() ?: typed else typed.toDoubleOrNull() ?: typed
                                        onChange(rows.mapIndexed { ri, old -> if (ri == index) old + (childField.fieldname to parsed.takeIf { typed.isNotEmpty() }) else old })
                                    }, Modifier.fillMaxWidth(), label = childLabel, enabled = enabled && !childField.readOnly, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = if (childField.fieldtype == "Int") KeyboardType.Number else KeyboardType.Decimal))
                                    "Check" -> Row(Modifier.fillMaxWidth().clickable(enabled = enabled && !childField.readOnly) { onChange(rows.mapIndexed { ri, old -> if (ri == index) old + (childField.fieldname to (old[childField.fieldname] != true)) else old }) }, verticalAlignment = Alignment.CenterVertically) { Checkbox(childValue == true || childValue.toString() == "1", { checked -> onChange(rows.mapIndexed { ri, old -> if (ri == index) old + (childField.fieldname to checked) else old }) }, enabled = enabled && !childField.readOnly); Text(childLabel) }
                                    "Link", "Dynamic Link" -> {
                                        val target = if (childField.fieldtype == "Dynamic Link") row[childField.options.orEmpty()]?.toString().orEmpty() else childField.options.orEmpty()
                                        val linkedOptions = state.linkOptions[target].orEmpty()
                                        val titleKey = state.linkTitleFields[target] ?: "name"
                                        val display = linkedOptions.firstOrNull { it.optString("name") == childValue?.toString() }?.let { it.optString(titleKey).ifBlank { it.optString("name") } } ?: childValue?.toString().orEmpty()
                                        var open by remember(field.fieldname, index, childField.fieldname) { mutableStateOf(false) }
                                        PickerField(childLabel, display, if (target.isBlank()) "Choose a link type first" else "Choose ${childField.label.lowercase()}", enabled && !childField.readOnly && target.isNotBlank()) { open = true; onLoadOptions(target) }
                                        if (open) ModalBottomSheet(onDismissRequest = { open = false }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = colors.surface) {
                                            Column(Modifier.fillMaxWidth().heightIn(max = 600.dp).padding(horizontal = 18.dp)) {
                                                Text("Choose ${childField.label.lowercase()}", style = androidx.compose.material3.MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                                                val opts = linkedOptions
                                                if (target in state.linkLoading) Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                                                else if (state.linkErrors[target] != null) Column(Modifier.fillMaxWidth().height(200.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) { Text("Couldn’t load records", fontWeight = FontWeight.SemiBold); Text(state.linkErrors[target].orEmpty(), color = colors.onSurfaceVariant); TextButton(onClick = { onLoadOptions(target) }) { Text("Try again") } }
                                                else if (opts.isEmpty()) EmptyCard("No records available", "There are no ${target.lowercase()} records to choose from.")
                                                else LazyColumn(Modifier.heightIn(max = 460.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    items(opts, key = { it.optString("name") }) { option ->
                                                        val id = option.optString("name")
                                                        val title = option.optString(titleKey).ifBlank { id }
                                                        TextButton(onClick = { onLinkedChange(childType, childField.fieldname, id); open = false }, modifier = Modifier.fillMaxWidth()) { Column(Modifier.fillMaxWidth()) { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis); if (title != id) Text("Ref · $id", color = colors.onSurfaceVariant, style = androidx.compose.material3.MaterialTheme.typography.labelSmall) } }
                                                    }
                                                }
                                                Spacer(Modifier.height(20.dp))
                                            }
                                        }
                                    }
                                    "Date" -> {
                                        var open by remember(field.fieldname, index, childField.fieldname) { mutableStateOf(false) }
                                        val date = childValue?.toString()?.take(10).orEmpty()
                                        PickerField(childLabel, date, "Choose a date", enabled && !childField.readOnly) { open = true }
                                        if (open) {
                                            val selected = runCatching { LocalDate.parse(date) }.getOrDefault(LocalDate.now())
                                            val dateState = rememberDatePickerState(initialSelectedDateMillis = selected.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
                                            DatePickerDialog(onDismissRequest = { open = false }, confirmButton = { TextButton(onClick = { dateState.selectedDateMillis?.let { millis ->
                                                val formatted = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()
                                                onChange(rows.mapIndexed { ri, old -> if (ri == index) old + (childField.fieldname to formatted) else old })
                                            }; open = false }) { Text("Apply") } }, dismissButton = { TextButton(onClick = { open = false }) { Text("Cancel") } }) { DatePicker(dateState, showModeToggle = true) }
                                        }
                                    }
                                    else -> KoraTextField(childValue?.toString().orEmpty(), { typed -> onChange(rows.mapIndexed { ri, old -> if (ri == index) old + (childField.fieldname to typed) else old }) }, Modifier.fillMaxWidth(), label = childLabel, enabled = enabled && !childField.readOnly, minLines = if (childField.fieldtype in setOf("Text", "Small Text", "Long Text")) 3 else 1)
                                }
                            }
                            }
                        }
                    }
                }
                OutlinedButton(onClick = {
                    val defaults = childFields.mapNotNull { f -> f.defaultValue?.let { f.fieldname to parseChildDefault(f.fieldtype, it) } }.toMap()
                    onChange(rows + defaults)
                    expandedRow = rows.size
                }, enabled = enabled, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Icon(Icons.Outlined.Add, null, modifier = Modifier.size(18.dp)); Text("  Add another item") }
            }
        }
    }
}

internal fun parseChildDefault(type: String, value: String): Any? = when (type) {
    "Check" -> value == "1" || value.equals("true", true)
    "Int" -> value.toLongOrNull() ?: value
    "Float", "Currency", "Percent" -> value.toDoubleOrNull() ?: value
    else -> value
}

internal fun fieldIsVisible(field: FieldSchema, values: Map<String, Any?>): Boolean =
    !field.hidden && evaluateFieldCondition(field.dependsOn, values, true)

internal fun fieldIsRequired(field: FieldSchema, values: Map<String, Any?>): Boolean =
    field.reqd || evaluateFieldCondition(field.mandatoryDependsOn, values, false)

internal fun fieldConstraintHint(field: FieldSchema, value: Any?, values: Map<String, Any?>): String? {
    if (value == null || value == JSONObject.NULL || value.toString().isBlank()) return null
    for (constraint in field.constraints) {
        if (!evaluateFieldCondition(constraint.condition, values, true)) continue
        val numeric = value.toString().toDoubleOrNull()
        val limit = constraint.value?.toDoubleOrNull()
        val failure = when (constraint.type) {
            "min" -> numeric != null && limit != null && numeric < limit
            "max" -> numeric != null && limit != null && numeric > limit
            "min_length" -> limit != null && value.toString().length < limit
            "max_length" -> limit != null && value.toString().length > limit
            "regex" -> constraint.pattern?.let { pattern -> runCatching { !Regex(pattern).containsMatchIn(value.toString()) }.getOrDefault(false) } ?: false
            "one_of" -> constraint.values.isNotEmpty() && value.toString() !in constraint.values
            "not_one_of" -> value.toString() in constraint.values
            else -> false
        }
        if (failure) return constraint.message.ifBlank { "Check this value." }
    }
    return null
}

internal fun evaluateFieldCondition(condition: String?, values: Map<String, Any?>, fallback: Boolean): Boolean {
    if (condition.isNullOrBlank()) return fallback
    return condition.split(',').map(String::trim).filter(String::isNotBlank).all { token ->
        val expr = token.removePrefix("eval:").removePrefix("doc.")
        if (expr.startsWith('!')) return@all !fieldTruthy(values[expr.drop(1)])
        val comparator = listOf("==", "!=", ">=", "<=", ">", "<").firstOrNull(expr::contains) ?: return@all fieldTruthy(values[expr])
        val parts = expr.split(comparator, limit = 2)
        val left = values[parts[0].trim()]
        val right = parts[1].trim().trim('"', '\'')
        val leftNumber = left?.toString()?.toDoubleOrNull()
        val rightNumber = right.toDoubleOrNull()
        val comparison = if (leftNumber != null && rightNumber != null) leftNumber.compareTo(rightNumber) else (left?.toString() ?: "").compareTo(right, ignoreCase = true)
        when (comparator) {
            "==" -> left?.toString() == right
            "!=" -> left?.toString() != right
            ">" -> comparison > 0
            "<" -> comparison < 0
            ">=" -> comparison >= 0
            "<=" -> comparison <= 0
            else -> false
        }
    }
}

internal fun fieldTruthy(value: Any?): Boolean = when (value) {
    null -> false
    is Boolean -> value
    is Number -> value.toDouble() != 0.0
    else -> value.toString().isNotBlank() && value.toString() != "0" && !value.toString().equals("false", true)
}
