package com.worksync.app.domain.usecase.task

import com.worksync.app.domain.model.Task
import com.worksync.app.domain.model.enums.TaskStatus
import com.worksync.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    fun getAllTasks(): Flow<List<Task>> {
        return taskRepository.getAllTasks()
    }

    fun getTasksForUser(userId: String): Flow<List<Task>> {
        return taskRepository.getTasksAssignedToUser(userId)
    }

    fun getTasksCreatedByUser(userId: String): Flow<List<Task>> {
        return taskRepository.getTasksCreatedByUser(userId)
    }

    fun getTasksByStatus(status: TaskStatus): Flow<List<Task>> {
        return taskRepository.getTasksByStatus(status)
    }

    fun getUserTasksByStatus(userId: String, status: TaskStatus): Flow<List<Task>> {
        return taskRepository.getUserTasksByStatus(userId, status)
    }

    fun getOverdueTasks(): Flow<List<Task>> {
        return taskRepository.getOverdueTasks()
    }

    suspend fun getTaskById(taskId: String): Task? {
        return taskRepository.getTaskById(taskId)
    }

    fun getTaskWithPhases(taskId: String): Flow<Task?> {
        return taskRepository.getTaskWithPhases(taskId)
    }
}
