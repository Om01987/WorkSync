package com.worksync.app.domain.usecase.task

import com.worksync.app.domain.model.Task
import com.worksync.app.domain.model.enums.TaskStatus
import com.worksync.app.domain.repository.TaskRepository
import javax.inject.Inject

class UpdateTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend fun updateTask(task: Task): Result<Unit> {
        if (task.title.isBlank()) {
            return Result.failure(Exception("Task title cannot be empty"))
        }

        return taskRepository.updateTask(task)
    }

    suspend fun updateTaskStatus(taskId: String, status: TaskStatus): Result<Unit> {
        return taskRepository.updateTaskStatus(taskId, status)
    }

    suspend fun updateTaskProgress(taskId: String, percentage: Int): Result<Unit> {
        if (percentage < 0 || percentage > 100) {
            return Result.failure(Exception("Progress percentage must be between 0 and 100"))
        }

        return taskRepository.updateTaskProgress(taskId, percentage)
    }
}
