package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.AcademiaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface Screen {
  data object Dashboard : Screen
  data object AdminPanel : Screen
  data object AddHomework : Screen
  data class HomeworkDetail(val homeworkId: String) : Screen
  data object Calendar : Screen
  data object Subjects : Screen
  data object CompletedArchive : Screen
  data object Announcements : Screen
  data object Profile : Screen
}

class AcademiaViewModel(
  private val repository: AcademiaRepository
) : ViewModel() {

  private val _screenStack = MutableStateFlow<List<Screen>>(listOf(Screen.Dashboard))
  val currentScreen: StateFlow<Screen> = _screenStack.map { it.lastOrNull() ?: Screen.Dashboard }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Screen.Dashboard)

  val currentUser: StateFlow<User?> = repository.currentUserFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val activeSubjects: StateFlow<List<Subject>> = repository.activeSubjectsFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allSubjects: StateFlow<List<Subject>> = repository.allSubjectsFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Student Filter & Search
  private val _selectedSubjectFilter = MutableStateFlow<String?>(null)
  val selectedSubjectFilter: StateFlow<String?> = _selectedSubjectFilter.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  // Student Urgency-ranked homework flow
  val studentHomework: StateFlow<List<HomeworkWithDetails>> = combine(
    _selectedSubjectFilter,
    _searchQuery
  ) { filter, query ->
    Pair(filter, query)
  }.flatMapLatest { (filter, query) ->
    repository.getHomeworkWithDetailsFlow(filterSubjectId = filter, searchQuery = query)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Admin class-wide homework flow
  val adminHomework: StateFlow<List<HomeworkWithDetails>> = repository.getAllHomeworkAdminFlow()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val pendingUsers: StateFlow<List<User>> = repository.pendingUsersFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val pendingUserCount: StateFlow<Int> = repository.pendingUserCountFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  val allUsers: StateFlow<List<User>> = repository.allUsersFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val announcements: StateFlow<List<Announcement>> = repository.announcementsFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val reports: StateFlow<List<Report>> = repository.reportsFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val settings: StateFlow<AppSettings> = repository.settingsFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

  val adminModeActive: StateFlow<Boolean> = repository.adminModeActive

  val isAuthLoading: StateFlow<Boolean> = repository.isAuthLoading
  val needsStudentNamePrompt: StateFlow<Boolean> = repository.needsStudentNamePrompt

  // Actions
  fun registerStudentName(name: String) {
    viewModelScope.launch {
      repository.registerStudentProfile(name)
    }
  }

  fun updateClassName(newClassName: String) {
    viewModelScope.launch {
      repository.updateClassName(newClassName)
    }
  }

  fun navigateTo(screen: Screen) {
    _screenStack.value = _screenStack.value + screen
  }

  fun navigateBack(): Boolean {
    if (_screenStack.value.size > 1) {
      _screenStack.value = _screenStack.value.dropLast(1)
      return true
    }
    return false
  }

  fun setSubjectFilter(subjectId: String?) {
    _selectedSubjectFilter.value = subjectId
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun toggleCompletion(homeworkId: String) {
    viewModelScope.launch {
      repository.toggleCompletion(homeworkId)
    }
  }

  fun addHomework(
    title: String,
    description: String,
    subjectId: String,
    dueAt: Long,
    effortMinutes: Int,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      val result = repository.addHomework(title, description, subjectId, dueAt, effortMinutes)
      result.fold(
        onSuccess = {
          navigateBack()
          onSuccess()
        },
        onFailure = { onError(it.message ?: "Failed to add homework") }
      )
    }
  }

  fun deleteHomework(homeworkId: String) {
    viewModelScope.launch {
      repository.deleteHomework(homeworkId)
    }
  }

  fun togglePin(homeworkId: String, currentPinned: Boolean) {
    viewModelScope.launch {
      repository.togglePinHomework(homeworkId, currentPinned)
    }
  }

  fun approveUser(userId: String) {
    viewModelScope.launch {
      repository.approveUser(userId)
    }
  }

  fun denyUser(userId: String) {
    viewModelScope.launch {
      repository.denyUser(userId)
    }
  }

  fun blockUser(userId: String) {
    viewModelScope.launch {
      repository.blockUser(userId)
    }
  }

  fun unblockUser(userId: String) {
    viewModelScope.launch {
      repository.unblockUser(userId)
    }
  }

  fun approveAllPending() {
    viewModelScope.launch {
      repository.approveAllPendingUsers()
    }
  }

  fun addSubject(name: String, colorHex: String, iconKey: String) {
    viewModelScope.launch {
      repository.addSubject(name, colorHex, iconKey)
    }
  }

  fun addAnnouncement(title: String, body: String, pinned: Boolean = false) {
    viewModelScope.launch {
      repository.addAnnouncement(title, body, pinned)
    }
  }

  fun reportHomework(homeworkId: String, reason: String) {
    viewModelScope.launch {
      repository.reportHomework(homeworkId, reason)
    }
  }

  fun resolveReport(reportId: String) {
    viewModelScope.launch {
      repository.resolveReport(reportId)
    }
  }

  fun dismissReport(reportId: String) {
    viewModelScope.launch {
      repository.dismissReport(reportId)
    }
  }

  fun toggleAdminMode(active: Boolean) {
    repository.setAdminMode(active)
    if (active) {
      navigateTo(Screen.AdminPanel)
    } else {
      navigateTo(Screen.Dashboard)
    }
  }

  fun signOut() {
    repository.signOut()
    _screenStack.value = listOf(Screen.Dashboard)
  }
}
