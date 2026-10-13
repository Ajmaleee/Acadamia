package com.example.ui.illustrations

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

/**
 * 2D Modern Academic Vector Illustrations
 * High craft, restrained geometric shapes, soft pastel backgrounds, navy/blue strokes.
 */

@Composable
fun WelcomeDeskIllustration(modifier: Modifier = Modifier) {
  val infiniteTransition = rememberInfiniteTransition(label = "desk_anim")
  val bounce by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = -6f,
    animationSpec = infiniteRepeatable(
      animation = tween(1600, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "bounce"
  )

  Box(
    modifier = modifier
      .size(140.dp)
      .clip(CircleShape)
      .background(AcademicPaleBlue),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
      val w = size.width
      val h = size.height

      // Soft back halo
      drawCircle(
        color = Color(0xFFE2EAFC),
        radius = w * 0.45f,
        center = Offset(w * 0.5f, h * 0.5f)
      )

      // Desk base surface
      drawRoundRect(
        color = AcademicPrimaryDark,
        topLeft = Offset(w * 0.15f, h * 0.72f),
        size = Size(w * 0.7f, 6.dp.toPx()),
        cornerRadius = CornerRadius(3.dp.toPx())
      )

      // Open Notebook / Laptop
      drawRoundRect(
        color = Color(0xFFFFFFFF),
        topLeft = Offset(w * 0.32f, h * 0.52f + bounce),
        size = Size(w * 0.36f, h * 0.20f),
        cornerRadius = CornerRadius(4.dp.toPx())
      )
      drawRoundRect(
        color = AcademicPrimary,
        topLeft = Offset(w * 0.32f, h * 0.52f + bounce),
        size = Size(w * 0.36f, h * 0.20f),
        cornerRadius = CornerRadius(4.dp.toPx()),
        style = Stroke(width = 2.dp.toPx())
      )
      // Notebook lines
      drawLine(
        color = AcademicBorder,
        start = Offset(w * 0.38f, h * 0.58f + bounce),
        end = Offset(w * 0.62f, h * 0.58f + bounce),
        strokeWidth = 2.dp.toPx()
      )
      drawLine(
        color = AcademicBorder,
        start = Offset(w * 0.38f, h * 0.64f + bounce),
        end = Offset(w * 0.56f, h * 0.64f + bounce),
        strokeWidth = 2.dp.toPx()
      )

      // Pencil leaning
      drawLine(
        color = Color(0xFFDD6B20),
        start = Offset(w * 0.72f, h * 0.48f),
        end = Offset(w * 0.68f, h * 0.72f),
        strokeWidth = 3.dp.toPx()
      )

      // Plant pot
      drawRoundRect(
        color = Color(0xFF38A169),
        topLeft = Offset(w * 0.20f, h * 0.60f),
        size = Size(w * 0.10f, h * 0.12f),
        cornerRadius = CornerRadius(3.dp.toPx())
      )
      drawCircle(
        color = Color(0xFF48BB78),
        radius = w * 0.05f,
        center = Offset(w * 0.25f, h * 0.55f)
      )
    }
  }
}

@Composable
fun AllCaughtUpIllustration(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .size(130.dp)
      .clip(CircleShape)
      .background(AcademicSuccessBg),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
      val w = size.width
      val h = size.height

      // Golden halo
      drawCircle(
        color = Color(0xFFD1F2DF),
        radius = w * 0.44f,
        center = Offset(w * 0.5f, h * 0.5f)
      )

      // Star bursts
      val starColor = Color(0xFF38A169)
      drawCircle(color = starColor, radius = 3.dp.toPx(), center = Offset(w * 0.22f, h * 0.28f))
      drawCircle(color = starColor, radius = 2.5f.dp.toPx(), center = Offset(w * 0.80f, h * 0.32f))
      drawCircle(color = Color(0xFFDD6B20), radius = 2.5f.dp.toPx(), center = Offset(w * 0.75f, h * 0.75f))

      // Circular badge
      drawCircle(
        color = AcademicSuccess,
        radius = w * 0.28f,
        center = Offset(w * 0.5f, h * 0.5f)
      )

      // Checkmark path
      val checkPath = Path().apply {
        moveTo(w * 0.38f, h * 0.50f)
        lineTo(w * 0.47f, h * 0.59f)
        lineTo(w * 0.63f, h * 0.42f)
      }
      drawPath(
        path = checkPath,
        color = Color.White,
        style = Stroke(width = 4.dp.toPx())
      )
    }
  }
}

@Composable
fun WaitingApprovalIllustration(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .size(130.dp)
      .clip(CircleShape)
      .background(AcademicWarningBg),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
      val w = size.width
      val h = size.height

      drawCircle(
        color = Color(0xFFFFECC4),
        radius = w * 0.45f,
        center = Offset(w * 0.5f, h * 0.5f)
      )

      // Paper plane path
      val plane = Path().apply {
        moveTo(w * 0.75f, h * 0.30f)
        lineTo(w * 0.25f, h * 0.50f)
        lineTo(w * 0.45f, h * 0.60f)
        close()
      }
      drawPath(plane, color = Color(0xFFDD6B20))

      val planeWing = Path().apply {
        moveTo(w * 0.75f, h * 0.30f)
        lineTo(w * 0.45f, h * 0.60f)
        lineTo(w * 0.55f, h * 0.72f)
        close()
      }
      drawPath(planeWing, color = Color(0xFFB7791F))

      // Flight dash line
      drawLine(
        color = Color(0xFFD69E2E),
        start = Offset(w * 0.20f, h * 0.70f),
        end = Offset(w * 0.35f, h * 0.62f),
        strokeWidth = 2.dp.toPx()
      )
    }
  }
}

@Composable
fun AccessDeniedIllustration(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .size(130.dp)
      .clip(CircleShape)
      .background(AcademicErrorBg),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
      val w = size.width
      val h = size.height

      drawCircle(
        color = Color(0xFFFCD5D7),
        radius = w * 0.45f,
        center = Offset(w * 0.5f, h * 0.5f)
      )

      // Shield body
      drawRoundRect(
        color = AcademicError,
        topLeft = Offset(w * 0.32f, h * 0.40f),
        size = Size(w * 0.36f, h * 0.32f),
        cornerRadius = CornerRadius(6.dp.toPx())
      )

      // Shield loop
      drawArc(
        color = AcademicError,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(w * 0.38f, h * 0.28f),
        size = Size(w * 0.24f, h * 0.24f),
        style = Stroke(width = 3.dp.toPx())
      )

      // Keyhole
      drawCircle(
        color = Color.White,
        radius = 3.dp.toPx(),
        center = Offset(w * 0.50f, h * 0.52f)
      )
    }
  }
}

@Composable
fun SubjectIconBadge(
  iconKey: String,
  colorHex: String,
  sizeDp: Dp = 40.dp,
  modifier: Modifier = Modifier
) {
  val tintColor = try {
    Color(android.graphics.Color.parseColor(colorHex))
  } catch (_: Exception) {
    AcademicPrimary
  }

  val icon = when (iconKey.lowercase()) {
    "math" -> Icons.Default.Calculate
    "science" -> Icons.Default.Science
    "english" -> Icons.AutoMirrored.Filled.MenuBook
    "social" -> Icons.Default.Public
    "cs" -> Icons.Default.Computer
    "art" -> Icons.Default.Palette
    else -> Icons.Default.School
  }

  Box(
    modifier = modifier
      .size(sizeDp)
      .clip(RoundedCornerShape(sizeDp / 3.5f))
      .background(tintColor.copy(alpha = 0.14f)),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = tintColor,
      modifier = Modifier.size(sizeDp * 0.56f)
    )
  }
}
