package com.example.ui.delivery

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.domain.model.Order
import com.example.domain.model.OrderStatus
import com.example.ui.state.AppState
import com.example.ui.theme.AlertRed
import com.example.ui.theme.DeepGold
import com.example.ui.theme.RoyalEmerald
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryScreen() {
    val isDark = AppState.isDarkMode
    val context = LocalContext.current

    // Initialize osmdroid
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    // Dynamic Theme Colors
    val pageBg = if (isDark) Color(0xFF121212) else Color(0xFFF4F6F5)
    val cardBg = if (isDark) Color(0xFF1E1E1E) else Color.White
    val cardBorder = if (isDark) Color(0xFF333333) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    var selectedTab by remember { mutableStateOf("ACTIVE") } // "ACTIVE", "COMPLETED"
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showEarningsModal by remember { mutableStateOf(false) }
    var showMultiStopModal by remember { mutableStateOf(false) }
    var activeMapOrder by remember { mutableStateOf<Order?>(null) }
    var activeCompletionOrder by remember { mutableStateOf<Order?>(null) }
    var activeItemsOrder by remember { mutableStateOf<Order?>(null) }
    var activeIssueOrder by remember { mutableStateOf<Order?>(null) }

    // Orders filtering
    val allOrders = AppState.ordersList
    val activeDeliveries = allOrders.filter {
        it.status == OrderStatus.OUT_FOR_DELIVERY || it.status == OrderStatus.PENDING
    }
    val completedDeliveries = allOrders.filter {
        it.status == OrderStatus.DELIVERED
    }

    val todayCompletedCount = completedDeliveries.size
    val totalCashCollected = completedDeliveries.sumOf { it.totalAmount }
    val remainingCashToSettle = (totalCashCollected - AppState.settledCashAmount).coerceAtLeast(0.0)
    val riderEarningsToday = AppState.getRiderEarningsToday(completedDeliveries)

    // Logout Sheet
    if (showLogoutConfirm) {
        ModalBottomSheet(onDismissRequest = { showLogoutConfirm = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("End Delivery Shift", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = textPrimary)
                Spacer(Modifier.height(8.dp))
                Text("Are you sure you want to end your delivery shift and log out?", color = textSecondary, fontSize = 14.sp)
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { AppState.logout(); showLogoutConfirm = false },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Yes, End Shift & Logout", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = { showLogoutConfirm = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel", color = textSecondary)
                }
            }
        }
    }

    // Earnings & Cash Handover Sheet
    if (showEarningsModal) {
        RiderEarningsSettlementModal(
            completedCount = todayCompletedCount,
            totalEarnings = riderEarningsToday,
            totalCashCollected = totalCashCollected,
            settledAmount = AppState.settledCashAmount,
            remainingToSettle = remainingCashToSettle,
            isDark = isDark,
            onSettleCash = { amount ->
                AppState.driverSettleCashWithStore(amount) {
                    showEarningsModal = false
                }
            },
            onDismiss = { showEarningsModal = false }
        )
    }

    // Multi-Stop Route Map Sheet
    if (showMultiStopModal) {
        MultiStopRouteModal(
            activeOrders = activeDeliveries,
            isDark = isDark,
            onDismiss = { showMultiStopModal = false }
        )
    }

    // Single Order Delivery Map Sheet
    activeMapOrder?.let { order ->
        DeliveryMapModal(
            order = order,
            isDark = isDark,
            onDismiss = { activeMapOrder = null }
        )
    }

    // Items Checklist Sheet
    activeItemsOrder?.let { order ->
        DeliveryItemsModal(
            order = order,
            isDark = isDark,
            onDismiss = { activeItemsOrder = null }
        )
    }

    // Report Delivery Issue Sheet
    activeIssueOrder?.let { order ->
        DeliveryIssueModal(
            order = order,
            isDark = isDark,
            onSubmitIssue = { reason, notes ->
                AppState.driverReportIssue(order.id, reason, notes) {
                    activeIssueOrder = null
                }
            },
            onDismiss = { activeIssueOrder = null }
        )
    }

    // Delivery Completion Bottom Sheet (with POD & Remarks)
    activeCompletionOrder?.let { order ->
        DeliveryCompletionModal(
            order = order,
            isDark = isDark,
            onConfirm = { otp, cash, photoUrl, remarks ->
                AppState.driverConfirmDelivery(order.id, otp, cash, photoUrl, remarks)
                activeCompletionOrder = null
            },
            onReportIssue = {
                val orderToReport = activeCompletionOrder
                activeCompletionOrder = null
                activeIssueOrder = orderToReport
            },
            onDismiss = { activeCompletionOrder = null }
        )
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDark) Color(0xFF1A1A1A) else Color.White)
                    .statusBarsPadding()
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(RoyalEmerald.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🛵", fontSize = 22.sp)
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = AppState.currentUser?.name ?: "Raju (G-Store Rider)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = "G-STORE DELIVERY PARTNER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RoyalEmerald,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Online/Offline Switch Indicator
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (AppState.isDriverOnline) RoyalEmerald.copy(alpha = 0.12f) else Color.Gray.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (AppState.isDriverOnline) RoyalEmerald.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.clickable { AppState.isDriverOnline = !AppState.isDriverOnline }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (AppState.isDriverOnline) RoyalEmerald else Color.Gray)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (AppState.isDriverOnline) "ONLINE" else "OFFLINE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (AppState.isDriverOnline) RoyalEmerald else textSecondary
                            )
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    IconButton(
                        onClick = { showLogoutConfirm = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Interactive Daily Metrics & Earnings Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) Color(0xFF262626) else Color(0xFFF1F5F3))
                        .clickable { showEarningsModal = true }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Earnings Today", fontSize = 11.sp, color = textSecondary, fontWeight = FontWeight.Medium)
                        Text("₹${String.format(Locale.ENGLISH, "%,.0f", riderEarningsToday)}", fontSize = 15.sp, fontWeight = FontWeight.Black, color = RoyalEmerald)
                    }

                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(if (isDark) Color(0xFF404040) else Color(0xFFD1D5DB)))

                    Column {
                        Text("Cash in Hand", fontSize = 11.sp, color = textSecondary, fontWeight = FontWeight.Medium)
                        Text("₹${String.format(Locale.ENGLISH, "%,.0f", remainingCashToSettle)}", fontSize = 15.sp, fontWeight = FontWeight.Black, color = DeepGold)
                    }

                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(if (isDark) Color(0xFF404040) else Color(0xFFD1D5DB)))

                    Column {
                        Text("Trips Done", fontSize = 11.sp, color = textSecondary, fontWeight = FontWeight.Medium)
                        Text("$todayCompletedCount Orders ➔", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    }
                }

                Spacer(Modifier.height(6.dp))

                // Tab Selector & Multi-Stop Button Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TabPill(
                        label = "Active (${activeDeliveries.size})",
                        isSelected = selectedTab == "ACTIVE",
                        onClick = { selectedTab = "ACTIVE" },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    TabPill(
                        label = "Completed ($todayCompletedCount)",
                        isSelected = selectedTab == "COMPLETED",
                        onClick = { selectedTab = "COMPLETED" },
                        modifier = Modifier.weight(1f)
                    )

                    if (activeDeliveries.isNotEmpty() && selectedTab == "ACTIVE") {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = RoyalEmerald.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RoyalEmerald.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .height(38.dp)
                                .clickable { showMultiStopModal = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Route, contentDescription = null, tint = RoyalEmerald, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Batch Map", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoyalEmerald)
                            }
                        }
                    }
                }

                HorizontalDivider(color = cardBorder, thickness = 1.dp)
            }
        },
        containerColor = pageBg
    ) { padding ->
        if (!AppState.isDriverOnline && selectedTab == "ACTIVE") {
            // Offline Status Screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("😴", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("You are currently Offline", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Toggle your status to ONLINE at the top to receive, navigate, and fulfill customer delivery orders.",
                        fontSize = 13.sp,
                        color = textSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { AppState.isDriverOnline = true },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalEmerald),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Go Online Now", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            val listToShow = if (selectedTab == "ACTIVE") activeDeliveries else completedDeliveries

            if (listToShow.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (selectedTab == "ACTIVE") Icons.Outlined.CheckCircle else Icons.Outlined.History,
                            contentDescription = null,
                            tint = textSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (selectedTab == "ACTIVE") "No active deliveries in queue" else "No completed trips yet today",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(listToShow, key = { it.id }) { order ->
                        DeliveryOrderCard(
                            order = order,
                            isDark = isDark,
                            onStartDelivery = {
                                AppState.driverMarkOrderPickedUp(order.id)
                            },
                            onCompleteDelivery = {
                                activeCompletionOrder = order
                            },
                            onOpenMap = {
                                activeMapOrder = order
                            },
                            onViewItems = {
                                activeItemsOrder = order
                            },
                            onReportIssue = {
                                activeIssueOrder = order
                            },
                            onCallCustomer = {
                                val phone = order.customerPhone.filter { it.isDigit() || it == '+' }
                                if (phone.isNotBlank()) {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                    context.startActivity(dialIntent)
                                }
                            },
                            onWhatsAppCustomer = {
                                val phoneDigits = order.customerPhone.filter { it.isDigit() }.takeLast(10)
                                if (phoneDigits.isNotBlank()) {
                                    val msg = "Hello ${order.customerName}! I am on the way with your G-Store order (#${order.id.takeLast(6).uppercase()}). I will arrive shortly."
                                    val waIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://api.whatsapp.com/send?phone=91$phoneDigits&text=${Uri.encode(msg)}")
                                    )
                                    context.startActivity(waIntent)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TabPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = AppState.isDarkMode
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) RoyalEmerald else if (isDark) Color(0xFF262626) else Color(0xFFEAEAEA),
        modifier = modifier
            .height(38.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
            )
        }
    }
}

@Composable
fun DeliveryOrderCard(
    order: Order,
    isDark: Boolean,
    onStartDelivery: () -> Unit,
    onCompleteDelivery: () -> Unit,
    onOpenMap: () -> Unit,
    onViewItems: () -> Unit,
    onReportIssue: () -> Unit,
    onCallCustomer: () -> Unit,
    onWhatsAppCustomer: () -> Unit
) {
    val cardBg = if (isDark) Color(0xFF1E1E1E) else Color.White
    val cardBorder = if (isDark) Color(0xFF333333) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    val isDelivered = order.status == OrderStatus.DELIVERED
    val isOutForDelivery = order.status == OrderStatus.OUT_FOR_DELIVERY

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Order ID & Distance Pill
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

                // Distance badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = RoyalEmerald.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${String.format(Locale.ENGLISH, "%.1f", if (order.distanceKm > 0) order.distanceKm else 2.4)} km away",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalEmerald,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Issue Banner if reported
            if (order.issueReported.isNotBlank() && !isDelivered) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AlertRed.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AlertRed.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠️", fontSize = 13.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Issue: ${order.issueReported}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AlertRed,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Customer Name & Phone
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = RoyalEmerald,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = order.customerName.ifEmpty { "Customer" },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "(${order.customerPhone})",
                    fontSize = 13.sp,
                    color = textSecondary
                )
            }

            Spacer(Modifier.height(6.dp))

            // Delivery Address with Landmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = textSecondary,
                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
                )
                Spacer(Modifier.width(6.dp))
                Column {
                    Text(
                        text = order.addressHouseNo.ifEmpty { "House / Flat Address" },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = textPrimary
                    )
                    if (order.addressLandmark.isNotBlank()) {
                        Text(
                            text = "Landmark: ${order.addressLandmark}",
                            fontSize = 12.sp,
                            color = textSecondary
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Items & Payment Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Item count chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDark) Color(0xFF2A2A2A) else Color(0xFFF1F5F3),
                    modifier = Modifier.clickable { onViewItems() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Outlined.ShoppingBag, contentDescription = null, tint = RoyalEmerald, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${order.items.size} Items (View) ➔",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textPrimary
                        )
                    }
                }

                // COD / Amount Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DeepGold.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DeepGold.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = if (isDelivered) "COLLECTED ₹${String.format(Locale.ENGLISH, "%,.0f", order.totalAmount)}" else "COLLECT ₹${String.format(Locale.ENGLISH, "%,.0f", order.totalAmount)} CASH",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepGold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Action Buttons Row (Call, WhatsApp, Map, Issue)
            if (!isDelivered) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Call Button
                    OutlinedButton(
                        onClick = onCallCustomer,
                        modifier = Modifier.weight(1f).height(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RoyalEmerald),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RoyalEmerald.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Call", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // WhatsApp Button
                    OutlinedButton(
                        onClick = onWhatsAppCustomer,
                        modifier = Modifier.weight(1.2f).height(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF25D366)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        Text("💬 WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Map Button
                    Button(
                        onClick = onOpenMap,
                        modifier = Modifier.weight(1f).height(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF333333) else Color(0xFFE2E8F0)),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        Icon(Icons.Default.Navigation, contentDescription = "Map", tint = textPrimary, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Map", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    }

                    // Issue Button
                    OutlinedButton(
                        onClick = onReportIssue,
                        modifier = Modifier.weight(0.9f).height(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AlertRed.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        Text("⚠️ Issue", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Main Progression Action
                if (isOutForDelivery) {
                    Button(
                        onClick = onCompleteDelivery,
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalEmerald)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Complete Delivery (POD & Cash)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                    }
                } else {
                    Button(
                        onClick = onStartDelivery,
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalEmerald)
                    ) {
                        Text("🛵 Pick Up & Start Delivery", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                    }
                }
            } else {
                // Delivered Pill with remarks
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = RoyalEmerald.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = RoyalEmerald, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Delivered Successfully", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RoyalEmerald)
                        }
                        if (order.deliveryRemarks.isNotBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(order.deliveryRemarks, fontSize = 11.sp, color = textSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeliveryItemsModal(
    order: Order,
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    @OptIn(ExperimentalMaterial3Api::class)
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Bag Checklist (${order.items.size} Items)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                Text("ORDER #${order.id.takeLast(6).uppercase()}", fontSize = 12.sp, color = textSecondary, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(order.items) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDark) Color(0xFF262626) else Color(0xFFF8FAF9))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.productName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Text("Size: ${item.selectedSize}", fontSize = 12.sp, color = textSecondary)
                        }
                        Text("Qty: ${item.quantity}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RoyalEmerald)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalEmerald)
            ) {
                Text("All Items Packed & Verified", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryCompletionModal(
    order: Order,
    isDark: Boolean,
    onConfirm: (otp: String, cash: Double, photoUrl: String, remarks: String) -> Unit,
    onReportIssue: () -> Unit,
    onDismiss: () -> Unit
) {
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    var otpInput by remember { mutableStateOf("") }
    var isCashReceived by remember { mutableStateOf(false) }
    var selectedDropNote by remember { mutableStateOf("Handed directly to customer") }
    var isPhotoCaptured by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Complete Delivery (Proof of Drop)", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = textPrimary)
            Spacer(Modifier.height(4.dp))
            Text("Order #${order.id.takeLast(6).uppercase()} for ${order.customerName}", fontSize = 13.sp, color = textSecondary)

            Spacer(Modifier.height(14.dp))

            // Cash Collection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DeepGold.copy(alpha = 0.15f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, DeepGold.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("CASH TO COLLECT (COD)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textSecondary)
                    Spacer(Modifier.height(2.dp))
                    Text("₹${String.format(Locale.ENGLISH, "%,.0f", order.totalAmount)}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = DeepGold)
                    Spacer(Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { isCashReceived = !isCashReceived }
                            .padding(2.dp)
                    ) {
                        Checkbox(
                            checked = isCashReceived,
                            onCheckedChange = { isCashReceived = it },
                            colors = CheckboxDefaults.colors(checkedColor = RoyalEmerald)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("I have collected ₹${String.format(Locale.ENGLISH, "%,.0f", order.totalAmount)} cash", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Photo Proof of Delivery (POD) Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) Color(0xFF262626) else Color(0xFFF1F5F3),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isPhotoCaptured) RoyalEmerald else if (isDark) Color(0xFF404040) else Color(0xFFCBD5E1)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isPhotoCaptured = !isPhotoCaptured }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isPhotoCaptured) RoyalEmerald.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPhotoCaptured) Icons.Default.Check else Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = if (isPhotoCaptured) RoyalEmerald else textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isPhotoCaptured) "Doorstep Photo Attached ✓" else "Snap Doorstep Photo (Optional POD)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPhotoCaptured) RoyalEmerald else textPrimary
                        )
                        Text(
                            text = if (isPhotoCaptured) "Timestamp & location tagged" else "Tap to attach delivery drop photo",
                            fontSize = 11.sp,
                            color = textSecondary
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Handover note options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Handed to customer", "Left at doorstep", "Given to security").forEach { note ->
                    val isSel = selectedDropNote == note
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSel) RoyalEmerald.copy(alpha = 0.15f) else (if (isDark) Color(0xFF262626) else Color(0xFFEAEAEA)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) RoyalEmerald else Color.Transparent),
                        modifier = Modifier.weight(1f).clickable { selectedDropNote = note }
                    ) {
                        Text(
                            text = note,
                            fontSize = 10.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) RoyalEmerald else textSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Optional PIN / OTP Verification
            OutlinedTextField(
                value = otpInput,
                onValueChange = { if (it.length <= 4) otpInput = it },
                label = { Text("Customer 4-Digit PIN (Optional)") },
                placeholder = { Text("Enter last 4 digits of phone") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    val mockPhotoUrl = if (isPhotoCaptured) "https://res.cloudinary.com/k1lw675z/image/upload/v1788031478/ricemart_products/pod_drop_${order.id.takeLast(6)}.jpg" else ""
                    onConfirm(otpInput, order.totalAmount, mockPhotoUrl, selectedDropNote)
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = isCashReceived,
                colors = ButtonDefaults.buttonColors(containerColor = RoyalEmerald)
            ) {
                Text("Confirm Handover & Settle", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
            }

            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = onReportIssue,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("⚠️ Report Delivery Issue (Gate Locked / Unreachable)", color = AlertRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryIssueModal(
    order: Order,
    isDark: Boolean,
    onSubmitIssue: (reason: String, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    val issueReasons = listOf(
        "📞 Customer phone unreachable / switched off",
        "🚪 Door locked / Nobody at home",
        "📍 Incorrect delivery address or landmark",
        "🛵 Vehicle breakdown / Traffic delay",
        "🔄 Customer requested reschedule / cancellation"
    )

    var selectedReason by remember { mutableStateOf(issueReasons[0]) }
    var notesText by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Report Delivery Issue", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AlertRed)
                Text("#${order.id.takeLast(6).uppercase()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textSecondary)
            }

            Spacer(Modifier.height(6.dp))
            Text("Select the reason so Store Admin and Customer are notified instantly:", fontSize = 12.sp, color = textSecondary)

            Spacer(Modifier.height(14.dp))

            issueReasons.forEach { reason ->
                val isSel = selectedReason == reason
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSel) AlertRed.copy(alpha = 0.12f) else if (isDark) Color(0xFF262626) else Color(0xFFF8FAF9),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) AlertRed else Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { selectedReason = reason }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSel,
                            onClick = { selectedReason = reason },
                            colors = RadioButtonDefaults.colors(selectedColor = AlertRed)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = reason,
                            fontSize = 13.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            color = textPrimary
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                label = { Text("Additional Remarks (Optional)") },
                placeholder = { Text("e.g. called 3 times, neighbor informed they will return at 6 PM") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(Modifier.height(18.dp))

            Button(
                onClick = { onSubmitIssue(selectedReason, notesText) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
            ) {
                Text("Submit Issue & Alert Store Manager", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiderEarningsSettlementModal(
    completedCount: Int,
    totalEarnings: Double,
    totalCashCollected: Double,
    settledAmount: Double,
    remainingToSettle: Double,
    isDark: Boolean,
    onSettleCash: (amount: Double) -> Unit,
    onDismiss: () -> Unit
) {
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    var settleSuccess by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Rider Earnings & Cash Settlement", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = RoyalEmerald.copy(alpha = 0.15f)
                ) {
                    Text("TODAY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalEmerald, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }

            Spacer(Modifier.height(14.dp))

            // Earnings Overview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = RoyalEmerald.copy(alpha = 0.12f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, RoyalEmerald.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("YOUR TOTAL EARNINGS TODAY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalEmerald)
                    Spacer(Modifier.height(4.dp))
                    Text("₹${String.format(Locale.ENGLISH, "%,.0f", totalEarnings)}", fontSize = 28.sp, fontWeight = FontWeight.Black, color = textPrimary)
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Base Pay ($completedCount trips × ₹25)", fontSize = 12.sp, color = textSecondary)
                        Text("₹${completedCount * 25}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Distance Allowance (>2km bonus)", fontSize = 12.sp, color = textSecondary)
                        Text("₹${String.format(Locale.ENGLISH, "%,.0f", (totalEarnings - completedCount * 25).coerceAtLeast(0.0))}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // COD Cash in Hand Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DeepGold.copy(alpha = 0.12f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, DeepGold.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("STORE CASH TO HANDOVER (COD)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepGold)
                    Spacer(Modifier.height(4.dp))
                    Text("₹${String.format(Locale.ENGLISH, "%,.0f", remainingToSettle)}", fontSize = 26.sp, fontWeight = FontWeight.Black, color = DeepGold)
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Cash Collected from Customers", fontSize = 12.sp, color = textSecondary)
                        Text("₹${String.format(Locale.ENGLISH, "%,.0f", totalCashCollected)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Already Handed Over to Store Manager", fontSize = 12.sp, color = textSecondary)
                        Text("₹${String.format(Locale.ENGLISH, "%,.0f", settledAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoyalEmerald)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            if (settleSuccess) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = RoyalEmerald.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cash handover settlement recorded successfully! ✓",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalEmerald,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                Button(
                    onClick = {
                        if (remainingToSettle > 0) {
                            onSettleCash(remainingToSettle)
                            settleSuccess = true
                        }
                    },
                    enabled = remainingToSettle > 0,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepGold)
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Handover & Settle ₹${String.format(Locale.ENGLISH, "%,.0f", remainingToSettle)} with Store", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiStopRouteModal(
    activeOrders: List<Order>,
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    val storeLat = 18.4529
    val storeLon = 83.6548

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Batch Route Map (${activeOrders.size} Stops)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    Text("Optimized Delivery Path from G-Store Hub", fontSize = 12.sp, color = textSecondary)
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = RoyalEmerald.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "TOTAL ~${String.format(Locale.ENGLISH, "%.1f", activeOrders.sumOf { if (it.distanceKm > 0) it.distanceKm else 2.0 })} km",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalEmerald,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Multi-Stop OSMDroid Map Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, if (isDark) Color(0xFF333333) else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(14.5)
                            controller.setCenter(GeoPoint(storeLat, storeLon))

                            // Store Marker (Hub)
                            val storeMarker = Marker(this).apply {
                                position = GeoPoint(storeLat, storeLon)
                                title = "🏬 G-Store Hub (Start Point)"
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            }
                            overlays.add(storeMarker)

                            // Add Numbered Pins for each customer stop
                            activeOrders.forEachIndexed { idx, order ->
                                val stopLat = if (order.latitude != 0.0) order.latitude else (storeLat + (idx + 1) * 0.003)
                                val stopLon = if (order.longitude != 0.0) order.longitude else (storeLon + (idx + 1) * 0.004)
                                val stopMarker = Marker(this).apply {
                                    position = GeoPoint(stopLat, stopLon)
                                    title = "Stop #${idx + 1}: ${order.customerName} (${order.addressHouseNo})"
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                }
                                overlays.add(stopMarker)
                            }
                        }
                    }
                )
            }

            Spacer(Modifier.height(14.dp))

            // List of stops
            Text("SEQUENCE OF STOPS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textSecondary, letterSpacing = 1.sp)
            Spacer(Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 160.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(activeOrders.size) { idx ->
                    val order = activeOrders[idx]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDark) Color(0xFF262626) else Color(0xFFF8FAF9))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(RoyalEmerald),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${idx + 1}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(order.customerName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Text(order.addressHouseNo, fontSize = 11.sp, color = textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Text("₹${String.format(Locale.ENGLISH, "%,.0f", order.totalAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepGold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryMapModal(
    order: Order,
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    // Store Location (Rajam Market) vs Customer Location
    val storeLat = 18.4529
    val storeLon = 83.6548
    val custLat = if (order.latitude != 0.0) order.latitude else 18.4550
    val custLon = if (order.longitude != 0.0) order.longitude else 83.6590

    var simProgress by remember { mutableFloatStateOf(0.35f) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Live Delivery Route", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    Text("To: ${order.addressHouseNo}", fontSize = 12.sp, color = textSecondary)
                }

                Button(
                    onClick = {
                        val gmmIntentUri = Uri.parse("google.navigation:q=$custLat,$custLon")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                            setPackage("com.google.android.apps.maps")
                        }
                        try {
                            context.startActivity(mapIntent)
                        } catch (e: Exception) {
                            val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:$custLat,$custLon?q=$custLat,$custLon(${Uri.encode(order.customerName)})"))
                            context.startActivity(fallbackIntent)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalEmerald),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Text("Google Maps", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(Modifier.height(12.dp))

            // OSMDroid Map Container with Live Scooter Pin
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, if (isDark) Color(0xFF333333) else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(15.0)
                            val centerPoint = GeoPoint((storeLat + custLat) / 2.0, (storeLon + custLon) / 2.0)
                            controller.setCenter(centerPoint)

                            // Store Marker
                            val storeMarker = Marker(this).apply {
                                position = GeoPoint(storeLat, storeLon)
                                title = "🏬 G-Store Hub (Rajam)"
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            }
                            overlays.add(storeMarker)

                            // Scooter Driver Live Marker
                            val driverMarker = Marker(this).apply {
                                position = GeoPoint(
                                    storeLat + (custLat - storeLat) * simProgress,
                                    storeLon + (custLon - storeLon) * simProgress
                                )
                                title = "🛵 Rider Raju (On the Way)"
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            }
                            overlays.add(driverMarker)

                            // Customer Marker
                            val custMarker = Marker(this).apply {
                                position = GeoPoint(custLat, custLon)
                                title = "🏠 ${order.customerName}: ${order.addressHouseNo}"
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            }
                            overlays.add(custMarker)
                        }
                    },
                    update = { mapView ->
                        // Dynamically update marker on position change
                        if (mapView.overlays.size >= 3) {
                            val driverMarker = mapView.overlays[1] as? Marker
                            driverMarker?.position = GeoPoint(
                                storeLat + (custLat - storeLat) * simProgress,
                                storeLon + (custLon - storeLon) * simProgress
                            )
                            mapView.invalidate()
                        }
                    }
                )
            }

            Spacer(Modifier.height(10.dp))

            // Simulation Step Button (for testing live movement & ETA)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🛵 Rider is ${(simProgress * 100).toInt()}% along route (~${String.format(Locale.ENGLISH, "%.1f", (1.0 - simProgress) * 2.4)} km left)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RoyalEmerald
                )

                TextButton(
                    onClick = {
                        simProgress = if (simProgress >= 0.9f) 0.1f else simProgress + 0.25f
                        AppState.simulateDriverProgress(order, simProgress)
                    }
                ) {
                    Text("Simulate GPS Step ➔", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalEmerald)
                }
            }
        }
    }
}
