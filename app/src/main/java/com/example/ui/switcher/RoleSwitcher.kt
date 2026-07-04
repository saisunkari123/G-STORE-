package com.example.ui.switcher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.state.AppState

@Composable
fun RoleSwitcherHeader() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("role_switcher_header"),
        color = MaterialTheme.colorScheme.secondaryContainer,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 10.dp, horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "G-STORE • PROTOTYPE PORTAL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Control Panel",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Store Hours Bypass Toggle
                    Button(
                        onClick = { AppState.forceStoreOpen = !AppState.forceStoreOpen },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (AppState.forceStoreOpen) MaterialTheme.colorScheme.primary else Color.Red,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp).testTag("store_open_toggle")
                    ) {
                        Text(
                            text = if (AppState.forceStoreOpen) "Store: Open" else "Store: Closed",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
