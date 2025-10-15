package com.worksync.app.domain.usecase.task

import com.worksync.app.domain.repository.TaskRepository
import javax.inject.Inject

class DeleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: String): Result<Unit> {
        if (taskId.isBlank()) {
            return Result.failure(Exception("Task ID cannot be empty"))
        }

        return taskRepository.deleteTask(taskId)
    }
}
