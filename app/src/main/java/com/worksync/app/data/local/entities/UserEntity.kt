package com.worksync.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.worksync.app.domain.model.User
import com.worksync.app.domain.model.enums.UserRole

class UserRoleConverter {
    @TypeConverter
    fun fromUserRole(role: UserRole): String {
        return role.name
    }

    @TypeConverter
    fun toUserRole(role: String): UserRole {
        return UserRole.valueOf(role)
    }
}

@Entity(tableName = "users")
@TypeConverters(UserRoleConverter::class)
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
