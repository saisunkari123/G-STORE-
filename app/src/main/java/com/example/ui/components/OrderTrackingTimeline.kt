package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.domain.model.Order
import com.example.ui.state.AppState
import com.example.ui.theme.AlertRed
import com.example.ui.theme.DeepGold
import com.example.ui.theme.RoyalEmerald
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.Locale

@Composable
fun OrderTrackingTimeline(
    orderStatus: String,
    order: Order? = null,
    modifier: Modifier = Modifier
) {
    val isDark = AppState.isDarkMode
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    val stages = listOf(
        Triple("Ordered", "PENDING", Icons.Default.ShoppingBag),
        Triple("Confirmed", "CONFIRMED", Icons.Default.ThumbUp),
        Triple("Dispatched", "OUT_FOR_DELIVERY", Icons.Default.LocalShipping),
        Triple("Delivered", "DELIVERED", Icons.Default.Check)
    )

    val currentStepIndex = when (orderStatus.uppercase()) {
        "PENDING" -> 0
        "CONFIRMED" -> 1
        "OUT_FOR_DELIVERY", "DISPATCHED" -> 2
        "DELIVERED" -> 3
        "CANCELLED" -> -1
        else -> 0
    }

    if (currentStepIndex == -1) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(if (isDark) Color(0xFF3F1D1D) else Color(0xFFFEE2E2), shape = RoundedCornerShape(12.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "❌ Order Cancelled",
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color(0xFFFCA5A5) else Color(0xFFDC2626),
                fontSize = 14.sp
            )
        }
        return
    }

    val containerBg = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF8FAFC)
    val inactiveBg = if (isDark) Color(0xFF333333) else Color(0xFFE2E8F0)
    val completedTextColor = if (isDark) Color(0xFFF1F5F9) else Color(0xFF1E293B)
    val inactiveTextColor = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(containerBg, shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ORDER STATUS TRACKER",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = RoyalEmerald,
                letterSpacing = 1.sp
            )

            if (currentStepIndex == 2) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = RoyalEmerald.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(RoyalEmerald))
                        Spacer(Modifier.width(4.dp))
                        Text("LIVE TRACKING", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RoyalEmerald)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4-Stage Stepper
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            stages.forEachIndexed { index, (label, _, icon) ->
                val isCompleted = index <= currentStepIndex
                val isCurrent = index == currentStepIndex
                val activeColor = RoyalEmerald

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isCurrent) activeColor else if (isCompleted) activeColor.copy(alpha = 0.15f) else inactiveBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isCurrent) Color.White else if (isCompleted) activeColor else inactiveTextColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (isCompleted) completedTextColor else inactiveTextColor,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }

                if (index < stages.size - 1) {
                    val lineActive = index < currentStepIndex
                    Box(
                        modifier = Modifier
                            .height(3.dp)
                            .weight(0.6f)
                            .background(if (lineActive) activeColor else inactiveBg, shape = CircleShape)
                    )
                }
            }
        }

        // Live Delivery Partner Details & Mini Map for Dispatched Order
        if (currentStepIndex == 2 && order != null) {
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = if (isDark) Color(0xFF333333) else Color(0xFFE2E8F0))
            Spacer(Modifier.height(14.dp))

            // Driver Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDark) Color(0xFF262626) else Color.White)
                    .border(1.dp, if (isDark) Color(0xFF404040) else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(RoyalEmerald.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🛵", fontSize = 20.sp)
                }

                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.assignedDriverName.ifEmpty { "Raju (G-Store Rider)" },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = completedTextColor
                    )
                    Text(
                        text = "Arriving in ~4-6 mins • ~${String.format(Locale.ENGLISH, "%.1f", if (order.distanceKm > 0) order.distanceKm else 2.0)} km",
                        fontSize = 11.sp,
                        color = RoyalEmerald,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Call Rider button
                IconButton(
                    onClick = {
                        val phone = (order.assignedDriverPhone.ifEmpty { "+919999900001" }).filter { it.isDigit() || it == '+' }
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                        context.startActivity(dialIntent)
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(RoyalEmerald)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = "Call Driver", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            // Issue Banner if reported
            if (order.issueReported.isNotBlank()) {
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
                        Text("⚠️", fontSize = 12.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Rider update: ${order.issueReported}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AlertRed
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Live Tracking Mini Map
            val storeLat = 18.4529
            val storeLon = 83.6548
            val custLat = if (order.latitude != 0.0) order.latitude else 18.4550
            val custLon = if (order.longitude != 0.0) order.longitude else 83.6590
            val driverLat = if (AppState.driverLiveLat != 0.0) AppState.driverLiveLat else ((storeLat + custLat) / 2.0)
            val driverLon = if (AppState.driverLiveLon != 0.0) AppState.driverLiveLon else ((storeLon + custLon) / 2.0)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, if (isDark) Color(0xFF404040) else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(15.0)
                            controller.setCenter(GeoPoint(driverLat, driverLon))

                            // Store Hub
                            val storeMarker = Marker(this).apply {
                                position = GeoPoint(storeLat, storeLon)
                                title = "🏬 G-Store Hub"
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            }
                            overlays.add(storeMarker)

                            // Live Scooter Marker
                            val riderMarker = Marker(this).apply {
                                position = GeoPoint(driverLat, driverLon)
                                title = "🛵 Delivery Partner"
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            }
                            overlays.add(riderMarker)

                            // Customer Home Marker
                            val custMarker = Marker(this).apply {
                                position = GeoPoint(custLat, custLon)
                                title = "🏠 Your Location"
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            }
                            overlays.add(custMarker)
                        }
                    },
                    update = { mapView ->
                        if (mapView.overlays.size >= 2) {
                            val riderMarker = mapView.overlays[1] as? Marker
                            riderMarker?.position = GeoPoint(driverLat, driverLon)
                            mapView.invalidate()
                        }
                    }
                )
            }
        } else if (currentStepIndex == 3 && order != null) {
            // Delivered Badge & Notes
            Spacer(Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = RoyalEmerald.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = RoyalEmerald, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (order.deliveryRemarks.isNotBlank()) "Delivered: ${order.deliveryRemarks}" else "Delivered safely at doorstep",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalEmerald
                    )
                }
            }
        }
    }
}
