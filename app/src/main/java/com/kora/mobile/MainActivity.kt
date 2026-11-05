package com.kora.mobile

import android.os.Bundle
import android.app.Activity
import android.content.Intent
import android.content.ClipData
import android.content.ClipboardManager
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.DesktopWindows
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Brightness6
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.ViewInAr
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import androidx.compose.material3.RadioButton
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
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.time.Instant
import java.time.LocalDate
import java.util.Locale
import java.time.ZoneOffset
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.kora.mobile.app.AppStage
import com.kora.mobile.app.AppViewModel
import com.kora.mobile.app.AppState
import com.kora.mobile.app.ConnectionState
import com.kora.mobile.domain.model.Branding
import com.kora.mobile.domain.model.DocTypeNavItem
import com.kora.mobile.domain.model.FieldSchema
import com.kora.mobile.domain.model.ModuleGroup
import com.kora.mobile.domain.model.NavigationConfig
import com.kora.mobile.domain.model.resolveComponentResourceDoctype
import org.json.JSONObject
import org.json.JSONArray
import com.kora.mobile.ui.theme.KoraTheme
import com.kora.mobile.ui.theme.KoraMeasures
import com.kora.mobile.ui.theme.KoraPaletteFamilies
import com.kora.mobile.ui.theme.KoraTokens
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @Suppress("UNCHECKED_CAST")
    private val factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AppViewModel(application) as T
    }
    private val appViewModel: AppViewModel by viewModels { factory }

    override fun onResume() {
        super.onResume()
        appViewModel.onAppResumed()
    }

    override fun onPause() {
        appViewModel.onAppPaused()
        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appViewModel.acceptCloudSignInIntent(intent?.data)
        setContent {
            val vm = appViewModel
            val state by vm.state.collectAsState()
            KoraTheme(
                primaryColor = state.tenantBranding?.primaryColor ?: state.navigation?.branding?.primaryColor,
                accentColor = state.tenantBranding?.accentColor,
                mode = state.themeMode,
            ) {
                val colors = MaterialTheme.colorScheme
                val darkAppearance = colors.background.red < 0.5f
                SideEffect {
                    window.statusBarColor = colors.background.toArgb()
                    window.navigationBarColor = colors.background.toArgb()
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = !darkAppearance
                        isAppearanceLightNavigationBars = !darkAppearance
                    }
                }
                KoraApp(state, vm)
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        appViewModel.acceptCloudSignInIntent(intent.data)
    }
}

@Composable
private fun KoraApp(state: AppState, vm: AppViewModel) {
    Surface(modifier = Modifier.fillMaxSize(), color = androidx.compose.material3.MaterialTheme.colorScheme.background) {
        when (state.stage) {
            AppStage.SERVER -> ServerSetup(state, onNext = vm::goToLogin, onConnect = vm::connect)
            AppStage.LOGIN -> LoginScreen(state, onBack = { vm.logout() }, onRequestMagicLink = vm::requestMagicLink, onChangeServer = { vm.logout() })
            AppStage.WORKSPACE -> WorkspaceShell(state, vm)
        }
    }
}

@Composable
private fun ServerSetup(state: AppState, onNext: (String, String) -> Unit, onConnect: (String, String) -> Unit) {
    var url by remember(state.serverUrl) { mutableStateOf(state.serverUrl) }
    var site by remember(state.siteRef) { mutableStateOf(state.siteRef) }
    val isBusy = state.busy
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val compact = maxHeight < KoraMeasures.dp600
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize().imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            item {
                Column(Modifier.fillMaxWidth().widthIn(max = KoraMeasures.dp480).padding(horizontal = KoraMeasures.dp24, vertical = if (compact) KoraMeasures.dp16 else KoraMeasures.dp28)) {
                    BrandMark()
                    Spacer(Modifier.height(if (compact) KoraMeasures.dp18 else KoraMeasures.dp30))
                    Text("Connect your workspace", fontSize = if (compact) KoraMeasures.sp25 else KoraMeasures.sp28, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(KoraMeasures.dp8))
                    Text("Choose the Kora server where your team works. You can change this before signing in.", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = KoraMeasures.sp22)
                    Spacer(Modifier.height(if (compact) KoraMeasures.dp16 else KoraMeasures.dp26))
                    KoraTextField(value = url, onValueChange = { value ->
                        url = value
                        Regex("/s/([^/?#]+)/?$").find(value.trim())?.groupValues?.get(1)?.let { site = it }
                    }, modifier = Modifier.fillMaxWidth(), label = "Server URL", placeholder = { Text("https://api.kora.mradiafrica.com") }, singleLine = true)
                    Spacer(Modifier.height(KoraMeasures.dp12))
                    KoraTextField(value = site, onValueChange = { site = it }, modifier = Modifier.fillMaxWidth(), label = "Site ID *", placeholder = { Text("Your site ID") }, singleLine = true)
                    Spacer(Modifier.height(KoraMeasures.dp12))
                    state.message?.let { MessageBanner(it, state.offline); Spacer(Modifier.height(KoraMeasures.dp10)) }
                    Button(onClick = { onConnect(url, site.trim()) }, modifier = Modifier.fillMaxWidth().height(KoraMeasures.dp54), enabled = !isBusy && url.isNotBlank() && site.isNotBlank(), shape = RoundedCornerShape(KoraMeasures.dp16)) {
                        if (isBusy) CircularProgressIndicator(modifier = Modifier.size(KoraMeasures.dp20), strokeWidth = KoraMeasures.dp2, color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary)
                        else Text("Check server", fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(onClick = { onNext(url, site.trim()) }, modifier = Modifier.fillMaxWidth(), enabled = !isBusy && url.isNotBlank() && site.isNotBlank()) { Text("Continue without checking") }
                    Spacer(Modifier.height(KoraMeasures.dp6))
                    Text("Default server · ${com.kora.mobile.BuildConfig.DEFAULT_SERVER_URL}", fontSize = KoraMeasures.sp12, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun BrandMark() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(KoraMeasures.dp46).background(androidx.compose.material3.MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
            Text("K", color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary, fontSize = KoraMeasures.sp24, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(KoraMeasures.dp12))
        Column { Text("Kora", fontSize = KoraMeasures.sp23, fontWeight = FontWeight.Bold); Text("WORKSPACE", fontSize = KoraMeasures.sp10, letterSpacing = KoraMeasures.sp1_8, color = androidx.compose.material3.MaterialTheme.colorScheme.primary) }
    }
}

@Composable
private fun LoginScreen(state: AppState, onBack: () -> Unit, onRequestMagicLink: (String, String) -> Unit, onChangeServer: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var cloudUrl by remember(state.serverUrl) {
        mutableStateOf(if (state.serverUrl.contains("localhost") || state.serverUrl.contains("127.0.0.1")) "http://localhost:5176" else "https://cloud.kora.mradiafrica.com")
    }
    androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.fillMaxSize().imePadding(), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = KoraMeasures.dp24, vertical = KoraMeasures.dp24), verticalArrangement = Arrangement.Center) {
      item {
       Column(Modifier.fillMaxWidth().widthIn(max = KoraMeasures.dp440).padding(horizontal = KoraMeasures.dp0)) {
        BrandMark()
        Spacer(Modifier.height(KoraMeasures.dp38))
        Text("Welcome back", fontSize = KoraMeasures.sp29, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(KoraMeasures.dp6))
        Text("Sign in to continue to your workspace", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(KoraMeasures.dp24))
        ServerPill(state.serverUrl, state.siteRef, onChangeServer)
        Spacer(Modifier.height(KoraMeasures.dp22))
        KoraTextField(value = email, onValueChange = { email = it }, modifier = Modifier.fillMaxWidth(), label = "Email", singleLine = true)
        Spacer(Modifier.height(KoraMeasures.dp12))
        KoraTextField(
            value = cloudUrl,
            onValueChange = { cloudUrl = it.trim() },
            modifier = Modifier.fillMaxWidth(),
            label = "Kora Cloud URL",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            singleLine = true,
        )
        Spacer(Modifier.height(KoraMeasures.dp14))
        state.message?.let { MessageBanner(it, state.messageIsError); Spacer(Modifier.height(KoraMeasures.dp10)) }
        Button(onClick = { onRequestMagicLink(email.trim(), cloudUrl.trim()) }, modifier = Modifier.fillMaxWidth().height(KoraMeasures.dp54), enabled = !state.busy && email.isNotBlank() && cloudUrl.isNotBlank(), shape = RoundedCornerShape(KoraMeasures.dp16)) {
            if (state.busy) CircularProgressIndicator(modifier = Modifier.size(KoraMeasures.dp20), strokeWidth = KoraMeasures.dp2, color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary)
            else Text("Email me a sign-in link", fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(KoraMeasures.dp10))
        Text("Open the email on this phone. The verified link will return you to Kora and sign you into this workspace.", fontSize = KoraMeasures.sp12, lineHeight = KoraMeasures.sp18, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
       }
      }
    }
}

@Composable
private fun ServerPill(url: String, site: String, onChange: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.secondary), shape = RoundedCornerShape(KoraMeasures.dp14)) {
        Row(Modifier.fillMaxWidth().padding(KoraMeasures.dp12), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Lock, null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary, modifier = Modifier.size(KoraMeasures.dp18))
            Spacer(Modifier.width(KoraMeasures.dp9))
            Column(Modifier.weight(1f)) {
                Text(url.removePrefix("https://").removePrefix("http://"), fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (site.isNotBlank()) Text("Site · $site", fontSize = KoraMeasures.sp12, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = onChange) { Text("Change") }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
private fun WorkspaceShell(state: AppState, vm: AppViewModel) {
    BackHandler(enabled = state.selectedModule != null && state.selectedDoctype == null && !state.formOpen && state.selectedRecord == null) { vm.closeModule() }
    BackHandler(enabled = state.section == "Settings" && state.settingsPage == "AI agents" && state.selectedAgentPolicy != null) { vm.closeAgentPolicy() }
    BackHandler(enabled = state.section == "Settings" && state.settingsPage != null) { vm.openSettingsPage() }
    val manifestComponents = state.posManifest?.optJSONObject("spec")?.optJSONObject("layout")?.optJSONArray("children")
    val hasProductGrid = manifestComponents?.let { array -> (0 until array.length()).any { array.optJSONObject(it)?.optString("component") == "product_grid" } } == true
    val hasPaymentPanel = manifestComponents?.let { array -> (0 until array.length()).any { array.optJSONObject(it)?.optString("component") == "payment_panel" } } == true
    val hasPos = hasProductGrid && hasPaymentPanel
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var notificationsOpen by remember { mutableStateOf(false) }
    var notificationFilter by rememberSaveable { mutableStateOf("All") }
    var askKoriOpen by rememberSaveable { mutableStateOf(false) }
    var askKoriPrompt by rememberSaveable { mutableStateOf("") }
    BackHandler(enabled = askKoriOpen) { askKoriOpen = false }
    val context = LocalContext.current
    val voiceInputLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val recognized = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
            if (recognized.isNotBlank()) {
                askKoriPrompt = recognized
                askKoriOpen = true
            }
        }
    }
    val startVoiceInput: () -> Unit = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Kori")
        }
        try {
            voiceInputLauncher.launch(intent)
        } catch (_: android.content.ActivityNotFoundException) {
            android.widget.Toast.makeText(context, "Voice input is not available on this device.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    val colors = MaterialTheme.colorScheme
    val sectionTitle = if (state.listSelectionActive) "${state.listSelectionCount} selected" else when {
        state.formOpen -> "${if (state.editingRecord == null) "New" else "Edit"} ${sentenceCaseUiLabel(state.selectedDoctype?.let { it.label.ifBlank { it.name } } ?: "record").replaceFirstChar { it.lowercase(Locale.getDefault()) }}"
        state.section == "Settings" && state.settingsPage == "AI agents" && state.selectedAgentPolicy != null -> "AI agents"
        state.section == "Settings" && state.settingsPage != null -> if (state.settingsPage == "AI connection") "Assistant" else state.settingsPage
        state.section == "Settings" -> "Settings"
        state.selectedRecord != null -> state.selectedDoctype?.let { doctype -> doctype.label.ifBlank { sentenceCaseUiLabel(doctype.name) } } ?: "Record"
        state.selectedDoctype != null -> state.selectedDoctype.label.ifBlank { sentenceCaseUiLabel(state.selectedDoctype.name) }
        state.selectedModule != null -> state.navigation?.modules?.firstOrNull { it.module == state.selectedModule }?.label ?: "Module"
        state.section == "Work" -> "Browse"
        state.section == "Sync" -> "Offline & sync"
        else -> state.section
    }
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !state.formOpen,
        drawerContent = {
            KoraMobileDrawer(
                state = state,
                hasPos = hasPos,
                onClose = { coroutineScope.launch { drawerState.close() } },
                onNavigate = { section ->
                    vm.selectSection(section)
                    coroutineScope.launch { drawerState.close() }
                },
                onOpenDoctype = { item ->
                    vm.openDoctype(item)
                    coroutineScope.launch { drawerState.close() }
                },
                onOpenModule = { module ->
                    vm.openModule(module)
                    coroutineScope.launch { drawerState.close() }
                },
                onLogout = vm::logout,
                onAppearanceChange = vm::setThemePreference,
            )
        },
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.navigationBars,
            topBar = {
              Column {
                TopAppBar(
                    expandedHeight = KoraTokens.TopBar,
                    title = { Text(sectionTitle, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = {
                            when {
                                state.formOpen -> vm.backFromForm()
                                state.selectedRecord != null -> vm.closeRecordDetail()
                                state.section == "Settings" && state.settingsPage == "AI agents" && state.selectedAgentPolicy != null -> vm.closeAgentPolicy()
                                state.section == "Settings" && state.settingsPage != null -> vm.openSettingsPage()
                                else -> coroutineScope.launch { drawerState.open() }
                            }
                        }, modifier = Modifier.size(KoraMeasures.dp48)) {
                            Icon(
                                when {
                                    state.formOpen -> Icons.Outlined.Close
                                    state.selectedRecord != null -> Icons.Outlined.ArrowBack
                                    state.section == "Settings" && state.settingsPage == "AI agents" && state.selectedAgentPolicy != null -> Icons.Outlined.ArrowBack
                                    state.section == "Settings" && state.settingsPage != null -> Icons.Outlined.ArrowBack
                                    else -> Icons.Filled.Menu
                                },
                                when {
                                    state.formOpen -> "Close form"
                                    state.selectedRecord != null -> "Back to records"
                                    state.section == "Settings" && state.settingsPage == "AI agents" && state.selectedAgentPolicy != null -> "Back to agent policies"
                                    state.section == "Settings" && state.settingsPage != null -> "Back to settings"
                                    else -> "Open menu"
                                },
                            )
                        }
                    },
                    actions = {
                        Box(Modifier.clickable { vm.selectSection("Sync") }) { StatusDot(state.connectionState) }
                        Box {
                            BadgedBox(badge = {
                                val unread = state.notifications.count { !it.optBoolean("read") && !it.optBoolean("archived") && !notificationIsSnoozed(it) }
                                if (unread > 0) Badge(
                                    modifier = Modifier.sizeIn(minWidth = KoraMeasures.dp18, minHeight = KoraMeasures.dp18),
                                    containerColor = KoraTokens.Danger,
                                ) {
                                    Text(if (unread > 9) "9+" else unread.toString())
                                }
                            }) {
                                IconButton(onClick = { notificationsOpen = true }, modifier = Modifier.size(KoraTokens.TouchTarget)) {
                                    Icon(Icons.Outlined.Notifications, "Notifications")
                                }
                            }
                        }
                        when {
                            state.listSelectionActive -> Unit
                            state.formOpen -> OutlinedButton(
                                onClick = vm::submitRecord,
                                enabled = !state.busy && formIsValid(state),
                                modifier = Modifier.padding(end = KoraMeasures.dp8).height(KoraTokens.CompactButtonHeight),
                                shape = RoundedCornerShape(KoraTokens.RadiusControl),
                                border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant),
                                contentPadding = PaddingValues(horizontal = KoraMeasures.dp12),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = colors.primary,
                                    disabledContentColor = colors.onSurfaceVariant,
                                ),
                            ) { Text("Save") }
                            state.section == "Notifications" -> {
                                val hasUnread = state.notifications.any { !it.optBoolean("read") && !it.optBoolean("archived") && !notificationIsSnoozed(it) }
                                TextButton(
                                    onClick = vm::markAllNotificationsRead,
                                    enabled = hasUnread,
                                    modifier = Modifier.heightIn(min = KoraMeasures.dp44),
                                ) {
                                    Text(
                                        "Mark all read",
                                        fontSize = KoraMeasures.sp13,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                            state.selectedRecord != null -> {
                                var actionsOpen by remember { mutableStateOf(false) }
                                Box {
                                    IconButton(onClick = { actionsOpen = true }, modifier = Modifier.size(KoraMeasures.dp48)) { Text("···", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
                                    DropdownMenu(expanded = actionsOpen, onDismissRequest = { actionsOpen = false }) {
                                        DropdownMenuItem(text = { Text("Edit record") }, onClick = { actionsOpen = false; state.selectedRecord?.let(vm::editRecord) })
                                        DropdownMenuItem(text = { Text("Ask Kori about this") }, onClick = {
                                            actionsOpen = false
                                            val recordName = state.selectedRecord?.optString(state.titleField).orEmpty()
                                                .ifBlank { state.selectedRecord?.optString("name").orEmpty() }
                                            val typeLabel = state.selectedDoctype?.label ?: "record"
                                            askKoriPrompt = "Tell me about $typeLabel ${recordName.ifBlank { "this record" }}"
                                            askKoriOpen = true
                                        })
                                    }
                                }
                            }
                            else -> Unit
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface),
                )
                Divider(color = colors.outlineVariant)
                if (state.connectionState != ConnectionState.Live) ConnectionBanner(state)
              }
            },
            bottomBar = {
                if (!state.formOpen && !state.listSelectionActive && !askKoriOpen) {
                    Row(
                        // Keep the 64dp navigation surface above Android's gesture area. Without
                        // this inset the raised centre action and its label can be clipped by
                        // the system navigation handle on edge-to-edge devices.
                        Modifier.fillMaxWidth().navigationBarsPadding().height(KoraTokens.BottomBar).background(colors.surface).padding(horizontal = KoraMeasures.dp4),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BottomNavItem("Home", Icons.Outlined.Home, state.section == "Home" && state.selectedDoctype == null && state.selectedModule == null) { vm.selectSection("Home") }
                        if (hasPos) BottomNavItem("POS", Icons.Outlined.DesktopWindows, state.section == "POS") { vm.selectSection("POS") }
                        if (state.koriEnabledInApp) {
                            Box(Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(
                                    modifier = Modifier.combinedClickable(
                                        onClick = { askKoriOpen = true },
                                        onLongClickLabel = "Start voice input",
                                        onLongClick = startVoiceInput,
                                    ),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                ) {
                                    Box(Modifier.size(KoraTokens.AskButton).offset(y = -KoraMeasures.dp14).shadow(KoraMeasures.dp6, CircleShape).background(colors.primary, CircleShape).border(KoraMeasures.dp3, colors.surface, CircleShape), contentAlignment = Alignment.Center) {
                                        KoriMascot(Modifier.size(KoraMeasures.dp34), colors.primary)
                                    }
                                    Text("Ask Kori", Modifier.offset(y = -KoraMeasures.dp10), style = MaterialTheme.typography.labelSmall, color = if (askKoriOpen) colors.onSurface else colors.onSurfaceVariant, maxLines = 1)
                                }
                            }
                        }
                        BottomNavItem("Reports", Icons.Filled.BarChart, state.section == "Reports") { vm.selectSection("Reports") }
                        BottomNavItem("More", Icons.Filled.Menu, state.section in setOf("Work", "Sync", "Requests", "Settings") || state.selectedDoctype != null || state.selectedModule != null) { coroutineScope.launch { drawerState.open() } }
                    }
                }
            },
        ) { padding ->
                    Column(Modifier.padding(padding).fillMaxSize()) {
                when {
                state.formOpen -> RecordForm(state, vm)
                state.selectedRecord != null -> RecordDetailScreen(state, vm)
                    state.selectedDoctype != null -> RecordListScreen(state, vm)
                    state.selectedModule != null -> ModuleHubScreen(state, vm)
                    state.section == "Home" -> DashboardScreen(state, vm)
                    state.section == "Work" -> WorkScreen(state, vm)
                    state.section == "POS" -> PointOfSaleScreen(state, vm)
                    state.section == "Requests" -> RequestsScreen(state)
                    state.section == "Notifications" -> NotificationScreen(state, vm)
                    state.section == "Reports" -> ReportsScreen(state, vm)
                    state.section == "Settings" -> SettingsScreen(state, vm, startVoiceInput)
                    else -> SyncScreen(state, vm)
                }
            }
        }
    }
    if (notificationsOpen) NotificationPopover(
        state = state,
        filter = notificationFilter,
        onFilterChange = { notificationFilter = it },
        onDismiss = { notificationsOpen = false },
        onMarkAllRead = vm::markAllNotificationsRead,
        onOpen = { notification -> notificationsOpen = false; vm.openNotification(notification) },
        onViewAll = { notificationsOpen = false; vm.selectSection("Notifications") },
    )
    if (askKoriOpen) AskKoriSheet(state, vm, initialPrompt = askKoriPrompt, onDismiss = { askKoriOpen = false })
    state.selectedNotification?.let { notification -> NotificationDialog(notification, state.navigation, vm) }
    state.notificationToast?.let { notification ->
        Surface(Modifier.fillMaxWidth().padding(top = KoraMeasures.dp8, start = KoraMeasures.dp8, end = KoraMeasures.dp8).clickable { vm.openNotification(notification); vm.dismissNotificationToast() }, color = colors.inverseSurface, contentColor = colors.inverseOnSurface, shape = RoundedCornerShape(KoraMeasures.dp12), shadowElevation = KoraMeasures.dp8) {
            Row(Modifier.fillMaxWidth().padding(horizontal = KoraMeasures.dp12, vertical = KoraMeasures.dp10), horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp10), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                        Text(notification.optString("title").ifBlank { "New notification" }, Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        val relativeTime = notificationSummaryTime(notification.optString("created_at"))
                        if (relativeTime.isNotBlank()) {
                            Text(
                                relativeTime,
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.inverseOnSurface.copy(alpha = .7f),
                                maxLines = 1,
                            )
                        }
                    }
                    Text(notification.optString("message"), style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                IconButton(onClick = vm::dismissNotificationToast, modifier = Modifier.size(KoraMeasures.dp36)) { Icon(Icons.Outlined.Close, "Dismiss notification") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KoraMobileDrawer(
    state: AppState,
    hasPos: Boolean,
    onClose: () -> Unit,
    onNavigate: (String) -> Unit,
    onOpenDoctype: (DocTypeNavItem) -> Unit,
    onOpenModule: (String) -> Unit,
    onLogout: () -> Unit,
    onAppearanceChange: (String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    var search by rememberSaveable { mutableStateOf("") }
    val modules = state.navigation?.modules.orEmpty()
    val destinations = buildList {
        add(DrawerTarget("section:Home", "Home", Icons.Filled.Home, section = "Home"))
        if (hasPos) add(DrawerTarget("section:POS", "POS", Icons.Filled.ShoppingCart, section = "POS"))
        add(DrawerTarget("section:Requests", "Requests", Icons.Filled.ListAlt, section = "Requests"))
        add(DrawerTarget("section:Reports", "Reports", Icons.Filled.BarChart, section = "Reports"))
        add(DrawerTarget("section:Sync", "Offline & sync", Icons.Filled.Sync, section = "Sync"))
        modules.forEach { module ->
            val fallbackIcon = moduleIcon(module.icon)
            add(DrawerTarget("module:${module.module}", module.label, fallbackIcon, moduleKey = module.module))
            module.doctypes.filterNot { it.isChild }.forEach { item ->
                add(DrawerTarget("doctype:${item.resourceName}", item.label, moduleIcon(item.icon ?: module.icon), doctype = item))
            }
        }
    }
    val destinationsById = destinations.associateBy { it.id }
    val navigationPrefs = context.getSharedPreferences("kora_mobile_navigation", android.content.Context.MODE_PRIVATE)
    val pinKey = "pinned:${state.siteRef}"
    val recentKey = "recent:${state.siteRef}"
    val defaultPins = listOf("section:Home").filter { id -> destinationsById.containsKey(id) }
    val destinationIds = destinationsById.keys
    var pinnedIds by remember(state.siteRef, destinationIds) {
        val stored = navigationPrefs.getString(pinKey, null)
        mutableStateOf(stored?.split('\n')?.filter { it in destinationsById }?.take(3) ?: defaultPins)
    }
    var recentIds by remember(state.siteRef, destinationIds) {
        mutableStateOf(navigationPrefs.getString(recentKey, "").orEmpty().split('\n').filter { it in destinationsById }.take(3))
    }
    var pinEditorOpen by remember { mutableStateOf(false) }
    val matches = { label: String -> search.isBlank() || label.contains(search, ignoreCase = true) }
    // Keep one module open at a time so a large catalog stays navigable.
    var expandedModule by rememberSaveable { mutableStateOf<String?>(null) }
    var showAllModules by rememberSaveable { mutableStateOf(false) }
    val activeDestinationId = state.selectedDoctype?.let { "doctype:${it.resourceName}" }
        ?: state.selectedModule?.let { "module:$it" }
        ?: "section:${state.section}"
    LaunchedEffect(activeDestinationId, state.siteRef) {
        if (activeDestinationId in destinationsById) {
            recentIds = (listOf(activeDestinationId) + recentIds.filterNot { it == activeDestinationId }).take(3)
            navigationPrefs.edit().putString(recentKey, recentIds.joinToString("\n")).apply()
        }
    }
    fun openDestination(target: DrawerTarget) {
        when {
            target.section != null -> onNavigate(target.section)
            target.doctype != null -> onOpenDoctype(target.doctype)
            target.moduleKey != null -> onOpenModule(target.moduleKey)
        }
    }
    ModalDrawerSheet(
        modifier = Modifier.width(KoraMeasures.dp312).fillMaxHeight(),
        drawerContainerColor = colors.surface,
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(start = KoraMeasures.dp20, end = KoraMeasures.dp8, top = KoraMeasures.dp12, bottom = KoraMeasures.dp8), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(state.navigation?.branding?.appName ?: "Kora", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                IconButton(onClick = onClose, modifier = Modifier.size(KoraMeasures.dp48)) { Icon(Icons.Outlined.Close, "Close menu") }
            }
            KoraTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = KoraMeasures.dp16, vertical = KoraMeasures.dp8),
                singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                placeholder = { Text("Find a module or record") },
            )
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = KoraMeasures.dp10)) {
                Row(Modifier.fillMaxWidth().padding(start = KoraMeasures.dp14, top = KoraMeasures.dp12, end = KoraMeasures.dp4, bottom = KoraMeasures.dp4), verticalAlignment = Alignment.CenterVertically) {
                    Text("Pinned", Modifier.weight(1f), color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
                    TextButton(onClick = { pinEditorOpen = true }, modifier = Modifier.heightIn(min = KoraMeasures.dp40)) { Text("Edit", color = colors.primary) }
                }
                pinnedIds.mapNotNull(destinationsById::get).filter { matches(it.label) }.forEach { target ->
                    // Home has no separate canonical row in the drawer. Other pinned
                    // destinations also appear in Work, Insights, System, or Data, so
                    // keep their active treatment on that canonical row only.
                    KoraDrawerItem(target.label, Icons.Outlined.Star, target.id == activeDestinationId && target.id == "section:Home") { openDestination(target) }
                }
                if (pinnedIds.isEmpty()) Text("Pin up to three destinations for quick access.", Modifier.padding(start = KoraMeasures.dp16, top = KoraMeasures.dp4, bottom = KoraMeasures.dp8), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                val recentTargets = recentIds.mapNotNull(destinationsById::get).filter { it.id !in pinnedIds && it.id != activeDestinationId && matches(it.label) }
                if (recentTargets.isNotEmpty()) {
                    DrawerGroupLabel("Recent")
                    recentTargets.forEach { target ->
                        KoraDrawerItem(target.label, target.icon, target.id == activeDestinationId) { openDestination(target) }
                    }
                }
                if (matches("Requests")) KoraDrawerItem("Requests", Icons.Filled.ListAlt, state.section == "Requests") { onNavigate("Requests") }
                DrawerGroupLabel("Work")
                if (hasPos && matches("POS")) KoraDrawerItem("POS", Icons.Filled.ShoppingCart, state.section == "POS") { onNavigate("POS") }
                val totalRecordTypes = modules.sumOf { module -> module.doctypes.count { !it.isChild } }
                DrawerGroupLabel("Data · $totalRecordTypes")
                val visibleModules = modules.filter { module -> module.doctypes.any { !it.isChild && matches(it.label) } || matches(module.label) }
                val displayedModules = if (showAllModules || search.isNotBlank()) visibleModules else visibleModules.take(8)
                if (totalRecordTypes <= 12) {
                    visibleModules.flatMap { module ->
                        module.doctypes.filterNot { it.isChild }
                            .filter { matches(it.label) || matches(module.label) }
                            .map { module to it }
                    }.forEach { (module, item) ->
                        KoraDrawerItem(item.label, item.icon?.let(::moduleIcon) ?: Icons.Outlined.ViewInAr, state.selectedDoctype?.resourceName == item.resourceName) { onOpenDoctype(item) }
                    }
                } else displayedModules.forEach { module ->
                    val visibleTypes = module.doctypes.filterNot { it.isChild }.filter { matches(it.label) || matches(module.label) }
                    if (visibleTypes.isNotEmpty()) {
                        val activeInModule = state.selectedDoctype?.resourceName in module.doctypes.map { it.resourceName }
                        val expanded = search.isNotBlank() || expandedModule == module.module || activeInModule
                        if (visibleTypes.size == 1) {
                            KoraDrawerItem(visibleTypes.first().label, visibleTypes.first().icon?.let(::moduleIcon) ?: Icons.Outlined.ViewInAr, state.selectedDoctype?.resourceName == visibleTypes.first().resourceName) { onOpenDoctype(visibleTypes.first()) }
                        } else {
                            TextButton(
                                onClick = {
                                    expandedModule = if (expanded && !activeInModule) null else module.module
                                },
                                contentPadding = PaddingValues(horizontal = KoraMeasures.dp14, vertical = KoraMeasures.dp4),
                                modifier = Modifier.fillMaxWidth().heightIn(min = KoraMeasures.dp44),
                            ) {
                                Text("${module.label} · ${module.doctypes.count { !it.isChild }}", Modifier.weight(1f), color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, textAlign = androidx.compose.ui.text.style.TextAlign.Start)
                                Icon(if (expanded) Icons.Filled.ArrowDropDown else Icons.Outlined.ChevronRight, contentDescription = if (expanded) "Collapse ${module.label}" else "Expand ${module.label}", tint = colors.onSurfaceVariant)
                            }
                            if (expanded) {
                                val shownTypes = if (expandedModule == module.module || search.isNotBlank()) visibleTypes else visibleTypes.take(3)
                                shownTypes.forEach { item ->
                                    KoraDrawerItem(item.label, item.icon?.let(::moduleIcon) ?: Icons.Outlined.ViewInAr, state.selectedDoctype?.resourceName == item.resourceName, Modifier.padding(start = KoraMeasures.dp8)) { onOpenDoctype(item) }
                                }
                                val hiddenCount = visibleTypes.size - shownTypes.size
                                if (hiddenCount > 0) TextButton(onClick = { expandedModule = module.module }, modifier = Modifier.fillMaxWidth().heightIn(min = KoraMeasures.dp44)) { Text("Show $hiddenCount more", color = colors.primary) }
                                else if (expandedModule == module.module && visibleTypes.size > 3) TextButton(onClick = { expandedModule = null }, modifier = Modifier.fillMaxWidth().heightIn(min = KoraMeasures.dp40)) { Text("Show fewer", color = colors.primary) }
                            }
                        }
                    }
                }
                if (visibleModules.size > 8 && search.isBlank()) {
                    TextButton(onClick = { showAllModules = !showAllModules }, modifier = Modifier.fillMaxWidth().heightIn(min = KoraMeasures.dp44)) {
                        Text(if (showAllModules) "Show fewer modules" else "More modules · ${visibleModules.size - 8}", color = colors.primary)
                    }
                }
                DrawerGroupLabel("Insights")
                if (matches("Reports")) KoraDrawerItem("Reports", Icons.Filled.BarChart, state.section == "Reports") { onNavigate("Reports") }
                DrawerGroupLabel("System")
                if (matches("Settings")) KoraDrawerItem("Settings", Icons.Filled.Settings, state.section == "Settings") { onNavigate("Settings") }
                if (matches("Offline sync")) KoraDrawerItem("Offline & sync", Icons.Filled.Sync, state.section == "Sync") { onNavigate("Sync") }
            }
            Divider(color = colors.outlineVariant)
            var appearanceMenuOpen by remember { mutableStateOf(false) }
            Row(Modifier.fillMaxWidth().padding(horizontal = KoraMeasures.dp16, vertical = KoraMeasures.dp8), verticalAlignment = Alignment.CenterVertically) {
                Surface(Modifier.size(KoraMeasures.dp36), shape = CircleShape, color = colors.secondaryContainer) {
                    Box(contentAlignment = Alignment.Center) { Text(state.user?.fullName?.firstOrNull()?.uppercase() ?: "K", fontWeight = FontWeight.Bold, color = colors.onSecondaryContainer) }
                }
                Column(Modifier.weight(1f).padding(start = KoraMeasures.dp10)) {
                    Text(state.user?.fullName?.ifBlank { state.user.email } ?: "Signed in", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    TextButton(onClick = onLogout, modifier = Modifier.heightIn(min = KoraTokens.TouchTarget), contentPadding = PaddingValues(horizontal = KoraMeasures.dp0, vertical = KoraMeasures.dp0)) {
                        Text("Sign out", style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                    }
                }
                Box {
                    IconButton(onClick = { appearanceMenuOpen = true }, modifier = Modifier.size(KoraMeasures.dp44)) {
                        Icon(Icons.Outlined.Brightness6, contentDescription = "Appearance: ${state.themePreference}")
                    }
                    DropdownMenu(expanded = appearanceMenuOpen, onDismissRequest = { appearanceMenuOpen = false }) {
                        DropdownMenuItem(text = { Text("Light") }, onClick = { onAppearanceChange("light"); appearanceMenuOpen = false })
                        DropdownMenuItem(text = { Text("Dark") }, onClick = { onAppearanceChange("dark"); appearanceMenuOpen = false })
                        DropdownMenuItem(text = { Text("Use site default") }, onClick = { onAppearanceChange("site"); appearanceMenuOpen = false })
                    }
                }
            }
        }
    }
    if (pinEditorOpen) ModalBottomSheet(
        onDismissRequest = { pinEditorOpen = false },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface,
    ) {
        Column(Modifier.fillMaxWidth().heightIn(max = KoraMeasures.dp680).padding(horizontal = KoraMeasures.dp16)) {
            Text("Pinned destinations", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("Choose up to three", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, modifier = Modifier.padding(top = KoraMeasures.dp4, bottom = KoraMeasures.dp12))
            LazyColumn(Modifier.weight(1f, fill = false)) {
                items(destinations, key = { it.id }) { target ->
                    val selected = target.id in pinnedIds
                    Row(Modifier.fillMaxWidth().heightIn(min = KoraMeasures.dp48).clickable {
                        if (selected) pinnedIds = pinnedIds - target.id
                        else if (pinnedIds.size < 3) pinnedIds = pinnedIds + target.id
                        navigationPrefs.edit().putString(pinKey, pinnedIds.joinToString("\n")).apply()
                    }, verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = selected, onCheckedChange = { checked ->
                            if (checked && pinnedIds.size < 3) pinnedIds = pinnedIds + target.id
                            else if (!checked) pinnedIds = pinnedIds - target.id
                            navigationPrefs.edit().putString(pinKey, pinnedIds.joinToString("\n")).apply()
                        })
                        Icon(target.icon, null, tint = colors.onSurfaceVariant, modifier = Modifier.padding(horizontal = KoraMeasures.dp8))
                        Text(target.label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Button(onClick = { pinEditorOpen = false }, modifier = Modifier.fillMaxWidth().padding(vertical = KoraMeasures.dp12).heightIn(min = KoraMeasures.dp48)) { Text("Done") }
        }
    }
}

private data class DrawerTarget(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val section: String? = null,
    val doctype: DocTypeNavItem? = null,
    val moduleKey: String? = null,
)

@Composable
private fun DrawerGroupLabel(label: String) {
    Text(label, Modifier.fillMaxWidth().padding(start = KoraMeasures.dp16, top = KoraMeasures.dp14, bottom = KoraMeasures.dp4), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
}

@Composable
private fun KoraDrawerItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(KoraTokens.RadiusControl)
    val rowWidth = if (selected) {
        Modifier.widthIn(max = KoraMeasures.dp312 - KoraMeasures.dp20).wrapContentWidth(Alignment.Start)
    } else {
        Modifier.fillMaxWidth()
    }
    Box(Modifier.fillMaxWidth().then(modifier)) {
        Row(
            Modifier
                .then(rowWidth)
                .heightIn(min = KoraMeasures.dp48)
                .clip(shape)
                .background(if (selected) colors.secondaryContainer else Color.Transparent)
                .selectable(selected = selected, role = Role.Button, onClick = onClick)
                .padding(horizontal = KoraMeasures.dp16, vertical = KoraMeasures.dp10),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp12),
        ) {
            Icon(icon, contentDescription = null, tint = if (selected) colors.onSecondaryContainer else colors.onSurfaceVariant, modifier = Modifier.size(KoraMeasures.dp24))
            Text(
                label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                color = if (selected) colors.onSecondaryContainer else colors.onSurface,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun RowScope.BottomNavItem(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(
        Modifier.weight(1f).fillMaxSize().clickable(onClick = onClick).padding(top = KoraMeasures.dp6, bottom = KoraMeasures.dp2),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(KoraMeasures.dp22), tint = if (selected) colors.onSurface else colors.onSurfaceVariant)
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) colors.onSurface else colors.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

@Composable
private fun KoraChip(label: String, selected: Boolean, leadingContent: (@Composable () -> Unit)? = null, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        shape = CircleShape,
        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        modifier = Modifier.heightIn(min = KoraTokens.ChipHeight),
        leadingIcon = leadingContent,
        border = androidx.compose.material3.FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            selectedBorderColor = MaterialTheme.colorScheme.primary,
        ),
        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
        ),
    )
}

@Composable
private fun KoraSegmentedControl(
    options: List<Pair<String, String>>,
    selected: String,
    modifier: Modifier = Modifier,
    height: Dp = KoraMeasures.dp46,
    onSelect: (String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val outerShape = RoundedCornerShape(KoraTokens.RadiusControl)
    Surface(
        modifier = modifier.fillMaxWidth().height(height),
        shape = outerShape,
        color = colors.surface,
        border = androidx.compose.foundation.BorderStroke(KoraTokens.Hairline, colors.outline),
    ) {
        Row(Modifier.fillMaxSize().padding(KoraTokens.Hairline)) {
            options.forEachIndexed { index, (value, label) ->
                val first = index == 0
                val last = index == options.lastIndex
                val segmentShape = RoundedCornerShape(
                    topStart = if (first) KoraTokens.RadiusControl - KoraMeasures.dp1 else KoraMeasures.dp0,
                    bottomStart = if (first) KoraTokens.RadiusControl - KoraMeasures.dp1 else KoraMeasures.dp0,
                    topEnd = if (last) KoraTokens.RadiusControl - KoraMeasures.dp1 else KoraMeasures.dp0,
                    bottomEnd = if (last) KoraTokens.RadiusControl - KoraMeasures.dp1 else KoraMeasures.dp0,
                )
                val isSelected = value == selected
                Surface(
                    onClick = { onSelect(value) },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    shape = segmentShape,
                    color = if (isSelected) colors.secondaryContainer else colors.surface,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) colors.onSecondaryContainer else colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusDot(connection: ConnectionState) {
    val colors = MaterialTheme.colorScheme
    val isWarning = connection is ConnectionState.Offline || connection is ConnectionState.Reconnecting
    val color = if (isWarning) warningForeground(colors) else successForeground(colors)
    val description = when (connection) {
        ConnectionState.Live -> "Connected"
        ConnectionState.Reconnecting -> "Reconnecting"
        is ConnectionState.Offline -> "Offline"
        is ConnectionState.Syncing -> "Syncing changes"
    }
    Box(
        Modifier.size(KoraTokens.TouchTarget).semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(KoraTokens.StatusHalo).background(color.copy(alpha = .20f), CircleShape))
        Box(Modifier.size(KoraTokens.StatusDot).background(color, CircleShape))
    }
}

@Composable
private fun ConnectionBanner(state: AppState) {
    val colors = MaterialTheme.colorScheme
    val warning = warningForeground(colors)
    val connection = state.connectionState
    val isOffline = connection is ConnectionState.Offline
    val isReconnecting = connection is ConnectionState.Reconnecting
    val isSyncing = connection is ConnectionState.Syncing
    val isTransient = isReconnecting || isSyncing
    val pending = when (connection) {
        is ConnectionState.Offline -> connection.pendingChanges
        is ConnectionState.Syncing -> connection.remaining
        ConnectionState.Reconnecting -> state.queuedMutations.size
        ConnectionState.Live -> 0
    }
    Surface(
        Modifier.fillMaxWidth().padding(horizontal = KoraTokens.SpaceS, vertical = KoraTokens.SpaceXs),
        color = if (isOffline) warningContainer(colors) else colors.surface,
        shape = RoundedCornerShape(KoraTokens.RadiusControl),
        border = androidx.compose.foundation.BorderStroke(
            KoraMeasures.dp1,
            if (isOffline) warningBorder(colors) else colors.outlineVariant,
        ),
    ) {
        Column {
            Row(Modifier.fillMaxWidth().padding(horizontal = KoraTokens.SpaceM, vertical = KoraTokens.SpaceS), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(KoraTokens.SpaceS)) {
                if (isOffline) {
                    KoriMascot(Modifier.size(KoraMeasures.dp44), warning, KoriMood.Offline)
                } else {
                    val statusColor = if (isTransient) warning else successForeground(colors)
                    Box(Modifier.size(KoraTokens.StatusHalo), contentAlignment = Alignment.Center) {
                        Box(Modifier.size(KoraTokens.StatusHalo).background(statusColor.copy(alpha = .20f), CircleShape))
                        Box(Modifier.size(KoraTokens.StatusDot).background(statusColor, CircleShape))
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp1)) {
                    Text(when (connection) {
                        is ConnectionState.Syncing -> "Syncing changes"
                        ConnectionState.Reconnecting -> "Reconnecting"
                        is ConnectionState.Offline -> "You are offline"
                        ConnectionState.Live -> "Connected"
                    }, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = if (isOffline) warning else colors.onSurface)
                    Text(
                        if (isSyncing) "Syncing $pending ${if (pending == 1) "change" else "changes"}…"
                        else if (isReconnecting) "Trying to reconnect to your workspace…"
                        else if (pending > 0) "$pending ${if (pending == 1) "change is" else "changes are"} saved on this device and will sync when you reconnect."
                        else "You can keep searching and viewing cached records. Changes will sync when you reconnect.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface,
                    )
                }
            }
            if (isTransient) LinearProgressIndicator(
                Modifier.fillMaxWidth().height(KoraMeasures.dp4),
                color = warning,
                trackColor = colors.outlineVariant.copy(alpha = .35f),
            )
        }
    }
}

private enum class KoriMood { Ready, Thinking, Offline, Alert }

@Composable
private fun KoriMascot(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary, mood: KoriMood = KoriMood.Ready) {
    Canvas(modifier) {
        val unit = size.minDimension / 64f
        val left = (size.width - 64f * unit) / 2f
        val top = (size.height - 64f * unit) / 2f
        val accent = when (mood) {
            KoriMood.Ready, KoriMood.Thinking -> KoraTokens.MascotAntennaReady
            KoriMood.Offline -> KoraTokens.MascotOffline
            KoriMood.Alert -> KoraTokens.Danger
        }
        val bodyTopLeft = androidx.compose.ui.geometry.Offset(left + 8f * unit, top + 16f * unit)
        val bodySize = androidx.compose.ui.geometry.Size(48f * unit, 40f * unit)
        drawRoundRect(
            color = KoraTokens.Surface,
            topLeft = bodyTopLeft,
            size = bodySize,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f * unit),
        )
        drawRoundRect(color, bodyTopLeft, bodySize, androidx.compose.ui.geometry.CornerRadius(18f * unit), style = androidx.compose.ui.graphics.drawscope.Stroke(3f * unit))
        drawLine(accent, androidx.compose.ui.geometry.Offset(left + 32f * unit, top + 16f * unit), androidx.compose.ui.geometry.Offset(left + 32f * unit, top + 9f * unit), 3f * unit, cap = StrokeCap.Round)
        drawCircle(accent, 4f * unit, androidx.compose.ui.geometry.Offset(left + 32f * unit, top + 6f * unit))
        if (mood == KoriMood.Offline) {
            drawLine(color, androidx.compose.ui.geometry.Offset(left + 21f * unit, top + 34f * unit), androidx.compose.ui.geometry.Offset(left + 27f * unit, top + 34f * unit), 2f * unit, cap = StrokeCap.Round)
            drawLine(color, androidx.compose.ui.geometry.Offset(left + 37f * unit, top + 34f * unit), androidx.compose.ui.geometry.Offset(left + 43f * unit, top + 34f * unit), 2f * unit, cap = StrokeCap.Round)
        } else {
            drawCircle(color, if (mood == KoriMood.Alert) 5f * unit else 4f * unit, androidx.compose.ui.geometry.Offset(left + 24f * unit, top + 34f * unit))
            drawCircle(
                color,
                when (mood) {
                    KoriMood.Alert -> 5f * unit
                    KoriMood.Thinking -> 2.5f * unit
                    else -> 4f * unit
                },
                androidx.compose.ui.geometry.Offset(left + 40f * unit, top + (if (mood == KoriMood.Thinking) 31f else 34f) * unit),
            )
        }
        when (mood) {
            KoriMood.Alert -> drawCircle(color, 3.5f * unit, androidx.compose.ui.geometry.Offset(left + 32f * unit, top + 45f * unit))
            KoriMood.Offline, KoriMood.Thinking -> drawLine(color, androidx.compose.ui.geometry.Offset(left + 27f * unit, top + 45f * unit), androidx.compose.ui.geometry.Offset(left + 37f * unit, top + 45f * unit), 2f * unit, cap = StrokeCap.Round)
            KoriMood.Ready -> {
                val mouth = Path().apply {
                    moveTo(left + 26f * unit, top + 44f * unit)
                    quadraticTo(left + 32f * unit, top + 49f * unit, left + 38f * unit, top + 44f * unit)
                }
                drawPath(mouth, color, style = androidx.compose.ui.graphics.drawscope.Stroke(3f * unit, cap = StrokeCap.Round))
            }
        }
        drawCircle(KoraTokens.MascotCheek, 3f * unit, androidx.compose.ui.geometry.Offset(left + 16f * unit, top + 43f * unit))
        drawCircle(KoraTokens.MascotCheek, 3f * unit, androidx.compose.ui.geometry.Offset(left + 48f * unit, top + 43f * unit))
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
private fun AskKoriSheet(state: AppState, vm: AppViewModel, initialPrompt: String = "", onDismiss: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    var draft by rememberSaveable(initialPrompt) { mutableStateOf(initialPrompt) }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val selectedRecord = state.selectedRecord
    val recordLabel = state.selectedDoctype?.label?.lowercase(Locale.getDefault()) ?: "record"
    val recordTitle = selectedRecord?.optString(state.titleField).orEmpty()
        .ifBlank { selectedRecord?.optString("name").orEmpty() }
    val suggestions = when {
        selectedRecord != null -> listOf(
            "Summarize this record" to "Summarize $recordLabel ${recordTitle.ifBlank { "record" }}.",
            "Find related records" to "Find records related to this $recordLabel.",
        )
        state.selectedDoctype != null -> listOf(
            "Review this type" to "Review the current $recordLabel records and suggest what needs attention.",
            "Create a record" to "Help me create a new $recordLabel record.",
            "Search this type" to "Help me find $recordLabel records that match my needs.",
        )
        else -> listOf(
            "Summarize this page" to "Summarize what I should focus on here.",
            "Find records" to "Help me find the records I need.",
            "What needs attention?" to "What needs my attention?",
        )
    }
    LaunchedEffect(state.assistantTurns.size, state.assistantBusy) {
        val last = state.assistantTurns.lastIndex
        if (last >= 0) listState.animateScrollToItem(last)
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface,
        dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle(color = colors.outlineVariant) },
    ) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(.86f).imePadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = KoraTokens.SpaceL, vertical = KoraTokens.SpaceM), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(KoraTokens.SpaceM)) {
                KoriMascot(Modifier.size(KoraMeasures.dp40), colors.primary)
                Column(Modifier.weight(1f)) {
                    Text("Ask Kori", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp6)) {
                        Box(Modifier.size(KoraMeasures.dp16), contentAlignment = Alignment.Center) {
                            val statusColor = if (state.offline) warningForeground(colors) else successForeground(colors)
                            Box(Modifier.size(KoraMeasures.dp16).background(statusColor.copy(alpha = .20f), CircleShape))
                            Box(Modifier.size(KoraMeasures.dp10).background(statusColor, CircleShape))
                        }
                        Text(
                            if (state.offline) "Offline · reconnect to continue" else "Connected · uses your role access",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (state.offline) warningForeground(colors) else successForeground(colors),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(KoraTokens.TouchTarget).testTag("ask-kori-close").semantics { testTagsAsResourceId = true },
                ) { Icon(Icons.Outlined.Close, "Close Ask Kori") }
            }
            Divider(color = colors.outlineVariant)
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = KoraTokens.SpaceL, vertical = KoraTokens.SpaceL),
                verticalArrangement = Arrangement.spacedBy(KoraTokens.SpaceM),
            ) {
                if (state.assistantTurns.isEmpty()) item {
                    Surface(
                        Modifier.fillMaxWidth(.9f),
                        color = colors.surfaceContainerLow,
                        shape = RoundedCornerShape(KoraMeasures.dp14),
                        border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant),
                    ) {
                        Text(
                            "Ask about the records and workspace information available to your account.",
                            Modifier.padding(horizontal = KoraTokens.SpaceM, vertical = KoraMeasures.dp10),
                            color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                items(state.assistantTurns.size) { index ->
                    val turn = state.assistantTurns[index]
                    val user = turn.role == "user"
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (user) Arrangement.End else Arrangement.Start) {
                        Column(Modifier.fillMaxWidth(.82f), verticalArrangement = Arrangement.spacedBy(KoraTokens.SpaceS)) {
                            Surface(
                                Modifier.fillMaxWidth(),
                                color = if (user) colors.primary else colors.surfaceContainerLow,
                                shape = RoundedCornerShape(KoraMeasures.dp14),
                                border = if (user) null else androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant),
                            ) {
                                Text(turn.content, Modifier.padding(horizontal = KoraTokens.SpaceM, vertical = KoraMeasures.dp10), color = if (user) colors.onPrimary else colors.onSurface, style = MaterialTheme.typography.bodyMedium)
                            }
                            if (!user) Row(
                                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(KoraTokens.SpaceS),
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Activity.CLIPBOARD_SERVICE) as? ClipboardManager
                                        clipboard?.setPrimaryClip(ClipData.newPlainText("Kori response", turn.content))
                                    },
                                    modifier = Modifier.heightIn(min = KoraTokens.ChipHeight),
                                    contentPadding = PaddingValues(horizontal = KoraTokens.SpaceM, vertical = KoraTokens.SpaceXs),
                                    shape = CircleShape,
                                ) { Text("Copy answer", style = MaterialTheme.typography.labelMedium) }
                                OutlinedButton(
                                    onClick = { vm.sendAssistantMessage("Can you explain that in more detail?") },
                                    enabled = !state.assistantBusy && !state.offline,
                                    modifier = Modifier.heightIn(min = KoraTokens.ChipHeight),
                                    contentPadding = PaddingValues(horizontal = KoraTokens.SpaceM, vertical = KoraTokens.SpaceXs),
                                    shape = CircleShape,
                                ) { Text("Ask a follow-up", style = MaterialTheme.typography.labelMedium) }
                            }
                        }
                    }
                }
                if (state.assistantBusy) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
                state.assistantError?.let { error -> item { MessageBanner(error, true) } }
            }
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = KoraTokens.SpaceL, vertical = KoraTokens.SpaceXs),
                horizontalArrangement = Arrangement.spacedBy(KoraTokens.SpaceS),
            ) {
                suggestions.forEach { (label, prompt) ->
                    OutlinedButton(
                        onClick = { vm.sendAssistantMessage(prompt) },
                        enabled = !state.assistantBusy && !state.offline,
                        modifier = Modifier.heightIn(min = KoraTokens.ChipHeight),
                        contentPadding = PaddingValues(horizontal = KoraTokens.SpaceM, vertical = KoraTokens.SpaceXs),
                        shape = CircleShape,
                    ) { Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1) }
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = KoraTokens.SpaceL, vertical = KoraTokens.SpaceM), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(KoraTokens.SpaceS)) {
                KoraTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.weight(1f).height(KoraTokens.FieldHeight),
                    singleLine = true,
                    placeholder = { Text("Ask about your records") },
                    keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Send),
                )
                IconButton(
                    onClick = { vm.sendAssistantMessage(draft); draft = "" },
                    enabled = draft.isNotBlank() && !state.assistantBusy && !state.offline,
                    modifier = Modifier.size(KoraTokens.ButtonHeight).background(colors.primary, RoundedCornerShape(KoraTokens.RadiusControl)),
                ) { Icon(Icons.Outlined.ArrowForward, "Send message", tint = colors.onPrimary) }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun PointOfSaleScreen(state: AppState, vm: AppViewModel) {
    var search by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("All") }
    var basketOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val sheetScope = rememberCoroutineScope()
    val productGridState = rememberLazyGridState()
    val manifest = state.posManifest ?: JSONObject()
    val metadata = manifest.optJSONObject("metadata") ?: JSONObject()
    val spec = manifest.optJSONObject("spec") ?: JSONObject()
    val components = spec.optJSONObject("layout")?.optJSONArray("children") ?: org.json.JSONArray()
    val componentRows = (0 until components.length()).mapNotNull { components.optJSONObject(it) }
    val productComponent = componentRows.firstOrNull { it.optString("component") == "product_grid" }
    val categoryComponent = componentRows.firstOrNull { it.optString("component") == "category_tabs" }
    val cartComponent = componentRows.firstOrNull { it.optString("component") == "cart_panel" }
    val paymentComponent = componentRows.firstOrNull { it.optString("component") == "payment_panel" }
    val productBindings = productComponent?.optJSONObject("props")?.optJSONObject("bindings") ?: JSONObject()
    val categoryBindings = categoryComponent?.optJSONObject("props")?.optJSONObject("bindings") ?: JSONObject()
    val paymentBindings = paymentComponent?.optJSONObject("props")?.optJSONObject("bindings") ?: JSONObject()
    val titleField = productBindings.optString("title").ifBlank { "name" }
    val priceField = productBindings.optString("price")
    val categoryField = categoryBindings.optString("group_field").ifBlank { productBindings.optString("subtitle") }
    val stockField = productBindings.optString("badge")
    val resourceDoctype = resolveComponentResourceDoctype(spec, productComponent).orEmpty()
    val resourceLabel = state.navigation?.modules.orEmpty().flatMap { it.doctypes }
        .firstOrNull { it.name.equals(resourceDoctype, true) }?.label
        ?: productComponent?.optJSONObject("props")?.optString("title")?.ifBlank { null }
        ?: "Records"
    val cartTitle = cartComponent?.optJSONObject("props")?.optString("title")?.takeIf(String::isNotBlank) ?: "Basket"
    val paymentTitle = paymentComponent?.optJSONObject("props")?.optString("title")?.takeIf(String::isNotBlank) ?: "Payment"
    val transactionTarget = paymentComponent?.optJSONObject("props")?.optJSONArray("actions")?.let { actions ->
        (0 until actions.length()).mapNotNull { actions.optJSONObject(it) }
            .firstOrNull { it.optString("type") == "create_transaction" }
            ?.optJSONObject("config")?.optString("target_doctype")
    }.orEmpty()
    val transactionLabel = state.navigation?.modules.orEmpty().flatMap { it.doctypes }
        .firstOrNull { it.name.equals(transactionTarget, true) }?.label
        ?: transactionTarget.takeIf(String::isNotBlank)?.replace('_', ' ')
        ?: "transaction"
    val methods = paymentBindings.optString("methods").removePrefix("literal:").split(',').map { it.trim().lowercase() }.filter(String::isNotBlank)
    val configuredPayment = paymentComponent?.optJSONObject("props")?.optJSONArray("actions")?.let { actions ->
        (0 until actions.length()).mapNotNull { actions.optJSONObject(it) }.any { it.optString("type") == "initiate_external_operation" }
    } == true
    val paymentActions = paymentComponent?.optJSONObject("props")?.optJSONArray("actions") ?: org.json.JSONArray()
    val prerequisiteActions = (0 until paymentActions.length()).mapNotNull(paymentActions::optJSONObject)
        .filter { it.optString("type") == "create_record" && it.optJSONObject("config")?.optString("context_field").orEmpty().isNotBlank() }
    val prerequisiteFields = prerequisiteActions.mapNotNull { it.optJSONObject("config")?.optString("context_field")?.takeIf(String::isNotBlank) }
    val relatedRecordFields = (0 until paymentActions.length()).mapNotNull(paymentActions::optJSONObject)
        .firstOrNull { it.optString("type") == "create_transaction" }?.optJSONObject("config")?.optJSONArray("required_related_status")
        ?.let { requirements -> (0 until requirements.length()).mapNotNull { requirements.optJSONObject(it)?.optString("record_field")?.takeIf(String::isNotBlank) } }
        .orEmpty()
    val missingPrerequisiteFields = (prerequisiteFields + relatedRecordFields).distinct().filter { state.posPrerequisiteRecords[it].isNullOrBlank() }
    val prerequisitesReady = missingPrerequisiteFields.isEmpty()
    val categories = listOf("All") + state.posCategories.values.distinct().sorted()
    val products = state.posProducts.filter { product ->
        val matchesText = search.isBlank() || product.optString(titleField).contains(search, true)
        val categoryName = state.posCategories[product.optString(categoryField)].orEmpty()
        // The resource query returned by the page manifest is the source of truth for
        // which records are available. Do not silently filter on a tenant field named
        // `status`; sites can expose their own lifecycle fields or query constraints.
        matchesText && (category == "All" || categoryName == category) && (stockField.isBlank() || product.optInt(stockField, 0) > 0)
    }
    val cartLines = state.posCart.values.toList()
    val total = cartLines.sumOf { it.product.optDouble(priceField) * it.quantity }
    val colors = androidx.compose.material3.MaterialTheme.colorScheme
    val registerName = paymentBindings.optString("register").removePrefix("literal:").ifBlank { "Register" }
    val isCompactHeight = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp < 700
    Column(Modifier.fillMaxSize().padding(horizontal = KoraTokens.Space4)) {
        Row(Modifier.fillMaxWidth().padding(top = if (isCompactHeight) KoraMeasures.dp2 else KoraTokens.Space2, bottom = KoraTokens.Space2), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(metadata.optString("name", "Point of sale"), style = androidx.compose.material3.MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("$registerName  ·  ${products.size} ${pluralizeDisplayLabel(resourceLabel, products.size)}", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
            Surface(color = colors.secondary, shape = RoundedCornerShape(KoraMeasures.dp14)) {
                Column(Modifier.padding(horizontal = KoraMeasures.dp12, vertical = KoraMeasures.dp7), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${cartLines.sumOf { it.quantity }}", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.primary)
                    Text(state.posLineLabel, style = androidx.compose.material3.MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                }
            }
        }
        KoraTextField(
            value = search, onValueChange = { search = it }, modifier = Modifier.fillMaxWidth(),
            singleLine = true, leadingIcon = { Icon(Icons.Outlined.Search, null) },
            placeholder = { Text("Search ${resourceLabel.lowercase(Locale.getDefault())}") }
        )
        Row(Modifier.fillMaxWidth().padding(top = KoraMeasures.dp4, bottom = KoraMeasures.dp6).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
            categories.forEach { item -> FilterChip(selected = category == item, onClick = { category = item }, label = { Text(item) }) }
        }
        state.message?.takeUnless(::isPassiveConnectionMessage)?.let { MessageBanner(it, false) }
        if (state.posLoading && state.posProducts.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { CircularProgressIndicator(); Text("Loading ${resourceLabel.lowercase(Locale.getDefault())}…", Modifier.padding(top = KoraMeasures.dp12), color = colors.onSurfaceVariant) } }
        } else if (state.posProducts.isEmpty()) {
            Column(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                EmptyCard("No ${resourceLabel.lowercase(Locale.getDefault())} available", state.message ?: "Configured records will appear here when available.")
                TextButton(onClick = vm::refreshPosCatalog) { Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(KoraMeasures.dp17)); Text(" Try again") }
            }
        } else if (products.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(if (search.isNotBlank()) "No ${resourceLabel.lowercase(Locale.getDefault())} match ‘$search’" else "No ${resourceLabel.lowercase(Locale.getDefault())} in this category", color = colors.onSurfaceVariant)
            }
        } else {
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                val columns = if (maxWidth >= KoraMeasures.dp680) 4 else if (maxWidth >= KoraMeasures.dp460) 3 else 2
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns), state = productGridState, modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = KoraMeasures.dp4, bottom = KoraMeasures.dp12), horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp10),
                    verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp8),
                ) {
                    items(products.size, key = { products[it].optString("name") }) { index ->
                        val product = products[index]
                        val inCart = state.posCart[product.optString("name")]?.quantity ?: 0
                        Card(
                            Modifier.fillMaxWidth().heightIn(min = KoraMeasures.dp76).clickable { vm.addPosProduct(product) },
                            shape = RoundedCornerShape(KoraTokens.RadiusCard),
                            colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow),
                            border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant),
                        ) {
                            Column(Modifier.fillMaxWidth().padding(horizontal = KoraMeasures.dp9, vertical = KoraMeasures.dp7), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp2)) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp4)) {
                                    Text(product.optString(titleField).ifBlank { product.optString("name") }, Modifier.weight(1f), style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    if (inCart > 0) Surface(color = colors.primary, shape = CircleShape) {
                                        Text("$inCart", Modifier.padding(horizontal = KoraMeasures.dp6, vertical = KoraMeasures.dp2), color = colors.onPrimary, style = androidx.compose.material3.MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    }
                                }
                                val categoryLabel = state.posCategories[product.optString(categoryField)].orEmpty()
                                if (categoryLabel.isNotBlank()) Text(categoryLabel, style = androidx.compose.material3.MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                                    Text(if (priceField.isBlank()) "—" else formatCurrency(product.optDouble(priceField), state.currency), Modifier.weight(1f), style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = colors.primary, maxLines = 1)
                                    if (stockField.isNotBlank()) Text("${product.optInt(stockField)} left", style = androidx.compose.material3.MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth().padding(top = KoraMeasures.dp4, bottom = KoraMeasures.dp8).clickable { basketOpen = true },
            shape = RoundedCornerShape(KoraTokens.RadiusCard), color = colors.surface, tonalElevation = KoraMeasures.dp2,
        ) {
            Row(Modifier.fillMaxWidth().padding(start = KoraMeasures.dp16, end = KoraMeasures.dp8, top = KoraMeasures.dp8, bottom = KoraMeasures.dp8), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.ShoppingCart, null, tint = colors.primary)
                Spacer(Modifier.width(KoraMeasures.dp12))
                Column(Modifier.weight(1f)) {
                    Text(if (cartLines.isEmpty()) "$cartTitle is empty" else "${cartLines.sumOf { it.quantity }} items in ${cartTitle.lowercase(Locale.getDefault())}", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(if (cartLines.isEmpty()) "Choose a record to add it" else "${formatCurrency(total, state.currency)} total", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                }
                Button(onClick = { basketOpen = true }, enabled = cartLines.isNotEmpty(), shape = RoundedCornerShape(KoraMeasures.dp14), contentPadding = PaddingValues(horizontal = KoraMeasures.dp18, vertical = KoraMeasures.dp11)) {
                    Text("Review", fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Outlined.ArrowForward, null, modifier = Modifier.padding(start = KoraMeasures.dp6).size(KoraMeasures.dp16))
                }
            }
        }
    }
    androidx.compose.runtime.LaunchedEffect(category, search) { productGridState.scrollToItem(0) }
    if (basketOpen) ModalBottomSheet(
        onDismissRequest = { basketOpen = false },
        sheetState = sheetState,
        containerColor = colors.surface,
        dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle() },
    ) {
        Column(Modifier.fillMaxWidth().heightIn(max = KoraMeasures.dp720)) {
            Column(Modifier.weight(1f, fill = false).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = KoraMeasures.dp20)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(cartTitle, style = androidx.compose.material3.MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        val quantity = cartLines.sumOf { it.quantity }
                        Text("$quantity ${pluralizeDisplayLabel(resourceLabel, quantity)} · $registerName", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                    }
                    if (cartLines.isNotEmpty()) TextButton(onClick = vm::clearPosCart) { Text("Clear") }
                }
                Spacer(Modifier.height(KoraMeasures.dp12))
                if (cartLines.isEmpty()) {
                    Text("Choose a ${resourceLabel.lowercase(Locale.getDefault())} to add it to ${cartTitle.lowercase(Locale.getDefault())}.", Modifier.padding(vertical = KoraMeasures.dp24), color = colors.onSurfaceVariant)
                } else {
                    cartLines.forEach { line ->
                        val productName = line.product.optString("name")
                        Row(Modifier.fillMaxWidth().padding(vertical = KoraMeasures.dp7), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(line.product.optString(titleField).ifBlank { line.product.optString("name") }, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text("${formatCurrency(line.product.optDouble(priceField), state.currency)} each", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                            }
                            IconButton(onClick = { vm.changePosQuantity(productName, -1) }, modifier = Modifier.size(KoraMeasures.dp44)) { Icon(Icons.Outlined.Remove, "Remove one ${line.product.optString(titleField)}") }
                            Text(line.quantity.toString(), Modifier.width(KoraMeasures.dp24), style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            IconButton(onClick = { vm.changePosQuantity(productName, 1) }, modifier = Modifier.size(KoraMeasures.dp44)) { Icon(Icons.Outlined.Add, "Add one ${line.product.optString(titleField)}") }
                        }
                        Divider(color = colors.outline.copy(alpha = 0.55f))
                    }
                    Spacer(Modifier.height(KoraMeasures.dp12))
                    Text(paymentTitle, style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    if (prerequisiteActions.isNotEmpty()) {
                        Surface(
                            Modifier.fillMaxWidth().padding(top = KoraMeasures.dp8),
                            shape = RoundedCornerShape(KoraMeasures.dp12),
                            color = colors.surfaceContainerLow,
                            border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant),
                        ) {
                            Column(Modifier.fillMaxWidth().padding(KoraMeasures.dp12), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                                Text(if (prerequisitesReady) "Required records are ready" else "Complete the required setup", style = androidx.compose.material3.MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                prerequisiteActions.forEach { action ->
                                    val config = action.optJSONObject("config") ?: JSONObject()
                                    val contextField = config.optString("context_field")
                                    val label = config.optString("label").ifBlank { config.optString("target_doctype", "Record") }
                                    val record = state.posPrerequisiteRecords[contextField]
                                    Text(if (record.isNullOrBlank()) "$label · Needs setup" else "$label · $record", style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    if (record.isNullOrBlank()) {
                                        val inputs = config.optJSONArray("inputs") ?: org.json.JSONArray()
                                        for (inputIndex in 0 until inputs.length()) {
                                            val input = inputs.optJSONObject(inputIndex) ?: continue
                                            val field = input.optString("field")
                                            if (field.isBlank()) continue
                                            val type = input.optString("fieldtype")
                                            KoraTextField(
                                                value = state.posPrerequisiteInputs[field].orEmpty().ifBlank { input.optString("default") },
                                                onValueChange = { vm.setPosPrerequisiteInput(field, it) },
                                                modifier = Modifier.fillMaxWidth(),
                                                label = input.optString("label").ifBlank { field },
                                                singleLine = true,
                                                leadingIcon = if (type == "Currency") ({ Text(state.currency.ifBlank { "Currency" }, style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant) }) else null,
                                                keyboardOptions = KeyboardOptions(keyboardType = if (type in setOf("Int", "Currency", "Float")) KeyboardType.Decimal else KeyboardType.Text),
                                            )
                                        }
                                    }
                                }
                                if (!prerequisitesReady) Button(onClick = vm::openPosPrerequisites, enabled = !state.posPrerequisitesInProgress && !state.offline, modifier = Modifier.fillMaxWidth()) {
                                    if (state.posPrerequisitesInProgress) CircularProgressIndicator(Modifier.size(KoraMeasures.dp18), strokeWidth = KoraMeasures.dp2)
                                    else Text(prerequisiteActions.firstOrNull { state.posPrerequisiteRecords[it.optJSONObject("config")?.optString("context_field").orEmpty()].isNullOrBlank() }
                                        ?.optJSONObject("config")?.optString("button_label")?.ifBlank { null } ?: "Create required records", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = KoraMeasures.dp5), horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                        methods.forEach { method ->
                            val isMobileMoney = method in setOf("mpesa", "mobile_money", "mobile")
                            FilterChip(selected = state.posPaymentMethod == method, enabled = !isMobileMoney, onClick = { vm.setPosPaymentMethod(method) }, label = {
                                Text(if (isMobileMoney && configuredPayment) "${method.replace('_', ' ')} · soon" else method.replace('_', ' ').replaceFirstChar { it.uppercase() }, maxLines = 1)
                            })
                        }
                    }
                    if (methods.any { it in setOf("mpesa", "mobile_money", "mobile") } && configuredPayment) {
                        Text("Mobile money prompts aren’t available in the native register yet.", Modifier.padding(top = KoraMeasures.dp4), style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                    }
                }
            }
            state.message?.takeUnless(::isPassiveConnectionMessage)?.let { MessageBanner(it, warning = true) }
            if (cartLines.isNotEmpty()) Column(Modifier.fillMaxWidth().background(colors.surface).padding(horizontal = KoraMeasures.dp20).padding(top = KoraMeasures.dp10, bottom = KoraMeasures.dp18)) {
                Divider(color = colors.outline.copy(alpha = 0.55f))
                Row(Modifier.fillMaxWidth().padding(top = KoraMeasures.dp12, bottom = KoraMeasures.dp10), verticalAlignment = Alignment.CenterVertically) {
                    Text("Total", Modifier.weight(1f), style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = colors.onSurfaceVariant)
                    Text(formatCurrency(total, state.currency), style = androidx.compose.material3.MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = colors.primary)
                }
                Button(
                    onClick = {
                        basketOpen = false
                        sheetScope.launch { sheetState.hide() }
                        vm.completePosSale()
                    },
                    modifier = Modifier.fillMaxWidth().height(KoraMeasures.dp54),
                    enabled = !state.posSaving && prerequisitesReady && state.posPaymentMethod !in setOf("mpesa", "mobile_money", "mobile"),
                    shape = RoundedCornerShape(KoraMeasures.dp16),
                ) {
                    if (state.posSaving) CircularProgressIndicator(Modifier.size(KoraMeasures.dp19), strokeWidth = KoraMeasures.dp2, color = colors.onPrimary)
                    else Text("Complete ${transactionLabel.lowercase(Locale.getDefault())} · ${state.posPaymentMethod.replace('_', ' ').replaceFirstChar { it.uppercase() }}", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun DashboardScreen(state: AppState, vm: AppViewModel) {
    var query by remember { mutableStateOf("") }
    val normalizedQuery = query.trim().lowercase()
    val modules = state.navigation?.modules.orEmpty().mapNotNull { module ->
        val availableDoctypes = module.doctypes.filterNot { it.isChild }
        val matchesModule = module.label.lowercase().contains(normalizedQuery) || module.module.lowercase().contains(normalizedQuery)
        val doctypes = availableDoctypes.filter { it.label.lowercase().contains(normalizedQuery) || it.name.lowercase().contains(normalizedQuery) }
        when { normalizedQuery.isBlank() || matchesModule -> module.copy(doctypes = availableDoctypes); doctypes.isNotEmpty() -> module.copy(doctypes = doctypes); else -> null }
    }.sortedWith(compareBy<ModuleGroup> { it.order ?: Int.MAX_VALUE }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.label })
    val colors = androidx.compose.material3.MaterialTheme.colorScheme
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = KoraMeasures.dp164),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = KoraMeasures.dp20, top = KoraMeasures.dp8, end = KoraMeasures.dp20, bottom = KoraMeasures.dp18),
        horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp12),
        verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp12),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(Modifier.fillMaxWidth().padding(bottom = KoraMeasures.dp2)) {
                Text("Good to see you,", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                Text(state.user?.fullName?.ifBlank { state.user.email } ?: "Your workspace", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            KoraTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                placeholder = { Text("Search modules and record types") }
            )
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            SectionHeading(if (normalizedQuery.isBlank()) "Your apps" else "Matching apps", "${modules.size} ${if (modules.size == 1) "module" else "modules"}")
        }
        if (modules.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
            EmptyCard(if (normalizedQuery.isBlank()) "No apps available" else "No matches found", if (normalizedQuery.isBlank()) "Apps appear here when your account has access to them." else "Try a different app or record type name.")
        }
        items(modules, key = { it.module }) { module ->
            ModuleCard(module.label, module.doctypes.size, module.icon) { vm.openModule(module.module) }
        }
        if (normalizedQuery.isNotBlank()) {
            val matchedTypes = modules.flatMap { it.doctypes }.distinctBy { it.resourceName }
            if (matchedTypes.isNotEmpty()) item(span = { GridItemSpan(maxLineSpan) }) { SectionHeading("Record types", "${matchedTypes.size}") }
            items(matchedTypes, key = { "doctype-${it.resourceName}" }, span = { GridItemSpan(maxLineSpan) }) { item -> DocTypeRow(item) { vm.openDoctype(item) } }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
private fun SettingsScreen(state: AppState, vm: AppViewModel, onVoiceInput: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val page = state.settingsPage
    if (page == null) {
        val admin = state.navigation?.adminCapabilities.orEmpty().toSet()
        val groups = listOf(
            "General" to listOf("Overview", "Tables", "Access", "Team"),
            "Automation" to listOf("Automations", "AI agents", "AI connection", "Scripts", "Integrations", "Credentials", "Templates"),
            "Monitoring" to listOf("Change history", "Insights"),
        )
        val capabilityFor = mapOf(
            "Tables" to "doctypes", "Access" to "permissions", "Team" to "users",
            "Automations" to "automations", "Scripts" to "scripts", "Integrations" to "extensions",
            "AI agents" to "agents", "AI connection" to "agents", "Credentials" to "secrets", "Templates" to "views", "Change history" to "versions", "Insights" to "analytics",
        )
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = KoraMeasures.dp32),
            verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp0),
        ) {
            groups.forEach { (group, labels) ->
                val visible = labels.filter { label -> label == "Overview" || admin.contains(capabilityFor[label]) }
                if (visible.isNotEmpty()) {
                    item(key = "settings-section-$group") {
                        Surface(color = colors.background) {
                            Text(
                                group,
                                Modifier.fillMaxWidth().padding(start = KoraMeasures.dp16, top = KoraMeasures.dp14, bottom = KoraMeasures.dp6),
                                color = colors.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                    items(visible, key = { "settings-$it" }) { label ->
                        Column(Modifier.fillMaxWidth().background(colors.surface).clickable { vm.openSettingsPage(label) }) {
                            Row(Modifier.fillMaxWidth().heightIn(min = KoraMeasures.dp48).padding(horizontal = KoraMeasures.dp16), verticalAlignment = Alignment.CenterVertically) {
                                Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                                Icon(Icons.Outlined.ChevronRight, null, tint = colors.onSurfaceVariant)
                            }
                            Divider(color = colors.outlineVariant.copy(alpha = .7f))
                        }
                    }
                }
            }
        }
        return
    }

    when (page) {
        "Automations" -> {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = KoraMeasures.dp16, vertical = KoraMeasures.dp12),
                verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp10),
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Automations", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        TextButton(onClick = vm::loadWorkflowDefinitions, modifier = Modifier.heightIn(min = KoraMeasures.dp40)) { Text("Refresh") }
                    }
                    Text("Published workflow definitions available to this account.", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
                if (state.workflowDefinitionsLoading && state.workflowDefinitions.isEmpty()) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
                state.workflowDefinitionsError?.let { error ->
                    item {
                        EmptyCard("Automations unavailable", error)
                        TextButton(onClick = vm::loadWorkflowDefinitions) { Text("Retry") }
                    }
                }
                if (!state.workflowDefinitionsLoading && state.workflowDefinitions.isEmpty() && state.workflowDefinitionsError == null) {
                    item { EmptyCard("No published automations", "Published workflow definitions will appear here when this workspace makes them available.") }
                }
                items(state.workflowDefinitions, key = { "workflow-${it.optString("definition_id")}-${it.optInt("version")}" }) { reference ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { vm.openWorkflowDefinition(reference) },
                        color = colors.surface,
                        shape = RoundedCornerShape(KoraMeasures.dp12),
                        border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant.copy(alpha = .75f)),
                    ) {
                        Row(Modifier.padding(horizontal = KoraMeasures.dp14, vertical = KoraMeasures.dp12), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp10)) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp3)) {
                                Text(reference.optString("definition_id").ifBlank { "Workflow" }, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text("Version ${reference.optInt("version")}", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                            }
                            Surface(color = colors.secondaryContainer, shape = RoundedCornerShape(KoraMeasures.dp4)) {
                                Text("Published", Modifier.padding(horizontal = KoraMeasures.dp8, vertical = KoraMeasures.dp3), style = MaterialTheme.typography.labelSmall, color = colors.onSecondaryContainer)
                            }
                            Icon(Icons.Outlined.ChevronRight, contentDescription = "View workflow", tint = colors.onSurfaceVariant)
                        }
                    }
                }
            }
            if (state.selectedWorkflowLoading || state.selectedWorkflow != null || state.selectedWorkflowError != null) {
                ModalBottomSheet(onDismissRequest = vm::closeWorkflowDefinition, containerColor = colors.surface) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = KoraMeasures.dp16).padding(bottom = KoraMeasures.dp24), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp10)) {
                        Text("Workflow definition", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        if (state.selectedWorkflowLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
                        state.selectedWorkflowError?.let { EmptyCard("Could not open workflow", it) }
                        state.selectedWorkflow?.let { definition ->
                            val trigger = definition.optJSONObject("trigger")
                            val steps = definition.optJSONArray("steps") ?: JSONArray()
                            val triggerLabel = listOfNotNull(trigger?.optString("kind")?.takeIf(String::isNotBlank), trigger?.optString("table")?.takeIf(String::isNotBlank)).joinToString(" · ")
                            Surface(color = colors.surface, shape = RoundedCornerShape(KoraMeasures.dp12), border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant)) {
                                Column(Modifier.fillMaxWidth().padding(KoraMeasures.dp14), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp4)) {
                                    Text("Starts when", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                                    Text(triggerLabel.ifBlank { "Workflow is started manually" }, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            for (index in 0 until steps.length()) {
                                Box(Modifier.padding(start = KoraMeasures.dp24).width(KoraMeasures.dp2).height(KoraMeasures.dp14).background(colors.outlineVariant))
                                val step = steps.optJSONObject(index) ?: continue
                                val kind = step.optString("kind").replace('_', ' ').replaceFirstChar { it.uppercase() }
                                val payloadKey = step.optString("kind").lowercase()
                                val payload = step.optJSONObject(payloadKey) ?: JSONObject()
                                val detail = when {
                                    payload.optString("name").isNotBlank() -> payload.optString("name")
                                    payload.optString("outcome").isNotBlank() -> payload.optString("outcome")
                                    payload.optString("field").isNotBlank() -> payload.optString("field")
                                    payload.optString("policy_id").isNotBlank() -> payload.optString("policy_id")
                                    else -> step.optString("id")
                                }
                                Surface(color = colors.surface, shape = RoundedCornerShape(KoraMeasures.dp12), border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant)) {
                                    Column(Modifier.fillMaxWidth().padding(KoraMeasures.dp14), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp3)) {
                                        Text(kind.ifBlank { "Step" }, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                                        Text(detail.ifBlank { "Configured step" }, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                            if (steps.length() > 0) {
                                Box(Modifier.padding(start = KoraMeasures.dp24).width(KoraMeasures.dp2).height(KoraMeasures.dp14).background(colors.outlineVariant))
                                Text("Finish", Modifier.padding(start = KoraMeasures.dp12), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
        "Overview" -> {
            val branding = state.brandingDraft ?: state.tenantBranding ?: Branding()
            val savedBranding = state.savedBranding ?: state.tenantBranding
            var currencyPickerOpen by rememberSaveable { mutableStateOf(false) }
            var currencySearch by rememberSaveable { mutableStateOf("") }
            val availableCurrencies = remember {
                java.util.Currency.getAvailableCurrencies().sortedBy { it.currencyCode }
            }
            val canEdit = state.user?.roles?.any { it.equals("Administrator", ignoreCase = true) } == true
            val editingEnabled = canEdit && !state.settingsSaving
            val primaryColor = branding.primaryColor.orEmpty()
            val accentColor = branding.accentColor.orEmpty()
            val selectedPalette = KoraPaletteFamilies.find(primaryColor)
            val paletteChoices = KoraPaletteFamilies.choices.map { it.name } + "Custom"
            val paletteName = selectedPalette?.name ?: "Custom"
            val primaryPreview = parseColorOr(primaryColor, colors.primary)
            val accentPreview = parseColorOr(accentColor, colors.tertiary)
            val palettePreview = selectedPalette?.preview ?: listOf(primaryPreview, accentPreview, colors.surfaceContainerHighest, KoraTokens.Success, KoraTokens.Warning)
            val brandingDirty = savedBranding != null && branding != savedBranding
            val dirty = state.currency != state.savedCurrency || brandingDirty
            val brandingValid = branding.appName.isNotBlank() &&
                primaryColor.matches(Regex("#[0-9a-fA-F]{6}")) &&
                (accentColor.isBlank() || accentColor.matches(Regex("#[0-9a-fA-F]{6}"))) &&
                (branding.defaultMode ?: "light") in setOf("light", "dark", "system")
            val currencyValid = state.currency.matches(Regex("[A-Z]{3}"))
            Column(Modifier.fillMaxSize()) {
                LazyColumn(
                    Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = KoraMeasures.dp16, vertical = KoraMeasures.dp12),
                    verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp16),
                ) {
                    item {
                        KoraTextField(
                            value = branding.appName,
                            onValueChange = { value -> vm.updateSettingsBranding { it.copy(appName = value) } },
                            label = "Workspace name",
                            enabled = editingEnabled,
                            singleLine = true,
                        )
                    }
                    item {
                        Text("Brand color", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = colors.onSurface)
                        Spacer(Modifier.height(KoraMeasures.dp6))
                        Row(horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp10), verticalAlignment = Alignment.CenterVertically) {
                            Surface(Modifier.size(KoraMeasures.dp36), shape = RoundedCornerShape(KoraMeasures.dp8), color = primaryPreview, border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant)) {}
                            KoraTextField(
                                value = primaryColor,
                                onValueChange = { value -> vm.updateSettingsBranding { it.copy(primaryColor = value) } },
                                modifier = Modifier.weight(1f),
                                enabled = editingEnabled,
                                singleLine = true,
                            )
                        }
                    }
                    item {
                        ChoiceField(
                            label = "Palette family",
                            value = paletteName,
                            placeholder = "Choose a palette",
                            choices = paletteChoices,
                            choiceLabels = mapOf("Custom" to "Custom"),
                            allowEmpty = false,
                            enabled = editingEnabled,
                            onSelect = { name ->
                                KoraPaletteFamilies.choices.firstOrNull { it.name == name }?.let { family ->
                                    vm.updateSettingsBranding { it.copy(primaryColor = family.primaryHex, accentColor = family.accentHex) }
                                }
                            },
                        )
                    }
                    item {
                        val appearance = branding.defaultMode?.takeIf { it in setOf("light", "dark", "system") } ?: "light"
                        ChoiceField(
                            label = "Default appearance",
                            value = appearance,
                            placeholder = "Choose appearance",
                            choices = listOf("light", "dark", "system"),
                            choiceLabels = mapOf("light" to "Light", "dark" to "Dark", "system" to "System"),
                            allowEmpty = false,
                            enabled = editingEnabled,
                            onSelect = { mode -> vm.updateSettingsBranding { it.copy(defaultMode = mode) } },
                        )
                    }
                    item {
                        val contrastRatio = colorContrastRatio(primaryPreview, colors.surface)
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("Palette preview", Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = colors.onSurface)
                            Surface(color = if (contrastRatio >= 4.5) colors.secondaryContainer else colors.errorContainer, shape = RoundedCornerShape(KoraMeasures.dp4)) {
                                Text(
                                    if (contrastRatio >= 4.5) "Contrast ${"%.1f".format(contrastRatio)}:1 · AA" else "Check contrast ${"%.1f".format(contrastRatio)}:1",
                                    Modifier.padding(horizontal = KoraMeasures.dp8, vertical = KoraMeasures.dp2),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (contrastRatio >= 4.5) colors.onSecondaryContainer else colors.onErrorContainer,
                                )
                            }
                        }
                        Spacer(Modifier.height(KoraMeasures.dp8))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                            palettePreview.forEachIndexed { index, color ->
                                Surface(
                                    Modifier.weight(1f).height(KoraMeasures.dp36),
                                    shape = RoundedCornerShape(KoraMeasures.dp8),
                                    color = color,
                                    border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant),
                                ) { Box(Modifier.fillMaxSize().semantics { contentDescription = "Palette color ${index + 1}" }) }
                            }
                        }
                    }
                    item {
                        val selectedCurrency = availableCurrencies.firstOrNull { it.currencyCode == state.currency }
                        val currencyLabel = selectedCurrency?.let { "${it.currencyCode} · ${it.getDisplayName(Locale.getDefault())}" } ?: state.currency
                        Column(verticalArrangement = Arrangement.spacedBy(KoraTokens.FieldLabelGap)) {
                            Text("Currency", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = colors.onSurface)
                            Surface(
                                modifier = Modifier.fillMaxWidth().heightIn(min = KoraTokens.FieldHeight)
                                    .clickable(enabled = editingEnabled, role = Role.Button) { currencySearch = ""; currencyPickerOpen = true }
                                    .semantics {
                                        contentDescription = "Currency: $currencyLabel"
                                    },
                                shape = RoundedCornerShape(KoraTokens.RadiusField),
                                color = if (editingEnabled) colors.surface else colors.surfaceContainerHigh,
                                border = androidx.compose.foundation.BorderStroke(KoraTokens.Hairline, colors.outlineVariant),
                            ) {
                                Row(Modifier.fillMaxWidth().padding(horizontal = KoraTokens.SpaceM, vertical = KoraMeasures.dp8), verticalAlignment = Alignment.CenterVertically) {
                                    Text(currencyLabel, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = colors.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    if (editingEnabled) Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = colors.onSurfaceVariant)
                                }
                            }
                        }
                    }
                    item {
                        Surface(
                            color = warningContainer(colors),
                            shape = RoundedCornerShape(KoraMeasures.dp10),
                        ) {
                            Text("Changing currency updates reports and receipts. Stored amounts are not converted.", Modifier.padding(horizontal = KoraMeasures.dp12, vertical = KoraMeasures.dp10), style = MaterialTheme.typography.bodySmall, color = warningForeground(colors))
                        }
                    }
                    state.message?.let { message -> item { Text(message, style = MaterialTheme.typography.bodySmall, color = if (state.message == "Settings saved") colors.primary else colors.error) } }
                }
                if (dirty && canEdit) Surface(color = colors.onSurface, contentColor = colors.surface) {
                    Row(Modifier.fillMaxWidth().height(KoraMeasures.dp64).padding(horizontal = KoraMeasures.dp16), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                        Text("Unsaved changes", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        OutlinedButton(
                            onClick = vm::discardSettingsChanges,
                            enabled = !state.settingsSaving,
                            modifier = Modifier.height(KoraMeasures.dp40),
                            shape = RoundedCornerShape(KoraTokens.RadiusControl),
                            border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.surface),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.surface),
                        ) { Text("Discard") }
                        Button(
                            onClick = vm::saveSettings,
                            enabled = !state.settingsSaving && currencyValid && brandingValid,
                            modifier = Modifier.height(KoraMeasures.dp40),
                            shape = RoundedCornerShape(KoraTokens.RadiusControl),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.surface,
                                contentColor = colors.onSurface,
                                disabledContainerColor = colors.surface.copy(alpha = .72f),
                                disabledContentColor = colors.onSurfaceVariant,
                            ),
                        ) {
                            if (state.settingsSaving) CircularProgressIndicator(Modifier.size(KoraMeasures.dp18), strokeWidth = KoraMeasures.dp2) else Text("Save")
                        }
                    }
                }
            }
            if (currencyPickerOpen) {
                val filteredCurrencies = availableCurrencies.filter { currency ->
                    currency.currencyCode.contains(currencySearch.trim(), ignoreCase = true) ||
                        currency.getDisplayName(Locale.getDefault()).contains(currencySearch.trim(), ignoreCase = true)
                }
                ModalBottomSheet(
                    onDismissRequest = { currencyPickerOpen = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    containerColor = colors.surface,
                ) {
                    Column(Modifier.fillMaxWidth().heightIn(max = KoraMeasures.dp600).padding(horizontal = KoraMeasures.dp16)) {
                        Text("Select currency", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        KoraTextField(
                            value = currencySearch,
                            onValueChange = { currencySearch = it },
                            modifier = Modifier.fillMaxWidth().padding(top = KoraMeasures.dp12, bottom = KoraMeasures.dp8),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                            placeholder = { Text("Search code or currency") },
                        )
                        LazyColumn(Modifier.weight(1f, fill = false).fillMaxWidth().padding(bottom = KoraMeasures.dp12)) {
                            items(filteredCurrencies, key = { it.currencyCode }) { currency ->
                                val displayName = currency.getDisplayName(Locale.getDefault())
                                val selected = currency.currencyCode == state.currency
                                Row(
                                    Modifier.fillMaxWidth().heightIn(min = KoraMeasures.dp52)
                                        .clickable {
                                            vm.updateSettingsCurrency(currency.currencyCode)
                                            currencyPickerOpen = false
                                        }
                                        .padding(horizontal = KoraMeasures.dp8, vertical = KoraMeasures.dp6),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(currency.currencyCode, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                        Text(displayName, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                                    }
                                    if (selected) Icon(Icons.Filled.CheckCircle, contentDescription = "Selected", tint = colors.primary)
                                }
                            }
                            if (filteredCurrencies.isEmpty()) item { Text("No matching currencies", Modifier.padding(vertical = KoraMeasures.dp16), color = colors.onSurfaceVariant) }
                        }
                    }
                }
            }
        }
        "Tables" -> {
            val modules = state.navigation?.modules.orEmpty()
            var createTableOpen by rememberSaveable { mutableStateOf(false) }
            var tableName by rememberSaveable { mutableStateOf("") }
            var selectedModule by rememberSaveable { mutableStateOf(modules.firstOrNull()?.module.orEmpty()) }
            var moduleMenuOpen by rememberSaveable { mutableStateOf(false) }
            var duplicateSource by remember { mutableStateOf<JSONObject?>(null) }
            var deleteTarget by remember { mutableStateOf<JSONObject?>(null) }
            Column(Modifier.fillMaxSize()) {
            LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(horizontal = KoraMeasures.dp16, vertical = KoraMeasures.dp12), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp10)) {
                if (state.settingsDoctypesLoading && state.settingsDoctypes.isEmpty()) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
                state.settingsDoctypesError?.let { error -> item { EmptyCard("Could not load tables", error); TextButton(onClick = vm::loadSettingsDoctypes) { Text("Retry") } } }
                if (!state.settingsDoctypesLoading && state.settingsDoctypes.isEmpty() && state.settingsDoctypesError == null) item { EmptyCard("No tables available", "Record types visible to this account will appear here.") }
                items(state.settingsDoctypes, key = { it.optString("name") }) { item ->
                    val name = item.optString("name")
                    val label = item.optString("label").ifBlank { name }
                    val fields = item.optJSONArray("fields")
                    val columns = fields?.let { json -> (0 until json.length()).count { index ->
                        json.optJSONObject(index)?.let { field -> !field.optBoolean("hidden") && field.optString("fieldtype") !in setOf("Section Break", "Column Break", "Heading") } ?: false
                    } } ?: 0
                    val active = item.optString("status").equals("Active", true)
                    Surface(color = colors.surface, shape = RoundedCornerShape(KoraMeasures.dp12), border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant)) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = KoraMeasures.dp14, vertical = KoraMeasures.dp12), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp3)) {
                                Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text("$columns ${if (columns == 1) "column" else "columns"}", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp6)) {
                                Box(Modifier.size(KoraMeasures.dp8).background(if (active) successForeground(colors) else colors.outline, CircleShape))
                                Text(item.optString("status").ifBlank { "Unknown" }, style = MaterialTheme.typography.labelMedium, color = colors.onSurface)
                                var menuOpen by remember { mutableStateOf(false) }
                                Box {
                                    IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(KoraMeasures.dp44), enabled = !state.settingsDoctypeSaving) { Icon(Icons.Outlined.MoreHoriz, contentDescription = "Actions for $label") }
                                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                                        DropdownMenuItem(text = { Text("Duplicate") }, onClick = { menuOpen = false; tableName = "$label copy"; duplicateSource = item })
                                        DropdownMenuItem(text = { Text("Delete table…", color = colors.error) }, onClick = { menuOpen = false; deleteTarget = item })
                                    }
                                }
                            }
                        }
                    }
                }
                if (state.settingsDoctypes.isNotEmpty()) item { Text("Approval settings appear once a table uses approvals.", Modifier.padding(horizontal = KoraMeasures.dp2, vertical = KoraMeasures.dp4), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant) }
            }
            if (state.settingsDoctypeActionError != null) Text(state.settingsDoctypeActionError, Modifier.fillMaxWidth().padding(horizontal = KoraMeasures.dp16, vertical = KoraMeasures.dp4), color = colors.error, style = MaterialTheme.typography.bodySmall)
            state.settingsDoctypeActionMessage?.let { Text(it, Modifier.fillMaxWidth().padding(horizontal = KoraMeasures.dp16, vertical = KoraMeasures.dp4), color = colors.primary, style = MaterialTheme.typography.bodySmall) }
            if (!state.settingsDoctypesLoading && state.settingsDoctypesError == null) ExtendedFloatingActionButton(
                onClick = { tableName = ""; duplicateSource = null; selectedModule = modules.firstOrNull()?.module.orEmpty(); createTableOpen = true },
                modifier = Modifier.align(Alignment.End).padding(start = KoraMeasures.dp16, top = KoraMeasures.dp12, end = KoraMeasures.dp16, bottom = KoraMeasures.dp16).height(KoraTokens.ButtonHeight)
                    .testTag("settings-new-table").semantics(mergeDescendants = true) { contentDescription = "New table"; testTagsAsResourceId = true },
                icon = { Icon(Icons.Outlined.Add, null) }, text = { Text("New table") },
                containerColor = colors.primary, contentColor = colors.onPrimary,
                shape = RoundedCornerShape(KoraTokens.RadiusControl),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = KoraMeasures.dp4),
            )
            }
            if (createTableOpen || duplicateSource != null) AlertDialog(
                onDismissRequest = { if (!state.settingsDoctypeSaving) { createTableOpen = false; duplicateSource = null } },
                title = { Text(if (duplicateSource == null) "New table" else "Duplicate table", fontWeight = FontWeight.SemiBold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp12)) {
                        KoraTextField(tableName, { tableName = it }, Modifier.fillMaxWidth(), label = "Table name", singleLine = true)
                        if (duplicateSource == null) {
                            Box {
                                OutlinedButton(onClick = { moduleMenuOpen = true }, modifier = Modifier.fillMaxWidth().heightIn(min = KoraMeasures.dp48), enabled = modules.isNotEmpty()) { Text(modules.firstOrNull { it.module == selectedModule }?.label ?: "Choose module", Modifier.weight(1f), textAlign = TextAlign.Start); Icon(Icons.Filled.ArrowDropDown, null) }
                                DropdownMenu(expanded = moduleMenuOpen, onDismissRequest = { moduleMenuOpen = false }) {
                                    modules.forEach { module -> DropdownMenuItem(text = { Text(module.label) }, onClick = { selectedModule = module.module; moduleMenuOpen = false }) }
                                }
                            }
                        } else Text("The table schema and module will be copied. Existing records are not copied.", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    }
                },
                confirmButton = { TextButton(enabled = tableName.isNotBlank() && !state.settingsDoctypeSaving && (duplicateSource != null || selectedModule.isNotBlank()), onClick = {
                    val source = duplicateSource
                    if (source == null) vm.createSettingsDoctype(tableName, selectedModule) else vm.duplicateSettingsDoctype(source, tableName)
                    createTableOpen = false; duplicateSource = null
                }) { Text(if (state.settingsDoctypeSaving) "Saving…" else if (duplicateSource == null) "Create" else "Duplicate") } },
                dismissButton = { TextButton(onClick = { createTableOpen = false; duplicateSource = null }) { Text("Cancel") } },
                containerColor = colors.surface,
            )
            deleteTarget?.let { target ->
                val label = target.optString("label").ifBlank { target.optString("name") }
                AlertDialog(
                    onDismissRequest = { deleteTarget = null },
                    title = { Text("Delete $label?", fontWeight = FontWeight.SemiBold) },
                    text = { Text("This removes the table definition and may affect records and pages that use it. This action cannot be undone.") },
                    confirmButton = { TextButton(enabled = !state.settingsDoctypeSaving, onClick = { vm.deleteSettingsDoctype(target.optString("name")); deleteTarget = null }) { Text("Delete", color = colors.error) } },
                    dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel") } },
                    containerColor = colors.surface,
                )
            }
        }
        "Scripts", "Integrations", "Credentials", "Templates" -> SettingsResourceScreen(page, state, vm)
        "AI agents" -> {
            var createOpen by rememberSaveable { mutableStateOf(false) }
            var newAgentName by rememberSaveable { mutableStateOf("") }
            val selectedAgent = state.selectedAgentPolicy
            if (selectedAgent != null) {
                AgentPolicyEditor(state, vm)
            } else {
                Column(Modifier.fillMaxSize()) {
                    LazyColumn(
                        Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = KoraMeasures.dp16, vertical = KoraMeasures.dp12),
                        verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp10),
                    ) {
                        item {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("AI agents", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                    Text("Agent access policies for this site.", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                                }
                                Button(
                                    onClick = { newAgentName = ""; createOpen = true },
                                    modifier = Modifier.heightIn(min = KoraMeasures.dp44)
                                        .semantics(mergeDescendants = true) { contentDescription = "New agent"; testTagsAsResourceId = true }
                                        .testTag("agent-policy-new"),
                                    contentPadding = PaddingValues(horizontal = KoraMeasures.dp14),
                                ) {
                                    Icon(Icons.Outlined.Add, null, modifier = Modifier.size(KoraMeasures.dp18))
                                    Spacer(Modifier.width(KoraMeasures.dp8))
                                    Text("New agent")
                                }
                            }
                        }
                        if (state.agentPoliciesLoading && state.agentPolicies.isEmpty()) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
                        state.agentPoliciesError?.let { error ->
                            item {
                                EmptyCard("Could not load agent policies", error)
                                TextButton(onClick = vm::loadAgentPolicies) { Text("Retry") }
                            }
                        }
                        if (!state.agentPoliciesLoading && state.agentPolicies.isEmpty() && state.agentPoliciesError == null) {
                            item {
                                Surface(color = colors.secondaryContainer, shape = RoundedCornerShape(KoraMeasures.dp10)) {
                                    Row(Modifier.fillMaxWidth().padding(horizontal = KoraMeasures.dp12, vertical = KoraMeasures.dp10), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                                        Box(Modifier.size(KoraMeasures.dp8).background(successForeground(colors), CircleShape))
                                        Text("New actions stay blocked until you allow them.", style = MaterialTheme.typography.bodySmall, color = colors.onSecondaryContainer)
                                    }
                                }
                            }
                            item { KoraEmptyState("No agent policies yet", "Create a policy to choose which site roles an assistant can use.") }
                        }
                        items(state.agentPolicies, key = { "agent-${it.optString("id")}" }) { agent ->
                            Surface(
                                modifier = Modifier.fillMaxWidth().clickable { vm.openAgentPolicy(agent) },
                                color = colors.surface,
                                shape = RoundedCornerShape(KoraTokens.RadiusCard),
                                border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant),
                            ) {
                                Row(Modifier.fillMaxWidth().padding(KoraMeasures.dp14), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp10)) {
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp3)) {
                                        Text(agent.optString("name").ifBlank { agent.optString("id") }, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                        val roles = agent.optJSONArray("roles")?.let { json -> (0 until json.length()).mapNotNull(json::optString) }.orEmpty()
                                        Text(if (roles.isEmpty()) "No roles assigned · actions denied by default" else "${roles.size} ${if (roles.size == 1) "role" else "roles"} assigned", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                                    }
                                    Icon(Icons.Outlined.ChevronRight, contentDescription = "Open ${agent.optString("name")} policy", tint = colors.onSurfaceVariant)
                                }
                            }
                        }
                    }
                    if (createOpen) AlertDialog(
                        onDismissRequest = { createOpen = false },
                        title = { Text("New agent policy", fontWeight = FontWeight.SemiBold) },
                        text = { KoraTextField(newAgentName, { newAgentName = it }, Modifier.fillMaxWidth(), label = "Agent name", singleLine = true) },
                        confirmButton = { TextButton(onClick = { vm.createAgentPolicy(newAgentName); createOpen = false }, enabled = newAgentName.isNotBlank() && !state.agentPolicySaving) { Text("Create") } },
                        dismissButton = { TextButton(onClick = { createOpen = false }) { Text("Cancel") } },
                        containerColor = colors.surface,
                    )
                }
            }
        }
        "AI connection" -> AIConnectionSettingsScreen(state, vm, onVoiceInput)
        "Team" -> {
            val context = LocalContext.current
            var query by rememberSaveable { mutableStateOf("") }
            var enabledFilter by rememberSaveable { mutableStateOf("All") }
            var addUserInfoOpen by rememberSaveable { mutableStateOf(false) }
            val teamUsers = if (state.settingsUsers.isEmpty() && state.user != null) {
                listOf(JSONObject()
                    .put("name", state.user.name)
                    .put("email", state.user.email)
                    .put("full_name", state.user.fullName)
                    .put("roles", JSONArray(state.user.roles))
                    .put("enabled", true))
            } else state.settingsUsers
            val filtered = teamUsers.filter { user ->
                val isEnabled = user.optBoolean("enabled", true)
                (enabledFilter == "All" || (enabledFilter == "Enabled") == isEnabled) &&
                    (query.isBlank() || listOf(user.optString("full_name"), user.optString("email"), user.optString("name"), user.optJSONArray("roles")?.toString().orEmpty()).any { it.contains(query, true) })
            }
            val enabledCount = teamUsers.count { it.optBoolean("enabled", true) }
            Column(Modifier.fillMaxSize()) {
                Column(Modifier.fillMaxWidth().background(colors.surface).padding(horizontal = KoraMeasures.dp16, vertical = KoraMeasures.dp10), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                    KoraTextField(query, { query = it }, Modifier.fillMaxWidth(), placeholder = { Text("Search by name, email or role") }, singleLine = true)
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                        KoraChip("All ${teamUsers.size}", enabledFilter == "All") { enabledFilter = "All" }
                        KoraChip("Enabled $enabledCount", enabledFilter == "Enabled") { enabledFilter = "Enabled" }
                        KoraChip("Disabled ${teamUsers.size - enabledCount}", enabledFilter == "Disabled") { enabledFilter = "Disabled" }
                    }
                }
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    when {
                        state.settingsUsersLoading && state.settingsUsers.isEmpty() -> LinearProgressIndicator(Modifier.fillMaxWidth())
                        state.settingsUsersError != null -> {
                            EmptyCard("Could not load team", state.settingsUsersError)
                            TextButton(onClick = vm::loadSettingsUsers) { Text("Retry") }
                        }
                        teamUsers.isEmpty() -> EmptyCard("No team members", "Workspace accounts will appear here when available.")
                        filtered.isEmpty() -> EmptyCard("No matching team members", "Adjust the search or status filter.")
                        else -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = KoraMeasures.dp16, top = KoraMeasures.dp12, end = KoraMeasures.dp16, bottom = KoraMeasures.dp96), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                            items(filtered, key = { it.optString("name") }) { user ->
                                val name = user.optString("full_name").ifBlank { user.optString("email") }
                                val email = user.optString("email")
                                val roles = user.optJSONArray("roles")
                                var actionsOpen by remember(user.optString("name")) { mutableStateOf(false) }
                                Surface(color = colors.surface, shape = RoundedCornerShape(KoraMeasures.dp12), border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant.copy(alpha = .7f))) {
                                    Row(Modifier.fillMaxWidth().padding(start = KoraMeasures.dp12, end = KoraMeasures.dp4, top = KoraMeasures.dp12, bottom = KoraMeasures.dp12), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp10)) {
                                        Surface(Modifier.size(KoraMeasures.dp40), shape = CircleShape, color = colors.secondaryContainer) {
                                            Box(contentAlignment = Alignment.Center) { Text(name.firstOrNull()?.uppercase() ?: "?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = colors.onSecondaryContainer) }
                                        }
                                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp3)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp6)) {
                                                Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                if (user.optString("name") == state.user?.name) Surface(color = colors.secondaryContainer, shape = RoundedCornerShape(KoraMeasures.dp4)) {
                                                    Text("You", Modifier.padding(horizontal = KoraMeasures.dp6, vertical = KoraMeasures.dp1), style = MaterialTheme.typography.labelSmall, color = colors.onSecondaryContainer)
                                                }
                                            }
                                            Text(email, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp6)) {
                                                if (roles != null && roles.length() > 0) Surface(color = colors.secondaryContainer, shape = RoundedCornerShape(KoraMeasures.dp4)) {
                                                    Text(roles.optString(0), Modifier.padding(horizontal = KoraMeasures.dp7, vertical = KoraMeasures.dp2), style = MaterialTheme.typography.labelSmall, color = colors.onSecondaryContainer, maxLines = 1)
                                                }
                                                val isEnabled = user.optBoolean("enabled", true)
                                                Box(Modifier.size(KoraMeasures.dp8).background(if (isEnabled) successForeground(colors) else colors.error, CircleShape))
                                                Text(if (isEnabled) "Enabled" else "Disabled", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                                            }
                                        }
                                        Box {
                                            IconButton(onClick = { actionsOpen = true }, modifier = Modifier.size(KoraMeasures.dp44)) { Icon(Icons.Outlined.MoreHoriz, "User actions") }
                                            DropdownMenu(expanded = actionsOpen, onDismissRequest = { actionsOpen = false }) {
                                                DropdownMenuItem(text = { Text("Open access settings") }, onClick = { actionsOpen = false; vm.openSettingsPage("Access") })
                                                DropdownMenuItem(text = { Text("Copy email") }, onClick = {
                                                    actionsOpen = false
                                                    val clipboard = context.getSystemService(Activity.CLIPBOARD_SERVICE) as? ClipboardManager
                                                    clipboard?.setPrimaryClip(ClipData.newPlainText("Email address", email))
                                                }, enabled = email.isNotBlank())
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (!state.settingsUsersLoading && state.settingsUsersError == null) ExtendedFloatingActionButton(
                        onClick = { addUserInfoOpen = true },
                        modifier = Modifier.align(Alignment.BottomEnd).padding(end = KoraMeasures.dp16, bottom = KoraMeasures.dp16).height(KoraTokens.ButtonHeight)
                            .semantics(mergeDescendants = true) { contentDescription = "Add user" },
                        icon = { Icon(Icons.Outlined.Add, null) },
                        text = { Text("Add user") },
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary,
                        shape = RoundedCornerShape(KoraTokens.RadiusControl),
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = KoraMeasures.dp4),
                    )
                }
            }
            if (addUserInfoOpen) AlertDialog(
                onDismissRequest = { addUserInfoOpen = false },
                title = { Text("Add user") },
                text = { Text("Account creation is managed by Kora Cloud. Add or invite the person there, then refresh this list to manage their workspace access.") },
                confirmButton = {
                    TextButton(onClick = { addUserInfoOpen = false; vm.loadSettingsUsers() }) { Text("Refresh list") }
                },
                dismissButton = { TextButton(onClick = { addUserInfoOpen = false }) { Text("Close") } },
                containerColor = colors.surface,
            )
        }
        "Access" -> {
            var addRoleOpen by rememberSaveable { mutableStateOf(false) }
            var newRoleName by rememberSaveable { mutableStateOf("") }
            var newRoleDescription by rememberSaveable { mutableStateOf("") }
            var newRoleWorkspaceAccess by rememberSaveable { mutableStateOf(false) }
            val roles = state.accessRoles
            val selectedRole = state.selectedAccessRole
            val isLockedRole = selectedRole.equals("Administrator", true)
            val doctypes = state.navigation?.modules.orEmpty().flatMap { module ->
                module.doctypes.filterNot { it.isChild }.map { module.label to it }
            }
            val visibleDoctypes = doctypes
            val dirty = state.accessPermissions.map { it.toString() }.sorted() != state.savedAccessPermissions.map { it.toString() }.sorted()
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().background(colors.surface).horizontalScroll(rememberScrollState()).padding(horizontal = KoraMeasures.dp12, vertical = KoraMeasures.dp8), horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                    roles.forEach { role ->
                        val roleName = role.optString("name")
                        KoraChip(if (roleName.equals("Administrator", true)) "$roleName · locked" else roleName, selectedRole == roleName) {
                            if (!roleName.equals("Administrator", true)) vm.selectAccessRole(roleName)
                        }
                    }
                    IconButton(onClick = { addRoleOpen = true }, modifier = Modifier.size(KoraMeasures.dp40)) { Icon(Icons.Outlined.Add, "Add role", tint = colors.onSurfaceVariant) }
                }
                when {
                    state.accessLoading && roles.isEmpty() -> LinearProgressIndicator(Modifier.fillMaxWidth())
                    state.accessError != null -> {
                        EmptyCard("Could not load access", state.accessError)
                        TextButton(onClick = vm::loadAccessSettings) { Text("Retry") }
                    }
                    selectedRole.isBlank() -> Column(
                        Modifier.fillMaxWidth().padding(horizontal = KoraMeasures.dp16, vertical = KoraMeasures.dp12),
                        verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp12),
                    ) {
                        EmptyCard(
                            "No roles configured",
                            "Create a role to define which record types and actions its members can access.",
                        )
                        Button(
                            onClick = { addRoleOpen = true },
                            modifier = Modifier.fillMaxWidth().height(KoraMeasures.dp48),
                            shape = RoundedCornerShape(KoraTokens.RadiusControl),
                        ) {
                            Icon(Icons.Outlined.Add, null, modifier = Modifier.size(KoraMeasures.dp18))
                            Spacer(Modifier.width(KoraMeasures.dp8))
                            Text("Create role")
                        }
                    }
                    else -> Column(Modifier.weight(1f)) {
                        Text("$selectedRole can use these tables:", Modifier.fillMaxWidth().padding(horizontal = KoraMeasures.dp16, vertical = KoraMeasures.dp10), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                        if (visibleDoctypes.isEmpty()) EmptyCard("No record types", "Record types available to this account will appear here.")
                        else LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = KoraMeasures.dp16, vertical = KoraMeasures.dp4), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                            items(visibleDoctypes, key = { it.second.name }) { (moduleLabel, doctype) ->
                                val permission = state.accessPermissions.firstOrNull { it.optString("role") == selectedRole && it.optString("doctype") == doctype.name }
                                var showMore by remember(selectedRole, doctype.name) { mutableStateOf(false) }
                                val baseOperations = listOf("read", "write", "create", "delete")
                                val extraOperations = listOf("submit", "cancel", "amend", "export", "import", "report")
                                Surface(color = colors.surface, shape = RoundedCornerShape(KoraMeasures.dp12), border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant.copy(alpha = .7f))) {
                                    Column(Modifier.fillMaxWidth().padding(KoraMeasures.dp12), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp6)) {
                                        Column(verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp1)) {
                                            Text(doctype.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                            Text(moduleLabel, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                                        }
                                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp5)) {
                                            baseOperations.forEach { operation ->
                                                val allowed = permission?.optBoolean(operation, false) == true
                                                FilterChip(
                                                    selected = allowed,
                                                    onClick = { if (!isLockedRole) vm.updateAccessPermission(doctype.name, operation, !allowed) },
                                                    enabled = !isLockedRole,
                                                    label = { Text(operation.replaceFirstChar { it.uppercase() }) },
                                                    modifier = Modifier.heightIn(min = KoraMeasures.dp36),
                                                )
                                            }
                                        }
                                        TextButton(onClick = { showMore = !showMore }, contentPadding = PaddingValues(horizontal = KoraMeasures.dp0, vertical = KoraMeasures.dp0), modifier = Modifier.heightIn(min = KoraMeasures.dp32)) {
                                            Text(if (showMore) "Less" else "More permissions", style = MaterialTheme.typography.bodySmall)
                                            Icon(if (showMore) Icons.Filled.ArrowDropDown else Icons.Outlined.ChevronRight, null, modifier = Modifier.size(KoraMeasures.dp16))
                                        }
                                        if (showMore) {
                                            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp5)) {
                                                extraOperations.forEach { operation ->
                                                    val allowed = permission?.optBoolean(operation, false) == true
                                                    FilterChip(
                                                        selected = allowed,
                                                        onClick = { if (!isLockedRole) vm.updateAccessPermission(doctype.name, operation, !allowed) },
                                                        enabled = !isLockedRole,
                                                        label = { Text(operation.replaceFirstChar { it.uppercase() }) },
                                                        modifier = Modifier.heightIn(min = KoraMeasures.dp36),
                                                    )
                                                }
                                            }
                                            val ownerOnly = permission?.optBoolean("if_owner", false) == true
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp6)) {
                                                Checkbox(checked = ownerOnly, onCheckedChange = { if (!isLockedRole) vm.updateAccessPermission(doctype.name, "if_owner", it) }, enabled = !isLockedRole, modifier = Modifier.size(KoraMeasures.dp40))
                                                Text("Limit to records owned by this user", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (dirty) Surface(color = colors.onSurface, contentColor = colors.surface) {
                    Row(Modifier.fillMaxWidth().height(KoraMeasures.dp64).padding(horizontal = KoraMeasures.dp16), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                        Text("Unsaved access changes", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        OutlinedButton(onClick = vm::discardAccessChanges, enabled = !state.accessSaving, modifier = Modifier.heightIn(min = KoraMeasures.dp40)) { Text("Discard") }
                        Button(onClick = vm::saveAccessChanges, enabled = !state.accessSaving, modifier = Modifier.heightIn(min = KoraMeasures.dp40)) {
                            if (state.accessSaving) CircularProgressIndicator(Modifier.size(KoraMeasures.dp18), strokeWidth = KoraMeasures.dp2) else Text("Save")
                        }
                    }
                }
            }
            if (addRoleOpen) AlertDialog(
                onDismissRequest = { if (!state.accessSaving) addRoleOpen = false },
                title = { Text("Add role") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp10)) {
                        KoraTextField(newRoleName, { newRoleName = it }, label = "Role name", singleLine = true)
                        KoraTextField(newRoleDescription, { newRoleDescription = it }, label = "Description", singleLine = true)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(newRoleWorkspaceAccess, { newRoleWorkspaceAccess = it }, modifier = Modifier.size(KoraMeasures.dp44))
                            Text("Workspace access", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { vm.createAccessRole(newRoleName, newRoleWorkspaceAccess, newRoleDescription); addRoleOpen = false; newRoleName = ""; newRoleDescription = ""; newRoleWorkspaceAccess = false },
                        enabled = newRoleName.isNotBlank() && !state.accessSaving,
                    ) { Text("Create") }
                },
                dismissButton = { TextButton(onClick = { addRoleOpen = false }, enabled = !state.accessSaving) { Text("Cancel") } },
            )
        }
        "Change history" -> {
            val activeVersion = state.configVersions.firstOrNull { it.optString("status").equals("Active", true) }
            val earlier = state.configVersions.filterNot { it === activeVersion }
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = KoraMeasures.dp16, vertical = KoraMeasures.dp12), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                if (state.configVersionsLoading && state.configVersions.isEmpty()) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
                state.configVersionsError?.let { error ->
                    item {
                        EmptyCard("Could not load history", error)
                        TextButton(onClick = vm::loadConfigVersions) { Text("Retry") }
                    }
                }
                if (!state.configVersionsLoading && state.configVersions.isEmpty() && state.configVersionsError == null) {
                    item { EmptyCard("No configuration history", "Saved configuration versions will appear here.") }
                }
                if (activeVersion != null) item { DrawerGroupLabel("In use") }
                activeVersion?.let { version ->
                    item(key = "active-version-${version.optString("id")}") {
                        Surface(
                            color = colors.surface,
                            shape = RoundedCornerShape(KoraMeasures.dp12),
                            border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, successForeground(colors)),
                        ) {
                            Column(Modifier.fillMaxWidth().padding(KoraMeasures.dp14), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                                    Text("v${version.optInt("version")}", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                    Surface(color = colors.secondaryContainer, shape = RoundedCornerShape(KoraMeasures.dp4)) {
                                        Text("In use", Modifier.padding(horizontal = KoraMeasures.dp8, vertical = KoraMeasures.dp2), style = MaterialTheme.typography.labelSmall, color = colors.onSecondaryContainer)
                                    }
                                    OutlinedButton(onClick = { vm.reviewConfigVersion(version) }, modifier = Modifier.heightIn(min = KoraMeasures.dp40)) { Text("Review") }
                                }
                                Text(version.optString("label").ifBlank { "Configuration update" }, style = MaterialTheme.typography.bodyMedium)
                                Text(formatHistoryDate(version.optString("created_at")), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                            }
                        }
                    }
                }
                if (earlier.isNotEmpty()) item { DrawerGroupLabel("Earlier versions · ${earlier.size}") }
                items(earlier, key = { "version-${it.optString("id")}" }) { version ->
                    Surface(color = colors.surface, shape = RoundedCornerShape(KoraMeasures.dp10), border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant.copy(alpha = .65f))) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = KoraMeasures.dp12, vertical = KoraMeasures.dp10), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp10)) {
                            Text("v${version.optInt("version")}", Modifier.widthIn(min = KoraMeasures.dp36), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp2)) {
                                Text(version.optString("label").ifBlank { "Configuration update" }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(formatHistoryDate(version.optString("created_at")), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                            }
                            var actionsOpen by remember(version.optString("id")) { mutableStateOf(false) }
                            Box {
                                IconButton(onClick = { actionsOpen = true }, modifier = Modifier.size(KoraMeasures.dp44)) { Icon(Icons.Outlined.MoreVert, "Version actions") }
                                DropdownMenu(expanded = actionsOpen, onDismissRequest = { actionsOpen = false }) {
                                    DropdownMenuItem(text = { Text("Review") }, onClick = { actionsOpen = false; vm.reviewConfigVersion(version) })
                                }
                            }
                        }
                    }
                }
            }
            state.configVersionDetail?.let { detail ->
                ModalBottomSheet(onDismissRequest = vm::closeConfigVersionReview, containerColor = colors.surface) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = KoraMeasures.dp16).padding(bottom = KoraMeasures.dp24), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp12)) {
                        Text("Version ${detail.optInt("version")}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        Text(detail.optString("label").ifBlank { "Configuration update" }, style = MaterialTheme.typography.bodyMedium)
                        if (state.configVersionDetailLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
                        val changelog = detail.optString("changelog").takeIf { it.isNotBlank() && it != "null" }
                        if (changelog != null) Text(changelog, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                        else Text("Version snapshot loaded.", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                        Text("Configuration snapshot is available for review.", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    }
                }
            }
        }
        "Insights" -> {
            val catalogModels = state.analyticsCatalog?.optJSONArray("models") ?: JSONArray()
            val models = (0 until catalogModels.length()).mapNotNull(catalogModels::optJSONObject)
            val measureCount = models.sumOf { model -> model.optJSONArray("measures")?.length() ?: 0 }
            var search by rememberSaveable { mutableStateOf("") }
            var selectedType by rememberSaveable { mutableStateOf("All") }
            val metricTypes = listOf("All", "Counts", "Totals", "Trends")
            fun measureType(measure: JSONObject, model: JSONObject): String {
                val name = measure.optString("name").lowercase()
                val format = measure.optString("format").lowercase()
                return when {
                    format == "currency" || name.contains("sum") || name.contains("total") -> "Totals"
                    name.contains("count") -> "Counts"
                    model.optString("time_dimension").isNotBlank() -> "Trends"
                    else -> "Counts"
                }
            }
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = KoraMeasures.dp16, vertical = KoraMeasures.dp12),
                verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp12),
            ) {
                item(key = "insights-stats") {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                        listOf("Metric models" to models.size.toString(), "Measures" to measureCount.toString(), "Reports" to state.savedReports.size.toString()).forEach { (label, value) ->
                            Surface(Modifier.weight(1f), color = colors.surface, shape = RoundedCornerShape(KoraMeasures.dp12), border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant.copy(alpha = .7f))) {
                                Column(Modifier.padding(KoraMeasures.dp10), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp4)) {
                                    Text(label, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(value, style = MaterialTheme.typography.titleMedium.copy(fontSize = KoraMeasures.sp20), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
                if (state.reportLoading && state.analyticsCatalog == null) item(key = "insights-loading") { LinearProgressIndicator(Modifier.fillMaxWidth()) }
                state.reportError?.let { error -> item(key = "insights-error") { MessageBanner("Insights unavailable: $error", warning = true) } }
                if (!state.reportLoading && state.analyticsCatalog == null && state.reportError == null) {
                    item(key = "insights-unavailable") { EmptyCard("Insights aren’t available", "This account does not currently expose a readable metric catalog.") }
                }
                if (state.analyticsCatalog != null && models.isEmpty()) {
                    item(key = "insights-no-models") { EmptyCard("No metric models", "Readable metrics will appear here when a record type exposes analytics data.") }
                }
                if (models.isNotEmpty()) {
                    item(key = "insights-search") {
                        KoraTextField(search, { search = it }, Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Outlined.Search, null) }, placeholder = { Text("Search metrics or record types") })
                    }
                    item(key = "insights-filters") {
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp6)) {
                            metricTypes.forEach { type -> FilterChip(selected = selectedType == type, onClick = { selectedType = type }, label = { Text(type) }) }
                        }
                    }
                    models.forEach { model ->
                        val measures = model.optJSONArray("measures") ?: JSONArray()
                        val visibleMeasures = (0 until measures.length()).mapNotNull(measures::optJSONObject).filter { measure ->
                            (selectedType == "All" || measureType(measure, model) == selectedType) &&
                                (search.isBlank() || measure.optString("label").contains(search, true) || measure.optString("name").contains(search, true) || model.optString("label").contains(search, true))
                        }
                        if (visibleMeasures.isNotEmpty() || (search.isNotBlank() && model.optString("label").contains(search, true))) {
                            item(key = "insights-group-${model.optString("name")}") {
                                var expanded by rememberSaveable(model.optString("name")) { mutableStateOf(true) }
                                Surface(color = colors.surface, shape = RoundedCornerShape(KoraMeasures.dp12), border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant.copy(alpha = .7f))) {
                                    Column {
                                        Row(Modifier.fillMaxWidth().heightIn(min = KoraMeasures.dp48).clickable { expanded = !expanded }.padding(horizontal = KoraMeasures.dp14), verticalAlignment = Alignment.CenterVertically) {
                                            Text(model.optString("label").ifBlank { model.optString("name") }, Modifier.weight(1f), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                            Text("${visibleMeasures.size} ${if (visibleMeasures.size == 1) "measure" else "measures"}", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                                            Icon(if (expanded) Icons.Filled.ArrowDropDown else Icons.Outlined.ChevronRight, null, tint = colors.onSurfaceVariant)
                                        }
                                        if (expanded) visibleMeasures.forEachIndexed { index, measure ->
                                            if (index > 0) Divider(color = colors.outlineVariant.copy(alpha = .55f))
                                            Row(Modifier.fillMaxWidth().heightIn(min = KoraMeasures.dp44).clickable { vm.selectAnalyticsMeasure(model.optString("name"), measure.optString("name")) }.padding(horizontal = KoraMeasures.dp14, vertical = KoraMeasures.dp8), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                                                val metricLabel = measure.optString("label").ifBlank { measure.optString("name") }
                                                    .replace(Regex("(?i)^total\\s+total\\b"), "Total")
                                                Text(metricLabel, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Surface(color = colors.secondaryContainer, shape = RoundedCornerShape(KoraMeasures.dp4)) {
                                                    Text(measureType(measure, model), Modifier.padding(horizontal = KoraMeasures.dp7, vertical = KoraMeasures.dp2), style = MaterialTheme.typography.labelSmall, color = colors.onSecondaryContainer)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (models.all { model -> (model.optJSONArray("measures")?.length() ?: 0) == 0 }) {
                        item(key = "insights-empty-measures") { EmptyCard("No measures available", "Readable measures will appear here when record types expose analytics fields.") }
                    }
                }
            }
        }
        else -> EmptyCard("Not available in this app yet", "This workspace exposes this setting, but its native mobile editor is not implemented yet.")
    }
}

@Composable
private fun KoraSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val thumbOffset by animateDpAsState(if (checked) KoraMeasures.dp18 else KoraMeasures.dp0, label = "switch-thumb")
    Box(
        modifier
            .size(KoraTokens.TouchTarget)
            .semantics { contentDescription = "Allow Kori in the app" }
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .width(KoraMeasures.dp44)
                .height(KoraMeasures.dp26)
                .background(if (checked) colors.primary else colors.outlineVariant, RoundedCornerShape(KoraMeasures.dp13)),
        )
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .padding(start = KoraMeasures.dp3)
                .offset(x = thumbOffset)
                .size(KoraMeasures.dp20)
                .shadow(KoraMeasures.dp1, CircleShape)
                .background(colors.surface, CircleShape),
        )
    }
}

@Composable
private fun AIConnectionSettingsScreen(state: AppState, vm: AppViewModel, onVoiceInput: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = KoraMeasures.dp12),
    ) {
        item {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = KoraMeasures.dp16, vertical = KoraMeasures.dp16),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp8),
            ) {
                KoriMascot(
                    Modifier.size(KoraMeasures.dp72),
                    colors.primary,
                    if (state.offline) KoriMood.Offline else KoriMood.Ready,
                )
                Text(
                    if (state.offline) "Kori is offline" else "Kori is connected",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = KoraMeasures.sp18, lineHeight = KoraMeasures.sp24),
                    fontWeight = FontWeight.SemiBold,
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp6)) {
                    Box(Modifier.size(KoraMeasures.dp8).background(if (state.offline) warningForeground(colors) else successForeground(colors), CircleShape))
                    Text("${state.user?.roles?.joinToString().orEmpty().ifBlank { "Your account" }} · uses your role access", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth().background(colors.surface).clickable { vm.setKoriEnabledInApp(!state.koriEnabledInApp) }.padding(horizontal = KoraMeasures.dp16, vertical = KoraMeasures.dp12),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp12),
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp2)) {
                    Text("Allow Kori in the app", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Text("Show the mascot in the bottom bar", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
                KoraSwitch(checked = state.koriEnabledInApp, onCheckedChange = vm::setKoriEnabledInApp)
            }
            HorizontalDivider(color = colors.outlineVariant)
        }
        item { DrawerGroupLabel("How to reach Kori") }
        item { AssistantHelpRow("Tap the mascot", "Bottom bar, on every screen") }
        item { AssistantHelpRow("Ask about a record", "Open a record, then choose Ask Kori from its actions") }
        item { AssistantHelpRow("Press and hold the mascot", "Speak to Kori", onClick = onVoiceInput) }
        item { DrawerGroupLabel("Access") }
        item {
            AssistantNavigationRow(
                title = "Roles and permissions",
                subtitle = "Kori can only do what its assigned roles allow",
                colors = colors,
                onClick = { vm.openSettingsPage("AI agents") },
            )
        }
        item {
            AssistantNavigationRow(
                title = "Connect another assistant",
                subtitle = "Manage assistant connections in Integrations",
                colors = colors,
                onClick = { vm.openSettingsPage("Integrations") },
            )
        }
    }
}

@Composable
private fun AssistantHelpRow(title: String, subtitle: String, onClick: (() -> Unit)? = null) {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().background(colors.surface).then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(horizontal = KoraMeasures.dp16, vertical = KoraMeasures.dp12), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp2)) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
    }
    HorizontalDivider(color = colors.outlineVariant)
}

@Composable
private fun AssistantNavigationRow(title: String, subtitle: String, colors: androidx.compose.material3.ColorScheme, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(colors.surface).clickable(onClick = onClick).padding(horizontal = KoraMeasures.dp16, vertical = KoraMeasures.dp12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp10),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp2)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
        Icon(Icons.Outlined.ChevronRight, contentDescription = "Open $title", tint = colors.onSurfaceVariant)
    }
    HorizontalDivider(color = colors.outlineVariant)
}

@Composable
private fun AgentPolicyEditor(state: AppState, vm: AppViewModel) {
    val colors = MaterialTheme.colorScheme
    val policy = state.selectedAgentPolicy ?: return
    val dirty = state.savedAgentPolicySnapshot == null || policy.toString() != state.savedAgentPolicySnapshot
    var rolesMenuOpen by remember(policy.optString("id")) { mutableStateOf(false) }
    val selectedRoles = policy.optJSONArray("roles")?.let { json ->
        (0 until json.length()).mapNotNull { index -> json.optString(index).takeIf(String::isNotBlank) }
    }.orEmpty().toSet()
    val availableRoles = state.accessRoles.mapNotNull { role -> role.optString("name").takeIf(String::isNotBlank) }.distinct()
    val grantedPermissions = state.agentEffectivePermissions.filter { it.optBoolean("allowed") || it.optBoolean("approval_required") }
    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = KoraMeasures.dp16, vertical = KoraMeasures.dp12),
            verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp14),
        ) {
            item {
                Surface(color = colors.surface, shape = RoundedCornerShape(KoraTokens.RadiusCard), border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant)) {
                    Column(Modifier.fillMaxWidth().padding(KoraMeasures.dp14), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp4)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(policy.optString("name").ifBlank { "Agent policy" }, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Surface(color = colors.secondaryContainer, shape = RoundedCornerShape(KoraMeasures.dp4)) {
                                Text(if (state.savedAgentPolicySnapshot == null) "Draft" else "Saved", Modifier.padding(horizontal = KoraMeasures.dp8, vertical = KoraMeasures.dp3), style = MaterialTheme.typography.labelSmall, color = colors.onSecondaryContainer)
                            }
                        }
                        Text("Controls what it can read and change.", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                    Text("Assigned roles", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                    Box {
                        OutlinedButton(
                            onClick = { rolesMenuOpen = true },
                            modifier = Modifier.fillMaxWidth().heightIn(min = KoraMeasures.dp48),
                            shape = RoundedCornerShape(KoraTokens.RadiusControl),
                            contentPadding = PaddingValues(horizontal = KoraMeasures.dp12),
                        ) {
                            Text("Add a role", Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Start, color = colors.onSurfaceVariant)
                            Icon(Icons.Filled.ArrowDropDown, null)
                        }
                        DropdownMenu(expanded = rolesMenuOpen, onDismissRequest = { rolesMenuOpen = false }) {
                            availableRoles.filterNot { it in selectedRoles }.forEach { role ->
                                DropdownMenuItem(text = { Text(role) }, onClick = {
                                    vm.updateAgentRoles((selectedRoles + role).toList())
                                    rolesMenuOpen = false
                                })
                            }
                            if (availableRoles.isEmpty()) DropdownMenuItem(text = { Text("No roles available") }, onClick = { rolesMenuOpen = false }, enabled = false)
                        }
                    }
                    if (selectedRoles.isNotEmpty()) Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                        selectedRoles.forEach { role ->
                            FilterChip(selected = true, onClick = { vm.updateAgentRoles((selectedRoles - role).toList()) }, label = { Text("$role  ×", maxLines = 1) })
                        }
                    }
                    if (state.accessRoles.isEmpty()) Text("No roles are available to assign.", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
            item {
                Surface(color = colors.secondaryContainer, shape = RoundedCornerShape(KoraMeasures.dp10)) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = KoraMeasures.dp12, vertical = KoraMeasures.dp10), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                        Box(Modifier.size(KoraMeasures.dp8).background(successForeground(colors), CircleShape))
                        Text("New actions stay blocked until you allow them.", style = MaterialTheme.typography.bodySmall, color = colors.onSecondaryContainer)
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                    Text("Effective permissions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    when {
                        state.agentPolicyLoading -> LinearProgressIndicator(Modifier.fillMaxWidth())
                        grantedPermissions.isEmpty() -> KoraEmptyState("No permissions yet", "Assign a role to see what it can do.")
                        else -> grantedPermissions.take(12).forEach { permission ->
                            Surface(color = colors.surface, shape = RoundedCornerShape(KoraTokens.RadiusCard), border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant)) {
                                Row(Modifier.fillMaxWidth().padding(horizontal = KoraMeasures.dp12, vertical = KoraMeasures.dp10), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                                    Column(Modifier.weight(1f)) {
                                        Text(permission.optString("doctype"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                                        Text(permission.optString("operation").replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                                    }
                                    val status = if (permission.optBoolean("approval_required")) "Approval" else "Allowed"
                                    Surface(color = if (permission.optBoolean("approval_required")) colors.tertiaryContainer else colors.secondaryContainer, shape = RoundedCornerShape(KoraMeasures.dp4)) {
                                        Text(status, Modifier.padding(horizontal = KoraMeasures.dp8, vertical = KoraMeasures.dp3), style = MaterialTheme.typography.labelSmall, color = if (permission.optBoolean("approval_required")) colors.onTertiaryContainer else colors.onSecondaryContainer)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                    Text("Recent runs", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    if (state.agentRuns.isEmpty() && !state.agentPolicyLoading) KoraEmptyState("No activity yet", "Agent activity will appear here when available.")
                    state.agentRuns.take(3).forEach { run ->
                        Surface(color = colors.surface, shape = RoundedCornerShape(KoraTokens.RadiusCard), border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant)) {
                            Row(Modifier.fillMaxWidth().padding(KoraMeasures.dp12), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                                Column(Modifier.weight(1f)) {
                                    Text(listOf(run.optString("doctype"), run.optString("operation")).filter(String::isNotBlank).joinToString(" · ").ifBlank { run.optString("id") }, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                                    Text(run.optString("created_at"), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                                }
                                Text(run.optString("status").ifBlank { "Unknown" }, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            state.agentPolicyError?.let { error -> item { MessageBanner(error, warning = true) } }
        }
        Surface(color = colors.surface, border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant.copy(alpha = .7f))) {
            Column(Modifier.fillMaxWidth().padding(horizontal = KoraMeasures.dp16, vertical = KoraMeasures.dp12)) {
                Button(onClick = vm::saveAgentPolicy, enabled = dirty && !state.agentPolicySaving, modifier = Modifier.fillMaxWidth().height(KoraMeasures.dp48), shape = RoundedCornerShape(KoraTokens.RadiusControl)) {
                    if (state.agentPolicySaving) CircularProgressIndicator(Modifier.size(KoraMeasures.dp18), strokeWidth = KoraMeasures.dp2) else Text("Save agent policy")
                }
            }
        }
    }
}

@Composable
private fun SettingsResourceScreen(page: String, state: AppState, vm: AppViewModel) {
    val colors = MaterialTheme.colorScheme
    val records = state.settingsResources[page].orEmpty()
    val loading = state.settingsResourcesLoading == page
    val error = state.settingsResourcesError[page]
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = KoraMeasures.dp16, vertical = KoraMeasures.dp12),
        verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp10),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(page, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                TextButton(onClick = { vm.loadSettingsResource(page) }, modifier = Modifier.heightIn(min = KoraMeasures.dp40)) { Text("Refresh") }
            }
            if (page == "Credentials") Text("Stored values are never shown here.", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
        if (loading && records.isEmpty()) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        error?.let { message ->
            item {
                EmptyCard("Could not load $page", message)
                TextButton(onClick = { vm.loadSettingsResource(page) }) { Text("Retry") }
            }
        }
        if (!loading && records.isEmpty() && error == null) {
            val description = when (page) {
                "Scripts" -> "Workspace scripts will appear here when they are configured."
                "Integrations" -> "Connected extensions will appear here when they are configured."
                "Credentials" -> "Credential names will appear here when the workspace has stored secrets. Secret values remain hidden."
                else -> "Page manifests available to this account will appear here."
            }
            item { EmptyCard("No ${page.lowercase()} available", description) }
        }
        items(records, key = { "${page}-${it.optString("name").ifBlank { it.optString("key_name") }}" }) { record ->
            val title = when (page) {
                "Credentials" -> record.optString("key_name")
                "Integrations" -> record.optString("display_name").ifBlank { record.optString("name") }
                else -> record.optString("name")
            }.ifBlank { "Untitled" }
            val secondary = when (page) {
                "Scripts" -> listOfNotNull(
                    record.optString("script_type").takeIf(String::isNotBlank),
                    record.optString("doctype").takeIf(String::isNotBlank),
                    record.optString("event").takeIf(String::isNotBlank),
                ).joinToString(" · ")
                "Integrations" -> listOfNotNull(
                    record.optString("description").takeIf(String::isNotBlank),
                    record.optString("endpoint_url").takeIf(String::isNotBlank),
                ).joinToString(" · ")
                "Credentials" -> "Updated ${formatHistoryDate(record.optString("updated_at"))}"
                else -> listOfNotNull(
                    record.optString("route").takeIf(String::isNotBlank),
                    record.optString("package").takeIf(String::isNotBlank),
                ).joinToString(" · ")
            }
            val active = when {
                record.has("is_active") -> record.optBoolean("is_active")
                record.optString("status").isNotBlank() -> record.optString("status").equals("active", true)
                else -> false
            }
            Surface(color = colors.surface, shape = RoundedCornerShape(KoraMeasures.dp12), border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant.copy(alpha = .75f))) {
                Row(Modifier.fillMaxWidth().padding(horizontal = KoraMeasures.dp14, vertical = KoraMeasures.dp12), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp10)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp4)) {
                        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (secondary.isNotBlank()) Text(secondary, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                    if (page != "Credentials") {
                        Surface(color = if (active) colors.secondaryContainer else colors.surfaceVariant, shape = RoundedCornerShape(KoraMeasures.dp4)) {
                            Text(if (active) "Active" else "Inactive", Modifier.padding(horizontal = KoraMeasures.dp8, vertical = KoraMeasures.dp3), style = MaterialTheme.typography.labelSmall, color = if (active) colors.onSecondaryContainer else colors.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

private fun parseColorOr(raw: String, fallback: Color): Color = runCatching {
    val hex = raw.removePrefix("#")
    Color(android.graphics.Color.parseColor("#$hex"))
}.getOrDefault(fallback)

private fun warningContainer(colors: androidx.compose.material3.ColorScheme): Color =
    if (colors.background.red < 0.5f) KoraTokens.WarningBgDark else KoraTokens.WarningBg

private fun warningForeground(colors: androidx.compose.material3.ColorScheme): Color =
    if (colors.background.red < 0.5f) KoraTokens.WarningTextDark else KoraTokens.WarningText

private fun successForeground(colors: androidx.compose.material3.ColorScheme): Color =
    if (colors.background.red < 0.5f) KoraTokens.SuccessTextDark else KoraTokens.Success

private fun warningBorder(colors: androidx.compose.material3.ColorScheme): Color =
    if (colors.background.red < 0.5f) KoraTokens.WarningBorderDark else KoraTokens.WarningBorder

private fun colorContrastRatio(first: Color, second: Color): Double {
    fun linear(channel: Float): Double {
        val value = channel.toDouble()
        return if (value <= .04045) value / 12.92 else Math.pow((value + .055) / 1.055, 2.4)
    }
    fun luminance(color: Color) = .2126 * linear(color.red) + .7152 * linear(color.green) + .0722 * linear(color.blue)
    val light = maxOf(luminance(first), luminance(second))
    val dark = minOf(luminance(first), luminance(second))
    return (light + .05) / (dark + .05)
}

private fun formatHistoryDate(value: String): String = runCatching {
    formatHumanDate(value, dateOnly = false)
}.getOrDefault(value.ifBlank { "Date unavailable" })

@Composable
private fun ModuleHubScreen(state: AppState, vm: AppViewModel) {
    val module = state.navigation?.modules?.firstOrNull { it.module == state.selectedModule } ?: return
    var query by rememberSaveable(module.module) { mutableStateOf("") }
    val recordTypes = module.doctypes.filter { item ->
        query.isBlank() || item.label.contains(query.trim(), true) || item.name.contains(query.trim(), true)
    }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
    Column(Modifier.fillMaxSize().padding(horizontal = KoraMeasures.dp20)) {
        Row(Modifier.fillMaxWidth().padding(top = KoraMeasures.dp8, bottom = KoraMeasures.dp12), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(module.label, style = androidx.compose.material3.MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("${module.doctypes.size} ${if (module.doctypes.size == 1) "record type" else "record types"}", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        module.description?.let { Text(it, Modifier.padding(bottom = KoraMeasures.dp12), style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) }
        if (module.doctypes.size > 8) KoraTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(bottom = KoraMeasures.dp12),
            singleLine = true,
            leadingIcon = { Icon(Icons.Outlined.Search, null) },
            placeholder = { Text("Search ${module.label.lowercase()}") },
        )
        if (module.doctypes.isEmpty()) EmptyCard("No record types available", "Your account does not currently have access to record types in this module.")
        else if (recordTypes.isEmpty()) EmptyCard("No matching record types", "Try a different search.")
        else LazyColumn(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp8), contentPadding = PaddingValues(bottom = KoraMeasures.dp18)) {
            items(recordTypes, key = { it.resourceName }) { item -> DocTypeRow(item) { vm.openDoctype(item) } }
        }
    }
}

@Composable
private fun WorkScreen(state: AppState, vm: AppViewModel) {
    var query by remember { mutableStateOf("") }
    val normalizedQuery = query.trim().lowercase()
    val modules = state.navigation?.modules.orEmpty().mapNotNull { module ->
        val moduleMatches = module.label.contains(normalizedQuery, true) || module.module.contains(normalizedQuery, true)
        val types = module.doctypes.filter { moduleMatches || it.label.contains(normalizedQuery, true) || it.name.contains(normalizedQuery, true) }
        if (types.isEmpty()) null else module.copy(doctypes = types)
    }
    Column(Modifier.fillMaxSize().padding(horizontal = KoraMeasures.dp20)) {
        Row(Modifier.fillMaxWidth().padding(top = KoraMeasures.dp8, bottom = KoraMeasures.dp12), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Browse", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("${modules.sumOf { it.doctypes.size }} record types across ${modules.size} areas", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        KoraTextField(
            value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth().padding(bottom = KoraMeasures.dp14),
            singleLine = true, leadingIcon = { Icon(Icons.Outlined.Search, null) },
            placeholder = { Text("Search records and areas") }
        )
        if (modules.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                EmptyCard(if (normalizedQuery.isBlank()) "Workspace is not loaded" else "No matching records", if (normalizedQuery.isBlank()) "Connect to Kora to load your workspace." else "Try another record type or area name.")
            }
        } else LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp4), contentPadding = PaddingValues(bottom = KoraMeasures.dp16)) {
            items(modules, key = { it.module }) { module ->
                val colors = androidx.compose.material3.MaterialTheme.colorScheme
                val previewTypes = module.doctypes.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label }).take(6)
                Column(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = KoraMeasures.dp48).clickable { vm.openModule(module.module) }.padding(horizontal = KoraMeasures.dp4),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(moduleIcon(module.icon), null, tint = colors.onSurfaceVariant, modifier = Modifier.size(KoraMeasures.dp19))
                        Spacer(Modifier.width(KoraMeasures.dp11))
                        Text(module.label, Modifier.weight(1f), fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${module.doctypes.size}", color = colors.onSurfaceVariant, style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
                        Icon(Icons.Outlined.ChevronRight, "Open ${module.label}", tint = colors.onSurfaceVariant, modifier = Modifier.padding(start = KoraMeasures.dp4))
                    }
                    previewTypes.forEach { item ->
                        Row(
                            Modifier.fillMaxWidth().heightIn(min = KoraMeasures.dp44).clickable { vm.openDoctype(item) }.padding(start = KoraMeasures.dp30, end = KoraMeasures.dp4),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(item.label, Modifier.weight(1f), style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Icon(Icons.Outlined.ChevronRight, null, tint = colors.onSurfaceVariant)
                        }
                    }
                    if (module.doctypes.size > previewTypes.size) {
                        TextButton(onClick = { vm.openModule(module.module) }, modifier = Modifier.padding(start = KoraMeasures.dp26)) {
                            Text("View all ${module.label} (${module.doctypes.size})")
                        }
                    }
                    Divider(color = colors.outlineVariant.copy(alpha = .55f))
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
private fun RecordListScreen(state: AppState, vm: AppViewModel) {
    val doctype = state.selectedDoctype ?: return
    val collection = RecordCollectionConfig.from(state.doctypeManifest, doctype.label)
    val displayFields = state.schemaFields.filterNot { field -> field.hidden || field.fieldtype in setOf("Section Break", "Column Break", "Heading", "Table", "JSON", "Password") }
    val readableFields = state.schemaFields.filterNot { field -> field.hidden || field.fieldtype in setOf("Section Break", "Column Break", "Heading", "JSON", "Password") }
    val inferredColumns = listOf(state.titleField, "name") + displayFields.filter { it.inListView }.map { it.fieldname }
    val listColumns = (if (displayFields.none { it.inListView }) listOf(state.titleField, "name") + displayFields.take(3).map { it.fieldname } else inferredColumns).distinct()
    val summaryColumns = displayFields.filter { field ->
        field.fieldtype.equals("Currency", true) || field.fieldtype.equals("Date", true) || field.fieldtype.equals("Datetime", true) || field.fieldtype.equals("DateTime", true) ||
            field.label.contains("payment", true) || field.label.contains("date", true) || field.label.contains("time", true)
    }.map { it.fieldname }
    val mobileColumns = if (collection.mobileColumns.isNotEmpty()) {
        collection.mobileColumns.distinct()
    } else {
        (listColumns + summaryColumns).distinct()
    }
    val titleField = state.titleField
    val defaultSort = displayFields.firstOrNull { field ->
        field.fieldtype.equals("Datetime", true) || field.fieldtype.equals("DateTime", true) ||
            field.fieldtype.equals("Date", true)
    }?.fieldname ?: titleField
    var filters by remember(doctype.resourceName) { mutableStateOf<Map<String, String>>(emptyMap()) }
    var selectedIds by remember(doctype.resourceName) { mutableStateOf<Set<String>>(emptySet()) }
    var selectionModeActive by remember(doctype.resourceName) { mutableStateOf(false) }
    var filterOpen by remember(doctype.resourceName) { mutableStateOf(false) }
    var exportOpen by remember(doctype.resourceName) { mutableStateOf(false) }
    var exportFormat by rememberSaveable(doctype.resourceName) { mutableStateOf("CSV") }
    var moreOpen by remember(doctype.resourceName) { mutableStateOf(false) }
    var lowStockOnly by remember(doctype.resourceName) { mutableStateOf(false) }
    var deleteConfirmOpen by remember(doctype.resourceName) { mutableStateOf(false) }
    var sortField by remember(doctype.resourceName, defaultSort) { mutableStateOf(defaultSort) }
    var ascending by remember(doctype.resourceName, defaultSort) { mutableStateOf(defaultSort == titleField) }
    val filterFields = displayFields.filter { field ->
        val distinctValues = state.records.map { it.displayValue(field.fieldname) }.filter(String::isNotBlank).distinct().size
        val primaryTitle = field.fieldname == state.titleField || field.fieldname.equals("name", ignoreCase = true)
        val categorical = field.fieldtype in setOf("Select", "Link", "Dynamic Link", "Check", "Status") ||
            (field.fieldtype == "Data" && !primaryTitle)
        val explicitlyCategorical = field.fieldtype in setOf("Select", "Link", "Dynamic Link", "Check", "Status")
        !primaryTitle && distinctValues in 1..12 && (
            field.inStandardFilter || categorical && (explicitlyCategorical || distinctValues < state.records.size)
        )
    }
    val visibleRecords = state.records.filter { record ->
        (state.listSearch.isBlank() || (mobileColumns + titleField + "name").distinct().any { column -> record.displayValue(column).contains(state.listSearch.trim(), ignoreCase = true) }) &&
            filters.all { (field, value) -> record.displayValue(field) == value }
    }.sortedWith { left, right ->
        val field = state.schemaFields.firstOrNull { it.fieldname == sortField }
        val a = left.opt(sortField)?.takeUnless { it == JSONObject.NULL }
        val b = right.opt(sortField)?.takeUnless { it == JSONObject.NULL }
        val comparison = when {
            field?.fieldtype in setOf("Int", "Float", "Currency", "Percent") ->
                ((a?.toString()?.toDoubleOrNull()) ?: Double.NEGATIVE_INFINITY).compareTo((b?.toString()?.toDoubleOrNull()) ?: Double.NEGATIVE_INFINITY)
            field?.fieldtype.equals("Date", true) || field?.fieldtype.equals("Datetime", true) || field?.fieldtype.equals("DateTime", true) -> {
                val aDate = parseLocalDateTime(a?.toString())?.atZone(ZoneId.systemDefault())?.toInstant()
                    ?: runCatching { LocalDate.parse(a?.toString()).atStartOfDay(ZoneId.systemDefault()).toInstant() }.getOrNull()
                val bDate = parseLocalDateTime(b?.toString())?.atZone(ZoneId.systemDefault())?.toInstant()
                    ?: runCatching { LocalDate.parse(b?.toString()).atStartOfDay(ZoneId.systemDefault()).toInstant() }.getOrNull()
                when { aDate == null && bDate == null -> 0; aDate == null -> -1; bDate == null -> 1; else -> aDate.compareTo(bDate) }
            }
            else -> (a?.toString().orEmpty()).compareTo(b?.toString().orEmpty(), ignoreCase = true)
        }
        if (ascending) comparison else -comparison
    }
    val statusField = state.schemaFields.firstOrNull { it.fieldname == "status" }?.fieldname
    val stockField = displayFields.firstOrNull { field ->
        field.fieldtype in setOf("Int", "Float", "Currency", "Percent") && isStockQuantityField(field.fieldname, field.label)
    }?.fieldname
    val categoryField = displayFields.firstOrNull { it.fieldname in setOf("category", "category_name") }?.fieldname
    val stockThresholdField = displayFields.firstOrNull { field ->
        field.fieldtype in setOf("Int", "Float", "Currency") &&
            isStockThresholdField(field.fieldname, field.label)
    }
    val lowStockLabel = if (stockThresholdField == null) "Out of stock" else "Low stock"
    val isLowStockRecord: (JSONObject) -> Boolean = { record ->
        val available = stockField?.let { record.opt(it)?.toString()?.toDoubleOrNull() }
        val threshold = stockThresholdField?.let { record.opt(it.fieldname)?.toString()?.toDoubleOrNull() } ?: 0.0
        available != null && available <= threshold
    }
    val selectionMode = selectionModeActive || selectedIds.isNotEmpty()
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    var pendingExport by remember { mutableStateOf<PendingExport?>(null) }
    val createExportDocument = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri ->
        val pending = pendingExport
        if (uri != null && pending != null) {
            runCatching { context.contentResolver.openOutputStream(uri)?.use { it.write(pending.bytes) } ?: error("Could not open the selected location") }
                .onFailure { android.widget.Toast.makeText(context, it.message ?: "Could not export records", android.widget.Toast.LENGTH_LONG).show() }
        }
        pendingExport = null
        exportOpen = false
    }
    LaunchedEffect(selectionMode, selectedIds.size) { vm.updateListSelection(selectionMode, selectedIds.size) }
    val filteredRecords = visibleRecords.filter { record ->
        !lowStockOnly || isLowStockRecord(record)
    }
    Column(Modifier.fillMaxSize()) {
        state.message?.takeUnless(::isPassiveConnectionMessage)?.let { message ->
            Box(Modifier.fillMaxWidth().padding(horizontal = KoraMeasures.dp16)) { MessageBanner(message, false) }
        }
        if (selectionMode) {
            Row(Modifier.fillMaxWidth().background(colors.surface).padding(horizontal = KoraMeasures.dp16, vertical = KoraMeasures.dp4), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    TextButton(
                        onClick = { selectedIds = if (selectedIds.size == filteredRecords.size) emptySet() else filteredRecords.map { it.displayValue("_local_id").ifBlank { it.displayValue("name") } }.toSet() },
                        modifier = Modifier.heightIn(min = KoraTokens.TouchTarget),
                        contentPadding = PaddingValues(horizontal = KoraMeasures.dp0),
                    ) { Text("Select all", color = colors.primary, fontWeight = FontWeight.SemiBold) }
                }
                TextButton(onClick = { selectionModeActive = false; selectedIds = emptySet() }, modifier = Modifier.heightIn(min = KoraTokens.TouchTarget), contentPadding = PaddingValues(horizontal = KoraMeasures.dp0)) { Text("Cancel", color = colors.onSurfaceVariant) }
            }
            Divider(color = colors.outlineVariant)
        } else {
        Row(Modifier.fillMaxWidth().background(colors.surface).padding(horizontal = KoraMeasures.dp16).padding(top = KoraMeasures.dp8, bottom = KoraMeasures.dp6), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(KoraTokens.SpaceS)) {
            KoraTextField(
                value = state.listSearch, onValueChange = vm::setListSearch,
                modifier = Modifier.weight(1f).height(KoraTokens.FieldHeight), singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.Search, null) }, placeholder = { Text("Search records") },
            )
            IconButton(onClick = { exportOpen = true }, modifier = Modifier.size(KoraTokens.TouchTarget).border(KoraMeasures.dp1, colors.outline, RoundedCornerShape(KoraTokens.RadiusControl))) {
                Icon(Icons.Outlined.Download, "Export current view", tint = colors.onSurface)
            }
            Box {
                IconButton(onClick = { moreOpen = true }, modifier = Modifier.size(KoraTokens.TouchTarget).border(KoraMeasures.dp1, colors.outline, RoundedCornerShape(KoraTokens.RadiusControl))) {
                    Icon(Icons.Outlined.MoreVert, "More list actions", tint = colors.onSurface)
                }
                DropdownMenu(expanded = moreOpen, onDismissRequest = { moreOpen = false }) {
                    DropdownMenuItem(text = { Text("Export current view") }, onClick = { moreOpen = false; exportOpen = true })
                    DropdownMenuItem(text = { Text(if (selectionMode) "Cancel selection" else "Select records") }, onClick = { moreOpen = false; selectionModeActive = !selectionMode; if (selectionMode) selectedIds = emptySet() })
                }
            }
        }
        if (!selectionMode) Row(Modifier.fillMaxWidth().background(colors.surface).horizontalScroll(rememberScrollState()).padding(horizontal = KoraMeasures.dp16).padding(top = KoraMeasures.dp2, bottom = KoraMeasures.dp4), horizontalArrangement = Arrangement.spacedBy(KoraTokens.SpaceS), verticalAlignment = Alignment.CenterVertically) {
            KoraChip(
                "Filter${if (filters.isNotEmpty()) " ${filters.size}" else ""}",
                filters.isNotEmpty() || lowStockOnly,
                leadingContent = {
                    val iconColor = MaterialTheme.colorScheme.onSurface
                    Canvas(Modifier.size(KoraMeasures.dp18)) {
                        val stroke = KoraMeasures.dp2.toPx() * 0.8f
                        drawLine(iconColor, androidx.compose.ui.geometry.Offset(0f, size.height * 0.25f), androidx.compose.ui.geometry.Offset(size.width, size.height * 0.25f), stroke)
                        drawLine(iconColor, androidx.compose.ui.geometry.Offset(0f, size.height * 0.5f), androidx.compose.ui.geometry.Offset(size.width * 0.7f, size.height * 0.5f), stroke)
                        drawLine(iconColor, androidx.compose.ui.geometry.Offset(0f, size.height * 0.75f), androidx.compose.ui.geometry.Offset(size.width * 0.42f, size.height * 0.75f), stroke)
                    }
                },
            ) { filterOpen = true }
            KoraChip("${sentenceCaseUiLabel(readableFields.firstOrNull { it.fieldname == sortField }?.label ?: "Name")} ${if (ascending) "↑" else "↓"}", false) { filterOpen = true }
            filters.forEach { (fieldName, value) ->
                val fieldLabel = sentenceCaseUiLabel(readableFields.firstOrNull { it.fieldname == fieldName }?.label ?: fieldName)
                KoraChip("$fieldLabel: $value ×", false) { filters = filters - fieldName }
            }
            if (lowStockOnly) KoraChip("Low stock ×", false) { lowStockOnly = false }
        }
        if (!selectionMode) Row(Modifier.fillMaxWidth().background(colors.surface).padding(horizontal = KoraMeasures.dp16).padding(top = KoraMeasures.dp2, bottom = KoraMeasures.dp6), verticalAlignment = Alignment.CenterVertically) {
            Text("${filteredRecords.size} ${if (filteredRecords.size == 1) "record" else "records"}", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                TextButton(onClick = { selectionModeActive = true }, modifier = Modifier.heightIn(min = KoraTokens.TouchTarget)) { Text("Select", color = colors.primary, fontWeight = FontWeight.SemiBold) }
        }
        if (!selectionMode) Divider(color = colors.outlineVariant)
        }
        if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when {
                state.records.isEmpty() && !state.busy -> Box(Modifier.fillMaxSize().padding(horizontal = KoraMeasures.dp16), contentAlignment = Alignment.TopCenter) { EmptyCard("No ${doctype.label.lowercase()} yet", if (state.offline) "This list has not been cached yet. Connect to load records." else "Create a record to get started.") }
                filteredRecords.isEmpty() && !state.busy -> Box(Modifier.fillMaxSize().padding(horizontal = KoraMeasures.dp16), contentAlignment = Alignment.TopCenter) { EmptyCard("No matching records", "Clear or adjust your search and filters.") }
                else -> LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp8), contentPadding = PaddingValues(start = KoraMeasures.dp16, top = KoraMeasures.dp8, end = KoraMeasures.dp16, bottom = KoraMeasures.dp104)) {
                    items(filteredRecords, key = { it.displayValue("_local_id").ifBlank { it.displayValue("name") } }) { record ->
                        val rowId = record.displayValue("_local_id").ifBlank { record.displayValue("name") }
                        RecordCard(
                            record, titleField, doctype.label, mobileColumns, readableFields, statusField, stockField, state.userLabels,
                            state.recordLinkLabels + state.detailLinkLabels, state.linkOptions, state.linkTitleFields,
                            state.childSchemas, state.currency,
                            selected = rowId in selectedIds,
                            selectionMode = selectionMode,
                            onToggleSelection = { selectedIds = if (rowId in selectedIds) selectedIds - rowId else selectedIds + rowId },
                            onClick = { if (selectionMode) selectedIds = if (rowId in selectedIds) selectedIds - rowId else selectedIds + rowId else vm.viewRecord(record) },
                            onEdit = { vm.editRecord(record) },
                            onDuplicate = { vm.duplicateRecord(record) },
                            onDelete = {
                                selectedIds = setOf(rowId)
                                deleteConfirmOpen = true
                            },
                        )
                    }
                }
            }
            if (!selectionMode) ExtendedFloatingActionButton(
                onClick = vm::startNewRecord,
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = KoraMeasures.dp16, bottom = KoraMeasures.dp20).height(KoraTokens.ButtonHeight)
                    .semantics(mergeDescendants = true) { contentDescription = "Create new ${doctype.label}"; testTagsAsResourceId = true }
                    .testTag("record-new"),
                icon = { Icon(Icons.Outlined.Add, null) },
                text = { Text("New ${sentenceCaseUiLabel(doctype.label).replaceFirstChar { it.lowercase(Locale.getDefault()) }}") },
                containerColor = colors.primary,
                contentColor = colors.onPrimary,
                shape = RoundedCornerShape(KoraTokens.RadiusControl),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = KoraMeasures.dp4),
            )
            if (selectionMode) Surface(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = KoraMeasures.dp16, vertical = KoraMeasures.dp10),
                color = colors.surface,
                shape = RoundedCornerShape(KoraTokens.RadiusCard),
                border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant),
                shadowElevation = KoraMeasures.dp4,
            ) {
                Row(Modifier.fillMaxWidth().padding(horizontal = KoraTokens.SpaceM, vertical = KoraTokens.SpaceS), verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { exportOpen = true }, enabled = selectedIds.isNotEmpty(), modifier = Modifier.heightIn(min = KoraTokens.TouchTarget)) { Text("Export") }
                    TextButton(
                        onClick = {
                            val selected = filteredRecords.firstOrNull { it.displayValue("_local_id").ifBlank { it.displayValue("name") } in selectedIds }
                            if (selected != null) { vm.duplicateRecord(selected); selectedIds = emptySet(); selectionModeActive = false }
                        },
                        enabled = selectedIds.size == 1 && !state.offline,
                        modifier = Modifier.heightIn(min = KoraTokens.TouchTarget),
                    ) { Text("Duplicate") }
                TextButton(onClick = { deleteConfirmOpen = true }, enabled = selectedIds.isNotEmpty() && !state.offline, modifier = Modifier.heightIn(min = KoraTokens.TouchTarget)) { Text("Delete", color = colors.error) }
                }
            }
        }
    }
    if (filterOpen) ModalBottomSheet(onDismissRequest = { filterOpen = false }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = colors.surface) {
        Column(Modifier.fillMaxWidth().heightIn(max = KoraMeasures.dp480).padding(horizontal = KoraMeasures.dp20)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Sort and filter", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                TextButton(onClick = {
                    filters = emptyMap()
                    lowStockOnly = false
                    sortField = defaultSort
                    ascending = defaultSort == titleField
                }) { Text("Reset", color = colors.primary, fontWeight = FontWeight.SemiBold) }
            }
            LazyColumn(Modifier.weight(1f, fill = false), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp8), contentPadding = PaddingValues(bottom = KoraMeasures.dp12)) {
                item {
                    Text("Sort by", style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
                    // Keep the sheet compact: offer the primary label in both directions, then
                    // a few useful typed fields from this view. The choices are metadata-driven.
                    val numericTypes = setOf("Int", "Float", "Currency", "Percent")
                    val dateTypes = setOf("Date", "Datetime", "DateTime")
                    val sortable = readableFields.filter { field ->
                        field.fieldname == titleField || field.fieldtype in numericTypes || field.fieldtype in dateTypes
                    }.distinctBy { it.fieldname }.take(4)
                    sortable.forEach { field ->
                        val numeric = field.fieldtype in setOf("Int", "Float", "Currency", "Percent")
                        val date = field.fieldtype.equals("Date", true) || field.fieldtype.equals("Datetime", true) || field.fieldtype.equals("DateTime", true)
                        val defaultDirection = !date
                        val directions = if (field.fieldname == titleField) listOf(true, false)
                            else listOf(if (sortField == field.fieldname) ascending else defaultDirection)
                        directions.forEach { ascendingOrder ->
                            val orderLabel = when {
                                date && ascendingOrder -> "oldest first"
                                date -> "newest first"
                                numeric && ascendingOrder -> "low to high"
                                numeric -> "high to low"
                                ascendingOrder -> "A to Z"
                                else -> "Z to A"
                            }
                            val selected = sortField == field.fieldname && ascending == ascendingOrder
                            Row(Modifier.fillMaxWidth().heightIn(min = KoraMeasures.dp44).selectable(
                                selected = selected,
                                role = Role.RadioButton,
                                onClick = {
                                if (field.fieldname != titleField && sortField == field.fieldname) ascending = !ascending
                                else { sortField = field.fieldname; ascending = ascendingOrder }
                            }), verticalAlignment = Alignment.CenterVertically) {
                                KoraRadioIndicator(selected)
                                Spacer(Modifier.width(KoraMeasures.dp12))
                                Text("${field.label}, $orderLabel", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
                if (filterFields.isEmpty()) item { Text("No fields with filter options in this list.", color = colors.onSurfaceVariant) }
                filterFields.forEach { field ->
                    item(key = "filter-${field.fieldname}") {
                        val options = state.records.map { it.optString(field.fieldname) }.filter(String::isNotBlank).distinct().take(12)
                        Column(verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp5)) {
                            Text(field.label, style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
                            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = KoraMeasures.dp4), horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp6)) {
                                options.forEach { option ->
                                    val label = if (field.fieldtype == "Link") state.detailLinkLabels["${field.options}:$option"] ?: option else option
                                    KoraChip(label, filters[field.fieldname] == option, onClick = {
                                        filters = if (filters[field.fieldname] == option) filters - field.fieldname else filters + (field.fieldname to option)
                                    })
                                }
                            }
                        }
                    }
                }
                if (stockField != null) item {
                    Text("Stock level", style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = KoraMeasures.dp4), horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp6)) {
                        KoraChip("All", !lowStockOnly) { lowStockOnly = false }
                        KoraChip(lowStockLabel, lowStockOnly) { lowStockOnly = true }
                    }
                }
            }
            val shownCount = visibleRecords.count { record -> !lowStockOnly || isLowStockRecord(record) }
            Button(
                onClick = { filterOpen = false },
                modifier = Modifier.fillMaxWidth().padding(bottom = KoraMeasures.dp14).heightIn(min = KoraMeasures.dp48),
                shape = RoundedCornerShape(KoraMeasures.dp10),
            ) { Text("Show $shownCount ${if (shownCount == 1) "record" else "records"}") }
        }
    }
    if (deleteConfirmOpen) {
        AlertDialog(
            onDismissRequest = { deleteConfirmOpen = false },
            title = { Text("Delete ${selectedIds.size} ${if (selectedIds.size == 1) "record" else "records"}?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    val names = filteredRecords.filter { it.displayValue("_local_id").ifBlank { it.displayValue("name") } in selectedIds }.map { it.optString("name") }
                    deleteConfirmOpen = false
                    vm.deleteRecords(names)
                    selectedIds = emptySet()
                    selectionModeActive = false
                }, enabled = !state.busy) { Text("Delete", color = colors.error) }
            },
            dismissButton = { TextButton(onClick = { deleteConfirmOpen = false }) { Text("Cancel") } },
        )
    }
    if (exportOpen) {
        val exportRecords = if (selectedIds.isNotEmpty()) filteredRecords.filter { it.displayValue("_local_id").ifBlank { it.displayValue("name") } in selectedIds } else filteredRecords
        val exportColumns = (listOf(titleField, "name") + mobileColumns).distinct()
        ModalBottomSheet(onDismissRequest = { exportOpen = false }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = colors.surface) {
            Column(Modifier.fillMaxWidth().padding(horizontal = KoraTokens.SpaceL).padding(bottom = KoraTokens.SpaceL)) {
                Text("Export ${exportRecords.size} ${if (exportRecords.size == 1) "record" else "records"}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("Uses the columns in your current view.", Modifier.padding(top = KoraMeasures.dp8), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                Spacer(Modifier.height(KoraTokens.SpaceM))
                listOf("CSV", "Excel", "PDF").forEach { format ->
                    Row(Modifier.fillMaxWidth().height(KoraMeasures.dp44).selectable(
                        selected = exportFormat == format,
                        role = Role.RadioButton,
                        onClick = { exportFormat = format },
                    ), verticalAlignment = Alignment.CenterVertically) {
                        KoraRadioIndicator(exportFormat == format)
                        Spacer(Modifier.width(KoraMeasures.dp8))
                        Text(format, style = MaterialTheme.typography.bodyLarge)
                    }
                }
                Spacer(Modifier.height(KoraMeasures.dp10))
                Button(
                    onClick = {
                        fun csv(value: String) = "\"${value.replace("\"", "\"\"")}\""
                        val header = exportColumns.map { field -> csv(readableFields.firstOrNull { it.fieldname == field }?.label ?: if (field == "name") "ID" else field) }.joinToString(",")
                        val rows = exportRecords.joinToString("\n") { record -> exportColumns.joinToString(",") { field -> csv(record.displayValue(field)) } }
                        val matrix = listOf(exportColumns.map { field -> readableFields.firstOrNull { it.fieldname == field }?.label ?: if (field == "name") "ID" else field }) +
                            exportRecords.map { record -> exportColumns.map { field -> record.displayValue(field) } }
                        val csvContent = listOf(header, rows).filter(String::isNotBlank).joinToString("\n")
                        val bytes = when (exportFormat) {
                            "Excel" -> createXlsx(matrix)
                            "PDF" -> createPdf(collection.title, matrix)
                            else -> csvContent.toByteArray(Charsets.UTF_8)
                        }
                        val extension = when (exportFormat) { "Excel" -> "xlsx"; "PDF" -> "pdf"; else -> "csv" }
                        val safeName = collection.title.replace(Regex("[^A-Za-z0-9_-]+"), "_").trim('_').ifBlank { "records" }
                        pendingExport = PendingExport(bytes)
                        createExportDocument.launch("$safeName.$extension")
                    },
                    modifier = Modifier.fillMaxWidth().height(KoraTokens.ButtonHeight),
                    shape = RoundedCornerShape(KoraTokens.RadiusControl),
                ) { Text("Export") }
            }
        }
    }
}

@Composable
private fun KoraRadioIndicator(selected: Boolean) {
    val colors = MaterialTheme.colorScheme
    Box(Modifier.size(KoraMeasures.dp20), contentAlignment = Alignment.Center) {
        Box(
            Modifier.fillMaxSize().border(if (selected) KoraMeasures.dp6 else KoraMeasures.dp2, if (selected) colors.primary else colors.outline, CircleShape),
        )
    }
}

private data class RecordCollectionConfig(
    val title: String,
    val mobileColumns: List<String>,
) {
    companion object {
        fun from(manifest: JSONObject?, fallbackTitle: String): RecordCollectionConfig {
            val children = manifest?.optJSONObject("spec")?.optJSONObject("layout")?.optJSONArray("children") ?: JSONArray()
            val component = (0 until children.length()).mapNotNull(children::optJSONObject)
                .firstOrNull { it.optString("component") in setOf("record_table", "record_list", "record_cards") }
            val props = component?.optJSONObject("props") ?: JSONObject()
            val mobileColumns = props.optJSONArray("mobile_columns")?.let { fields ->
                (0 until fields.length()).map(fields::optString).filter(String::isNotBlank)
            }.orEmpty()
            return RecordCollectionConfig(props.optString("title").ifBlank { fallbackTitle }, mobileColumns)
        }
    }
}

@Composable
private fun RecordCard(
    record: JSONObject,
    titleField: String,
    fallbackTitle: String,
    columns: List<String>,
    fields: List<FieldSchema>,
    statusField: String?,
    stockField: String?,
    userLabels: Map<String, String>,
    detailLinkLabels: Map<String, String>,
    linkOptions: Map<String, List<JSONObject>>,
    linkTitleFields: Map<String, String>,
    childSchemas: Map<String, List<FieldSchema>>,
    currency: String,
    selected: Boolean,
    selectionMode: Boolean,
    onToggleSelection: () -> Unit,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val dateField = fields.firstOrNull { field ->
        field.fieldtype.equals("Date", true) || field.fieldtype.equals("Datetime", true) || field.fieldtype.equals("DateTime", true)
    }
    val configuredTitleField = fields.firstOrNull { it.fieldname == titleField }
    val configuredTitleValue = record.displayValue(titleField)
    val fallbackTitleField = if (configuredTitleValue.isBlank()) {
        columns.asSequence()
            .filter { it != titleField && !it.equals("name", ignoreCase = true) }
            .mapNotNull { fieldname -> fields.firstOrNull { it.fieldname == fieldname } }
            .filter { field -> field.fieldtype in setOf("Data", "Text", "Small Text", "Long Text", "Link", "Dynamic Link", "Date", "Datetime", "DateTime") }
            .firstOrNull { field -> record.displayValue(field.fieldname).isNotBlank() }
    } else null
    val valueField = columns.firstOrNull { column ->
        fields.firstOrNull { it.fieldname == column }?.fieldtype.equals("Currency", true)
    }
    val detailColumns = columns.filter { it != titleField && it != "name" && it != valueField && it != stockField }.take(3)
    val stock = stockField?.let { record.displayValue(it).toDoubleOrNull() }
    val childTable = fields.firstOrNull { it.fieldtype.equals("Table", true) }
    val childRows = childTable?.let { record.optJSONArray(it.fieldname) }
    val childSummary = childTable?.let { table -> summarizeChildRows(childRows, childSchemas[table.options].orEmpty(), detailLinkLabels, currency) }.orEmpty()
    val displayTitleField = configuredTitleField.takeIf { configuredTitleValue.isNotBlank() } ?: fallbackTitleField
    val rawTitle = configuredTitleValue
        .ifBlank { fallbackTitleField?.let { record.displayValue(it.fieldname) }.orEmpty() }
        .ifBlank { fallbackTitle.ifBlank { "Record" } }
    val title = when {
        displayTitleField?.fieldtype.equals("Date", true) -> formatHumanDate(rawTitle, dateOnly = true)
        displayTitleField?.fieldtype.equals("Datetime", true) || displayTitleField?.fieldtype.equals("DateTime", true) -> formatHumanDate(rawTitle, dateOnly = false)
        displayTitleField?.fieldtype == "Link" -> detailLinkLabels["${displayTitleField.options}:$rawTitle"] ?: rawTitle
        else -> rawTitle
    }
    val derivedChildTotal = childTable?.let { table ->
        val schema = childSchemas[table.options].orEmpty()
        val quantity = schema.firstOrNull { it.matchesSemanticRole("quantity", "qty", "units") }
        val price = schema.firstOrNull { it.matchesSemanticRole("unit price", "price", "rate", "unit cost") }
        val rows = childRows
        if (quantity == null || price == null || rows == null || rows.length() == 0) null
        else (0 until rows.length()).mapNotNull { index ->
            val row = rows.optJSONObject(index) ?: return@mapNotNull null
            val qty = row.opt(quantity.fieldname)?.takeUnless { it == JSONObject.NULL }?.toString()?.toDoubleOrNull()
            val rate = row.opt(price.fieldname)?.takeUnless { it == JSONObject.NULL }?.toString()?.toDoubleOrNull()
            if (qty == null || rate == null) null else qty * rate
        }.takeIf { it.size == rows.length() }?.sum()
    }
    var rowActionsOpen by remember(record.optString("name")) { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(KoraTokens.RadiusCard),
        color = if (selected) colors.secondaryContainer.copy(alpha = .58f) else colors.surface,
        border = androidx.compose.foundation.BorderStroke(KoraTokens.Hairline, if (selected) colors.primary else colors.outlineVariant),
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = KoraTokens.CardPaddingHorizontal, vertical = KoraTokens.CardPaddingVertical), verticalArrangement = Arrangement.spacedBy(KoraTokens.SpaceXs)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (selectionMode || selected) Checkbox(checked = selected, onCheckedChange = { onToggleSelection() }, modifier = Modifier.size(KoraMeasures.dp44))
                Column(Modifier.weight(1f).padding(start = if (selectionMode || selected) KoraTokens.SpaceXs else KoraMeasures.dp0, end = KoraTokens.SpaceM), verticalArrangement = Arrangement.spacedBy(KoraTokens.SpaceXs)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    val skuField = columns.firstOrNull { column -> fields.firstOrNull { it.fieldname == column }?.let { it.fieldname.contains("sku", true) || it.label.contains("sku", true) } == true }
                    val categoryField = columns.firstOrNull { column ->
                        fields.firstOrNull { it.fieldname == column }?.fieldtype.equals("Select", true)
                    }
                    val extraFields = detailColumns.filterNot { it == skuField || it == categoryField || it == dateField?.fieldname }
                    Row(horizontalArrangement = Arrangement.spacedBy(KoraTokens.SpaceSm), verticalAlignment = Alignment.CenterVertically) {
                        skuField?.let { sku ->
                            Text(record.displayValue(sku).ifBlank { "—" }, style = MaterialTheme.typography.labelMedium.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = KoraMeasures.sp12_5), color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        categoryField?.let { category ->
                            Surface(color = colors.secondaryContainer, shape = RoundedCornerShape(KoraTokens.TagRadius)) {
                                Text(record.displayValue(category).ifBlank { "—" }, Modifier.padding(horizontal = KoraTokens.TagPaddingHorizontal, vertical = KoraTokens.TagPaddingVertical), style = MaterialTheme.typography.labelSmall, color = colors.onSecondaryContainer, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        if (skuField == null && categoryField == null && extraFields.isNotEmpty()) {
                            val details = extraFields.take(2).mapNotNull { fieldName ->
                                val field = fields.firstOrNull { it.fieldname == fieldName } ?: return@mapNotNull null
                                val raw = record.opt(fieldName)?.takeUnless { it == JSONObject.NULL }?.toString().orEmpty()
                                if (raw.isBlank()) return@mapNotNull null
                                val value = when {
                                    field.fieldtype.equals("Date", true) -> formatHumanDate(raw, dateOnly = true)
                                    field.fieldtype.equals("Datetime", true) || field.fieldtype.equals("DateTime", true) -> formatHumanDate(raw, dateOnly = false)
                                    field.fieldtype.equals("Currency", true) -> formatCurrency(raw, currency)
                                    field.fieldtype.equals("Link", true) -> detailLinkLabels["${field.options}:$raw"] ?: raw
                                    else -> raw
                                }
                                "${sentenceCaseUiLabel(field.label.ifBlank { fieldName })}: $value"
                            }
                            val summary = (details + childSummary.takeIf { it.isNotBlank() && it != "—" }.orEmpty()).joinToString(" · ")
                            if (summary.isNotBlank()) Text(summary, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                if (valueField != null || stock != null || (!statusField.isNullOrBlank() && record.optString(statusField).isNotBlank())) {
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(KoraTokens.SpaceXs)) {
                        valueField?.let { field ->
                            val storedValue = record.opt(field)?.takeUnless { it == JSONObject.NULL || it.toString().isBlank() }
                            val value = storedValue ?: derivedChildTotal
                            Text(formatCurrency(value, currency), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = colors.onSurface, maxLines = 1)
                        }
                        if (stock != null) {
                            val thresholdField = fields.firstOrNull { isStockThresholdField(it.fieldname, it.label) }
                            val threshold = thresholdField?.let { record.opt(it.fieldname)?.toString()?.toDoubleOrNull() } ?: 0.0
                            val belowThreshold = stock <= threshold
                            if (belowThreshold) Surface(
                                color = warningContainer(colors),
                                shape = RoundedCornerShape(KoraTokens.TagRadius),
                            ) {
                                Text(
                                    "${stock.toIntIfWhole()} left",
                                    Modifier.padding(horizontal = KoraTokens.TagPaddingHorizontal, vertical = KoraTokens.TagPaddingVertical),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = warningForeground(colors),
                                    maxLines = 1,
                                )
                            } else Text("${stock.toIntIfWhole()} in stock", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 1)
                        }
                        if (!statusField.isNullOrBlank() && record.optString(statusField).isNotBlank()) {
                            Surface(color = colors.secondary, shape = RoundedCornerShape(KoraMeasures.dp4)) {
                                Text(record.optString(statusField), Modifier.padding(horizontal = KoraMeasures.dp8, vertical = KoraMeasures.dp2), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = colors.onSecondary, maxLines = 1)
                            }
                        }
                    }
                }
                if (!selectionMode) Box {
                    IconButton(
                        onClick = { rowActionsOpen = true },
                        modifier = Modifier.size(KoraTokens.TouchTarget),
                    ) { Icon(Icons.Outlined.MoreVert, contentDescription = "Actions for $title", tint = colors.onSurfaceVariant) }
                    DropdownMenu(expanded = rowActionsOpen, onDismissRequest = { rowActionsOpen = false }) {
                        DropdownMenuItem(text = { Text("Open record") }, onClick = { rowActionsOpen = false; onClick() })
                        DropdownMenuItem(text = { Text("Edit") }, onClick = { rowActionsOpen = false; onEdit() })
                        DropdownMenuItem(text = { Text("Duplicate") }, onClick = { rowActionsOpen = false; onDuplicate() })
                        DropdownMenuItem(text = { Text("Delete", color = colors.error) }, onClick = { rowActionsOpen = false; onDelete() })
                    }
                }
            }
        }
    }
}

private fun summarizeChildRows(rows: JSONArray?, fields: List<FieldSchema>, labels: Map<String, String>, currency: String): String {
    if (rows == null || rows.length() == 0) return "—"
    val primaryField = fields.firstOrNull { it.fieldtype == "Link" }
        ?: fields.firstOrNull { it.fieldtype in setOf("Data", "Select", "Int", "Float", "Currency") }
    fun FieldSchema.matchesRole(vararg terms: String): Boolean =
        terms.any { term -> fieldname.contains(term, ignoreCase = true) || label.contains(term, ignoreCase = true) }
    val quantityField = fields.firstOrNull { it.matchesRole("quantity", "qty") && it.fieldtype in setOf("Int", "Float", "Currency") }
    val amountField = fields.firstOrNull { it.fieldtype.equals("Currency", true) && it != quantityField && it.matchesRole("amount", "total", "subtotal") }
        ?: fields.firstOrNull { it.fieldtype.equals("Currency", true) && it != quantityField }
    val unitPriceField = fields.firstOrNull { it.fieldtype.equals("Currency", true) && it != amountField && it.matchesRole("unit_price", "rate", "price") }
    val values = (0 until rows.length()).mapNotNull { index ->
        val row = rows.optJSONObject(index) ?: return@mapNotNull null
        val rawName = primaryField?.let { row.optString(it.fieldname) }.orEmpty()
        val displayName = if (primaryField?.fieldtype == "Link") labels["${primaryField.options}:$rawName"] ?: rawName else rawName
        val quantity = quantityField?.let { row.opt(it.fieldname)?.takeUnless { value -> value == JSONObject.NULL }?.toString()?.toDoubleOrNull() }
        val amount = amountField?.let { row.opt(it.fieldname)?.takeUnless { value -> value == JSONObject.NULL } }
        val unitPrice = unitPriceField?.let { row.opt(it.fieldname)?.takeUnless { value -> value == JSONObject.NULL }?.toString()?.toDoubleOrNull() }
        val effectiveAmount = amount ?: if (quantity != null && unitPrice != null) quantity * unitPrice else null
        val quantityText = quantity?.let { " × ${it.toIntIfWhole()}" }.orEmpty()
        val amountText = effectiveAmount?.let { " · ${formatCurrency(it, currency)}" }.orEmpty()
        when {
            displayName.isNotBlank() -> "$displayName$quantityText$amountText"
            effectiveAmount != null -> formatCurrency(effectiveAmount, currency)
            quantity != null -> quantity.toIntIfWhole()
            else -> null
        }
    }
    return when {
        values.isEmpty() -> "${rows.length()} ${if (rows.length() == 1) "row" else "rows"}"
        values.size <= 2 -> values.joinToString(" · ")
        else -> "${values.take(2).joinToString(" · ")} +${values.size - 2} more"
    }
}

private data class PendingExport(val bytes: ByteArray)

private fun createXlsx(rows: List<List<String>>): ByteArray {
    fun xml(value: String) = buildString {
        value.forEach { char ->
            when (char) {
                '&' -> append("&amp;")
                '<' -> append("&lt;")
                '>' -> append("&gt;")
                '"' -> append("&quot;")
                '\'' -> append("&apos;")
                else -> if (char == '\t' || char == '\n' || char == '\r' || char.code >= 0x20) append(char)
            }
        }
    }
    fun columnName(index: Int): String {
        var value = index + 1
        var result = ""
        while (value > 0) {
            val remainder = (value - 1) % 26
            result = ('A' + remainder) + result
            value = (value - 1) / 26
        }
        return result
    }
    val sheet = buildString {
        append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
        append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>")
        rows.forEachIndexed { rowIndex, row ->
            val rowNumber = rowIndex + 1
            append("<row r=\"$rowNumber\">")
            row.forEachIndexed { columnIndex, value ->
                val ref = "${columnName(columnIndex)}$rowNumber"
                append("<c r=\"$ref\" t=\"inlineStr\"><is><t xml:space=\"preserve\">${xml(value)}</t></is></c>")
            }
            append("</row>")
        }
        append("</sheetData></worksheet>")
    }
    val output = java.io.ByteArrayOutputStream()
    java.util.zip.ZipOutputStream(output).use { zip ->
        fun entry(name: String, content: String) {
            zip.putNextEntry(java.util.zip.ZipEntry(name))
            zip.write(content.toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }
        entry("[Content_Types].xml", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/><Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/></Types>""")
        entry("_rels/.rels", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>""")
        entry("xl/workbook.xml", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets><sheet name="Records" sheetId="1" r:id="rId1"/></sheets></workbook>""")
        entry("xl/_rels/workbook.xml.rels", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/></Relationships>""")
        entry("xl/worksheets/sheet1.xml", sheet)
    }
    return output.toByteArray()
}

private fun createPdf(title: String, rows: List<List<String>>): ByteArray {
    val document = android.graphics.pdf.PdfDocument()
    val width = 842
    val height = 595
    val margin = 28f
    val rowHeight = 22f
    val pageCapacity = 22
    val columns = rows.maxOfOrNull { it.size }?.coerceAtLeast(1) ?: 1
    val cellWidth = (width - margin * 2) / columns
    val headerPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(232, 237, 244); style = android.graphics.Paint.Style.FILL
    }
    val textPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(22, 25, 29); textSize = 9f
    }
    val titlePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(22, 25, 29); textSize = 16f; typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
    }
    val borderPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(207, 212, 219); strokeWidth = 0.7f; style = android.graphics.Paint.Style.STROKE
    }
    val bodyRows = rows.drop(1)
    val pageCount = maxOf(1, (bodyRows.size + pageCapacity - 1) / pageCapacity)
    repeat(pageCount) { pageIndex ->
        val pageNumber = pageIndex + 1
        val page = document.startPage(android.graphics.pdf.PdfDocument.PageInfo.Builder(width, height, pageNumber).create())
        val canvas = page.canvas
        canvas.drawColor(android.graphics.Color.WHITE)
        canvas.drawText(title, margin, 28f, titlePaint)
        canvas.drawText("Page $pageNumber of $pageCount", width - margin - 80f, 28f, textPaint)
        val top = 46f
        val headers = rows.firstOrNull().orEmpty()
        canvas.drawRect(margin, top, width - margin, top + rowHeight, headerPaint)
        headers.forEachIndexed { index, text ->
            val left = margin + index * cellWidth
            canvas.drawText(fitPdfText(text, textPaint, cellWidth - 10f), left + 5f, top + 15f, textPaint)
            canvas.drawRect(left, top, left + cellWidth, top + rowHeight, borderPaint)
        }
        val pageRows = bodyRows.drop(pageIndex * pageCapacity).take(pageCapacity)
        pageRows.forEachIndexed { rowIndex, row ->
            val y = top + rowHeight * (rowIndex + 1)
            row.forEachIndexed { columnIndex, text ->
                val left = margin + columnIndex * cellWidth
                canvas.drawText(fitPdfText(text, textPaint, cellWidth - 10f), left + 5f, y + 15f, textPaint)
                canvas.drawRect(left, y, left + cellWidth, y + rowHeight, borderPaint)
            }
        }
        document.finishPage(page)
    }
    val output = java.io.ByteArrayOutputStream()
    document.writeTo(output)
    document.close()
    return output.toByteArray()
}

private fun fitPdfText(value: String, paint: android.graphics.Paint, maxWidth: Float): String {
    if (paint.measureText(value) <= maxWidth) return value
    var result = value
    while (result.isNotEmpty() && paint.measureText("$result…") > maxWidth) result = result.dropLast(1)
    return "$result…"
}

@Composable
private fun RecordDetailScreen(state: AppState, vm: AppViewModel) {
    val record = state.selectedRecord ?: return
    val colors = androidx.compose.material3.MaterialTheme.colorScheme
    val visibleFields = state.schemaFields.filterNot { it.hidden }
    val title = record.displayValue(state.titleField).ifBlank { record.displayValue("name").ifBlank { "Record" } }
    val hasChildTables = state.schemaFields.any { it.fieldtype == "Table" }
    val tableFields = visibleFields.filter { it.fieldtype == "Table" }
    val primaryTable = tableFields.firstOrNull()
    var activeDetailTab by rememberSaveable(record.optString("name")) {
        mutableStateOf(primaryTable?.let { "table:${it.fieldname}" } ?: "details")
    }
    val status = state.schemaFields.firstOrNull { it.fieldname == "status" }?.let { record.optString(it.fieldname) }.orEmpty()
    val regularFields = visibleFields.filterNot { it.fieldtype in setOf("Section Break", "Column Break", "Heading", "Table") || it.fieldname == state.titleField || it.fieldname == "status" }
    val summaryFields = regularFields.filter { field ->
        val raw = record.opt(field.fieldname)
        raw != null && raw != JSONObject.NULL && raw.toString().isNotBlank() &&
            (field.fieldtype in setOf("Currency", "Date", "Datetime", "DateTime", "Select", "Link") || field.label.contains(Regex("(?i)total|amount|status|method")))
    }
    val primaryMetric = summaryFields.firstOrNull { it.fieldtype == "Currency" || it.label.contains(Regex("(?i)total|amount")) }
    val primaryDateField = summaryFields.firstOrNull { it.fieldtype in setOf("Datetime", "DateTime", "Date") }
        ?: visibleFields.firstOrNull { field ->
            field.fieldname == state.titleField &&
                field.fieldtype in setOf("Datetime", "DateTime", "Date") &&
                record.opt(field.fieldname)?.let { it != JSONObject.NULL && it.toString().isNotBlank() } == true
        }
    val primaryContextField = summaryFields.firstOrNull { it.fieldtype in setOf("Select", "Link") && it != primaryMetric }
    val quickFacts = summaryFields.filterNot { it == primaryMetric }.take(2)
    val activeTable = tableFields.firstOrNull { activeDetailTab == "table:${it.fieldname}" } ?: primaryTable
    LaunchedEffect(record.optString("name"), tableFields.map { it.fieldname }) {
        val validTabs = setOf("details", "history") + tableFields.map { "table:${it.fieldname}" }
        if (activeDetailTab !in validTabs || (primaryTable != null && activeDetailTab == "details")) {
            activeDetailTab = primaryTable?.let { "table:${it.fieldname}" } ?: "details"
        }
    }
    val childRows = activeTable?.let { record.optJSONArray(it.fieldname) } ?: JSONArray()
    val primaryChildFields = activeTable?.let { state.childSchemas[it.options].orEmpty() }.orEmpty()
    var itemEditorRows by remember(record.optString("name")) { mutableStateOf<List<Map<String, Any?>>?>(null) }
    var itemEditorIndex by remember(record.optString("name")) { mutableStateOf(-1) }
    val childQuantityField = primaryChildFields.firstOrNull { it.fieldtype in setOf("Int", "Float", "Percent") && it.matchesSemanticRole("quantity", "qty", "units") }
    val childRateField = primaryChildFields.firstOrNull { it.fieldtype in setOf("Int", "Float", "Currency", "Percent") && it.matchesSemanticRole("unit price", "price", "rate", "unit cost") }
    val childAmountField = primaryChildFields.firstOrNull { it.fieldtype in setOf("Int", "Float", "Currency", "Percent") && (it.computed != null || it.matchesSemanticRole("amount", "total")) && it != childRateField }
    val itemLikeTable = activeTable != null && primaryChildFields.any { it.fieldtype == "Link" } && childQuantityField != null && childRateField != null
    val storedChildTotal = primaryMetric?.takeIf { activeTable == primaryTable }?.let { record.opt(it.fieldname) }
        ?.takeUnless { it == JSONObject.NULL || it.toString().isBlank() }
    val computedChildTotal = activeTable?.let {
        if (childRows.length() == 0 || (childAmountField == null && (childQuantityField == null || childRateField == null))) null
        else (0 until childRows.length()).sumOf { index ->
            val line = childRows.optJSONObject(index) ?: JSONObject()
            val storedLine = childAmountField?.let { line.opt(it.fieldname) }
                ?.takeUnless { it == JSONObject.NULL || it.toString().isBlank() }
                ?.toString()?.toDoubleOrNull()
            storedLine ?: ((childQuantityField?.let { line.optString(it.fieldname).toDoubleOrNull() } ?: 0.0) *
                (childRateField?.let { line.optString(it.fieldname).toDoubleOrNull() } ?: 0.0))
        }
    }
    val childTableTotal = storedChildTotal?.toString()?.toDoubleOrNull() ?: computedChildTotal
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
            contentPadding = PaddingValues(start = KoraMeasures.dp16, top = KoraMeasures.dp10, end = KoraMeasures.dp16, bottom = if (hasChildTables && activeTable != null && itemLikeTable) KoraMeasures.dp12 else KoraMeasures.dp24),
            verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp12),
        ) {
            item(key = "record-summary") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(KoraTokens.RadiusCard),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant.copy(alpha = .65f)),
                ) {
                    if (hasChildTables) {
                        Column(Modifier.fillMaxWidth().padding(horizontal = KoraTokens.CardPaddingHorizontal, vertical = KoraTokens.CardPaddingVertical), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp4)) {
                            val dateText = primaryDateField?.let { field ->
                                formatHumanDate(record.optString(field.fieldname), dateOnly = field.fieldtype == "Date")
                            }.orEmpty()
                            val contextText = primaryContextField?.let { field ->
                                val raw = record.optString(field.fieldname)
                                if (field.fieldtype == "Link") state.detailLinkLabels["${field.options}:$raw"].orEmpty().ifBlank { raw } else raw
                            }.orEmpty()
                            Text(
                                listOf(dateText, contextText).filter(String::isNotBlank).joinToString(" · ").ifBlank { title },
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = KoraMeasures.sp13),
                                color = colors.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            childTableTotal?.let { total ->
                                Text(formatCurrency(total, state.currency), style = MaterialTheme.typography.headlineSmall.copy(fontSize = KoraMeasures.sp26), fontWeight = FontWeight.SemiBold, color = colors.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            if (status.isNotBlank()) Text(status, style = MaterialTheme.typography.labelMedium, color = colors.primary)
                            if (state.detailLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
                        }
                    } else {
                        Column(Modifier.fillMaxWidth().padding(KoraMeasures.dp16), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp12)) {
                            Row(verticalAlignment = Alignment.Top) {
                                Column(Modifier.weight(1f).padding(end = KoraMeasures.dp10), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                                    Text(title, style = MaterialTheme.typography.headlineSmall.copy(fontSize = KoraMeasures.sp23, lineHeight = KoraMeasures.sp29), fontWeight = FontWeight.SemiBold, color = colors.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    if (status.isNotBlank()) Surface(color = colors.surfaceVariant, shape = RoundedCornerShape(KoraMeasures.dp8)) { Text(status, Modifier.padding(horizontal = KoraMeasures.dp12, vertical = KoraMeasures.dp6), style = MaterialTheme.typography.labelLarge, color = colors.primary, fontWeight = FontWeight.SemiBold) }
                                }
                                OutlinedButton(onClick = { vm.editRecord(record) }, shape = RoundedCornerShape(KoraMeasures.dp12), contentPadding = PaddingValues(horizontal = KoraMeasures.dp12, vertical = KoraMeasures.dp8)) {
                                    Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(KoraMeasures.dp18)); Spacer(Modifier.width(KoraMeasures.dp7)); Text("Edit")
                                }
                            }
                            if (state.detailLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
                        }
                    }
                }
            }
            if (!hasChildTables && (primaryMetric != null || quickFacts.isNotEmpty())) {
                item(key = "record-highlights") {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp12)) {
                        primaryMetric?.let { field ->
                                    val value = if (field.fieldtype == "Currency") formatCurrency(record.opt(field.fieldname), state.currency) else record.opt(field.fieldname)?.toString().orEmpty()
                                    Surface(Modifier.weight(1.15f), color = colors.surface, shape = RoundedCornerShape(KoraMeasures.dp12), border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant.copy(alpha = .65f))) {
                                Column(Modifier.padding(horizontal = KoraMeasures.dp18, vertical = KoraMeasures.dp15), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp4)) {
                                    Text(sentenceCaseUiLabel(field.label), style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                                    Text(value, style = androidx.compose.material3.MaterialTheme.typography.headlineSmall, color = colors.primary, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                        if (quickFacts.isNotEmpty()) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                                quickFacts.forEach { field ->
                                    val raw = record.opt(field.fieldname)
                                    val value = when {
                                        field.fieldtype == "Date" -> formatHumanDate(raw.toString(), dateOnly = true)
                                        field.fieldtype == "Datetime" -> formatHumanDate(raw.toString(), dateOnly = false)
                                        field.fieldtype == "Link" -> state.detailLinkLabels["${field.options}:$raw"].orEmpty().ifBlank { raw.toString() }
                                        else -> raw.toString()
                                    }
                                    Surface(Modifier.fillMaxWidth(), color = colors.surfaceContainerLow, shape = RoundedCornerShape(KoraMeasures.dp14)) {
                                        Column(Modifier.padding(horizontal = KoraMeasures.dp13, vertical = KoraMeasures.dp10), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp2)) {
                                            Text(sentenceCaseUiLabel(field.label), style = androidx.compose.material3.MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                                            Text(value, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (hasChildTables) item(key = "transaction-tabs") {
                val tabs = buildList {
                    add("details" to "Details")
                    tableFields.forEach { table ->
                        val count = record.optJSONArray(table.fieldname)?.length() ?: 0
                        val label = sentenceCaseUiLabel(table.label).ifBlank { "Records" }
                        add("table:${table.fieldname}" to "$label $count")
                    }
                    add("history" to "History")
                }
                KoraSegmentedControl(tabs, activeDetailTab, height = KoraMeasures.dp48) { activeDetailTab = it }
            }
            state.message?.takeUnless(::isPassiveConnectionMessage)?.let { message -> item(key = "record-message") { MessageBanner(message, false) } }

            val displayedTableFields = if (!hasChildTables) tableFields else listOfNotNull(activeTable)
            displayedTableFields.forEach { tableField ->
                val childRows = record.optJSONArray(tableField.fieldname) ?: JSONArray()
                val childFields = state.childSchemas[tableField.options].orEmpty()
                    .filterNot { it.hidden || it.fieldtype in setOf("Section Break", "Column Break", "Heading", "Table", "JSON") }
                val tableQuantityField = childFields.firstOrNull { it.fieldtype in setOf("Int", "Float", "Percent") && it.matchesSemanticRole("quantity", "qty", "units") }
                val tableRateField = childFields.firstOrNull { it.fieldtype in setOf("Int", "Float", "Currency", "Percent") && it.matchesSemanticRole("unit price", "price", "rate", "unit cost") }
                val tableIsLineLike = childFields.any { it.fieldtype == "Link" } && tableQuantityField != null && tableRateField != null
                val childRowLabel = singularizeDisplayLabel(tableField.label)
                item(key = "table-heading-${tableField.fieldname}") {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp2)) {
                            Text(tableField.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                        if (tableIsLineLike) OutlinedButton(onClick = {
                            val rows = childRowsFromValue(childRows)
                            val defaults = childFields.mapNotNull { field -> field.defaultValue?.let { field.fieldname to parseChildDefault(field.fieldtype, it) } }.toMap()
                            itemEditorRows = rows + defaults
                            itemEditorIndex = rows.size
                            childFields.firstOrNull { it.fieldtype == "Link" }?.options?.let(vm::loadLinkOptions)
                        }, shape = RoundedCornerShape(KoraMeasures.dp10), contentPadding = PaddingValues(horizontal = KoraMeasures.dp14, vertical = KoraMeasures.dp6), modifier = Modifier.heightIn(min = KoraTokens.SecondaryButtonHeight)) {
                            Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(KoraMeasures.dp18)); Spacer(Modifier.width(KoraMeasures.dp5)); Text(sentenceCaseUiLabel("Add $childRowLabel"))
                        }
                    }
                }
                if (childRows.length() == 0) {
                    item(key = "table-empty-${tableField.fieldname}") {
                        Text(
                            if (state.detailLoading) "Loading ${tableField.label.lowercase()}…" else "No ${tableField.label.lowercase()} in this record",
                            Modifier.fillMaxWidth().padding(vertical = KoraMeasures.dp4),
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                        )
                    }
                } else if (tableIsLineLike) {
                    items((0 until childRows.length()).toList(), key = { index -> "${tableField.fieldname}-child-$index" }) { index ->
                        ChildRecordCard(childRows.optJSONObject(index) ?: JSONObject(), index, childFields, state) {
                            itemEditorRows = childRowsFromValue(childRows)
                            itemEditorIndex = index
                            childFields.firstOrNull { it.fieldtype == "Link" }?.options?.let(vm::loadLinkOptions)
                        }
                    }
                } else {
                    items((0 until childRows.length()).toList(), key = { index -> "${tableField.fieldname}-$index" }) { index ->
                        val child = childRows.optJSONObject(index) ?: JSONObject()
                        val primaryField = childFields.firstOrNull { it.fieldtype == "Link" }
                            ?: childFields.firstOrNull { it.fieldtype == "Data" }
                        val primaryId = primaryField?.let { child.optString(it.fieldname) }.orEmpty()
                        val primaryLabel = primaryField?.let { field ->
                            if (field.fieldtype == "Link") {
                                state.detailLinkLabels["${field.options}:$primaryId"]
                                    ?: state.linkOptions[field.options].orEmpty().firstOrNull { it.optString("name") == primaryId }
                                        ?.let { it.optString(state.linkTitleFields[field.options] ?: "name").ifBlank { primaryId } }
                                    ?: primaryId
                            } else primaryId
                        }.orEmpty()
                        val visibleChildFields = childFields.filter { it.fieldname != primaryField?.fieldname }
                        Surface(color = colors.surface, shape = RoundedCornerShape(KoraMeasures.dp12), border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant.copy(alpha = .65f))) {
                            Column(Modifier.fillMaxWidth().padding(horizontal = KoraMeasures.dp18, vertical = KoraMeasures.dp16), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp14)) {
                                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp12)) {
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp4)) {
                                        if (primaryLabel.isNotBlank()) Text(primaryLabel, style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                        val secondary = childFields.firstOrNull { it.fieldtype == "Data" && it != primaryField }
                                            ?.let { child.optString(it.fieldname) }.orEmpty()
                                        if (secondary.isNotBlank()) Text(secondary, style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                                    }
                                    visibleChildFields.firstOrNull { it.fieldtype == "Currency" || it.label.contains(Regex("(?i)amount|total")) }?.let { totalField ->
                                        val amount = if (totalField.fieldtype == "Currency") formatCurrency(child.opt(totalField.fieldname), state.currency) else child.opt(totalField.fieldname)?.toString().orEmpty()
                                        Text(amount, style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.primary)
                                    }
                                }
                                val summaryFields = visibleChildFields.filterNot { it == primaryField || it.fieldtype == "Currency" || it.label.contains(Regex("(?i)amount|total")) }
                                if (summaryFields.isNotEmpty()) {
                                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp10)) {
                                        summaryFields.forEach { childField ->
                                    val raw = child.opt(childField.fieldname)
                                    val value = when {
                                        raw == null || raw == JSONObject.NULL || raw == "" -> "—"
                                        childField.fieldtype == "Link" -> state.detailLinkLabels["${childField.options}:$raw"].orEmpty().ifBlank { raw.toString() }
                                        childField.fieldtype == "Date" -> formatHumanDate(raw.toString(), dateOnly = true)
                                        childField.fieldtype == "Datetime" -> formatHumanDate(raw.toString(), dateOnly = false)
                                        childField.fieldtype == "Currency" -> formatCurrency(raw, state.currency)
                                        else -> raw.toString()
                                    }
                                            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp2)) {
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

            if (detailFields.isNotEmpty() && (!hasChildTables || activeDetailTab == "details")) {
                    detailSections.forEachIndexed { sectionIndex, (sectionTitle, sectionFields) ->
                        item(key = "detail-section-$sectionIndex") {
                            Column(verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp3)) {
                                Text(sectionTitle, style = androidx.compose.material3.MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                                if (sectionTitle == "Details") Text("Record information", style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                            }
                        }
                        item(key = "detail-table-$sectionIndex") {
                    Surface(color = colors.surface, shape = RoundedCornerShape(KoraMeasures.dp12), border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant.copy(alpha = .65f))) {
                        Column(Modifier.fillMaxWidth().padding(horizontal = KoraMeasures.dp16, vertical = KoraMeasures.dp4)) {
                            sectionFields.forEachIndexed { index, field ->
                    val raw = record.opt(field.fieldname)
                    val value = when {
                        raw == null || raw == JSONObject.NULL || raw == "" -> "—"
                        raw is Boolean -> if (raw) "Yes" else "No"
                        field.fieldtype == "User" || (field.fieldtype == "Link" && field.options.equals("User", true)) -> state.userLabels[raw.toString()] ?: "Workspace user unavailable"
                    field.fieldtype == "Link" -> state.detailLinkLabels["${field.options}:$raw"].orEmpty().ifBlank { raw.toString() }
                    field.fieldtype == "Currency" -> formatCurrency(raw, state.currency)
                        field.fieldtype == "Date" -> formatHumanDate(raw.toString(), dateOnly = true)
                        field.fieldtype == "Datetime" -> formatHumanDate(raw.toString(), dateOnly = false)
                        else -> raw.toString()
                    }
                                Row(Modifier.fillMaxWidth().padding(vertical = KoraMeasures.dp13), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp16)) {
                                    Text(sentenceCaseUiLabel(field.label), Modifier.weight(.9f), style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                                    Text(value, Modifier.weight(1.1f), style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, textAlign = androidx.compose.ui.text.style.TextAlign.End)
                                }
                                if (index < sectionFields.lastIndex) Divider(color = colors.outlineVariant.copy(alpha = .55f))
                            }
                        }
                    }
                        }
                    }
            }
            if (hasChildTables && activeDetailTab == "history") item(key = "record-history-empty") {
                EmptyCard("No history available", "This workspace does not provide record history to the mobile app yet.")
            }
            if (detailFields.isEmpty() && tableFields.isEmpty()) {
                item(key = "no-detail-fields") { EmptyCard("No additional details", "This record has no other fields to display.") }
            }
        }
        if (hasChildTables && activeTable != null && itemLikeTable) {
            Surface(color = colors.surface, shadowElevation = KoraMeasures.dp6, border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant.copy(alpha = .5f))) {
                Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = KoraMeasures.dp18, vertical = KoraMeasures.dp10), verticalAlignment = Alignment.CenterVertically) {
                    Text("${childRows.length()} ${activeTable.label.lowercase()}", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    childTableTotal?.let { Text(formatCurrency(it, state.currency), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = colors.onSurface) }
                }
            }
        }
    }
    val rowsForEditor = itemEditorRows
    if (rowsForEditor != null && itemEditorIndex in rowsForEditor.indices && activeTable != null) {
        ChildRowEditorSheet(
            rowIndex = itemEditorIndex,
            tableLabel = activeTable.label,
            fields = primaryChildFields,
            rows = rowsForEditor,
            state = state,
            onDismiss = { itemEditorIndex = -1; itemEditorRows = null },
            onRowsChange = { updatedRows ->
                vm.saveChildRows(record, activeTable, updatedRows)
                itemEditorRows = updatedRows
            },
            onLoadOptions = vm::loadLinkOptions,
        )
    }
}

@Composable
private fun ChildRecordCard(row: JSONObject, index: Int, fields: List<FieldSchema>, state: AppState, onEdit: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val warningFill = warningContainer(colors)
    val warningInk = warningForeground(colors)
    val linkedField = fields.firstOrNull { it.fieldtype == "Link" }
    val quantityField = fields.firstOrNull { it.fieldtype in setOf("Int", "Float", "Percent") && it.matchesSemanticRole("quantity", "qty", "units") }
    val rateField = fields.firstOrNull { it.fieldtype in setOf("Int", "Float", "Currency") && it.matchesSemanticRole("unit price", "price", "rate", "unit cost") }
    val amountField = fields.firstOrNull { it != rateField && (it.computed != null || it.matchesSemanticRole("line amount", "line total", "amount", "total")) }
    val linkedId = linkedField?.let { row.optString(it.fieldname) }.orEmpty()
    val linkedRecord = linkedField?.let { state.detailLinkedRecords["${it.options}:$linkedId"] }
    val linkedSchema = linkedField?.let { state.detailLinkedSchemas[it.options].orEmpty() }.orEmpty()
    val linkedLabel = linkedField?.let { field ->
        state.detailLinkLabels["${field.options}:$linkedId"].orEmpty()
            .ifBlank { state.linkOptions[field.options].orEmpty().firstOrNull { it.optString("name") == linkedId }
                ?.let { it.optString(state.linkTitleFields[field.options] ?: "name").ifBlank { linkedId } }.orEmpty() }
    }.orEmpty()
    val primaryField = linkedField ?: fields.firstOrNull { it.fieldtype in setOf("Data", "Text") }
    val title = linkedLabel.ifBlank { primaryField?.let { row.optString(it.fieldname) }.orEmpty().ifBlank { "Record ${index + 1}" } }
    val referenceField = linkedSchema.firstOrNull { field ->
        field.fieldtype in setOf("Data", "Small Text") &&
        field.matchesSemanticRole("reference", "code", "sku", "number") &&
            linkedRecord?.opt(field.fieldname)?.let { it != JSONObject.NULL && it.toString().isNotBlank() } == true
    } ?: linkedSchema.firstOrNull { field ->
        field.fieldtype in setOf("Data", "Small Text") && field.inListView && field.fieldname != state.linkTitleFields[linkedField?.options] &&
            linkedRecord?.opt(field.fieldname)?.let { it != JSONObject.NULL && it.toString().isNotBlank() } == true
    }
    val reference = referenceField?.let { linkedRecord?.optString(it.fieldname) }.orEmpty().ifBlank {
        linkedRecord?.keys()?.asSequence()?.firstOrNull { key -> key.contains(Regex("(?i)reference|code|sku|number")) }
            ?.let { linkedRecord.optString(it) }.orEmpty()
    }
    val quantity = quantityField?.let { row.opt(it.fieldname)?.takeUnless { value -> value == JSONObject.NULL }?.toString() }.orEmpty()
    val rateValue = rateField?.let { row.opt(it.fieldname)?.takeUnless { value -> value == JSONObject.NULL } }
    val rate = rateValue?.let { if (rateField?.fieldtype == "Currency") formatCurrency(it, state.currency) else it.toString() }.orEmpty()
    val storedAmount = amountField?.let { row.opt(it.fieldname)?.takeUnless { value -> value == JSONObject.NULL || value.toString().isBlank() } }
    val calculatedAmount = if (quantity.isNotBlank() && rateValue != null) {
        (quantity.toDoubleOrNull() ?: 0.0) * (rateValue.toString().toDoubleOrNull() ?: 0.0)
    } else null
    val amount = storedAmount?.let { if (amountField?.fieldtype == "Currency" || amountField?.label?.contains(Regex("(?i)amount|total")) == true) formatCurrency(it, state.currency) else it.toString() }
        ?: calculatedAmount?.let { formatCurrency(it, state.currency) }.orEmpty()
    val availableField = linkedSchema.firstOrNull { field ->
        field.fieldtype in setOf("Int", "Float", "Currency", "Percent") &&
            field.matchesSemanticRole("stock", "available", "on hand", "remaining")
    }
    val available = availableField?.let { linkedRecord?.opt(it.fieldname)?.toString()?.toDoubleOrNull() }
    val exceedsAvailable = available != null && (quantity.toDoubleOrNull() ?: 0.0) > available
    val secondaryField = fields.firstOrNull { it != primaryField && it != quantityField && it != rateField && it != amountField && it.fieldtype in setOf("Data", "Select") }
    val secondary = secondaryField?.let { row.optString(it.fieldname) }.orEmpty()
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit),
        color = colors.surface,
        shape = RoundedCornerShape(KoraTokens.RadiusCard),
        border = androidx.compose.foundation.BorderStroke(KoraTokens.Hairline, colors.outlineVariant.copy(alpha = .7f)),
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = KoraTokens.CardPaddingHorizontal, vertical = KoraTokens.CardPaddingVertical), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(KoraTokens.SpaceS)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp3)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val codeOrReference = reference.ifBlank { secondary }
                val details = listOfNotNull(codeOrReference.takeIf(String::isNotBlank), listOfNotNull(quantity.takeIf(String::isNotBlank), rate.takeIf(String::isNotBlank)).joinToString(" × ").takeIf(String::isNotBlank)).joinToString(" · ")
                if (details.isNotBlank()) Text(details, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (exceedsAvailable) Surface(color = warningFill, shape = RoundedCornerShape(KoraTokens.TagRadius)) {
                    Text("Exceeds available quantity (${available?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }})", Modifier.padding(horizontal = KoraTokens.TagPaddingHorizontal, vertical = KoraTokens.TagPaddingVertical), style = MaterialTheme.typography.labelSmall, color = warningInk, maxLines = 1)
                }
            }
            if (amount.isNotBlank()) Text(amount, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = colors.onSurface, maxLines = 1)
            Icon(Icons.Outlined.ChevronRight, contentDescription = "Edit ${primaryField?.label?.lowercase() ?: "record"}", tint = colors.onSurfaceVariant, modifier = Modifier.size(KoraMeasures.dp20))
        }
    }
}

private fun JSONObject.displayValue(field: String): String =
    opt(field)?.takeUnless { it == JSONObject.NULL }?.toString().orEmpty()

private fun singularizeDisplayLabel(value: String): String {
    val label = value.trim()
    return when {
        label.endsWith("ies", ignoreCase = true) && label.length > 3 -> label.dropLast(3) + "y"
        label.endsWith("sses", ignoreCase = true) && label.length > 4 -> label.dropLast(2)
        label.endsWith("s", ignoreCase = true) && !label.endsWith("ss", ignoreCase = true) && label.length > 1 -> label.dropLast(1)
        else -> label
    }
}

private fun pluralizeDisplayLabel(value: String, count: Int): String {
    val singular = singularizeDisplayLabel(value).trim().lowercase(Locale.getDefault())
    if (count == 1) return singular
    return when {
        singular.endsWith("y") && singular.length > 1 && singular[singular.lastIndex - 1] !in "aeiou" -> singular.dropLast(1) + "ies"
        singular.endsWith("s") || singular.endsWith("x") || singular.endsWith("ch") || singular.endsWith("sh") -> singular + "es"
        else -> singular + "s"
    }
}

private fun isStockThresholdField(fieldname: String, label: String = fieldname): Boolean {
    val identity = "$fieldname $label".lowercase().replace(Regex("[^a-z0-9]+"), "_")
    return listOf("reorder_level", "reorder_point", "reorder_qty", "minimum_stock", "min_stock", "safety_stock", "low_stock_threshold")
        .any(identity::contains)
}

private fun isStockQuantityField(fieldname: String, label: String = fieldname): Boolean {
    val identity = "$fieldname $label".lowercase().replace(Regex("[^a-z0-9]+"), " ")
    return Regex("\\b(stock|inventory|available|on hand|remaining)\\b").containsMatchIn(identity)
}

private fun stockThreshold(record: JSONObject): Double? =
    numericField(record) { isStockThresholdField(it) }

private fun numericFieldName(record: JSONObject, matches: (String) -> Boolean): String? {
    val keys = record.keys()
    while (keys.hasNext()) {
        val key = keys.next()
        if (matches(key) && record.opt(key) is Number) return key
    }
    return null
}

private fun numericFieldValue(record: JSONObject, matches: (String) -> Boolean): Any? =
    numericFieldName(record, matches)?.let(record::opt)

private fun numericField(record: JSONObject, matches: (String) -> Boolean): Double? =
    numericFieldValue(record, matches)?.toString()?.toDoubleOrNull()

private fun childRowsFromValue(value: Any?): List<Map<String, Any?>> = when (value) {
    is JSONArray -> (0 until value.length()).mapNotNull { index ->
        value.optJSONObject(index)?.let { child -> buildMap { val keys = child.keys(); while (keys.hasNext()) { val key = keys.next(); put(key, child.opt(key)) } } }
    }
    is List<*> -> value.mapNotNull { row ->
        when (row) {
            is Map<*, *> -> row.entries.associate { it.key.toString() to it.value }
            is JSONObject -> buildMap { val keys = row.keys(); while (keys.hasNext()) { val key = keys.next(); put(key, row.opt(key)) } }
            else -> null
        }
    }
    else -> emptyList()
}

private fun Double.toIntIfWhole(): String = if (isFinite() && this == toLong().toDouble()) toLong().toString() else toString()

private fun formatCurrency(value: Any?, currency: String = ""): String {
    if (value == null || value == JSONObject.NULL || value.toString().isBlank() || value.toString().equals("null", true)) return "—"
    val number = value?.toString()?.toDoubleOrNull() ?: return value?.toString() ?: "—"
    val code = currency.uppercase()
    val locale = Locale.getDefault()
    val validCurrency = runCatching { java.util.Currency.getInstance(code) }.getOrNull()
    val digits = validCurrency?.defaultFractionDigits?.coerceAtLeast(0) ?: 2
    val formatter = java.text.NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = digits
        maximumFractionDigits = digits
    }
    val formatted = formatter.format(number)
    return if (validCurrency == null) formatted else "$code $formatted"
}

private fun parseLocalDateTime(value: String?): LocalDateTime? {
    if (value.isNullOrBlank()) return null
    return runCatching { OffsetDateTime.parse(value).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime() }
        .getOrElse { runCatching { LocalDateTime.parse(value.replace(' ', 'T')) }.getOrNull() }
}

private data class MobileReportMetric(val label: String, val value: String)
private data class MobileReportChart(val title: String, val rows: JSONArray, val indexField: String, val seriesField: String, val mode: String, val format: String = "number")

@Composable
private fun ReportChartCard(chart: MobileReportChart, currency: String) {
    val colors = androidx.compose.material3.MaterialTheme.colorScheme
    val values = (0 until chart.rows.length()).mapNotNull { index -> (chart.rows.optJSONObject(index)?.opt(chart.seriesField) as? Number)?.toFloat() }
    Card(shape = RoundedCornerShape(KoraMeasures.dp12), colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow)) {
        Column(Modifier.fillMaxWidth().padding(KoraMeasures.dp18), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp12)) {
            Text(chart.title, style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (values.isEmpty()) Text("No time series is available for this report.", color = colors.onSurfaceVariant)
            else if (values.size == 1) {
                val row = chart.rows.optJSONObject(0) ?: JSONObject()
                val raw = row.opt(chart.seriesField)
                val number = (raw as? Number)?.toDouble()
                val value = when {
                    number == null -> raw?.toString() ?: "—"
                    chart.format == "currency" -> formatCurrency(number, currency)
                    chart.format == "percent" -> "${"%.1f".format(number)}%"
                    else -> java.text.NumberFormat.getNumberInstance().format(number)
                }
                val rawCategory = row.optString(chart.indexField)
                val category = when {
                    rawCategory.isBlank() -> "This period"
                    chart.indexField.contains("date", ignoreCase = true) || chart.indexField.contains("time", ignoreCase = true) -> formatHumanDate(rawCategory, dateOnly = true)
                    else -> rawCategory
                }
                Surface(color = colors.surfaceVariant, shape = RoundedCornerShape(KoraMeasures.dp10)) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = KoraMeasures.dp14, vertical = KoraMeasures.dp13), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(category, color = colors.onSurfaceVariant, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                        Text(value, color = colors.tertiary, style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            else {
                Canvas(Modifier.fillMaxWidth().height(KoraMeasures.dp164)) {
                    for (line in 1..3) {
                        val y = size.height * line / 4f
                        drawLine(colors.outlineVariant.copy(alpha = .35f), Offset(0f, y), Offset(size.width, y), KoraMeasures.dp1.toPx())
                    }
                    val max = values.maxOrNull() ?: 1f
                    val min = values.minOrNull() ?: 0f
                    if (chart.mode == "line" || chart.mode == "area") {
                        val points = values.mapIndexed { index, value ->
                            val x = if (values.size == 1) size.width / 2f else KoraMeasures.dp8.toPx() + index.toFloat() / (values.size - 1) * (size.width - KoraMeasures.dp16.toPx())
                            val ratio = if (max == min) .58f else (value - min) / (max - min)
                            Offset(x, size.height - KoraMeasures.dp12.toPx() - ratio * (size.height - KoraMeasures.dp28.toPx()))
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
                            drawPath(linePath, colors.tertiary, style = androidx.compose.ui.graphics.drawscope.Stroke(width = KoraMeasures.dp3.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
                        }
                        points.lastOrNull()?.let { point ->
                            drawCircle(colors.surface, KoraMeasures.dp6.toPx(), point)
                            drawCircle(colors.tertiary, KoraMeasures.dp4.toPx(), point)
                        }
                    } else {
                        val cellWidth = size.width / values.size
                        val barWidth = cellWidth * .58f
                        values.forEachIndexed { index, value ->
                            val ratio = if (max == min) .72f else ((value - min) / (max - min)).coerceAtLeast(.06f)
                            val height = ratio * (size.height - KoraMeasures.dp12.toPx())
                            val x = index * cellWidth + (cellWidth - barWidth) / 2f
                            drawRoundRect(colors.tertiary, Offset(x, size.height - height), androidx.compose.ui.geometry.Size(barWidth, height), androidx.compose.ui.geometry.CornerRadius(KoraMeasures.dp4.toPx()))
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
private fun ReportsScreen(state: AppState, vm: AppViewModel) {
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
                val display = if (format == "currency") formatCurrency(total, state.currency) else java.text.NumberFormat.getNumberInstance().format(total)
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
        contentPadding = PaddingValues(horizontal = KoraMeasures.dp18, vertical = KoraMeasures.dp14),
        verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp16),
    ) {
        if (state.savedReports.isNotEmpty()) item(key = "saved-report-picker") {
            Column(verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp6)) {
                Text("Choose a report", style = androidx.compose.material3.MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                    state.savedReports.forEach { report ->
                        val name = report.optString("name")
                        FilterChip(selected = state.selectedReportName == name, onClick = { vm.selectSavedReport(name) }, label = { Text(report.optString("label").ifBlank { name }) })
                    }
                }
            }
        }
        item(key = "report-period") {
            Column(verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp6)) {
                Text("Time period", style = androidx.compose.material3.MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                    timeRange.forEach { (days, label) -> FilterChip(selected = state.reportWindowDays == days, onClick = { vm.setReportWindow(days) }, label = { Text(label, style = androidx.compose.material3.MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium) }) }
                    IconButton(onClick = vm::refreshAnalytics, enabled = !state.reportLoading) { Icon(Icons.Filled.Refresh, "Refresh report") }
                }
            }
        }
        if (state.reportLoading) item(key = "report-loading") { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        state.reportError?.let { error -> item(key = "report-error") { MessageBanner("Reporting unavailable: $error", state.offline) } }
        if (state.analyticsCatalog == null && state.reportLoading) {
            item(key = "report-skeleton") { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(KoraMeasures.dp12)) { Box(Modifier.fillMaxWidth().height(KoraMeasures.dp180), contentAlignment = Alignment.Center) { CircularProgressIndicator() } } }
        } else if (!state.reportLoading && state.analyticsCatalog == null) {
            item(key = "report-empty") { EmptyCard("Reports aren’t available", "Connect to the workspace or ask an administrator to enable an approved analytics model.") }
        } else if (state.savedReports.isEmpty()) {
            item(key = "report-not-configured") { EmptyCard("No reports are set up", "Ask your workspace administrator to add the reports your team needs.") }
        } else if (state.reportResult != null && metrics.isEmpty()) {
            item(key = "report-no-data") { EmptyCard("No data for this period", "Choose a longer time period or check again later.") }
        } else if (metrics.isNotEmpty()) {
            item(key = "report-metrics") {
                Column(verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp10)) {
                    Text("At a glance", style = androidx.compose.material3.MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp10)) {
                        metrics.take(2).forEach { metric ->
                            Surface(Modifier.weight(1f), color = colors.surfaceContainerLow, shape = RoundedCornerShape(KoraMeasures.dp12)) {
                                Column(Modifier.padding(KoraMeasures.dp16), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp7)) {
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
                item(key = "report-chart") { ReportChartCard(chart, state.currency) }
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
                    Column(verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                        Text(queryModel?.optString("label")?.ifBlank { result.optString("model") } ?: result.optString("model"), style = androidx.compose.material3.MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        if (rows.length() == 0) Text("No rows in this period.", color = colors.onSurfaceVariant)
                        else for (rowIndex in 0 until minOf(rows.length(), 100)) {
                            val row = rows.optJSONObject(rowIndex) ?: continue
                            val primary = row.optString(rowIndexField).ifBlank { row.optString(columns.optString(0)) }
                            val dataColumns = (0 until columns.length()).map(columns::optString).filterNot { it == rowIndexField }
                            Surface(color = colors.surfaceContainerLow, shape = RoundedCornerShape(KoraMeasures.dp16)) {
                                Column(Modifier.fillMaxWidth().padding(horizontal = KoraMeasures.dp15, vertical = KoraMeasures.dp13), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
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
private fun RecordForm(state: AppState, vm: AppViewModel) {
    val context = LocalContext.current
    val colors = androidx.compose.material3.MaterialTheme.colorScheme
    val visibleFields = state.schemaFields.filter { fieldIsVisible(it, state.formValues) }
    val lineItemsTableField = visibleFields.firstOrNull { field ->
        field.fieldtype == "Table" && state.childSchemas[field.options].orEmpty().let { childFields ->
            childFields.any { it.fieldtype == "Link" } &&
                childFields.any { child -> child.fieldtype in setOf("Int", "Float", "Percent") && child.matchesSemanticRole("quantity", "qty", "units") } &&
                childFields.any { child -> child.fieldtype in setOf("Int", "Float", "Currency", "Percent") && child.matchesSemanticRole("unit price", "price", "rate", "unit cost") }
        }
    }
    val lineItemFields = lineItemsTableField?.let { state.childSchemas[it.options].orEmpty().filterNot { child -> child.hidden || child.fieldtype in setOf("Section Break", "Column Break", "Heading", "Table", "JSON") } }.orEmpty()
    val lineItemRows = lineItemsTableField?.let { childRowsFromValue(state.formValues[it.fieldname]) }.orEmpty()
    val quantityField = lineItemFields.firstOrNull { child -> child.fieldtype in setOf("Int", "Float", "Percent") && child.matchesSemanticRole("quantity", "qty", "units") }
    val unitPriceField = lineItemFields.firstOrNull { child -> child.fieldtype in setOf("Int", "Float", "Currency", "Percent") && child.matchesSemanticRole("unit price", "price", "rate", "unit cost") }
    val lineAmountField = lineItemFields.firstOrNull { child -> child.fieldtype in setOf("Int", "Float", "Currency", "Percent") && (child.computed != null || child.matchesSemanticRole("amount", "total")) && child != unitPriceField }
    val paymentFields = visibleFields.filter { it.fieldtype == "Select" && it.label.contains("payment", true) && it.options.orEmpty().lineSequence().count(String::isNotBlank) in 2..4 }
    val paymentField = paymentFields.firstOrNull { it.label.contains("method", true) }
        ?: paymentFields.firstOrNull { !it.label.contains("status", true) && !it.label.contains("state", true) }
    val lineItemsTotal = lineItemRows.sumOf { row ->
        lineAmountField?.let { row[it.fieldname]?.toString()?.toDoubleOrNull() }
            ?: ((quantityField?.let { row[it.fieldname]?.toString()?.toDoubleOrNull() } ?: 0.0) * (unitPriceField?.let { row[it.fieldname]?.toString()?.toDoubleOrNull() } ?: 0.0))
    }
    val parentTotalField = visibleFields.firstOrNull { it.fieldtype == "Currency" && (it.readOnly || it.label.contains("total", true)) }
    val requiredFieldsValid = formIsValid(state)
    var sectionToggles by rememberSaveable(state.selectedDoctype?.resourceName) { mutableStateOf(emptySet<String>()) }
    val hasAuthoredSections = visibleFields.any { it.fieldtype == "Section Break" }
    val workflowCoreFields = if (lineItemsTableField != null && !hasAuthoredSections) {
        val primaryDateField = visibleFields.firstOrNull { field -> field.fieldtype in setOf("Date", "Datetime", "DateTime") }
        val coreRequiredFields = visibleFields.filter { field ->
            field != primaryDateField && field != lineItemsTableField && field != paymentField && field != parentTotalField &&
                fieldIsRequired(field, state.formValues) &&
                !(field.defaultValue?.isNotBlank() == true && state.formValues[field.fieldname]?.toString().orEmpty().isNotBlank())
        }
        listOfNotNull(primaryDateField, lineItemsTableField) + coreRequiredFields + listOfNotNull(paymentField, parentTotalField)
    } else emptyList()
    val additionalFields = if (workflowCoreFields.isNotEmpty()) visibleFields.filterNot { it in workflowCoreFields } else emptyList()
    val orderedFields = if (workflowCoreFields.isNotEmpty()) {
        workflowCoreFields + if (additionalFields.isNotEmpty()) {
            listOf(FieldSchema(fieldname = "_mobile_more_details", label = "More details", fieldtype = "Section Break", collapsible = true, collapsed = true)) + additionalFields
        } else emptyList()
    } else visibleFields
    var activeSectionIsCollapsed = false
    val displayedFields = buildList {
        orderedFields.forEach { field ->
            if (field.fieldtype == "Section Break") {
                add(field)
                activeSectionIsCollapsed = field.collapsible &&
                    (if (field.fieldname in sectionToggles) !field.collapsed else field.collapsed)
            } else if (!activeSectionIsCollapsed) add(field)
        }
    }
    var lineItemEditor by rememberSaveable(lineItemsTableField?.fieldname) { mutableStateOf<Int?>(null) }
    if (lineItemsTableField != null) LaunchedEffect(lineItemsTableField.options) {
        vm.loadChildSchema(lineItemsTableField.options.orEmpty())
        lineItemFields.firstOrNull { it.fieldtype == "Link" }?.options?.let(vm::loadLinkOptions)
    }
    LaunchedEffect(lineItemsTableField?.fieldname, lineItemRows, lineItemsTotal, parentTotalField?.fieldname) {
        val totalField = parentTotalField ?: return@LaunchedEffect
        val currentTotal = state.formValues[totalField.fieldname]?.toString()?.toDoubleOrNull()
        if (currentTotal == null || kotlin.math.abs(currentTotal - lineItemsTotal) > 0.000001) vm.updateField(totalField.fieldname, lineItemsTotal)
    }
    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(start = KoraTokens.SpaceL, top = KoraTokens.SpaceM, end = KoraTokens.SpaceL, bottom = KoraTokens.SpaceL),
            verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp16),
        ) {
            state.message?.takeUnless(::isPassiveConnectionMessage)?.let { message ->
                item(key = "form-message") { MessageBanner(message, warning = true) }
            }
            items(displayedFields, key = { it.fieldname }) { field ->
                val value = state.formValues[field.fieldname]
                val isComputedField = !field.computed.isNullOrBlank()
                val editable = !isComputedField && (!field.readOnly || (state.editingRecord == null && field.editableOnCreate))
                val required = fieldIsRequired(field, state.formValues)
                val label = buildString {
                    append(requiredFieldLabel(field.label, required))
                    if (isComputedField) append(" (calculated)")
                }
                val constraintHint = fieldConstraintHint(field, value, state.formValues)
                when (field.fieldtype) {
                    "Section Break" -> {
                        val collapsed = field.collapsible &&
                            (if (field.fieldname in sectionToggles) !field.collapsed else field.collapsed)
                        if (field.collapsible) {
                            Row(
                                Modifier.fillMaxWidth().heightIn(min = KoraTokens.TouchTarget)
                                    .clickable {
                                        sectionToggles = if (field.fieldname in sectionToggles) sectionToggles - field.fieldname else sectionToggles + field.fieldname
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(sentenceCaseUiLabel(field.label), Modifier.weight(1f), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = colors.onSurface)
                                Icon(if (collapsed) Icons.Outlined.ChevronRight else Icons.Filled.ArrowDropDown, if (collapsed) "Expand ${field.label}" else "Collapse ${field.label}", tint = colors.onSurfaceVariant)
                            }
                        } else {
                            Column(Modifier.fillMaxWidth().padding(top = KoraMeasures.dp8), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                                Text(sentenceCaseUiLabel(field.label), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = colors.onSurface)
                                Divider(color = colors.outlineVariant.copy(alpha = .7f))
                            }
                        }
                    }
                    "Heading" -> Text(sentenceCaseUiLabel(field.label), Modifier.padding(top = KoraMeasures.dp8), fontWeight = FontWeight.SemiBold, color = colors.onSurfaceVariant)
                    "Column Break" -> Spacer(Modifier.height(KoraMeasures.dp1))
                    "User" -> {
                        val userChoices = buildList {
                            state.user?.name?.takeIf(String::isNotBlank)?.let { add(it) }
                            state.userChoices.keys.filter { it != state.user?.name }.forEach { add(it) }
                        }
                        val userChoiceLabels = buildMap {
                            state.user?.name?.takeIf(String::isNotBlank)?.let { id -> put(id, state.user.fullName.ifBlank { state.user.email.ifBlank { id } }) }
                            putAll(state.userChoices)
                        }
                        ChoiceField(
                            label = label,
                            value = value?.toString().orEmpty(),
                            placeholder = "Choose ${field.label.lowercase()}",
                            choices = userChoices,
                            choiceLabels = userChoiceLabels,
                            allowEmpty = !required,
                            enabled = editable,
                            onSelect = { vm.updateField(field.fieldname, it) },
                        )
                    }
                    "Check" -> Row(Modifier.fillMaxWidth().clickable(enabled = editable) { vm.updateField(field.fieldname, value != true) }, verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = value == true || value.toString() == "1", onCheckedChange = { vm.updateField(field.fieldname, it) }, enabled = editable)
                        Column(Modifier.weight(1f)) {
                            Text(label, fontWeight = if (field.bold) FontWeight.SemiBold else FontWeight.Medium)
                            field.description?.let { Text(it, fontSize = KoraMeasures.sp12, color = colors.onSurfaceVariant) }
                        }
                    }
                    "Select", "Data" -> {
                        if (field.fieldname == paymentField?.fieldname) {
                            Column(verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                                Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                                KoraSegmentedControl(
                                    options = field.options.orEmpty().lineSequence().map(String::trim).filter(String::isNotBlank).distinct().map { it to it }.toList(),
                                    selected = value?.toString().orEmpty(),
                                    onSelect = { vm.updateField(field.fieldname, it) },
                                )
                            }
                        } else {
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
                    }
                    "Int", "Float", "Currency", "Percent" -> KoraTextField(
                        value = if (field.fieldtype == "Currency" && !editable && value != null) {
                            formatCurrency(value, state.currency)
                        } else value?.toString().orEmpty(), onValueChange = { raw ->
                            val parsed: Any? = when (field.fieldtype) { "Int" -> raw.toLongOrNull() ?: raw; else -> raw.toDoubleOrNull() ?: raw }
                            vm.updateField(field.fieldname, if (raw.isEmpty()) null else parsed)
                        }, modifier = Modifier.fillMaxWidth(), label = label,
                        enabled = editable,
                        keyboardOptions = KeyboardOptions(keyboardType = if (field.fieldtype == "Int") KeyboardType.Number else KeyboardType.Decimal),
                        singleLine = true,
                        textAlign = if (field.fieldtype == "Currency" && !editable) androidx.compose.ui.text.style.TextAlign.Start else androidx.compose.ui.text.style.TextAlign.End,
                        leadingIcon = if (field.fieldtype == "Currency" && editable) ({ Text(state.currency, style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant) }) else null,
                    )
                    "Date" -> {
                        var pickerOpen by remember(field.fieldname) { mutableStateOf(false) }
                        var selectedDate by remember(value) { mutableStateOf(runCatching { LocalDate.parse(value?.toString()?.take(10)) }.getOrDefault(LocalDate.now())) }
                        PickerField(label, value?.toString()?.take(10)?.let { runCatching { LocalDate.parse(it).format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())) }.getOrNull() }.orEmpty(), "Choose a date", enabled = editable) { pickerOpen = true }
                        if (pickerOpen) AlertDialog(
                            onDismissRequest = { pickerOpen = false }, containerColor = colors.surface,
                            title = { Text(field.label, fontWeight = FontWeight.SemiBold) },
                            text = { KoraCalendarPicker(selectedDate, onSelect = { selectedDate = it }) },
                            confirmButton = { TextButton(onClick = { vm.updateField(field.fieldname, selectedDate.toString()); pickerOpen = false }) { Text("Save") } },
                            dismissButton = { Row {
                                TextButton(onClick = { selectedDate = LocalDate.now(); vm.updateField(field.fieldname, LocalDate.now().toString()); pickerOpen = false }) { Text("Today") }
                                TextButton(onClick = { pickerOpen = false }) { Text("Cancel") }
                            } },
                        )
                    }
                    "Datetime", "DateTime" -> {
                        val parsedDateTime = remember(value) { parseLocalDateTime(value?.toString()) }
                        var pickerOpen by remember(field.fieldname) { mutableStateOf(false) }
                        val display = parsedDateTime?.format(DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.getDefault())).orEmpty()
                        PickerField(label, display, "Choose date and time", enabled = editable) { pickerOpen = true }
                        if (pickerOpen) {
                            var selectedDate by remember(field.fieldname, parsedDateTime) { mutableStateOf(parsedDateTime?.toLocalDate() ?: LocalDate.now()) }
                            var hour by remember(field.fieldname, parsedDateTime) { mutableStateOf((parsedDateTime ?: LocalDateTime.now()).hour.toString().padStart(2, '0')) }
                            var minute by remember(field.fieldname, parsedDateTime) { mutableStateOf((parsedDateTime ?: LocalDateTime.now()).minute.toString().padStart(2, '0')) }
                            AlertDialog(
                                onDismissRequest = { pickerOpen = false }, containerColor = colors.surface,
                                title = { Text(field.label, fontWeight = FontWeight.SemiBold) },
                                text = {
                                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                                        KoraCalendarPicker(selectedDate, onSelect = { selectedDate = it })
                                        KoraTimeFields(hour, minute, { hour = it }, { minute = it })
                                    }
                                },
                                confirmButton = { TextButton(onClick = {
                                    val h = hour.toIntOrNull()?.coerceIn(0, 23) ?: 0
                                    val m = minute.toIntOrNull()?.coerceIn(0, 59) ?: 0
                                    vm.updateField(field.fieldname, selectedDate.atTime(h, m).atZone(ZoneId.systemDefault()).toOffsetDateTime().toString())
                                    pickerOpen = false
                                }) { Text("Save") } },
                                dismissButton = { Row {
                                    TextButton(onClick = { val now = LocalDateTime.now(); vm.updateField(field.fieldname, now.atZone(ZoneId.systemDefault()).toOffsetDateTime().toString()); pickerOpen = false }) { Text("Now") }
                                    TextButton(onClick = { pickerOpen = false }) { Text("Cancel") }
                                } },
                            )
                        }
                    }
                    "Time" -> {
                        val rawTime = value?.toString().orEmpty().take(5)
                        val currentTime = remember(field.fieldname, value) {
                            val now = LocalDateTime.now()
                            val pieces = rawTime.split(':')
                            val hour = pieces.getOrNull(0)?.toIntOrNull() ?: now.hour
                            val minute = pieces.getOrNull(1)?.toIntOrNull() ?: now.minute
                            hour to minute
                        }
                        var pickerOpen by remember(field.fieldname) { mutableStateOf(false) }
                        var hour by remember(field.fieldname, value) { mutableStateOf(currentTime.first.toString().padStart(2, '0')) }
                        var minute by remember(field.fieldname, value) { mutableStateOf(currentTime.second.toString().padStart(2, '0')) }
                        PickerField(label, rawTime, "Choose time", enabled = editable) { pickerOpen = true }
                        if (pickerOpen) AlertDialog(
                            onDismissRequest = { pickerOpen = false }, containerColor = colors.surface,
                            title = { Text(field.label, fontWeight = FontWeight.SemiBold) },
                            text = { KoraTimeFields(hour, minute, { hour = it }, { minute = it }) },
                            confirmButton = { TextButton(onClick = {
                                val h = hour.toIntOrNull()?.coerceIn(0, 23) ?: 0
                                val m = minute.toIntOrNull()?.coerceIn(0, 59) ?: 0
                                vm.updateField(field.fieldname, "%02d:%02d".format(h, m))
                                pickerOpen = false
                            }) { Text("Save") } },
                            dismissButton = { Row {
                                TextButton(onClick = {
                                    val now = LocalDateTime.now()
                                    vm.updateField(field.fieldname, "%02d:%02d".format(now.hour, now.minute))
                                    pickerOpen = false
                                }) { Text("Now") }
                                TextButton(onClick = { pickerOpen = false }) { Text("Cancel") }
                            } },
                        )
                    }
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
                                Column(Modifier.fillMaxWidth().heightIn(min = KoraMeasures.dp450, max = KoraMeasures.dp720).imePadding().padding(horizontal = KoraMeasures.dp20)) {
                                    Text("Choose ${field.label.lowercase()}", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                                    Text("Select a ${target.lowercase()} record to link to this form.", Modifier.padding(top = KoraMeasures.dp5, bottom = KoraMeasures.dp16), color = colors.onSurfaceVariant, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                                    KoraTextField(
                                        pickerSearch, { pickerSearch = it }, Modifier.fillMaxWidth(), singleLine = true,
                                        leadingIcon = { Icon(Icons.Outlined.Search, null) }, placeholder = { Text("Search ${target.lowercase()}") }
                                    )
                                    val matches = options.filter { item -> item.optString(targetTitleField).contains(pickerSearch, true) || item.optString("name").contains(pickerSearch, true) }
                                    Row(Modifier.fillMaxWidth().padding(top = KoraMeasures.dp12, bottom = KoraMeasures.dp8), verticalAlignment = Alignment.CenterVertically) {
                                        Text(if (pickerSearch.isBlank()) "${options.size} available" else "${matches.size} matches", style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                                        Spacer(Modifier.weight(1f))
                                        if (selectedLabel.isNotBlank()) Text("Selected: $selectedLabel", style = androidx.compose.material3.MaterialTheme.typography.labelSmall, color = colors.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    when {
                                        target in state.linkLoading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { CircularProgressIndicator(); Text("Loading ${target.lowercase()}…", Modifier.padding(top = KoraMeasures.dp12), color = colors.onSurfaceVariant) } }
                                        state.linkErrors[target] != null -> Column(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Filled.CloudOff, null, tint = colors.error, modifier = Modifier.size(KoraMeasures.dp32))
                                            Text("Couldn’t load records", Modifier.padding(top = KoraMeasures.dp10), fontWeight = FontWeight.SemiBold)
                                            Text(state.linkErrors[target].orEmpty(), Modifier.padding(top = KoraMeasures.dp4), color = colors.onSurfaceVariant)
                                            TextButton(onClick = { vm.loadLinkOptions(target) }) { Text("Try again") }
                                        }
                                        options.isEmpty() -> EmptyCard("No records available", "There are no ${target.lowercase()} records to choose from.")
                                        matches.isEmpty() -> EmptyCard("No matches", "Try a different name or reference.")
                                        else -> LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(bottom = KoraMeasures.dp18), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                                            items(matches, key = { it.optString("name") }) { item ->
                                                val itemName = item.optString("name")
                                                val itemLabel = item.optString(targetTitleField).ifBlank { itemName }
                                                val isSelected = value?.toString() == itemName
                                                Card(Modifier.fillMaxWidth().clickable {
                                                    vm.updateLinkedField(field.fieldname, itemName)
                                                    pickerOpen = false
                                                    pickerSearch = ""
                                                }, shape = RoundedCornerShape(KoraMeasures.dp16), colors = CardDefaults.cardColors(containerColor = if (isSelected) colors.secondaryContainer else colors.surfaceVariant.copy(alpha = .45f))) {
                                                    Row(Modifier.fillMaxWidth().padding(horizontal = KoraMeasures.dp14, vertical = KoraMeasures.dp13), verticalAlignment = Alignment.CenterVertically) {
                                                        Box(Modifier.size(KoraMeasures.dp38).background(if (isSelected) colors.primary.copy(alpha = .12f) else colors.surfaceVariant, CircleShape), contentAlignment = Alignment.Center) {
                                                            Icon(Icons.Filled.Inventory2, null, tint = if (isSelected) colors.primary else colors.onSurfaceVariant, modifier = Modifier.size(KoraMeasures.dp19))
                                                        }
                                                        Column(Modifier.weight(1f).padding(start = KoraMeasures.dp12, end = KoraMeasures.dp8)) {
                                                            Text(itemLabel, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                            if (itemLabel != itemName) Text("Ref · $itemName", Modifier.padding(top = KoraMeasures.dp2), style = androidx.compose.material3.MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                        }
                                                        if (isSelected) Icon(Icons.Filled.CheckCircle, null, tint = colors.primary) else Icon(Icons.Outlined.ChevronRight, null, tint = colors.onSurfaceVariant)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(KoraMeasures.dp16))
                                }
                            }
                        }
                    }
                    "Table" -> if (field.fieldname == lineItemsTableField?.fieldname) {
                        LineItemTableSection(
                            table = field,
                            rows = lineItemRows,
                            fields = lineItemFields,
                            state = state,
                            onAdd = { defaults ->
                                vm.updateField(field.fieldname, lineItemRows + defaults)
                                lineItemEditor = lineItemRows.size
                                lineItemFields.firstOrNull { it.fieldtype == "Link" }?.options?.let(vm::loadLinkOptions)
                            },
                            onEdit = { index ->
                                lineItemEditor = index
                                lineItemFields.firstOrNull { it.fieldtype == "Link" }?.options?.let(vm::loadLinkOptions)
                            },
                        )
                    } else ChildTableField(
                        field = field,
                        value = value,
                        enabled = editable,
                        onChange = { vm.updateField(field.fieldname, it) },
                        onLinkedChange = { childDoctype, childField, picked -> vm.updateChildLinkedField(field.fieldname, childDoctype, childField, picked) },
                        onLoadOptions = vm::loadLinkOptions,
                        state = state,
                        onLoadSchema = vm::loadChildSchema,
                    )
                    "Attach", "Attach Image", "Attach Audio" -> Surface(shape = RoundedCornerShape(KoraMeasures.dp12), color = colors.surface, border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant)) {
                        Column(Modifier.fillMaxWidth().padding(KoraMeasures.dp14)) {
                            Text(label, fontWeight = if (field.bold) FontWeight.SemiBold else FontWeight.Medium)
                            Text(value?.toString()?.takeIf(String::isNotBlank) ?: "File uploads are not available in this app yet. Use the website to attach a file.", Modifier.padding(top = KoraMeasures.dp4), fontSize = KoraMeasures.sp12, color = colors.onSurfaceVariant)
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
                val dateTimeDefaultsNow = state.editingRecord == null &&
                    field.fieldtype in setOf("Datetime", "DateTime") &&
                    field.defaultValue?.lowercase(Locale.getDefault()) in setOf("now", "__now__", "\$now")
                val helperText = field.description ?: if (dateTimeDefaultsNow) "Defaults to now." else null
                if (!helperText.isNullOrBlank() && field.fieldtype !in setOf("Check", "Attach", "Attach Image", "Attach Audio", "Table")) {
                    Text(helperText, Modifier.padding(start = KoraMeasures.dp4, top = KoraMeasures.dp4), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
                constraintHint?.let { Text(it, Modifier.padding(start = KoraMeasures.dp4, top = KoraMeasures.dp4), style = MaterialTheme.typography.bodySmall, color = colors.error) }
            }
            if (state.schemaFields.isEmpty()) item { EmptyCard("Form schema unavailable", "Connect to load the field layout for this record type.") }
        }
        Surface(color = colors.surface, border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant.copy(alpha = .7f))) {
            if (lineItemsTableField != null) {
                val typeLabel = state.selectedDoctype?.label?.lowercase(Locale.getDefault()) ?: "record"
                Column(
                    Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(horizontal = KoraTokens.SpaceL, vertical = KoraTokens.SpaceM),
                    verticalArrangement = Arrangement.spacedBy(KoraTokens.SpaceS),
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Total", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                        Text(formatCurrency(lineItemsTotal, state.currency), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = vm::submitRecord,
                        modifier = Modifier.fillMaxWidth().heightIn(min = KoraTokens.ButtonHeight),
                        enabled = !state.busy && requiredFieldsValid,
                        shape = RoundedCornerShape(KoraTokens.RadiusControl),
                    ) {
                        if (state.busy) CircularProgressIndicator(Modifier.size(KoraMeasures.dp18), strokeWidth = KoraMeasures.dp2)
                        else Text(if (state.editingRecord == null) "Save $typeLabel" else "Save changes")
                    }
                }
            } else {
                Row(
                    Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(horizontal = KoraTokens.SpaceL, vertical = KoraTokens.SpaceM),
                    horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp10),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = vm::backFromForm, enabled = !state.busy, modifier = Modifier.heightIn(min = KoraTokens.ButtonHeight)) { Text("Cancel") }
                    Button(
                        onClick = vm::submitRecord,
                        modifier = Modifier.weight(1f).heightIn(min = KoraTokens.ButtonHeight),
                        enabled = !state.busy && requiredFieldsValid,
                        shape = RoundedCornerShape(KoraTokens.RadiusControl),
                    ) {
                        if (state.busy) CircularProgressIndicator(Modifier.size(KoraMeasures.dp18), strokeWidth = KoraMeasures.dp2)
                        else Text(if (state.editingRecord == null) "Create ${state.selectedDoctype?.label ?: "record"}" else "Save changes")
                    }
                }
            }
        }
    }
    if (lineItemEditor != null && lineItemsTableField != null) ChildRowEditorSheet(
        rowIndex = lineItemEditor!!,
        tableLabel = lineItemsTableField.label,
        fields = lineItemFields,
        rows = lineItemRows,
        state = state,
        onDismiss = { lineItemEditor = null },
        onRowsChange = { vm.updateField(lineItemsTableField.fieldname, it) },
        onLoadOptions = vm::loadLinkOptions,
    )
}

@Composable
private fun LineItemTableSection(
    table: FieldSchema,
    rows: List<Map<String, Any?>>,
    fields: List<FieldSchema>,
    state: AppState,
    onAdd: (Map<String, Any?>) -> Unit,
    onEdit: (Int) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val rowLabel = singularizeDisplayLabel(table.label).ifBlank { "record" }
    val rowLabelLower = rowLabel.replaceFirstChar { it.lowercase(Locale.getDefault()) }
    val tableLabelLower = table.label.trim().lowercase(Locale.getDefault()).ifBlank { "records" }
    val linkedField = fields.firstOrNull { it.fieldtype == "Link" }
    val quantityField = fields.firstOrNull { it.fieldtype in setOf("Int", "Float", "Percent") && it.matchesSemanticRole("quantity", "qty", "units") }
    val unitPriceField = fields.firstOrNull { it.fieldtype in setOf("Int", "Float", "Currency", "Percent") && it.matchesSemanticRole("unit price", "price", "rate", "unit cost") }
    val lineAmountField = fields.firstOrNull { it.fieldtype in setOf("Int", "Float", "Currency", "Percent") && (it.computed != null || it.matchesSemanticRole("amount", "total")) && it != unitPriceField }

    Column(verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp10)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(sentenceCaseUiLabel(table.label), Modifier.widthIn(max = KoraMeasures.dp160), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.width(KoraMeasures.dp8))
            Text("${rows.size} ${if (rows.size == 1) rowLabelLower else tableLabelLower}", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 1)
            OutlinedButton(
                onClick = { onAdd(fields.mapNotNull { child -> child.defaultValue?.let { child.fieldname to parseChildDefault(child.fieldtype, it) } }.toMap()) },
                modifier = Modifier.heightIn(min = KoraMeasures.dp44),
                shape = RoundedCornerShape(KoraTokens.RadiusControl),
            ) {
                Icon(Icons.Outlined.Add, null, modifier = Modifier.size(KoraMeasures.dp18))
                Spacer(Modifier.width(KoraMeasures.dp4))
                Text("Add $rowLabelLower")
            }
        }
        if (rows.isEmpty()) {
            val article = if (rowLabelLower.firstOrNull() in setOf('a', 'e', 'i', 'o', 'u')) "an" else "a"
            KoraEmptyState("No ${table.label.lowercase(Locale.getDefault())} added", "Add $article $rowLabelLower to calculate the total.")
        }
        else rows.forEachIndexed { index, row ->
            val linkedId = linkedField?.let { row[it.fieldname]?.toString().orEmpty() }.orEmpty()
            val linkedRecord = linkedField?.let { field -> state.linkOptions[field.options].orEmpty().firstOrNull { it.optString("name") == linkedId } }
            val linkedLabel = linkedRecord?.optString(state.linkTitleFields[linkedField?.options] ?: "name")?.ifBlank { linkedId }
                ?: state.detailLinkLabels["${linkedField?.options}:$linkedId"]
                ?: linkedId.ifBlank { "Choose ${linkedField?.label?.replaceFirstChar { it.lowercase(Locale.getDefault()) } ?: "record"}" }
            val quantity = quantityField?.let { row[it.fieldname]?.toString()?.toDoubleOrNull() } ?: 0.0
            val price = unitPriceField?.let { row[it.fieldname]?.toString()?.toDoubleOrNull() } ?: 0.0
            val availableQuantity = linkedRecord?.let { data -> numericField(data) { it.contains("stock", true) || it.contains("available", true) } }
            val lineAmount = lineAmountField?.let { row[it.fieldname]?.toString()?.toDoubleOrNull() } ?: quantity * price
            Card(
                shape = RoundedCornerShape(KoraTokens.RadiusCard),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant),
            ) {
                Row(
                    Modifier.fillMaxWidth().clickable { onEdit(index) }.padding(horizontal = KoraTokens.CardPaddingHorizontal, vertical = KoraTokens.CardPaddingVertical),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp10),
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp4)) {
                        Text(linkedLabel, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        val referenceField = fields.firstOrNull { field -> field != linkedField && field != quantityField && field != unitPriceField && field != lineAmountField && field.label.contains(Regex("(?i)reference|code|sku|number")) }
                        val reference = referenceField?.let { row[it.fieldname]?.toString().orEmpty() }.orEmpty()
                        Text(
                            listOf(reference.takeIf(String::isNotBlank), "${quantity.toIntIfWhole()} × ${formatCurrency(price, state.currency)}").filterNotNull().joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (availableQuantity != null && quantity > availableQuantity) {
                            Text("Exceeds available quantity (${availableQuantity.toIntIfWhole()})", style = MaterialTheme.typography.labelSmall, color = colors.error)
                        } else if (availableQuantity != null && stockThreshold(linkedRecord ?: JSONObject())?.let { availableQuantity <= it } == true) {
                            Text("Low available quantity (${availableQuantity.toIntIfWhole()})", style = MaterialTheme.typography.labelSmall, color = warningForeground(colors))
                        }
                    }
                    Text(formatCurrency(lineAmount, state.currency), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    Icon(Icons.Outlined.ChevronRight, null, tint = colors.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
private fun KoraTextField(
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
    textAlign: androidx.compose.ui.text.style.TextAlign = androidx.compose.ui.text.style.TextAlign.Start,
) {
    val colors = androidx.compose.material3.MaterialTheme.colorScheme
    val type = androidx.compose.material3.MaterialTheme.typography
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val shape = RoundedCornerShape(KoraTokens.RadiusField)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(KoraTokens.FieldLabelGap)) {
        if (!label.isNullOrBlank()) {
            Text(label, style = type.labelMedium, fontWeight = FontWeight.Medium, color = colors.onSurface)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = if (minLines > 1) KoraMeasures.dp60 else KoraTokens.FieldHeight)
                .background(if (enabled) colors.surface else colors.surfaceContainerHigh, shape)
                .border(if (focused) KoraTokens.FocusRing else KoraTokens.Hairline, if (focused) colors.primary else colors.outlineVariant, shape)
                .semantics { label?.let { contentDescription = it } }
                .padding(horizontal = KoraTokens.SpaceM, vertical = KoraMeasures.dp5),
            verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
        ) {
            leadingIcon?.let { icon ->
                Box(Modifier.padding(end = KoraTokens.SpaceS), contentAlignment = Alignment.Center) { icon() }
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f).padding(vertical = KoraMeasures.dp0)
                    .semantics { testTagsAsResourceId = true; label?.let { contentDescription = "$it input" } }
                    .testTag("input-" + label.orEmpty().lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')),
                enabled = enabled,
                readOnly = readOnly,
                textStyle = type.bodyMedium.copy(color = if (enabled) colors.onSurface else colors.onSurfaceVariant, textAlign = textAlign),
                keyboardOptions = keyboardOptions,
                singleLine = singleLine,
                minLines = minLines,
                visualTransformation = visualTransformation,
                interactionSource = interactionSource,
                decorationBox = { innerTextField ->
                    Box(
                        Modifier.fillMaxWidth(),
                        contentAlignment = if (textAlign == androidx.compose.ui.text.style.TextAlign.End) Alignment.CenterEnd else Alignment.CenterStart,
                    ) {
                        if (value.isEmpty() && placeholder != null) {
                            placeholder()
                        }
                        innerTextField()
                    }
                },
            )
            trailingIcon?.let { icon ->
                Box(Modifier.padding(start = KoraTokens.SpaceS), contentAlignment = Alignment.Center) { icon() }
            }
        }
    }
}

@Composable
private fun ChoiceField(
    label: String,
    value: String,
    placeholder: String,
    choices: List<String>,
    allowEmpty: Boolean,
    enabled: Boolean,
    onSelect: (String) -> Unit,
    choiceLabels: Map<String, String> = emptyMap(),
) {
    var expanded by remember(label) { mutableStateOf(false) }
    val colors = androidx.compose.material3.MaterialTheme.colorScheme
    Box(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp3)) {
            Text(label, style = androidx.compose.material3.MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = colors.onSurface)
            Surface(
                modifier = Modifier.fillMaxWidth().heightIn(min = KoraMeasures.dp48).clickable(enabled = enabled) { expanded = true },
                shape = RoundedCornerShape(KoraTokens.RadiusField),
                color = colors.surfaceContainerLow,
                border = androidx.compose.foundation.BorderStroke(if (expanded) KoraMeasures.dp2 else KoraMeasures.dp1, if (expanded) colors.primary else colors.outlineVariant),
            ) {
                Row(Modifier.fillMaxWidth().padding(horizontal = KoraMeasures.dp12, vertical = KoraMeasures.dp8), verticalAlignment = Alignment.CenterVertically) {
                    Text((choiceLabels[value] ?: value).ifBlank { placeholder }, Modifier.weight(1f), style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = if (value.isBlank()) colors.onSurfaceVariant else colors.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Icon(Icons.Filled.ArrowDropDown, null, tint = if (expanded) colors.primary else colors.onSurfaceVariant)
                }
            }
        }
        val menuOptions = buildList {
            if (allowEmpty && value.isNotBlank()) add("" to "None")
            choices.forEach { add(it to it) }
        }
        val menuViewport = KoraMeasures.dp48 * menuOptions.size.coerceAtMost(4).coerceAtLeast(1).toFloat()
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(.88f),
            shape = RoundedCornerShape(KoraMeasures.dp2),
            containerColor = colors.surface,
            tonalElevation = KoraMeasures.dp1,
            shadowElevation = KoraMeasures.dp4,
            border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant),
        ) {
            Column(Modifier.height(menuViewport).verticalScroll(rememberScrollState())) {
                if (menuOptions.isEmpty()) {
                    Text("No options available", Modifier.padding(horizontal = KoraMeasures.dp16, vertical = KoraMeasures.dp14), color = colors.onSurfaceVariant, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                } else menuOptions.forEach { (option, optionLabel) ->
                    val selected = option == value
                    DropdownMenuItem(
                        text = { Text(choiceLabels[optionLabel] ?: optionLabel, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal) },
                        onClick = { onSelect(option); expanded = false },
                        modifier = Modifier.fillMaxWidth().height(KoraMeasures.dp48).background(if (selected) colors.secondaryContainer else colors.surface),
                        trailingIcon = { if (selected) Icon(Icons.Filled.CheckCircle, null, tint = colors.primary, modifier = Modifier.size(KoraMeasures.dp18)) },
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
private fun PickerField(label: String, value: String, placeholder: String, enabled: Boolean, onClick: () -> Unit) {
    val colors = androidx.compose.material3.MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp3)) {
        Text(label, style = androidx.compose.material3.MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = colors.onSurface)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = KoraMeasures.dp48)
                .clickable(enabled = enabled, onClick = onClick)
                .semantics(mergeDescendants = true) {
                    contentDescription = "$label: ${value.ifBlank { placeholder }}"
                    testTagsAsResourceId = true
                }
                .testTag("picker-field-" + label.lowercase(Locale.getDefault()).replace(Regex("[^a-z0-9]+"), "-").trim('-')),
            shape = RoundedCornerShape(KoraTokens.RadiusField),
            color = colors.surfaceContainerLow,
            border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant),
        ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = KoraMeasures.dp12, vertical = KoraMeasures.dp6), verticalAlignment = Alignment.CenterVertically) {
                Text(value.ifBlank { placeholder }, Modifier.weight(1f), style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = if (value.isBlank()) colors.onSurfaceVariant else colors.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Icon(Icons.Filled.ArrowDropDown, null, tint = colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ChildTableField(
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
    val rowNoun = singularizeDisplayLabel(field.label).ifBlank { "record" }
    val rowPlural = field.label.trim().ifBlank { "records" }
    var expandedRow by rememberSaveable(field.fieldname) { mutableStateOf<Int?>(null) }
    val childFields = state.childSchemas[childType].orEmpty().filter { !it.hidden && it.fieldtype !in setOf("Section Break", "Column Break", "Heading", "Table") }
    val rows: List<Map<String, Any?>> = when (value) {
        is JSONArray -> (0 until value.length()).mapNotNull { index ->
            value.optJSONObject(index)?.let { child -> buildMap {
                val keys = child.keys()
                while (keys.hasNext()) { val key = keys.next(); put(key, child.opt(key)) }
            } }
        }
        is List<*> -> value.mapNotNull { row -> (row as? Map<*, *>)?.entries?.associate { it.key.toString() to it.value } }
        else -> emptyList()
    }
    androidx.compose.runtime.LaunchedEffect(childType) { if (childType.isNotBlank()) onLoadSchema(childType) }
    Column(verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp10)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(field.label, fontWeight = if (field.bold) FontWeight.SemiBold else FontWeight.Medium)
                Text(if (rows.isEmpty()) "Add ${rowPlural.lowercase(Locale.getDefault())} to this record" else "${rows.size} ${if (rows.size == 1) rowNoun.lowercase(Locale.getDefault()) else rowPlural.lowercase(Locale.getDefault())}", style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
            }
            OutlinedButton(enabled = enabled && childFields.isNotEmpty(), onClick = {
                val defaults = childFields.mapNotNull { f -> f.defaultValue?.let { f.fieldname to parseChildDefault(f.fieldtype, it) } }.toMap()
                onChange(rows + defaults)
                expandedRow = rows.size
            }, shape = RoundedCornerShape(KoraMeasures.dp14)) {
                Icon(Icons.Outlined.Add, null, modifier = Modifier.size(KoraMeasures.dp18))
                Text(" Add ${rowNoun.lowercase(Locale.getDefault())}")
            }
        }
        when {
            childType.isBlank() -> EmptyCard("Child type missing", "Set the table field’s child doctype in the website configuration.")
            childType !in state.childSchemas -> Card(shape = RoundedCornerShape(KoraMeasures.dp16), colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant.copy(alpha = .5f))) {
                Row(Modifier.fillMaxWidth().padding(KoraMeasures.dp16), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp12)) { CircularProgressIndicator(Modifier.size(KoraMeasures.dp20), strokeWidth = KoraMeasures.dp2); Text("Loading $childType fields…", color = colors.onSurfaceVariant) }
            }
            childFields.isEmpty() -> EmptyCard("No editable columns", "This child type has no fields available for entry.")
            rows.isEmpty() -> Card(shape = RoundedCornerShape(KoraMeasures.dp12), colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant.copy(alpha = .35f))) {
                Column(Modifier.fillMaxWidth().padding(horizontal = KoraMeasures.dp20, vertical = KoraMeasures.dp24), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(KoraMeasures.dp44).background(colors.primary.copy(alpha = .10f), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Inventory2, null, tint = colors.primary) }
                    Text("No ${rowPlural.lowercase(Locale.getDefault())} added", Modifier.padding(top = KoraMeasures.dp12), fontWeight = FontWeight.SemiBold)
                    Text("Add a row to start building this section.", Modifier.padding(top = KoraMeasures.dp4), style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
            childFields.any { it.fieldtype == "Link" } &&
                childFields.any { it.fieldtype in setOf("Int", "Float", "Currency", "Percent") && it.matchesSemanticRole("quantity", "qty", "units") } &&
                childFields.any { it.fieldtype in setOf("Int", "Float", "Currency", "Percent") && it.matchesSemanticRole("unit price", "price", "rate", "unit cost") } -> ChildLineCards(
                childDoctype = childType, tableLabel = field.label, fields = childFields, rows = rows, enabled = enabled, state = state,
                onChange = onChange, onLinkedChange = onLinkedChange, onLoadOptions = onLoadOptions,
            )
            else -> Column(verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp10)) {
                rows.forEachIndexed { index, row ->
                    val primaryField = childFields.firstOrNull { it.fieldtype == "Link" }
                        ?: childFields.firstOrNull { it.fieldtype in setOf("Data", "Select", "Int", "Float", "Currency", "Date", "Datetime", "DateTime") }
                    val rawPrimaryValue = primaryField?.let { row[it.fieldname]?.toString().orEmpty() }.orEmpty()
                    val primaryValue = if (primaryField?.fieldtype == "Link") {
                        state.detailLinkLabels["${primaryField.options}:$rawPrimaryValue"]
                            ?: state.linkOptions[primaryField.options].orEmpty().firstOrNull { it.optString("name") == rawPrimaryValue }
                                ?.let { option -> option.optString(state.linkTitleFields[primaryField.options] ?: "name").ifBlank { rawPrimaryValue } }
                            ?: rawPrimaryValue
                    } else rawPrimaryValue
                    val expanded = expandedRow == index
                    Card(shape = RoundedCornerShape(KoraMeasures.dp12), colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow)) {
                        Column(Modifier.fillMaxWidth()) {
                            Row(
                                Modifier.fillMaxWidth().clickable { expandedRow = if (expanded) null else index }.padding(start = KoraMeasures.dp12, end = KoraMeasures.dp4, top = KoraMeasures.dp6, bottom = KoraMeasures.dp6),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp10),
                            ) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp3)) {
                                    Text(primaryValue.ifBlank { "Item ${index + 1}" }, style = androidx.compose.material3.MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    val quantityField = childFields.firstOrNull { it.fieldtype == "Int" }
                                        ?: childFields.firstOrNull { it.fieldtype in setOf("Float", "Percent") }
                                    val amountField = childFields.lastOrNull { it.fieldtype == "Currency" && it != primaryField }
                                    val quantityText = quantityField?.let { field -> row[field.fieldname]?.toString()?.takeIf(String::isNotBlank)?.let { quantity -> "${field.label} $quantity" } }
                                    val amountText = amountField?.let { amount -> row[amount.fieldname]?.toString()?.takeIf(String::isNotBlank)?.let { formatCurrency(it, state.currency) } }
                                    val summary = listOfNotNull(quantityText, amountText).joinToString(" · ").ifBlank { "Row ${index + 1}" }
                                    Text(if (expanded) "Editing · $summary" else summary, style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                IconButton(enabled = enabled, onClick = { onChange(rows.filterIndexed { rowIndex, _ -> rowIndex != index }); if (expandedRow == index) expandedRow = null }, modifier = Modifier.size(KoraMeasures.dp40)) { Icon(Icons.Outlined.DeleteOutline, "Remove item", tint = colors.error) }
                                Icon(if (expanded) Icons.Filled.ArrowDropDown else Icons.Outlined.ChevronRight, contentDescription = if (expanded) "Collapse item" else "Expand item", tint = colors.onSurfaceVariant)
                            }
                            if (expanded) Column(Modifier.fillMaxWidth().padding(start = KoraMeasures.dp12, end = KoraMeasures.dp12, bottom = KoraMeasures.dp12), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                            childFields.forEach { childField ->
                                val childValue = row[childField.fieldname]
                                val childLabel = requiredFieldLabel(childField.label, childField.reqd)
                                when (childField.fieldtype) {
                                    "Select", "Data" -> {
                                        val choices = childField.options.orEmpty().lineSequence().map(String::trim).filter(String::isNotBlank).distinct().toList()
                                        if (childField.fieldtype == "Select" || (childField.fieldtype == "Data" && choices.size > 1)) ChoiceField(label = childLabel, value = childValue?.toString().orEmpty(), placeholder = "Choose ${childField.label.lowercase()}", choices = choices, allowEmpty = !childField.reqd, enabled = enabled && !childField.readOnly, onSelect = { selected -> onChange(rows.mapIndexed { ri, old -> if (ri == index) old + (childField.fieldname to selected.ifBlank { null }) else old }) })
                                        else KoraTextField(childValue?.toString().orEmpty(), { typed -> onChange(rows.mapIndexed { ri, old -> if (ri == index) old + (childField.fieldname to typed) else old }) }, Modifier.fillMaxWidth(), label = childLabel, enabled = enabled && !childField.readOnly, singleLine = true)
                                    }
                                    "Int", "Float", "Currency", "Percent" -> KoraTextField(childValue?.toString().orEmpty(), { typed ->
                                        val parsed: Any? = if (childField.fieldtype == "Int") typed.toLongOrNull() ?: typed else typed.toDoubleOrNull() ?: typed
                                        onChange(rows.mapIndexed { ri, old -> if (ri == index) old + (childField.fieldname to parsed.takeIf { typed.isNotEmpty() }) else old })
                                    }, Modifier.fillMaxWidth(), label = childLabel, enabled = enabled && !childField.readOnly, singleLine = true, textAlign = androidx.compose.ui.text.style.TextAlign.End, leadingIcon = if (childField.fieldtype == "Currency") ({ Text(state.currency, style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant) }) else null, keyboardOptions = KeyboardOptions(keyboardType = if (childField.fieldtype == "Int") KeyboardType.Number else KeyboardType.Decimal))
                                    "Check" -> Row(Modifier.fillMaxWidth().clickable(enabled = enabled && !childField.readOnly) { onChange(rows.mapIndexed { ri, old -> if (ri == index) old + (childField.fieldname to (old[childField.fieldname] != true)) else old }) }, verticalAlignment = Alignment.CenterVertically) { Checkbox(childValue == true || childValue.toString() == "1", { checked -> onChange(rows.mapIndexed { ri, old -> if (ri == index) old + (childField.fieldname to checked) else old }) }, enabled = enabled && !childField.readOnly); Text(childLabel) }
                                    "Link", "Dynamic Link" -> {
                                        val target = if (childField.fieldtype == "Dynamic Link") row[childField.options.orEmpty()]?.toString().orEmpty() else childField.options.orEmpty()
                                        val linkedOptions = state.linkOptions[target].orEmpty()
                                        val titleKey = state.linkTitleFields[target] ?: "name"
                                        val linkedName = childValue?.toString().orEmpty()
                                        val display = state.detailLinkLabels["$target:$linkedName"]
                                            ?: linkedOptions.firstOrNull { it.optString("name") == linkedName }?.let { it.optString(titleKey).ifBlank { it.optString("name") } }
                                            ?: linkedName
                                        var open by remember(field.fieldname, index, childField.fieldname) { mutableStateOf(false) }
                                        PickerField(childLabel, display, if (target.isBlank()) "Choose a link type first" else "Choose ${childField.label.lowercase()}", enabled && !childField.readOnly && target.isNotBlank()) { open = true; onLoadOptions(target) }
                                        if (open) ModalBottomSheet(onDismissRequest = { open = false }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = colors.surface) {
                                            Column(Modifier.fillMaxWidth().heightIn(max = KoraMeasures.dp600).padding(horizontal = KoraMeasures.dp18)) {
                                                Text("Choose ${childField.label.lowercase()}", style = androidx.compose.material3.MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                                                val opts = linkedOptions
                                                if (target in state.linkLoading) Box(Modifier.fillMaxWidth().height(KoraMeasures.dp160), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                                                else if (state.linkErrors[target] != null) Column(Modifier.fillMaxWidth().height(KoraMeasures.dp200), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) { Text("Couldn’t load records", fontWeight = FontWeight.SemiBold); Text(state.linkErrors[target].orEmpty(), color = colors.onSurfaceVariant); TextButton(onClick = { onLoadOptions(target) }) { Text("Try again") } }
                                                else if (opts.isEmpty()) EmptyCard("No records available", "There are no ${target.lowercase()} records to choose from.")
                                                else LazyColumn(Modifier.heightIn(max = KoraMeasures.dp460), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp6)) {
                                                    items(opts, key = { it.optString("name") }) { option ->
                                                        val id = option.optString("name")
                                                        val title = option.optString(titleKey).ifBlank { id }
                                                        TextButton(onClick = { onLinkedChange(childType, childField.fieldname, id); open = false }, modifier = Modifier.fillMaxWidth()) { Column(Modifier.fillMaxWidth()) { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis); if (title != id) Text("Ref · $id", color = colors.onSurfaceVariant, style = androidx.compose.material3.MaterialTheme.typography.labelSmall) } }
                                                    }
                                                }
                                                Spacer(Modifier.height(KoraMeasures.dp20))
                                            }
                                        }
                                    }
                                    "Date" -> {
                                        var open by remember(field.fieldname, index, childField.fieldname) { mutableStateOf(false) }
                                        val date = childValue?.toString()?.take(10).orEmpty()
                                        PickerField(childLabel, date, "Choose a date", enabled && !childField.readOnly) { open = true }
                                        if (open) {
                                            var selected by remember(field.fieldname, index, date) { mutableStateOf(runCatching { LocalDate.parse(date) }.getOrDefault(LocalDate.now())) }
                                            AlertDialog(onDismissRequest = { open = false }, containerColor = colors.surface,
                                                title = { Text(childField.label, fontWeight = FontWeight.SemiBold) },
                                                text = { KoraCalendarPicker(selected, onSelect = { selected = it }) },
                                                confirmButton = { TextButton(onClick = { onChange(rows.mapIndexed { ri, old -> if (ri == index) old + (childField.fieldname to selected.toString()) else old }); open = false }) { Text("Save") } },
                                                dismissButton = { TextButton(onClick = { open = false }) { Text("Cancel") } })
                                        }
                                    }
                                    else -> KoraTextField(childValue?.toString().orEmpty(), { typed -> onChange(rows.mapIndexed { ri, old -> if (ri == index) old + (childField.fieldname to typed) else old }) }, Modifier.fillMaxWidth(), label = childLabel, enabled = enabled && !childField.readOnly, minLines = if (childField.fieldtype in setOf("Text", "Small Text", "Long Text")) 3 else 1)
                                }
                            }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChildRowEditorSheet(
    rowIndex: Int,
    tableLabel: String,
    fields: List<FieldSchema>,
    rows: List<Map<String, Any?>>,
    state: AppState,
    onDismiss: () -> Unit,
    onRowsChange: (List<Map<String, Any?>>) -> Unit,
    onLoadOptions: (String) -> Unit,
) {
    if (rowIndex !in rows.indices) return
    val colors = MaterialTheme.colorScheme
    val original = rows[rowIndex]
    val linkField = fields.firstOrNull { it.fieldtype == "Link" }
    val quantityField = fields.firstOrNull { it.fieldtype in setOf("Int", "Float", "Percent") && it.matchesSemanticRole("quantity", "qty", "units") }
    val rateField = fields.firstOrNull { it.fieldtype in setOf("Int", "Float", "Currency", "Percent") && it.matchesSemanticRole("unit price", "price", "rate", "unit cost") }
    val amountField = fields.firstOrNull { it.fieldtype in setOf("Int", "Float", "Currency", "Percent") && (it.computed != null || it.matchesSemanticRole("amount", "total")) && it != rateField }
    val itemizedRow = quantityField != null && rateField != null
    val additionalFields = fields.filterNot { field ->
        field.hidden || field.readOnly || field == linkField || (itemizedRow && field in listOfNotNull(quantityField, rateField, amountField)) ||
            field.fieldname in setOf("name", "_version", "doc_status", "idx") ||
            field.fieldtype in setOf("Section Break", "Column Break", "Heading", "Table", "JSON", "Password", "Attach", "Attach Image")
    }
    var draft by remember(rowIndex) { mutableStateOf(original) }
    val linkedName = linkField?.let { draft[it.fieldname]?.toString().orEmpty() }.orEmpty()
    val options = linkField?.let { state.linkOptions[it.options].orEmpty() }.orEmpty()
    val selectedRecord = options.firstOrNull { it.optString("name") == linkedName }
    val titleKey = linkField?.let { state.linkTitleFields[it.options] } ?: "name"
    val title = selectedRecord?.optString(titleKey).orEmpty().ifBlank { state.detailLinkLabels["${linkField?.options}:$linkedName"].orEmpty() }.ifBlank { linkedName }
    val quantity = quantityField?.let { draft[it.fieldname]?.toString()?.toDoubleOrNull() } ?: 0.0
    val price = rateField?.let { draft[it.fieldname]?.toString()?.toDoubleOrNull() } ?: 0.0
    val catalogPriceField = rateField?.takeIf { itemizedRow }?.linkedField?.substringAfter('.', "")?.takeIf(String::isNotBlank)
    val stockFieldName = selectedRecord?.takeIf { itemizedRow }?.let { numericFieldName(it) { label -> label.contains("stock", true) || label.contains("available", true) } }
    val stock = selectedRecord?.let { record -> stockFieldName?.let { record.opt(it)?.takeUnless { value -> value == JSONObject.NULL }?.toString()?.toDoubleOrNull() } }
    val linkedTypeLabel = linkField?.label?.replaceFirstChar { it.lowercase(Locale.getDefault()) } ?: "record"
    val rowNoun = singularizeDisplayLabel(tableLabel).ifBlank { "record" }
    val rowTitle = title.ifBlank { "${sentenceCaseUiLabel(rowNoun)} ${rowIndex + 1}" }
    val editorFieldsLabel = listOfNotNull(linkField?.label, quantityField?.label, rateField?.label).joinToString(", ").replaceFirstChar { it.lowercase(Locale.getDefault()) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = colors.surface) {
        Column(Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = KoraMeasures.dp20).padding(bottom = KoraMeasures.dp20), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp14)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(rowTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold); if (editorFieldsLabel.isNotBlank()) Text("Update $editorFieldsLabel", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant) }
                TextButton(onClick = { onRowsChange(rows.filterIndexed { index, _ -> index != rowIndex }); onDismiss() }, modifier = Modifier.heightIn(min = KoraMeasures.dp44)) { Icon(Icons.Outlined.DeleteOutline, null); Text(" Remove") }
            }
            if (linkField != null) {
                var pickerOpen by remember(rowIndex) { mutableStateOf(false) }
                PickerField(requiredFieldLabel(linkField.label, fieldIsRequired(linkField, draft)), title.ifBlank { "Choose $linkedTypeLabel" }, "Choose $linkedTypeLabel", enabled = true) {
                    pickerOpen = true
                    onLoadOptions(linkField.options.orEmpty())
                }
                if (pickerOpen) ModalBottomSheet(onDismissRequest = { pickerOpen = false }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = colors.surface) {
                    Column(Modifier.fillMaxWidth().heightIn(max = KoraMeasures.dp620).padding(horizontal = KoraMeasures.dp18)) {
                        Text("Choose ${linkField.label.lowercase(Locale.getDefault())}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        val error = state.linkErrors[linkField.options]
                        when {
                            linkField.options in state.linkLoading -> Box(Modifier.fillMaxWidth().height(KoraMeasures.dp160), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                            error != null -> Column(Modifier.padding(KoraMeasures.dp18)) { Text("Couldn’t load ${linkField.label.lowercase(Locale.getDefault())} records"); Text(error, color = colors.onSurfaceVariant); TextButton(onClick = { onLoadOptions(linkField.options.orEmpty()) }) { Text("Try again") } }
                            options.isEmpty() -> EmptyCard("No records available", "There are no ${linkField.options.orEmpty().lowercase(Locale.getDefault())} records to choose from.")
                            else -> LazyColumn(Modifier.heightIn(max = KoraMeasures.dp500)) {
                                items(options, key = { it.optString("name") }) { option ->
                                    val id = option.optString("name")
                                    val label = option.optString(titleKey).ifBlank { id }
                                    val linkedRateValue = catalogPriceField?.let(option::opt)
                                        ?.takeUnless { it == JSONObject.NULL }?.toString()?.toDoubleOrNull()
                                    TextButton(onClick = {
                                        draft = draft + (linkField.fieldname to id) + listOfNotNull(rateField?.let { field -> linkedRateValue?.let { field.fieldname to it } }).toMap()
                                        val qty = quantityField?.let { draft[it.fieldname]?.toString()?.toDoubleOrNull() } ?: 0.0
                                        if (amountField != null && linkedRateValue != null) draft = draft + (amountField.fieldname to qty * linkedRateValue)
                                        pickerOpen = false
                                    }, modifier = Modifier.fillMaxWidth().heightIn(min = KoraMeasures.dp52)) {
                                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp2)) {
                                            Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            val detail = listOfNotNull(
                                                id.takeIf { title != id }?.let { "Ref · $id" },
                                                linkedRateValue?.takeIf { itemizedRow }?.let { formatCurrency(it, state.currency) },
                                                if (itemizedRow) numericFieldValue(option) { it.contains("stock", true) || it.contains("available", true) }
                                                    ?.takeUnless { it == JSONObject.NULL }?.toString()?.let { "$it available" } else null,
                                            ).joinToString(" · ")
                                            if (detail.isNotBlank()) Text(detail, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (itemizedRow) selectedRecord?.let { record ->
                    val linkedPrice = catalogPriceField?.let(record::opt)?.takeUnless { it == JSONObject.NULL }
                    val available = stockFieldName?.let(record::opt)?.takeUnless { it == JSONObject.NULL }
                    Text("${rateField?.label ?: "Price"}: ${linkedPrice?.let { formatCurrency(it, state.currency) } ?: "—"}${available?.let { " · ${stockFieldName?.replace('_', ' ')}: $it" }.orEmpty()}", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
            if (itemizedRow) quantityField?.let { field -> KoraTextField(draft[field.fieldname]?.toString().orEmpty(), { raw ->
                val parsed = raw.toDoubleOrNull()
                draft = draft + (field.fieldname to if (field.fieldtype == "Int") parsed?.toLong() ?: raw else parsed ?: raw)
                if (amountField != null) draft = draft + (amountField.fieldname to ((parsed ?: 0.0) * price))
            }, Modifier.fillMaxWidth(), label = requiredFieldLabel(field.label, fieldIsRequired(field, draft)), singleLine = true, textAlign = androidx.compose.ui.text.style.TextAlign.End, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                fieldConstraintHint(field, draft[field.fieldname], draft)?.let { hint -> Text(hint, style = MaterialTheme.typography.bodySmall, color = colors.error) }
            }
            if (itemizedRow) rateField?.let { field -> KoraTextField(draft[field.fieldname]?.toString().orEmpty(), { raw ->
                val parsed = raw.toDoubleOrNull()
                draft = draft + (field.fieldname to (parsed ?: raw))
                if (amountField != null) draft = draft + (amountField.fieldname to (quantity * (parsed ?: 0.0)))
            }, Modifier.fillMaxWidth(), label = requiredFieldLabel(field.label, fieldIsRequired(field, draft)), singleLine = true, textAlign = androidx.compose.ui.text.style.TextAlign.End, leadingIcon = { Text(state.currency, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                fieldConstraintHint(field, draft[field.fieldname], draft)?.let { hint -> Text(hint, style = MaterialTheme.typography.bodySmall, color = colors.error) }
            }
            additionalFields.forEach { field ->
                val currentValue = draft[field.fieldname]
                val label = requiredFieldLabel(field.label, fieldIsRequired(field, draft))
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp4)) {
                    when (field.fieldtype) {
                    "Check" -> Row(
                        Modifier.fillMaxWidth().heightIn(min = KoraMeasures.dp48).clickable { draft = draft + (field.fieldname to (currentValue != true && currentValue?.toString() != "1")) },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = currentValue == true || currentValue?.toString() == "1",
                            onCheckedChange = { draft = draft + (field.fieldname to it) },
                        )
                        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    }
                    "Select" -> {
                        val choices = field.options.orEmpty().lineSequence().map(String::trim).filter(String::isNotBlank).distinct().toList()
                        if (choices.isNotEmpty()) ChoiceField(
                            label = label,
                            value = currentValue?.toString().orEmpty(),
                            placeholder = "Choose ${field.label.lowercase(Locale.getDefault())}",
                            choices = choices,
                            allowEmpty = !fieldIsRequired(field, draft),
                            enabled = true,
                            onSelect = { draft = draft + (field.fieldname to it) },
                        ) else KoraTextField(
                            currentValue?.toString().orEmpty(), { draft = draft + (field.fieldname to it) },
                            Modifier.fillMaxWidth(), label = label, singleLine = true,
                        )
                    }
                    "Int", "Float", "Currency", "Percent" -> KoraTextField(
                        currentValue?.toString().orEmpty(), { raw ->
                            val parsed: Any? = if (field.fieldtype == "Int") raw.toLongOrNull() ?: raw else raw.toDoubleOrNull() ?: raw
                            draft = draft + (field.fieldname to if (raw.isBlank()) null else parsed)
                        },
                        Modifier.fillMaxWidth(), label = label, singleLine = true,
                        textAlign = androidx.compose.ui.text.style.TextAlign.End,
                        leadingIcon = if (field.fieldtype == "Currency") ({ Text(state.currency, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant) }) else null,
                        keyboardOptions = KeyboardOptions(keyboardType = if (field.fieldtype == "Int") KeyboardType.Number else KeyboardType.Decimal),
                    )
                    "Date" -> {
                        var datePickerOpen by remember(rowIndex, field.fieldname) { mutableStateOf(false) }
                        val selectedDate = runCatching { LocalDate.parse(currentValue?.toString()?.take(10)) }.getOrDefault(LocalDate.now())
                        PickerField(
                            label, currentValue?.toString()?.take(10).orEmpty(), "Choose a date", enabled = true,
                        ) { datePickerOpen = true }
                        if (datePickerOpen) {
                            var date by remember(rowIndex, field.fieldname, currentValue) { mutableStateOf(selectedDate) }
                            AlertDialog(
                                onDismissRequest = { datePickerOpen = false }, containerColor = colors.surface,
                                title = { Text(field.label, fontWeight = FontWeight.SemiBold) },
                                text = { KoraCalendarPicker(date, onSelect = { date = it }) },
                                confirmButton = { TextButton(onClick = { draft = draft + (field.fieldname to date.toString()); datePickerOpen = false }) { Text("Save") } },
                                dismissButton = { TextButton(onClick = { datePickerOpen = false }) { Text("Cancel") } },
                            )
                        }
                    }
                    "Link" -> {
                        var linkPickerOpen by remember(rowIndex, field.fieldname) { mutableStateOf(false) }
                        val linkOptions = state.linkOptions[field.options].orEmpty()
                        val linkedId = currentValue?.toString().orEmpty()
                        val linkedLabel = linkOptions.firstOrNull { it.optString("name") == linkedId }
                            ?.let { it.optString(state.linkTitleFields[field.options] ?: "name").ifBlank { linkedId } }
                            ?: state.detailLinkLabels["${field.options}:$linkedId"].orEmpty().ifBlank { linkedId }
                        PickerField(label, linkedLabel, "Choose ${field.label.lowercase(Locale.getDefault())}", enabled = true) {
                            linkPickerOpen = true
                            onLoadOptions(field.options.orEmpty())
                        }
                        if (linkPickerOpen) ModalBottomSheet(
                            onDismissRequest = { linkPickerOpen = false },
                            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                            containerColor = colors.surface,
                        ) {
                            Column(Modifier.fillMaxWidth().heightIn(max = KoraMeasures.dp600).padding(horizontal = KoraMeasures.dp18)) {
                                Text("Choose ${field.label.lowercase(Locale.getDefault())}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                                when {
                                    field.options in state.linkLoading -> Box(Modifier.fillMaxWidth().height(KoraMeasures.dp140), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                                    state.linkErrors[field.options] != null -> Column(Modifier.padding(KoraMeasures.dp16)) {
                                        Text("Couldn’t load ${field.label.lowercase(Locale.getDefault())} records")
                                        Text(state.linkErrors[field.options].orEmpty(), color = colors.onSurfaceVariant)
                                        TextButton(onClick = { onLoadOptions(field.options.orEmpty()) }) { Text("Try again") }
                                    }
                                    linkOptions.isEmpty() -> EmptyCard("No records available", "There are no linked records to choose from.")
                                    else -> LazyColumn(Modifier.heightIn(max = KoraMeasures.dp500)) {
                                        items(linkOptions, key = { it.optString("name") }) { option ->
                                            val id = option.optString("name")
                                            val display = option.optString(state.linkTitleFields[field.options] ?: "name").ifBlank { id }
                                            TextButton(onClick = { draft = draft + (field.fieldname to id); linkPickerOpen = false }, modifier = Modifier.fillMaxWidth().heightIn(min = KoraMeasures.dp48)) {
                                                Text(display, Modifier.fillMaxWidth(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else -> KoraTextField(
                        currentValue?.toString().orEmpty(), { draft = draft + (field.fieldname to it) },
                        Modifier.fillMaxWidth(), label = label, singleLine = field.fieldtype !in setOf("Text", "Small Text", "Long Text"),
                        minLines = if (field.fieldtype in setOf("Text", "Small Text", "Long Text")) 3 else 1,
                    )
                    }
                    fieldConstraintHint(field, draft[field.fieldname], draft)?.let { hint ->
                        Text(hint, style = MaterialTheme.typography.bodySmall, color = colors.error)
                    }
                }
            }
            if (itemizedRow) stock?.let { available ->
                if (quantity > available) MessageBanner("Quantity exceeds available amount (${available.toIntIfWhole()})", true)
                else if (selectedRecord?.let(::stockThreshold)?.let { available <= it } == true) Text("Low available amount (${available.toIntIfWhole()})", style = MaterialTheme.typography.labelMedium, color = warningForeground(colors))
            }
            if (itemizedRow) {
                Surface(color = colors.surfaceContainerLow, shape = RoundedCornerShape(KoraTokens.RadiusCard)) {
                    Row(Modifier.fillMaxWidth().padding(KoraMeasures.dp15), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(amountField?.label ?: "Line total", color = colors.onSurfaceVariant)
                        Text(formatCurrency(quantity * price, state.currency), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
            val visibleDraftFields = fields.filter { !it.hidden && it.fieldtype !in setOf("Section Break", "Column Break", "Heading") }
            val requiredFieldsValid = visibleDraftFields.filter { fieldIsRequired(it, draft) }.all { field ->
                val value = draft[field.fieldname]
                value != null && value != JSONObject.NULL && value.toString().isNotBlank()
            }
            val constraintsValid = visibleDraftFields.none { field -> fieldConstraintHint(field, draft[field.fieldname], draft) != null }
            Button(
                onClick = { onRowsChange(rows.mapIndexed { index, row -> if (index == rowIndex) draft else row }); onDismiss() },
                enabled = requiredFieldsValid && constraintsValid,
                modifier = Modifier.fillMaxWidth().heightIn(min = KoraMeasures.dp52),
                shape = RoundedCornerShape(KoraTokens.RadiusControl),
            ) { Text("Done") }
        }
    }
}

@Composable
private fun KoraTimeFields(
    hour: String,
    minute: String,
    onHourChange: (String) -> Unit,
    onMinuteChange: (String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp12)) {
            KoraTextField(
                hour,
                { onHourChange(it.filter(Char::isDigit).take(2)) },
                Modifier.weight(1f),
                label = "Hour",
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            KoraTextField(
                minute,
                { onMinuteChange(it.filter(Char::isDigit).take(2)) },
                Modifier.weight(1f),
                label = "Minute",
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
        Text("24-hour time", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
    }
}

@Composable
private fun KoraCalendarPicker(selectedDate: LocalDate, onSelect: (LocalDate) -> Unit) {
    val colors = MaterialTheme.colorScheme
    var visibleMonth by remember(selectedDate) { mutableStateOf(java.time.YearMonth.from(selectedDate)) }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp5)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { visibleMonth = visibleMonth.minusMonths(1) }, contentPadding = PaddingValues(horizontal = KoraMeasures.dp8, vertical = KoraMeasures.dp2)) { Text("‹", fontSize = KoraMeasures.sp24) }
            Text(visibleMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            TextButton(onClick = { visibleMonth = visibleMonth.plusMonths(1) }, contentPadding = PaddingValues(horizontal = KoraMeasures.dp8, vertical = KoraMeasures.dp2)) { Text("›", fontSize = KoraMeasures.sp24) }
        }
        Row(Modifier.fillMaxWidth()) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                Text(day, Modifier.weight(1f).height(KoraMeasures.dp28), textAlign = androidx.compose.ui.text.style.TextAlign.Center, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
            }
        }
        val firstOffset = visibleMonth.atDay(1).dayOfWeek.value % 7
        val dayCount = visibleMonth.lengthOfMonth()
        (0 until 6).forEach { week ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp2)) {
                (0 until 7).forEach { weekday ->
                    val day = week * 7 + weekday - firstOffset + 1
                    if (day in 1..dayCount) {
                        val date = visibleMonth.atDay(day)
                        val selected = date == selectedDate
                        val today = date == LocalDate.now()
                        Surface(
                            Modifier.weight(1f).height(KoraMeasures.dp38).clickable { onSelect(date) },
                            shape = RoundedCornerShape(KoraMeasures.dp10),
                            color = if (selected) colors.primary else colors.surface,
                            border = androidx.compose.foundation.BorderStroke(if (today && !selected) KoraMeasures.dp1 else KoraMeasures.dp0, colors.primary.copy(alpha = .55f)),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(day.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = if (selected || today) FontWeight.SemiBold else FontWeight.Normal, color = if (selected) colors.onPrimary else colors.onSurface)
                            }
                        }
                    } else Spacer(Modifier.weight(1f).height(KoraMeasures.dp38))
                }
            }
        }
        Text(selectedDate.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.getDefault())), Modifier.fillMaxWidth(), style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ChildLineCards(
    childDoctype: String,
    tableLabel: String,
    fields: List<FieldSchema>,
    rows: List<Map<String, Any?>>,
    enabled: Boolean,
    state: AppState,
    onChange: (List<Map<String, Any?>>) -> Unit,
    onLinkedChange: (String, String, Any?) -> Unit,
    onLoadOptions: (String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val linkedField = fields.firstOrNull { it.fieldtype == "Link" }
    val numericFields = fields.filter { it.fieldtype in setOf("Int", "Float", "Currency", "Percent") }
    val quantity = numericFields.firstOrNull { it.matchesSemanticRole("quantity", "qty", "units") }
    val unitPrice = numericFields.firstOrNull { it.matchesSemanticRole("unit price", "price", "rate", "unit cost") }
    val amount = numericFields.firstOrNull { it != unitPrice && (it.computed != null || it.matchesSemanticRole("amount", "total")) }
    val referenceField = fields.firstOrNull { it != linkedField && it != quantity && it != unitPrice && it != amount && it.fieldtype in setOf("Data", "Select") }
    var editingRow by rememberSaveable(childDoctype) { mutableStateOf<Int?>(null) }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
        rows.forEachIndexed { index, row ->
            val linkedId = linkedField?.let { row[it.fieldname]?.toString().orEmpty() }.orEmpty()
            val option = linkedField?.let { field -> state.linkOptions[field.options].orEmpty().firstOrNull { it.optString("name") == linkedId } }
            val displayName = option?.optString(linkedField?.let { state.linkTitleFields[it.options] } ?: "name").orEmpty()
                .ifBlank { linkedField?.let { state.detailLinkLabels["${it.options}:$linkedId"] }.orEmpty() }
                .ifBlank { linkedId.ifBlank { "Choose ${linkedField?.label?.lowercase(Locale.getDefault()) ?: "record"}" } }
            val reference = referenceField?.let { row[it.fieldname]?.toString().orEmpty() }.orEmpty()
            val quantityValue = quantity?.let { row[it.fieldname]?.toString()?.toDoubleOrNull() } ?: 0.0
            val unitPriceValue = unitPrice?.let { row[it.fieldname]?.toString()?.toDoubleOrNull() } ?: 0.0
            val total = amount?.let { row[it.fieldname]?.toString()?.toDoubleOrNull() } ?: quantityValue * unitPriceValue
            val available = option?.let { linked -> numericField(linked) { key ->
                listOf("stock", "available", "on hand", "remaining").any { term -> key.replace('_', ' ').contains(term, true) }
            } }
            val exceedsAvailable = available != null && quantityValue > available
            var menuOpen by remember(index, rows.size) { mutableStateOf(false) }
            Surface(
                modifier = Modifier.fillMaxWidth().clickable(enabled = enabled) { editingRow = index },
                color = colors.surface,
                shape = RoundedCornerShape(KoraTokens.RadiusCard),
                border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant.copy(alpha = .75f)),
            ) {
                Row(Modifier.fillMaxWidth().padding(start = KoraMeasures.dp12, end = KoraMeasures.dp4, top = KoraMeasures.dp10, bottom = KoraMeasures.dp10), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp4)) {
                        Text(displayName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        val summary = listOfNotNull(
                            reference.takeIf(String::isNotBlank),
                            listOfNotNull(quantityValue.takeIf { quantity?.let { field -> row[field.fieldname] != null } == true }?.let { it.toIntIfWhole() }, unitPriceValue.takeIf { unitPrice?.let { field -> row[field.fieldname] != null } == true }?.let { formatCurrency(it, state.currency) }).joinToString(" × ").takeIf(String::isNotBlank),
                        ).joinToString(" · ")
                        if (summary.isNotBlank()) Text(summary, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (exceedsAvailable) Surface(color = warningContainer(colors), shape = RoundedCornerShape(KoraMeasures.dp4)) {
                            Text("${quantity?.label ?: "Quantity"} exceeds available (${available?.toIntIfWhole()})", Modifier.padding(horizontal = KoraMeasures.dp8, vertical = KoraMeasures.dp2), style = MaterialTheme.typography.labelSmall, color = warningForeground(colors), maxLines = 1)
                        }
                    }
                    Text(formatCurrency(total, state.currency), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = colors.onSurface, maxLines = 1)
                    Box {
                        IconButton(onClick = { menuOpen = true }, enabled = enabled, modifier = Modifier.size(KoraMeasures.dp40)) { Icon(Icons.Outlined.MoreVert, "Row actions") }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(text = { Text("Edit row") }, onClick = { menuOpen = false; editingRow = index })
                            DropdownMenuItem(text = { Text("Remove row", color = colors.error) }, onClick = { menuOpen = false; onChange(rows.filterIndexed { rowIndex, _ -> rowIndex != index }) })
                        }
                    }
                }
            }
        }
    }
    editingRow?.let { index ->
        ChildRowEditorSheet(
            rowIndex = index,
            tableLabel = tableLabel,
            fields = fields,
            rows = rows,
            state = state,
            onDismiss = { editingRow = null },
            onRowsChange = onChange,
            onLoadOptions = onLoadOptions,
        )
    }
}

private fun parseChildDefault(type: String, value: String): Any? = when (type) {
    "Check" -> value == "1" || value.equals("true", true)
    "Int" -> value.toLongOrNull() ?: value
    "Float", "Currency", "Percent" -> value.toDoubleOrNull() ?: value
    else -> value
}

private fun fieldIsVisible(field: FieldSchema, values: Map<String, Any?>): Boolean =
    !field.hidden && evaluateFieldCondition(field.dependsOn, values, true)

private val uiAcronyms = setOf("AI", "API", "CSV", "ERP", "HTML", "ID", "JSON", "KES", "KRA", "OTP", "PDF", "POS", "QR", "SKU", "SMS", "SLA", "URL", "UTC", "VAT")

/** Present configurable field labels in sentence case without mutating their stored metadata. */
private fun sentenceCaseUiLabel(label: String): String {
    val sentence = label.trim().lowercase(Locale.getDefault()).replaceFirstChar { it.titlecase(Locale.getDefault()) }
    return Regex("\\b[A-Za-z]{2,}\\b").replace(sentence) { match ->
        uiAcronyms.firstOrNull { it.equals(match.value, ignoreCase = true) } ?: match.value
    }
}

private fun requiredFieldLabel(label: String, required: Boolean): String {
    val displayLabel = sentenceCaseUiLabel(label)
    return if (required) "$displayLabel (required)" else displayLabel
}

/** Infer reusable presentation roles from DocType metadata, never from a DocType or module name. */
private fun FieldSchema.matchesSemanticRole(vararg terms: String): Boolean {
    val metadata = "$label ${fieldname.replace('_', ' ')}"
    return terms.any { term ->
        Regex("(^|[^a-z0-9])${Regex.escape(term)}([^a-z0-9]|$)", RegexOption.IGNORE_CASE).containsMatchIn(metadata)
    }
}

private fun fieldIsRequired(field: FieldSchema, values: Map<String, Any?>): Boolean =
    field.reqd || evaluateFieldCondition(field.mandatoryDependsOn, values, false)

private fun formIsValid(state: AppState): Boolean {
    val values = state.formValues
    val visibleFields = state.schemaFields.filter { fieldIsVisible(it, values) }
    val requiredValid = visibleFields.filter { fieldIsRequired(it, values) }.all { field ->
        if (field.fieldtype == "Table") {
            val rows = childRowsFromValue(values[field.fieldname])
            rows.isNotEmpty() && rows.all { row ->
                state.childSchemas[field.options].orEmpty().filter { child -> !child.hidden && fieldIsRequired(child, row) }.all { child ->
                    row[child.fieldname] != null && row[child.fieldname] != JSONObject.NULL && row[child.fieldname].toString().isNotBlank()
                }
            }
        } else {
            val value = values[field.fieldname]
            value != null && value != JSONObject.NULL && value.toString().isNotBlank()
        }
    }
    if (!requiredValid) return false
    if (visibleFields.any { field -> fieldConstraintHint(field, values[field.fieldname], values) != null }) return false
    return visibleFields.filter { it.fieldtype == "Table" }.all { table ->
        val rows = childRowsFromValue(values[table.fieldname])
        val childFields = state.childSchemas[table.options].orEmpty().filterNot { it.hidden }
        rows.all { row ->
            val childRequiredValid = childFields.filter { fieldIsRequired(it, row) }.all { field ->
                val value = row[field.fieldname]
                value != null && value != JSONObject.NULL && value.toString().isNotBlank()
            }
            val childConstraintsValid = childFields.none { field -> fieldConstraintHint(field, row[field.fieldname], row) != null }
            childRequiredValid && childConstraintsValid
        }
    }
}

private fun fieldConstraintHint(field: FieldSchema, value: Any?, values: Map<String, Any?>): String? {
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

private fun evaluateFieldCondition(condition: String?, values: Map<String, Any?>, fallback: Boolean): Boolean {
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

private fun fieldTruthy(value: Any?): Boolean = when (value) {
    null -> false
    is Boolean -> value
    is Number -> value.toDouble() != 0.0
    else -> value.toString().isNotBlank() && value.toString() != "0" && !value.toString().equals("false", true)
}

@Composable
private fun RequestsScreen(state: AppState) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = KoraMeasures.dp18), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp11)) {
        item { Spacer(Modifier.height(KoraMeasures.dp8)); SectionHeading("Requests", "Needs action") }
        if (state.requests.isEmpty()) item { EmptyCard("Nothing needs your attention", if (state.offline) "No cached requests are available. Reconnect to refresh." else "Requests assigned to you will show up here.") }
        items(state.requests) { request ->
            val title = request.optString("title", "Request")
            val message = request.optString("message")
            val status = request.optString("status", "Open")
            Card(shape = RoundedCornerShape(KoraMeasures.dp16)) {
                Column(Modifier.fillMaxWidth().padding(KoraMeasures.dp16)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Text(title, Modifier.weight(1f), fontWeight = FontWeight.SemiBold); Text(status.replace('_', ' '), color = androidx.compose.material3.MaterialTheme.colorScheme.primary, fontSize = KoraMeasures.sp11) }
                    if (message.isNotBlank()) Text(message, Modifier.padding(top = KoraMeasures.dp5), color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun NotificationScreen(state: AppState, vm: AppViewModel) {
    var filter by rememberSaveable { mutableStateOf("All") }
    val colors = MaterialTheme.colorScheme
    val notifications = state.notifications.filterNot { it.optBoolean("archived") || notificationIsSnoozed(it) }
    val filtered = notifications.filter { notification -> when (filter) {
        "Unread" -> !notification.optBoolean("read")
        "Alerts" -> notificationHasAlertCategory(notification)
        else -> true
    } }
    val unreadCount = notifications.count { !it.optBoolean("read") }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().background(colors.surface).horizontalScroll(rememberScrollState()).padding(horizontal = KoraMeasures.dp16, vertical = KoraMeasures.dp10), horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
            KoraChip("All", filter == "All") { filter = "All" }
            KoraChip("Unread $unreadCount", filter == "Unread") { filter = "Unread" }
            KoraChip("Alerts", filter == "Alerts") { filter = "Alerts" }
        }
        when {
            filtered.isEmpty() -> EmptyCard(if (filter == "Unread") "No unread notifications" else "No notifications yet", if (state.offline) "Reconnect to receive updates. Existing notifications remain available on this device." else "Updates delivered to your account will appear here.")
            else -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = KoraMeasures.dp16)) {
                val grouped = filtered.groupBy { notificationIsToday(it.optString("created_at")) }
                if (grouped[true].orEmpty().isNotEmpty()) {
                    item { Text("Today", Modifier.fillMaxWidth().background(colors.background).padding(start = KoraMeasures.dp16, top = KoraMeasures.dp14, bottom = KoraMeasures.dp6), fontSize = KoraMeasures.sp13, color = colors.onSurfaceVariant) }
                    items(grouped[true].orEmpty(), key = { it.optString("id") }) { notification -> NotificationRow(notification, vm) }
                }
                if (grouped[false].orEmpty().isNotEmpty()) {
                    item { Text("Earlier", Modifier.fillMaxWidth().background(colors.background).padding(start = KoraMeasures.dp16, top = KoraMeasures.dp14, bottom = KoraMeasures.dp6), fontSize = KoraMeasures.sp13, color = colors.onSurfaceVariant) }
                    items(grouped[false].orEmpty(), key = { it.optString("id") }) { notification -> NotificationRow(notification, vm) }
                }
            }
        }
    }
}

@Composable
private fun NotificationPopover(
    state: AppState,
    filter: String,
    onFilterChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onMarkAllRead: () -> Unit,
    onOpen: (JSONObject) -> Unit,
    onViewAll: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val unread = state.notifications.count { !it.optBoolean("read") && !it.optBoolean("archived") && !notificationIsSnoozed(it) }
    val entries = state.notifications.filterNot { it.optBoolean("archived") || notificationIsSnoozed(it) }
        .filter { filter != "Unread" || !it.optBoolean("read") }.take(3)
    Popup(
        alignment = Alignment.TopStart,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true, dismissOnBackPress = true, dismissOnClickOutside = true),
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                Spacer(Modifier.height(KoraMeasures.dp62))
                Box(Modifier.fillMaxSize().background(KoraTokens.Scrim).clickable(onClick = onDismiss))
            }
            Surface(
                Modifier.align(Alignment.TopEnd).fillMaxWidth().widthIn(max = KoraMeasures.dp400).padding(start = KoraMeasures.dp8, end = KoraMeasures.dp8, top = KoraMeasures.dp62),
                color = colors.surface,
                contentColor = colors.onSurface,
                shape = RoundedCornerShape(KoraMeasures.dp14),
                border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant),
                shadowElevation = KoraMeasures.dp12,
            ) {
                Column(Modifier.padding(horizontal = KoraMeasures.dp14, vertical = KoraMeasures.dp10)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Notifications", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        TextButton(
                            onClick = onMarkAllRead,
                            enabled = unread > 0,
                            contentPadding = PaddingValues(horizontal = KoraMeasures.dp6),
                        ) {
                            Text(
                                "Mark all read",
                                fontSize = KoraMeasures.sp13,
                                fontWeight = FontWeight.SemiBold,
                                color = if (unread > 0) colors.primary else colors.onSurfaceVariant,
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp6), modifier = Modifier.padding(top = KoraMeasures.dp4, bottom = KoraMeasures.dp6)) {
                        KoraChip("All", filter == "All") { onFilterChange("All") }
                        KoraChip("Unread $unread", filter == "Unread") { onFilterChange("Unread") }
                    }
                    if (entries.isEmpty()) {
                        Text(if (state.offline) "Reconnect to receive updates." else "You're all caught up.", Modifier.fillMaxWidth().padding(vertical = KoraMeasures.dp14), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    } else entries.forEachIndexed { index, notification ->
                        Row(
                            Modifier.fillMaxWidth().clickable { onOpen(notification) }.padding(vertical = KoraMeasures.dp9),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp9),
                        ) {
                            Box(Modifier.padding(top = KoraMeasures.dp5).size(KoraMeasures.dp8).background(if (notification.optBoolean("read")) Color.Transparent else colors.primary, CircleShape))
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp2)) {
                                Text(notification.optString("title").ifBlank { "Workspace update" }, style = MaterialTheme.typography.bodyMedium, fontWeight = if (notification.optBoolean("read")) FontWeight.Normal else FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(notification.optString("message").ifBlank { "Open to view details." }, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                            Text(notificationRelativeTime(notification.optString("created_at")), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                        }
                        if (index < entries.lastIndex) Divider(color = colors.outlineVariant.copy(alpha = .6f))
                    }
                    OutlinedButton(
                        onClick = onViewAll,
                        modifier = Modifier.fillMaxWidth().padding(top = KoraMeasures.dp4).heightIn(min = KoraTokens.SecondaryButtonHeight),
                        shape = RoundedCornerShape(KoraTokens.RadiusControl),
                        contentPadding = PaddingValues(horizontal = KoraMeasures.dp16),
                    ) { Text("View all") }
                }
            }
        }
    }
}

private fun notificationHasAlertCategory(notification: JSONObject): Boolean =
    notification.optString("category").trim().lowercase() in setOf("alert", "alerts")

@Composable
private fun NotificationRow(notification: JSONObject, vm: AppViewModel) {
    val colors = MaterialTheme.colorScheme
    var revealArchive by remember(notification.optString("id")) { mutableStateOf(false) }
    var swipeDistance by remember(notification.optString("id")) { mutableFloatStateOf(0f) }
    Row(Modifier.fillMaxWidth().background(colors.surface).pointerInput(notification.optString("id")) {
        detectHorizontalDragGestures(onDragEnd = {
            if (swipeDistance < -36f) revealArchive = true else if (swipeDistance > 36f) revealArchive = false
            swipeDistance = 0f
        }) { _, delta -> swipeDistance += delta }
    }.clickable { if (revealArchive) revealArchive = false else vm.openNotification(notification) }.padding(start = KoraMeasures.dp16, end = KoraMeasures.dp8, top = KoraMeasures.dp8, bottom = KoraMeasures.dp8), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp10)) {
        Box(Modifier.size(KoraMeasures.dp8).background(if (notification.optBoolean("read")) Color.Transparent else colors.primary, CircleShape))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp3)) {
            Text(notification.optString("title").ifBlank { "Workspace update" }, fontWeight = if (notification.optBoolean("read")) FontWeight.Normal else FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (notification.optString("message").isNotBlank()) Text(notification.optString("message"), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        if (revealArchive) TextButton(onClick = { vm.archiveNotification(notification) }, contentPadding = PaddingValues(horizontal = KoraMeasures.dp8, vertical = KoraMeasures.dp0)) { Text("Archive", style = MaterialTheme.typography.labelSmall) }
        else Text(formatNotificationTime(notification.optString("created_at")), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
    }
    Divider(color = colors.outlineVariant.copy(alpha = .65f))
}

@Composable
private fun NotificationDialog(notification: JSONObject, navigation: NavigationConfig?, vm: AppViewModel) {
    val colors = MaterialTheme.colorScheme
    Dialog(onDismissRequest = vm::closeNotification, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxWidth().padding(horizontal = KoraMeasures.dp20), contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colors.surface,
                contentColor = colors.onSurface,
                shape = RoundedCornerShape(KoraMeasures.dp18),
                border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant),
                shadowElevation = KoraMeasures.dp12,
            ) {
                Box {
                    Column(
                        Modifier.fillMaxWidth().padding(horizontal = KoraMeasures.dp20, vertical = KoraMeasures.dp22),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp10),
                    ) {
                        Spacer(Modifier.height(KoraMeasures.dp20))
                        KoriMascot(Modifier.size(KoraMeasures.dp64), colors.primary, KoriMood.Alert)
                        Text(notification.optString("title").ifBlank { "Notification" }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Text(notificationSummaryTime(notification.optString("created_at")), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                        Text(
                            notification.optString("message").ifBlank { "There are no additional details." },
                            Modifier.fillMaxWidth().padding(top = KoraMeasures.dp2, bottom = KoraMeasures.dp6),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                        val requestId = notification.optString("request_id")
                        val action = notification.optJSONObject("action")
                        val actionTarget = action?.optJSONObject("target") ?: action ?: JSONObject()
                        val targetDoctype = actionTarget.optString("doctype").ifBlank { notification.optString("target_doctype") }
                        val targetName = actionTarget.optString("name")
                            .ifBlank { actionTarget.optString("id") }
                            .ifBlank { notification.optString("target_name") }
                        val targetMetadata = navigation?.modules?.flatMap { it.doctypes }
                            ?.firstOrNull { !it.isChild && (it.name.equals(targetDoctype, true) || it.resourceName.equals(targetDoctype, true)) }
                        val targetLabel = targetMetadata?.label?.takeIf(String::isNotBlank)
                            ?: sentenceCaseUiLabel(targetDoctype)
                        val canOpenRecord = targetMetadata != null && targetName.isNotBlank()
                        val supportsPrimaryAction = canOpenRecord || requestId.isNotBlank()
                        val primaryActionLabel = if (!supportsPrimaryAction) "Done" else {
                            action?.optString("label")?.takeIf(String::isNotBlank)
                                ?: notification.optString("action_label").takeIf(String::isNotBlank)
                                ?: if (canOpenRecord) "Open ${targetLabel.lowercase()}" else "Open request"
                        }
                        Button(
                            onClick = { vm.activateNotification(notification) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = KoraTokens.ButtonHeight),
                            shape = RoundedCornerShape(KoraTokens.RadiusControl),
                        ) { Text(primaryActionLabel, fontWeight = FontWeight.SemiBold) }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(KoraMeasures.dp8)) {
                            OutlinedButton(
                                onClick = { vm.snoozeNotification(notification) },
                                modifier = Modifier.weight(1f).heightIn(min = KoraTokens.SecondaryButtonHeight),
                                shape = RoundedCornerShape(KoraTokens.RadiusControl),
                            ) { Text("Snooze a day", maxLines = 1) }
                            OutlinedButton(
                                onClick = { vm.markNotificationRead(notification); vm.closeNotification() },
                                modifier = Modifier.weight(1f).heightIn(min = KoraTokens.SecondaryButtonHeight),
                                shape = RoundedCornerShape(KoraTokens.RadiusControl),
                            ) { Text("Mark as read", maxLines = 1) }
                        }
                    }
                    IconButton(
                        onClick = vm::closeNotification,
                        modifier = Modifier.align(Alignment.TopEnd).padding(KoraMeasures.dp4).size(KoraMeasures.dp40),
                    ) { Icon(Icons.Outlined.Close, contentDescription = "Close notification") }
                }
            }
        }
    }
}

private fun formatNotificationTime(value: String): String {
    if (value.isBlank()) return ""
    val parsed = runCatching { java.time.Instant.parse(value).toEpochMilli() }.getOrNull()
        ?: runCatching { java.time.OffsetDateTime.parse(value).toInstant().toEpochMilli() }.getOrNull()
        ?: return value
    return java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT).format(java.util.Date(parsed))
}

private fun notificationRelativeTime(value: String): String {
    val instant = runCatching { java.time.Instant.parse(value) }.getOrNull()
        ?: runCatching { java.time.OffsetDateTime.parse(value).toInstant() }.getOrNull()
        ?: return ""
    val minutes = java.time.Duration.between(instant, java.time.Instant.now()).toMinutes().coerceAtLeast(0)
    return when {
        minutes < 1 -> "now"
        minutes < 60 -> "${minutes}m"
        minutes < 24 * 60 -> "${minutes / 60}h"
        else -> "${minutes / (24 * 60)}d"
    }
}

private fun notificationSummaryTime(value: String): String {
    val relative = notificationRelativeTime(value)
    return when (relative) {
        "now" -> "Just now"
        "" -> formatNotificationTime(value)
        else -> relative
    }
}

private fun notificationIsToday(value: String): Boolean {
    val instant = runCatching { java.time.Instant.parse(value) }.getOrNull()
        ?: runCatching { java.time.OffsetDateTime.parse(value).toInstant() }.getOrNull()
        ?: return true
    val zone = java.time.ZoneId.systemDefault()
    return instant.atZone(zone).toLocalDate() == java.time.LocalDate.now(zone)
}

private fun notificationIsSnoozed(notification: JSONObject): Boolean = notification.optLong("snoozed_until", 0L) > System.currentTimeMillis()

@Composable
private fun SyncScreen(state: AppState, vm: AppViewModel) {
    var confirmDiscard by rememberSaveable { mutableStateOf(false) }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = KoraMeasures.dp20), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp15)) {
        item { Spacer(Modifier.height(KoraMeasures.dp8)); Text("Offline & sync", fontSize = KoraMeasures.sp26, fontWeight = FontWeight.Bold); Text("Your saved records", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) }
        item {
            OutlinedButton(onClick = { vm.logout() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(KoraMeasures.dp12)) {
                Text("Change server address")
            }
        }
        if (state.message != null) item { MessageBanner(state.message, state.offline && state.message.contains("waiting", true)) }
        item {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp6)) {
                Text("Available on this device", fontWeight = FontWeight.SemiBold)
                val coverage = state.offlineCoverage
                Text(when {
                    coverage.downloading -> "Downloading ${coverage.completedTypes} of ${coverage.totalTypes} record types…"
                    coverage.error != null -> "Download incomplete · ${coverage.error}"
                    coverage.completedAt != null -> "Workspace downloaded · " + java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.SHORT).format(java.util.Date(coverage.completedAt))
                    else -> "Download the workspace before working without a connection."
                }, fontSize = KoraMeasures.sp13, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedButton(onClick = { vm.downloadForOffline() }, enabled = !coverage.downloading) { Text(if (coverage.completedAt == null) "Download workspace" else "Update download") }
            }
        }
        item { SectionHeading("Waiting to sync", "${state.queuedMutations.size} saved") }
        if (state.queuedMutations.isNotEmpty()) {
            items(state.queuedMutations, key = { it.id }) { mutation ->
                val label = if (mutation.doctype.startsWith("@view-action/")) {
                    mutation.doctype.removePrefix("@view-action/")
                        .replace(Regex("[-_]+"), " ")
                        .takeIf(String::isNotBlank)
                        ?.let(::sentenceCaseUiLabel)
                        ?: "Configured action"
                } else sentenceCaseUiLabel(mutation.doctype.replace('-', ' '))
                Card(shape = RoundedCornerShape(KoraMeasures.dp12), colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface)) {
                    Row(Modifier.fillMaxWidth().padding(KoraMeasures.dp14), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CloudDone, null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(KoraMeasures.dp10))
                        Column(Modifier.weight(1f)) {
                            Text(label, fontWeight = FontWeight.Medium)
                            Text(mutation.displayName ?: mutation.recordName ?: "New record", fontSize = KoraMeasures.sp12, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(when (mutation.status) { "conflict" -> "Conflict"; "rejected" -> "Needs attention"; "sent" -> "Syncing"; else -> "Pending" }, fontSize = KoraMeasures.sp12, color = if (mutation.status in setOf("conflict", "rejected")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (mutation.lastError != null) Text(mutation.lastError, modifier = Modifier.padding(horizontal = KoraMeasures.dp14, vertical = KoraMeasures.dp6), fontSize = KoraMeasures.sp13, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (mutation.status in setOf("conflict", "rejected")) Row(Modifier.padding(horizontal = KoraMeasures.dp8)) {
                        if (!mutation.doctype.startsWith("@view-action/")) TextButton(onClick = { vm.reviewSyncIssue(mutation) }) { Text("Review correction") }
                        TextButton(onClick = { vm.acceptServerVersion(mutation) }) { Text(if (mutation.localId == null || mutation.recordName == null) "Discard local change" else "Use server version") }
                    }
                }
            }
            item {
                Button(onClick = { vm.syncQueuedMutations(showMessage = true) }, enabled = !state.syncingMutations, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(KoraMeasures.dp12)) {
                    if (state.syncingMutations) CircularProgressIndicator(Modifier.size(KoraMeasures.dp18), strokeWidth = KoraMeasures.dp2)
                    else Text("Sync now")
                }
            }
            item {
                TextButton(
                    onClick = { confirmDiscard = true },
                    enabled = !state.syncingMutations && !state.discardingMutations,
                    modifier = Modifier.fillMaxWidth().heightIn(min = KoraMeasures.dp44),
                ) {
                    Text(
                        if (state.discardingMutations) "Clearing pending changes…" else "Discard all ${state.queuedMutations.size} pending changes",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        } else item { EmptyCard("Everything is synced", "Records you save offline will appear here until they reach Kora.") }
        item { OutlinedButton(onClick = { vm.logout() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(KoraMeasures.dp14)) { Icon(Icons.Outlined.Logout, null); Spacer(Modifier.width(KoraMeasures.dp8)); Text("Sign out") } }
    }
    if (confirmDiscard) AlertDialog(
        onDismissRequest = { if (!state.discardingMutations) confirmDiscard = false },
        title = { Text("Discard pending changes?") },
        text = {
            Text("This removes ${state.queuedMutations.size} unsynced changes and their local previews from this device. Changes already synced to this workspace will remain.")
        },
        confirmButton = {
            TextButton(
                onClick = { confirmDiscard = false; vm.discardAllPendingChanges() },
                enabled = !state.discardingMutations,
            ) { Text("Discard changes", color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = {
            TextButton(onClick = { confirmDiscard = false }, enabled = !state.discardingMutations) { Text("Keep changes") }
        },
    )
}

@Composable
private fun DocTypeRow(item: DocTypeNavItem, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(KoraMeasures.dp12), colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface), border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant.copy(alpha = .7f))) {
        Row(Modifier.fillMaxWidth().padding(horizontal = KoraMeasures.dp15, vertical = KoraMeasures.dp14), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(KoraMeasures.dp38).background(androidx.compose.material3.MaterialTheme.colorScheme.secondary, RoundedCornerShape(KoraMeasures.dp12)), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Inventory2, null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary, modifier = Modifier.size(KoraMeasures.dp19)) }
            Spacer(Modifier.width(KoraMeasures.dp12)); Column(Modifier.weight(1f)) { Text(item.label, fontWeight = FontWeight.Medium); if (item.name != item.label) Text(item.name, fontSize = KoraMeasures.sp11, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) }
            Icon(Icons.Outlined.ChevronRight, null, tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ModuleCard(title: String, count: Int, iconName: String?, onClick: () -> Unit) {
    val colors = androidx.compose.material3.MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = KoraMeasures.dp104),
        shape = RoundedCornerShape(KoraTokens.RadiusCard),
        color = colors.surfaceContainerLow,
        border = androidx.compose.foundation.BorderStroke(KoraMeasures.dp1, colors.outlineVariant.copy(alpha = .7f)),
    ) {
        Column(Modifier.fillMaxWidth().padding(KoraMeasures.dp14), verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp10)) {
            Box(Modifier.size(KoraMeasures.dp36).background(colors.secondaryContainer, RoundedCornerShape(KoraMeasures.dp11)), contentAlignment = Alignment.Center) {
                Icon(moduleIcon(iconName), contentDescription = null, tint = colors.primary, modifier = Modifier.size(KoraMeasures.dp19))
            }
            Column(verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp2)) {
                Text(title, style = androidx.compose.material3.MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("$count ${if (count == 1) "record type" else "record types"}", style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
    }
}

private fun moduleIcon(name: String?): ImageVector = when (name?.lowercase()?.replace('-', '_')) {
    "shopping_cart", "cart" -> Icons.Filled.ShoppingCart
    "chart", "bar_chart" -> Icons.Filled.BarChart
    "settings" -> Icons.Filled.Settings
    "inventory_2" -> Icons.Filled.Inventory2
    else -> Icons.Filled.ListAlt
}

@Composable
private fun SectionHeading(title: String, detail: String) {
    Row(Modifier.fillMaxWidth().padding(top = KoraMeasures.dp2, bottom = KoraMeasures.dp4), verticalAlignment = Alignment.CenterVertically) { Text(title, fontWeight = FontWeight.SemiBold, fontSize = KoraMeasures.sp17, modifier = Modifier.weight(1f)); Text(detail, fontSize = KoraMeasures.sp12, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) }
}

@Composable
private fun InfoRow(icon: ImageVector, title: String, detail: String) {
    Card(shape = RoundedCornerShape(KoraMeasures.dp14)) { Row(Modifier.fillMaxWidth().padding(KoraMeasures.dp14), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary); Spacer(Modifier.width(KoraMeasures.dp10)); Column { Text(title, fontWeight = FontWeight.Medium); Text(detail, fontSize = KoraMeasures.sp12, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) } } }
}

private fun isPassiveConnectionMessage(message: String): Boolean =
    message.startsWith("Offline ·") || message.contains("waiting to sync", ignoreCase = true)

@Composable
private fun MessageBanner(message: String, warning: Boolean) {
    val color = if (warning) androidx.compose.material3.MaterialTheme.colorScheme.error else androidx.compose.material3.MaterialTheme.colorScheme.primary
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = .08f)), shape = RoundedCornerShape(KoraMeasures.dp13)) { Text(message, Modifier.padding(KoraMeasures.dp13), color = color, fontSize = KoraMeasures.sp13) }
}

@Composable
private fun EmptyCard(title: String, detail: String) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(KoraMeasures.dp12)) {
        Column(Modifier.fillMaxWidth().padding(KoraMeasures.dp20), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(KoraMeasures.dp44).background(androidx.compose.material3.MaterialTheme.colorScheme.secondary, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Inventory2, null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary) }
            Spacer(Modifier.height(KoraMeasures.dp10)); Text(title, fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(KoraMeasures.dp4)); Text(detail, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, fontSize = KoraMeasures.sp13, lineHeight = KoraMeasures.sp19)
        }
    }
}

@Composable
private fun KoraEmptyState(title: String, detail: String) {
    val colors = MaterialTheme.colorScheme
    Box(
        Modifier.fillMaxWidth()
            .drawBehind {
                val inset = KoraMeasures.dp0_5.toPx()
                drawRoundRect(
                    color = colors.outline,
                    topLeft = Offset(inset, inset),
                    size = androidx.compose.ui.geometry.Size(size.width - inset * 2, size.height - inset * 2),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(KoraTokens.RadiusCard.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = KoraMeasures.dp1.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(KoraMeasures.dp5.toPx(), KoraMeasures.dp4.toPx())),
                    ),
                )
            }
            .heightIn(min = KoraMeasures.dp84)
            .padding(horizontal = KoraMeasures.dp16, vertical = KoraMeasures.dp12),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(KoraMeasures.dp3)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

private fun formatHumanDate(value: String, dateOnly: Boolean): String = runCatching {
    val locale = Locale.getDefault()
    if (dateOnly) {
        val date = LocalDate.parse(value.take(10)).atStartOfDay(ZoneId.systemDefault()).toInstant()
        java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM, locale).format(java.util.Date.from(date))
    } else {
        val instant = runCatching { Instant.parse(value) }.getOrElse {
            runCatching { OffsetDateTime.parse(value).toInstant() }.getOrElse {
                LocalDateTime.parse(value.replace(' ', 'T')).atZone(ZoneId.systemDefault()).toInstant()
            }
        }
        java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT, locale).apply {
            timeZone = java.util.TimeZone.getTimeZone(ZoneId.systemDefault())
        }.format(java.util.Date.from(instant))
    }
}.getOrDefault(value)
