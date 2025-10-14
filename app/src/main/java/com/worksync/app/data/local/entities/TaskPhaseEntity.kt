package com.worksync.app.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.worksync.app.domain.model.TaskPhase

@Entity(
    tableName = "task_phases",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TaskPhaseEntity(
    @PrimaryKey
    val id: String,
    val taskId: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val completedAt: Long?,
    val order: Int,
    val isCustom: Boolean,
    val createdBy: String,
    val createdAt: Long
) {
    fun toDomainModel(): TaskPhase {
        return TaskPhase(
            id = id,
            taskId = taskId,
            title = title,
            description = description,
            isCompleted = isCompleted,
            completedAt = completedAt,
            order = order,
            isCustom = isCustom,
            createdBy = createdBy,
            createdAt = createdAt
        )
    }
}

fun TaskPhase.toEntity(): TaskPhaseEntity {
    return TaskPhaseEntity(
        id = id,
        taskId = taskId,
        title = title,
        description = description,
        isCompleted = isCompleted,
        completedAt = completedAt,
        order = order,
        isCustom = isCustom,
        createdBy = createdBy,
        createdAt = createdAt
    )
}
