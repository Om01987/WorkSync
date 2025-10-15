package com.worksync.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import com.worksync.app.data.local.dao.UserDao
import com.worksync.app.data.local.entities.toEntity
import com.worksync.app.domain.model.User
import com.worksync.app.domain.model.enums.UserRole
import com.worksync.app.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch // <-- added
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao
) : AuthRepository {

    companion object {
        private const val USERS_COLLECTION = "users"
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                val user = getUserFromFirestore(firebaseUser.uid)
                if (user != null) {
                    userDao.insertUser(user.toEntity())
                    Result.success(user)
                } else {
                    Result.failure(Exception("User data not found"))
                }
            } else {
                Result.failure(Exception("Authentication failed"))
            }
        } catch (e: FirebaseAuthException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        name: String,
        role: UserRole
    ): Result<User> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                val user = User(
                    id = firebaseUser.uid,
                    email = email,
                    name = name,
                    role = role,
                    createdAt = System.currentTimeMillis(),
                    isActive = true
                )
                saveUserToFirestore(user)
                userDao.insertUser(user.toEntity())
                Result.success(user)
            } else {
                Result.failure(Exception("User creation failed"))
            }
        } catch (e: FirebaseAuthException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            userDao.deleteAllUsers()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser
        return if (firebaseUser != null) {
            getUserFromFirestore(firebaseUser.uid)
        } else {
            null
        }
    }

    // UPDATED: wrap suspend call inside launch within callbackFlow
    override fun getCurrentUserFlow(): Flow<User?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                launch {
                    val user = try {
                        getUserFromFirestore(firebaseUser.uid)
                    } catch (e: Exception) {
                        null
                    }
                    trySend(user).isSuccess
                }
            } else {
                trySend(null).isSuccess
            }
        }

        firebaseAuth.addAuthStateListener(authStateListener)
        awaitClose { firebaseAuth.removeAuthStateListener(authStateListener) }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: FirebaseAuthException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(user: User): Result<User> {
        return try {
            saveUserToFirestore(user)
            userDao.updateUser(user.toEntity())
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                firestore.collection(USERS_COLLECTION)
                    .document(firebaseUser.uid)
                    .delete()
                    .await()
                userDao.deleteUserById(firebaseUser.uid)
                firebaseUser.delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isUserLoggedIn(): Boolean = firebaseAuth.currentUser != null

    private suspend fun getUserFromFirestore(userId: String): User? {
        return try {
            val document = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()
            if (document.exists()) {
                User(
                    id = document.getString("id") ?: "",
                    email = document.getString("email") ?: "",
                    name = document.getString("name") ?: "",
                    role = UserRole.valueOf(document.getString("role") ?: "EMPLOYEE"),
                    profileImageUrl = document.getString("profileImageUrl"),
                    createdAt = document.getLong("createdAt") ?: 0L,
                    isActive = document.getBoolean("isActive") ?: true
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun saveUserToFirestore(user: User) {
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
    }
}
