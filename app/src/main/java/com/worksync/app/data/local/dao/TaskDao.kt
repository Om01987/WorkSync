package com.worksync.app.data.local.dao

import androidx.room.*
import com.worksync.app.data.local.entities.TaskEntity
import com.worksync.app.domain.model.enums.TaskStatus
import com.worksync.app.domain.model.enums.TaskPriority
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): TaskEntity?

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE assignedTo = :userId ORDER BY createdAt DESC")
    fun getTasksAssignedToUser(userId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE assignedBy = :userId ORDER BY createdAt DESC")
    fun getTasksCreatedByUser(userId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY createdAt DESC")
    fun getTasksByStatus(status: TaskStatus): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE priority = :priority ORDER BY createdAt DESC")
    fun getTasksByPriority(priority: TaskPriority): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE assignedTo = :userId AND status = :status ORDER BY createdAt DESC")
    fun getUserTasksByStatus(userId: String, status: TaskStatus): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE deadline IS NOT NULL AND deadline < :currentTime AND status != :completedStatus ORDER BY deadline ASC")
    fun getOverdueTasks(currentTime: Long, completedStatus: TaskStatus = TaskStatus.COMPLETED): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE deadline IS NOT NULL AND deadline BETWEEN :startTime AND :endTime ORDER BY deadline ASC")
    fun getTasksDueInRange(startTime: Long, endTime: Long): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)

    @Query("UPDATE tasks SET status = :status, updatedAt = :updatedAt WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: String, status: TaskStatus, updatedAt: Long)

    @Query("UPDATE tasks SET completionPercentage = :percentage, updatedAt = :updatedAt WHERE id = :taskId")
    suspend fun updateTaskProgress(taskId: String, percentage: Int, updatedAt: Long)

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
}
