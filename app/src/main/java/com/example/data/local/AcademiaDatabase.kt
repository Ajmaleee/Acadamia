package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [
    UserEntity::class,
    SubjectEntity::class,
    HomeworkEntity::class,
    CompletionEntity::class,
    ReportEntity::class,
    AnnouncementEntity::class,
    NotificationEntity::class,
    AppSettingEntity::class,
    AuditLogEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class AcademiaDatabase : RoomDatabase() {
  abstract fun appDao(): AppDao

  companion object {
    @Volatile
    private var INSTANCE: AcademiaDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope): AcademiaDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AcademiaDatabase::class.java,
          "academia_homework_database"
        )
          .addCallback(DatabaseCallback(scope))
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }

  private class DatabaseCallback(
    private val scope: CoroutineScope
  ) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
      super.onCreate(db)
      INSTANCE?.let { database ->
        scope.launch(Dispatchers.IO) {
          populateInitialData(database.appDao())
        }
      }
    }

    private suspend fun populateInitialData(dao: AppDao) {
      val now = System.currentTimeMillis()

      // Initial Subjects
      val defaultSubjects = listOf(
        SubjectEntity("sub_math", "Mathematics", "#4778D0", "math", 1, false),
        SubjectEntity("sub_science", "Science", "#38A169", "science", 2, false),
        SubjectEntity("sub_english", "English Literature", "#805AD5", "english", 3, false),
        SubjectEntity("sub_social", "Social Studies", "#DD6B20", "social", 4, false),
        SubjectEntity("sub_cs", "Computer Science", "#319795", "cs", 5, false),
        SubjectEntity("sub_art", "Fine Arts", "#D53F8C", "art", 6, false),
        SubjectEntity("sub_other", "General Studies", "#718096", "other", 7, false)
      )
      dao.insertSubjects(defaultSubjects)

      // Initial Default Settings
      dao.insertSetting(AppSettingEntity("requireHomeworkApproval", "false"))
      dao.insertSetting(AppSettingEntity("studentsCanPost", "true"))
      dao.insertSetting(AppSettingEntity("autoNotifyOnNewHomework", "true"))
      dao.insertSetting(AppSettingEntity("maxPostsPerDay", "5"))
      dao.insertSetting(AppSettingEntity("blockedWords", "curse,swear,badword,spam"))
      dao.insertSetting(AppSettingEntity("className", "Class 10-A • Modern Academy"))
    }
  }
}
