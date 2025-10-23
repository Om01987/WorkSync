package com.worksync.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worksync.app.domain.model.User
import com.worksync.app.domain.model.enums.UserRole
import com.worksync.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserUiState(
    val employees: List<User> = emptyList(),
    val allUsers: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    // Force a remote pull, then start collecting employees from Room
    fun loadEmployees() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                // Pull latest users from Firestore to local cache
                userRepository.syncUsers()
                // Now collect employees from DAO-backed flow
                userRepository.getUsersByRole(UserRole.EMPLOYEE).collect { employees ->
                    _uiState.value = _uiState.value.copy(
                        employees = employees,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load employees"
                )
            }
        }
    }

    fun loadAllUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                userRepository.syncUsers()
                userRepository.getAllActiveUsers().collect { users ->
                    _uiState.value = _uiState.value.copy(
                        allUsers = users,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load users"
                )
            }
        }
    }

    // Manual one-shot refresh to pull from Firestore (used by dialog Refresh button)
    fun refreshEmployeesOnce() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                userRepository.syncUsers()
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Refresh failed"
                )
            }
        }
    }
}
