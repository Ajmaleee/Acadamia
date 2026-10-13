package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.data.model.*
import com.example.ui.components.HomeworkCard
import com.example.ui.illustrations.SubjectIconBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
  pendingUsers: List<User>,
  allUsers: List<User>,
  homeworkList: List<HomeworkWithDetails>,
  subjects: List<Subject>,
  announcements: List<Announcement>,
  reports: List<Report>,
  onApproveUser: (String) -> Unit,
  onDenyUser: (String) -> Unit,
  onBlockUser: (String) -> Unit,
  onUnblockUser: (String) -> Unit,
  onApproveAllPending: () -> Unit,
  onDeleteHomework: (String) -> Unit,
  onTogglePinHomework: (String, Boolean) -> Unit,
  onAddSubject: (String, String, String) -> Unit,
  onAddAnnouncement: (String, String) -> Unit,
  onResolveReport: (String) -> Unit,
  onDismissReport: (String) -> Unit,
  onNavigateBack: () -> Unit,
  className: String = "11A • Academia",
  onUpdateClassName: (String) -> Unit = {},
  modifier: Modifier = Modifier
) {
  BackHandler(onBack = onNavigateBack)

  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Requests", "Students", "Homework", "Subjects", "Announce", "Reports")

  // Class dialog state
  var showEditClassDialog by remember { mutableStateOf(false) }
  var editedClassText by remember { mutableStateOf(className) }

  // Subject dialog state
  var showAddSubjectDialog by remember { mutableStateOf(false) }
  var newSubjectName by remember { mutableStateOf("") }
  var newSubjectColor by remember { mutableStateOf("#4778D0") }
  var newSubjectIcon by remember { mutableStateOf("math") }

  // Announcement dialog state
  var showAddAnnounceDialog by remember { mutableStateOf(false) }
  var newAnnounceTitle by remember { mutableStateOf("") }
  var newAnnounceBody by remember { mutableStateOf("") }

  if (showEditClassDialog) {
    AlertDialog(
      onDismissRequest = { showEditClassDialog = false },
      title = { Text("Edit Class Identifier", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Update the class name or section:", style = MaterialTheme.typography.bodyMedium)
          OutlinedTextField(
            value = editedClassText,
            onValueChange = { editedClassText = it },
            placeholder = { Text("e.g. 11A or Class 11A") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (editedClassText.isNotBlank()) {
              onUpdateClassName(editedClassText)
              showEditClassDialog = false
            }
          }
        ) {
          Text("Save")
        }
      },
      dismissButton = {
        TextButton(onClick = { showEditClassDialog = false }) { Text("Cancel") }
      }
    )
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Administrator Control",
              style = MaterialTheme.typography.titleMedium,
              color = AcademicTextPrimary,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "$className Management • Live Backend",
              style = MaterialTheme.typography.labelSmall,
              color = AcademicPrimary
            )
          }
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
          IconButton(onClick = {
            editedClassText = className
            showEditClassDialog = true
          }) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit Class",
              tint = AcademicPrimary
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
    ) {
      // Metric Summary Badges
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        MetricBadge(
          label = "Pending",
          value = pendingUsers.size.toString(),
          color = if (pendingUsers.isNotEmpty()) AcademicWarning else AcademicTextSecondary,
          modifier = Modifier.weight(1f)
        )
        MetricBadge(
          label = "Students",
          value = allUsers.size.toString(),
          color = AcademicPrimary,
          modifier = Modifier.weight(1f)
        )
        MetricBadge(
          label = "Homework",
          value = homeworkList.size.toString(),
          color = AcademicPrimaryDark,
          modifier = Modifier.weight(1f)
        )
        MetricBadge(
          label = "Reports",
          value = reports.count { it.status.name == "OPEN" }.toString(),
          color = if (reports.any { it.status.name == "OPEN" }) AcademicError else AcademicSuccess,
          modifier = Modifier.weight(1f)
        )
      }

      // Scrollable Tab Row
      ScrollableTabRow(
        selectedTabIndex = selectedTab,
        containerColor = AcademicBgSecondary,
        contentColor = AcademicPrimary,
        edgePadding = 16.dp,
        divider = {}
      ) {
        tabs.forEachIndexed { index, title ->
          Tab(
            selected = selectedTab == index,
            onClick = { selectedTab = index },
            text = {
              Text(
                text = title,
                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp
              )
            }
          )
        }
      }

      HorizontalDivider(color = AcademicBorder)

      // Tab Content
      Box(modifier = Modifier.weight(1f)) {
        when (selectedTab) {
          0 -> PendingRequestsTab(
            pendingUsers = pendingUsers,
            onApprove = onApproveUser,
            onDeny = onDenyUser,
            onApproveAll = onApproveAllPending
          )
          1 -> StudentsRosterTab(
            users = allUsers,
            onBlock = onBlockUser,
            onUnblock = onUnblockUser
          )
          2 -> HomeworkManagementTab(
            homeworkList = homeworkList,
            onDelete = onDeleteHomework,
            onTogglePin = onTogglePinHomework
          )
          3 -> SubjectsManagementTab(
            subjects = subjects,
            onOpenAddDialog = { showAddSubjectDialog = true }
          )
          4 -> AnnouncementsTab(
            announcements = announcements,
            onOpenAddDialog = { showAddAnnounceDialog = true }
          )
          5 -> ReportsQueueTab(
            reports = reports,
            onResolve = onResolveReport,
            onDismiss = onDismissReport
          )
        }
      }
    }
  }

  // Add Subject Dialog
  if (showAddSubjectDialog) {
    AlertDialog(
      onDismissRequest = { showAddSubjectDialog = false },
      title = { Text("Add New Subject", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          OutlinedTextField(
            value = newSubjectName,
            onValueChange = { newSubjectName = it },
            label = { Text("Subject Name") },
            placeholder = { Text("e.g. Physics") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          Text("Color Accent", style = MaterialTheme.typography.labelSmall)
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val colors = listOf("#4778D0", "#38A169", "#805AD5", "#DD6B20", "#319795", "#D53F8C")
            colors.forEach { hex ->
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(Color(android.graphics.Color.parseColor(hex)))
                  .clickable { newSubjectColor = hex }
                  .then(
                    if (newSubjectColor == hex) {
                      Modifier.border(2.dp, AcademicPrimaryDark, CircleShape)
                    } else Modifier
                  )
              )
            }
          }

          Text("Icon Key", style = MaterialTheme.typography.labelSmall)
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val icons = listOf("math", "science", "english", "social", "cs", "art")
            icons.forEach { ic ->
              FilterChip(
                selected = newSubjectIcon == ic,
                onClick = { newSubjectIcon = ic },
                label = { Text(ic.capitalize()) }
              )
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newSubjectName.isNotBlank()) {
              onAddSubject(newSubjectName.trim(), newSubjectColor, newSubjectIcon)
              newSubjectName = ""
              showAddSubjectDialog = false
            }
          }
        ) {
          Text("Create")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddSubjectDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Add Announcement Dialog
  if (showAddAnnounceDialog) {
    AlertDialog(
      onDismissRequest = { showAddAnnounceDialog = false },
      title = { Text("Broadcast Announcement", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          OutlinedTextField(
            value = newAnnounceTitle,
            onValueChange = { newAnnounceTitle = it },
            label = { Text("Title") },
            placeholder = { Text("e.g. Science Fair Submission") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = newAnnounceBody,
            onValueChange = { newAnnounceBody = it },
            label = { Text("Message") },
            placeholder = { Text("Important reminder for all students...") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newAnnounceTitle.isNotBlank() && newAnnounceBody.isNotBlank()) {
              onAddAnnouncement(newAnnounceTitle.trim(), newAnnounceBody.trim())
              newAnnounceTitle = ""
              newAnnounceBody = ""
              showAddAnnounceDialog = false
            }
          }
        ) {
          Text("Broadcast")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddAnnounceDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun MetricBadge(
  label: String,
  value: String,
  color: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    color = AcademicBgPrimary,
    border = BorderStroke(1.dp, AcademicBorder)
  ) {
    Column(
      modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
      Text(text = label, fontSize = 11.sp, color = AcademicTextSecondary)
    }
  }
}

@Composable
fun PendingRequestsTab(
  pendingUsers: List<User>,
  onApprove: (String) -> Unit,
  onDeny: (String) -> Unit,
  onApproveAll: () -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    if (pendingUsers.isNotEmpty()) {
      item {
        Button(
          onClick = onApproveAll,
          colors = ButtonDefaults.buttonColors(containerColor = AcademicSuccess),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().testTag("approve_all_button")
        ) {
          Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Approve All (${pendingUsers.size})")
        }
      }
    }

    if (pendingUsers.isEmpty()) {
      item {
        Box(
          modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
          contentAlignment = Alignment.Center
        ) {
          Text("No pending join requests.", color = AcademicTextSecondary)
        }
      }
    } else {
      items(pendingUsers, key = { it.id }) { user ->
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = AcademicBgPrimary,
          border = BorderStroke(1.dp, AcademicBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(AcademicWarningBg),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = user.name.take(1).uppercase(),
                color = AcademicWarning,
                fontWeight = FontWeight.Bold
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(text = user.name, fontWeight = FontWeight.Bold, color = AcademicTextPrimary)
              Text(text = "Requested to join", fontSize = 12.sp, color = AcademicTextSecondary)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              FilledTonalIconButton(
                onClick = { onDeny(user.id) },
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                  containerColor = AcademicErrorBg,
                  contentColor = AcademicError
                )
              ) {
                Icon(Icons.Default.Close, contentDescription = "Decline")
              }
              FilledIconButton(
                onClick = { onApprove(user.id) },
                colors = IconButtonDefaults.filledIconButtonColors(
                  containerColor = AcademicSuccess,
                  contentColor = Color.White
                )
              ) {
                Icon(Icons.Default.Check, contentDescription = "Approve")
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun StudentsRosterTab(
  users: List<User>,
  onBlock: (String) -> Unit,
  onUnblock: (String) -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    items(users, key = { it.id }) { user ->
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = AcademicBgPrimary,
        border = BorderStroke(1.dp, AcademicBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(if (user.role.name == "ADMIN") AcademicPrimaryDark else AcademicPaleBlue),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = user.name.take(1).uppercase(),
              color = if (user.role.name == "ADMIN") Color.White else AcademicPrimary,
              fontWeight = FontWeight.Bold
            )
          }

          Column(modifier = Modifier.weight(1f)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(text = user.name, fontWeight = FontWeight.Bold, color = AcademicTextPrimary)
              if (user.role.name == "ADMIN") {
                Surface(
                  color = AcademicPrimaryLight,
                  shape = RoundedCornerShape(6.dp)
                ) {
                  Text(
                    text = "ADMIN",
                    color = AcademicPrimaryDark,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }
            Text(text = "Status: ${user.status.name}", fontSize = 12.sp, color = AcademicTextSecondary)
          }

          if (user.role.name != "ADMIN") {
            if (user.status.name == "BLOCKED") {
              OutlinedButton(
                onClick = { onUnblock(user.id) },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
              ) {
                Text("Unblock", fontSize = 12.sp)
              }
            } else {
              OutlinedButton(
                onClick = { onBlock(user.id) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AcademicError),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
              ) {
                Text("Block", fontSize = 12.sp)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun HomeworkManagementTab(
  homeworkList: List<HomeworkWithDetails>,
  onDelete: (String) -> Unit,
  onTogglePin: (String, Boolean) -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    if (homeworkList.isEmpty()) {
      item {
        Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
          Text("No assignments in class repository.", color = AcademicTextSecondary)
        }
      }
    } else {
      items(homeworkList, key = { it.homework.id }) { item ->
        val hw = item.homework
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = AcademicBgPrimary,
          border = BorderStroke(1.dp, AcademicBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = item.subject?.name ?: "General",
                style = MaterialTheme.typography.labelSmall,
                color = AcademicPrimary,
                fontWeight = FontWeight.Bold
              )

              Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = { onTogglePin(hw.id, hw.pinned) }) {
                  Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = "Pin",
                    tint = if (hw.pinned) AcademicWarning else AcademicTextMuted
                  )
                }
                IconButton(onClick = { onDelete(hw.id) }) {
                  Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = AcademicError
                  )
                }
              }
            }

            Text(text = hw.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AcademicTextPrimary)
            if (hw.description.isNotBlank()) {
              Text(text = hw.description, fontSize = 13.sp, color = AcademicTextSecondary)
            }

            Row(
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(text = "Author: ${hw.authorName}", fontSize = 12.sp, color = AcademicTextMuted)
              Text(text = "${hw.effortMinutes} min est.", fontSize = 12.sp, color = AcademicTextMuted)
            }
          }
        }
      }
    }
  }
}

@Composable
fun SubjectsManagementTab(
  subjects: List<Subject>,
  onOpenAddDialog: () -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Button(
        onClick = onOpenAddDialog,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Icon(Icons.Default.Add, contentDescription = null)
        Spacer(modifier = Modifier.width(6.dp))
        Text("Add New Subject")
      }
    }

    items(subjects, key = { it.id }) { sub ->
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = AcademicBgPrimary,
        border = BorderStroke(1.dp, AcademicBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          SubjectIconBadge(iconKey = sub.iconKey, colorHex = sub.colorHex, sizeDp = 40.dp)
          Column(modifier = Modifier.weight(1f)) {
            Text(text = sub.name, fontWeight = FontWeight.Bold, color = AcademicTextPrimary)
            Text(text = "Icon: ${sub.iconKey} • Hex: ${sub.colorHex}", fontSize = 12.sp, color = AcademicTextSecondary)
          }
        }
      }
    }
  }
}

@Composable
fun AnnouncementsTab(
  announcements: List<Announcement>,
  onOpenAddDialog: () -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Button(
        onClick = onOpenAddDialog,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Icon(Icons.Default.Campaign, contentDescription = null)
        Spacer(modifier = Modifier.width(6.dp))
        Text("New Class Announcement")
      }
    }

    if (announcements.isEmpty()) {
      item {
        Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
          Text("No announcements broadcast yet.", color = AcademicTextSecondary)
        }
      }
    } else {
      items(announcements, key = { it.id }) { ann ->
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = AcademicBgPrimary,
          border = BorderStroke(1.dp, AcademicBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = ann.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AcademicTextPrimary)
            Text(text = ann.body, fontSize = 14.sp, color = AcademicTextSecondary)
            Text(text = "By ${ann.authorName}", fontSize = 12.sp, color = AcademicTextMuted)
          }
        }
      }
    }
  }
}

@Composable
fun ReportsQueueTab(
  reports: List<Report>,
  onResolve: (String) -> Unit,
  onDismiss: (String) -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    if (reports.isEmpty()) {
      item {
        Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
          Text("Zero reported assignments. Class content is clean!", color = AcademicSuccess)
        }
      }
    } else {
      items(reports, key = { it.id }) { rep ->
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = AcademicBgPrimary,
          border = BorderStroke(1.dp, AcademicBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(text = "Status: ${rep.status.name}", fontWeight = FontWeight.Bold, color = AcademicWarning)
              Text(text = "By ${rep.reporterName}", fontSize = 12.sp, color = AcademicTextSecondary)
            }
            Text(text = "Reason: ${rep.reason}", fontSize = 14.sp, color = AcademicTextPrimary)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Button(
                onClick = { onResolve(rep.id) },
                colors = ButtonDefaults.buttonColors(containerColor = AcademicSuccess)
              ) {
                Text("Resolve")
              }
              OutlinedButton(onClick = { onDismiss(rep.id) }) {
                Text("Dismiss")
              }
            }
          }
        }
      }
    }
  }
}
