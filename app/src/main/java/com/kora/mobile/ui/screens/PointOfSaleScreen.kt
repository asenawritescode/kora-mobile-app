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
@OptIn(ExperimentalMaterial3Api::class)
internal fun PointOfSaleScreen(state: AppState, vm: AppViewModel) {
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
