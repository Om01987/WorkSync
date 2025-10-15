package com.worksync.app.domain.repository

import com.worksync.app.domain.model.Task
import com.worksync.app.domain.model.TaskPhase
import com.worksync.app.domain.model.enums.TaskStatus
import com.worksync.app.domain.model.enums.TaskPriority
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    suspend fun createTask(task: Task): Result<Unit>
    suspend fun updateTask(task: Task): Result<Unit>
    suspend fun deleteTask(taskId: String): Result<Unit>
    suspend fun getTaskById(taskId: String): Task?
    fun getAllTasks(): Flow<List<Task>>
    fun getTasksAssignedToUser(userId: String): Flow<List<Task>>
    fun getTasksCreatedByUser(userId: String): Flow<List<Task>>
    fun getTasksByStatus(status: TaskStatus): Flow<List<Task>>
    fun getUserTasksByStatus(userId: String, status: TaskStatus): Flow<List<Task>>
    suspend fun updateTaskStatus(taskId: String, status: TaskStatus): Result<Unit>
    suspend fun updateTaskProgress(taskId: String, percentage: Int): Result<Unit>
    fun getOverdueTasks(): Flow<List<Task>>

    // Phase management
    suspend fun addPhaseToTask(taskId: String, phase: TaskPhase): Result<Unit>
    suspend fun updatePhase(phase: TaskPhase): Result<Unit>
    suspend fun deletePhase(phaseId: String): Result<Unit>
    suspend fun markPhaseCompleted(phaseId: String): Result<Unit>
    fun getTaskWithPhases(taskId: String): Flow<Task?>

    // Sync operations
    suspend fun syncTasks(): Result<Unit>
    suspend fun syncTasksForUser(userId: String): Result<Unit>
}
