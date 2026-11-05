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
