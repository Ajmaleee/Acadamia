package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HomeworkWithDetails
import com.example.data.model.Subject
import com.example.data.model.UrgencyTier
import com.example.ui.illustrations.SubjectIconBadge
import com.example.ui.theme.*

/**
 * iOS-style tactile spring bounce micro-interactions & tactile cards.
 */

@Composable
fun Modifier.bounceClick(
  scaleDown: Float = 0.94f,
  enabled: Boolean = true,
  onClick: () -> Unit
): Modifier {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  val scale by animateFloatAsState(
    targetValue = if (isPressed && enabled) scaleDown else 1.0f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMediumLow
    ),
    label = "bounce_scale"
  )

  return this
    .scale(scale)
    .clickable(
      interactionSource = interactionSource,
      indication = ripple(),
      enabled = enabled,
      onClick = onClick
    )
}

@Composable
fun BouncySurface(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  scaleDown: Float = 0.95f,
  content: @Composable BoxScope.() -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  val scale by animateFloatAsState(
    targetValue = if (isPressed && enabled) scaleDown else 1.0f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMediumLow
    ),
    label = "bouncy_scale"
  )

  Box(
    modifier = modifier
      .scale(scale)
      .clickable(
        interactionSource = interactionSource,
        indication = ripple(),
        enabled = enabled,
        onClick = onClick
      ),
    content = content
  )
}

@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier) {
  androidx.compose.foundation.Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val strokeWidth = w * 0.22f
    val barHeight = strokeWidth * 0.96f
    val barWidth = w * 0.52f

    // Red arc (top / top-left: 175° to 285°)
    drawArc(
      color = Color(0xFFEA4335),
      startAngle = 175f,
      sweepAngle = 110f,
      useCenter = false,
      style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
    )

    // Yellow arc (bottom-left: 115° to 175°)
    drawArc(
      color = Color(0xFFFBBC05),
      startAngle = 115f,
      sweepAngle = 60f,
      useCenter = false,
      style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
    )

    // Green arc (bottom / bottom-right: 30° to 115°)
    drawArc(
      color = Color(0xFF34A853),
      startAngle = 30f,
      sweepAngle = 85f,
      useCenter = false,
      style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
    )

    // Blue arc (top-right: 285° to 360°)
    drawArc(
      color = Color(0xFF4285F4),
      startAngle = 285f,
      sweepAngle = 75f,
      useCenter = false,
      style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
    )

    // Blue horizontal bar
    drawRect(
      color = Color(0xFF4285F4),
      topLeft = androidx.compose.ui.geometry.Offset(w * 0.46f, (h - barHeight) / 2f),
      size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
    )
  }
}

@Composable
fun GoogleSignInButton(
  onClick: () -> Unit,
  isLoading: Boolean,
  modifier: Modifier = Modifier
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  val scale by animateFloatAsState(
    targetValue = if (isPressed && !isLoading) 0.95f else 1.0f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMediumLow
    ),
    label = "google_btn_bounce"
  )

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .height(52.dp)
      .scale(scale)
      .clickable(
        interactionSource = interactionSource,
        indication = ripple(),
        enabled = !isLoading,
        onClick = onClick
      )
      .testTag("google_signin_button"),
    shape = RoundedCornerShape(14.dp),
    color = Color.White,
    border = BorderStroke(1.5.dp, Color(0xFFDADCE0)),
    shadowElevation = 2.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      if (isLoading) {
        CircularProgressIndicator(
          modifier = Modifier.size(20.dp),
          strokeWidth = 2.5.dp,
          color = AcademicPrimary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
          text = "Connecting to Google...",
          color = Color(0xFF1F1F1F),
          fontWeight = FontWeight.SemiBold,
          fontSize = 15.sp
        )
      } else {
        GoogleLogoIcon(modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(
          text = "Sign in with Google",
          color = Color(0xFF1F1F1F),
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp
        )
      }
    }
  }
}

@Composable
fun UrgencyBadgeView(
  tier: UrgencyTier,
  label: String,
  modifier: Modifier = Modifier
) {
  val (bgColor, textColor, icon) = when (tier) {
    UrgencyTier.OVERDUE -> Triple(UrgencyOverdueBg, UrgencyOverdue, Icons.Default.Warning)
    UrgencyTier.DO_TODAY -> Triple(UrgencyDoTodayBg, UrgencyDoToday, Icons.Default.Timer)
    UrgencyTier.THIS_WEEK -> Triple(UrgencyThisWeekBg, UrgencyThisWeek, Icons.Default.Schedule)
    UrgencyTier.LATER -> Triple(UrgencyLaterBg, UrgencyLater, Icons.Default.CalendarToday)
    UrgencyTier.COMPLETED -> Triple(UrgencyCompletedBg, UrgencyCompleted, Icons.Default.CheckCircle)
  }

  Row(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(bgColor)
      .padding(horizontal = 8.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = textColor,
      modifier = Modifier.size(13.dp)
    )
    Text(
      text = label,
      color = textColor,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold
    )
  }
}

@Composable
fun HomeworkCard(
  item: HomeworkWithDetails,
  onItemClick: () -> Unit,
  onToggleCompletion: () -> Unit,
  modifier: Modifier = Modifier,
  showAuthor: Boolean = true,
  allowCompletion: Boolean = true
) {
  val hw = item.homework
  val subject = item.subject
  val isCompleted = item.isCompleted

  val accentColor = remember(subject?.colorHex) {
    try {
      Color(android.graphics.Color.parseColor(subject?.colorHex ?: "#4778D0"))
    } catch (_: Exception) {
      AcademicPrimary
    }
  }

  // Tactile completion checkmark animation
  val checkScale by animateFloatAsState(
    targetValue = if (isCompleted) 1.15f else 1.0f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioHighBouncy,
      stiffness = Spring.StiffnessMedium
    ),
    label = "check_anim"
  )

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .testTag("homework_card_${hw.id}"),
    shape = RoundedCornerShape(16.dp),
    color = if (isCompleted) AcademicBgSecondary.copy(alpha = 0.8f) else AcademicBgPrimary,
    border = BorderStroke(1.dp, if (isCompleted) AcademicBorder.copy(alpha = 0.6f) else AcademicBorder),
    shadowElevation = if (isCompleted) 0.dp else 1.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onItemClick)
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Subject Icon
      SubjectIconBadge(
        iconKey = subject?.iconKey ?: "other",
        colorHex = subject?.colorHex ?: "#4778D0",
        sizeDp = 44.dp
      )

      // Details
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = subject?.name ?: "General",
            style = MaterialTheme.typography.labelSmall,
            color = accentColor,
            fontWeight = FontWeight.Bold
          )
          if (hw.pinned) {
            Icon(
              imageVector = Icons.Default.PushPin,
              contentDescription = "Pinned",
              tint = AcademicWarning,
              modifier = Modifier.size(13.dp)
            )
          }
          Spacer(modifier = Modifier.weight(1f))
          UrgencyBadgeView(
            tier = item.urgency.tier,
            label = item.urgency.label
          )
        }

        Text(
          text = hw.title,
          style = MaterialTheme.typography.titleMedium,
          color = if (isCompleted) AcademicTextSecondary else AcademicTextPrimary,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          if (showAuthor && hw.authorName.isNotBlank()) {
            Text(
              text = "By ${hw.authorName}",
              style = MaterialTheme.typography.bodySmall,
              color = AcademicTextSecondary
            )
          }

          Text(
            text = "${hw.effortMinutes} min est.",
            style = MaterialTheme.typography.bodySmall,
            color = AcademicTextMuted
          )
        }
      }

      // Checkbox (Interactive only if student/allowed)
      if (allowCompletion) {
        IconButton(
          onClick = onToggleCompletion,
          modifier = Modifier
            .size(36.dp)
            .scale(checkScale)
            .testTag("toggle_completion_${hw.id}")
        ) {
          if (isCompleted) {
            Box(
              modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(AcademicSuccess),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Completed",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
              )
            }
          } else {
            Box(
              modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.Transparent)
                .padding(2.dp),
              contentAlignment = Alignment.Center
            ) {
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .clip(CircleShape)
                  .background(AcademicBorder)
              )
            }
          }
        }
      }
    }
  }
}

/**
 * High-priority "Do This First" hero card.
 * Rendered ONLY for Students.
 */
@Composable
fun DoThisFirstCard(
  item: HomeworkWithDetails,
  onClick: () -> Unit,
  onToggleComplete: () -> Unit,
  modifier: Modifier = Modifier
) {
  val hw = item.homework
  val subject = item.subject

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .testTag("do_this_first_card"),
    shape = RoundedCornerShape(20.dp),
    color = AcademicPrimaryDark,
    shadowElevation = 3.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(Color(0xFFFFB020))
          )
          Text(
            text = "DO THIS FIRST",
            color = Color(0xFFFFB020),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
          )
        }

        UrgencyBadgeView(
          tier = item.urgency.tier,
          label = item.urgency.label
        )
      }

      Text(
        text = hw.title,
        color = Color.White,
        style = MaterialTheme.typography.titleLarge,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      if (hw.description.isNotBlank()) {
        Text(
          text = hw.description,
          color = Color(0xFFD2D6DC),
          style = MaterialTheme.typography.bodyMedium,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
      }

      HorizontalDivider(color = Color(0xFF2C3E5D), thickness = 1.dp)

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column {
          Text(
            text = subject?.name ?: "General Studies",
            color = Color(0xFFA0AEC0),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
          )
          Text(
            text = "Est. ${hw.effortMinutes} mins • By ${hw.authorName}",
            color = Color(0xFFE2E8F0),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
          )
        }

        Button(
          onClick = onToggleComplete,
          colors = ButtonDefaults.buttonColors(
            containerColor = AcademicSuccess,
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(12.dp),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Done", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun FilterChipModern(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  count: Int? = null,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier.testTag("filter_chip_$label"),
    shape = RoundedCornerShape(20.dp),
    color = if (selected) AcademicPrimary else AcademicBgPrimary,
    border = BorderStroke(1.dp, if (selected) AcademicPrimary else AcademicBorder),
    shadowElevation = if (selected) 1.dp else 0.dp
  ) {
    Row(
      modifier = Modifier
        .clickable(onClick = onClick)
        .padding(horizontal = 14.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Text(
        text = label,
        color = if (selected) Color.White else AcademicTextPrimary,
        fontSize = 13.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
      )
      if (count != null && count > 0) {
        Box(
          modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) Color.White.copy(alpha = 0.25f) else AcademicPaleBlue)
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = count.toString(),
            color = if (selected) Color.White else AcademicPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}
