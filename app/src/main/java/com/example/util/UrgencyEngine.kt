package com.example.util

import com.example.data.model.UrgencyInfo
import com.example.data.model.UrgencyTier
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {
  private val defaultTimeZone: TimeZone = TimeZone.getTimeZone("Asia/Kolkata")

  fun formatDueLabel(dueAtMillis: Long, nowMillis: Long = System.currentTimeMillis()): String {
    val dueCal = Calendar.getInstance(defaultTimeZone).apply { timeInMillis = dueAtMillis }
    val nowCal = Calendar.getInstance(defaultTimeZone).apply { timeInMillis = nowMillis }

    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault()).apply {
      timeZone = defaultTimeZone
    }
    val timeStr = timeFormat.format(Date(dueAtMillis))

    val isSameDay = dueCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
        dueCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)

    nowCal.add(Calendar.DAY_OF_YEAR, 1)
    val isTomorrow = dueCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
        dueCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)

    nowCal.add(Calendar.DAY_OF_YEAR, -2)
    val isYesterday = dueCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
        dueCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)

    return when {
      isSameDay -> "Due today, $timeStr"
      isTomorrow -> "Due tomorrow, $timeStr"
      isYesterday -> "Due yesterday, $timeStr"
      else -> {
        val dateFormat = SimpleDateFormat("EEE, MMM d, h:mm a", Locale.getDefault()).apply {
          timeZone = defaultTimeZone
        }
        "Due ${dateFormat.format(Date(dueAtMillis))}"
      }
    }
  }

  fun formatStartBy(dueAtMillis: Long, effortMinutes: Int): String {
    val effortMillis = effortMinutes * 60 * 1000L
    val bufferMillis = 30 * 60 * 1000L
    val startByMillis = dueAtMillis - (effortMillis + bufferMillis)

    val format = SimpleDateFormat("EEE h:mm a", Locale.getDefault()).apply {
      timeZone = defaultTimeZone
    }
    return "Start by ${format.format(Date(startByMillis))}"
  }

  fun formatFullDateTime(millis: Long): String {
    val format = SimpleDateFormat("EEEE, MMMM d, yyyy 'at' h:mm a", Locale.getDefault()).apply {
      timeZone = defaultTimeZone
    }
    return format.format(Date(millis))
  }

  fun formatShortDate(millis: Long): String {
    val format = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).apply {
      timeZone = defaultTimeZone
    }
    return format.format(Date(millis))
  }

  fun formatRelativeTime(millis: Long, now: Long = System.currentTimeMillis()): String {
    val diff = now - millis
    val minutes = diff / (60 * 1000)
    val hours = diff / (60 * 60 * 1000)
    val days = diff / (24 * 60 * 60 * 1000)

    return when {
      minutes < 1 -> "just now"
      minutes < 60 -> "${minutes}m ago"
      hours < 24 -> "${hours}h ago"
      days < 7 -> "${days}d ago"
      else -> formatShortDate(millis)
    }
  }

  fun formatEffort(effortMinutes: Int): String {
    return when {
      effortMinutes < 60 -> "${effortMinutes}m"
      effortMinutes % 60 == 0 -> "${effortMinutes / 60}h"
      else -> "${effortMinutes / 60}h ${effortMinutes % 60}m"
    }
  }
}

object UrgencyEngine {
  fun calculateUrgency(
    dueAtMillis: Long,
    effortMinutes: Int,
    isCompleted: Boolean,
    nowMillis: Long = System.currentTimeMillis()
  ): UrgencyInfo {
    if (isCompleted) {
      return UrgencyInfo(
        tier = UrgencyTier.COMPLETED,
        label = "Completed",
        startByText = "Finished",
        hoursLeft = 0.0,
        hoursNeeded = effortMinutes / 60.0,
        isCompleted = true
      )
    }

    val hoursLeft = (dueAtMillis - nowMillis) / (1000.0 * 60.0 * 60.0)
    val hoursNeeded = (effortMinutes / 60.0).coerceAtLeast(0.25)
    val startByText = DateUtils.formatStartBy(dueAtMillis, effortMinutes)

    val tier = when {
      hoursLeft < 0 -> UrgencyTier.OVERDUE
      hoursLeft <= 24.0 || hoursLeft < (hoursNeeded * 3.0) -> UrgencyTier.DO_TODAY
      hoursLeft <= (24.0 * 7.0) -> UrgencyTier.THIS_WEEK
      else -> UrgencyTier.LATER
    }

    val label = when (tier) {
      UrgencyTier.OVERDUE -> "Overdue"
      UrgencyTier.DO_TODAY -> "Do today"
      UrgencyTier.THIS_WEEK -> "This week"
      UrgencyTier.LATER -> "Later"
      UrgencyTier.COMPLETED -> "Completed"
    }

    return UrgencyInfo(
      tier = tier,
      label = label,
      startByText = startByText,
      hoursLeft = hoursLeft,
      hoursNeeded = hoursNeeded,
      isCompleted = false
    )
  }
}
