package com.worksync.app.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.worksync.app.data.local.dao.TaskDao
import com.worksync.app.data.local.dao.TaskPhaseDao
import com.worksync.app.data.local.dao.UserDao
import com.worksync.app.data.local.entities.TaskEntity
import com.worksync.app.data.local.entities.TaskPhaseEntity
import com.worksync.app.data.local.entities.UserEntity
import com.worksync.app.data.local.entities.TaskConverters
import com.worksync.app.data.local.entities.UserRoleConverter

@Database(
    entities = [
        UserEntity::class,
        TaskEntity::class,
        TaskPhaseEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(TaskConverters::class, UserRoleConverter::class)
abstract class WorkSyncDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun taskDao(): TaskDao
    abstract fun taskPhaseDao(): TaskPhaseDao

    companion object {
        const val DATABASE_NAME = "worksync_database"

        fun buildDatabase(context: Context): WorkSyncDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                WorkSyncDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
