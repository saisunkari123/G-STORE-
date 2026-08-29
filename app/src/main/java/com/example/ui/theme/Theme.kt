package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.ui.state.AppState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

private val DarkColorScheme = darkColorScheme(
  primary = RoyalEmerald,
  secondary = DeepGold,
  tertiary = PremiumIvory,
  background = Color(0xFF121212),
  surface = Color(0xFF1E1E1E),
  surfaceVariant = Color(0xFF262626),
  onPrimary = Color.White,
  onSecondary = Color.White,
  onTertiary = Color.White,
  onBackground = Color(0xFFF8FAFC),
  onSurface = Color(0xFFF8FAFC),
  onSurfaceVariant = Color(0xFF94A3B8),
  outline = Color(0xFF404040)
)

private val LightColorScheme = lightColorScheme(
  primary = RoyalEmerald,
  secondary = DeepGold,
  tertiary = PremiumIvory,
  background = Color(0xFFF8F9FF),
  surface = Color.White,
  surfaceVariant = Color(0xFFF1F5F9),
  onPrimary = Color.White,
  onSecondary = Color(0xFF1E293B),
  onTertiary = Color(0xFF1E293B),
  onBackground = Color(0xFF0F172A),
  onSurface = Color(0xFF0F172A),
  onSurfaceVariant = Color(0xFF64748B),
  outline = Color(0xFFE2E8F0)
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit
) {
  val colorScheme = if (AppState.isDarkMode) DarkColorScheme else LightColorScheme

  val fontScale = minOf(LocalDensity.current.fontScale, 1.1f)
  CompositionLocalProvider(
    LocalDensity provides Density(LocalDensity.current.density, fontScale)
  ) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      content = content
    )
  }
}
