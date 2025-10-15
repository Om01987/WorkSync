package com.worksync.app.domain.usecase.task

import com.worksync.app.domain.model.Task
import com.worksync.app.domain.repository.TaskRepository
import javax.inject.Inject

class CreateTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(task: Task): Result<Unit> {
        if (task.title.isBlank()) {
            return Result.failure(Exception("Task title cannot be empty"))
        }

        if (task.description.isBlank()) {
            return Result.failure(Exception("Task description cannot be empty"))
        }

        if (task.assignedTo.isBlank()) {
            return Result.failure(Exception("Task must be assigned to someone"))
        }

        if (task.assignedBy.isBlank()) {
            return Result.failure(Exception("Task creator must be specified"))
        }

        return taskRepository.createTask(task)
    }
}
