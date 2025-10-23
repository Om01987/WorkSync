package com.worksync.app.di

import com.worksync.app.data.repository.AuthRepositoryImpl
import com.worksync.app.data.repository.TaskRepositoryImpl
import com.worksync.app.data.repository.UserRepositoryImpl
import com.worksync.app.domain.repository.AuthRepository
import com.worksync.app.domain.repository.TaskRepository
import com.worksync.app.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        taskRepositoryImpl: TaskRepositoryImpl
    ): TaskRepository


    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: com.worksync.app.data.repository.UserRepositoryImpl
    ): com.worksync.app.domain.repository.UserRepository

}
