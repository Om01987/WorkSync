package com.worksync.app.domain.usecase.auth

import com.worksync.app.domain.model.User
import com.worksync.app.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        if (email.isBlank()) {
            return Result.failure(Exception("Email cannot be empty"))
        }

        if (password.isBlank()) {
            return Result.failure(Exception("Password cannot be empty"))
        }

        if (!isValidEmail(email)) {
            return Result.failure(Exception("Please enter a valid email address"))
        }

        if (password.length < 6) {
            return Result.failure(Exception("Password must be at least 6 characters"))
        }

        return authRepository.login(email, password)
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
