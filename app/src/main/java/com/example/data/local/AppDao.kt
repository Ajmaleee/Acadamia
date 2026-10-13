package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
  // Users
  @Query("SELECT * FROM users WHERE id = :userId")
  fun getUserFlow(userId: String): Flow<UserEntity?>

  @Query("SELECT * FROM users WHERE id = :userId")
  suspend fun getUser(userId: String): UserEntity?

  @Query("SELECT * FROM users ORDER BY createdAt DESC")
  fun getAllUsersFlow(): Flow<List<UserEntity>>

  @Query("SELECT * FROM users WHERE status = 'PENDING' ORDER BY createdAt ASC")
  fun getPendingUsersFlow(): Flow<List<UserEntity>>

  @Query("SELECT COUNT(*) FROM users WHERE status = 'PENDING'")
  fun getPendingUserCountFlow(): Flow<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUser(user: UserEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUsers(users: List<UserEntity>)

  @Update
  suspend fun updateUser(user: UserEntity)

  @Query("UPDATE users SET status = :status WHERE id = :userId")
  suspend fun updateUserStatus(userId: String, status: String)

  @Query("UPDATE users SET name = :newName WHERE id = :userId")
  suspend fun updateUserName(userId: String, newName: String)

  @Query("DELETE FROM users WHERE id = :userId")
  suspend fun deleteUser(userId: String)

  // Subjects
  @Query("SELECT * FROM subjects WHERE archived = 0 ORDER BY orderNum ASC, name ASC")
  fun getActiveSubjectsFlow(): Flow<List<SubjectEntity>>

  @Query("SELECT * FROM subjects ORDER BY orderNum ASC, name ASC")
  fun getAllSubjectsFlow(): Flow<List<SubjectEntity>>

  @Query("SELECT * FROM subjects WHERE id = :subjectId")
  suspend fun getSubject(subjectId: String): SubjectEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSubject(subject: SubjectEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSubjects(subjects: List<SubjectEntity>)

  @Update
  suspend fun updateSubject(subject: SubjectEntity)

  @Query("DELETE FROM subjects WHERE id = :subjectId")
  suspend fun deleteSubject(subjectId: String)

  // Homework
  @Query("SELECT * FROM homework WHERE status = 'ACTIVE' ORDER BY pinned DESC, dueAt ASC")
  fun getActiveHomeworkFlow(): Flow<List<HomeworkEntity>>

  @Query("SELECT * FROM homework ORDER BY pinned DESC, dueAt ASC")
  fun getAllHomeworkFlow(): Flow<List<HomeworkEntity>>

  @Query("SELECT * FROM homework WHERE id = :id")
  suspend fun getHomework(id: String): HomeworkEntity?

  @Query("SELECT * FROM homework WHERE id = :id")
  fun getHomeworkFlow(id: String): Flow<HomeworkEntity?>

  @Query("SELECT COUNT(*) FROM homework WHERE authorUid = :authorUid AND createdAt >= :sinceTimestamp")
  suspend fun countHomeworkByAuthorSince(authorUid: String, sinceTimestamp: Long): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertHomework(homework: HomeworkEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertHomeworkList(homeworkList: List<HomeworkEntity>)

  @Update
  suspend fun updateHomework(homework: HomeworkEntity)

  @Query("UPDATE homework SET status = :status WHERE id = :id")
  suspend fun updateHomeworkStatus(id: String, status: String)

  @Query("UPDATE homework SET pinned = :pinned WHERE id = :id")
  suspend fun updateHomeworkPinned(id: String, pinned: Boolean)

  @Query("DELETE FROM homework WHERE id = :id")
  suspend fun deleteHomework(id: String)

  @Query("UPDATE homework SET reportCount = reportCount + 1 WHERE id = :id")
  suspend fun incrementReportCount(id: String)

  // Completions
  @Query("SELECT * FROM completions WHERE userId = :userId")
  fun getUserCompletionsFlow(userId: String): Flow<List<CompletionEntity>>

  @Query("SELECT * FROM completions WHERE userId = :userId AND homeworkId = :homeworkId")
  suspend fun getCompletion(userId: String, homeworkId: String): CompletionEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCompletion(completion: CompletionEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCompletions(completions: List<CompletionEntity>)

  @Query("DELETE FROM completions WHERE userId = :userId AND homeworkId = :homeworkId")
  suspend fun deleteCompletion(userId: String, homeworkId: String)

  // Reports
  @Query("SELECT * FROM reports ORDER BY createdAt DESC")
  fun getAllReportsFlow(): Flow<List<ReportEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertReport(report: ReportEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertReports(reports: List<ReportEntity>)

  @Query("UPDATE reports SET status = :status WHERE id = :id")
  suspend fun updateReportStatus(id: String, status: String)

  @Query("DELETE FROM reports WHERE id = :id")
  suspend fun deleteReport(id: String)

  // Announcements
  @Query("SELECT * FROM announcements ORDER BY pinned DESC, createdAt DESC")
  fun getAnnouncementsFlow(): Flow<List<AnnouncementEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAnnouncement(announcement: AnnouncementEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAnnouncements(announcements: List<AnnouncementEntity>)

  @Query("DELETE FROM announcements WHERE id = :id")
  suspend fun deleteAnnouncement(id: String)

  // Notifications
  @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
  fun getNotificationsFlow(): Flow<List<NotificationEntity>>

  @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
  fun getUnreadNotificationsCountFlow(): Flow<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNotification(notification: NotificationEntity)

  @Query("UPDATE notifications SET isRead = 1")
  suspend fun markAllNotificationsRead()

  @Query("DELETE FROM notifications")
  suspend fun clearAllNotifications()

  // Settings
  @Query("SELECT * FROM settings")
  fun getAllSettingsFlow(): Flow<List<AppSettingEntity>>

  @Query("SELECT value FROM settings WHERE key = :key")
  suspend fun getSetting(key: String): String?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSetting(setting: AppSettingEntity)

  // Audit Log
  @Query("SELECT * FROM audit_log ORDER BY createdAt DESC LIMIT 50")
  fun getAuditLogsFlow(): Flow<List<AuditLogEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAuditLog(log: AuditLogEntity)
}
