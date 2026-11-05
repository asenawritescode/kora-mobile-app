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
internal fun BrandMark() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(46.dp).background(androidx.compose.material3.MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
            Text("K", color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column { Text("Kora", fontSize = 23.sp, fontWeight = FontWeight.Bold); Text("WORKSPACE", fontSize = 10.sp, letterSpacing = 1.8.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.primary) }
    }
}

@Composable
internal fun DocTypeRow(item: DocTypeNavItem, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface), border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant.copy(alpha = .7f))) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(38.dp).background(androidx.compose.material3.MaterialTheme.colorScheme.secondary, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Inventory2, null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp)) }
            Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(item.label, fontWeight = FontWeight.Medium); if (item.name != item.label) Text(item.name, fontSize = 11.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) }
            Icon(Icons.Outlined.ChevronRight, null, tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
internal fun ModuleCard(title: String, count: Int, previews: List<String>, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface), border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant.copy(alpha = .7f))) {
        Column(Modifier.padding(17.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).background(androidx.compose.material3.MaterialTheme.colorScheme.secondary, RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Inventory2, null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary) }
                Spacer(Modifier.width(11.dp)); Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.SemiBold); Text("$count published surfaces", fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) }
                Icon(Icons.Outlined.ChevronRight, null, tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (previews.isNotEmpty()) { Spacer(Modifier.height(14.dp)); Divider(color = androidx.compose.material3.MaterialTheme.colorScheme.outline.copy(alpha = .55f)); Spacer(Modifier.height(10.dp)); Text(previews.joinToString("   ·   "), fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        }
    }
}

@Composable
internal fun SectionHeading(title: String, detail: String) {
    Row(Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) { Text(title, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, modifier = Modifier.weight(1f)); Text(detail, fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) }
}

@Composable
internal fun InfoRow(icon: ImageVector, title: String, detail: String) {
    Card(shape = RoundedCornerShape(14.dp)) { Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary); Spacer(Modifier.width(10.dp)); Column { Text(title, fontWeight = FontWeight.Medium); Text(detail, fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) } } }
}

internal fun isPassiveConnectionMessage(message: String): Boolean =
    message.startsWith("Offline ·") || message.contains("waiting to sync", ignoreCase = true)

@Composable
internal fun MessageBanner(message: String, warning: Boolean) {
    val color = if (warning) androidx.compose.material3.MaterialTheme.colorScheme.error else androidx.compose.material3.MaterialTheme.colorScheme.primary
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = .08f)), shape = RoundedCornerShape(13.dp)) { Text(message, Modifier.padding(13.dp), color = color, fontSize = 13.sp) }
}

@Composable
internal fun EmptyCard(title: String, detail: String) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(44.dp).background(androidx.compose.material3.MaterialTheme.colorScheme.secondary, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Inventory2, null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary) }
            Spacer(Modifier.height(10.dp)); Text(title, fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(4.dp)); Text(detail, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, lineHeight = 19.sp)
        }
    }
}
