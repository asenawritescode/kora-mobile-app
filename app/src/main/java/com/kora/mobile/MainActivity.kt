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

class MainActivity : ComponentActivity() {
    @Suppress("UNCHECKED_CAST")
    private val factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AppViewModel(application) as T
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: AppViewModel = viewModel(factory = factory)
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
}

@Composable
private fun KoraApp(state: AppState, vm: AppViewModel) {
    Surface(modifier = Modifier.fillMaxSize(), color = androidx.compose.material3.MaterialTheme.colorScheme.background) {
        when (state.stage) {
            AppStage.SERVER -> ServerSetup(state, onNext = vm::goToLogin, onConnect = vm::connect)
            AppStage.LOGIN -> LoginScreen(state, onBack = { vm.logout() }, onSubmit = { email, password -> vm.signIn(email, password) }, onChangeServer = { vm.logout() })
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
                    }, modifier = Modifier.fillMaxWidth(), label = "Server URL", placeholder = { Text("https://api.kora.mradiafrica.com") }, singleLine = true)
                    Spacer(Modifier.height(12.dp))
                    KoraTextField(value = site, onValueChange = { site = it }, modifier = Modifier.fillMaxWidth(), label = "Site ID *", placeholder = { Text("Your site ID") }, singleLine = true)
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
private fun BrandMark() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(46.dp).background(androidx.compose.material3.MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
            Text("K", color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column { Text("Kora", fontSize = 23.sp, fontWeight = FontWeight.Bold); Text("WORKSPACE", fontSize = 10.sp, letterSpacing = 1.8.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.primary) }
    }
}

@Composable
private fun LoginScreen(state: AppState, onBack: () -> Unit, onSubmit: (String, String) -> Unit, onChangeServer: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
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
        KoraTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = "Password",
            visualTransformation = if (showPassword) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (showPassword) "Hide password" else "Show password",
                    )
                }
            },
            singleLine = true,
        )
        Spacer(Modifier.height(14.dp))
        state.message?.let { MessageBanner(it, false); Spacer(Modifier.height(10.dp)) }
        Button(onClick = { onSubmit(email.trim(), password) }, modifier = Modifier.fillMaxWidth().height(54.dp), enabled = !state.busy && email.isNotBlank() && password.isNotBlank(), shape = RoundedCornerShape(16.dp)) {
            if (state.busy) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary)
            else Text("Sign in", fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(12.dp))
        Text("Sign in with your Kora account. Need a magic link? Open it on this device to continue.", fontSize = 12.sp, lineHeight = 18.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
       }
      }
    }
}

@Composable
private fun ServerPill(url: String, site: String, onChange: () -> Unit) {
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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun WorkspaceShell(state: AppState, vm: AppViewModel) {
    val manifestComponents = state.posManifest?.optJSONObject("spec")?.optJSONObject("layout")?.optJSONArray("children")
    val hasProductGrid = manifestComponents?.let { array -> (0 until array.length()).any { array.optJSONObject(it)?.optString("component") == "product_grid" } } == true
    val hasPaymentPanel = manifestComponents?.let { array -> (0 until array.length()).any { array.optJSONObject(it)?.optString("component") == "payment_panel" } } == true
    val hasPos = hasProductGrid && hasPaymentPanel
    val tabs = buildList {
        add("Home" to Icons.Filled.Home)
        add("Browse" to Icons.Filled.Inventory2)
        if (hasPos) add("POS" to Icons.Filled.ShoppingCart)
        add("Requests" to Icons.Filled.ListAlt)
        add("Reports" to Icons.Filled.BarChart)
    }
    Scaffold(
        contentWindowInsets = WindowInsets.navigationBars,
        topBar = {
            if (state.formOpen || state.selectedRecord != null || state.selectedDoctype != null) {
                TopAppBar(
                    title = { Text(when { state.formOpen -> if (state.editingRecord == null) "New record" else "Edit record"; state.selectedRecord != null -> state.selectedDoctype?.label ?: "Record"; else -> "Browse" }, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    navigationIcon = { IconButton(onClick = { if (state.formOpen) vm.backFromForm() else if (state.selectedRecord != null) vm.closeRecordDetail() else vm.selectSection("Work") }) { Icon(Icons.Outlined.ArrowBack, "Back") } },
                    actions = {
                        if (state.formOpen) {
                            if (state.offline) Icon(Icons.Filled.CloudOff, "Offline", Modifier.padding(12.dp).size(24.dp), tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                            else Icon(Icons.Outlined.CloudDone, "Connected", Modifier.padding(12.dp).size(24.dp), tint = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                        } else IconButton(onClick = { vm.selectSection("Sync") }) {
                            if (state.offline) Icon(Icons.Filled.CloudOff, "Offline. Open sync", tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                            else Icon(Icons.Outlined.CloudDone, "Connected. Open sync", tint = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background),
                )
            } else {
                TopAppBar(
                    title = { Column { Text(if (state.section in setOf("Reports", "Requests", "Sync")) state.section else "Kora", fontWeight = FontWeight.Bold); Text(state.navigation?.branding?.appName ?: "Workspace", fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) } },
                    actions = { IconButton(onClick = { vm.selectSection("Sync") }) { if (state.offline) Icon(Icons.Filled.CloudOff, "Offline. Open sync", tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) else Icon(Icons.Outlined.CloudDone, "Connected. Open sync", tint = androidx.compose.material3.MaterialTheme.colorScheme.primary) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background),
                )
            }
        },
        bottomBar = {
            if (state.selectedDoctype == null && !state.formOpen) {
                NavigationBar(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface) {
                    tabs.forEach { (name, icon) ->
                        val section = if (name == "Browse") "Work" else name
                        NavigationBarItem(selected = state.section == section, onClick = { vm.selectSection(section) }, icon = { Icon(icon, contentDescription = name) }, label = { Text(name) })
                    }
                }
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.formOpen -> RecordForm(state, vm)
                state.selectedRecord != null -> RecordDetailScreen(state, vm)
                state.selectedDoctype != null -> RecordListScreen(state, vm)
                state.section == "Home" -> DashboardScreen(state, vm)
                state.section == "Work" -> WorkScreen(state, vm)
                state.section == "POS" -> PointOfSaleScreen(state, vm)
                state.section == "Requests" -> RequestsScreen(state)
                state.section == "Reports" -> ReportsScreen(state, vm)
                else -> SyncScreen(state, vm)
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
    val titleField = productBindings.optString("title").ifBlank { "product_name" }
    val priceField = productBindings.optString("price").ifBlank { "selling_price" }
    val categoryField = categoryBindings.optString("group_field").ifBlank { productBindings.optString("subtitle", "category") }
    val stockField = productBindings.optString("badge").ifBlank { "stock_qty" }
    val methods = paymentBindings.optString("methods").removePrefix("literal:").split(',').map { it.trim().lowercase() }.filter(String::isNotBlank)
    val configuredPayment = paymentComponent?.optJSONObject("props")?.optJSONArray("actions")?.let { actions ->
        (0 until actions.length()).mapNotNull { actions.optJSONObject(it) }.any { it.optString("type") == "initiate_external_operation" }
    } == true
    val categories = listOf("All") + state.posCategories.values.distinct().sorted()
    val products = state.posProducts.filter { product ->
        val matchesText = search.isBlank() || product.optString(titleField).contains(search, true) || product.optString("reference").contains(search, true)
        val categoryName = state.posCategories[product.optString(categoryField)].orEmpty()
        matchesText && (category == "All" || categoryName == category) && product.optInt(stockField, 0) > 0 && product.optString("status", "Active").equals("Active", true)
    }
    val cartLines = state.posCart.values.toList()
    val total = cartLines.sumOf { it.product.optDouble(priceField) * it.quantity }
    val colors = androidx.compose.material3.MaterialTheme.colorScheme
    val registerName = paymentBindings.optString("register").removePrefix("literal:").ifBlank { "Register" }
    val isCompactHeight = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp < 700
    Column(Modifier.fillMaxSize().padding(horizontal = KoraTokens.Space4)) {
        Row(Modifier.fillMaxWidth().padding(top = if (isCompactHeight) 2.dp else KoraTokens.Space2, bottom = KoraTokens.Space2), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(metadata.optString("name", "Point of sale"), style = androidx.compose.material3.MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("$registerName  ·  ${products.size} products", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
            Surface(color = colors.secondary, shape = RoundedCornerShape(14.dp)) {
                Column(Modifier.padding(horizontal = 12.dp, vertical = 7.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${cartLines.sumOf { it.quantity }}", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.primary)
                    Text("ITEMS", style = androidx.compose.material3.MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                }
            }
        }
        KoraTextField(
            value = search, onValueChange = { search = it }, modifier = Modifier.fillMaxWidth(),
            singleLine = true, leadingIcon = { Icon(Icons.Outlined.Search, null) },
            placeholder = { Text("Search products") }
        )
        Row(Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 6.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.forEach { item -> FilterChip(selected = category == item, onClick = { category = item }, label = { Text(item) }) }
        }
        state.message?.takeUnless(::isPassiveConnectionMessage)?.let { MessageBanner(it, false) }
        if (state.posLoading && state.posProducts.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { CircularProgressIndicator(); Text("Loading products…", Modifier.padding(top = 12.dp), color = colors.onSurfaceVariant) } }
        } else if (state.posProducts.isEmpty()) {
            Column(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                EmptyCard("No products available", state.message ?: "The product catalogue could not be loaded for this site.")
                TextButton(onClick = vm::refreshPosCatalog) { Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(17.dp)); Text(" Try again") }
            }
        } else if (products.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(if (search.isNotBlank()) "No products match ‘$search’" else "No products in this category", color = colors.onSurfaceVariant)
            }
        } else {
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                val columns = if (maxWidth >= 680.dp) 4 else if (maxWidth >= 460.dp) 3 else 2
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns), state = productGridState, modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(products.size, key = { products[it].optString("name") }) { index ->
                        val product = products[index]
                        val inCart = state.posCart[product.optString("name")]?.quantity ?: 0
                        Card(
                            Modifier.fillMaxWidth().heightIn(min = 116.dp).clickable { vm.addPosProduct(product) },
                            shape = RoundedCornerShape(KoraTokens.RadiusCard),
                            colors = CardDefaults.cardColors(containerColor = colors.surface),
                        ) {
                            Column(Modifier.fillMaxWidth().padding(10.dp)) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(32.dp).background(colors.secondaryContainer, RoundedCornerShape(11.dp)), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Filled.Inventory2, null, tint = colors.primary, modifier = Modifier.size(17.dp))
                                    }
                                    Spacer(Modifier.weight(1f))
                                    if (inCart > 0) Surface(color = colors.primary, shape = CircleShape) {
                                        Text("$inCart", Modifier.padding(horizontal = 8.dp, vertical = 3.dp), color = colors.onPrimary, style = androidx.compose.material3.MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(product.optString(titleField, "Product"), style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 2, minLines = 1, overflow = TextOverflow.Ellipsis)
                                val categoryLabel = state.posCategories[product.optString(categoryField)].orEmpty()
                                if (categoryLabel.isNotBlank()) Text(categoryLabel, style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(Modifier.height(8.dp))
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                                    Text("KES ${"%.0f".format(product.optDouble(priceField))}", Modifier.weight(1f), style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.primary, maxLines = 1)
                                    Text("${product.optInt(stockField)} in stock", style = androidx.compose.material3.MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp).clickable { basketOpen = true },
            shape = RoundedCornerShape(KoraTokens.RadiusCard), color = colors.surface, tonalElevation = 2.dp,
        ) {
            Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.ShoppingCart, null, tint = colors.primary)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (cartLines.isEmpty()) "Basket is empty" else "${cartLines.sumOf { it.quantity }} items in basket", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(if (cartLines.isEmpty()) "Tap products to add them" else "KES ${"%.0f".format(total)} total", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                }
                Button(onClick = { basketOpen = true }, enabled = cartLines.isNotEmpty(), shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(horizontal = 18.dp, vertical = 11.dp)) {
                    Text("Review", fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Outlined.ArrowForward, null, modifier = Modifier.padding(start = 6.dp).size(16.dp))
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
        Column(Modifier.fillMaxWidth().heightIn(max = 720.dp)) {
            Column(Modifier.weight(1f, fill = false).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Current basket", style = androidx.compose.material3.MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("${cartLines.sumOf { it.quantity }} items · $registerName", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                    }
                    if (cartLines.isNotEmpty()) TextButton(onClick = vm::clearPosCart) { Text("Clear") }
                }
                Spacer(Modifier.height(12.dp))
                if (cartLines.isEmpty()) {
                    Text("Your basket is empty. Close this panel and tap a product to add it.", Modifier.padding(vertical = 24.dp), color = colors.onSurfaceVariant)
                } else {
                    cartLines.forEach { line ->
                        val productName = line.product.optString("name")
                        Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(line.product.optString(titleField, "Product"), style = androidx.compose.material3.MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text("KES ${"%.0f".format(line.product.optDouble(priceField))} each", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                            }
                            IconButton(onClick = { vm.changePosQuantity(productName, -1) }, modifier = Modifier.size(44.dp)) { Icon(Icons.Outlined.Remove, "Remove one ${line.product.optString(titleField)}") }
                            Text(line.quantity.toString(), Modifier.width(24.dp), style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            IconButton(onClick = { vm.changePosQuantity(productName, 1) }, modifier = Modifier.size(44.dp)) { Icon(Icons.Outlined.Add, "Add one ${line.product.optString(titleField)}") }
                        }
                        Divider(color = colors.outline.copy(alpha = 0.55f))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Payment method", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 5.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        methods.forEach { method ->
                            val isMobileMoney = method in setOf("mpesa", "mobile_money", "mobile")
                            FilterChip(selected = state.posPaymentMethod == method, enabled = !isMobileMoney, onClick = { vm.setPosPaymentMethod(method) }, label = {
                                Text(if (isMobileMoney && configuredPayment) "${method.replace('_', ' ')} · soon" else method.replace('_', ' ').replaceFirstChar { it.uppercase() }, maxLines = 1)
                            })
                        }
                    }
                    if (methods.any { it in setOf("mpesa", "mobile_money", "mobile") } && configuredPayment) {
                        Text("Mobile money prompts aren’t available in the native register yet.", Modifier.padding(top = 4.dp), style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                    }
                }
            }
            if (cartLines.isNotEmpty()) Column(Modifier.fillMaxWidth().background(colors.surface).padding(horizontal = 20.dp).padding(top = 10.dp, bottom = 18.dp)) {
                Divider(color = colors.outline.copy(alpha = 0.55f))
                Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Total", Modifier.weight(1f), style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = colors.onSurfaceVariant)
                    Text("KES ${"%.0f".format(total)}", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = colors.primary)
                }
                Button(
                    onClick = {
                        basketOpen = false
                        sheetScope.launch { sheetState.hide() }
                        vm.completePosSale()
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    enabled = !state.posSaving && state.posPaymentMethod !in setOf("mpesa", "mobile_money", "mobile"),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    if (state.posSaving) CircularProgressIndicator(Modifier.size(19.dp), strokeWidth = 2.dp, color = colors.onPrimary)
                    else Text("Complete sale · ${state.posPaymentMethod.replace('_', ' ').replaceFirstChar { it.uppercase() }}", fontWeight = FontWeight.SemiBold)
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
        val matchesModule = module.label.lowercase().contains(normalizedQuery) || module.module.lowercase().contains(normalizedQuery)
        val doctypes = module.doctypes.filter { it.label.lowercase().contains(normalizedQuery) || it.name.lowercase().contains(normalizedQuery) }
        when { normalizedQuery.isBlank() || matchesModule -> module; doctypes.isNotEmpty() -> module.copy(doctypes = doctypes); else -> null }
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
                    Button(onClick = { vm.selectSection("Work") }, shape = RoundedCornerShape(10.dp)) { Text("Browse records"); Spacer(Modifier.width(8.dp)); Icon(Icons.Outlined.ArrowForward, null, Modifier.size(16.dp)) }
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
                placeholder = { Text("Search modules and record types") }
            )
        }
        item { SectionHeading(if (normalizedQuery.isBlank()) "Your modules" else "Search results", "${modules.sumOf { it.doctypes.size }} record types") }
        if (modules.isEmpty()) item { EmptyCard("No modules available", "Connect to refresh your workspace navigation.") }
        if (modules.isEmpty() && normalizedQuery.isNotBlank()) item { EmptyCard("No matches found", "Try a different module or record type name.") }
        items(modules) { module -> ModuleCard(module.label, module.doctypes.size, module.doctypes.take(3).map { it.label }) { vm.selectSection("Work") } }
        if (normalizedQuery.isNotBlank()) {
            val matchedTypes = modules.flatMap { it.doctypes }.distinctBy { it.resourceName }
            items(matchedTypes) { item -> DocTypeRow(item) { vm.openDoctype(item) } }
        }
        item { Spacer(Modifier.height(8.dp)) }
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
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Browse", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("${modules.sumOf { it.doctypes.size }} record types across ${modules.size} areas", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        KoraTextField(
            value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
            singleLine = true, leadingIcon = { Icon(Icons.Outlined.Search, null) },
            placeholder = { Text("Search records and areas") }
        )
        if (modules.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                EmptyCard(if (normalizedQuery.isBlank()) "Workspace is not loaded" else "No matching records", if (normalizedQuery.isBlank()) "Connect to Kora to load your workspace." else "Try another record type or area name.")
            }
        } else LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
            items(modules, key = { it.module }) { module ->
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(34.dp).background(androidx.compose.material3.MaterialTheme.colorScheme.secondary, RoundedCornerShape(11.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Inventory2, null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(11.dp))
                            Text(module.label, Modifier.weight(1f), fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${module.doctypes.size}", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
                        }
                        module.doctypes.forEachIndexed { index, item ->
                            if (index == 0) Divider(Modifier.padding(top = 4.dp), color = androidx.compose.material3.MaterialTheme.colorScheme.outline.copy(alpha = .45f))
                            Row(
                                Modifier.fillMaxWidth().heightIn(min = 44.dp).clickable { vm.openDoctype(item) }.padding(start = 45.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(item.label, Modifier.weight(1f), style = androidx.compose.material3.MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Icon(Icons.Outlined.ChevronRight, null, tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (index < module.doctypes.lastIndex) Divider(Modifier.padding(start = 45.dp), color = androidx.compose.material3.MaterialTheme.colorScheme.outline.copy(alpha = .3f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordListScreen(state: AppState, vm: AppViewModel) {
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
private fun RecordCard(
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

@Composable
private fun RecordDetailScreen(state: AppState, vm: AppViewModel) {
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

private fun formatCurrency(value: Any?): String {
    val number = value?.toString()?.toDoubleOrNull() ?: return value?.toString() ?: "—"
    val formatted = java.text.NumberFormat.getNumberInstance(java.util.Locale("en", "KE")).format(number)
    return "KES $formatted"
}

private data class MobileReportMetric(val label: String, val value: String)
private data class MobileReportChart(val title: String, val rows: JSONArray, val indexField: String, val seriesField: String, val mode: String, val format: String = "number")

@Composable
private fun ReportChartCard(chart: MobileReportChart) {
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
private fun RecordForm(state: AppState, vm: AppViewModel) {
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
private fun ChoiceField(
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
private fun PickerField(label: String, value: String, placeholder: String, enabled: Boolean, onClick: () -> Unit) {
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

private fun parseChildDefault(type: String, value: String): Any? = when (type) {
    "Check" -> value == "1" || value.equals("true", true)
    "Int" -> value.toLongOrNull() ?: value
    "Float", "Currency", "Percent" -> value.toDoubleOrNull() ?: value
    else -> value
}

private fun fieldIsVisible(field: FieldSchema, values: Map<String, Any?>): Boolean =
    !field.hidden && evaluateFieldCondition(field.dependsOn, values, true)

private fun fieldIsRequired(field: FieldSchema, values: Map<String, Any?>): Boolean =
    field.reqd || evaluateFieldCondition(field.mandatoryDependsOn, values, false)

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
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        item { Spacer(Modifier.height(8.dp)); SectionHeading("Requests", "Needs action") }
        if (state.requests.isEmpty()) item { EmptyCard("Nothing needs your attention", if (state.offline) "No cached requests are available. Reconnect to refresh." else "Requests assigned to you will show up here.") }
        items(state.requests) { request ->
            val title = request.optString("title", "Request")
            val message = request.optString("message")
            val status = request.optString("status", "Open")
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Text(title, Modifier.weight(1f), fontWeight = FontWeight.SemiBold); Text(status.replace('_', ' '), color = androidx.compose.material3.MaterialTheme.colorScheme.primary, fontSize = 11.sp) }
                    if (message.isNotBlank()) Text(message, Modifier.padding(top = 5.dp), color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun SyncScreen(state: AppState, vm: AppViewModel) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        item { Spacer(Modifier.height(8.dp)); Text("Offline & sync", fontSize = 26.sp, fontWeight = FontWeight.Bold); Text("Your saved records", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) }
        item {
            OutlinedButton(onClick = { vm.logout() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Text("Change server address")
            }
        }
        if (state.message != null) item { MessageBanner(state.message, state.offline && state.message.contains("waiting", true)) }
        item {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Available on this device", fontWeight = FontWeight.SemiBold)
                val coverage = state.offlineCoverage
                Text(when {
                    coverage.downloading -> "Downloading ${coverage.completedTypes} of ${coverage.totalTypes} record types…"
                    coverage.error != null -> "Download incomplete · ${coverage.error}"
                    coverage.completedAt != null -> "Workspace downloaded · " + java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.SHORT).format(java.util.Date(coverage.completedAt))
                    else -> "Download the workspace before working without a connection."
                }, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedButton(onClick = { vm.downloadForOffline() }, enabled = !coverage.downloading) { Text(if (coverage.completedAt == null) "Download workspace" else "Update download") }
            }
        }
        item { SectionHeading("Waiting to sync", "${state.queuedMutations.size} saved") }
        if (state.queuedMutations.isNotEmpty()) {
            items(state.queuedMutations, key = { it.id }) { mutation ->
                val label = if (mutation.doctype.startsWith("@view-action/")) "POS sale" else mutation.doctype.replace('-', ' ').split(' ').joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface)) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CloudDone, null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(label, fontWeight = FontWeight.Medium)
                            Text(mutation.displayName ?: mutation.recordName ?: "New record", fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(when (mutation.status) { "conflict" -> "Conflict"; "rejected" -> "Needs attention"; "sent" -> "Syncing"; else -> "Pending" }, fontSize = 12.sp, color = if (mutation.status in setOf("conflict", "rejected")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (mutation.lastError != null) Text(mutation.lastError, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (mutation.status in setOf("conflict", "rejected")) Row(Modifier.padding(horizontal = 8.dp)) {
                        if (!mutation.doctype.startsWith("@view-action/")) TextButton(onClick = { vm.reviewSyncIssue(mutation) }) { Text("Review correction") }
                        TextButton(onClick = { vm.acceptServerVersion(mutation) }) { Text(if (mutation.localId == null || mutation.recordName == null) "Discard local change" else "Use server version") }
                    }
                }
            }
            item {
                Button(onClick = { vm.syncQueuedMutations(showMessage = true) }, enabled = !state.syncingMutations, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    if (state.syncingMutations) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Text("Sync now")
                }
            }
        } else item { EmptyCard("Everything is synced", "Records you save offline will appear here until they reach Kora.") }
        item { OutlinedButton(onClick = { vm.logout() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Icon(Icons.Outlined.Logout, null); Spacer(Modifier.width(8.dp)); Text("Sign out") } }
    }
}

@Composable
private fun DocTypeRow(item: DocTypeNavItem, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface), border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant.copy(alpha = .7f))) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(38.dp).background(androidx.compose.material3.MaterialTheme.colorScheme.secondary, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Inventory2, null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp)) }
            Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(item.label, fontWeight = FontWeight.Medium); if (item.name != item.label) Text(item.name, fontSize = 11.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) }
            Icon(Icons.Outlined.ChevronRight, null, tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ModuleCard(title: String, count: Int, previews: List<String>, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface), border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant.copy(alpha = .7f))) {
        Column(Modifier.padding(17.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).background(androidx.compose.material3.MaterialTheme.colorScheme.secondary, RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Inventory2, null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary) }
                Spacer(Modifier.width(11.dp)); Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.SemiBold); Text("$count record types", fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) }
                Icon(Icons.Outlined.ChevronRight, null, tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (previews.isNotEmpty()) { Spacer(Modifier.height(14.dp)); Divider(color = androidx.compose.material3.MaterialTheme.colorScheme.outline.copy(alpha = .55f)); Spacer(Modifier.height(10.dp)); Text(previews.joinToString("   ·   "), fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        }
    }
}

@Composable
private fun SectionHeading(title: String, detail: String) {
    Row(Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) { Text(title, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, modifier = Modifier.weight(1f)); Text(detail, fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) }
}

@Composable
private fun InfoRow(icon: ImageVector, title: String, detail: String) {
    Card(shape = RoundedCornerShape(14.dp)) { Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary); Spacer(Modifier.width(10.dp)); Column { Text(title, fontWeight = FontWeight.Medium); Text(detail, fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) } } }
}

private fun isPassiveConnectionMessage(message: String): Boolean =
    message.startsWith("Offline ·") || message.contains("waiting to sync", ignoreCase = true)

@Composable
private fun MessageBanner(message: String, warning: Boolean) {
    val color = if (warning) androidx.compose.material3.MaterialTheme.colorScheme.error else androidx.compose.material3.MaterialTheme.colorScheme.primary
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = .08f)), shape = RoundedCornerShape(13.dp)) { Text(message, Modifier.padding(13.dp), color = color, fontSize = 13.sp) }
}

@Composable
private fun EmptyCard(title: String, detail: String) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(44.dp).background(androidx.compose.material3.MaterialTheme.colorScheme.secondary, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Inventory2, null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary) }
            Spacer(Modifier.height(10.dp)); Text(title, fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(4.dp)); Text(detail, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, lineHeight = 19.sp)
        }
    }
}

private fun formatHumanDate(value: String, dateOnly: Boolean): String = runCatching {
    val locale = Locale.getDefault()
    if (dateOnly) {
        LocalDate.parse(value.take(10)).format(DateTimeFormatter.ofPattern("MMM d, yyyy", locale))
    } else {
        val instant = runCatching { Instant.parse(value) }.getOrElse {
            runCatching { OffsetDateTime.parse(value).toInstant() }.getOrElse {
                LocalDateTime.parse(value.replace(' ', 'T')).atZone(ZoneId.systemDefault()).toInstant()
            }
        }
        DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm", locale).withZone(ZoneId.systemDefault()).format(instant)
    }
}.getOrDefault(value)
