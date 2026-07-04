package com.example.ui.admin

import android.net.Uri
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.data.remote.CloudinaryUploader
import com.example.domain.model.OrderStatus
import com.example.domain.model.Product
import com.example.domain.model.ProductVariant
import com.example.ui.state.AppState
import com.example.ui.theme.DeepGold
import com.example.ui.theme.RoyalEmerald
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen() {
    var adminTab by remember { mutableStateOf("ORDERS") } // "ORDERS", "INVENTORY"
    var showProductEditor by remember { mutableStateOf<Product?>(null) }
    var isAddingProduct by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

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
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
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
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                // Header with Logout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("G-STORE Control", fontWeight = FontWeight.Black, fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("Admin Center", fontSize = 12.sp, color = Color.Gray)
                    }
                    IconButton(
                        onClick = { showLogoutConfirm = true },
                        modifier = Modifier.background(DeepGold.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = DeepGold)
                    }
                }

                AdminDashboardStats()

                Spacer(modifier = Modifier.height(12.dp))

                TabRow(
                    selectedTabIndex = if (adminTab == "ORDERS") 0 else 1,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = RoyalEmerald,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[if (adminTab == "ORDERS") 0 else 1]),
                            color = RoyalEmerald
                        )
                    }
                ) {
                    Tab(
                        selected = adminTab == "ORDERS",
                        onClick = { adminTab = "ORDERS" },
                        text = { Text("Live Orders", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = adminTab == "INVENTORY",
                        onClick = { adminTab = "INVENTORY" },
                        text = { Text("Inventory", fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (adminTab) {
                "ORDERS" -> AdminOrdersView()
                "INVENTORY" -> AdminInventoryView(
                    onAddProductClicked = { isAddingProduct = true },
                    onEditProductClicked = { showProductEditor = it }
                )
            }

            if (isAddingProduct || showProductEditor != null) {
                AdminProductEditor(
                    existingProduct = showProductEditor,
                    onDismiss = { 
                        isAddingProduct = false
                        showProductEditor = null
                    }
                )
            }
        }
    }
}

@Composable
fun AdminDashboardStats() {
    val totalSales = AppState.ordersList.filter { it.status == OrderStatus.DELIVERED }.sumOf { it.totalAmount }
    val pendingCount = AppState.ordersList.filter { it.status == OrderStatus.PENDING }.size
    val totalOrdersCount = AppState.ordersList.size

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(label = "Total Sales", value = "₹${totalSales.toInt()}", icon = Icons.Default.ShoppingCart, color = RoyalEmerald)
        StatCard(label = "Pending", value = "$pendingCount", icon = Icons.Default.Notifications, color = DeepGold)
        StatCard(label = "Orders", value = "$totalOrdersCount", icon = Icons.Default.List, color = Color.Gray)
    }
}

@Composable
fun RowScope.StatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Card(
        modifier = Modifier.weight(1f),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

@Composable
fun AdminOrdersView() {
    val activeOrders = AppState.ordersList.filter {
        it.status != OrderStatus.DELIVERED && it.status != OrderStatus.CANCELLED
    }

    if (activeOrders.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(64.dp), tint = RoyalEmerald.copy(alpha = 0.3f))
            Text("No active orders", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(activeOrders) { order ->
                OrderCard(order)
            }
        }
    }
}

@Composable
fun OrderCard(order: com.example.domain.model.Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Order #${order.id}", fontWeight = FontWeight.Black, color = DeepGold, fontSize = 18.sp)
                    val sdf = java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.ENGLISH)
                    Text(sdf.format(java.util.Date(order.createdAt)), fontSize = 11.sp, color = Color.Gray)
                }
                Surface(
                    color = when(order.status) {
                        OrderStatus.PENDING -> Color(0xFFFFF4E5)
                        else -> RoyalEmerald.copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = order.status.name,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when(order.status) {
                            OrderStatus.PENDING -> DeepGold
                            else -> RoyalEmerald
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Customer Details Section
            Text("CUSTOMER DETAILS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Gray, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = RoyalEmerald)
                Spacer(modifier = Modifier.width(8.dp))
                Text(order.customerName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp), tint = RoyalEmerald)
                Spacer(modifier = Modifier.width(8.dp))
                Text(order.customerPhone, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
            }
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(top = 4.dp)) {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = RoyalEmerald)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${order.addressHouseNo}\n${order.addressLandmark}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    lineHeight = 18.sp
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = Color.LightGray)
            
            // Order Items Section
            Text("ITEMS ORDERED", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Gray, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            order.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.productName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("Size: ${item.selectedSize}", fontSize = 12.sp, color = RoyalEmerald, fontWeight = FontWeight.Bold)
                    }
                    Text("Qty: ${item.quantity}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text("₹${(item.priceAtPurchase * item.quantity).toInt()}", fontWeight = FontWeight.Black, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(60.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text("Bill Amount", fontSize = 11.sp, color = Color.Gray)
                    Text("₹${order.totalAmount.toInt()}", fontWeight = FontWeight.Black, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                if (order.status == OrderStatus.PENDING) {
                    Button(
                        onClick = { AppState.updateOrderStatus(order.id, OrderStatus.OUT_FOR_DELIVERY) },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalEmerald),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !AppState.isNetworkLoading
                    ) {
                        if (AppState.isNetworkLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Dispatch Order", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (order.status != OrderStatus.DELIVERED && order.status != OrderStatus.CANCELLED) {
                    Button(
                        onClick = { AppState.updateOrderStatus(order.id, OrderStatus.DELIVERED) },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepGold),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !AppState.isNetworkLoading
                    ) {
                        if (AppState.isNetworkLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Mark Delivered", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminInventoryView(onAddProductClicked: () -> Unit, onEditProductClicked: (Product) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Product Catalog", fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
            Button(onClick = onAddProductClicked, colors = ButtonDefaults.buttonColors(containerColor = RoyalEmerald)) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = Color.White)
                Text("New Item", modifier = Modifier.padding(start = 4.dp), color = Color.White)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(AppState.productsList, key = { it.id }) { product ->
                InventoryItemCard(product, onEditClicked = onEditProductClicked)
            }
        }
    }
}

@Composable
fun InventoryItemCard(product: Product, onEditClicked: (Product) -> Unit) {
    var showAddVariantDialog by remember { mutableStateOf(false) }
    var variantToEdit by remember { mutableStateOf<ProductVariant?>(null) }
    var variantToDelete by remember { mutableStateOf<ProductVariant?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                // Product Thumbnail in Inventory
                AsyncImage(
                    model = product.imageUrls.getOrNull(product.thumbnailIndex) ?: "",
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.background),
                    contentScale = ContentScale.Crop
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(product.nameEn, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(product.brand, fontSize = 12.sp, color = RoyalEmerald, fontWeight = FontWeight.Black)
                }
                Row {
                    IconButton(onClick = { onEditClicked(product) }) { Icon(Icons.Default.Edit, "Edit Product", tint = Color.Gray) }
                    IconButton(onClick = { AppState.adminDeleteProduct(product.id) }) { Icon(Icons.Default.Delete, "Delete Product", tint = Color.Red.copy(alpha = 0.5f)) }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Text("Variants", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            
            product.variants.forEach { variant ->
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp, color = Color.LightGray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${variant.weight} ${variant.unit}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("₹${variant.currentPrice.toInt()}", fontWeight = FontWeight.Black, color = RoyalEmerald)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("MRP ₹${variant.mrp.toInt()}", fontSize = 12.sp, color = Color.Gray, textDecoration = TextDecoration.LineThrough)
                        }
                        val stockColor = if (variant.stockQuantity < 5) Color.Red else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        Text("Stock: ${variant.stockQuantity}", color = stockColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(if(variant.stockQuantity > 0) "Available" else "Out of Stock", color = if(variant.stockQuantity > 0) RoyalEmerald else Color.Red, fontSize = 11.sp)
                    }
                    
                    Row {
                        IconButton(onClick = { variantToEdit = variant }) {
                            Icon(Icons.Default.Edit, "Edit Variant", tint = RoyalEmerald, modifier = Modifier.size(20.dp))
                        }
                        IconButton(onClick = { variantToDelete = variant }) {
                            Icon(Icons.Default.Delete, "Delete Variant", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            Button(
                onClick = { showAddVariantDialog = true },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalEmerald.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, null, tint = RoyalEmerald, modifier = Modifier.size(16.dp))
                Text("Add New Variant", color = RoyalEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showAddVariantDialog || variantToEdit != null) {
        VariantEditorDialog(
            productId = product.id,
            existingVariant = variantToEdit,
            onDismiss = {
                showAddVariantDialog = false
                variantToEdit = null
            }
        )
    }

    if (variantToDelete != null) {
        AlertDialog(
            onDismissRequest = { variantToDelete = null },
            title = { Text("Delete Variant") },
            text = { Text("Are you sure you want to delete the ${variantToDelete!!.weight} ${variantToDelete!!.unit} variant?") },
            confirmButton = {
                TextButton(onClick = { 
                    AppState.adminDeleteProductVariant(product.id, variantToDelete!!.id)
                    variantToDelete = null 
                }) { Text("Delete", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { variantToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun VariantEditorDialog(productId: String, existingVariant: ProductVariant?, onDismiss: () -> Unit) {
    var weight by remember { mutableStateOf(existingVariant?.weight ?: "") }
    var price by remember { mutableStateOf(existingVariant?.currentPrice?.toInt()?.toString() ?: "") }
    var mrp by remember { mutableStateOf(existingVariant?.mrp?.toInt()?.toString() ?: "") }
    var stock by remember { mutableStateOf(existingVariant?.stockQuantity?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(if (existingVariant == null) "Add Variant" else "Edit Variant", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Bag Size (e.g. 5, 10, 25)") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = RoyalEmerald,
                        unfocusedLabelColor = Color.Gray,
                        focusedBorderColor = RoyalEmerald,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Sale Price") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = RoyalEmerald,
                        unfocusedLabelColor = Color.Gray,
                        focusedBorderColor = RoyalEmerald,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                OutlinedTextField(
                    value = mrp,
                    onValueChange = { mrp = it },
                    label = { Text("MRP") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = RoyalEmerald,
                        unfocusedLabelColor = Color.Gray,
                        focusedBorderColor = RoyalEmerald,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Stock Quantity") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = RoyalEmerald,
                        unfocusedLabelColor = Color.Gray,
                        focusedBorderColor = RoyalEmerald,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val variant = ProductVariant(
                        id = existingVariant?.id ?: "v_${System.currentTimeMillis()}",
                        weight = weight,
                        currentPrice = price.toDoubleOrNull() ?: 0.0,
                        mrp = mrp.toDoubleOrNull() ?: 0.0,
                        stockQuantity = stock.toIntOrNull() ?: 0
                    )
                    AppState.adminUpdateProductVariant(productId, variant)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalEmerald)
            ) {
                Text("Save Variant", color = Color.White)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductEditor(existingProduct: Product?, onDismiss: () -> Unit) {
    var nameEn by remember { mutableStateOf(existingProduct?.nameEn ?: "") }
    var brand by remember { mutableStateOf(existingProduct?.brand ?: "") }
    var descEn by remember { mutableStateOf(existingProduct?.descriptionEn ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var currentImageUrl by remember { mutableStateOf(existingProduct?.imageUrls?.firstOrNull() ?: "") }
    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(if (existingProduct == null) "Product Info" else "Edit Product Info", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text("Product Name") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = RoyalEmerald,
                        unfocusedLabelColor = Color.Gray,
                        focusedBorderColor = RoyalEmerald,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand Name") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = RoyalEmerald,
                        unfocusedLabelColor = Color.Gray,
                        focusedBorderColor = RoyalEmerald,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                OutlinedTextField(
                    value = descEn,
                    onValueChange = { descEn = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = RoyalEmerald,
                        unfocusedLabelColor = Color.Gray,
                        focusedBorderColor = RoyalEmerald,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                
                Text("Product Image", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(model = selectedImageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else if (currentImageUrl.isNotBlank()) {
                        AsyncImage(model = currentImageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Add, null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                            Text("Upload Image", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                    
                    if (selectedImageUri != null || currentImageUrl.isNotBlank()) {
                        IconButton(
                            onClick = { selectedImageUri = null; currentImageUrl = "" },
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(Color.White.copy(alpha = 0.8f), CircleShape).size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                
                if (existingProduct == null) {
                    Text("Note: Variants can be added after saving the basic product info.", fontSize = 11.sp, color = RoyalEmerald, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Column {
                if (uploadError != null) {
                    Text(
                        text = "⚠️ ${uploadError}",
                        color = Color.Red,
                        fontSize = 11.sp,
                        modifier = androidx.compose.ui.Modifier.padding(bottom = 4.dp)
                    )
                }
                Button(
                    onClick = {
                        uploadError = null
                        if (selectedImageUri != null) {
                            isUploading = true
                            scope.launch {
                                try {
                                    // Copy URI to cache file on IO thread
                                    val tempFile = withContext(Dispatchers.IO) {
                                        getFileFromUri(context, selectedImageUri!!)
                                    }
                                    if (tempFile == null) {
                                        uploadError = "Failed to open selected image."
                                        isUploading = false
                                        return@launch
                                    }
                                    // Upload to Cloudinary on IO thread (blocking OkHttp call)
                                    val downloadUrl = withContext(Dispatchers.IO) {
                                        CloudinaryUploader.upload(tempFile)
                                    }
                                    // Clean up temp file
                                    withContext(Dispatchers.IO) {
                                        try { tempFile.delete() } catch (_: Exception) {}
                                    }
                                    saveProductWithImage(downloadUrl, nameEn, brand, descEn, existingProduct, onDismiss)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    uploadError = "Image upload failed: ${e.message ?: e.javaClass.simpleName}"
                                } finally {
                                    isUploading = false
                                }
                            }
                        } else {
                            saveProductWithImage(currentImageUrl, nameEn, brand, descEn, existingProduct, onDismiss)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalEmerald),
                    enabled = !isUploading && nameEn.isNotBlank()
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Uploading...", color = Color.White)
                    } else Text("Save Product", color = Color.White)
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isUploading) { Text("Cancel") } }
    )
}

private fun getFileFromUri(context: Context, uri: Uri): java.io.File? {
    return try {
        val tempFile = java.io.File(context.cacheDir, "temp_upload_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun saveProductWithImage(
    imageUrl: String,
    nameEn: String,
    brand: String,
    descEn: String,
    existingProduct: Product?,
    onDismiss: () -> Unit
) {
    if (existingProduct == null) {
        AppState.adminAddProduct(
            nameEn,
            brand,
            descEn,
            if (imageUrl.isBlank()) emptyList() else listOf(imageUrl),
            emptyList()
        )
    } else {
        val updatedProduct = existingProduct.copy(
            nameEn = nameEn,
            brand = brand,
            descriptionEn = descEn,
            imageUrls = if (imageUrl.isNotBlank()) listOf(imageUrl) else emptyList(),
            lastUpdated = System.currentTimeMillis()
        )
        AppState.adminUpdateProduct(updatedProduct)
    }
    onDismiss()
}
