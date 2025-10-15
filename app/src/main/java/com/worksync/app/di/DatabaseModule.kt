package com.worksync.app.di

import android.content.Context
import androidx.room.Room
import com.worksync.app.data.local.dao.TaskDao
import com.worksync.app.data.local.dao.TaskPhaseDao
import com.worksync.app.data.local.dao.UserDao
import com.worksync.app.data.local.database.WorkSyncDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideWorkSyncDatabase(@ApplicationContext context: Context): WorkSyncDatabase {
        return WorkSyncDatabase.buildDatabase(context)
    }

    @Provides
    fun provideUserDao(database: WorkSyncDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideTaskDao(database: WorkSyncDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    fun provideTaskPhaseDao(database: WorkSyncDatabase): TaskPhaseDao {
        return database.taskPhaseDao()
    }
}
