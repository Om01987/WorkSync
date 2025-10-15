package com.worksync.app.data.local.dao

import androidx.room.*
import com.worksync.app.data.local.entities.TaskPhaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskPhaseDao {

    @Query("SELECT * FROM task_phases WHERE id = :phaseId")
    suspend fun getPhaseById(phaseId: String): TaskPhaseEntity?

    @Query("SELECT * FROM task_phases WHERE taskId = :taskId ORDER BY `order` ASC")
    fun getPhasesByTaskId(taskId: String): Flow<List<TaskPhaseEntity>>

    @Query("SELECT * FROM task_phases WHERE taskId = :taskId ORDER BY `order` ASC")
    suspend fun getPhasesByTaskIdSync(taskId: String): List<TaskPhaseEntity>

    @Query("SELECT * FROM task_phases WHERE taskId = :taskId AND isCompleted = 1 ORDER BY `order` ASC")
    fun getCompletedPhasesByTaskId(taskId: String): Flow<List<TaskPhaseEntity>>

    @Query("SELECT * FROM task_phases WHERE taskId = :taskId AND isCompleted = 0 ORDER BY `order` ASC")
    fun getPendingPhasesByTaskId(taskId: String): Flow<List<TaskPhaseEntity>>

    @Query("SELECT * FROM task_phases WHERE taskId = :taskId AND isCustom = 1 ORDER BY `order` ASC")
    fun getCustomPhasesByTaskId(taskId: String): Flow<List<TaskPhaseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhase(phase: TaskPhaseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhases(phases: List<TaskPhaseEntity>)

    @Update
    suspend fun updatePhase(phase: TaskPhaseEntity)

    @Delete
    suspend fun deletePhase(phase: TaskPhaseEntity)

    @Query("DELETE FROM task_phases WHERE id = :phaseId")
    suspend fun deletePhaseById(phaseId: String)

    @Query("DELETE FROM task_phases WHERE taskId = :taskId")
    suspend fun deletePhasesByTaskId(taskId: String)

    @Query("UPDATE task_phases SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :phaseId")
    suspend fun updatePhaseCompletion(phaseId: String, isCompleted: Boolean, completedAt: Long?)

    @Query("SELECT COUNT(*) FROM task_phases WHERE taskId = :taskId")
    suspend fun getPhaseCountForTask(taskId: String): Int

    @Query("SELECT COUNT(*) FROM task_phases WHERE taskId = :taskId AND isCompleted = 1")
    suspend fun getCompletedPhaseCountForTask(taskId: String): Int

    @Query("DELETE FROM task_phases")
    suspend fun deleteAllPhases()
}
