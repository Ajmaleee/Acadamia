package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Announcement
import com.example.data.model.HomeworkWithDetails
import com.example.data.model.User
import com.example.ui.components.HomeworkCard
import com.example.ui.components.bounceClick
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
  homeworkList: List<HomeworkWithDetails>,
  onHomeworkClick: (String) -> Unit,
  onToggleComplete: (String) -> Unit,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler(onBack = onNavigateBack)

  val groupedByDate = remember(homeworkList) {
    val fmt = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    homeworkList.groupBy { fmt.format(Date(it.homework.dueAt)) }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Homework Schedule", fontWeight = FontWeight.Bold, color = AcademicTextPrimary) },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AcademicTextPrimary)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = AcademicBgSecondary)
      )
    },
    containerColor = AcademicBgSecondary
  ) { paddingValues ->
    LazyColumn(
      modifier = modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      if (groupedByDate.isEmpty()) {
        item {
          Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
            Text("No scheduled assignments.", color = AcademicTextSecondary)
          }
        }
      } else {
        groupedByDate.forEach { (dateHeader, items) ->
          item {
            Text(
              text = dateHeader,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = AcademicPrimaryDark,
              modifier = Modifier.padding(top = 8.dp)
            )
          }
          items(items, key = { it.homework.id }) { item ->
            HomeworkCard(
              item = item,
              onItemClick = { onHomeworkClick(item.homework.id) },
              onToggleCompletion = { onToggleComplete(item.homework.id) }
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementsScreen(
  announcements: List<Announcement>,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler(onBack = onNavigateBack)

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Class Announcements", fontWeight = FontWeight.Bold, color = AcademicTextPrimary) },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AcademicTextPrimary)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = AcademicBgSecondary)
      )
    },
    containerColor = AcademicBgSecondary
  ) { paddingValues ->
    LazyColumn(
      modifier = modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      if (announcements.isEmpty()) {
        item {
          Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
            Text("No class announcements yet.", color = AcademicTextSecondary)
          }
        }
      } else {
        items(announcements, key = { it.id }) { ann ->
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = AcademicBgPrimary,
            border = BorderStroke(1.dp, AcademicBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Campaign,
                  contentDescription = null,
                  tint = AcademicPrimary,
                  modifier = Modifier.size(20.dp)
                )
                Text(
                  text = ann.title,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = AcademicTextPrimary
                )
              }
              Text(
                text = ann.body,
                style = MaterialTheme.typography.bodyMedium,
                color = AcademicTextSecondary
              )
              Text(
                text = "Posted by ${ann.authorName}",
                style = MaterialTheme.typography.labelSmall,
                color = AcademicTextMuted
              )
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
  user: User,
  isAdmin: Boolean,
  className: String,
  onUpdateClassName: (String) -> Unit,
  onToggleAdminMode: (Boolean) -> Unit,
  onSignOut: () -> Unit,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  BackHandler(onBack = onNavigateBack)

  var showEditClassDialog by remember { mutableStateOf(false) }
  var editedClassName by remember { mutableStateOf(className) }

  if (showEditClassDialog) {
    AlertDialog(
      onDismissRequest = { showEditClassDialog = false },
      title = { Text("Edit Class", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(
            text = "Update the class identifier or section for your classroom:",
            style = MaterialTheme.typography.bodyMedium,
            color = AcademicTextSecondary
          )
          OutlinedTextField(
            value = editedClassName,
            onValueChange = { editedClassName = it },
            placeholder = { Text("e.g. 11A or Class 11A • Science") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (editedClassName.isNotBlank()) {
              onUpdateClassName(editedClassName)
              showEditClassDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = AcademicPrimary),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("Save Class", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showEditClassDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Profile & Settings", fontWeight = FontWeight.Bold, color = AcademicTextPrimary) },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AcademicTextPrimary)
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
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // User Avatar Card
      Box(
        modifier = Modifier
          .size(80.dp)
          .clip(CircleShape)
          .background(AcademicPrimary),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = user.name.take(1).uppercase(),
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = 32.sp
        )
      }

      Text(
        text = user.name,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = AcademicTextPrimary
      )

      Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isAdmin) AcademicPrimaryLight else AcademicPaleBlue
      ) {
        Text(
          text = if (isAdmin) "CLASS ADMINISTRATOR" else "APPROVED STUDENT",
          color = if (isAdmin) AcademicPrimaryDark else AcademicPrimary,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
      }

      // Class Information with Edit Class feature
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = AcademicBgPrimary,
        border = BorderStroke(1.dp, AcademicBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("Class Information", fontWeight = FontWeight.Bold, color = AcademicTextPrimary)
              Text("Current: $className", color = AcademicPrimaryDark, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }

            // Edit class button
            OutlinedButton(
              onClick = {
                editedClassName = className
                showEditClassDialog = true
              },
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
              modifier = Modifier.bounceClick {
                editedClassName = className
                showEditClassDialog = true
              }
            ) {
              Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Edit Class", fontSize = 13.sp)
            }
          }

          HorizontalDivider(color = AcademicBorder)

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(AcademicSuccess)
            )
            Text(
              text = "Realtime Cloud Firestore Synchronized",
              color = AcademicSuccess,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }
      }

      // If user is Admin, allow switching view
      if (isAdmin) {
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = AcademicBgPrimary,
          border = BorderStroke(1.dp, AcademicBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Administrator Mode", fontWeight = FontWeight.Bold, color = AcademicTextPrimary)
              Text("Review students, manage homework & subjects", fontSize = 12.sp, color = AcademicTextSecondary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
              onClick = { onToggleAdminMode(true) },
              colors = ButtonDefaults.buttonColors(containerColor = AcademicPrimaryDark),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.bounceClick { onToggleAdminMode(true) }
            ) {
              Text("Open Admin")
            }
          }
        }
      }

      // ABOUT ACADEMIA & CREATOR ATTRIBUTION SECTION
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = AcademicBgPrimary,
        border = BorderStroke(1.dp, AcademicBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = null,
              tint = AcademicPrimary,
              modifier = Modifier.size(20.dp)
            )
            Text(
              text = "About Academia",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = AcademicTextPrimary
            )
          }

          Surface(
            shape = RoundedCornerShape(14.dp),
            color = AcademicPaleBlue,
            border = BorderStroke(1.dp, AcademicPrimary.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = "Created by Ajmal Ali A, 11A",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AcademicPrimaryDark
              )
              Text(
                text = "Built for modern collaborative student workflows and urgency-ranked homework tracking.",
                style = MaterialTheme.typography.bodySmall,
                color = AcademicTextSecondary
              )
            }
          }

          // Instagram link card with iOS tactile bounce
          val instagramUrl = "https://www.instagram.com/ajmaleee__"
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFFBF4F6),
            border = BorderStroke(1.dp, Color(0xFFE1306C).copy(alpha = 0.3f)),
            modifier = Modifier
              .fillMaxWidth()
              .bounceClick {
                try {
                  val intent = Intent(Intent.ACTION_VIEW, Uri.parse(instagramUrl))
                  context.startActivity(intent)
                } catch (_: Exception) {}
              }
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(CircleShape)
                  .background(
                    Brush.linearGradient(
                      listOf(
                        Color(0xFF833AB4),
                        Color(0xFFFD1D1D),
                        Color(0xFFFCB045)
                      )
                    )
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                  contentDescription = "Instagram",
                  tint = Color.White,
                  modifier = Modifier.size(20.dp)
                )
              }

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Instagram: @ajmaleee__",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF833AB4)
                )
                Text(
                  text = "Tap to view profile on Instagram",
                  style = MaterialTheme.typography.bodySmall,
                  color = AcademicTextSecondary
                )
              }
            }
          }

          Text(
            text = "Version 1.2.0 • Class 11A Edition",
            style = MaterialTheme.typography.labelSmall,
            color = AcademicTextMuted,
            modifier = Modifier.padding(top = 2.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Button(
        onClick = onSignOut,
        colors = ButtonDefaults.buttonColors(
          containerColor = AcademicErrorBg,
          contentColor = AcademicError
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .bounceClick(onClick = onSignOut)
          .testTag("profile_sign_out_button")
      ) {
        Icon(Icons.Default.ExitToApp, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Sign Out", fontWeight = FontWeight.Bold)
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}
