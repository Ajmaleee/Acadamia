package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.model.UserRole
import com.example.data.model.UserStatus
import com.example.data.repository.AcademiaRepository
import com.example.ui.AcademiaViewModel
import com.example.ui.Screen
import com.example.ui.screens.*
import com.example.ui.theme.AcademiaTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MainActivity : ComponentActivity() {
  private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val repository = AcademiaRepository(applicationContext, appScope)

    setContent {
      AcademiaTheme {
        val viewModel: AcademiaViewModel = viewModel(
          factory = viewModelFactory {
            initializer {
              AcademiaViewModel(repository)
            }
          }
        )

        val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
        val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
        val isAuthLoading by viewModel.isAuthLoading.collectAsStateWithLifecycle()
        val needsStudentNamePrompt by viewModel.needsStudentNamePrompt.collectAsStateWithLifecycle()
        val settings by viewModel.settings.collectAsStateWithLifecycle()

        val firebaseUser = Firebase.auth.currentUser

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (isAuthLoading) {
              AuthLoadingScreen()
            } else if (firebaseUser == null) {
              // Not signed in with Google -> Show Google Sign-in screen (no name entry allowed before Google sign-in)
              WelcomeScreen()
            } else if (needsStudentNamePrompt) {
              // Signed in with Google, but needs to enter their real student name
              EnterStudentNameScreen(
                googleUser = firebaseUser,
                onSubmitName = { name -> viewModel.registerStudentName(name) },
                onSignOut = { viewModel.signOut() }
              )
            } else {
              val user = currentUser
              if (user == null) {
                // If user entity is still resolving, show waiting or loading
                AuthLoadingScreen()
              } else {
                when (user.status) {
                  UserStatus.PENDING -> {
                    // Waiting for approval -> Show ONLY this screen, never ask them to log in again
                    WaitingApprovalScreen(
                      user = user,
                      onSignOut = { viewModel.signOut() }
                    )
                  }
                  UserStatus.DENIED, UserStatus.BLOCKED -> {
                    AccessDeniedScreen(
                      user = user,
                      onSignOut = { viewModel.signOut() }
                    )
                  }
                  UserStatus.APPROVED -> {
                    val isAdmin = user.role == UserRole.ADMIN
                    val adminModeActive by viewModel.adminModeActive.collectAsStateWithLifecycle()

                    // If user is admin and on initial screen, show AdminPanel by default
                    val effectiveScreen = if (isAdmin && adminModeActive && currentScreen is Screen.Dashboard) {
                      Screen.AdminPanel
                    } else {
                      currentScreen
                    }

                    val studentHomework by viewModel.studentHomework.collectAsStateWithLifecycle()
                    val adminHomework by viewModel.adminHomework.collectAsStateWithLifecycle()
                    val activeSubjects by viewModel.activeSubjects.collectAsStateWithLifecycle()
                    val allSubjects by viewModel.allSubjects.collectAsStateWithLifecycle()
                    val pendingUsers by viewModel.pendingUsers.collectAsStateWithLifecycle()
                    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
                    val announcements by viewModel.announcements.collectAsStateWithLifecycle()
                    val reports by viewModel.reports.collectAsStateWithLifecycle()
                    val selectedFilter by viewModel.selectedSubjectFilter.collectAsStateWithLifecycle()
                    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

                    // iOS style tactile spring bounce screen transitions
                    AnimatedContent(
                      targetState = effectiveScreen,
                      transitionSpec = {
                        (slideInHorizontally(
                          animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                          ),
                          initialOffsetX = { it }
                        ) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)))
                          .togetherWith(
                            slideOutHorizontally(
                              animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMedium
                              ),
                              targetOffsetX = { -it / 3 }
                            ) + fadeOut()
                          )
                      },
                      label = "screen_transition"
                    ) { screen ->
                      when (screen) {
                        is Screen.AdminPanel -> {
                          AdminPanelScreen(
                            pendingUsers = pendingUsers,
                            allUsers = allUsers,
                            homeworkList = adminHomework,
                            subjects = allSubjects,
                            announcements = announcements,
                            reports = reports,
                            onApproveUser = { viewModel.approveUser(it) },
                            onDenyUser = { viewModel.denyUser(it) },
                            onBlockUser = { viewModel.blockUser(it) },
                            onUnblockUser = { viewModel.unblockUser(it) },
                            onApproveAllPending = { viewModel.approveAllPending() },
                            onDeleteHomework = { viewModel.deleteHomework(it) },
                            onTogglePinHomework = { id, pinned -> viewModel.togglePin(id, pinned) },
                            onAddSubject = { name, color, icon -> viewModel.addSubject(name, color, icon) },
                            onAddAnnouncement = { title, body -> viewModel.addAnnouncement(title, body) },
                            onResolveReport = { viewModel.resolveReport(it) },
                            onDismissReport = { viewModel.dismissReport(it) },
                            className = settings.className,
                            onUpdateClassName = { viewModel.updateClassName(it) },
                            onNavigateBack = {
                              if (isAdmin) {
                                viewModel.toggleAdminMode(false)
                              } else {
                                viewModel.navigateBack()
                              }
                            }
                          )
                        }
                        is Screen.Dashboard -> {
                          StudentDashboardScreen(
                            user = user,
                            homeworkList = studentHomework,
                            subjects = activeSubjects,
                            selectedSubjectFilter = selectedFilter,
                            searchQuery = searchQuery,
                            onSubjectFilterChange = { viewModel.setSubjectFilter(it) },
                            onSearchQueryChange = { viewModel.setSearchQuery(it) },
                            onToggleComplete = { viewModel.toggleCompletion(it) },
                            onHomeworkClick = { viewModel.navigateTo(Screen.HomeworkDetail(it)) },
                            onNavigateToAddHomework = { viewModel.navigateTo(Screen.AddHomework) },
                            onNavigateToCalendar = { viewModel.navigateTo(Screen.Calendar) },
                            onNavigateToAnnouncements = { viewModel.navigateTo(Screen.Announcements) },
                            onNavigateToProfile = { viewModel.navigateTo(Screen.Profile) },
                            onNavigateToAdminPanel = { viewModel.toggleAdminMode(true) },
                            className = settings.className
                          )
                        }
                        is Screen.AddHomework -> {
                          AddHomeworkScreen(
                            user = user,
                            subjects = activeSubjects,
                            onAddHomework = { title, desc, subjectId, dueAt, effort ->
                              viewModel.addHomework(
                                title = title,
                                description = desc,
                                subjectId = subjectId,
                                dueAt = dueAt,
                                effortMinutes = effort,
                                onSuccess = {},
                                onError = {}
                              )
                            },
                            onNavigateBack = { viewModel.navigateBack() }
                          )
                        }
                        is Screen.HomeworkDetail -> {
                          val detailItem = studentHomework.firstOrNull { it.homework.id == screen.homeworkId }
                            ?: adminHomework.firstOrNull { it.homework.id == screen.homeworkId }
                          HomeworkDetailScreen(
                            item = detailItem,
                            user = user,
                            onToggleComplete = { viewModel.toggleCompletion(screen.homeworkId) },
                            onReportHomework = { reason -> viewModel.reportHomework(screen.homeworkId, reason) },
                            onNavigateBack = { viewModel.navigateBack() }
                          )
                        }
                        is Screen.Calendar -> {
                          CalendarScreen(
                            homeworkList = studentHomework,
                            onHomeworkClick = { viewModel.navigateTo(Screen.HomeworkDetail(it)) },
                            onToggleComplete = { viewModel.toggleCompletion(it) },
                            onNavigateBack = { viewModel.navigateBack() }
                          )
                        }
                        is Screen.Announcements -> {
                          AnnouncementsScreen(
                            announcements = announcements,
                            onNavigateBack = { viewModel.navigateBack() }
                          )
                        }
                        is Screen.Profile -> {
                          ProfileScreen(
                            user = user,
                            isAdmin = isAdmin,
                            className = settings.className,
                            onUpdateClassName = { viewModel.updateClassName(it) },
                            onToggleAdminMode = { viewModel.toggleAdminMode(it) },
                            onSignOut = { viewModel.signOut() },
                            onNavigateBack = { viewModel.navigateBack() }
                          )
                        }
                        else -> {
                          StudentDashboardScreen(
                            user = user,
                            homeworkList = studentHomework,
                            subjects = activeSubjects,
                            selectedSubjectFilter = selectedFilter,
                            searchQuery = searchQuery,
                            onSubjectFilterChange = { viewModel.setSubjectFilter(it) },
                            onSearchQueryChange = { viewModel.setSearchQuery(it) },
                            onToggleComplete = { viewModel.toggleCompletion(it) },
                            onHomeworkClick = { viewModel.navigateTo(Screen.HomeworkDetail(it)) },
                            onNavigateToAddHomework = { viewModel.navigateTo(Screen.AddHomework) },
                            onNavigateToCalendar = { viewModel.navigateTo(Screen.Calendar) },
                            onNavigateToAnnouncements = { viewModel.navigateTo(Screen.Announcements) },
                            onNavigateToProfile = { viewModel.navigateTo(Screen.Profile) },
                            onNavigateToAdminPanel = { viewModel.toggleAdminMode(true) },
                            className = settings.className
                          )
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
