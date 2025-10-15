package com.worksync.app.domain.repository

import com.worksync.app.domain.model.User
import com.worksync.app.domain.model.enums.UserRole
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String, name: String, role: UserRole): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun getCurrentUser(): User?
    fun getCurrentUserFlow(): Flow<User?>
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun updateProfile(user: User): Result<User>
    suspend fun deleteAccount(): Result<Unit>
    fun isUserLoggedIn(): Boolean
}
