package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Subject
import com.example.data.model.User
import com.example.ui.theme.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHomeworkScreen(
  user: User,
  subjects: List<Subject>,
  onAddHomework: (title: String, desc: String, subjectId: String, dueAt: Long, effortMinutes: Int) -> Unit,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler(onBack = onNavigateBack)

  var title by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var selectedSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: "") }
  var selectedEffortMinutes by remember { mutableIntStateOf(30) }
  var selectedDueOffsetHours by remember { mutableLongStateOf(24L) } // default 24h (tomorrow)
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val dueOffsets = listOf(
    Pair("Today 5 PM", 6L),
    Pair("Tomorrow", 24L),
    Pair("In 2 Days", 48L),
    Pair("This Friday", 96L),
    Pair("Next Week", 168L)
  )

  val effortOptions = listOf(15, 30, 45, 60, 90)

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Add Class Homework",
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
      verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      // Author Notice
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = AcademicPaleBlue,
        border = BorderStroke(1.dp, Color(0xFFD0E1FD))
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = "Posting as ${user.name} • All students in the class will receive this assignment live.",
            style = MaterialTheme.typography.bodySmall,
            color = AcademicPrimaryDark
          )
        }
      }

      // Subject Selector
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Subject",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = AcademicTextPrimary
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          subjects.take(4).forEach { sub ->
            val isSelected = selectedSubjectId == sub.id
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isSelected) AcademicPrimary else AcademicBgPrimary,
              border = BorderStroke(1.dp, if (isSelected) AcademicPrimary else AcademicBorder),
              modifier = Modifier
                .weight(1f)
                .clickable { selectedSubjectId = sub.id }
            ) {
              Box(
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = sub.name.take(9),
                  fontSize = 12.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) Color.White else AcademicTextPrimary
                )
              }
            }
          }
        }
      }

      // Title Input
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = "Assignment Title",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = AcademicTextPrimary
        )
        OutlinedTextField(
          value = title,
          onValueChange = {
            title = it
            errorMessage = null
          },
          placeholder = { Text("e.g. Chapter 4 Exercises 1 to 15") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("homework_title_input"),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = AcademicBgPrimary,
            unfocusedContainerColor = AcademicBgPrimary,
            focusedBorderColor = AcademicPrimary,
            unfocusedBorderColor = AcademicBorder
          )
        )
      }

      // Description Input
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = "Instructions / Notes",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = AcademicTextPrimary
        )
        OutlinedTextField(
          value = description,
          onValueChange = {
            description = it
            errorMessage = null
          },
          placeholder = { Text("Include page numbers, format instructions, or tips...") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("homework_desc_input"),
          minLines = 3,
          maxLines = 6,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = AcademicBgPrimary,
            unfocusedContainerColor = AcademicBgPrimary,
            focusedBorderColor = AcademicPrimary,
            unfocusedBorderColor = AcademicBorder
          )
        )
      }

      // Due Date Quick Offsets
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "When is it due?",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = AcademicTextPrimary
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          dueOffsets.forEach { (label, offsetHours) ->
            val isSelected = selectedDueOffsetHours == offsetHours
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (isSelected) AcademicPrimaryDark else AcademicBgPrimary,
              border = BorderStroke(1.dp, if (isSelected) AcademicPrimaryDark else AcademicBorder),
              modifier = Modifier
                .weight(1f)
                .clickable { selectedDueOffsetHours = offsetHours }
            ) {
              Box(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = label,
                  fontSize = 10.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) Color.White else AcademicTextPrimary
                )
              }
            }
          }
        }
      }

      // Effort Estimate
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Estimated Time Needed",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = AcademicTextPrimary
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          effortOptions.forEach { mins ->
            val isSelected = selectedEffortMinutes == mins
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (isSelected) AcademicPrimary else AcademicBgPrimary,
              border = BorderStroke(1.dp, if (isSelected) AcademicPrimary else AcademicBorder),
              modifier = Modifier
                .weight(1f)
                .clickable { selectedEffortMinutes = mins }
            ) {
              Box(
                modifier = Modifier.padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "${mins}m",
                  fontSize = 12.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) Color.White else AcademicTextPrimary
                )
              }
            }
          }
        }
      }

      if (errorMessage != null) {
        Text(
          text = errorMessage!!,
          color = AcademicError,
          style = MaterialTheme.typography.bodySmall
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Button(
        onClick = {
          if (title.isBlank()) {
            errorMessage = "Please enter an assignment title"
          } else {
            val dueAt = System.currentTimeMillis() + (selectedDueOffsetHours * 3600_000L)
            onAddHomework(
              title.trim(),
              description.trim(),
              selectedSubjectId,
              dueAt,
              selectedEffortMinutes
            )
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("submit_homework_button"),
        colors = ButtonDefaults.buttonColors(
          containerColor = AcademicPrimary,
          contentColor = Color.White
        ),
        shape = RoundedCornerShape(14.dp)
      ) {
        Text("Publish for Class", fontWeight = FontWeight.Bold, fontSize = 16.sp)
      }
    }
  }
}
