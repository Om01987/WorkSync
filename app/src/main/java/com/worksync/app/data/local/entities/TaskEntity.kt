package com.worksync.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.worksync.app.domain.model.Task
import com.worksync.app.domain.model.enums.TaskPriority
import com.worksync.app.domain.model.enums.TaskStatus

class TaskConverters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }

    @TypeConverter
    fun fromTaskStatus(status: TaskStatus): String {
        return status.name
    }

    @TypeConverter
    fun toTaskStatus(status: String): TaskStatus {
        return TaskStatus.valueOf(status)
    }

    @TypeConverter
    fun fromTaskPriority(priority: TaskPriority): String {
        return priority.name
    }

    @TypeConverter
    fun toTaskPriority(priority: String): TaskPriority {
        return TaskPriority.valueOf(priority)
    }
}

@Entity(tableName = "tasks")
@TypeConverters(TaskConverters::class)
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val assignedTo: String,
    val assignedBy: String,
    val priority: TaskPriority,
    val status: TaskStatus,
    val completionPercentage: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val deadline: Long?,
    val estimatedHours: Int?,
    val tags: List<String>,
    val assignedToName: String,
    val assignedByName: String
) {
    fun toDomainModel(): Task {
        return Task(
            id = id,
            title = title,
            description = description,
            assignedTo = assignedTo,
            assignedBy = assignedBy,
            priority = priority,
            status = status,
            completionPercentage = completionPercentage,
            createdAt = createdAt,
            updatedAt = updatedAt,
            deadline = deadline,
            estimatedHours = estimatedHours,
            tags = tags,
            assignedToName = assignedToName,
            assignedByName = assignedByName
        )
    }
}

fun Task.toEntity(): TaskEntity {
    return TaskEntity(
        id = id,
        title = title,
        description = description,
        assignedTo = assignedTo,
        assignedBy = assignedBy,
        priority = priority,
        status = status,
        completionPercentage = completionPercentage,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deadline = deadline,
        estimatedHours = estimatedHours,
        tags = tags,
        assignedToName = assignedToName,
        assignedByName = assignedByName
    )
}
