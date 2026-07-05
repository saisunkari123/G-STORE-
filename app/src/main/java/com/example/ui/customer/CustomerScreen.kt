package com.example.ui.customer

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.compose.AsyncImagePainter
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import java.text.SimpleDateFormat
import java.util.*

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
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == "HOME",
                    onClick = { selectedTab = "HOME" },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Shop") },
                    label = { Text("Shop", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_home")
                )
                NavigationBarItem(
                    selected = selectedTab == "CART",
                    onClick = { selectedTab = "CART" },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (AppState.cartItems.isNotEmpty()) {
                                    Badge(containerColor = DeepGold) {
                                        Text(
                                            text = AppState.cartItems.values.sum().toString(),
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                        }
                    },
                    label = { Text("Cart", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_cart")
                )
                NavigationBarItem(
                    selected = selectedTab == "TRACKING",
                    onClick = { selectedTab = "TRACKING" },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "My Orders") },
                    label = { Text("Orders", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_orders")
                )
                NavigationBarItem(
                    selected = selectedTab == "ACCOUNT",
                    onClick = { selectedTab = "ACCOUNT" },
                    icon = { Icon(Icons.Default.Person, contentDescription = "My Account") },
                    label = { Text("Account", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_account")
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                "HOME" -> CustomerCatalogView(onLogoutClick = { showLogoutConfirm = true })
                "CART" -> CustomerCartView(onOpenAddressManager = { showAddressModal = true })
                "TRACKING" -> CustomerOrdersView()
                "ACCOUNT" -> CustomerAccountView(onLogoutClick = { showLogoutConfirm = true })
            }

            if (showAddressModal) {
                AddressSelectionDialog(onDismiss = { showAddressModal = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerCatalogView(onLogoutClick: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val filteredProducts = AppState.productsList.filter {
        val matchesSearch = it.nameEn.contains(searchQuery, ignoreCase = true) ||
            it.brand.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "All" || it.brand.equals(selectedCategory, ignoreCase = true)
        matchesSearch && matchesCategory
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "G-STORE",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-1).sp,
                                color = RoyalEmerald
                            )
                        )
                    },
                    actions = {}
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                // Search Bar integrated nicely
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search premium grains...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = RoyalEmerald) },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = RoyalEmerald.copy(alpha = 0.3f),
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                    )
                )
            }

            item {
                CategoryFilters(selectedCategory, onCategorySelect = { selectedCategory = it })
            }

            item {
                SectionHeader(
                    title = if (searchQuery.isEmpty()) "Curated Selection" else "Search Results"
                )
            }

            if (filteredProducts.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No items found matching '$searchQuery'", color = Color.Gray)
                    }
                }
            } else {
                items(filteredProducts) { product ->
                    CustomerProductCard(product)
                }
            }
        }
    }
}



@Composable
fun CategoryFilters(selectedCategory: String, onCategorySelect: (String) -> Unit) {
    // Uncle's actual 4 brands — used as filter chips
    val categories = listOf("All", "Akshaya", "Sameera", "Bell Brand", "Lalitha Brand")
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
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
                    containerColor = MaterialTheme.colorScheme.surface
                )
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

@Composable
fun CustomerProductCard(product: Product) {
    var selectedVariantIndex by remember { mutableStateOf(0) }
    // Create a dummy variant so the card always renders — even for products with no variants yet
    val dummyVariant = com.example.domain.model.ProductVariant(
        id = "none", weight = "—", unit = "", currentPrice = 0.0, mrp = 0.0, stockQuantity = 0, sku = ""
    )
    val hasVariants = product.variants.isNotEmpty()
    val currentVariant = product.variants.getOrNull(selectedVariantIndex)
        ?: product.variants.firstOrNull()
        ?: dummyVariant

    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp), ambientColor = RoyalEmerald.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box {
            val imageUrl = product.imageUrls.getOrNull(product.thumbnailIndex)?.trim() ?: ""
            val isValidUrl = imageUrl.isNotEmpty()

            if (isValidUrl) {
                // SubcomposeAsyncImage correctly triggers Coil loading from the first composition
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = product.nameEn,
                    modifier = Modifier.fillMaxWidth().height(240.dp),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .background(Brush.verticalGradient(
                                    listOf(RoyalEmerald.copy(alpha = 0.15f), RoyalEmerald.copy(alpha = 0.05f))
                                ))
                        )
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .background(Brush.verticalGradient(
                                    listOf(RoyalEmerald.copy(alpha = 0.15f), RoyalEmerald.copy(alpha = 0.05f))
                                )),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.ShoppingCart, null,
                                    tint = RoyalEmerald.copy(alpha = 0.4f),
                                    modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(8.dp))
                                Text(product.nameEn,
                                    color = RoyalEmerald.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                )
            } else {
                // No valid URL — show placeholder directly
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(Brush.verticalGradient(
                            listOf(RoyalEmerald.copy(alpha = 0.15f), RoyalEmerald.copy(alpha = 0.05f))
                        )),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ShoppingCart, null,
                            tint = RoyalEmerald.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(product.nameEn,
                            color = RoyalEmerald.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.TopStart)
            ) {
                TrustBadge(if (product.brand.isNotBlank()) product.brand else "Mill-Direct")
            }

            if (currentVariant.currentPrice < currentVariant.mrp) {
                val discount = ((currentVariant.mrp - currentVariant.currentPrice) / currentVariant.mrp * 100).toInt()
                Surface(
                    color = DeepGold,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
                ) {
                    Text(
                        "$discount% OFF",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    product.nameEn,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Column(horizontalAlignment = Alignment.End) {
                    if (hasVariants && currentVariant.mrp > 0) {
                        Text(
                            "₹${currentVariant.mrp.toInt()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        )
                        Text(
                            "₹${currentVariant.currentPrice.toInt()}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = RoyalEmerald
                        )
                    } else {
                        Text(
                            "Price TBD",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RoyalEmerald.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            if (product.descriptionEn.isNotBlank()) {
                Text(
                    product.descriptionEn,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 2,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Spacer(Modifier.height(8.dp))
            }

            // Weight Selectors — only show when variants exist
            if (hasVariants) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 12.dp).horizontalScroll(rememberScrollState())
                ) {
                    product.variants.forEachIndexed { index, variant ->
                        val isSelected = selectedVariantIndex == index
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedVariantIndex = index },
                            color = if (isSelected) RoyalEmerald else Color.Transparent,
                            border = if (isSelected) null else BorderStroke(1.dp, Color.LightGray),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "${variant.weight}${variant.unit}",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            } else {
                Spacer(Modifier.height(12.dp))
            }

            val isOutOfStock = !hasVariants || currentVariant.stockQuantity <= 0
            val cartKey = "${product.id}#${currentVariant.id}"
            val currentInCart = AppState.cartItems[cartKey] ?: 0

            if (hasVariants && currentInCart > 0) {
                QtyController(
                    qty = currentInCart,
                    onMinus = { AppState.updateCartQty(product.id, currentVariant.id, currentInCart - 1) },
                    onPlus = { AppState.updateCartQty(product.id, currentVariant.id, currentInCart + 1) },
                    maxStock = currentVariant.stockQuantity
                )
            } else {
                Button(
                    onClick = { if (hasVariants) AppState.addToCart(product.id, currentVariant.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    enabled = hasVariants && !isOutOfStock,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasVariants) RoyalEmerald else Color.Gray.copy(alpha = 0.3f)
                    )
                ) {
                    Icon(Icons.Outlined.ShoppingBag, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        when {
                            !hasVariants -> "COMING SOON"
                            isOutOfStock -> "OUT OF STOCK"
                            else -> "ADD TO CART"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
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
                    Icons.Default.Clear,
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
fun CustomerCartView(onOpenAddressManager: () -> Unit) {
    val selectedAddress = AppState.addressesList.find { it.isSelected }

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
                CheckoutBottomBar()
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
                            CartItemRow(prod, variant, qty)
                        }
                    }
                }

                item {
                    PriceBreakdown()
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
fun CartItemRow(product: Product, variant: ProductVariant, quantity: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = product.imageUrls.getOrNull(product.thumbnailIndex) ?: "",
            contentDescription = product.nameEn,
            modifier = Modifier
                .size(90.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF2F2F2)),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                product.nameEn,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "${variant.weight}${variant.unit}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "₹${variant.currentPrice.toInt()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = DeepGold
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            IconButton(
                onClick = { AppState.updateCartQty(product.id, variant.id, 0) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Remove", tint = Color.LightGray, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(4.dp)
            ) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = "Decrease",
                    modifier = Modifier.size(16.dp).clickable { AppState.updateCartQty(product.id, variant.id, quantity - 1) },
                    tint = RoyalEmerald
                )
                Text(
                    "$quantity",
                    modifier = Modifier.padding(horizontal = 12.dp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val atMaxStock = quantity >= variant.stockQuantity
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Increase",
                    modifier = Modifier
                        .size(16.dp)
                        .then(
                            if (!atMaxStock) Modifier.clickable { AppState.updateCartQty(product.id, variant.id, quantity + 1) }
                            else Modifier
                        ),
                    tint = if (!atMaxStock) RoyalEmerald else Color.LightGray
                )
            }
        }
    }
}

@Composable
fun PriceBreakdown() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PriceRow("Subtotal", "₹${AppState.cartSubtotal.toInt()}")
        PriceRow("Delivery Fee", if (AppState.cartDeliveryFee == 0.0) "FREE" else "₹${AppState.cartDeliveryFee.toInt()}")
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 1.dp)
        PriceRow("Total Amount", "₹${AppState.cartTotal.toInt()}", isTotal = true)
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
fun CheckoutBottomBar() {
    val selectedAddress = AppState.addressesList.find { it.isSelected }
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val isClosed = (hour < 8 || hour >= 20) && !AppState.forceStoreOpen
    // Button is enabled as long as store is open and not currently placing
    val canCheckout = !isClosed && !AppState.isPlacingOrder

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
            } else if (selectedAddress == null) {
                Text(
                    "⚠ Tap Place Order to add a delivery address",
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
                    Text("₹${AppState.cartTotal.toInt()}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = RoyalEmerald)
                }

                Spacer(Modifier.width(24.dp))

                Button(
                    onClick = {
                        if (selectedAddress == null) {
                            // No address — show address error message
                            orderErrorMessage = "Please add and select a delivery address first. Tap the address card above to add one."
                        } else {
                            scope.launch {
                                val err = AppState.placeOrder()
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
    var flatDetails by remember { mutableStateOf("") }
    var landmarkDetails by remember { mutableStateOf("") }
    var selectedLatitude by remember { mutableStateOf(0.0) }
    var selectedLongitude by remember { mutableStateOf(0.0) }
    var selectedDist by remember { mutableStateOf(2.5) }
    var showMapPicker by remember { mutableStateOf(false) }

    if (showMapPicker) {
        GoogleMapPickerDialog(
            onDismiss = { showMapPicker = false },
            onLocationSelected = { latLng, address ->
                selectedLatitude = latLng.latitude
                selectedLongitude = latLng.longitude
                landmarkDetails = address
                selectedDist = AppState.calculateDistanceKm(
                    AppState.SHOP_LATITUDE,
                    AppState.SHOP_LONGITUDE,
                    latLng.latitude,
                    latLng.longitude
                )
            }
        )
    }

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

                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (addr.isSelected) SoftOrange else LightGrey),
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
                                            Text("${addr.landmark} (${addr.distanceKm} KM)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
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

                if (selectedLatitude != 0.0) {
                    val feeText = if (selectedDist <= 2.0) "Free" else "${Math.ceil(selectedDist) * 10.0} rupees"
                    Text(
                        text = "Calculated Distance: ${String.format("%.2f", selectedDist)} km\nDelivery Fee: $feeText",
                        color = RoyalEmerald,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                OutlinedTextField(
                    value = flatDetails,
                    onValueChange = { flatDetails = it },
                    label = { Text("Flat/House No, Road Name") },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.fillMaxWidth().testTag("addr_flat"),
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

                OutlinedTextField(
                    value = landmarkDetails,
                    onValueChange = { landmarkDetails = it },
                    label = { Text("Landmark / Map Address") },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.fillMaxWidth().testTag("addr_landmark"),
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

                Button(
                    onClick = {
                        if (flatDetails.isNotBlank()) {
                            AppState.addNewAddress(
                                house = flatDetails,
                                landmark = landmarkDetails,
                                distance = selectedDist,
                                lat = selectedLatitude,
                                lon = selectedLongitude
                            )
                            flatDetails = ""
                            landmarkDetails = ""
                            selectedDist = 2.5
                            selectedLatitude = 0.0
                            selectedLongitude = 0.0
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("addr_add_submit"),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalEmerald)
                ) {
                    Text("Add & Select", color = Color.White)
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

@Composable
fun CustomerOrdersView() {
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
            items(AppState.ordersList) { order ->
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
                    }
                }
            }
        }
    }
}

@Composable
fun OrderProgressStepper(currentStep: Int) {
    // Steps: 0 = Placed, 1 = Out for Delivery, 2 = Delivered
    val steps = listOf("Placed", "Dispatched", "Delivered")
    val icons = listOf(Icons.Default.CheckCircle, Icons.Default.LocalShipping, Icons.Default.Home)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, label ->
            val isCompleted = index <= currentStep
            val isCurrent = index == currentStep

            // Step node
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(if (isCurrent) 36.dp else 30.dp)
                            .background(
                                color = if (isCompleted) RoyalEmerald else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icons[index],
                            contentDescription = null,
                            tint = if (isCompleted) Color.White else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    // Pulse ring for current step
                    if (isCurrent) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(RoyalEmerald.copy(alpha = 0.15f), CircleShape)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCompleted) RoyalEmerald else Color.Gray,
                    textAlign = TextAlign.Center
                )
            }

            // Connector line between steps
            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(0.5f)
                        .height(2.dp)
                        .background(
                            if (index < currentStep) RoyalEmerald
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                )
            }
        }
    }
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

@Composable
fun GoogleMapPickerDialog(
    onDismiss: () -> Unit,
    onLocationSelected: (LatLng, String) -> Unit
) {
    val shopLatLng = LatLng(AppState.SHOP_LATITUDE, AppState.SHOP_LONGITUDE)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(shopLatLng, 15f)
    }

    var selectedLatLng by remember { mutableStateOf(shopLatLng) }
    var addressText by remember { mutableStateOf("Fetching location...") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Geocoder to fetch address details for free locally on device
    fun updateAddress(latLng: LatLng) {
        selectedLatLng = latLng
        scope.launch(Dispatchers.IO) {
            try {
                val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                // Geocoder.getFromLocation has a blocking signature in older API levels, so run on IO
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val line = address.getAddressLine(0) ?: "Selected Pin Location"
                    withContext(Dispatchers.Main) {
                        addressText = line
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        addressText = "Lat: ${String.format("%.4f", latLng.latitude)}, Lon: ${String.format("%.4f", latLng.longitude)}"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    addressText = "Lat: ${String.format("%.4f", latLng.latitude)}, Lon: ${String.format("%.4f", latLng.longitude)}"
                }
            }
        }
    }

    LaunchedEffect(cameraPositionState.position) {
        val target = cameraPositionState.position.target
        updateAddress(target)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Set Delivery Pin",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().height(420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Drag the map to position your exact home under the red center pin.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState
                    )
                    
                    // Stationary pin icon locked in the absolute center of the map
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Center Pin",
                        tint = Color.Red,
                        modifier = Modifier
                            .size(44.dp)
                            .align(Alignment.Center)
                            .padding(bottom = 22.dp) // Offset to align pin tip exactly to target center
                    )
                }
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = addressText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onLocationSelected(selectedLatLng, addressText)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalEmerald)
            ) {
                Text("Confirm Location", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

