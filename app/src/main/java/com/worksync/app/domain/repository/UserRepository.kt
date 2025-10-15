package com.worksync.app.domain.repository

import com.worksync.app.domain.model.User
import com.worksync.app.domain.model.enums.UserRole
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getUserById(userId: String): User?
    suspend fun getUserByEmail(email: String): User?
    fun getUsersByRole(role: UserRole): Flow<List<User>>
    fun getAllActiveUsers(): Flow<List<User>>
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun deactivateUser(userId: String): Result<Unit>
    suspend fun activateUser(userId: String): Result<Unit>
    suspend fun deleteUser(userId: String): Result<Unit>

    // Sync operations
    suspend fun syncUsers(): Result<Unit>
    suspend fun cacheUser(user: User): Result<Unit>
}
