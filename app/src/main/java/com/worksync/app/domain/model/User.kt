package com.worksync.app.domain.model

import com.worksync.app.domain.model.enums.UserRole

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val role: UserRole = UserRole.EMPLOYEE,
    val profileImageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
