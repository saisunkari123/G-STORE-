package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.ui.state.AppState

private val DarkColorScheme = darkColorScheme(
  primary = RoyalEmerald,
  secondary = DeepGold,
  tertiary = PremiumIvory,
  background = Color(0xFF121212),
  surface = Color(0xFF1E1E1E),
  onPrimary = Color.White,
  onSecondary = Color.White,
  onTertiary = Color.White,
  onBackground = Color.White,
  onSurface = Color.White,
)

private val LightColorScheme = lightColorScheme(
  primary = RoyalEmerald,
  secondary = DeepGold,
  tertiary = PremiumIvory,
  background = Color(0xFFF8F9FF),
  surface = Color.White,
  onPrimary = Color.White,
  onSecondary = Color.Black,
  onTertiary = Color.Black,
  onBackground = Color.Black,
  onSurface = Color.Black,
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit
) {
  val colorScheme = if (AppState.isDarkMode) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
