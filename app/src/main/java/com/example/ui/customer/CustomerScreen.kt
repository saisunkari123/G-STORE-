package com.example.ui.customer

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import android.view.ViewGroup
import androidx.compose.ui.platform.LocalView
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.domain.model.Category
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.amplifyframework.core.Amplify
import com.amplifyframework.geo.models.Coordinates
import com.amplifyframework.geo.options.GeoSearchByCoordinatesOptions
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.compose.AsyncImagePainter
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Address
import com.example.domain.model.Product
import com.example.domain.model.ProductVariant
import com.example.domain.model.Order
import com.example.ui.state.AppState
import com.example.ui.theme.RoyalEmerald
import com.example.ui.theme.DeepGold
import com.example.ui.theme.WarmSand
import com.example.ui.theme.SoftAlabaster
import com.example.ui.theme.CharcoalOnyx
import com.example.ui.theme.LightGrey
import com.example.ui.theme.DarkGrey
import com.example.ui.theme.AlertRed
import com.example.ui.theme.SoftGreen
import com.example.ui.theme.SoftOrange
import com.example.ui.theme.MediumGrey
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.*

data class MapLatLng(val latitude: Double, val longitude: Double)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerScreen() {
    var selectedTab by remember { mutableStateOf("HOME") } // "HOME", "CART", "TRACKING"
    var showAddressModal by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    val lastOrder = AppState.lastPlacedOrder
    if (lastOrder != null) {
        OrderSuccessView(
            order = lastOrder,
            onContinueShopping = {
                AppState.lastPlacedOrder = null
                selectedTab = "HOME"
            }
        )
        return
    }

    if (showLogoutConfirm) {
        @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showLogoutConfirm = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Logout", fontWeight = FontWeight.Black, fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                Text("Are you sure you want to log out?", color = Color.Gray, fontSize = 16.sp)
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { AppState.logout(); showLogoutConfirm = false },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Yes, Logout", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = { showLogoutConfirm = false },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Cancel", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("customer_bottom_nav")
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val navItems = listOf(
                        Triple("HOME", Icons.Default.Home, "Shop"),
                        Triple("CART", Icons.Default.ShoppingCart, "Cart"),
                        Triple("TRACKING", Icons.AutoMirrored.Filled.List, "Orders"),
                        Triple("ACCOUNT", Icons.Default.Person, "Account")
                    )
                    
                    navItems.forEach { (tab, icon, label) ->
                        val isSelected = selectedTab == tab
                        val activeColor = RoyalEmerald
                        val inactiveColor = Color.Gray
                        
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = tab }
                                .padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (tab == "CART") {
                                BadgedBox(
                                    badge = {
                                        if (AppState.cartItems.isNotEmpty()) {
                                            Badge(containerColor = DeepGold) {
                                                Text(
                                                    text = AppState.cartItems.values.sum().toString(),
                                                    color = Color.White,
                                                    fontSize = 9.sp
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = if (isSelected) activeColor else inactiveColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) activeColor else inactiveColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) activeColor else inactiveColor
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        var selectedProductForDetail by remember { mutableStateOf<Product?>(null) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = paddingValues.calculateBottomPadding(),
                    top = if (selectedTab == "HOME") 0.dp else paddingValues.calculateTopPadding()
                )
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                "HOME" -> CustomerCatalogView(
                    onLogoutClick = { showLogoutConfirm = true },
                    onCartClick = { selectedTab = "CART" },
                    onLocationClick = { showAddressModal = true },
                    onProductClick = { selectedProductForDetail = it }
                )
                "CART" -> CustomerCartView(
                    onOpenAddressManager = { showAddressModal = true },
                    onProductClick = { selectedProductForDetail = it }
                )
                "TRACKING" -> CustomerOrdersView()
                "ACCOUNT" -> CustomerAccountView(onLogoutClick = { showLogoutConfirm = true })
            }

            if (showAddressModal) {
                AddressSelectionDialog(onDismiss = { showAddressModal = false })
            }

            if (selectedProductForDetail != null) {
                ProductDetailBottomSheet(
                    product = selectedProductForDetail!!,
                    onDismiss = { selectedProductForDetail = null }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerCatalogView(
    onLogoutClick: () -> Unit,
    onCartClick: () -> Unit,
    onLocationClick: () -> Unit,
    onProductClick: (Product) -> Unit
) {
    // Refresh product list from cloud whenever catalog opens, ensuring new admin products are shown
    LaunchedEffect(Unit) {
        AppState.refreshProductsFromCloud()
    }

    var searchQuery by remember { mutableStateOf("") }
    var appliedCategories by remember { mutableStateOf(emptySet<String>()) }
    var tempSelectedCategories by remember { mutableStateOf(emptySet<String>()) }
    var selectedSort by remember { mutableStateOf("Default") }
    var onlyWithDiscount by remember { mutableStateOf(false) }

    var showCategoryFilterSheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    var tempSort by remember { mutableStateOf("Default") }
    var tempOnlyWithDiscount by remember { mutableStateOf(false) }

    val filteredProducts = AppState.productsList.filter {
        val cleanQuery = searchQuery.replace("\\s".toRegex(), "").lowercase()
        val matchesQuery = it.isEnabled && (
            cleanQuery.isEmpty() ||
            it.nameEn.replace("\\s".toRegex(), "").lowercase().contains(cleanQuery) ||
            it.brand.replace("\\s".toRegex(), "").lowercase().contains(cleanQuery)
        )
        val matchesCategory = appliedCategories.isEmpty() || appliedCategories.contains(it.categoryId)
        val matchesDiscount = !onlyWithDiscount || it.variants.any { v -> v.currentPrice < v.mrp }
        matchesQuery && matchesCategory && matchesDiscount
    }.let { list ->
        list.sortedWith(
            compareBy { prod ->
                val highestPrice = prod.variants.maxOfOrNull { it.currentPrice } ?: 0.0
                when (selectedSort) {
                    "Price: Low to High" -> highestPrice
                    "Price: High to Low" -> -highestPrice
                    "What's New" -> -prod.dateCreated.toDouble()
                    else -> -highestPrice // Default is High to Low
                }
            }
        )
    }

    val selectedAddress = AppState.addressesList.find { it.isSelected }
    val addressDisplay = if (selectedAddress != null) {
        "${selectedAddress.houseNo}, ${selectedAddress.landmark}"
    } else {
        "Tap to select delivery address"
    }

    var isRefreshing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            val context = androidx.compose.ui.platform.LocalContext.current
            val isOnline by androidx.compose.runtime.produceState(initialValue = true) {
                com.example.util.NetworkMonitor.observeNetworkStatus(context).collect { value = it }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(RoyalEmerald, Color(0xFF022C22))
                        )
                    )
                    .statusBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, top = 2.dp, bottom = 10.dp)
                ) {
                    // Row 1: Top Branding & Greeting + Actions (Cart & Logout)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left/Center: G Logo + G-STORE + Hi, Sai
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "G",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            val userName = AppState.currentUser?.name ?: "Guest"
                            Text(
                                text = "G-STORE",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "•  Hi, $userName",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            androidx.compose.animation.AnimatedVisibility(visible = !isOnline) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = Color(0xFFDC2626),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "Offline",
                                            color = Color.White,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Right: Cart & Logout Action Icons
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onCartClick,
                                modifier = Modifier.size(32.dp)
                            ) {
                                val cartItemsCount = AppState.cartItems.values.sum()
                                if (cartItemsCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = DeepGold,
                                                contentColor = Color.White
                                            ) {
                                                Text(cartItemsCount.toString(), fontSize = 9.sp)
                                            }
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.ShoppingCart,
                                            contentDescription = "Cart",
                                            tint = Color.White,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        Icons.Default.ShoppingCart,
                                        contentDescription = "Cart",
                                        tint = Color.White,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(2.dp))

                            IconButton(
                                onClick = onLogoutClick,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "Logout",
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Row 2: Delivery Location Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLocationClick() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Deliver to:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$addressDisplay ▼",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 2: Sleek 38dp Search Bar with Clear Icon (Explicit readable text in light & dark mode)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = RoyalEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        "Search for quality products...",
                                        color = Color(0xFF64748B),
                                        fontSize = 12.5.sp
                                    )
                                }
                                androidx.compose.foundation.text.BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        color = Color(0xFF1E293B),
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            if (searchQuery.isNotEmpty()) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { searchQuery = "" }
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        @OptIn(ExperimentalMaterial3Api::class)
        androidx.compose.material3.pulltorefresh.PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                coroutineScope.launch {
                    isRefreshing = true
                    AppState.refreshAllFromCloud {
                        isRefreshing = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 90.dp)
                ) {
                    // Full-width Category Carousel Chips
                    item(span = { GridItemSpan(2) }) {
                        val allCats = listOf(Category(id = "ALL", nameEn = "All Products", imageUrl = "")) + AppState.categoriesList
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(allCats) { cat ->
                                val isDark = AppState.isDarkMode
                                val isSelected = (cat.id == "ALL" && appliedCategories.isEmpty()) || appliedCategories.contains(cat.id)
                                Surface(
                                    onClick = {
                                        appliedCategories = if (cat.id == "ALL") emptySet() else setOf(cat.id)
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) RoyalEmerald else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, if (isSelected) RoyalEmerald else (if (isDark) Color(0xFF404040) else Color(0xFFE5E7EB))),
                                    shadowElevation = if (isSelected) 2.dp else 0.dp
                                ) {
                                    Text(
                                        text = cat.nameEn,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Full-width Filter / Sort Bar
                    item(span = { GridItemSpan(2) }) {
                        val isDark = AppState.isDarkMode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Category Filter Button
                            val catText = if (appliedCategories.isEmpty()) "Categories" else "Categories (${appliedCategories.size})"
                            OutlinedButton(
                                onClick = {
                                    tempSelectedCategories = appliedCategories
                                    showCategoryFilterSheet = true
                                },
                                modifier = Modifier.weight(1f).height(38.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (appliedCategories.isNotEmpty()) (if (isDark) RoyalEmerald.copy(alpha = 0.25f) else Color(0xFFECFDF5)) else MaterialTheme.colorScheme.surface,
                                    contentColor = if (appliedCategories.isNotEmpty()) (if (isDark) Color(0xFF34D399) else RoyalEmerald) else MaterialTheme.colorScheme.onSurface
                                ),
                                border = BorderStroke(1.dp, if (appliedCategories.isNotEmpty()) RoyalEmerald else (if (isDark) Color(0xFF404040) else Color(0xFFE5E7EB))),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(catText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            // Sort By Button
                            val sortText = when (selectedSort) {
                                "Price: Low to High" -> "Low to High"
                                "Price: High to Low" -> "High to Low"
                                "What's New" -> "What's New"
                                else -> "Sort"
                            }
                            val isSortActive = selectedSort != "Default" || onlyWithDiscount
                            OutlinedButton(
                                onClick = {
                                    tempSort = selectedSort
                                    tempOnlyWithDiscount = onlyWithDiscount
                                    showSortSheet = true
                                },
                                modifier = Modifier.weight(1f).height(38.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSortActive) (if (isDark) RoyalEmerald.copy(alpha = 0.25f) else Color(0xFFECFDF5)) else MaterialTheme.colorScheme.surface,
                                    contentColor = if (isSortActive) (if (isDark) Color(0xFF34D399) else RoyalEmerald) else MaterialTheme.colorScheme.onSurface
                                ),
                                border = BorderStroke(1.dp, if (isSortActive) RoyalEmerald else (if (isDark) Color(0xFF404040) else Color(0xFFE5E7EB))),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.SwapVert, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(sortText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    if (filteredProducts.isEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.SearchOff, null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("No items found matching '$searchQuery'", color = Color.Gray, fontSize = 14.sp)
                            }
                        }
                    } else {
                        items(filteredProducts) { product ->
                            CustomerProductCard(product, selectedSort, onProductClick)
                        }
                    }
                }

                // Floating Quick Cart Bar at Bottom
                if (AppState.cartItems.isNotEmpty()) {
                    val totalQty = AppState.cartItems.values.sum()
                    FloatingQuickCartBar(
                        itemCount = totalQty,
                        subtotal = AppState.cartSubtotal,
                        onClick = onCartClick,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
        }
        
        // --- Bottom Sheets and Dialogs ---

        if (showCategoryFilterSheet) {
            @OptIn(ExperimentalMaterial3Api::class)
            ModalBottomSheet(
                onDismissRequest = { showCategoryFilterSheet = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "Select Categories",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                    ) {
                        items(AppState.categoriesList) { category ->
                            val isChecked = tempSelectedCategories.contains(category.id)
                            FilterCheckboxRow(
                                label = category.nameEn,
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    tempSelectedCategories = if (checked) {
                                        tempSelectedCategories + category.id
                                    } else {
                                        tempSelectedCategories - category.id
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showCategoryFilterSheet = false },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                appliedCategories = tempSelectedCategories
                                showCategoryFilterSheet = false
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalEmerald),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Apply Filter", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (showSortSheet) {
            @OptIn(ExperimentalMaterial3Api::class)
            ModalBottomSheet(
                onDismissRequest = { showSortSheet = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "Sort & Filter",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Price: Low to High
                    FilterCheckboxRow(
                        label = "Price: Low to High",
                        checked = tempSort == "Price: Low to High",
                        onCheckedChange = { checked ->
                            tempSort = if (checked) "Price: Low to High" else "Default"
                        }
                    )

                    // Price: High to Low
                    FilterCheckboxRow(
                        label = "Price: High to Low",
                        checked = tempSort == "Price: High to Low",
                        onCheckedChange = { checked ->
                            tempSort = if (checked) "Price: High to Low" else "Default"
                        }
                    )

                    // What's New
                    FilterCheckboxRow(
                        label = "What's New",
                        checked = tempSort == "What's New",
                        onCheckedChange = { checked ->
                            tempSort = if (checked) "What's New" else "Default"
                        }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .height(1.dp)
                            .background(Color(0xFFEEEEEE))
                    )

                    // With Discount (independent filter)
                    FilterCheckboxRow(
                        label = "With Discount",
                        checked = tempOnlyWithDiscount,
                        onCheckedChange = { checked ->
                            tempOnlyWithDiscount = checked
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showSortSheet = false },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                selectedSort = tempSort
                                onlyWithDiscount = tempOnlyWithDiscount
                                showSortSheet = false
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalEmerald),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Apply", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterCheckboxRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = RoyalEmerald)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilters(selectedCategory: String, onCategorySelect: (String) -> Unit) {
    val categories = listOf("All", "Rice Bags", "Dals & Pulses", "Cooking Oils", "Dairy Essentials", "Spices & Masalas")
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                selected = category == selectedCategory,
                onClick = { onCategorySelect(category) },
                label = { Text(category, color = if (category == selectedCategory) Color.White else MaterialTheme.colorScheme.onSurface) },
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = RoyalEmerald,
                    selectedLabelColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = null
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortFiltersRow(selectedSort: String, onSortSelect: (String) -> Unit) {
    val sortOptions = listOf("Default", "Price: Low to High", "Price: High to Low")
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(sortOptions) { option ->
            FilterChip(
                selected = option == selectedSort,
                onClick = { onSortSelect(option) },
                label = { Text(option, fontSize = 12.sp, color = if (option == selectedSort) Color.White else MaterialTheme.colorScheme.onSurface) },
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = RoyalEmerald,
                    selectedLabelColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = null
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
    }
}

fun formatProductNameNoOrphan(name: String): String {
    val words = name.trim().split(Regex("\\s+"))
    if (words.size <= 2) return name
    val prefix = words.subList(0, words.size - 2).joinToString(" ")
    val lastTwo = words.subList(words.size - 2, words.size).joinToString("\u00A0")
    return if (prefix.isEmpty()) lastTwo else "$prefix $lastTwo"
}

@Composable
fun CustomerProductCard(
    product: Product,
    selectedSort: String = "Default",
    onProductClick: (Product) -> Unit = {}
) {
    val sortedVariants = remember(product.variants, selectedSort) {
        if (selectedSort == "Price: Low to High") {
            product.variants.sortedBy { it.currentPrice }
        } else {
            product.variants.sortedByDescending { it.currentPrice }
        }
    }
    var selectedVariantIndex by remember { mutableStateOf(0) }
    val dummyVariant = com.example.domain.model.ProductVariant(
        id = "none", weight = "—", unit = "", currentPrice = 0.0, mrp = 0.0, stockQuantity = 0, sku = ""
    )
    val hasVariants = sortedVariants.isNotEmpty()
    val currentVariant = sortedVariants.getOrNull(selectedVariantIndex)
        ?: sortedVariants.firstOrNull()
        ?: dummyVariant

    var showVariantSheet by remember { mutableStateOf(false) }

    val isOutOfStock = !hasVariants || currentVariant.stockQuantity <= 0
    val cartKey = "${product.id}#${currentVariant.id}"
    val currentInCart = AppState.cartItems[cartKey] ?: 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(236.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = 0.04f))
            .clickable { onProductClick(product) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (AppState.isDarkMode) Color(0xFF2E2E2E) else Color(0xFFF1F3F4))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Top Image Box with Discount & Out of Stock Overlay (Fixed 115dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(if (AppState.isDarkMode) Color(0xFF262626) else Color(0xFFF8FAF9))
            ) {
                val imageUrl = product.imageUrls.getOrNull(product.thumbnailIndex)?.trim() ?: ""
                if (imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = product.nameEn,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = RoyalEmerald.copy(alpha = 0.3f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Discount badge (top-left)
                if (hasVariants && currentVariant.mrp > currentVariant.currentPrice) {
                    val discount = ((currentVariant.mrp - currentVariant.currentPrice) / currentVariant.mrp * 100).toInt()
                    if (discount > 0) {
                        Surface(
                            color = Color(0xFFEA580C),
                            shape = RoundedCornerShape(topStart = 16.dp, bottomEnd = 8.dp),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Text(
                                "$discount% OFF",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Out of stock overlay
                if (isOutOfStock) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.55f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = Color(0xFFDC2626),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "OUT OF STOCK",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // 2. Body Details: Strictly Aligned Slots with SpaceBetween
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top section of body: Title & Size
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Product Title (Fixed 32dp container)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp),
                        contentAlignment = Alignment.TopStart
                    ) {
                        Text(
                            text = formatProductNameNoOrphan(product.nameEn),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 15.sp
                        )
                    }

                    Spacer(Modifier.height(2.dp))

                    // Variant / Size chip (Fixed 22dp container with dark mode contrast)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(22.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (sortedVariants.size > 1) {
                            Surface(
                                onClick = { showVariantSheet = true },
                                shape = RoundedCornerShape(5.dp),
                                color = if (AppState.isDarkMode) Color(0xFF262626) else Color(0xFFF3F4F6),
                                border = BorderStroke(0.5.dp, if (AppState.isDarkMode) Color(0xFF4A4A4A) else Color(0xFFD1D5DB))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "${currentVariant.weight}${currentVariant.unit}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (AppState.isDarkMode) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.width(2.dp))
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = "Select Variant",
                                        modifier = Modifier.size(12.dp),
                                        tint = if (AppState.isDarkMode) Color(0xFFD1D5DB) else Color.Gray
                                    )
                                }
                            }
                        } else if (hasVariants) {
                            Surface(
                                shape = RoundedCornerShape(5.dp),
                                color = if (AppState.isDarkMode) Color(0xFF262626) else Color(0xFFF9FAFB),
                                border = BorderStroke(0.5.dp, if (AppState.isDarkMode) Color(0xFF404040) else Color(0xFFE5E7EB))
                            ) {
                                Text(
                                    "${currentVariant.weight}${currentVariant.unit}",
                                    fontSize = 10.sp,
                                    color = if (AppState.isDarkMode) Color(0xFFD1D5DB) else Color.Gray,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Bottom section: Price & Add to Cart Action Row (Fixed 30dp container)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Price stack
                    Column(verticalArrangement = Arrangement.Center) {
                        Text(
                            "₹${currentVariant.currentPrice.toInt()}",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = RoyalEmerald,
                            lineHeight = 14.sp
                        )
                        if (hasVariants && currentVariant.mrp > currentVariant.currentPrice) {
                            Text(
                                "₹${currentVariant.mrp.toInt()}",
                                fontSize = 9.5.sp,
                                color = Color.Gray,
                                textDecoration = TextDecoration.LineThrough,
                                lineHeight = 10.sp
                            )
                        }
                    }

                    // Stepper / + ADD Button
                    if (isOutOfStock) {
                        Surface(
                            shape = RoundedCornerShape(7.dp),
                            color = Color(0xFFE5E7EB)
                        ) {
                            Text(
                                "SOON",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else if (currentInCart > 0) {
                        Surface(
                            shape = RoundedCornerShape(7.dp),
                            color = RoyalEmerald,
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable { AppState.updateCartQty(product.id, currentVariant.id, currentInCart - 1) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Remove, "Minus", tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                                Text(
                                    text = "$currentInCart",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 3.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable(enabled = currentInCart < currentVariant.stockQuantity) {
                                            AppState.updateCartQty(product.id, currentVariant.id, currentInCart + 1)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Add, "Plus", tint = if (currentInCart < currentVariant.stockQuantity) Color.White else Color.White.copy(alpha = 0.4f), modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    } else {
                        Surface(
                            onClick = { AppState.addToCart(product.id, currentVariant.id) },
                            shape = RoundedCornerShape(7.dp),
                            color = if (AppState.isDarkMode) RoyalEmerald.copy(alpha = 0.2f) else Color(0xFFECFDF5),
                            border = BorderStroke(1.dp, RoyalEmerald)
                        ) {
                            Text(
                                "ADD",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (AppState.isDarkMode) Color(0xFF34D399) else RoyalEmerald,
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal to choose size variant if tapped
    if (showVariantSheet) {
        @OptIn(ExperimentalMaterial3Api::class)
        ModalBottomSheet(
            onDismissRequest = { showVariantSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "Select Size for ${product.nameEn}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(16.dp))
                sortedVariants.forEachIndexed { idx, v ->
                    val isSel = selectedVariantIndex == idx
                    Surface(
                        onClick = {
                            selectedVariantIndex = idx
                            showVariantSheet = false
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSel) (if (AppState.isDarkMode) RoyalEmerald.copy(alpha = 0.25f) else Color(0xFFECFDF5)) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (isSel) RoyalEmerald else (if (AppState.isDarkMode) Color(0xFF404040) else Color(0xFFE5E7EB))),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "${v.weight}${v.unit}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isSel && AppState.isDarkMode) Color(0xFF34D399) else MaterialTheme.colorScheme.onSurface
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "₹${v.currentPrice.toInt()}",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSel && AppState.isDarkMode) Color(0xFF34D399) else RoyalEmerald,
                                        fontSize = 14.sp
                                    )
                                    if (v.mrp > v.currentPrice) {
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            "₹${v.mrp.toInt()}",
                                            fontSize = 12.sp,
                                            color = Color.Gray,
                                            textDecoration = TextDecoration.LineThrough
                                        )
                                    }
                                }
                            }
                            if (isSel) {
                                Icon(Icons.Default.CheckCircle, null, tint = if (AppState.isDarkMode) Color(0xFF34D399) else RoyalEmerald, modifier = Modifier.size(20.dp))
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
fun ProductDetailBottomSheet(
    product: Product,
    onDismiss: () -> Unit
) {
    var selectedVariantIndex by remember { mutableStateOf(0) }
    val variants = product.variants
    val dummyVariant = com.example.domain.model.ProductVariant(
        id = "none", weight = "—", unit = "", currentPrice = 0.0, mrp = 0.0, stockQuantity = 0, sku = ""
    )
    val hasVariants = variants.isNotEmpty()
    val currentVariant = variants.getOrNull(selectedVariantIndex) ?: variants.firstOrNull() ?: dummyVariant

    val cartKey = "${product.id}#${currentVariant.id}"
    val currentInCart = AppState.cartItems[cartKey] ?: 0
    val isOutOfStock = !hasVariants || currentVariant.stockQuantity <= 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            val isDark = AppState.isDarkMode
            // 1. Large Image Hero (200dp with Fit scaling)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isDark) Color(0xFF262626) else Color(0xFFF8FAF9)),
                contentAlignment = Alignment.Center
            ) {
                val imageUrl = product.imageUrls.getOrNull(product.thumbnailIndex)?.trim() ?: ""
                if (imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = product.nameEn,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = RoyalEmerald.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                }

                // Discount Pill
                if (hasVariants && currentVariant.mrp > currentVariant.currentPrice) {
                    val discount = ((currentVariant.mrp - currentVariant.currentPrice) / currentVariant.mrp * 100).toInt()
                    if (discount > 0) {
                        Surface(
                            color = Color(0xFFEA580C),
                            shape = RoundedCornerShape(topStart = 20.dp, bottomEnd = 10.dp),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Text(
                                "$discount% OFF",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 2. Product Name & Brand Tag
            Text(
                text = product.nameEn,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (product.brand.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Surface(
                    color = if (isDark) RoyalEmerald.copy(alpha = 0.25f) else Color(0xFFECFDF5),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(0.5.dp, if (isDark) Color(0xFF34D399).copy(alpha = 0.5f) else RoyalEmerald.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "Brand: ${product.brand}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF34D399) else RoyalEmerald,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = if (isDark) Color(0xFF333333) else Color(0xFFF1F3F4))
            Spacer(Modifier.height(14.dp))

            // 3. Size / Variant Selector
            if (variants.size > 1) {
                Text(
                    text = "Select Pack Size",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    variants.forEachIndexed { idx, v ->
                        val isSelected = selectedVariantIndex == idx
                        Surface(
                            onClick = { selectedVariantIndex = idx },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) (if (AppState.isDarkMode) RoyalEmerald.copy(alpha = 0.25f) else Color(0xFFECFDF5)) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.5.dp, if (isSelected) (if (AppState.isDarkMode) Color(0xFF34D399) else RoyalEmerald) else (if (AppState.isDarkMode) Color(0xFF404040) else Color(0xFFE5E7EB))),
                            shadowElevation = if (isSelected) 2.dp else 0.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "${v.weight}${v.unit}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isSelected && AppState.isDarkMode) Color(0xFF34D399) else if (isSelected) RoyalEmerald else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    "₹${v.currentPrice.toInt()}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = if (isSelected && AppState.isDarkMode) Color(0xFF34D399) else MaterialTheme.colorScheme.onSurface
                                )
                                if (v.mrp > v.currentPrice) {
                                    Text(
                                        "₹${v.mrp.toInt()}",
                                        fontSize = 10.sp,
                                        color = Color.Gray,
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = if (AppState.isDarkMode) Color(0xFF333333) else Color(0xFFF1F3F4))
                Spacer(Modifier.height(14.dp))
            }

            // 4. Product Details / Description
            if (product.descriptionEn.isNotBlank()) {
                Text(
                    text = "Product Details",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = product.descriptionEn,
                    fontSize = 13.sp,
                    color = if (AppState.isDarkMode) Color(0xFFCCCCCC) else Color.Gray,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(16.dp))
            }

            // 5. Price & Sticky Add to Cart Action Row
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (AppState.isDarkMode) Color(0xFF242424) else Color(0xFFF9FAFB),
                border = BorderStroke(1.dp, if (AppState.isDarkMode) Color(0xFF3A3A3A) else Color(0xFFE5E7EB)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "₹${currentVariant.currentPrice.toInt()}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = RoyalEmerald
                        )
                        if (hasVariants && currentVariant.mrp > currentVariant.currentPrice) {
                            Text(
                                "MRP ₹${currentVariant.mrp.toInt()}",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                    }

                    if (isOutOfStock) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFE5E7EB)
                        ) {
                            Text(
                                "OUT OF STOCK",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                    } else if (currentInCart > 0) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = RoyalEmerald,
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clickable { AppState.updateCartQty(product.id, currentVariant.id, currentInCart - 1) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Remove, "Minus", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    text = "$currentInCart in Cart",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clickable(enabled = currentInCart < currentVariant.stockQuantity) {
                                            AppState.updateCartQty(product.id, currentVariant.id, currentInCart + 1)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Add, "Plus", tint = if (currentInCart < currentVariant.stockQuantity) Color.White else Color.White.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    } else {
                        Button(
                            onClick = { AppState.addToCart(product.id, currentVariant.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalEmerald),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                        ) {
                            Icon(Icons.Default.ShoppingCart, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "ADD TO CART",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingQuickCartBar(
    itemCount: Int,
    subtotal: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = RoyalEmerald,
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ShoppingCart, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "$itemCount ${if (itemCount == 1) "ITEM" else "ITEMS"} • ₹${subtotal.toInt()}",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                    Text(
                        "Extra discounts applied",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 10.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "View Cart",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun TrustBadge(label: String) {
    Surface(
        color = Color.White.copy(alpha = 0.9f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
            fontWeight = FontWeight.Bold,
            color = RoyalEmerald,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun QtyController(qty: Int, onMinus: () -> Unit, onPlus: () -> Unit, maxStock: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .background(Color(0xFFF1F3F4), RoundedCornerShape(10.dp))
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            IconButton(onClick = onMinus, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = "Decrease Quantity",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(text = qty.toString(), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            IconButton(onClick = onPlus, enabled = qty < maxStock, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Increase Quantity",
                    modifier = Modifier.size(16.dp),
                    tint = if (qty < maxStock) RoyalEmerald else Color.Gray
                )
            }
        }
        if (qty >= maxStock) {
            Text(
                text = "Only $maxStock available",
                color = AlertRed,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun CustomerCartView(
    onOpenAddressManager: () -> Unit,
    onProductClick: (Product) -> Unit = {}
) {
    val selectedAddress = AppState.addressesList.find { it.isSelected }
    var selectedGiftId by remember { mutableStateOf<String?>(null) }

    if (AppState.cartItems.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .testTag("empty_cart_view"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(72.dp), tint = Color.LightGray)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Your Cart is Empty",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Go back to home screen and add products to your cart.",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    } else {
        Scaffold(
            bottomBar = {
                CheckoutBottomBar(selectedGiftId)
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        "MY CART",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = RoyalEmerald
                        )
                    )
                }

                item {
                    AddressCard(selectedAddress, onOpenAddressManager)
                }

                item {
                    Text(
                        "Order Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                val listKeys = AppState.cartItems.keys.toList()
                items(listKeys) { key ->
                    val parts = key.split("#")
                    if (parts.size == 2) {
                        val prodId = parts[0]
                        val variantId = parts[1]
                        val qty = AppState.cartItems[key] ?: 0
                        val prod = AppState.productsList.find { it.id == prodId }
                        val variant = prod?.variants?.find { it.id == variantId }

                        if (prod != null && variant != null) {
                            CartItemRow(
                                product = prod,
                                variant = variant,
                                quantity = qty,
                                onProductClick = onProductClick
                            )
                        }
                    }
                }

                item {
                    GiftSelectionSection(
                        selectedGiftId = selectedGiftId,
                        onGiftSelect = { selectedGiftId = it }
                    )
                }

                item {
                    PriceBreakdown(selectedGiftId)
                }

                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun AddressCard(address: Address?, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(RoyalEmerald.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = RoyalEmerald)
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Delivery Address",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
                Text(
                    address?.houseNo ?: "No Address Selected",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (address != null) {
                    Text(
                        address.landmark,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}

@Composable
fun CartItemRow(
    product: Product,
    variant: ProductVariant,
    quantity: Int,
    onProductClick: (Product) -> Unit = {}
) {
    val isDark = AppState.isDarkMode
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF2E2E2E) else Color(0xFFF1F3F4))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product image (clickable to open detail view)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDark) Color(0xFF262626) else Color(0xFFF8FAF9))
                    .clickable { onProductClick(product) },
                contentAlignment = Alignment.Center
            ) {
                val imageUrl = product.imageUrls.getOrNull(product.thumbnailIndex) ?: ""
                if (imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = product.nameEn,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.ShoppingCart, null, tint = RoyalEmerald.copy(alpha = 0.4f), modifier = Modifier.size(24.dp))
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onProductClick(product) }
            ) {
                Text(
                    product.nameEn,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${variant.weight}${variant.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Color(0xFF94A3B8) else Color.Gray
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "₹${variant.currentPrice.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) Color(0xFF34D399) else RoyalEmerald
                    )
                    if (variant.mrp > variant.currentPrice) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "₹${variant.mrp.toInt()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) Color(0xFF64748B) else Color.Gray,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }
            }

            // Sleek Cart Stepper
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = RoyalEmerald,
                shadowElevation = 1.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clickable { AppState.updateCartQty(product.id, variant.id, quantity - 1) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Remove, "Minus", tint = Color.White, modifier = Modifier.size(13.dp))
                    }
                    Text(
                        text = "$quantity",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    val atMaxStock = quantity >= variant.stockQuantity
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clickable(enabled = !atMaxStock) {
                                AppState.updateCartQty(product.id, variant.id, quantity + 1)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            "Plus",
                            tint = if (!atMaxStock) Color.White else Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PriceBreakdown(selectedGiftId: String? = null) {
    val selectedGift = AppState.giftConfigsList.find { it.id == selectedGiftId }
    val finalTotal = AppState.cartTotal + (selectedGift?.giftPrice ?: 0.0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PriceRow("Subtotal", "₹${AppState.cartSubtotal.toInt()}")
        if (selectedGift != null) {
            PriceRow("Gift: ${selectedGift.productName}", "₹${selectedGift.giftPrice.toInt()}")
        }
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 1.dp)
        PriceRow("Total Amount", "₹${finalTotal.toInt()}", isTotal = true)
    }
}

@Composable
fun PriceRow(label: String, value: String, isTotal: Boolean = false, color: Color = Color.Unspecified) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            color = if (isTotal) MaterialTheme.colorScheme.onSurface else Color.Gray
        )
        Text(
            value,
            style = if (isTotal) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.ExtraBold,
            color = if (isTotal) RoyalEmerald else if (value == "FREE") RoyalEmerald else if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color
        )
    }
}

@Composable
fun CheckoutBottomBar(selectedGiftId: String?) {
    val selectedAddress = AppState.addressesList.find { it.isSelected }
    val selectedGift = AppState.giftConfigsList.find { it.id == selectedGiftId }
    val finalTotal = AppState.cartTotal + (selectedGift?.giftPrice ?: 0.0)
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val isClosed = (hour < 8 || hour >= 20) && !AppState.forceStoreOpen
    val isBelowMinimum = AppState.cartSubtotal < AppState.minimumOrderAmount
    // Button enabled only when: store open, not placing, cart meets minimum order
    val canCheckout = !isClosed && !AppState.isPlacingOrder && !isBelowMinimum

    var orderErrorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    if (orderErrorMessage != null) {
        AlertDialog(
            onDismissRequest = { orderErrorMessage = null },
            title = { Text("Order Alert", fontWeight = FontWeight.Bold) },
            text = { Text(orderErrorMessage ?: "") },
            confirmButton = {
                Button(
                    onClick = { orderErrorMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalEmerald)
                ) {
                    Text("OK", color = Color.White)
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 16.dp
    ) {
        Column(modifier = Modifier.padding(20.dp).navigationBarsPadding()) {
            if (isClosed) {
                Text(
                    "Store Closed (8 AM - 8 PM)",
                    color = Color.Red,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            } else if (isBelowMinimum) {
                val needed = (AppState.minimumOrderAmount - AppState.cartSubtotal).toInt()
                Text(
                    "\u26a0 Minimum order \u20b9${AppState.minimumOrderAmount.toInt()} \u2014 Add \u20b9$needed more",
                    color = DeepGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            } else if (selectedAddress == null) {
                Text(
                    "\u26a0 Tap Place Order to add a delivery address",
                    color = DeepGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Price", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("₹${finalTotal.toInt()}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = RoyalEmerald)
                }

                Spacer(Modifier.width(24.dp))

                Button(
                    onClick = {
                        if (selectedAddress == null) {
                            // No address — show address error message
                            orderErrorMessage = "Please add and select a delivery address first. Tap the address card above to add one."
                        } else {
                            scope.launch {
                                val err = AppState.placeOrder(selectedGiftId)
                                if (err != null) {
                                    orderErrorMessage = err
                                }
                            }
                        }
                    },
                    enabled = canCheckout,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalEmerald)
                ) {
                    if (AppState.isPlacingOrder) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Placing...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    } else {
                        Text(
                            if (selectedAddress == null) "Add Address" else "Place Order",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddressSelectionDialog(onDismiss: () -> Unit) {
    var showMapPicker by remember { mutableStateOf(false) }

    if (showMapPicker) {
        AWSMapPickerDialog(
            onDismiss = { showMapPicker = false },
            onLocationSelected = { latLng, address ->
                val dist = AppState.calculateDistanceKm(
                    AppState.SHOP_LATITUDE,
                    AppState.SHOP_LONGITUDE,
                    latLng.latitude,
                    latLng.longitude
                )
                val cleanLandmark = if (address.isBlank() || address.contains("Fetching", ignoreCase = true)) {
                    "Location: %.4f, %.4f".format(java.util.Locale.US, latLng.latitude, latLng.longitude)
                } else {
                    address
                }
                AppState.addNewAddress(
                    house = "Home",
                    landmark = cleanLandmark,
                    distance = dist,
                    lat = latLng.latitude,
                    lon = latLng.longitude
                )
                showMapPicker = false
                onDismiss() // Close the AddressSelectionDialog immediately!
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
        title = {
            Text("Select or Add Address", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Choose Existing Address:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

                AppState.addressesList.forEach { addr ->
                    key(addr.id) {
                        var isEditing by remember { mutableStateOf(false) }
                        var editFlat by remember { mutableStateOf(addr.houseNo) }
                        var editLandmark by remember { mutableStateOf(addr.landmark) }
                        val isDark = AppState.isDarkMode
                        val addressContainerBg = if (isDark) {
                            if (addr.isSelected) Color(0xFF2E2415) else Color(0xFF262626)
                        } else {
                            if (addr.isSelected) SoftOrange else Color(0xFFF1F5F9)
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = addressContainerBg),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                if (isEditing) {
                                    OutlinedTextField(
                                        value = editFlat,
                                        onValueChange = { editFlat = it },
                                        label = { Text("Flat/House No") },
                                        singleLine = true,
                                        textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            focusedLabelColor = RoyalEmerald,
                                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                            focusedBorderColor = RoyalEmerald,
                                            unfocusedBorderColor = Color.Gray,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                            focusedContainerColor = MaterialTheme.colorScheme.surface
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = editLandmark,
                                        onValueChange = { editLandmark = it },
                                        label = { Text("Landmark") },
                                        singleLine = true,
                                        textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            focusedLabelColor = RoyalEmerald,
                                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                            focusedBorderColor = RoyalEmerald,
                                            unfocusedBorderColor = Color.Gray,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                            focusedContainerColor = MaterialTheme.colorScheme.surface
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.End,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        TextButton(onClick = { isEditing = false }) {
                                            Text("Cancel", color = Color.Gray)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                if (editFlat.isNotBlank()) {
                                                    AppState.editAddress(addr.id, editFlat, editLandmark)
                                                    isEditing = false
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = RoyalEmerald)
                                        ) {
                                            Text("Save", color = Color.White)
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(selected = addr.isSelected, onClick = { AppState.selectAddress(addr.id) })
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { AppState.selectAddress(addr.id) }
                                        ) {
                                            Text(addr.houseNo, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                            val isOutOfRange = addr.distanceKm > AppState.deliveryRadiusKm
                                            if (isOutOfRange) {
                                                Text(
                                                    text = "${addr.landmark} (${String.format("%.2f", addr.distanceKm)} KM) - Out of Delivery Range!",
                                                    fontSize = 12.sp,
                                                    color = Color.Red,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            } else {
                                                Text(
                                                    text = "${addr.landmark} (${String.format("%.2f", addr.distanceKm)} KM)",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                )
                                            }
                                        }
                                        IconButton(onClick = { isEditing = true }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = RoyalEmerald, modifier = Modifier.size(20.dp))
                                        }
                                        IconButton(onClick = { AppState.deleteAddress(addr.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.padding(vertical = 6.dp))

                Text("Or Add New Address:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

                Button(
                    onClick = { showMapPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepGold)
                ) {
                    Icon(Icons.Default.Map, contentDescription = "Map")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pin Location on Map", color = Color.White)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = DeepGold)) {
                Text("Done")
            }
        }
    )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerOrdersView() {
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                isRefreshing = true
                AppState.refreshAllFromCloud {
                    isRefreshing = false
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("orders_tracking_view"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Order History & Tracking",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

        if (AppState.ordersList.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.AutoMirrored.Filled.List, null, modifier = Modifier.size(54.dp), tint = Color.LightGray)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "No orders placed yet",
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        "Place your first order to see it here",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                }
            }
        } else {
            items(AppState.ordersList.sortedByDescending { it.createdAt }) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Header: Order ID + Date
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Order #${order.id.takeLast(8).uppercase()}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 15.sp
                                )
                                val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.ENGLISH)
                                Text(sdf.format(Date(order.createdAt)), fontSize = 11.sp, color = Color.Gray)
                            }
                            Text(
                                text = "₹${order.totalAmount.toInt()}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = RoyalEmerald
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // CANCELLED: show red banner instead of stepper
                        val isReturnStatus = order.status == com.example.domain.model.OrderStatus.RETURN_REQUESTED ||
                                order.status == com.example.domain.model.OrderStatus.RETURN_ACCEPTED ||
                                order.status == com.example.domain.model.OrderStatus.RETURNED

                        if (order.status == com.example.domain.model.OrderStatus.CANCELLED) {
                            Surface(
                                color = Color.Red.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Close, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                    Text("Order Cancelled", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        } else if (isReturnStatus) {
                            // Stepper hidden for return lifecycle; return banners will be shown below
                        } else {
                            // Progress Stepper
                            val step = when (order.status) {
                                com.example.domain.model.OrderStatus.PENDING -> 0
                                com.example.domain.model.OrderStatus.OUT_FOR_DELIVERY -> 1
                                com.example.domain.model.OrderStatus.DELIVERED -> 2
                                else -> 0
                            }
                            OrderProgressStepper(currentStep = step)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Items list
                        Text("Items:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color.Gray)
                        Spacer(Modifier.height(4.dp))
                        order.items.forEach { item ->
                            Text(
                                text = "• ${item.productName} (${item.selectedSize}) × ${item.quantity}  ₹${(item.priceAtPurchase * item.quantity).toInt()}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }

                        // Cancel / Return action buttons
                        var showCancelConfirm by remember { mutableStateOf(false) }
                        var showReturnConfirm by remember { mutableStateOf(false) }

                        if (order.status == com.example.domain.model.OrderStatus.PENDING) {
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { showCancelConfirm = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Cancel Order", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        if (order.status == com.example.domain.model.OrderStatus.DELIVERED) {
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { showReturnConfirm = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepGold),
                                border = BorderStroke(1.dp, DeepGold.copy(alpha = 0.6f))
                            ) {
                                Icon(Icons.Default.Replay, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Request Return", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        if (order.status == com.example.domain.model.OrderStatus.RETURN_REQUESTED) {
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                color = DeepGold.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Replay, null, tint = DeepGold, modifier = Modifier.size(16.dp))
                                    Text("Return Requested", color = DeepGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }

                        if (order.status == com.example.domain.model.OrderStatus.RETURN_ACCEPTED) {
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                color = DeepGold.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Check, null, tint = DeepGold, modifier = Modifier.size(16.dp))
                                    Text("Return Accepted (Pickup in Progress)", color = DeepGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }

                        if (order.status == com.example.domain.model.OrderStatus.RETURNED) {
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                color = RoyalEmerald.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, null, tint = RoyalEmerald, modifier = Modifier.size(16.dp))
                                    Text("Return Completed (Order Completed)", color = RoyalEmerald, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }

                        if (showCancelConfirm) {
                            AlertDialog(
                                onDismissRequest = { showCancelConfirm = false },
                                title = { Text("Cancel Order?", fontWeight = FontWeight.Bold) },
                                text = { Text("Are you sure you want to cancel this order? This action cannot be undone.") },
                                confirmButton = {
                                    TextButton(onClick = {
                                        showCancelConfirm = false
                                        AppState.customerCancelOrder(order.id)
                                    }) { Text("Yes, Cancel", color = Color.Red, fontWeight = FontWeight.Bold) }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showCancelConfirm = false }) { Text("Keep Order", color = Color.Gray) }
                                }
                            )
                        }

                        if (showReturnConfirm) {
                            AlertDialog(
                                onDismissRequest = { showReturnConfirm = false },
                                title = { Text("Request Return?", fontWeight = FontWeight.Bold) },
                                text = { Text("Submit a return request for this order? Our team will contact you to arrange the return.") },
                                confirmButton = {
                                    TextButton(onClick = {
                                        showReturnConfirm = false
                                        AppState.customerRequestReturn(order.id)
                                    }) { Text("Yes, Request Return", color = DeepGold, fontWeight = FontWeight.Bold) }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showReturnConfirm = false }) { Text("Cancel", color = Color.Gray) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun OrderProgressStepper(currentStep: Int) {
    val statusStr = when (currentStep) {
        0 -> "PENDING"
        1 -> "OUT_FOR_DELIVERY"
        2 -> "DELIVERED"
        else -> "PENDING"
    }
    com.example.ui.components.OrderTrackingTimeline(orderStatus = statusStr)
}


class ConfettiParticle(
    var xPercent: Float,
    var yPercent: Float,
    val color: Color,
    val size: Float,
    val speedY: Float,
    val speedX: Float,
    var rotation: Float,
    val rotationSpeed: Float
)

@Composable
fun OrderSuccessView(order: Order, onContinueShopping: () -> Unit) {
    // Generate some confetti particles
    val colors = listOf(RoyalEmerald, DeepGold, Color(0xFF80BEA6), Color(0xFFFD8A42))
    val particles = remember {
        List(85) {
            ConfettiParticle(
                xPercent = (0..100).random() / 100f,
                yPercent = -(0..100).random() / 100f,
                color = colors.random(),
                size = (8..20).random().toFloat(),
                speedY = (0.015f + (0..15).random() / 1000f),
                speedX = (-0.005f + (0..10).random() / 1000f),
                rotation = (0..360).random().toFloat(),
                rotationSpeed = (-4..4).random().toFloat()
            )
        }
    }

    // Confetti animation loop
    var tick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            withFrameMillis {
                particles.forEach { p ->
                    p.yPercent += p.speedY
                    p.xPercent += p.speedX
                    p.rotation += p.rotationSpeed
                    // Wrap around if it goes off bottom
                    if (p.yPercent > 1.1f) {
                        p.yPercent = -0.1f
                    }
                }
                tick++
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Draw Confetti
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { p ->
                val px = p.xPercent * size.width
                val py = p.yPercent * size.height
                if (py > 0) {
                    rotate(p.rotation, pivot = Offset(px, py)) {
                        drawRect(
                            color = p.color,
                            topLeft = Offset(px - p.size / 2, py - p.size / 2),
                            size = Size(p.size, p.size)
                        )
                    }
                }
            }
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Animated Success Checkmark
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .shadow(12.dp, CircleShape)
                    .background(Color.White, CircleShape)
                    .border(2.dp, RoyalEmerald.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(RoyalEmerald, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            // Congratulations Headers
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Order Placed!",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = RoyalEmerald
                )
                Text(
                    text = "Thank you for your purchase. We are preparing your premium pantry essentials.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(24.dp), ambientColor = RoyalEmerald.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("ORDER ID", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text(text = order.id, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("AMOUNT PAID", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text(text = "₹${order.totalAmount.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = RoyalEmerald)
                        }
                    }

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                    // Delivery Address
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = RoyalEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text("DELIVERY ADDRESS", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${order.addressHouseNo}, ${order.addressLandmark}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                    // Payment Method
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = RoyalEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text("PAYMENT METHOD", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Cash on Delivery (COD)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Button
            Button(
                onClick = onContinueShopping,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalEmerald)
            ) {
                Text(
                    text = "Continue Shopping",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CustomerAccountView(onLogoutClick: () -> Unit) {
    val user = AppState.currentUser
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "My Account",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // 1. Profile Details Card
        Text(
            text = "PROFILE INFORMATION",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(RoyalEmerald.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = RoyalEmerald)
                    }
                    Column {
                        Text(
                            text = "Full Name",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Text(
                            text = user?.name ?: "Valued Customer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                // Email Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(RoyalEmerald.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = RoyalEmerald)
                    }
                    Column {
                        Text(
                            text = "Email Address",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Text(
                            text = user?.email ?: "No email provided",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))


                // Phone Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(RoyalEmerald.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = RoyalEmerald)
                    }
                    Column {
                        Text(
                            text = "Phone Number",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Text(
                            text = user?.phone ?: "No phone provided",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // 2. Settings Card
        Text(
            text = "APP SETTINGS",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Theme Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(DeepGold.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = DeepGold)
                        }
                        Column {
                            Text(
                                text = "Dark Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Toggle light / dark appearance",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }
                    Switch(
                        checked = AppState.isDarkMode,
                        onCheckedChange = { AppState.isDarkMode = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = RoyalEmerald
                        )
                    )
                }

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                // Log out row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLogoutClick() }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.Red.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            tint = Color.Red
                        )
                    }
                    Column {
                        Text(
                            text = "Log Out",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                        Text(
                            text = "Sign out of your customer account",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AWSMapPickerDialog(
    onDismiss: () -> Unit,
    onLocationSelected: (MapLatLng, String) -> Unit
) {
    val context = LocalContext.current
    var selectedLatLng by remember { mutableStateOf(MapLatLng(AppState.SHOP_LATITUDE, AppState.SHOP_LONGITUDE)) }
    var addressText by remember { mutableStateOf("Fetching location...") }
    val scope = rememberCoroutineScope()

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    fun updateAddress(latLng: MapLatLng) {
        selectedLatLng = latLng
        scope.launch(Dispatchers.IO) {
            var resolvedAddress = ""
            // 1. Try Android Native Geocoder for exact street address
            try {
                val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val lines = (0..addr.maxAddressLineIndex).mapNotNull { addr.getAddressLine(it) }
                    if (lines.isNotEmpty()) {
                        resolvedAddress = lines.joinToString(", ")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 2. Fallback to AWS Location Services if Geocoder returns empty
            if (resolvedAddress.isBlank()) {
                try {
                    val options = GeoSearchByCoordinatesOptions.builder().maxResults(1).build()
                    val latch = java.util.concurrent.CountDownLatch(1)
                    Amplify.Geo.searchByCoordinates(
                        Coordinates(latLng.latitude, latLng.longitude),
                        options,
                        { result ->
                            val places = result.places
                            if (places.isNotEmpty()) {
                                val place = places[0]
                                val labelMatch = Regex("label=(.*?),\\s*addressNumber=").find(place.toString())
                                resolvedAddress = labelMatch?.groups?.get(1)?.value?.trim() ?: place.toString()
                            }
                            latch.countDown()
                        },
                        { error ->
                            latch.countDown()
                        }
                    )
                    latch.await(2, java.util.concurrent.TimeUnit.SECONDS)
                } catch (_: Exception) {}
            }

            // 3. Fallback if address is still blank
            if (resolvedAddress.isBlank()) {
                resolvedAddress = "Lat: ${String.format("%.4f", latLng.latitude)}, Lon: ${String.format("%.4f", latLng.longitude)}"
            }

            withContext(Dispatchers.Main) {
                addressText = resolvedAddress
            }
        }
    }

    var locationPermissionGranted by remember { mutableStateOf(androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        locationPermissionGranted = isGranted
        if (isGranted) {
            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            val currentLatLng = MapLatLng(location.latitude, location.longitude)
                            updateAddress(currentLatLng)
                        }
                    }
                    .addOnFailureListener { }
            } catch (_: SecurityException) {}
        }
    }

    LaunchedEffect(Unit) {
        updateAddress(selectedLatLng)
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            val currentLatLng = MapLatLng(location.latitude, location.longitude)
                            updateAddress(currentLatLng)
                        }
                    }
                    .addOnFailureListener { }
            } catch (_: SecurityException) {}
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val dialogContext = LocalContext.current
    var mapSearchQuery by remember { mutableStateOf("") }
    var searchResultsList by remember { mutableStateOf<List<Pair<String, MapLatLng>>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var mapZoomLevel by remember { mutableStateOf(17.0) }
    var isZoomedIn by remember { mutableStateOf(false) }
    var isMapScrolling by remember { mutableStateOf(false) }

    // Pin Drop Lift & Bounce Animations
    val pinOffsetY by animateDpAsState(
        targetValue = if (isMapScrolling) (-40).dp else (-26).dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )
    val pinScale by animateFloatAsState(
        targetValue = if (isMapScrolling) 1.2f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )
    val shadowScale by animateFloatAsState(
        targetValue = if (isMapScrolling) 0.5f else 1.0f,
        animationSpec = tween(durationMillis = 150)
    )

    // Automatic Live Search Suggestions (Debounced as user types)
    LaunchedEffect(mapSearchQuery) {
        val query = mapSearchQuery.trim()
        if (query.length >= 3) {
            kotlinx.coroutines.delay(300) // 300ms debounce while typing
            isSearching = true
            withContext(Dispatchers.IO) {
                try {
                    val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocationName(query, 5)
                    if (!addresses.isNullOrEmpty()) {
                        val list = addresses.mapNotNull { addr ->
                            val line = (0..addr.maxAddressLineIndex).mapNotNull { addr.getAddressLine(it) }.joinToString(", ")
                            val placeName = if (line.isNotBlank()) line else "${addr.featureName ?: ""}, ${addr.locality ?: ""}".trim(',', ' ')
                            if (placeName.isNotBlank()) {
                                Pair(placeName, MapLatLng(addr.latitude, addr.longitude))
                            } else null
                        }
                        withContext(Dispatchers.Main) {
                            searchResultsList = list
                        }
                    }
                } catch (_: Exception) {
                } finally {
                    withContext(Dispatchers.Main) { isSearching = false }
                }
            }
        } else {
            searchResultsList = emptyList()
        }
    }
    
    // Initialize OSMDroid Configuration
    LaunchedEffect(Unit) {
        try {
            org.osmdroid.config.Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", android.content.Context.MODE_PRIVATE))
            org.osmdroid.config.Configuration.getInstance().userAgentValue = context.packageName
        } catch (_: Exception) {}
    }
    
    BackHandler { onDismiss() }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Fully Native Android OSMMapView Component with Pin Drop Animation
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                val cameraPositionState = com.google.maps.android.compose.rememberCameraPositionState {
                    position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
                        com.google.android.gms.maps.model.LatLng(selectedLatLng.latitude, selectedLatLng.longitude), 
                        19f
                    )
                }

                LaunchedEffect(selectedLatLng) {
                    val currentTarget = cameraPositionState.position.target
                    if (Math.abs(currentTarget.latitude - selectedLatLng.latitude) > 0.0005 || Math.abs(currentTarget.longitude - selectedLatLng.longitude) > 0.0005) {
                        cameraPositionState.animate(
                            com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(
                                com.google.android.gms.maps.model.LatLng(selectedLatLng.latitude, selectedLatLng.longitude),
                                19f
                            )
                        )
                    }
                }

                com.google.maps.android.compose.GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = com.google.maps.android.compose.MapProperties(
                        mapType = com.google.maps.android.compose.MapType.NORMAL,
                        isMyLocationEnabled = locationPermissionGranted
                    ),
                    uiSettings = com.google.maps.android.compose.MapUiSettings(
                        zoomControlsEnabled = true,
                        myLocationButtonEnabled = false
                    ),
                    onMapClick = { latLng ->
                        updateAddress(MapLatLng(latLng.latitude, latLng.longitude))
                    }
                ) {
                    com.google.maps.android.compose.Marker(
                        state = com.google.maps.android.compose.MarkerState(position = com.google.android.gms.maps.model.LatLng(selectedLatLng.latitude, selectedLatLng.longitude)),
                        onClick = { true }
                    )
                }
            }

            // 2. Compact Floating Search Bar Card at top
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp)
                    .fillMaxWidth()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                        }
                        
                        OutlinedTextField(
                            value = mapSearchQuery,
                            onValueChange = { newQuery ->
                                mapSearchQuery = newQuery
                            },
                            placeholder = { Text("Search area, place, pincode...", fontSize = 14.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )

                        if (isSearching) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp).padding(4.dp), strokeWidth = 2.dp, color = RoyalEmerald)
                        } else {
                            IconButton(
                                onClick = {
                                    val query = mapSearchQuery.trim()
                                    if (query.isNotEmpty()) {
                                        val parts = query.split(",")
                                        if (parts.size == 2) {
                                            val lat = parts[0].trim().toDoubleOrNull()
                                            val lon = parts[1].trim().toDoubleOrNull()
                                            if (lat != null && lon != null) {
                                                val newLatLng = MapLatLng(lat, lon)
                                                updateAddress(newLatLng)
                                                searchResultsList = emptyList()
                                                return@IconButton
                                            }
                                        }
                                        
                                        isSearching = true
                                        scope.launch(Dispatchers.IO) {
                                            try {
                                                val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                                                @Suppress("DEPRECATION")
                                                val addresses = geocoder.getFromLocationName(query, 5)
                                                if (!addresses.isNullOrEmpty()) {
                                                    val list = addresses.mapNotNull { addr ->
                                                        val line = (0..addr.maxAddressLineIndex).mapNotNull { addr.getAddressLine(it) }.joinToString(", ")
                                                        val placeName = if (line.isNotBlank()) line else "${addr.featureName ?: ""}, ${addr.locality ?: ""}".trim(',', ' ')
                                                        if (placeName.isNotBlank()) {
                                                            Pair(placeName, MapLatLng(addr.latitude, addr.longitude))
                                                        } else null
                                                    }
                                                    withContext(Dispatchers.Main) {
                                                        searchResultsList = list
                                                        if (list.isNotEmpty()) {
                                                            updateAddress(list[0].second)
                                                        }
                                                    }
                                                } else {
                                                    withContext(Dispatchers.Main) {
                                                        Toast.makeText(dialogContext, "Location not found", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(dialogContext, "Search error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                                }
                                            } finally {
                                                withContext(Dispatchers.Main) { isSearching = false }
                                            }
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = RoyalEmerald)
                            }
                        }
                    }
                }

                // Automatic Search Results Dropdown List (Appears as user types!)
                if (searchResultsList.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                            items(searchResultsList) { result ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            updateAddress(result.second)
                                            mapSearchQuery = result.first
                                            searchResultsList = emptyList()
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = RoyalEmerald, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = result.first,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }

            // Small location icon on the side with some color
            FloatingActionButton(
                onClick = {
                    if (locationPermissionGranted) {
                        try {
                            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                                if (loc != null) {
                                    updateAddress(MapLatLng(loc.latitude, loc.longitude))
                                }
                            }
                            fusedLocationClient.getCurrentLocation(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, null)
                                .addOnSuccessListener { loc ->
                                    if (loc != null) {
                                        updateAddress(MapLatLng(loc.latitude, loc.longitude))
                                    }
                                }
                        } catch (e: SecurityException) {
                            Toast.makeText(dialogContext, "Location permission required", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 150.dp)
                    .size(44.dp),
                containerColor = RoyalEmerald,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "My Location", modifier = Modifier.size(20.dp))
            }

            // Floating Bottom Panel Card (Positioned neatly at the bottom with 12dp margin)
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = addressText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth()
                    )

                    val coordsText = "Lat: ${String.format("%.6f", selectedLatLng.latitude)}, Lon: ${String.format("%.6f", selectedLatLng.longitude)}"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = RoyalEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = coordsText,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                val rawCoords = "${selectedLatLng.latitude},${selectedLatLng.longitude}"
                                val clipboard = dialogContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("GPS Coordinates", rawCoords))
                                Toast.makeText(dialogContext, "Raw coordinates copied!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy coordinates",
                                tint = RoyalEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val confirmedAddress = if (addressText.isBlank() || addressText.contains("Fetching", ignoreCase = true)) {
                                "Location: %.4f, %.4f".format(java.util.Locale.US, selectedLatLng.latitude, selectedLatLng.longitude)
                            } else {
                                addressText
                            }
                            onLocationSelected(selectedLatLng, confirmedAddress)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalEmerald)
                    ) {
                        Text("Confirm Location", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun GiftSelectionSection(selectedGiftId: String?, onGiftSelect: (String?) -> Unit) {
    val subtotal = AppState.cartSubtotal
    val availableGifts = AppState.giftConfigsList.sortedBy { it.thresholdAmount }
    
    if (availableGifts.isEmpty()) return

    // Find unlocked gifts
    val unlockedGifts = availableGifts.filter { subtotal >= it.thresholdAmount }
    val nextGift = availableGifts.firstOrNull { subtotal < it.thresholdAmount && it.stockQuantity > 0 }

    // Check if current selection is valid
    androidx.compose.runtime.LaunchedEffect(subtotal, availableGifts) {
        if (selectedGiftId != null && unlockedGifts.none { it.id == selectedGiftId }) {
            onGiftSelect(null)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .background(RoyalEmerald.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, contentDescription = null, tint = DeepGold)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Special Offers", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
        }

        if (unlockedGifts.isNotEmpty()) {
            Text("Congratulations! You've unlocked these special gifts. Select one below:", fontSize = 13.sp, color = Color.Gray)
            
            unlockedGifts.forEach { gift ->
                val isOutOfStock = gift.stockQuantity <= 0
                val isSelected = selectedGiftId == gift.id
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) RoyalEmerald.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface)
                        .clickable(enabled = !isOutOfStock) {
                            if (isSelected) onGiftSelect(null) else onGiftSelect(gift.id)
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { if (isSelected) onGiftSelect(null) else onGiftSelect(gift.id) },
                        enabled = !isOutOfStock,
                        colors = RadioButtonDefaults.colors(selectedColor = RoyalEmerald)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    if (gift.imageUrl.isNotBlank()) {
                        coil.compose.AsyncImage(
                            model = gift.imageUrl,
                            contentDescription = gift.productName,
                            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(gift.productName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        if (isOutOfStock) {
                            Text("OUT OF STOCK", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AlertRed)
                        } else {
                            Text("Only ₹${gift.giftPrice.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepGold)
                        }
                    }
                }
            }
        }

        // FOMO Message for next tier
        if (nextGift != null) {
            val amountNeeded = nextGift.thresholdAmount - subtotal
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepGold.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = DeepGold, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Add ₹${amountNeeded.toInt()} more to unlock ${nextGift.productName} for just ₹${nextGift.giftPrice.toInt()}!",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
