package com.worksync.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.worksync.app.data.local.dao.TaskDao
import com.worksync.app.data.local.dao.TaskPhaseDao
import com.worksync.app.data.local.entities.toEntity
import com.worksync.app.domain.model.Task
import com.worksync.app.domain.model.TaskPhase
import com.worksync.app.domain.model.enums.TaskPriority
import com.worksync.app.domain.model.enums.TaskStatus
import com.worksync.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val taskPhaseDao: TaskPhaseDao,
    private val firestore: FirebaseFirestore
) : TaskRepository {

    companion object {
        private const val TASKS_COLLECTION = "tasks"
        private const val PHASES_COLLECTION = "phases"
    }

    override suspend fun createTask(task: Task): Result<Unit> {
        return try {
            val taskWithId = if (task.id.isEmpty()) {
                task.copy(id = UUID.randomUUID().toString())
            } else task

            // Save to Firestore
            saveTaskToFirestore(taskWithId)

            // Save locally
            taskDao.insertTask(taskWithId.toEntity())

            // Create default phases if none exist
            if (taskWithId.phases.isEmpty()) {
                val defaultPhases = createDefaultPhases(taskWithId.id)
                defaultPhases.forEach { phase ->
                    taskPhaseDao.insertPhase(phase.toEntity())
                    savePhaseToFirestore(phase)
                }
            } else {
                taskWithId.phases.forEach { phase ->
                    taskPhaseDao.insertPhase(phase.toEntity())
                    savePhaseToFirestore(phase)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTask(task: Task): Result<Unit> {
        return try {
            val updatedTask = task.copy(updatedAt = System.currentTimeMillis())

            // Update in Firestore
            saveTaskToFirestore(updatedTask)

            // Update locally
            taskDao.updateTask(updatedTask.toEntity())

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTask(taskId: String): Result<Unit> {
        return try {
            // Delete from Firestore
            firestore.collection(TASKS_COLLECTION)
                .document(taskId)
                .delete()
                .await()

            // Delete phases from Firestore
            val phasesSnapshot = firestore.collection(TASKS_COLLECTION)
                .document(taskId)
                .collection(PHASES_COLLECTION)
                .get()
                .await()

            phasesSnapshot.documents.forEach { doc ->
                doc.reference.delete()
            }

            // Delete locally
            taskDao.deleteTaskById(taskId)
            taskPhaseDao.deletePhasesByTaskId(taskId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTaskById(taskId: String): Task? {
        return try {
            val taskEntity = taskDao.getTaskById(taskId)
            if (taskEntity != null) {
                val phases = taskPhaseDao.getPhasesByTaskIdSync(taskId)
                taskEntity.toDomainModel().copy(
                    phases = phases.map { it.toDomainModel() }
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks().map { taskEntities ->
            taskEntities.map { taskEntity ->
                val phases = taskPhaseDao.getPhasesByTaskIdSync(taskEntity.id)
                taskEntity.toDomainModel().copy(
                    phases = phases.map { it.toDomainModel() }
                )
            }
        }
    }

    override fun getTasksAssignedToUser(userId: String): Flow<List<Task>> {
        return taskDao.getTasksAssignedToUser(userId).map { taskEntities ->
            taskEntities.map { taskEntity ->
                val phases = taskPhaseDao.getPhasesByTaskIdSync(taskEntity.id)
                taskEntity.toDomainModel().copy(
                    phases = phases.map { it.toDomainModel() }
                )
            }
        }
    }

    override fun getTasksCreatedByUser(userId: String): Flow<List<Task>> {
        return taskDao.getTasksCreatedByUser(userId).map { taskEntities ->
            taskEntities.map { taskEntity ->
                val phases = taskPhaseDao.getPhasesByTaskIdSync(taskEntity.id)
                taskEntity.toDomainModel().copy(
                    phases = phases.map { it.toDomainModel() }
                )
            }
        }
    }

    override fun getTasksByStatus(status: TaskStatus): Flow<List<Task>> {
        return taskDao.getTasksByStatus(status).map { taskEntities ->
            taskEntities.map { taskEntity ->
                val phases = taskPhaseDao.getPhasesByTaskIdSync(taskEntity.id)
                taskEntity.toDomainModel().copy(
                    phases = phases.map { it.toDomainModel() }
                )
            }
        }
    }

    override fun getUserTasksByStatus(userId: String, status: TaskStatus): Flow<List<Task>> {
        return taskDao.getUserTasksByStatus(userId, status).map { taskEntities ->
            taskEntities.map { taskEntity ->
                val phases = taskPhaseDao.getPhasesByTaskIdSync(taskEntity.id)
                taskEntity.toDomainModel().copy(
                    phases = phases.map { it.toDomainModel() }
                )
            }
        }
    }

    override suspend fun updateTaskStatus(taskId: String, status: TaskStatus): Result<Unit> {
        return try {
            val currentTime = System.currentTimeMillis()

            // Update locally
            taskDao.updateTaskStatus(taskId, status, currentTime)

            // Update in Firestore
            firestore.collection(TASKS_COLLECTION)
                .document(taskId)
                .update(
                    mapOf(
                        "status" to status.name,
                        "updatedAt" to currentTime
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTaskProgress(taskId: String, percentage: Int): Result<Unit> {
        return try {
            val currentTime = System.currentTimeMillis()
            val clampedPercentage = percentage.coerceIn(0, 100)

            // Update locally
            taskDao.updateTaskProgress(taskId, clampedPercentage, currentTime)

            // Update in Firestore
            firestore.collection(TASKS_COLLECTION)
                .document(taskId)
                .update(
                    mapOf(
                        "completionPercentage" to clampedPercentage,
                        "updatedAt" to currentTime
                    )
                )
                .await()

            // Auto-update status based on progress
            val newStatus = when (clampedPercentage) {
                0 -> TaskStatus.PENDING
                in 1..99 -> TaskStatus.IN_PROGRESS
                100 -> TaskStatus.COMPLETED
                else -> TaskStatus.PENDING
            }

            updateTaskStatus(taskId, newStatus)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getOverdueTasks(): Flow<List<Task>> {
        val currentTime = System.currentTimeMillis()
        return taskDao.getOverdueTasks(currentTime).map { taskEntities ->
            taskEntities.map { taskEntity ->
                val phases = taskPhaseDao.getPhasesByTaskIdSync(taskEntity.id)
                taskEntity.toDomainModel().copy(
                    phases = phases.map { it.toDomainModel() }
                )
            }
        }
    }

    override suspend fun addPhaseToTask(taskId: String, phase: TaskPhase): Result<Unit> {
        return try {
            val phaseWithId = if (phase.id.isEmpty()) {
                phase.copy(
                    id = UUID.randomUUID().toString(),
                    taskId = taskId
                )
            } else {
                phase.copy(taskId = taskId)
            }

            // Save locally
            taskPhaseDao.insertPhase(phaseWithId.toEntity())

            // Save to Firestore
            savePhaseToFirestore(phaseWithId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePhase(phase: TaskPhase): Result<Unit> {
        return try {
            // Update locally
            taskPhaseDao.updatePhase(phase.toEntity())

            // Update in Firestore
            savePhaseToFirestore(phase)

            // Update task completion percentage based on completed phases
            updateTaskCompletionFromPhases(phase.taskId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePhase(phaseId: String): Result<Unit> {
        return try {
            val phase = taskPhaseDao.getPhaseById(phaseId)

            if (phase != null) {
                // Delete from Firestore
                firestore.collection(TASKS_COLLECTION)
                    .document(phase.taskId)
                    .collection(PHASES_COLLECTION)
                    .document(phaseId)
                    .delete()
                    .await()

                // Delete locally
                taskPhaseDao.deletePhaseById(phaseId)

                // Update task completion percentage
                updateTaskCompletionFromPhases(phase.taskId)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markPhaseCompleted(phaseId: String): Result<Unit> {
        return try {
            val currentTime = System.currentTimeMillis()

            // Update locally
            taskPhaseDao.updatePhaseCompletion(phaseId, true, currentTime)

            val phase = taskPhaseDao.getPhaseById(phaseId)
            if (phase != null) {
                val updatedPhase = phase.toDomainModel().copy(
                    isCompleted = true,
                    completedAt = currentTime
                )

                // Update in Firestore
                savePhaseToFirestore(updatedPhase)

                // Update task completion percentage
                updateTaskCompletionFromPhases(phase.taskId)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getTaskWithPhases(taskId: String): Flow<Task?> {
        return combine(
            taskDao.getAllTasks(),
            taskPhaseDao.getPhasesByTaskId(taskId)
        ) { tasks, phases ->
            val task = tasks.find { it.id == taskId }
            task?.toDomainModel()?.copy(
                phases = phases.map { it.toDomainModel() }
            )
        }
    }

    override suspend fun syncTasks(): Result<Unit> {
        return try {
            val snapshot = firestore.collection(TASKS_COLLECTION)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val tasks = snapshot.documents.mapNotNull { doc ->
                try {
                    documentToTask(doc.data ?: return@mapNotNull null, doc.id)
                } catch (e: Exception) {
                    null
                }
            }

            // Save to local database
            taskDao.insertTasks(tasks.map { it.toEntity() })

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncTasksForUser(userId: String): Result<Unit> {
        return try {
            val snapshot = firestore.collection(TASKS_COLLECTION)
                .whereEqualTo("assignedTo", userId)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val tasks = snapshot.documents.mapNotNull { doc ->
                try {
                    documentToTask(doc.data ?: return@mapNotNull null, doc.id)
                } catch (e: Exception) {
                    null
                }
            }

            // Save to local database
            taskDao.insertTasks(tasks.map { it.toEntity() })

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun saveTaskToFirestore(task: Task) {
        val taskMap = hashMapOf(
            "id" to task.id,
            "title" to task.title,
            "description" to task.description,
            "assignedTo" to task.assignedTo,
            "assignedBy" to task.assignedBy,
            "priority" to task.priority.name,
            "status" to task.status.name,
            "completionPercentage" to task.completionPercentage,
            "createdAt" to task.createdAt,
            "updatedAt" to task.updatedAt,
            "deadline" to task.deadline,
            "estimatedHours" to task.estimatedHours,
            "tags" to task.tags,
            "assignedToName" to task.assignedToName,
            "assignedByName" to task.assignedByName
        )

        firestore.collection(TASKS_COLLECTION)
            .document(task.id)
            .set(taskMap)
            .await()
    }

    private suspend fun savePhaseToFirestore(phase: TaskPhase) {
        val phaseMap = hashMapOf(
            "id" to phase.id,
            "taskId" to phase.taskId,
            "title" to phase.title,
            "description" to phase.description,
            "isCompleted" to phase.isCompleted,
            "completedAt" to phase.completedAt,
            "order" to phase.order,
            "isCustom" to phase.isCustom,
            "createdBy" to phase.createdBy,
            "createdAt" to phase.createdAt
        )

        firestore.collection(TASKS_COLLECTION)
            .document(phase.taskId)
            .collection(PHASES_COLLECTION)
            .document(phase.id)
            .set(phaseMap)
            .await()
    }

    private fun createDefaultPhases(taskId: String): List<TaskPhase> {
        return listOf(
            TaskPhase(
                id = UUID.randomUUID().toString(),
                taskId = taskId,
                title = "Task Created",
                description = "Task has been created and assigned",
                isCompleted = true,
                completedAt = System.currentTimeMillis(),
                order = 0,
                isCustom = false,
                createdBy = "system"
            ),
            TaskPhase(
                id = UUID.randomUUID().toString(),
                taskId = taskId,
                title = "In Progress",
                description = "Work has started on this task",
                isCompleted = false,
                order = 1,
                isCustom = false,
                createdBy = "system"
            ),
            TaskPhase(
                id = UUID.randomUUID().toString(),
                taskId = taskId,
                title = "Under Review",
                description = "Task is being reviewed",
                isCompleted = false,
                order = 2,
                isCustom = false,
                createdBy = "system"
            ),
            TaskPhase(
                id = UUID.randomUUID().toString(),
                taskId = taskId,
                title = "Completed",
                description = "Task has been completed successfully",
                isCompleted = false,
                order = 3,
                isCustom = false,
                createdBy = "system"
            )
        )
    }

    private suspend fun updateTaskCompletionFromPhases(taskId: String) {
        try {
            val totalPhases = taskPhaseDao.getPhaseCountForTask(taskId)
            val completedPhases = taskPhaseDao.getCompletedPhaseCountForTask(taskId)

            val percentage = if (totalPhases > 0) {
                (completedPhases * 100) / totalPhases
            } else {
                0
            }

            updateTaskProgress(taskId, percentage)
        } catch (e: Exception) {
            // Handle error silently
        }
    }

    private fun documentToTask(data: Map<String, Any>, id: String): Task {
        return Task(
            id = id,
            title = data["title"] as? String ?: "",
            description = data["description"] as? String ?: "",
            assignedTo = data["assignedTo"] as? String ?: "",
            assignedBy = data["assignedBy"] as? String ?: "",
            priority = try {
                TaskPriority.valueOf(data["priority"] as? String ?: "MEDIUM")
            } catch (e: Exception) {
                TaskPriority.MEDIUM
            },
            status = try {
                TaskStatus.valueOf(data["status"] as? String ?: "PENDING")
            } catch (e: Exception) {
                TaskStatus.PENDING
            },
            completionPercentage = (data["completionPercentage"] as? Long)?.toInt() ?: 0,
            createdAt = data["createdAt"] as? Long ?: System.currentTimeMillis(),
            updatedAt = data["updatedAt"] as? Long ?: System.currentTimeMillis(),
            deadline = data["deadline"] as? Long,
            estimatedHours = (data["estimatedHours"] as? Long)?.toInt(),
            tags = (data["tags"] as? List<String>) ?: emptyList(),
            assignedToName = data["assignedToName"] as? String ?: "",
            assignedByName = data["assignedByName"] as? String ?: ""
        )
    }
}
