package com.worksync.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.worksync.app.domain.model.User
import com.worksync.app.domain.model.enums.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class UserUiState(
    val employees: List<User> = emptyList(),
    val allUsers: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class UserViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    companion object {
        private const val USERS_COLLECTION = "users"
    }

    // Fetch employees directly from Firestore
    fun loadEmployees() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val snapshot = firestore.collection(USERS_COLLECTION)
                    .whereEqualTo("role", UserRole.EMPLOYEE.name)
                    .whereEqualTo("isActive", true)
                    .get()
                    .await()

                val employees = snapshot.documents.mapNotNull { doc ->
                    try {
                        User(
                            id = doc.getString("id") ?: doc.id,
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

                _uiState.value = _uiState.value.copy(
                    employees = employees,
                    isLoading = false,
                    errorMessage = if (employees.isEmpty()) "No employees found" else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load employees"
                )
            }
        }
    }

    // Fetch all active users directly from Firestore
    fun loadAllUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val snapshot = firestore.collection(USERS_COLLECTION)
                    .whereEqualTo("isActive", true)
                    .get()
                    .await()

                val users = snapshot.documents.mapNotNull { doc ->
                    try {
                        User(
                            id = doc.getString("id") ?: doc.id,
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

                _uiState.value = _uiState.value.copy(
                    allUsers = users,
                    isLoading = false,
                    errorMessage = if (users.isEmpty()) "No users found" else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load users"
                )
            }
        }
    }

    // Refresh employees (same as loadEmployees)
    fun refreshEmployeesOnce() {
        loadEmployees()
    }
}
