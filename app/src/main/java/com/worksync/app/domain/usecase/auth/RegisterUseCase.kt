package com.worksync.app.domain.usecase.auth

import com.worksync.app.domain.model.User
import com.worksync.app.domain.model.enums.UserRole
import com.worksync.app.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        confirmPassword: String,
        name: String,
        role: UserRole
    ): Result<User> {

        if (email.isBlank()) {
            return Result.failure(Exception("Email cannot be empty"))
        }

        if (password.isBlank()) {
            return Result.failure(Exception("Password cannot be empty"))
        }

        if (name.isBlank()) {
            return Result.failure(Exception("Name cannot be empty"))
        }

        if (!isValidEmail(email)) {
            return Result.failure(Exception("Please enter a valid email address"))
        }

        if (password.length < 6) {
            return Result.failure(Exception("Password must be at least 6 characters"))
        }

        if (password != confirmPassword) {
            return Result.failure(Exception("Passwords do not match"))
        }

        if (name.length < 2) {
            return Result.failure(Exception("Name must be at least 2 characters"))
        }

        return authRepository.register(email, password, name, role)
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
