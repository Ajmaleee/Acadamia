package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HomeworkWithDetails
import com.example.data.model.Subject
import com.example.data.model.UrgencyTier
import com.example.data.model.User
import com.example.ui.Screen
import com.example.ui.components.DoThisFirstCard
import com.example.ui.components.FilterChipModern
import com.example.ui.components.HomeworkCard
import com.example.ui.components.bounceClick
import com.example.ui.illustrations.AllCaughtUpIllustration
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboardScreen(
  user: User,
  homeworkList: List<HomeworkWithDetails>,
  subjects: List<Subject>,
  selectedSubjectFilter: String?,
  searchQuery: String,
  onSubjectFilterChange: (String?) -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onToggleComplete: (String) -> Unit,
  onHomeworkClick: (String) -> Unit,
  onNavigateToAddHomework: () -> Unit,
  onNavigateToCalendar: () -> Unit,
  onNavigateToAnnouncements: () -> Unit,
  onNavigateToProfile: () -> Unit,
  onNavigateToAdminPanel: () -> Unit,
  className: String = "11A • Academia",
  modifier: Modifier = Modifier
) {
  val todayFormatted = remember {
    SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
  }

  // Find the single highest priority uncompleted item for "Do This First"
  val doThisFirstItem = remember(homeworkList) {
    homeworkList.firstOrNull { !it.isCompleted }
  }

  val totalDueCount = remember(homeworkList) {
    homeworkList.count { !it.isCompleted }
  }
  val completedCount = remember(homeworkList) {
    homeworkList.count { it.isCompleted }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "$todayFormatted • $className",
              style = MaterialTheme.typography.labelSmall,
              color = AcademicTextSecondary
            )
            Text(
              text = "Hi, ${user.name}",
              style = MaterialTheme.typography.titleMedium,
              color = AcademicTextPrimary,
              fontWeight = FontWeight.Bold
            )
          }
        },
        actions = {
          IconButton(onClick = onNavigateToAnnouncements) {
            Icon(
              imageVector = Icons.Default.Campaign,
              contentDescription = "Announcements",
              tint = AcademicPrimary
            )
          }
          IconButton(onClick = onNavigateToCalendar) {
            Icon(
              imageVector = Icons.Default.CalendarMonth,
              contentDescription = "Calendar",
              tint = AcademicTextSecondary
            )
          }
          if (user.role.name == "ADMIN") {
            IconButton(
              onClick = onNavigateToAdminPanel,
              modifier = Modifier.testTag("admin_panel_button")
            ) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(AcademicPrimaryDark),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.AdminPanelSettings,
                  contentDescription = "Admin Panel",
                  tint = Color.White,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
          IconButton(onClick = onNavigateToProfile) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(AcademicPaleBlue),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = user.name.take(1).uppercase(),
                color = AcademicPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = AcademicBgSecondary)
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = onNavigateToAddHomework,
        containerColor = AcademicPrimary,
        contentColor = Color.White,
        shape = CircleShape,
        modifier = Modifier
          .bounceClick(scaleDown = 0.90f, onClick = onNavigateToAddHomework)
          .testTag("add_homework_fab")
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Homework")
      }
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
      // Summary Stats Row
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            color = AcademicBgPrimary,
            border = androidx.compose.foundation.BorderStroke(1.dp, AcademicBorder)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text("To Do", style = MaterialTheme.typography.labelSmall, color = AcademicTextSecondary)
              Text(
                text = "$totalDueCount tasks",
                style = MaterialTheme.typography.titleMedium,
                color = if (totalDueCount > 0) AcademicPrimaryDark else AcademicSuccess,
                fontWeight = FontWeight.Bold
              )
            }
          }
          Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            color = AcademicBgPrimary,
            border = androidx.compose.foundation.BorderStroke(1.dp, AcademicBorder)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text("Completed", style = MaterialTheme.typography.labelSmall, color = AcademicTextSecondary)
              Text(
                text = "$completedCount done",
                style = MaterialTheme.typography.titleMedium,
                color = AcademicSuccess,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      // "DO THIS FIRST" Card (Hero task)
      if (doThisFirstItem != null) {
        item {
          DoThisFirstCard(
            item = doThisFirstItem,
            onClick = { onHomeworkClick(doThisFirstItem.homework.id) },
            onToggleComplete = { onToggleComplete(doThisFirstItem.homework.id) }
          )
        }
      }

      // Search Bar
      item {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = onSearchQueryChange,
          placeholder = { Text("Search assignments...", fontSize = 14.sp) },
          leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = AcademicTextSecondary)
          },
          trailingIcon = {
            if (searchQuery.isNotBlank()) {
              IconButton(onClick = { onSearchQueryChange("") }) {
                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search", tint = AcademicTextSecondary)
              }
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("homework_search_field"),
          shape = RoundedCornerShape(14.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = AcademicBgPrimary,
            unfocusedContainerColor = AcademicBgPrimary,
            focusedBorderColor = AcademicPrimary,
            unfocusedBorderColor = AcademicBorder
          ),
          singleLine = true
        )
      }

      // Subject Filter Chips
      item {
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          contentPadding = PaddingValues(vertical = 4.dp)
        ) {
          item {
            FilterChipModern(
              label = "All Subjects",
              selected = selectedSubjectFilter == null,
              onClick = { onSubjectFilterChange(null) },
              count = homeworkList.size
            )
          }
          items(subjects) { sub ->
            val count = homeworkList.count { it.homework.subjectId == sub.id }
            FilterChipModern(
              label = sub.name,
              selected = selectedSubjectFilter == sub.id,
              onClick = { onSubjectFilterChange(sub.id) },
              count = count
            )
          }
        }
      }

      // Homework Items List
      if (homeworkList.isEmpty()) {
        item {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            AllCaughtUpIllustration(modifier = Modifier.padding(bottom = 16.dp))
            Text(
              text = "All Caught Up!",
              style = MaterialTheme.typography.titleLarge,
              color = AcademicTextPrimary,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "No pending homework found. Tap + to add assignments for the class.",
              style = MaterialTheme.typography.bodyMedium,
              color = AcademicTextSecondary,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center,
              modifier = Modifier.padding(horizontal = 24.dp)
            )
          }
        }
      } else {
        items(
          items = homeworkList,
          key = { it.homework.id }
        ) { item ->
          Box(
            modifier = Modifier.animateItem(
              placementSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
              )
            )
          ) {
            HomeworkCard(
              item = item,
              onItemClick = { onHomeworkClick(item.homework.id) },
              onToggleCompletion = { onToggleComplete(item.homework.id) },
              showAuthor = true,
              allowCompletion = true
            )
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(64.dp))
      }
    }
  }
}
