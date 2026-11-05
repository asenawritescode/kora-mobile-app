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
