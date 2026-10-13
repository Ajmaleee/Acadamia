package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
  primary = AcademicPrimary,
  onPrimary = AcademicBgPrimary,
  primaryContainer = AcademicPrimaryLight,
  onPrimaryContainer = AcademicPrimaryDark,
  secondary = AcademicPrimaryDark,
  onSecondary = AcademicBgPrimary,
  secondaryContainer = AcademicPaleBlue,
  onSecondaryContainer = AcademicPrimaryDark,
  tertiary = AcademicSuccess,
  onTertiary = AcademicBgPrimary,
  tertiaryContainer = AcademicSuccessBg,
  onTertiaryContainer = AcademicSuccess,
  background = AcademicBgSecondary,
  onBackground = AcademicTextPrimary,
  surface = AcademicBgPrimary,
  onSurface = AcademicTextPrimary,
  surfaceVariant = AcademicBgSecondary,
  onSurfaceVariant = AcademicTextSecondary,
  outline = AcademicBorder,
  error = AcademicError,
  onError = AcademicBgPrimary,
  errorContainer = AcademicErrorBg,
  onErrorContainer = AcademicError
)

private val DarkColorScheme = darkColorScheme(
  primary = AcademicPrimary,
  onPrimary = AcademicBgPrimary,
  primaryContainer = AcademicPrimaryDark,
  onPrimaryContainer = AcademicPrimaryLight,
  secondary = AcademicPrimaryLight,
  onSecondary = AcademicBgPrimary,
  background = AcademicPrimaryDark,
  onBackground = AcademicBgPrimary,
  surface = AcademicPrimaryDark,
  onSurface = AcademicBgPrimary,
  surfaceVariant = AcademicTextPrimary,
  onSurfaceVariant = AcademicTextSecondary,
  outline = AcademicBorder,
  error = AcademicError,
  onError = AcademicBgPrimary
)

@Composable
fun AcademiaTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        window.statusBarColor = AcademicBgPrimary.toArgb()
        window.navigationBarColor = AcademicBgPrimary.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
      }
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
