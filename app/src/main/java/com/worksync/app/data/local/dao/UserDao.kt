package com.worksync.app.data.local.dao

import androidx.room.*
import com.worksync.app.data.local.entities.UserEntity
import com.worksync.app.domain.model.enums.UserRole
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE role = :role AND isActive = 1")
    fun getUsersByRole(role: UserRole): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE isActive = 1")
    fun getAllActiveUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)

    @Query("UPDATE users SET isActive = :isActive WHERE id = :userId")
    suspend fun updateUserActiveStatus(userId: String, isActive: Boolean)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}
