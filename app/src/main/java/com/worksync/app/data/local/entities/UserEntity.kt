package com.worksync.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.worksync.app.domain.model.User
import com.worksync.app.domain.model.enums.UserRole

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val name: String,
    val role: UserRole,
    val profileImageUrl: String?,
    val createdAt: Long,
    val isActive: Boolean
) {
    fun toDomainModel(): User {
        return User(
            id = id,
            email = email,
            name = name,
            role = role,
            profileImageUrl = profileImageUrl,
            createdAt = createdAt,
            isActive = isActive
        )
    }
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        email = email,
        name = name,
        role = role,
        profileImageUrl = profileImageUrl,
        createdAt = createdAt,
        isActive = isActive
    )
}
