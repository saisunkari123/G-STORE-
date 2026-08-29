package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RoyalEmerald

import com.example.ui.state.AppState

@Composable
fun OrderTrackingTimeline(
    orderStatus: String,
    modifier: Modifier = Modifier
) {
    val isDark = AppState.isDarkMode
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
        Text(
            text = "ORDER STATUS TRACKER",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = RoyalEmerald,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

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
    }
}
