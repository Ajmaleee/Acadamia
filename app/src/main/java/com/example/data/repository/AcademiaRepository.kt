package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.R
import com.example.data.local.*
import com.example.data.model.*
import com.example.util.ModerationUtils
import com.example.util.UrgencyEngine
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AcademiaRepository(
  private val context: Context,
  private val externalScope: CoroutineScope
) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("academia_prefs", Context.MODE_PRIVATE)
  private val database = AcademiaDatabase.getDatabase(context, externalScope)
  private val dao = database.appDao()

  // Firestore DB instance using provisioned database ID
  val firestoreDb: FirebaseFirestore by lazy {
    val dbId = context.getString(R.string.firestore_database_id)
    FirebaseFirestore.getInstance(dbId)
  }

  val auth: FirebaseAuth = Firebase.auth

  // Active User ID
  private val _activeUserId = MutableStateFlow<String?>(auth.currentUser?.uid ?: prefs.getString("active_user_id", null))
  val activeUserId: StateFlow<String?> = _activeUserId.asStateFlow()

  val isAuthLoading = MutableStateFlow(true)
  val needsStudentNamePrompt = MutableStateFlow(false)

  // Role preview toggle for admins (default: true for admin mode)
  private val _adminModeActive = MutableStateFlow(true)
  val adminModeActive: StateFlow<Boolean> = _adminModeActive.asStateFlow()

  fun setAdminMode(active: Boolean) {
    _adminModeActive.value = active
  }

  val currentUserFlow: Flow<User?> = _activeUserId.flatMapLatest { uid ->
    if (uid == null) flowOf(null)
    else dao.getUserFlow(uid).map { it?.toDomain() }
  }

  val activeSubjectsFlow: Flow<List<Subject>> = dao.getActiveSubjectsFlow().map { list ->
    list.map { it.toDomain() }
  }

  val allSubjectsFlow: Flow<List<Subject>> = dao.getAllSubjectsFlow().map { list ->
    list.map { it.toDomain() }
  }

  val pendingUserCountFlow: Flow<Int> = dao.getPendingUserCountFlow()

  val pendingUsersFlow: Flow<List<User>> = dao.getPendingUsersFlow().map { list ->
    list.map { it.toDomain() }
  }

  val allUsersFlow: Flow<List<User>> = dao.getAllUsersFlow().map { list ->
    list.map { it.toDomain() }
  }

  val announcementsFlow: Flow<List<Announcement>> = dao.getAnnouncementsFlow().map { list ->
    list.map { it.toDomain() }
  }

  val notificationsFlow: Flow<List<AppNotification>> = dao.getNotificationsFlow().map { list ->
    list.map { it.toDomain() }
  }

  val unreadNotificationsCountFlow: Flow<Int> = dao.getUnreadNotificationsCountFlow()

  val reportsFlow: Flow<List<Report>> = dao.getAllReportsFlow().map { list ->
    list.map { it.toDomain() }
  }

  val auditLogsFlow: Flow<List<AuditLog>> = dao.getAuditLogsFlow().map { list ->
    list.map { it.toDomain() }
  }

  val settingsFlow: Flow<AppSettings> = dao.getAllSettingsFlow().map { list ->
    val map = list.associate { it.key to it.value }
    AppSettings(
      requireHomeworkApproval = map["requireHomeworkApproval"]?.toBooleanStrictOrNull() ?: false,
      studentsCanPost = map["studentsCanPost"]?.toBooleanStrictOrNull() ?: true,
      autoNotifyOnNewHomework = map["autoNotifyOnNewHomework"]?.toBooleanStrictOrNull() ?: true,
      maxPostsPerDay = map["maxPostsPerDay"]?.toIntOrNull() ?: 5,
      blockedWords = map["blockedWords"] ?: "curse,swear,badword,spam",
      className = map["className"] ?: "11A • Academia"
    )
  }

  private var homeworkListener: ListenerRegistration? = null
  private var usersListener: ListenerRegistration? = null
  private var subjectsListener: ListenerRegistration? = null
  private var announcementsListener: ListenerRegistration? = null
  private var reportsListener: ListenerRegistration? = null
  private var completionsListener: ListenerRegistration? = null
  private var settingsListener: ListenerRegistration? = null

  init {
    val cachedUid = auth.currentUser?.uid ?: prefs.getString("active_user_id", null)
    if (cachedUid != null) {
      _activeUserId.value = cachedUid
      startCloudListeners(cachedUid)
      externalScope.launch(Dispatchers.IO) {
        val user = dao.getUser(cachedUid)
        if (user != null) {
          isAuthLoading.value = false
        }
      }
    } else {
      isAuthLoading.value = false
    }

    // Listen for auth state changes
    auth.addAuthStateListener { firebaseAuth ->
      val user = firebaseAuth.currentUser
      if (user != null) {
        _activeUserId.value = user.uid
        prefs.edit().putString("active_user_id", user.uid).apply()
        startCloudListeners(user.uid)
        syncCurrentUserRecord(user.uid, user.email, user.displayName)
      } else {
        stopCloudListeners()
        _activeUserId.value = null
        isAuthLoading.value = false
      }
    }
  }

  private fun syncCurrentUserRecord(uid: String, email: String?, displayName: String?) {
    externalScope.launch(Dispatchers.IO) {
      try {
        val userDoc = firestoreDb.collection("users").document(uid).get().await()
        val isAdminEmail = email?.equals("ajmalsworkshop@gmail.com", ignoreCase = true) == true

        if (userDoc.exists()) {
          val role = if (isAdminEmail) "ADMIN" else (userDoc.getString("role") ?: "STUDENT")
          val status = if (isAdminEmail) "APPROVED" else (userDoc.getString("status") ?: "PENDING")
          val name = userDoc.getString("name") ?: displayName ?: "Student"
          val entity = UserEntity(
            id = uid,
            name = name,
            status = status,
            role = role,
            createdAt = userDoc.getLong("createdAt") ?: System.currentTimeMillis(),
            lastSeenAt = System.currentTimeMillis()
          )
          dao.insertUser(entity)
          prefs.edit().putString("cached_user_status", status).apply()
          needsStudentNamePrompt.value = false
          isAuthLoading.value = false

          // Update last seen in background
          try {
            firestoreDb.collection("users").document(uid).update(
              mapOf(
                "lastSeenAt" to System.currentTimeMillis(),
                "role" to role,
                "status" to status
              )
            )
          } catch (_: Exception) {}
        } else {
          // Document does not exist in Firestore
          if (isAdminEmail) {
            val now = System.currentTimeMillis()
            val entity = UserEntity(
              id = uid,
              name = displayName?.takeIf { it.isNotBlank() } ?: "Class Admin",
              status = "APPROVED",
              role = "ADMIN",
              createdAt = now,
              lastSeenAt = now
            )
            dao.insertUser(entity)
            prefs.edit().putString("cached_user_status", "APPROVED").apply()
            firestoreDb.collection("users").document(uid).set(
              mapOf(
                "id" to uid,
                "name" to entity.name,
                "status" to "APPROVED",
                "role" to "ADMIN",
                "email" to (email ?: ""),
                "createdAt" to now,
                "lastSeenAt" to now
              )
            ).await()
            needsStudentNamePrompt.value = false
            isAuthLoading.value = false
          } else {
            // Check if Room already has a saved user for this uid
            val localUser = dao.getUser(uid)
            if (localUser != null && localUser.name.isNotBlank() && localUser.name != "New Student") {
              prefs.edit().putString("cached_user_status", localUser.status).apply()
              needsStudentNamePrompt.value = false
              isAuthLoading.value = false
              try {
                firestoreDb.collection("users").document(uid).set(
                  mapOf(
                    "id" to uid,
                    "name" to localUser.name,
                    "status" to localUser.status,
                    "role" to localUser.role,
                    "email" to (email ?: ""),
                    "createdAt" to localUser.createdAt,
                    "lastSeenAt" to System.currentTimeMillis()
                  )
                ).await()
              } catch (_: Exception) {}
            } else {
              // Genuinely requires student to submit their name!
              needsStudentNamePrompt.value = true
              isAuthLoading.value = false
            }
          }
        }
      } catch (e: Exception) {
        Log.w("AcademiaRepo", "syncCurrentUserRecord: ${e.message}")
        val localUser = dao.getUser(uid)
        if (localUser != null) {
          prefs.edit().putString("cached_user_status", localUser.status).apply()
          needsStudentNamePrompt.value = false
        }
        isAuthLoading.value = false
      }
    }
  }

  fun startCloudListeners(userId: String) {
    stopCloudListeners()
    val db = firestoreDb

    // 1. Homework listener
    homeworkListener = db.collection("homework")
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.w("AcademiaRepo", "Homework snapshot error: ${error.message}")
          return@addSnapshotListener
        }
        snapshot?.documents?.let { docs ->
          externalScope.launch(Dispatchers.IO) {
            val items = docs.mapNotNull { doc ->
              try {
                HomeworkEntity(
                  id = doc.id,
                  title = doc.getString("title") ?: "",
                  description = doc.getString("description") ?: "",
                  subjectId = doc.getString("subjectId") ?: "",
                  dueAt = doc.getLong("dueAt") ?: 0L,
                  effortMinutes = (doc.getLong("effortMinutes") ?: 30L).toInt(),
                  authorName = doc.getString("authorName") ?: "",
                  authorUid = doc.getString("authorUid") ?: "",
                  createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                  pinned = doc.getBoolean("pinned") ?: false,
                  status = doc.getString("status") ?: "ACTIVE",
                  reportCount = (doc.getLong("reportCount") ?: 0L).toInt()
                )
              } catch (_: Exception) {
                null
              }
            }
            if (items.isNotEmpty()) {
              dao.insertHomeworkList(items)
            }
          }
        }
      }

    // 2. Users listener
    usersListener = db.collection("users")
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.w("AcademiaRepo", "Users snapshot error: ${error.message}")
          return@addSnapshotListener
        }
        snapshot?.documents?.let { docs ->
          externalScope.launch(Dispatchers.IO) {
            val users = docs.mapNotNull { doc ->
              try {
                UserEntity(
                  id = doc.id,
                  name = doc.getString("name") ?: "",
                  status = doc.getString("status") ?: "PENDING",
                  role = doc.getString("role") ?: "STUDENT",
                  createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                  lastSeenAt = doc.getLong("lastSeenAt") ?: System.currentTimeMillis()
                )
              } catch (_: Exception) {
                null
              }
            }
            if (users.isNotEmpty()) {
              dao.insertUsers(users)
              val activeId = _activeUserId.value
              users.firstOrNull { it.id == activeId }?.let { activeUser ->
                prefs.edit().putString("cached_user_status", activeUser.status).apply()
              }
            }
          }
        }
      }

    // 3. Subjects listener
    subjectsListener = db.collection("subjects")
      .addSnapshotListener { snapshot, error ->
        if (error != null) return@addSnapshotListener
        snapshot?.documents?.let { docs ->
          if (docs.isNotEmpty()) {
            externalScope.launch(Dispatchers.IO) {
              val subjects = docs.mapNotNull { doc ->
                try {
                  SubjectEntity(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    colorHex = doc.getString("colorHex") ?: "#4778D0",
                    iconKey = doc.getString("iconKey") ?: "other",
                    orderNum = (doc.getLong("orderNum") ?: 0L).toInt(),
                    archived = doc.getBoolean("archived") ?: false
                  )
                } catch (_: Exception) {
                  null
                }
              }
              dao.insertSubjects(subjects)
            }
          }
        }
      }

    // 4. Announcements listener
    announcementsListener = db.collection("announcements")
      .addSnapshotListener { snapshot, error ->
        if (error != null) return@addSnapshotListener
        snapshot?.documents?.let { docs ->
          externalScope.launch(Dispatchers.IO) {
            val list = docs.mapNotNull { doc ->
              try {
                AnnouncementEntity(
                  id = doc.id,
                  title = doc.getString("title") ?: "",
                  body = doc.getString("body") ?: "",
                  authorUid = doc.getString("authorUid") ?: "",
                  authorName = doc.getString("authorName") ?: "Admin",
                  createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                  pinned = doc.getBoolean("pinned") ?: false
                )
              } catch (_: Exception) {
                null
              }
            }
            if (list.isNotEmpty()) {
              dao.insertAnnouncements(list)
            }
          }
        }
      }

    // 5. Reports listener
    reportsListener = db.collection("reports")
      .addSnapshotListener { snapshot, error ->
        if (error != null) return@addSnapshotListener
        snapshot?.documents?.let { docs ->
          externalScope.launch(Dispatchers.IO) {
            val list = docs.mapNotNull { doc ->
              try {
                ReportEntity(
                  id = doc.id,
                  homeworkId = doc.getString("homeworkId") ?: "",
                  reporterUid = doc.getString("reporterUid") ?: "",
                  reporterName = doc.getString("reporterName") ?: "",
                  reason = doc.getString("reason") ?: "",
                  status = doc.getString("status") ?: "OPEN",
                  createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                )
              } catch (_: Exception) {
                null
              }
            }
            if (list.isNotEmpty()) {
              dao.insertReports(list)
            }
          }
        }
      }

    // 6. User personal completions listener
    completionsListener = db.collection("users").document(userId)
      .collection("completions")
      .addSnapshotListener { snapshot, error ->
        if (error != null) return@addSnapshotListener
        snapshot?.documents?.let { docs ->
          externalScope.launch(Dispatchers.IO) {
            val items = docs.mapNotNull { doc ->
              CompletionEntity(
                userId = userId,
                homeworkId = doc.id,
                completedAt = doc.getLong("completedAt") ?: System.currentTimeMillis()
              )
            }
            if (items.isNotEmpty()) {
              dao.insertCompletions(items)
            }
          }
        }
      }

    // 7. Class settings listener
    settingsListener = db.collection("settings").document("class_info")
      .addSnapshotListener { snapshot, error ->
        if (error != null) return@addSnapshotListener
        snapshot?.let { doc ->
          val className = doc.getString("className")
          if (!className.isNullOrBlank()) {
            externalScope.launch(Dispatchers.IO) {
              dao.insertSetting(AppSettingEntity("className", className))
            }
          }
        }
      }
  }

  fun stopCloudListeners() {
    homeworkListener?.remove()
    homeworkListener = null
    usersListener?.remove()
    usersListener = null
    subjectsListener?.remove()
    subjectsListener = null
    announcementsListener?.remove()
    announcementsListener = null
    reportsListener?.remove()
    reportsListener = null
    completionsListener?.remove()
    completionsListener = null
    settingsListener?.remove()
    settingsListener = null
  }

  fun setActiveUserId(userId: String?) {
    _activeUserId.value = userId
    prefs.edit().apply {
      if (userId == null) remove("active_user_id")
      else putString("active_user_id", userId)
    }.apply()
    if (userId != null) {
      startCloudListeners(userId)
    }
  }

  suspend fun registerStudentProfile(name: String): Result<User> {
    val (valid, error) = ModerationUtils.validateName(name)
    if (!valid) {
      return Result.failure(IllegalArgumentException(error ?: "Invalid name"))
    }

    val currentFirebaseUser = auth.currentUser
      ?: return Result.failure(IllegalStateException("Please sign in with Google first"))

    val trimmedName = ModerationUtils.sanitizeText(name)
    val uid = currentFirebaseUser.uid
    val email = currentFirebaseUser.email
    val isAdmin = email?.equals("ajmalsworkshop@gmail.com", ignoreCase = true) == true
    val role = if (isAdmin) "ADMIN" else "STUDENT"
    val status = if (isAdmin) "APPROVED" else "PENDING"
    val now = System.currentTimeMillis()

    val entity = UserEntity(
      id = uid,
      name = trimmedName,
      status = status,
      role = role,
      createdAt = now,
      lastSeenAt = now
    )
    dao.insertUser(entity)
    setActiveUserId(uid)
    prefs.edit().putString("cached_user_status", status).apply()
    needsStudentNamePrompt.value = false

    try {
      firestoreDb.collection("users").document(uid).set(
        mapOf(
          "id" to uid,
          "name" to trimmedName,
          "status" to status,
          "role" to role,
          "email" to (email ?: ""),
          "createdAt" to now,
          "lastSeenAt" to now
        )
      ).await()
    } catch (e: Exception) {
      Log.w("AcademiaRepo", "Failed to sync user to Firestore: ${e.message}")
    }

    return Result.success(entity.toDomain())
  }

  suspend fun updateClassName(newClassName: String): Result<Unit> {
    val trimmed = newClassName.trim()
    if (trimmed.isBlank()) return Result.failure(IllegalArgumentException("Class name cannot be blank"))
    dao.insertSetting(AppSettingEntity("className", trimmed))
    return try {
      firestoreDb.collection("settings").document("class_info").set(
        mapOf(
          "className" to trimmed,
          "updatedAt" to System.currentTimeMillis(),
          "updatedBy" to (_activeUserId.value ?: "")
        )
      ).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Log.w("AcademiaRepo", "Failed to update className in Firestore: ${e.message}")
      Result.success(Unit)
    }
  }

  fun getHomeworkWithDetailsFlow(
    filterSubjectId: String? = null,
    searchQuery: String = "",
    onlyMyHomework: Boolean = false
  ): Flow<List<HomeworkWithDetails>> {
    val uid = _activeUserId.value ?: ""
    return combine(
      dao.getActiveHomeworkFlow(),
      dao.getAllSubjectsFlow(),
      dao.getUserCompletionsFlow(uid)
    ) { homeworkList, subjectsList, completions ->
      val subjectMap = subjectsList.associateBy { it.id }
      val completedIds = completions.map { it.homeworkId }.toSet()

      val filtered = homeworkList.filter { hw ->
        val matchesSubject = filterSubjectId == null || hw.subjectId == filterSubjectId
        val matchesAuthor = !onlyMyHomework || hw.authorUid == _activeUserId.value
        val matchesSearch = searchQuery.isBlank() ||
            hw.title.contains(searchQuery, ignoreCase = true) ||
            hw.description.contains(searchQuery, ignoreCase = true)
        matchesSubject && matchesAuthor && matchesSearch
      }

      filtered.map { hw ->
        val isCompleted = completedIds.contains(hw.id)
        val urgency = UrgencyEngine.calculateUrgency(hw.dueAt, hw.effortMinutes, isCompleted)
        HomeworkWithDetails(
          homework = hw.toDomain(),
          subject = subjectMap[hw.subjectId]?.toDomain(),
          isCompleted = isCompleted,
          urgency = urgency
        )
      }.sortedWith(
        compareBy<HomeworkWithDetails> { it.isCompleted }
          .thenByDescending { it.homework.pinned }
          .thenBy { it.urgency.tier.priority }
          .thenBy { it.homework.dueAt }
      )
    }.flowOn(Dispatchers.Default)
  }

  fun getAllHomeworkAdminFlow(): Flow<List<HomeworkWithDetails>> {
    return combine(
      dao.getAllHomeworkFlow(),
      dao.getAllSubjectsFlow()
    ) { homeworkList, subjectsList ->
      val subjectMap = subjectsList.associateBy { it.id }
      homeworkList.map { hw ->
        val urgency = UrgencyEngine.calculateUrgency(hw.dueAt, hw.effortMinutes, false)
        HomeworkWithDetails(
          homework = hw.toDomain(),
          subject = subjectMap[hw.subjectId]?.toDomain(),
          isCompleted = false,
          urgency = urgency
        )
      }.sortedWith(
        compareByDescending<HomeworkWithDetails> { it.homework.pinned }
          .thenBy { it.homework.dueAt }
      )
    }.flowOn(Dispatchers.Default)
  }

  suspend fun toggleCompletion(homeworkId: String) {
    val uid = _activeUserId.value ?: return
    val existing = dao.getCompletion(uid, homeworkId)
    if (existing != null) {
      dao.deleteCompletion(uid, homeworkId)
      try {
        firestoreDb.collection("users").document(uid)
          .collection("completions").document(homeworkId).delete()
      } catch (_: Exception) {}
    } else {
      val now = System.currentTimeMillis()
      dao.insertCompletion(CompletionEntity(userId = uid, homeworkId = homeworkId, completedAt = now))
      try {
        firestoreDb.collection("users").document(uid)
          .collection("completions").document(homeworkId).set(
            mapOf("completedAt" to now)
          )
      } catch (_: Exception) {}
    }
  }

  suspend fun addHomework(
    title: String,
    description: String,
    subjectId: String,
    dueAt: Long,
    effortMinutes: Int
  ): Result<String> {
    val cleanTitle = ModerationUtils.sanitizeText(title)
    val cleanDesc = ModerationUtils.sanitizeText(description)

    val (titleValid, titleErr) = ModerationUtils.validateHomeworkTitle(cleanTitle)
    if (!titleValid) return Result.failure(IllegalArgumentException(titleErr))

    val (descValid, descErr) = ModerationUtils.validateDescription(cleanDesc)
    if (!descValid) return Result.failure(IllegalArgumentException(descErr))

    val user = _activeUserId.value?.let { dao.getUser(it) }
    val authorName = user?.name ?: "Student"
    val authorUid = user?.id ?: ""
    val now = System.currentTimeMillis()
    val hwId = "hw_" + UUID.randomUUID().toString().take(8)

    val entity = HomeworkEntity(
      id = hwId,
      title = cleanTitle,
      description = cleanDesc,
      subjectId = subjectId,
      dueAt = dueAt,
      effortMinutes = effortMinutes,
      authorName = authorName,
      authorUid = authorUid,
      createdAt = now,
      pinned = false,
      status = "ACTIVE",
      reportCount = 0
    )

    dao.insertHomework(entity)

    try {
      firestoreDb.collection("homework").document(hwId).set(
        mapOf(
          "id" to hwId,
          "title" to cleanTitle,
          "description" to cleanDesc,
          "subjectId" to subjectId,
          "dueAt" to dueAt,
          "effortMinutes" to effortMinutes,
          "authorName" to authorName,
          "authorUid" to authorUid,
          "createdAt" to now,
          "pinned" to false,
          "status" to "ACTIVE",
          "reportCount" to 0
        )
      ).await()
    } catch (e: Exception) {
      Log.w("AcademiaRepo", "Firestore addHomework sync error: ${e.message}")
    }

    return Result.success(hwId)
  }

  suspend fun deleteHomework(homeworkId: String) {
    dao.deleteHomework(homeworkId)
    try {
      firestoreDb.collection("homework").document(homeworkId).delete()
    } catch (_: Exception) {}
  }

  suspend fun togglePinHomework(homeworkId: String, currentPinned: Boolean) {
    dao.updateHomeworkPinned(homeworkId, !currentPinned)
    try {
      firestoreDb.collection("homework").document(homeworkId).update("pinned", !currentPinned)
    } catch (_: Exception) {}
  }

  suspend fun approveUser(userId: String) {
    dao.updateUserStatus(userId, "APPROVED")
    try {
      firestoreDb.collection("users").document(userId).update("status", "APPROVED")
    } catch (_: Exception) {}
  }

  suspend fun denyUser(userId: String) {
    dao.updateUserStatus(userId, "DENIED")
    try {
      firestoreDb.collection("users").document(userId).update("status", "DENIED")
    } catch (_: Exception) {}
  }

  suspend fun blockUser(userId: String) {
    dao.updateUserStatus(userId, "BLOCKED")
    try {
      firestoreDb.collection("users").document(userId).update("status", "BLOCKED")
    } catch (_: Exception) {}
  }

  suspend fun unblockUser(userId: String) {
    dao.updateUserStatus(userId, "APPROVED")
    try {
      firestoreDb.collection("users").document(userId).update("status", "APPROVED")
    } catch (_: Exception) {}
  }

  suspend fun approveAllPendingUsers() {
    val pending = dao.getPendingUsersFlow().first()
    pending.forEach { user ->
      approveUser(user.id)
    }
  }

  suspend fun addSubject(name: String, colorHex: String, iconKey: String) {
    val subId = "sub_" + UUID.randomUUID().toString().take(6)
    val entity = SubjectEntity(
      id = subId,
      name = name,
      colorHex = colorHex,
      iconKey = iconKey,
      orderNum = 10,
      archived = false
    )
    dao.insertSubject(entity)
    try {
      firestoreDb.collection("subjects").document(subId).set(
        mapOf(
          "id" to subId,
          "name" to name,
          "colorHex" to colorHex,
          "iconKey" to iconKey,
          "orderNum" to 10,
          "archived" to false
        )
      )
    } catch (_: Exception) {}
  }

  suspend fun addAnnouncement(title: String, body: String, pinned: Boolean = false) {
    val id = "ann_" + UUID.randomUUID().toString().take(8)
    val now = System.currentTimeMillis()
    val user = _activeUserId.value?.let { dao.getUser(it) }
    val entity = AnnouncementEntity(
      id = id,
      title = title,
      body = body,
      authorUid = user?.id ?: "",
      authorName = user?.name ?: "Class Admin",
      createdAt = now,
      pinned = pinned
    )
    dao.insertAnnouncement(entity)
    try {
      firestoreDb.collection("announcements").document(id).set(
        mapOf(
          "id" to id,
          "title" to title,
          "body" to body,
          "authorUid" to (user?.id ?: ""),
          "authorName" to (user?.name ?: "Class Admin"),
          "createdAt" to now,
          "pinned" to pinned
        )
      )
    } catch (_: Exception) {}
  }

  suspend fun reportHomework(homeworkId: String, reason: String) {
    val repId = "rep_" + UUID.randomUUID().toString().take(8)
    val now = System.currentTimeMillis()
    val user = _activeUserId.value?.let { dao.getUser(it) }
    val entity = ReportEntity(
      id = repId,
      homeworkId = homeworkId,
      reporterUid = user?.id ?: "",
      reporterName = user?.name ?: "Student",
      reason = reason,
      status = "OPEN",
      createdAt = now
    )
    dao.insertReport(entity)
    dao.incrementReportCount(homeworkId)
    try {
      firestoreDb.collection("reports").document(repId).set(
        mapOf(
          "id" to repId,
          "homeworkId" to homeworkId,
          "reporterUid" to (user?.id ?: ""),
          "reporterName" to (user?.name ?: "Student"),
          "reason" to reason,
          "status" to "OPEN",
          "createdAt" to now
        )
      )
      firestoreDb.collection("homework").document(homeworkId).update(
        "reportCount", FieldValue.increment(1)
      )
    } catch (_: Exception) {}
  }

  suspend fun resolveReport(reportId: String) {
    dao.updateReportStatus(reportId, "RESOLVED")
    try {
      firestoreDb.collection("reports").document(reportId).update("status", "RESOLVED")
    } catch (_: Exception) {}
  }

  suspend fun dismissReport(reportId: String) {
    dao.updateReportStatus(reportId, "DISMISSED")
    try {
      firestoreDb.collection("reports").document(reportId).update("status", "DISMISSED")
    } catch (_: Exception) {}
  }

  fun signOut() {
    auth.signOut()
    setActiveUserId(null)
  }
}
