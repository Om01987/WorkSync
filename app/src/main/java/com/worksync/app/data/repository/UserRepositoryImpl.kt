package com.worksync.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.worksync.app.data.local.dao.UserDao
import com.worksync.app.data.local.entities.toEntity
import com.worksync.app.domain.model.User
import com.worksync.app.domain.model.enums.UserRole
import com.worksync.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val firestore: FirebaseFirestore
) : UserRepository {

    companion object {
        private const val USERS_COLLECTION = "users"
    }

    override suspend fun getUserById(userId: String): User? {
        return try {
            val userEntity = userDao.getUserById(userId)
            userEntity?.toDomainModel()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getUserByEmail(email: String): User? {
        return try {
            val userEntity = userDao.getUserByEmail(email)
            userEntity?.toDomainModel()
        } catch (e: Exception) {
            null
        }
    }

    override fun getUsersByRole(role: UserRole): Flow<List<User>> {
        return userDao.getUsersByRole(role).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getAllActiveUsers(): Flow<List<User>> {
        return userDao.getAllActiveUsers().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        return try {
            userDao.updateUser(user.toEntity())

            // Update in Firestore
            val userMap = hashMapOf(
                "id" to user.id,
                "email" to user.email,
                "name" to user.name,
                "role" to user.role.name,
                "profileImageUrl" to user.profileImageUrl,
                "createdAt" to user.createdAt,
                "isActive" to user.isActive
            )

            firestore.collection(USERS_COLLECTION)
                .document(user.id)
                .set(userMap)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deactivateUser(userId: String): Result<Unit> {
        return try {
            userDao.updateUserActiveStatus(userId, false)

            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .update("isActive", false)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun activateUser(userId: String): Result<Unit> {
        return try {
            userDao.updateUserActiveStatus(userId, true)

            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .update("isActive", true)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            userDao.deleteUserById(userId)

            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncUsers(): Result<Unit> {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val users = snapshot.documents.mapNotNull { doc ->
                try {
                    User(
                        id = doc.getString("id") ?: "",
                        email = doc.getString("email") ?: "",
                        name = doc.getString("name") ?: "",
                        role = UserRole.valueOf(doc.getString("role") ?: "EMPLOYEE"),
                        profileImageUrl = doc.getString("profileImageUrl"),
                        createdAt = doc.getLong("createdAt") ?: 0L,
                        isActive = doc.getBoolean("isActive") ?: true
                    )
                } catch (e: Exception) {
                    null
                }
            }

            userDao.insertUsers(users.map { it.toEntity() })
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cacheUser(user: User): Result<Unit> {
        return try {
            userDao.insertUser(user.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
