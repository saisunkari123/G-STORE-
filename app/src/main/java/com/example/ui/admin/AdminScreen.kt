package com.example.ui.admin

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.data.remote.CloudinaryUploader
import com.example.domain.model.Order
import com.example.domain.model.OrderStatus
import com.example.domain.model.Product
import com.example.domain.model.ProductVariant
import com.example.ui.state.AppState
import com.example.ui.theme.AlertRed
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
    var showManageCategoriesDialog by remember { mutableStateOf(false) }
    var showManageGiftsDialog by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(320.dp)
            ) {
                AdminSidePanelContent(
                    onLogoutClicked = {
                        scope.launch { drawerState.close() }
                        showLogoutConfirm = true
                    },
                    onManageCategoriesClicked = {
                        scope.launch { drawerState.close() }
                        showManageCategoriesDialog = true
                    },
                    onManageGiftsClicked = {
                        scope.launch { drawerState.close() }
                        showManageGiftsDialog = true
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .statusBarsPadding()
                ) {
                    // Header with Menu Icon on LEFT and Centered G-STORE Admin in ONE LINE
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 3 lines Menu Icon on the LEFT
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier
                                .size(32.dp)
                                .background(RoyalEmerald.copy(alpha = 0.12f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = if (AppState.isDarkMode) Color(0xFF34D399) else RoyalEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Centered Title in ONE single clean line
                        Text(
                            text = "G-STORE Admin",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )

                        // Theme Toggle Icon on the RIGHT for easy contrast testing
                        IconButton(
                            onClick = { AppState.isDarkMode = !AppState.isDarkMode },
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    if (AppState.isDarkMode) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                if (AppState.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme",
                                tint = if (AppState.isDarkMode) Color(0xFFFBBF24) else Color(0xFF4B5563),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    val selectedTabIndex = when (adminTab) {
                        "ORDERS" -> 0
                        "INVENTORY" -> 1
                        "DELIVERY" -> 2
                        else -> 0
                    }

                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = RoyalEmerald,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = if (AppState.isDarkMode) Color(0xFF34D399) else RoyalEmerald
                            )
                        }
                    ) {
                        Tab(
                            selected = adminTab == "ORDERS",
                            onClick = { adminTab = "ORDERS" },
                            modifier = Modifier.height(38.dp),
                            text = { 
                                Text(
                                    "Live Orders", 
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (adminTab == "ORDERS") (if (AppState.isDarkMode) Color(0xFF34D399) else RoyalEmerald) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                ) 
                            }
                        )
                        Tab(
                            selected = adminTab == "INVENTORY",
                            onClick = { adminTab = "INVENTORY" },
                            modifier = Modifier.height(38.dp),
                            text = { 
                                Text(
                                    "Inventory", 
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (adminTab == "INVENTORY") (if (AppState.isDarkMode) Color(0xFF34D399) else RoyalEmerald) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                ) 
                            }
                        )
                        Tab(
                            selected = adminTab == "DELIVERY",
                            onClick = { adminTab = "DELIVERY" },
                            modifier = Modifier.height(38.dp),
                            text = { 
                                Text(
                                    "Delivery", 
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (adminTab == "DELIVERY") (if (AppState.isDarkMode) Color(0xFF34D399) else RoyalEmerald) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                ) 
                            }
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
                    "DELIVERY" -> AdminDeliveryManagementView()
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

    if (showManageCategoriesDialog) {
        AlertDialog(
            onDismissRequest = { showManageCategoriesDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Manage Categories", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = {
                var newCatName by remember { mutableStateOf("") }
                var newCatImageUrl by remember { mutableStateOf("") }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Current categories list
                    Text("Existing Categories:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 150.dp)
                            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(AppState.categoriesList) { cat ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(RoyalEmerald)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(cat.nameEn, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }

                    // Add Category Form
                    Text("Add New Category:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)

                    OutlinedTextField(
                        value = newCatName,
                        onValueChange = { newCatName = it },
                        label = { Text("Category Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = RoyalEmerald,
                            focusedLabelColor = RoyalEmerald
                        )
                    )

                    OutlinedTextField(
                        value = newCatImageUrl,
                        onValueChange = { newCatImageUrl = it },
                        label = { Text("Image URL (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = RoyalEmerald,
                            focusedLabelColor = RoyalEmerald
                        )
                    )

                    Button(
                        onClick = {
                            if (newCatName.isNotBlank()) {
                                AppState.addNewCategory(newCatName.trim(), newCatImageUrl.trim())
                                newCatName = ""
                                newCatImageUrl = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalEmerald),
                        shape = RoundedCornerShape(10.dp),
                        enabled = newCatName.isNotBlank()
                    ) {
                        Text("Add Category", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showManageCategoriesDialog = false }) {
                    Text("Close", color = RoyalEmerald, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showManageGiftsDialog) {
        ManageGiftsDialog(onDismiss = { showManageGiftsDialog = false })
    }
}

@Composable
fun AdminSidePanelContent(onLogoutClicked: () -> Unit, onManageCategoriesClicked: () -> Unit, onManageGiftsClicked: () -> Unit) {
    val totalSales = AppState.ordersList.filter { it.status == OrderStatus.DELIVERED }.sumOf { it.totalAmount }
    val pendingCount = AppState.ordersList.filter { it.status == OrderStatus.PENDING }.size
    val totalOrdersCount = AppState.ordersList.size

    var showGoalEditDialog by remember { mutableStateOf(false) }
    var newGoalText by remember { mutableStateOf("") }
    var showStoreSettingsDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = null,
                tint = RoyalEmerald,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "G-STORE Menu",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text("Dashboard Analytics & Tools", fontSize = 11.sp, color = Color.Gray)
            }
        }

        // Profile Info Card
        val currentUser = AppState.currentUser
        if (currentUser != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(RoyalEmerald, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (currentUser.name.take(1) ?: "A").uppercase(Locale.ROOT),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = currentUser.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = currentUser.email,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .background(RoyalEmerald.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            val isDev = currentUser.email.equals("developer@gstore.com", ignoreCase = true)
                            Text(
                                text = if (isDev) "DEVELOPER ADMIN" else "ADMINISTRATOR",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoyalEmerald
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))

        val percentage = if (AppState.monthlySalesGoal > 0) (totalSales / AppState.monthlySalesGoal).coerceIn(0.0, 1.0) else 0.0
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = RoyalEmerald.copy(alpha = 0.02f)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Monthly Goal (₹${AppState.monthlySalesGoal.toInt()})",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = {
                                newGoalText = AppState.monthlySalesGoal.toInt().toString()
                                showGoalEditDialog = true
                            },
                            modifier = Modifier.size(16.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Goal", tint = RoyalEmerald, modifier = Modifier.size(12.dp))
                        }
                    }
                    Text(
                        text = "${(percentage * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Black,
                        color = RoyalEmerald
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { percentage.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = RoyalEmerald,
                    trackColor = Color.LightGray.copy(alpha = 0.3f)
                )
            }
        }

        val recentOrders = AppState.ordersList.takeLast(7).map { it.totalAmount.toFloat() }
        if (recentOrders.size >= 2) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (AppState.isDarkMode) Color(0xFF333333) else Color(0xFFE5E7EB))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Sales growth trend (recent orders)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val width = size.width
                            val height = size.height
                            val maxVal = (recentOrders.maxOrNull() ?: 1f).coerceAtLeast(1f)
                            val minVal = recentOrders.minOrNull() ?: 0f
                            val delta = (maxVal - minVal).coerceAtLeast(1f)

                            val points = recentOrders.mapIndexed { idx, valAmount ->
                                val x = idx * (width / (recentOrders.size - 1))
                                val y = height - ((valAmount - minVal) / delta) * (height - 12f) - 6f
                                androidx.compose.ui.geometry.Offset(x, y)
                            }

                            val path = androidx.compose.ui.graphics.Path().apply {
                                if (points.isNotEmpty()) {
                                    moveTo(points.first().x, points.first().y)
                                    for (i in 0 until points.size - 1) {
                                        val from = points[i]
                                        val to = points[i + 1]
                                        val cx1 = (from.x + to.x) / 2f
                                        val cy1 = from.y
                                        val cx2 = (from.x + to.x) / 2f
                                        val cy2 = to.y
                                        cubicTo(cx1, cy1, cx2, cy2, to.x, to.y)
                                    }
                                }
                            }

                            val fillPath = androidx.compose.ui.graphics.Path().apply {
                                addPath(path)
                                if (points.isNotEmpty()) {
                                    lineTo(points.last().x, height)
                                    lineTo(points.first().x, height)
                                    close()
                                }
                            }
                            drawPath(
                                path = fillPath,
                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(RoyalEmerald.copy(alpha = 0.2f), Color.Transparent)
                                )
                            )

                            drawPath(
                                path = path,
                                color = if (AppState.isDarkMode) Color(0xFF34D399) else RoyalEmerald,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = 3.dp.toPx(),
                                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                            )

                            points.forEach { pt ->
                                drawCircle(
                                    color = if (AppState.isDarkMode) Color(0xFF34D399) else RoyalEmerald,
                                    radius = 4.dp.toPx(),
                                    center = pt
                                )
                            }
                        }
                    }
                }
            }
        }

        val brandSalesMap = remember(AppState.ordersList) {
            val m = mutableMapOf<String, Int>()
            AppState.ordersList.forEach { order ->
                order.items.forEach { item ->
                    val prod = AppState.productsList.find { it.id == item.productId }
                    val brandName = prod?.brand ?: "G-Store"
                    m[brandName] = (m[brandName] ?: 0) + item.quantity
                }
            }
            m.toList().sortedByDescending { it.second }.take(4)
        }
        if (brandSalesMap.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (AppState.isDarkMode) Color(0xFF333333) else Color(0xFFE5E7EB))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Top Selling Brands (Units sold)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    brandSalesMap.forEach { (brandName, unitsSold) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = brandName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$unitsSold units",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (AppState.isDarkMode) Color(0xFF34D399) else RoyalEmerald
                            )
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (AppState.isDarkMode) Color(0xFF333333) else Color(0xFFE5E7EB))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Revenue", fontSize = 12.sp, color = Color.Gray)
                    Text("₹${totalSales.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Pending Orders", fontSize = 12.sp, color = Color.Gray)
                    Text("$pendingCount", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("All Orders Count", fontSize = 12.sp, color = Color.Gray)
                    Text("$totalOrdersCount", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onManageCategoriesClicked,
            colors = ButtonDefaults.buttonColors(containerColor = RoyalEmerald),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().height(44.dp)
        ) {
            Icon(Icons.Default.Category, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Manage Categories", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onManageGiftsClicked,
            colors = ButtonDefaults.buttonColors(containerColor = RoyalEmerald),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().height(44.dp)
        ) {
            Icon(Icons.Default.Star, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Manage Gifts", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Store Settings Card ──────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = RoyalEmerald.copy(alpha = 0.04f)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(0.5.dp, RoyalEmerald.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚙ Store Settings", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    TextButton(
                        onClick = { showStoreSettingsDialog = true },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Edit", color = RoyalEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Min. Order", fontSize = 12.sp, color = Color.Gray)
                    Text("₹${AppState.minimumOrderAmount.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Delivery Radius", fontSize = 12.sp, color = Color.Gray)
                    Text("${AppState.deliveryRadiusKm.toInt()} km", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onLogoutClicked,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().height(44.dp)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log Out", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }

    if (showStoreSettingsDialog) {
        var minOrderText by remember { mutableStateOf(AppState.minimumOrderAmount.toInt().toString()) }
        var radiusText by remember { mutableStateOf(AppState.deliveryRadiusKm.toInt().toString()) }
        AlertDialog(
            onDismissRequest = { showStoreSettingsDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Store Settings", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Changes save to cloud and reflect on all devices within 10 seconds.", fontSize = 12.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = minOrderText,
                        onValueChange = { minOrderText = it.filter { c -> c.isDigit() } },
                        label = { Text("Minimum Order Amount (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = RoyalEmerald,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                    OutlinedTextField(
                        value = radiusText,
                        onValueChange = { radiusText = it.filter { c -> c.isDigit() } },
                        label = { Text("Delivery Radius (km)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = RoyalEmerald,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newMin = minOrderText.toDoubleOrNull() ?: 150.0
                        val newRadius = radiusText.toDoubleOrNull() ?: 10.0
                        AppState.saveStoreSettings(newMin, newRadius)
                        android.widget.Toast.makeText(context, "✓ Store settings updated successfully! (Min: ₹${newMin.toInt()}, Radius: ${newRadius.toInt()} km)", android.widget.Toast.LENGTH_SHORT).show()
                        showStoreSettingsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalEmerald)
                ) {
                    Text("Save to Cloud", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStoreSettingsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showGoalEditDialog) {
        AlertDialog(
            onDismissRequest = { showGoalEditDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Set Monthly Goal", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter monthly target sales amount (₹):", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    OutlinedTextField(
                        value = newGoalText,
                        onValueChange = { newGoalText = it.filter { char -> char.isDigit() } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = RoyalEmerald,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = newGoalText.toDoubleOrNull() ?: 50000.0
                        AppState.monthlySalesGoal = amount
                        showGoalEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalEmerald)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersView() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<OrderStatus?>(null) }
    var selectedOrderForMap by remember { mutableStateOf<Order?>(null) }
    var selectedOrderForDetail by remember { mutableStateOf<Order?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    if (selectedOrderForMap != null) {
        AdminOrderMapModal(order = selectedOrderForMap!!, onDismiss = { selectedOrderForMap = null })
    }

    if (selectedOrderForDetail != null) {
        AdminOrderDetailModal(
            order = selectedOrderForDetail!!,
            onDismiss = { selectedOrderForDetail = null },
            onViewMap = { orderToMap ->
                selectedOrderForDetail = null
                selectedOrderForMap = orderToMap
            }
        )
    }

    val filteredOrders = AppState.ordersList.filter { order ->
        val cleanQuery = searchQuery.replace("\\s".toRegex(), "").lowercase()
        val matchesSearch = cleanQuery.isEmpty() ||
            order.id.replace("\\s".toRegex(), "").lowercase().contains(cleanQuery) ||
            order.customerName.replace("\\s".toRegex(), "").lowercase().contains(cleanQuery) ||
            order.customerPhone.replace("\\s".toRegex(), "").lowercase().contains(cleanQuery)
            
        val matchesStatus = cleanQuery.isNotEmpty() || selectedStatusFilter == null || order.status == selectedStatusFilter
        
        matchesSearch && matchesStatus
    }.sortedByDescending { it.createdAt }

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
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by ID, name, or phone...", color = Color.Gray, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = if (AppState.isDarkMode) Color(0xFF34D399) else RoyalEmerald) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = if (AppState.isDarkMode) Color(0xFF34D399) else RoyalEmerald,
                    unfocusedBorderColor = if (AppState.isDarkMode) Color(0xFF3A3A3A) else Color.LightGray
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Status filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val isAllSelected = selectedStatusFilter == null
                AssistChip(
                    onClick = { selectedStatusFilter = null },
                    label = { Text("All (${AppState.ordersList.size})", fontSize = 11.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (isAllSelected) (if (AppState.isDarkMode) Color(0xFF34D399).copy(alpha = 0.2f) else RoyalEmerald.copy(alpha = 0.15f)) else Color.Transparent,
                        labelColor = if (isAllSelected) (if (AppState.isDarkMode) Color(0xFF34D399) else RoyalEmerald) else Color.Gray
                    ),
                    border = BorderStroke(1.dp, if (isAllSelected) (if (AppState.isDarkMode) Color(0xFF34D399) else RoyalEmerald) else Color.LightGray.copy(alpha = 0.4f))
                )

                OrderStatus.values().forEach { status ->
                    val count = AppState.ordersList.count { it.status == status }
                    val isSelected = selectedStatusFilter == status
                    val label = when(status) {
                        OrderStatus.PENDING -> "Pending"
                        OrderStatus.OUT_FOR_DELIVERY -> "Dispatched"
                        OrderStatus.DELIVERED -> "Delivered"
                        OrderStatus.CANCELLED -> "Cancelled"
                        OrderStatus.RETURN_REQUESTED -> "Return Req."
                        OrderStatus.RETURN_ACCEPTED -> "Ret. Acc."
                        OrderStatus.RETURNED -> "Returned"
                    }
                    AssistChip(
                        onClick = { selectedStatusFilter = status },
                        label = { Text("$label ($count)", fontSize = 11.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isSelected) (if (AppState.isDarkMode) Color(0xFF34D399).copy(alpha = 0.2f) else RoyalEmerald.copy(alpha = 0.15f)) else Color.Transparent,
                            labelColor = if (isSelected) (if (AppState.isDarkMode) Color(0xFF34D399) else RoyalEmerald) else Color.Gray
                        ),
                        border = BorderStroke(1.dp, if (isSelected) (if (AppState.isDarkMode) Color(0xFF34D399) else RoyalEmerald) else Color.LightGray.copy(alpha = 0.4f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredOrders.isEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(56.dp), tint = RoyalEmerald.copy(alpha = 0.3f))
                    Text("No matching orders found", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                // 2-Column Grid Layout for compact 2-order visibility
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredOrders, key = { it.id }) { order ->
                        AdminOrderGridCard(
                            order = order,
                            onClick = { selectedOrderForDetail = order }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminOrderGridCard(
    order: com.example.domain.model.Order,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    var isUpdatingStatus by remember { mutableStateOf(false) }

    val friendlyStatus = when(order.status) {
        OrderStatus.PENDING -> "Pending"
        OrderStatus.OUT_FOR_DELIVERY -> "Dispatched"
        OrderStatus.DELIVERED -> "Delivered"
        OrderStatus.CANCELLED -> "Cancelled"
        OrderStatus.RETURN_REQUESTED -> "Return Req."
        OrderStatus.RETURN_ACCEPTED -> "Ret. Acc."
        OrderStatus.RETURNED -> "Returned"
    }

    val isDark = AppState.isDarkMode
    val sdf = remember { java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.ENGLISH) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF333333) else Color(0xFFE5E7EB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: #ID + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${order.id.takeLast(6).uppercase()}",
                    fontWeight = FontWeight.Black,
                    color = DeepGold,
                    fontSize = 13.5.sp
                )
                Surface(
                    color = when(order.status) {
                        OrderStatus.PENDING -> if (isDark) DeepGold.copy(alpha = 0.25f) else Color(0xFFFFF4E5)
                        OrderStatus.OUT_FOR_DELIVERY -> (if (isDark) Color(0xFF34D399) else RoyalEmerald).copy(alpha = 0.2f)
                        OrderStatus.CANCELLED -> Color.Red.copy(alpha = 0.15f)
                        OrderStatus.RETURN_REQUESTED -> DeepGold.copy(alpha = 0.2f)
                        OrderStatus.RETURN_ACCEPTED -> DeepGold.copy(alpha = 0.3f)
                        else -> (if (isDark) Color(0xFF34D399) else RoyalEmerald).copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = friendlyStatus,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        color = when(order.status) {
                            OrderStatus.PENDING -> DeepGold
                            OrderStatus.CANCELLED -> Color.Red
                            OrderStatus.RETURN_REQUESTED -> DeepGold
                            OrderStatus.RETURN_ACCEPTED -> DeepGold
                            else -> if (isDark) Color(0xFF34D399) else RoyalEmerald
                        }
                    )
                }
            }

            // Time
            Text(
                text = sdf.format(java.util.Date(order.createdAt)),
                fontSize = 10.5.sp,
                color = Color.Gray
            )

            // Customer Name & Phone Call Quick Action Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.customerName.ifBlank { "Customer" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                // Call Icon Button on Card
                if (order.customerPhone.isNotBlank()) {
                    IconButton(
                        onClick = {
                            val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                data = android.net.Uri.parse("tel:${order.customerPhone}")
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .size(26.dp)
                            .background(
                                (if (isDark) Color(0xFF34D399) else RoyalEmerald).copy(alpha = 0.12f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Call,
                            contentDescription = "Call",
                            tint = if (isDark) Color(0xFF34D399) else RoyalEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Address summary snippet
            val addressSnippet = listOfNotNull(order.addressHouseNo.takeIf { it.isNotBlank() }, order.addressLandmark.takeIf { it.isNotBlank() }).joinToString(", ")
            if (addressSnippet.isNotBlank()) {
                Text(
                    text = "📍 $addressSnippet",
                    fontSize = 10.5.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            // Items Count Snippet
            val itemsCount = order.items.sumOf { it.quantity }
            Text(
                text = "$itemsCount item(s) • ${order.items.firstOrNull()?.productName ?: "Items"}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            // Bill Amount (High Contrast in both Dark and Light mode)
            Surface(
                color = if (isDark) Color(0xFF242424) else Color(0xFFF8FAFC),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.5.dp, if (isDark) Color(0xFF3A3A3A) else Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Bill", fontSize = 10.5.sp, color = Color.Gray)
                    Text(
                        "₹${order.totalAmount.toInt()}",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = if (isDark) Color(0xFF34D399) else RoyalEmerald
                    )
                }
            }

            // Action Button (Taller and clean)
            if (order.status == OrderStatus.PENDING) {
                Button(
                    onClick = {
                        isUpdatingStatus = true
                        AppState.adminDispatchOrderWithDriver(order.id) {
                            isUpdatingStatus = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF34D399) else RoyalEmerald),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(34.dp),
                    contentPadding = PaddingValues(0.dp),
                    enabled = !isUpdatingStatus
                ) {
                    if (isUpdatingStatus) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp), strokeWidth = 1.5.dp)
                    } else {
                        Text("Dispatch", fontSize = 11.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            } else if (order.status == OrderStatus.OUT_FOR_DELIVERY) {
                Button(
                    onClick = {
                        isUpdatingStatus = true
                        AppState.updateOrderStatus(order.id, OrderStatus.DELIVERED) {
                            isUpdatingStatus = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepGold),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(34.dp),
                    contentPadding = PaddingValues(0.dp),
                    enabled = !isUpdatingStatus
                ) {
                    if (isUpdatingStatus) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp), strokeWidth = 1.5.dp)
                    } else {
                        Text("Mark Delivered", fontSize = 11.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                OutlinedButton(
                    onClick = onClick,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(32.dp),
                    contentPadding = PaddingValues(0.dp),
                    border = BorderStroke(0.5.dp, if (isDark) Color(0xFF4B5563) else Color(0xFFD1D5DB))
                ) {
                    Text("View Details", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrderDetailModal(
    order: com.example.domain.model.Order,
    onDismiss: () -> Unit,
    onViewMap: (Order) -> Unit
) {
    val context = LocalContext.current
    val isDark = AppState.isDarkMode
    var isUpdatingStatus by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Order #${order.id.takeLast(6).uppercase()}",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val sdf = remember { java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.ENGLISH) }
                    Text(
                        text = sdf.format(java.util.Date(order.createdAt)),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Surface(
                    color = when(order.status) {
                        OrderStatus.PENDING -> if (isDark) DeepGold.copy(alpha = 0.25f) else Color(0xFFFFF4E5)
                        OrderStatus.OUT_FOR_DELIVERY -> (if (isDark) Color(0xFF34D399) else RoyalEmerald).copy(alpha = 0.2f)
                        OrderStatus.CANCELLED -> Color.Red.copy(alpha = 0.15f)
                        OrderStatus.RETURN_REQUESTED -> DeepGold.copy(alpha = 0.2f)
                        OrderStatus.RETURN_ACCEPTED -> DeepGold.copy(alpha = 0.3f)
                        else -> (if (isDark) Color(0xFF34D399) else RoyalEmerald).copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = order.status.name.replace("_", " "),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when(order.status) {
                            OrderStatus.PENDING -> DeepGold
                            OrderStatus.CANCELLED -> Color.Red
                            OrderStatus.RETURN_REQUESTED -> DeepGold
                            OrderStatus.RETURN_ACCEPTED -> DeepGold
                            else -> if (isDark) Color(0xFF34D399) else RoyalEmerald
                        }
                    )
                }
            }

            HorizontalDivider(color = if (isDark) Color(0xFF333333) else Color(0xFFEEEEEE))

            // Customer Info & Map
            val displayAddress = listOfNotNull(
                order.addressHouseNo.takeIf { it.isNotBlank() },
                order.addressLandmark.takeIf { it.isNotBlank() }
            ).joinToString(", ").ifBlank { "No address provided" }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(order.customerName.ifBlank { "Customer" }, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(order.customerPhone.ifBlank { "No phone number" }, fontSize = 13.sp, color = Color.Gray)
                }

                if (order.customerPhone.isNotBlank()) {
                    Button(
                        onClick = {
                            val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                data = android.net.Uri.parse("tel:${order.customerPhone}")
                            }
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF34D399) else RoyalEmerald),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Call, null, modifier = Modifier.size(14.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Call", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.Top, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (isDark) Color(0xFF34D399) else RoyalEmerald)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = displayAddress, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), lineHeight = 17.sp)
                }
                OutlinedButton(
                    onClick = { 
                        val currentOrder = order
                        onDismiss()
                        onViewMap(currentOrder) 
                    },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF34D399) else RoyalEmerald)
                ) {
                    Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(14.dp), tint = if (isDark) Color(0xFF34D399) else RoyalEmerald)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Map", fontSize = 12.sp, color = if (isDark) Color(0xFF34D399) else RoyalEmerald, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = if (isDark) Color(0xFF333333) else Color(0xFFEEEEEE))

            // Items Breakdown
            Text("ORDER ITEMS (${order.items.size})", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Gray, letterSpacing = 1.sp)
            order.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.productName, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("Size: ${item.selectedSize}", fontSize = 11.5.sp, color = if (isDark) Color(0xFF34D399) else RoyalEmerald, fontWeight = FontWeight.Bold)
                    }
                    Text("Qty: ${item.quantity}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 8.dp))
                    Text("₹${(item.priceAtPurchase * item.quantity).toInt()}", fontWeight = FontWeight.Black, fontSize = 13.5.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            // Total Bill Container (High Contrast)
            Surface(
                color = if (isDark) Color(0xFF242424) else Color(0xFFF8FAFC),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF3A3A3A) else Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Bill Amount", fontSize = 11.sp, color = Color.Gray)
                        Text("₹${order.totalAmount.toInt()}", fontWeight = FontWeight.Black, fontSize = 22.sp, color = if (isDark) Color(0xFF34D399) else RoyalEmerald)
                    }
                    if (order.status == OrderStatus.PENDING) {
                        Button(
                            onClick = { 
                                isUpdatingStatus = true
                                AppState.adminDispatchOrderWithDriver(order.id) {
                                    isUpdatingStatus = false
                                    onDismiss()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalEmerald),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isUpdatingStatus
                        ) {
                            Text("Dispatch Order", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else if (order.status == OrderStatus.OUT_FOR_DELIVERY) {
                        Button(
                            onClick = { 
                                isUpdatingStatus = true
                                AppState.updateOrderStatus(order.id, OrderStatus.DELIVERED) {
                                    isUpdatingStatus = false
                                    onDismiss()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepGold),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isUpdatingStatus
                        ) {
                            Text("Mark Delivered", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
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
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = RoyalEmerald,
                uncheckedColor = Color.Gray
            )
        )
    }
}

@Composable
fun FilterRadioButtonRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = RoyalEmerald,
                unselectedColor = Color.Gray
            )
        )
    }
}

private fun formatAdminProductName(name: String): String {
    val trimmed = name.trim()
    val lastSpaceIdx = trimmed.lastIndexOf(' ')
    if (lastSpaceIdx == -1) return trimmed
    return trimmed.substring(0, lastSpaceIdx) + "\u00A0" + trimmed.substring(lastSpaceIdx + 1)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminInventoryView(onAddProductClicked: () -> Unit, onEditProductClicked: (Product) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var appliedCategories by remember { mutableStateOf(emptySet<String>()) }
    var tempSelectedCategories by remember { mutableStateOf(emptySet<String>()) }
    var selectedSort by remember { mutableStateOf("Default") }
    var tempSort by remember { mutableStateOf("Default") }
    var stockFilter by remember { mutableStateOf("All") } // "All", "In Stock", "Out of Stock"
    var tempStockFilter by remember { mutableStateOf("All") }
    var selectedProductForDetail by remember { mutableStateOf<Product?>(null) }

    var showCategoryFilterSheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }

    if (selectedProductForDetail != null) {
        AdminProductDetailSheet(
            product = selectedProductForDetail!!,
            onDismiss = { selectedProductForDetail = null },
            onEditClicked = onEditProductClicked
        )
    }

    val filteredProducts = AppState.productsList.filter { product ->
        val cleanQuery = searchQuery.replace("\\s".toRegex(), "").lowercase()
        val matchesQuery = cleanQuery.isEmpty() ||
                product.nameEn.replace("\\s".toRegex(), "").lowercase().contains(cleanQuery) ||
                product.brand.replace("\\s".toRegex(), "").lowercase().contains(cleanQuery)
        
        // If search query is active, search across ALL products (ignoring category filter)
        val matchesCategory = cleanQuery.isNotEmpty() || appliedCategories.isEmpty() || appliedCategories.contains(product.categoryId)
        
        val matchesStock = cleanQuery.isNotEmpty() || when (stockFilter) {
            "In Stock" -> product.variants.any { it.stockQuantity > 0 }
            "Out of Stock" -> product.variants.all { it.stockQuantity <= 0 }
            else -> true
        }
        
        matchesQuery && matchesCategory && matchesStock
    }.let { list ->
        list.sortedWith(
            compareByDescending<Product> { prod ->
                prod.variants.maxOfOrNull { it.weight.toDoubleOrNull() ?: 0.0 } ?: 0.0
            }.thenBy { prod ->
                when (selectedSort) {
                    "Price: Low to High" -> {
                        val maxWeightVariant = prod.variants.maxByOrNull { it.weight.toDoubleOrNull() ?: 0.0 }
                        maxWeightVariant?.currentPrice ?: 0.0
                    }
                    "Price: High to Low" -> {
                        val maxWeightVariant = prod.variants.maxByOrNull { it.weight.toDoubleOrNull() ?: 0.0 }
                        -(maxWeightVariant?.currentPrice ?: 0.0)
                    }
                    "What's New" -> -prod.dateCreated.toDouble()
                    else -> 0.0
                }
            }
        )
    }

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
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)) {
            // Search Bar and "+ New" button in ONE single compact row (matches Live Orders height)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = null,
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = if (AppState.isDarkMode) Color(0xFF34D399) else RoyalEmerald, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 12.5.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = if (AppState.isDarkMode) Color(0xFF34D399) else RoyalEmerald,
                        unfocusedBorderColor = if (AppState.isDarkMode) Color(0xFF3A3A3A) else Color.LightGray
                    )
                )

                Button(
                    onClick = onAddProductClicked, 
                    colors = ButtonDefaults.buttonColors(containerColor = if (AppState.isDarkMode) Color(0xFF34D399) else RoyalEmerald),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier.height(46.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("New", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Buttons Row (Compact 34dp height)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Filter Button
                val catText = if (appliedCategories.isEmpty()) "Category" else "Category (${appliedCategories.size})"
                Button(
                    onClick = {
                        tempSelectedCategories = appliedCategories
                        showCategoryFilterSheet = true
                    },
                    modifier = Modifier.weight(1f).height(34.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (appliedCategories.isNotEmpty()) (if (AppState.isDarkMode) Color(0xFF34D399) else RoyalEmerald) else (if (AppState.isDarkMode) Color(0xFF242424) else MaterialTheme.colorScheme.surfaceVariant),
                        contentColor = if (appliedCategories.isNotEmpty()) Color.White else MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(catText, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                }

                // Sort & Stock Filter Button
                val isSortActive = selectedSort != "Default" || stockFilter != "All"
                val sortStockText = when {
                    selectedSort != "Default" && stockFilter != "All" -> "Filters (2)"
                    selectedSort != "Default" -> "Sort: ${selectedSort.replace("Price: ", "")}"
                    stockFilter != "All" -> "Stock: $stockFilter"
                    else -> "Sort & Stock"
                }
                Button(
                    onClick = {
                        tempSort = selectedSort
                        tempStockFilter = stockFilter
                        showSortSheet = true
                    },
                    modifier = Modifier.weight(1f).height(34.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSortActive) (if (AppState.isDarkMode) Color(0xFF34D399) else RoyalEmerald) else (if (AppState.isDarkMode) Color(0xFF242424) else MaterialTheme.colorScheme.surfaceVariant),
                        contentColor = if (isSortActive) Color.White else MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SwapVert, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(sortStockText, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            if (filteredProducts.isEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Inventory2, null, modifier = Modifier.size(56.dp), tint = RoyalEmerald.copy(alpha = 0.3f))
                    Text("No items found", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                // 2-Column Grid Layout for Products in Inventory
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        AdminInventoryGridCard(
                            product = product,
                            onClick = { selectedProductForDetail = product },
                            onEditClicked = { onEditProductClicked(product) }
                        )
                    }
                }
            }
        }
    }

    // --- Bottom Sheets ---

    if (showCategoryFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCategoryFilterSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
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
                    fontSize = 18.sp,
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

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showCategoryFilterSheet = false },
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            appliedCategories = tempSelectedCategories
                            showCategoryFilterSheet = false
                        },
                        modifier = Modifier.weight(1f).height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (AppState.isDarkMode) Color(0xFF34D399) else RoyalEmerald),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Apply Filter", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showSortSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSortSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
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
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = "SORT BY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                FilterRadioButtonRow(
                    label = "Default",
                    selected = tempSort == "Default",
                    onClick = { tempSort = "Default" }
                )
                FilterRadioButtonRow(
                    label = "Price: Low to High",
                    selected = tempSort == "Price: Low to High",
                    onClick = { tempSort = "Price: Low to High" }
                )
                FilterRadioButtonRow(
                    label = "Price: High to Low",
                    selected = tempSort == "Price: High to Low",
                    onClick = { tempSort = "Price: High to Low" }
                )
                FilterRadioButtonRow(
                    label = "What's New",
                    selected = tempSort == "What's New",
                    onClick = { tempSort = "What's New" }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), thickness = 0.5.dp, color = Color.LightGray)

                Text(
                    text = "STOCK STATUS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                FilterRadioButtonRow(
                    label = "All Items",
                    selected = tempStockFilter == "All",
                    onClick = { tempStockFilter = "All" }
                )
                FilterRadioButtonRow(
                    label = "In Stock",
                    selected = tempStockFilter == "In Stock",
                    onClick = { tempStockFilter = "In Stock" }
                )
                FilterRadioButtonRow(
                    label = "Out of Stock",
                    selected = tempStockFilter == "Out of Stock",
                    onClick = { tempStockFilter = "Out of Stock" }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showSortSheet = false },
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            selectedSort = tempSort
                            stockFilter = tempStockFilter
                            showSortSheet = false
                        },
                        modifier = Modifier.weight(1f).height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (AppState.isDarkMode) Color(0xFF34D399) else RoyalEmerald),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Apply", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminInventoryGridCard(
    product: Product,
    onClick: () -> Unit,
    onEditClicked: () -> Unit
) {
    val isDark = AppState.isDarkMode
    val isAllOutOfStock = product.variants.isEmpty() || product.variants.all { it.stockQuantity <= 0 }
    val isLowStock = !isAllOutOfStock && product.variants.any { it.stockQuantity in 1..4 }
    val minPrice = product.variants.minOfOrNull { it.currentPrice } ?: 0.0

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF2E2E2E) else Color(0xFFE5E7EB))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Product Image Box (Taller 600x400 aspect ratio container)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp)
                    .background(if (isDark) Color(0xFF1E1E1E) else Color(0xFFF3F4F6))
            ) {
                AsyncImage(
                    model = product.imageUrls.getOrNull(product.thumbnailIndex) ?: (product.imageUrls.firstOrNull() ?: ""),
                    contentDescription = product.nameEn,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Stock / Visibility Badge
                Surface(
                    color = when {
                        !product.isEnabled -> Color.Red.copy(alpha = 0.85f)
                        isAllOutOfStock -> Color.Red.copy(alpha = 0.85f)
                        isLowStock -> Color(0xFFF59E0B).copy(alpha = 0.9f)
                        else -> (if (isDark) Color(0xFF059669) else RoyalEmerald).copy(alpha = 0.9f)
                    },
                    shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 8.dp, topEnd = 0.dp, bottomEnd = 0.dp),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = when {
                            !product.isEnabled -> "Hidden"
                            isAllOutOfStock -> "Out of Stock"
                            isLowStock -> "Low Stock"
                            else -> "In Stock"
                        },
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Card Body (Spacious & Clean)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Brand Name
                Text(
                    text = product.brand.ifBlank { "G-STORE" },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isDark) Color(0xFF34D399) else RoyalEmerald,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                // Product Title (Max 2 lines, orphan-free)
                Text(
                    text = formatAdminProductName(product.nameEn),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    lineHeight = 16.sp,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.heightIn(min = 34.dp)
                )

                // Bottom Row: Price & Sizes summary + Compact sleek pencil icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (minPrice > 0) "₹${minPrice.toInt()}" else "₹0",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${product.variants.size} size${if (product.variants.size == 1) "" else "s"}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                    }

                    // Compact, non-cluttered pencil edit icon
                    IconButton(
                        onClick = onEditClicked,
                        modifier = Modifier
                            .size(26.dp)
                            .background(
                                (if (isDark) Color(0xFF34D399) else RoyalEmerald).copy(alpha = 0.12f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = if (isDark) Color(0xFF34D399) else RoyalEmerald,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductDetailSheet(
    product: Product,
    onDismiss: () -> Unit,
    onEditClicked: (Product) -> Unit
) {
    var showAddVariantDialog by remember { mutableStateOf(false) }
    var variantToEdit by remember { mutableStateOf<ProductVariant?>(null) }
    var variantToDelete by remember { mutableStateOf<ProductVariant?>(null) }
    var showProductDeleteConfirmation by remember { mutableStateOf(false) }
    var isListed by remember(product.id) { mutableStateOf(product.isEnabled) }
    var showHideConfirmation by remember { mutableStateOf(false) }
    val isDark = AppState.isDarkMode

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Image & Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = product.imageUrls.getOrNull(product.thumbnailIndex) ?: "",
                    contentDescription = null,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) Color(0xFF1E1E1E) else Color(0xFFF3F4F6)),
                    contentScale = ContentScale.Crop
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.nameEn,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 21.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Brand: ${product.brand.ifBlank { "G-STORE" }}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF34D399) else RoyalEmerald
                    )
                    val catName = AppState.categoriesList.find { it.id == product.categoryId }?.nameEn ?: product.categoryId
                    Text(
                        text = "Category: $catName",
                        fontSize = 11.5.sp,
                        color = Color.Gray
                    )
                }
            }

            HorizontalDivider(color = if (isDark) Color(0xFF333333) else Color(0xFFEEEEEE))

            // Listed Toggle & Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Listed Toggle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isListed) "Listed in Store" else "Hidden from Store",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isListed) (if (isDark) Color(0xFF34D399) else RoyalEmerald) else Color.Red,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Switch(
                        checked = isListed,
                        onCheckedChange = { checked ->
                            if (checked) {
                                isListed = true
                                AppState.adminUpdateProduct(product.copy(isEnabled = true))
                            } else {
                                showHideConfirmation = true
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = if (isDark) Color(0xFF34D399) else RoyalEmerald
                        ),
                        modifier = Modifier.scale(0.85f)
                    )
                }

                // Edit & Delete Action Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onDismiss(); onEditClicked(product) },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF34D399) else RoyalEmerald),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit Info", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    IconButton(
                        onClick = { showProductDeleteConfirmation = true },
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color.Red.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                    }
                }
            }

            if (showHideConfirmation) {
                AlertDialog(
                    onDismissRequest = { showHideConfirmation = false },
                    title = { Text("Hide Product?", fontWeight = FontWeight.Bold) },
                    text = { Text("Are you sure you want to hide \"${product.nameEn}\" from customers? It will no longer be visible on the customer screen.") },
                    confirmButton = {
                        TextButton(onClick = {
                            showHideConfirmation = false
                            isListed = false
                            AppState.adminUpdateProduct(product.copy(isEnabled = false))
                        }) {
                            Text("Hide", color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showHideConfirmation = false }) {
                            Text("Cancel", color = Color.Gray)
                        }
                    }
                )
            }

            if (showProductDeleteConfirmation) {
                AlertDialog(
                    onDismissRequest = { showProductDeleteConfirmation = false },
                    title = { Text("Delete Product", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                    text = { Text("Permanently delete \"${product.nameEn}\" and all its variants? This cannot be undone.") },
                    confirmButton = {
                        TextButton(onClick = {
                            showProductDeleteConfirmation = false
                            AppState.adminDeleteProduct(product.id)
                            onDismiss()
                        }) {
                            Text("Delete Permanently", color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showProductDeleteConfirmation = false }) {
                            Text("Cancel", color = Color.Gray)
                        }
                    }
                )
            }

            HorizontalDivider(color = if (isDark) Color(0xFF333333) else Color(0xFFEEEEEE))

            // Variants Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VARIANTS & PRICING (${product.variants.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
                Button(
                    onClick = { showAddVariantDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = (if (isDark) Color(0xFF34D399) else RoyalEmerald).copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp), tint = if (isDark) Color(0xFF34D399) else RoyalEmerald)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Variant", fontSize = 11.sp, color = if (isDark) Color(0xFF34D399) else RoyalEmerald, fontWeight = FontWeight.Bold)
                }
            }

            // Variants List Cards
            product.variants.sortedByDescending { it.weight.toDoubleOrNull() ?: 0.0 }.forEach { variant ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF242424) else Color(0xFFF8FAFC)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.5.dp, if (isDark) Color(0xFF3A3A3A) else Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${variant.weight} ${variant.unit}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "₹${variant.currentPrice.toInt()}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = if (isDark) Color(0xFF34D399) else RoyalEmerald
                                )
                                if (variant.mrp > variant.currentPrice) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "MRP ₹${variant.mrp.toInt()}",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            if (variant.stockQuantity < 5) {
                                Text(
                                    text = if (variant.stockQuantity <= 0) "⚠️ Out of Stock" else "⚠️ Low Stock: ${variant.stockQuantity} left",
                                    color = Color.Red,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = "Stock: ${variant.stockQuantity} units available",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = { variantToEdit = variant },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Edit, "Edit", tint = if (isDark) Color(0xFF34D399) else RoyalEmerald, modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = { variantToDelete = variant },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Delete, "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
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
            title = { Text("Delete Variant", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete the ${variantToDelete!!.weight} ${variantToDelete!!.unit} variant?") },
            confirmButton = {
                TextButton(onClick = {
                    AppState.adminDeleteProductVariant(product.id, variantToDelete!!.id)
                    variantToDelete = null
                }) { Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold) }
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
    var unit by remember { mutableStateOf(existingVariant?.unit ?: "Kg") }
    var price by remember { mutableStateOf(existingVariant?.currentPrice?.toInt()?.toString() ?: "") }
    var mrp by remember { mutableStateOf(existingVariant?.mrp?.toInt()?.toString() ?: "") }
    var stock by remember { mutableStateOf(existingVariant?.stockQuantity?.toString() ?: "") }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(if (existingVariant == null) "Add Variant" else "Edit Variant", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { 
                        weight = it 
                        validationError = null
                    },
                    label = { Text("Size / Quantity (e.g. 5, 10, 500)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = validationError != null && weight.trim().isEmpty(),
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

                // Custom Unit Input Field
                OutlinedTextField(
                    value = unit,
                    onValueChange = { 
                        unit = it 
                        validationError = null
                    },
                    label = { Text("Unit of Measurement") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = validationError != null && (unit.trim().isEmpty() || !unit.trim().all { it.isLetter() }),
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

                // Quick selector chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val quickUnits = listOf("Kg", "g", "Ltr", "ml", "Pcs")
                    quickUnits.forEach { qu ->
                        val isSelected = unit.trim().equals(qu, ignoreCase = true)
                        AssistChip(
                            onClick = { 
                                unit = qu
                                validationError = null
                            },
                            label = { Text(qu, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (isSelected) RoyalEmerald.copy(alpha = 0.15f) else Color.Transparent,
                                labelColor = if (isSelected) RoyalEmerald else Color.Gray
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) RoyalEmerald else Color.LightGray.copy(alpha = 0.5f)
                            )
                        )
                    }
                }

                if (validationError != null) {
                    Text(
                        text = validationError!!,
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

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
                    val trimmedWeight = weight.trim()
                    val trimmedUnit = unit.trim()
                    
                    if (trimmedWeight.isEmpty()) {
                        validationError = "Please enter a size or weight value"
                        return@Button
                    }
                    if (trimmedUnit.isEmpty()) {
                        validationError = "Please enter or select a unit"
                        return@Button
                    }
                    val cleanUnit = trimmedUnit.lowercase()
                    val isApprovedUnit = cleanUnit in setOf(
                        "kg", "kgs", "g", "grams", "ltr", "ltrs", "liters", "ml", "pcs", "pieces", "packet", "packets", "pkt", "pkts", "box", "boxes"
                    )
                    if (!isApprovedUnit) {
                        validationError = "Invalid unit. Please use standard units like Kg, g, Ltr, ml, Pcs, Packet, Box."
                        return@Button
                    }

                    val variant = ProductVariant(
                        id = existingVariant?.id ?: "v_${System.currentTimeMillis()}",
                        weight = trimmedWeight,
                        unit = trimmedUnit,
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

@Composable
fun AdminDeliveryManagementView() {
    val isDark = AppState.isDarkMode
    val context = LocalContext.current
    val allOrders = AppState.ordersList

    var filterStatus by remember { mutableStateOf("ALL") } // "ALL", "OUT_FOR_DELIVERY", "PENDING", "DELIVERED", "ISSUES"
    var selectedOrderForDriver by remember { mutableStateOf<Order?>(null) }
    var selectedOrderForEdit by remember { mutableStateOf<Order?>(null) }
    var selectedOrderForMap by remember { mutableStateOf<Order?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val fleetRiders = listOf(
        Pair("Raju (G-Store Rider)", "+919999900001"),
        Pair("Suresh (Express Rider)", "+919999900002"),
        Pair("Kiran (Local Delivery)", "+919999900003")
    )

    val outForDeliveryOrders = allOrders.filter { it.status == OrderStatus.OUT_FOR_DELIVERY }
    val pendingAssignmentOrders = allOrders.filter { it.status == OrderStatus.PENDING }
    val deliveredOrders = allOrders.filter { it.status == OrderStatus.DELIVERED }
    val issueOrders = allOrders.filter { it.issueReported.isNotBlank() && it.status != OrderStatus.DELIVERED }

    val totalCashCollected = deliveredOrders.sumOf { it.totalAmount }
    val remainingCashToSettle = (totalCashCollected - AppState.settledCashAmount).coerceAtLeast(0.0)

    val filteredOrders = allOrders.filter { order ->
        val matchesStatus = when (filterStatus) {
            "OUT_FOR_DELIVERY" -> order.status == OrderStatus.OUT_FOR_DELIVERY
            "PENDING" -> order.status == OrderStatus.PENDING
            "DELIVERED" -> order.status == OrderStatus.DELIVERED
            "ISSUES" -> order.issueReported.isNotBlank()
            else -> true
        }
        val query = searchQuery.trim().lowercase()
        val matchesSearch = query.isEmpty() ||
                order.customerName.lowercase().contains(query) ||
                order.customerPhone.contains(query) ||
                order.id.lowercase().contains(query) ||
                order.assignedDriverName.lowercase().contains(query)
        matchesStatus && matchesSearch
    }

    if (selectedOrderForDriver != null) {
        AdminAssignDriverModal(
            order = selectedOrderForDriver!!,
            isDark = isDark,
            onDismiss = { selectedOrderForDriver = null }
        )
    }

    if (selectedOrderForEdit != null) {
        AdminEditDeliveryModal(
            order = selectedOrderForEdit!!,
            isDark = isDark,
            onDismiss = { selectedOrderForEdit = null }
        )
    }

    if (selectedOrderForMap != null) {
        AdminOrderMapModal(
            order = selectedOrderForMap!!,
            onDismiss = { selectedOrderForMap = null }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Top Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Out For Delivery
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF333333) else Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("On Route", fontSize = 10.5.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                        Text("${outForDeliveryOrders.size}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (isDark) Color(0xFF34D399) else RoyalEmerald)
                    }
                }

                // Unassigned
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF333333) else Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Unassigned", fontSize = 10.5.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                        Text("${pendingAssignmentOrders.size}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (pendingAssignmentOrders.isNotEmpty()) DeepGold else Color.Gray)
                    }
                }

                // Cash in Hand
                Card(
                    modifier = Modifier.weight(1.2f),
                    colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF333333) else Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("COD to Settle", fontSize = 10.5.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                        Text("₹${remainingCashToSettle.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = DeepGold)
                    }
                }
            }
        }

        // 2. Delivery Boys Fleet & Performance Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1A1A1A) else Color.White),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF333333) else Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🛵 Delivery Partners & Fleet Work", fontWeight = FontWeight.Black, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = RoyalEmerald.copy(alpha = 0.12f)
                        ) {
                            Text("3 ONLINE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RoyalEmerald, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    fleetRiders.forEach { (riderName, riderPhone) ->
                        val riderDelivered = deliveredOrders.filter { it.assignedDriverPhone == riderPhone || it.assignedDriverName == riderName }
                        val riderActive = outForDeliveryOrders.filter { it.assignedDriverPhone == riderPhone || it.assignedDriverName == riderName }
                        val riderCash = riderDelivered.sumOf { it.totalAmount }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDark) Color(0xFF262626) else Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF383838) else Color(0xFFEAEAEA)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(RoyalEmerald.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🛵", fontSize = 16.sp)
                                }

                                Spacer(Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(riderName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text(
                                        "${riderDelivered.size} delivered today • ${riderActive.size} active • ₹${riderCash.toInt()} collected",
                                        fontSize = 11.sp,
                                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                    )
                                }

                                // Quick Call Rider
                                IconButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$riderPhone"))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = "Call", tint = RoyalEmerald, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                placeholder = { Text("Search deliveries by customer, order ID, or rider...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Gray) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RoyalEmerald,
                    unfocusedBorderColor = if (isDark) Color(0xFF333333) else Color(0xFFE2E8F0)
                )
            )
        }

        // 4. Filter Chips Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    Pair("ALL", "All (${allOrders.size})"),
                    Pair("OUT_FOR_DELIVERY", "Out for Delivery (${outForDeliveryOrders.size})"),
                    Pair("PENDING", "Unassigned (${pendingAssignmentOrders.size})"),
                    Pair("DELIVERED", "Delivered (${deliveredOrders.size})"),
                    Pair("ISSUES", "⚠️ Issues (${issueOrders.size})")
                ).forEach { (key, label) ->
                    val isSel = filterStatus == key
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSel) RoyalEmerald else (if (isDark) Color(0xFF262626) else Color(0xFFF1F5F9)),
                        border = BorderStroke(1.dp, if (isSel) RoyalEmerald else Color.Transparent),
                        modifier = Modifier.clickable { filterStatus = key }
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) Color.White else (if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 5. Deliveries List
        if (filteredOrders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.DeliveryDining, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No deliveries match this filter", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            items(filteredOrders, key = { it.id }) { order ->
                AdminDeliveryOrderCard(
                    order = order,
                    isDark = isDark,
                    onAssignDriver = { selectedOrderForDriver = order },
                    onEditDelivery = { selectedOrderForEdit = order },
                    onViewMap = { selectedOrderForMap = order },
                    onCallCustomer = {
                        val phone = order.customerPhone.filter { it.isDigit() || it == '+' }
                        if (phone.isNotBlank()) {
                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                        }
                    },
                    onCallRider = {
                        val phone = order.assignedDriverPhone.filter { it.isDigit() || it == '+' }
                        if (phone.isNotBlank()) {
                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AdminDeliveryOrderCard(
    order: Order,
    isDark: Boolean,
    onAssignDriver: () -> Unit,
    onEditDelivery: () -> Unit,
    onViewMap: () -> Unit,
    onCallCustomer: () -> Unit,
    onCallRider: () -> Unit
) {
    val cardBg = if (isDark) Color(0xFF1E1E1E) else Color.White
    val cardBorder = if (isDark) Color(0xFF333333) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    val isDelivered = order.status == OrderStatus.DELIVERED
    val isOut = order.status == OrderStatus.OUT_FOR_DELIVERY
    val isAssigned = order.assignedDriverName.isNotBlank()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Order ID & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ORDER #${order.id.takeLast(6).uppercase()}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = textSecondary
                )

                val (badgeBg, badgeText, badgeColor) = when (order.status) {
                    OrderStatus.OUT_FOR_DELIVERY -> Triple(RoyalEmerald.copy(alpha = 0.15f), "OUT FOR DELIVERY", RoyalEmerald)
                    OrderStatus.DELIVERED -> Triple(Color(0xFF10B981).copy(alpha = 0.15f), "DELIVERED", Color(0xFF10B981))
                    OrderStatus.PENDING -> Triple(DeepGold.copy(alpha = 0.15f), "PENDING ASSIGNMENT", DeepGold)
                    else -> Triple(Color.Gray.copy(alpha = 0.15f), order.status.name, Color.Gray)
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeBg
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Issue Banner if present
            if (order.issueReported.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AlertRed.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, AlertRed.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠️", fontSize = 12.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Driver Issue: ${order.issueReported}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AlertRed,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Customer Name & Contact
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = RoyalEmerald, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(order.customerName.ifEmpty { "Customer" }, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textPrimary)
                Spacer(Modifier.width(6.dp))
                Text("(${order.customerPhone})", fontSize = 12.sp, color = textSecondary)

                Spacer(Modifier.weight(1f))

                // Call customer
                IconButton(onClick = onCallCustomer, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Phone, contentDescription = "Call Customer", tint = RoyalEmerald, modifier = Modifier.size(14.dp))
                }
            }

            // Address snippet
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = textSecondary, modifier = Modifier.size(14.dp).padding(top = 2.dp))
                Spacer(Modifier.width(6.dp))
                Column {
                    Text(order.addressHouseNo.ifEmpty { "Delivery Address" }, fontSize = 12.sp, color = textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (order.addressLandmark.isNotBlank()) {
                        Text("Landmark: ${order.addressLandmark}", fontSize = 11.sp, color = textSecondary)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Assigned Delivery Partner Card
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isAssigned) (if (isDark) Color(0xFF262626) else Color(0xFFF1F5F3)) else DeepGold.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, if (isAssigned) (if (isDark) Color(0xFF383838) else Color(0xFFE2E8F0)) else DeepGold.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Text("🛵", fontSize = 14.sp)
                        Spacer(Modifier.width(6.dp))
                        Column {
                            Text(
                                text = if (isAssigned) "Driver: ${order.assignedDriverName}" else "⚠️ No Delivery Partner Assigned",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAssigned) textPrimary else DeepGold
                            )
                            if (isAssigned && order.assignedDriverPhone.isNotBlank()) {
                                Text(order.assignedDriverPhone, fontSize = 10.5.sp, color = textSecondary)
                            }
                        }
                    }

                    if (isAssigned) {
                        IconButton(onClick = onCallRider, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Phone, contentDescription = "Call Rider", tint = RoyalEmerald, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Bill & Items Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${order.items.size} Items (Total: ₹${order.totalAmount.toInt()})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoyalEmerald)
                if (order.deliveryRemarks.isNotBlank()) {
                    Text(order.deliveryRemarks, fontSize = 10.5.sp, color = textSecondary, maxLines = 1)
                }
            }

            Spacer(Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Assign Partner Button
                Button(
                    onClick = onAssignDriver,
                    modifier = Modifier.weight(1.2f).height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF34D399) else RoyalEmerald),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (isAssigned) "Reassign" else "Assign Driver", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                // Edit Details Button
                OutlinedButton(
                    onClick = onEditDelivery,
                    modifier = Modifier.weight(1f).height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = textPrimary),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF404040) else Color(0xFFCBD5E1)),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Edit Info", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Map Button
                OutlinedButton(
                    onClick = onViewMap,
                    modifier = Modifier.weight(0.9f).height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = textPrimary),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF404040) else Color(0xFFCBD5E1)),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Map", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAssignDriverModal(
    order: Order,
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    val predefinedRiders = listOf(
        Triple("Raju (G-Store Rider)", "+919999900001", "Primary Hub Rider"),
        Triple("Suresh (Express Rider)", "+919999900002", "Express Quick-Commerce"),
        Triple("Kiran (Local Delivery)", "+919999900003", "Rajam Town & Rural")
    )

    var selectedRiderName by remember { mutableStateOf(order.assignedDriverName.ifEmpty { predefinedRiders[0].first }) }
    var selectedRiderPhone by remember { mutableStateOf(order.assignedDriverPhone.ifEmpty { predefinedRiders[0].second }) }
    var isCustomRider by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text("Assign Delivery Partner", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
            Spacer(Modifier.height(4.dp))
            Text("Order #${order.id.takeLast(6).uppercase()} • ${order.customerName} (${order.addressHouseNo})", fontSize = 13.sp, color = textSecondary)

            Spacer(Modifier.height(16.dp))

            Text("SELECT FROM FLEET", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textSecondary, letterSpacing = 0.5.sp)
            Spacer(Modifier.height(8.dp))

            predefinedRiders.forEach { (name, phone, role) ->
                val isSelected = !isCustomRider && selectedRiderName == name
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) RoyalEmerald.copy(alpha = 0.12f) else if (isDark) Color(0xFF262626) else Color(0xFFF8FAF9),
                    border = BorderStroke(1.dp, if (isSelected) RoyalEmerald else Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            isCustomRider = false
                            selectedRiderName = name
                            selectedRiderPhone = phone
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                isCustomRider = false
                                selectedRiderName = name
                                selectedRiderPhone = phone
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = RoyalEmerald)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textPrimary)
                            Text("$phone • $role", fontSize = 11.5.sp, color = textSecondary)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Custom Rider Option
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isCustomRider) RoyalEmerald.copy(alpha = 0.12f) else if (isDark) Color(0xFF262626) else Color(0xFFF8FAF9),
                border = BorderStroke(1.dp, if (isCustomRider) RoyalEmerald else Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isCustomRider = true }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isCustomRider,
                        onClick = { isCustomRider = true },
                        colors = RadioButtonDefaults.colors(selectedColor = RoyalEmerald)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Custom / Other Driver", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textPrimary)
                }
            }

            if (isCustomRider) {
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = selectedRiderName,
                    onValueChange = { selectedRiderName = it },
                    label = { Text("Driver Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = selectedRiderPhone,
                    onValueChange = { selectedRiderPhone = it },
                    label = { Text("Driver Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    AppState.adminDispatchOrderWithDriver(
                        orderId = order.id,
                        driverName = selectedRiderName.trim().ifEmpty { "Raju (G-Store Rider)" },
                        driverPhone = selectedRiderPhone.trim().ifEmpty { "+919999900001" }
                    ) {
                        android.widget.Toast.makeText(context, "✓ Delivery Partner Assigned to Order #${order.id.takeLast(6).uppercase()}!", android.widget.Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalEmerald)
            ) {
                Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Confirm Assignment & Dispatch", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEditDeliveryModal(
    order: Order,
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)

    var addressText by remember { mutableStateOf(order.addressHouseNo) }
    var landmarkText by remember { mutableStateOf(order.addressLandmark) }
    var driverNameText by remember { mutableStateOf(order.assignedDriverName) }
    var driverPhoneText by remember { mutableStateOf(order.assignedDriverPhone) }
    var deliveryFeeText by remember { mutableStateOf(order.deliveryFee.toInt().toString()) }
    var remarksText by remember { mutableStateOf(order.deliveryRemarks) }
    var selectedStatus by remember { mutableStateOf(order.status) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Edit Delivery Details", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                Text("#${order.id.takeLast(6).uppercase()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RoyalEmerald)
            }

            OutlinedTextField(
                value = addressText,
                onValueChange = { addressText = it },
                label = { Text("Delivery House / Street Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 3
            )

            OutlinedTextField(
                value = landmarkText,
                onValueChange = { landmarkText = it },
                label = { Text("Landmark") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = driverNameText,
                    onValueChange = { driverNameText = it },
                    label = { Text("Assigned Driver") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = driverPhoneText,
                    onValueChange = { driverPhoneText = it },
                    label = { Text("Driver Phone") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = deliveryFeeText,
                onValueChange = { deliveryFeeText = it.filter { c -> c.isDigit() } },
                label = { Text("Delivery Fee (₹)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = remarksText,
                onValueChange = { remarksText = it },
                label = { Text("Delivery Notes / Instructions") },
                placeholder = { Text("e.g. Ring bell twice, deliver before 7 PM") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 2
            )

            // Status Selector
            Text("ORDER STATUS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 0.5.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(OrderStatus.PENDING, OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED).forEach { st ->
                    val isSel = selectedStatus == st
                    val label = when (st) {
                        OrderStatus.PENDING -> "Pending"
                        OrderStatus.OUT_FOR_DELIVERY -> "Out for Delivery"
                        OrderStatus.DELIVERED -> "Delivered"
                        else -> st.name
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSel) RoyalEmerald.copy(alpha = 0.15f) else (if (isDark) Color(0xFF262626) else Color(0xFFEAEAEA)),
                        border = BorderStroke(1.dp, if (isSel) RoyalEmerald else Color.Transparent),
                        modifier = Modifier.weight(1f).clickable { selectedStatus = st }
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) RoyalEmerald else (if (isDark) Color.LightGray else Color.DarkGray),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = {
                    val parsedFee = deliveryFeeText.toDoubleOrNull() ?: order.deliveryFee
                    val updatedOrder = order.copy(
                        addressHouseNo = addressText.trim(),
                        addressLandmark = landmarkText.trim(),
                        assignedDriverName = driverNameText.trim(),
                        assignedDriverPhone = driverPhoneText.trim(),
                        deliveryFee = parsedFee,
                        totalAmount = (order.subtotal + parsedFee),
                        deliveryRemarks = remarksText.trim(),
                        status = selectedStatus
                    )
                    AppState.ioScope.launch {
                        AppState.orderRepository.saveOrder(updatedOrder)
                        if (selectedStatus != order.status) {
                            AppState.orderRepository.updateOrderStatus(updatedOrder.id, selectedStatus.name)
                        }
                    }
                    android.widget.Toast.makeText(context, "✓ Delivery Details Updated Successfully!", android.widget.Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalEmerald)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
            }
        }
    }
}

fun getCategoryFallbackImage(categoryId: String): String {
    return when (categoryId.lowercase()) {
        "c_rice" -> "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600&h=400&fit=crop"
        "c_oil" -> "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=600&h=400&fit=crop"
        "c_dal" -> "https://images.unsplash.com/photo-1546833999-b9f581a1996d?w=600&h=400&fit=crop"
        "c_dairy" -> "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=600&h=400&fit=crop"
        "c_spices" -> "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=600&h=400&fit=crop"
        "c_dryfruits" -> "https://images.unsplash.com/photo-1599599810769-bcde5a160d32?w=600&h=400&fit=crop"
        "c_snacks" -> "https://images.unsplash.com/photo-1621996346565-e3d5d6281699?w=600&h=400&fit=crop"
        "c_biscuits" -> "https://images.unsplash.com/photo-1558961363-fa8fdf82db35?w=600&h=400&fit=crop"
        "c_beverages" -> "https://images.unsplash.com/photo-1576092768241-dec231879fc3?w=600&h=400&fit=crop"
        "c_cleaning" -> "https://images.unsplash.com/photo-1583947215259-38e31be8751f?w=600&h=400&fit=crop"
        else -> "https://images.unsplash.com/photo-1542838132-92c53300491e?w=600&h=400&fit=crop"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductEditor(existingProduct: Product?, onDismiss: () -> Unit) {
    var nameEn by rememberSaveable { mutableStateOf(existingProduct?.nameEn ?: "") }
    var brand by rememberSaveable { mutableStateOf(existingProduct?.brand ?: "") }
    var descEn by rememberSaveable { mutableStateOf(existingProduct?.descriptionEn ?: "") }
    var selectedImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var currentImageUrl by rememberSaveable { mutableStateOf(existingProduct?.imageUrls?.firstOrNull() ?: "") }
    var isUploading by rememberSaveable { mutableStateOf(false) }
    var uploadError by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Initial Variant inputs for new items
    var initWeight by rememberSaveable { mutableStateOf("1") }
    var initUnit by rememberSaveable { mutableStateOf("Kg") }
    var initPrice by rememberSaveable { mutableStateOf("") }
    var initMrp by rememberSaveable { mutableStateOf("") }
    var initStock by rememberSaveable { mutableStateOf("50") }

    val categories = remember(AppState.categoriesList) {
        AppState.categoriesList.map { Pair(it.id, it.nameEn) }
    }
    val defaultCategoryId = remember(AppState.categoriesList) {
        AppState.categoriesList.firstOrNull()?.id ?: "c_rice"
    }
    var selectedCategoryId by rememberSaveable { mutableStateOf(existingProduct?.categoryId ?: defaultCategoryId) }
    var expanded by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(if (existingProduct == null) "Add New Product" else "Edit Product Info", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text("Product Name *") },
                    placeholder = { Text("e.g. Premium Basmati Rice") },
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

                // Category Selector Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = categories.find { it.first == selectedCategoryId }?.second ?: (AppState.categoriesList.firstOrNull()?.nameEn ?: "Select Category"),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.clickable { expanded = true }) },
                        modifier = Modifier.fillMaxWidth().clickable { expanded = true },
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
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        categories.forEach { (id, name) ->
                            DropdownMenuItem(
                                text = { Text(name, color = MaterialTheme.colorScheme.onSurface) },
                                onClick = {
                                    selectedCategoryId = id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand Name") },
                    placeholder = { Text("e.g. India Gate, Tata, Nestle") },
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
                    placeholder = { Text("Short product details...") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = RoyalEmerald,
                        unfocusedLabelColor = Color.Gray,
                        focusedBorderColor = RoyalEmerald,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                // Initial Variant Details (When adding new item)
                if (existingProduct == null) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.LightGray.copy(alpha = 0.5f))
                    Text("Initial Pack Size & Pricing", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RoyalEmerald)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = initWeight,
                            onValueChange = { initWeight = it },
                            label = { Text("Size (e.g. 1, 500)") },
                            modifier = Modifier.weight(1f),
                            textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedLabelColor = RoyalEmerald,
                                focusedBorderColor = RoyalEmerald
                            )
                        )
                        OutlinedTextField(
                            value = initUnit,
                            onValueChange = { initUnit = it },
                            label = { Text("Unit") },
                            modifier = Modifier.weight(1f),
                            textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedLabelColor = RoyalEmerald,
                                focusedBorderColor = RoyalEmerald
                            )
                        )
                    }

                    // Quick selector chips for units
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val quickUnits = listOf("Kg", "g", "Ltr", "ml", "Pcs")
                        quickUnits.forEach { qu ->
                            val isSelected = initUnit.trim().equals(qu, ignoreCase = true)
                            AssistChip(
                                onClick = { initUnit = qu },
                                label = { Text(qu, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (isSelected) RoyalEmerald.copy(alpha = 0.15f) else Color.Transparent,
                                    labelColor = if (isSelected) RoyalEmerald else Color.Gray
                                ),
                                border = BorderStroke(1.dp, if (isSelected) RoyalEmerald else Color.LightGray.copy(alpha = 0.5f))
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = initPrice,
                            onValueChange = { initPrice = it },
                            label = { Text("Sale Price (₹) *") },
                            placeholder = { Text("100") },
                            modifier = Modifier.weight(1f),
                            textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedLabelColor = RoyalEmerald,
                                focusedBorderColor = RoyalEmerald
                            )
                        )
                        OutlinedTextField(
                            value = initMrp,
                            onValueChange = { initMrp = it },
                            label = { Text("MRP (₹)") },
                            placeholder = { Text("120") },
                            modifier = Modifier.weight(1f),
                            textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedLabelColor = RoyalEmerald,
                                focusedBorderColor = RoyalEmerald
                            )
                        )
                    }

                    OutlinedTextField(
                        value = initStock,
                        onValueChange = { initStock = it },
                        label = { Text("Initial Stock Quantity") },
                        placeholder = { Text("50") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = RoyalEmerald,
                            focusedBorderColor = RoyalEmerald
                        )
                    )
                }
                
                Text("Product Image (Optional)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    val displayImage = when {
                        selectedImageUri != null -> selectedImageUri
                        currentImageUrl.isNotBlank() -> currentImageUrl
                        else -> getCategoryFallbackImage(selectedCategoryId)
                    }
                    AsyncImage(
                        model = displayImage,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    if (selectedImageUri != null || currentImageUrl.isNotBlank()) {
                        IconButton(
                            onClick = { selectedImageUri = null; currentImageUrl = "" },
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(Color.White.copy(alpha = 0.8f), CircleShape).size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Surface(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text("Tap to upload custom image", color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
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
                                    val tempFile = withContext(Dispatchers.IO) {
                                        getFileFromUri(context, selectedImageUri!!)
                                    }
                                    if (tempFile == null) {
                                        uploadError = "Failed to open selected image."
                                        isUploading = false
                                        return@launch
                                    }
                                    val downloadUrl = withContext(Dispatchers.IO) {
                                        try {
                                            CloudinaryUploader.upload(tempFile)
                                        } catch (cErr: Exception) {
                                            android.util.Log.w("ProductEditor", "Cloudinary upload failed, falling back to AWS S3 Storage...", cErr)
                                            com.example.data.repository.AwsStorageUploader.uploadProductImage(tempFile)
                                        }
                                    }
                                    withContext(Dispatchers.IO) {
                                        try { tempFile.delete() } catch (_: Exception) {}
                                    }
                                    saveProductWithImage(
                                        context = context,
                                        imageUrl = downloadUrl,
                                        nameEn = nameEn,
                                        brand = brand,
                                        descEn = descEn,
                                        categoryId = selectedCategoryId,
                                        initWeight = initWeight,
                                        initUnit = initUnit,
                                        initPrice = initPrice,
                                        initMrp = initMrp,
                                        initStock = initStock,
                                        existingProduct = existingProduct,
                                        onDismiss = onDismiss
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    uploadError = if (com.example.util.NetworkUtils.isOfflineError(e)) {
                                        "Device is offline. Please check your internet connection."
                                    } else {
                                        "Image upload failed: ${e.message ?: e.javaClass.simpleName}"
                                    }
                                } finally {
                                    isUploading = false
                                }
                            }
                        } else {
                            saveProductWithImage(
                                context = context,
                                imageUrl = currentImageUrl,
                                nameEn = nameEn,
                                brand = brand,
                                descEn = descEn,
                                categoryId = selectedCategoryId,
                                initWeight = initWeight,
                                initUnit = initUnit,
                                initPrice = initPrice,
                                initMrp = initMrp,
                                initStock = initStock,
                                existingProduct = existingProduct,
                                onDismiss = onDismiss
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (AppState.isDarkMode) Color(0xFF34D399) else RoyalEmerald),
                    enabled = !isUploading && nameEn.isNotBlank()
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Saving...", color = Color.White)
                    } else Text(if (existingProduct == null) "Create Product" else "Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
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
    context: Context,
    imageUrl: String,
    nameEn: String,
    brand: String,
    descEn: String,
    categoryId: String,
    initWeight: String,
    initUnit: String,
    initPrice: String,
    initMrp: String,
    initStock: String,
    existingProduct: Product?,
    onDismiss: () -> Unit
) {
    if (existingProduct == null) {
        val finalImageUrl = if (imageUrl.isNotBlank()) imageUrl else getCategoryFallbackImage(categoryId)
        val pPrice = initPrice.toDoubleOrNull() ?: 100.0
        val pMrp = initMrp.toDoubleOrNull() ?: pPrice
        val pStock = initStock.toIntOrNull() ?: 50
        val pWeight = initWeight.ifBlank { "1" }
        val pUnit = initUnit.ifBlank { "Kg" }

        val initialVariant = ProductVariant(
            id = "v_${System.currentTimeMillis()}",
            weight = pWeight,
            unit = pUnit,
            currentPrice = pPrice,
            mrp = pMrp,
            stockQuantity = pStock
        )

        AppState.adminAddProduct(
            nameEn = nameEn.trim(),
            brand = brand.trim().ifBlank { "G-STORE" },
            descEn = descEn.trim().ifBlank { "High quality ${nameEn.trim()} available at G-STORE." },
            imageUrls = listOf(finalImageUrl),
            variants = listOf(initialVariant),
            categoryId = categoryId
        )
        android.widget.Toast.makeText(context, "✓ Product '${nameEn.trim()}' added to inventory successfully!", android.widget.Toast.LENGTH_SHORT).show()
    } else {
        val updatedProduct = existingProduct.copy(
            nameEn = nameEn.trim(),
            brand = brand.trim().ifBlank { existingProduct.brand },
            descriptionEn = descEn.trim(),
            categoryId = categoryId,
            imageUrls = if (imageUrl.isNotBlank()) listOf(imageUrl) else existingProduct.imageUrls,
            lastUpdated = System.currentTimeMillis()
        )
        AppState.adminUpdateProduct(updatedProduct)
        android.widget.Toast.makeText(context, "✓ Product '${nameEn.trim()}' updated successfully!", android.widget.Toast.LENGTH_SHORT).show()
    }
    onDismiss()
}

@Composable
fun ManageGiftsDialog(onDismiss: () -> Unit) {
    var newThreshold by remember { mutableStateOf("") }
    var newPrice by remember { mutableStateOf("") }
    var newProductName by remember { mutableStateOf("") }
    var newStock by remember { mutableStateOf("100") }
    var newImageUrl by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Manage Gifts", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Existing Gifts:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    if (AppState.giftConfigsList.isEmpty()) {
                        Text("No gifts configured yet.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(8.dp))
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppState.giftConfigsList.forEach { gift ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${gift.productName} (₹${gift.giftPrice})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Text("Unlocks at ₹${gift.thresholdAmount} | Stock: ${gift.stockQuantity}", fontSize = 12.sp, color = Color.Gray)
                                    }
                                    IconButton(onClick = { AppState.deleteGiftConfig(gift.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                    }
                                }
                                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                            }
                        }
                    }
                }

                Text("Add New Gift:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                
                OutlinedTextField(
                    value = newProductName,
                    onValueChange = { newProductName = it },
                    label = { Text("Gift Product Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = RoyalEmerald,
                        focusedLabelColor = RoyalEmerald
                    )
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newThreshold,
                        onValueChange = { newThreshold = it.filter { char -> char.isDigit() || char == '.' } },
                        label = { Text("Threshold (₹)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = RoyalEmerald,
                            focusedLabelColor = RoyalEmerald
                        )
                    )
                    OutlinedTextField(
                        value = newPrice,
                        onValueChange = { newPrice = it.filter { char -> char.isDigit() || char == '.' } },
                        label = { Text("Gift Price (₹)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = RoyalEmerald,
                            focusedLabelColor = RoyalEmerald
                        )
                    )
                }
                OutlinedTextField(
                    value = newStock,
                    onValueChange = { newStock = it.filter { char -> char.isDigit() } },
                    label = { Text("Stock Quantity") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = RoyalEmerald,
                        focusedLabelColor = RoyalEmerald
                    )
                )
                OutlinedTextField(
                    value = newImageUrl,
                    onValueChange = { newImageUrl = it },
                    label = { Text("Image URL (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = RoyalEmerald,
                        focusedLabelColor = RoyalEmerald
                    )
                )
                
                Button(
                    onClick = {
                        val thresh = newThreshold.toDoubleOrNull() ?: 0.0
                        val price = newPrice.toDoubleOrNull() ?: 0.0
                        val stock = newStock.toIntOrNull() ?: 100
                        if (newProductName.isNotBlank() && thresh > 0) {
                            AppState.addNewGiftConfig(thresh, price, newProductName.trim(), newImageUrl.trim(), stock)
                            newProductName = ""
                            newThreshold = ""
                            newPrice = ""
                            newImageUrl = ""
                            newStock = "100"
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalEmerald),
                    shape = RoundedCornerShape(10.dp),
                    enabled = newProductName.isNotBlank() && newThreshold.isNotBlank()
                ) {
                    Text("Add Gift Tier", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = RoyalEmerald, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun AdminOrderMapModal(order: Order, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val targetLat = if (order.latitude != 0.0) order.latitude else AppState.SHOP_LATITUDE
    val targetLon = if (order.longitude != 0.0) order.longitude else AppState.SHOP_LONGITUDE
    val hasExactCoords = order.latitude != 0.0 && order.longitude != 0.0
    val isDark = AppState.isDarkMode

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.94f).height(560.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 12.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // 1. Native OSMMapView showing exact location pin
                AndroidView(
                    factory = { ctx ->
                        try {
                            org.osmdroid.config.Configuration.getInstance().userAgentValue = ctx.packageName
                        } catch (_: Exception) {}
                        org.osmdroid.views.MapView(ctx).apply {
                            setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(17.0) // House / street level zoom
                            val point = org.osmdroid.util.GeoPoint(targetLat, targetLon)
                            controller.setCenter(point)

                            // Customer Location Marker (Red Pin)
                            val customerMarker = org.osmdroid.views.overlay.Marker(this).apply {
                                position = point
                                title = "Customer: ${order.customerName}"
                                snippet = "${order.addressHouseNo}, ${order.addressLandmark}"
                                setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM)
                            }
                            overlays.add(customerMarker)

                            // Shop Location Marker (Green Pin)
                            val shopPoint = org.osmdroid.util.GeoPoint(AppState.SHOP_LATITUDE, AppState.SHOP_LONGITUDE)
                            val shopMarker = org.osmdroid.views.overlay.Marker(this).apply {
                                position = shopPoint
                                title = "G-STORE Shop Location"
                                setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM)
                            }
                            overlays.add(shopMarker)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Top Header Overlay with Close Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .background(
                            if (isDark) Color(0xFF1E1E1E).copy(alpha = 0.95f) else Color.White.copy(alpha = 0.95f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "📍 Delivery Location",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isDark) Color(0xFF34D399) else RoyalEmerald
                        )
                        Text(
                            text = if (hasExactCoords) "GPS Pin • ${String.format("%.2f", order.distanceKm)} km from store" else "Shop Area Location",
                            fontSize = 11.5.sp,
                            color = Color.Gray
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                // Bottom Floating Info & Open in Google Maps Button
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF333333) else Color(0xFFE5E7EB))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("👤 ${order.customerName} (${order.customerPhone})", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = MaterialTheme.colorScheme.onSurface)
                        val addressText = listOfNotNull(order.addressHouseNo.takeIf { it.isNotBlank() }, order.addressLandmark.takeIf { it.isNotBlank() }).joinToString(", ")
                        Text("🏠 ${addressText.ifBlank { "Delivery address" }}", fontSize = 12.sp, color = Color.Gray, maxLines = 2)

                        Button(
                            onClick = {
                                val uriStr = if (hasExactCoords) {
                                    "https://www.google.com/maps/search/?api=1&query=${targetLat},${targetLon}"
                                } else {
                                    val q = java.net.URLEncoder.encode(addressText, "UTF-8")
                                    "https://www.google.com/maps/search/?api=1&query=$q"
                                }
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(uriStr))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF34D399) else RoyalEmerald)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Open in Google Maps App", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        }
                    }
                }
            }
        }
    }
}

