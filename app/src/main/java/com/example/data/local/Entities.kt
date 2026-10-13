package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.*

@Entity(tableName = "users")
data class UserEntity(
  @PrimaryKey val id: String,
  val name: String,
  val status: String,
  val role: String,
  val createdAt: Long,
  val lastSeenAt: Long
)

fun UserEntity.toDomain(): User = User(
  id = id,
  name = name,
  status = try { UserStatus.valueOf(status) } catch (_: Exception) { UserStatus.PENDING },
  role = try { UserRole.valueOf(role) } catch (_: Exception) { UserRole.STUDENT },
  createdAt = createdAt,
  lastSeenAt = lastSeenAt
)

@Entity(tableName = "subjects")
data class SubjectEntity(
  @PrimaryKey val id: String,
  val name: String,
  val colorHex: String,
  val iconKey: String,
  val orderNum: Int,
  val archived: Boolean
)

fun SubjectEntity.toDomain(): Subject = Subject(
  id = id,
  name = name,
  colorHex = colorHex,
  iconKey = iconKey,
  orderNum = orderNum,
  archived = archived
)

@Entity(tableName = "homework")
data class HomeworkEntity(
  @PrimaryKey val id: String,
  val title: String,
  val description: String,
  val subjectId: String,
  val dueAt: Long,
  val effortMinutes: Int,
  val authorName: String,
  val authorUid: String,
  val createdAt: Long,
  val pinned: Boolean,
  val status: String,
  val reportCount: Int
)

fun HomeworkEntity.toDomain(): Homework = Homework(
  id = id,
  title = title,
  description = description,
  subjectId = subjectId,
  dueAt = dueAt,
  effortMinutes = effortMinutes,
  authorName = authorName,
  authorUid = authorUid,
  createdAt = createdAt,
  pinned = pinned,
  status = try { HomeworkStatus.valueOf(status) } catch (_: Exception) { HomeworkStatus.ACTIVE },
  reportCount = reportCount
)

@Entity(tableName = "completions", primaryKeys = ["userId", "homeworkId"])
data class CompletionEntity(
  val userId: String,
  val homeworkId: String,
  val completedAt: Long
)

fun CompletionEntity.toDomain(): Completion = Completion(
  userId = userId,
  homeworkId = homeworkId,
  completedAt = completedAt
)

@Entity(tableName = "reports")
data class ReportEntity(
  @PrimaryKey val id: String,
  val homeworkId: String,
  val reporterUid: String,
  val reporterName: String,
  val reason: String,
  val status: String,
  val createdAt: Long
)

fun ReportEntity.toDomain(): Report = Report(
  id = id,
  homeworkId = homeworkId,
  reporterUid = reporterUid,
  reporterName = reporterName,
  reason = reason,
  status = try { ReportStatus.valueOf(status) } catch (_: Exception) { ReportStatus.OPEN },
  createdAt = createdAt
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
  @PrimaryKey val id: String,
  val title: String,
  val body: String,
  val authorUid: String,
  val authorName: String,
  val pinned: Boolean,
  val createdAt: Long
)

fun AnnouncementEntity.toDomain(): Announcement = Announcement(
  id = id,
  title = title,
  body = body,
  authorUid = authorUid,
  authorName = authorName,
  pinned = pinned,
  createdAt = createdAt
)

@Entity(tableName = "notifications")
data class NotificationEntity(
  @PrimaryKey val id: String,
  val type: String,
  val title: String,
  val body: String,
  val homeworkId: String?,
  val createdAt: Long,
  val isRead: Boolean
)

fun NotificationEntity.toDomain(): AppNotification = AppNotification(
  id = id,
  type = type,
  title = title,
  body = body,
  homeworkId = homeworkId,
  createdAt = createdAt,
  isRead = isRead
)

@Entity(tableName = "settings")
data class AppSettingEntity(
  @PrimaryKey val key: String,
  val value: String
)

@Entity(tableName = "audit_log")
data class AuditLogEntity(
  @PrimaryKey val id: String,
  val adminUid: String,
  val adminName: String,
  val action: String,
  val target: String,
  val createdAt: Long
)

fun AuditLogEntity.toDomain(): AuditLog = AuditLog(
  id = id,
  adminUid = adminUid,
  adminName = adminName,
  action = action,
  target = target,
  createdAt = createdAt
)
