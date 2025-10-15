package com.worksync.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worksync.app.domain.model.User
import com.worksync.app.domain.model.enums.UserRole
import com.worksync.app.domain.usecase.auth.LoginUseCase
import com.worksync.app.domain.usecase.auth.LogoutUseCase
import com.worksync.app.domain.usecase.auth.RegisterUseCase
import com.worksync.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val currentUser: User? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkAuthenticationState()
        observeAuthState()
    }

    private fun checkAuthenticationState() {
        viewModelScope.launch {
            val isLoggedIn = authRepository.isUserLoggedIn()
            val currentUser = if (isLoggedIn) authRepository.getCurrentUser() else null

            _uiState.value = _uiState.value.copy(
                isLoggedIn = isLoggedIn,
                currentUser = currentUser
            )
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.getCurrentUserFlow().collect { user ->
                _uiState.value = _uiState.value.copy(
                    isLoggedIn = user != null,
                    currentUser = user
                )
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            loginUseCase(email, password)
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        currentUser = user,
                        successMessage = "Login successful!"
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Login failed"
                    )
                }
        }
    }

    fun register(
        email: String,
        password: String,
        confirmPassword: String,
        name: String,
        role: UserRole
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            registerUseCase(email, password, confirmPassword, name, role)
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        currentUser = user,
                        successMessage = "Registration successful!"
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Registration failed"
                    )
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            logoutUseCase()
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = false,
                        currentUser = null,
                        successMessage = "Logged out successfully"
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Logout failed"
                    )
                }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
}
