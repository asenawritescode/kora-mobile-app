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
internal fun DashboardScreen(state: AppState, vm: AppViewModel) {
    var query by remember { mutableStateOf("") }
    val normalizedQuery = query.trim().lowercase()
    val catalog = state.publishedCatalog
    val modules = catalog?.modules.orEmpty().filter { module ->
        normalizedQuery.isBlank() || module.id.lowercase().contains(normalizedQuery) || catalog?.queries?.any { it.ownerModuleId == module.id && it.name.lowercase().contains(normalizedQuery) } == true || catalog?.projections?.any { it.ownerModuleId == module.id && it.id.lowercase().contains(normalizedQuery) } == true
    }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Spacer(Modifier.height(8.dp))
            Text("Good to see you,", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            Text(state.user?.fullName?.ifBlank { state.user.email } ?: "Your workspace", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Pick up where your team left off.", modifier = Modifier.padding(top = 4.dp), color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow)) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("WORKSPACE", color = androidx.compose.material3.MaterialTheme.colorScheme.primary, fontSize = 11.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(7.dp))
                    Text("Your work, ready when you are.", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface, fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(14.dp))
                    Button(onClick = { vm.selectSection("Work") }, shape = RoundedCornerShape(10.dp)) { Text("Browse published data"); Spacer(Modifier.width(8.dp)); Icon(Icons.Outlined.ArrowForward, null, Modifier.size(16.dp)) }
                }
            }
        }
        item {
            KoraTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                placeholder = { Text("Search published modules and queries") }
            )
        }
        item { SectionHeading(if (normalizedQuery.isBlank()) "Your modules" else "Search results", "${modules.size} published modules") }
        if (catalog == null) item { EmptyCard("Published workspace unavailable", state.surfaceError ?: "Connect to load actor-authorized workspace surfaces.") }
        else if (modules.isEmpty()) item { EmptyCard(if (normalizedQuery.isBlank()) "No published modules" else "No matches found", if (normalizedQuery.isBlank()) "No module surfaces are available in this business scope." else "Try another module or query name.") }
        items(modules, key = { it.id }) { module ->
            val queries = catalog?.queries?.filter { it.ownerModuleId == module.id }.orEmpty()
            val projections = catalog?.projections?.filter { it.ownerModuleId == module.id }.orEmpty()
            val operations = catalog?.operations?.filter { it.ownerModuleId == module.id }.orEmpty()
            ModuleCard(module.id, queries.size + projections.size + operations.size, (queries.map { it.presentation.optString("title").ifBlank { it.name } } + projections.map { it.id } + operations.map { it.presentation.optString("title").ifBlank { it.operationRef } }).take(3)) { vm.selectSection("Work") }
        }
        if (normalizedQuery.isNotBlank()) {
            catalog?.queries?.filter { it.name.contains(normalizedQuery, true) || it.ownerModuleId.contains(normalizedQuery, true) }?.forEach { item ->
                item { PublishedSurfaceRow(item.presentation.optString("title").ifBlank { item.name }, "Query · ${item.ownerModuleId}") { vm.openPublishedQuery(item) } }
            }
            catalog?.projections?.filter { it.id.contains(normalizedQuery, true) || it.ownerModuleId.contains(normalizedQuery, true) }?.forEach { item ->
                item { PublishedSurfaceRow(item.id, "Historical view · ${item.ownerModuleId}") { vm.openPublishedProjection(item) } }
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}
