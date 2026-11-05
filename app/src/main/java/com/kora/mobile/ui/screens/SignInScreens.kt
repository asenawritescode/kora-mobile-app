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
internal fun ServerSetup(state: AppState, onNext: (String, String) -> Unit, onConnect: (String, String) -> Unit) {
    var url by remember(state.serverUrl) { mutableStateOf(state.serverUrl) }
    var site by remember(state.siteRef) { mutableStateOf(state.siteRef) }
    val isBusy = state.busy
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val compact = maxHeight < 600.dp
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize().imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            item {
                Column(Modifier.fillMaxWidth().widthIn(max = 480.dp).padding(horizontal = 24.dp, vertical = if (compact) 16.dp else 28.dp)) {
                    BrandMark()
                    Spacer(Modifier.height(if (compact) 18.dp else 30.dp))
                    Text("Connect your workspace", fontSize = if (compact) 25.sp else 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Choose the Kora server where your team works. You can change this before signing in.", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 22.sp)
                    Spacer(Modifier.height(if (compact) 16.dp else 26.dp))
                    KoraTextField(value = url, onValueChange = { value ->
                        url = value
                        Regex("/s/([^/?#]+)/?$").find(value.trim())?.groupValues?.get(1)?.let { site = it }
                    }, modifier = Modifier.fillMaxWidth(), label = "Server URL", placeholder = { Text("https://api.kora.mradiafrica.com") }, singleLine = true,
                        trailingIcon = if (url.isNotBlank()) ({
                            IconButton(onClick = { url = "" }, modifier = Modifier.semantics { contentDescription = "Clear server URL" }) {
                                Icon(Icons.Outlined.DeleteOutline, contentDescription = null)
                            }
                        }) else null)
                    Spacer(Modifier.height(12.dp))
                    KoraTextField(value = site, onValueChange = { site = it }, modifier = Modifier.fillMaxWidth(), label = "Site ID *", placeholder = { Text("Your site ID") }, singleLine = true,
                        trailingIcon = if (site.isNotBlank()) ({
                            IconButton(onClick = { site = "" }, modifier = Modifier.semantics { contentDescription = "Clear site ID" }) {
                                Icon(Icons.Outlined.DeleteOutline, contentDescription = null)
                            }
                        }) else null)
                    Spacer(Modifier.height(12.dp))
                    state.message?.let { MessageBanner(it, state.offline); Spacer(Modifier.height(10.dp)) }
                    Button(onClick = { onConnect(url, site.trim()) }, modifier = Modifier.fillMaxWidth().height(54.dp), enabled = !isBusy && url.isNotBlank() && site.isNotBlank(), shape = RoundedCornerShape(16.dp)) {
                        if (isBusy) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary)
                        else Text("Check server", fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(onClick = { onNext(url, site.trim()) }, modifier = Modifier.fillMaxWidth(), enabled = !isBusy && url.isNotBlank() && site.isNotBlank()) { Text("Continue without checking") }
                    Spacer(Modifier.height(6.dp))
                    Text("Default server · ${com.kora.mobile.BuildConfig.DEFAULT_SERVER_URL}", fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
internal fun LoginScreen(state: AppState, onBack: () -> Unit, onMagicLink: (String) -> Unit, onChangeServer: () -> Unit) {
    var email by remember { mutableStateOf("") }
    androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.fillMaxSize().imePadding(), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp, vertical = 24.dp), verticalArrangement = Arrangement.Center) {
      item {
       Column(Modifier.fillMaxWidth().widthIn(max = 440.dp).padding(horizontal = 0.dp)) {
        BrandMark()
        Spacer(Modifier.height(38.dp))
        Text("Welcome back", fontSize = 29.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text("Sign in to continue to your workspace", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        ServerPill(state.serverUrl, state.siteRef, onChangeServer)
        Spacer(Modifier.height(22.dp))
        KoraTextField(value = email, onValueChange = { email = it }, modifier = Modifier.fillMaxWidth(), label = "Email", singleLine = true)
        Spacer(Modifier.height(12.dp))
        state.message?.let { MessageBanner(it, false); Spacer(Modifier.height(10.dp)) }
        Button(onClick = { onMagicLink(email.trim()) }, modifier = Modifier.fillMaxWidth().height(54.dp), enabled = !state.busy && email.isNotBlank(), shape = RoundedCornerShape(16.dp)) {
            if (state.busy) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary)
            else Text("Email me a sign-in link", fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(8.dp))
        Text("The link opens this workspace on your device and can only be used once.", fontSize = 12.sp, lineHeight = 18.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
       }
      }
    }
}

@Composable
internal fun ServerPill(url: String, site: String, onChange: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.secondary), shape = RoundedCornerShape(14.dp)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Lock, null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f)) {
                Text(url.removePrefix("https://").removePrefix("http://"), fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (site.isNotBlank()) Text("Site · $site", fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = onChange) { Text("Change") }
        }
    }
}
