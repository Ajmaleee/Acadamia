package com.example.data.model

enum class UserStatus {
  PENDING,
  APPROVED,
  DENIED,
  BLOCKED
}

enum class UserRole {
  STUDENT,
  ADMIN
}

enum class HomeworkStatus {
  ACTIVE,
  PENDING_REVIEW,
  HIDDEN
}

enum class ReportStatus {
  OPEN,
  DISMISSED,
  RESOLVED
}

enum class UrgencyTier(val priority: Int) {
  OVERDUE(0),
  DO_TODAY(1),
  THIS_WEEK(2),
  LATER(3),
  COMPLETED(4)
}

data class User(
  val id: String = "",
  val name: String = "",
  val status: UserStatus = UserStatus.PENDING,
  val role: UserRole = UserRole.STUDENT,
  val createdAt: Long = System.currentTimeMillis(),
  val lastSeenAt: Long = System.currentTimeMillis()
)

data class Subject(
  val id: String = "",
  val name: String = "",
  val colorHex: String = "#4778D0",
  val iconKey: String = "other",
  val orderNum: Int = 0,
  val archived: Boolean = false
)

data class Homework(
  val id: String = "",
  val title: String = "",
  val description: String = "",
  val subjectId: String = "",
  val dueAt: Long = 0L,
  val effortMinutes: Int = 30,
  val authorName: String = "",
  val authorUid: String = "",
  val createdAt: Long = System.currentTimeMillis(),
  val pinned: Boolean = false,
  val status: HomeworkStatus = HomeworkStatus.ACTIVE,
  val reportCount: Int = 0
)

data class Completion(
  val userId: String = "",
  val homeworkId: String = "",
  val completedAt: Long = System.currentTimeMillis()
)

data class UrgencyInfo(
  val tier: UrgencyTier,
  val label: String,
  val startByText: String,
  val hoursLeft: Double,
  val hoursNeeded: Double,
  val isCompleted: Boolean = false
)

data class HomeworkWithDetails(
  val homework: Homework,
  val subject: Subject?,
  val isCompleted: Boolean,
  val urgency: UrgencyInfo
)

data class Report(
  val id: String = "",
  val homeworkId: String = "",
  val reporterUid: String = "",
  val reporterName: String = "",
  val reason: String = "",
  val status: ReportStatus = ReportStatus.OPEN,
  val createdAt: Long = System.currentTimeMillis()
)

data class Announcement(
  val id: String = "",
  val title: String = "",
  val body: String = "",
  val authorUid: String = "",
  val authorName: String = "",
  val pinned: Boolean = false,
  val createdAt: Long = System.currentTimeMillis()
)

data class AppNotification(
  val id: String = "",
  val type: String = "homework",
  val title: String = "",
  val body: String = "",
  val homeworkId: String? = null,
  val createdAt: Long = System.currentTimeMillis(),
  val isRead: Boolean = false
)

data class AppSettings(
  val requireHomeworkApproval: Boolean = false,
  val studentsCanPost: Boolean = true,
  val autoNotifyOnNewHomework: Boolean = true,
  val maxPostsPerDay: Int = 5,
  val blockedWords: String = "curse,swear,badword,spam",
  val className: String = "11A • Academia"
)

data class AuditLog(
  val id: String = "",
  val adminUid: String = "",
  val adminName: String = "",
  val action: String = "",
  val target: String = "",
  val createdAt: Long = System.currentTimeMillis()
)
