package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HomeworkWithDetails
import com.example.data.model.User
import com.example.ui.components.UrgencyBadgeView
import com.example.ui.illustrations.SubjectIconBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeworkDetailScreen(
  item: HomeworkWithDetails?,
  user: User,
  onToggleComplete: () -> Unit,
  onReportHomework: (String) -> Unit,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler(onBack = onNavigateBack)

  var showReportDialog by remember { mutableStateOf(false) }
  var reportReason by remember { mutableStateOf("") }

  if (item == null) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text("Homework not found", color = AcademicTextSecondary)
    }
    return
  }

  val hw = item.homework
  val subject = item.subject
  val dueDateString = remember(hw.dueAt) {
    SimpleDateFormat("EEEE, MMMM d 'at' h:mm a", Locale.getDefault()).format(Date(hw.dueAt))
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = subject?.name ?: "Assignment Details",
            fontWeight = FontWeight.Bold,
            color = AcademicTextPrimary
          )
        },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = AcademicTextPrimary
            )
          }
        },
        actions = {
          IconButton(onClick = { showReportDialog = true }) {
            Icon(
              imageVector = Icons.Default.Flag,
              contentDescription = "Report",
              tint = AcademicTextMuted
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = AcademicBgSecondary)
      )
    },
    containerColor = AcademicBgSecondary
  ) { paddingValues ->
    Column(
      modifier = modifier
        .fillMaxSize()
        .padding(paddingValues)
        .verticalScroll(rememberScrollState())
        .padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
      // Header Card
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = AcademicBgPrimary,
        border = BorderStroke(1.dp, AcademicBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            SubjectIconBadge(
              iconKey = subject?.iconKey ?: "other",
              colorHex = subject?.colorHex ?: "#4778D0",
              sizeDp = 48.dp
            )
            UrgencyBadgeView(
              tier = item.urgency.tier,
              label = item.urgency.label
            )
          }

          Text(
            text = hw.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = AcademicTextPrimary
          )

          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Schedule,
              contentDescription = null,
              tint = AcademicTextSecondary,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = "Due $dueDateString",
              style = MaterialTheme.typography.bodyMedium,
              color = AcademicTextSecondary,
              fontWeight = FontWeight.Medium
            )
          }

          if (item.urgency.startByText.isNotBlank()) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = AcademicPaleBlue,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "Recommendation: ${item.urgency.startByText}",
                style = MaterialTheme.typography.bodySmall,
                color = AcademicPrimaryDark,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
              )
            }
          }
        }
      }

      // Metadata Card
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = AcademicBgPrimary,
        border = BorderStroke(1.dp, AcademicBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Estimated Time", style = MaterialTheme.typography.labelSmall, color = AcademicTextSecondary)
            Text("${hw.effortMinutes} mins", fontWeight = FontWeight.Bold, color = AcademicTextPrimary)
          }
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Author", style = MaterialTheme.typography.labelSmall, color = AcademicTextSecondary)
            Text(hw.authorName.ifBlank { "Classmate" }, fontWeight = FontWeight.Bold, color = AcademicTextPrimary)
          }
        }
      }

      // Description
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = AcademicBgPrimary,
        border = BorderStroke(1.dp, AcademicBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = "Instructions & Details",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AcademicTextPrimary
          )
          Text(
            text = if (hw.description.isNotBlank()) hw.description else "No additional notes provided.",
            style = MaterialTheme.typography.bodyLarge,
            color = AcademicTextSecondary,
            lineHeight = 22.sp
          )
        }
      }

      // Completion Button (For Students)
      if (user.role.name != "ADMIN") {
        Button(
          onClick = onToggleComplete,
          colors = ButtonDefaults.buttonColors(
            containerColor = if (item.isCompleted) AcademicSuccess else AcademicPrimary,
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("detail_complete_button")
        ) {
          Icon(
            imageVector = if (item.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (item.isCompleted) "Completed (Tap to Undo)" else "Mark as Complete",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
        }
      }
    }
  }

  // Report Dialog
  if (showReportDialog) {
    AlertDialog(
      onDismissRequest = { showReportDialog = false },
      title = { Text("Report Assignment", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Flag inaccurate deadlines, spam, or problematic content for the teacher to review.")
          OutlinedTextField(
            value = reportReason,
            onValueChange = { reportReason = it },
            placeholder = { Text("Reason for report...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (reportReason.isNotBlank()) {
              onReportHomework(reportReason.trim())
              showReportDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = AcademicError)
        ) {
          Text("Submit Report")
        }
      },
      dismissButton = {
        TextButton(onClick = { showReportDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
