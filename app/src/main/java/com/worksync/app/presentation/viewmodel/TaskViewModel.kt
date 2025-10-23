package com.worksync.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worksync.app.domain.model.Task
import com.worksync.app.domain.model.TaskPhase
import com.worksync.app.domain.model.enums.TaskPriority
import com.worksync.app.domain.model.enums.TaskStatus
import com.worksync.app.domain.repository.TaskRepository
import com.worksync.app.domain.usecase.task.CreateTaskUseCase
import com.worksync.app.domain.usecase.task.DeleteTaskUseCase
import com.worksync.app.domain.usecase.task.GetTasksUseCase
import com.worksync.app.domain.usecase.task.UpdateTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskUiState(
    val tasks: List<Task> = emptyList(),
    val selectedTask: Task? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val filterStatus: TaskStatus? = null
)

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val createTaskUseCase: CreateTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val getTasksUseCase: GetTasksUseCase,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    fun loadAllTasks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            getTasksUseCase.getAllTasks().collect { tasks ->
                _uiState.value = _uiState.value.copy(
                    tasks = tasks,
                    isLoading = false
                )
            }
        }
    }

    fun loadTasksForUser(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            getTasksUseCase.getTasksForUser(userId).collect { tasks ->
                _uiState.value = _uiState.value.copy(
                    tasks = tasks,
                    isLoading = false
                )
            }
        }
    }

    fun loadTasksCreatedByUser(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            getTasksUseCase.getTasksCreatedByUser(userId).collect { tasks ->
                _uiState.value = _uiState.value.copy(
                    tasks = tasks,
                    isLoading = false
                )
            }
        }
    }

    fun filterTasksByStatus(status: TaskStatus?) {
        _uiState.value = _uiState.value.copy(filterStatus = status)
    }

    fun getFilteredTasks(): List<Task> {
        val allTasks = _uiState.value.tasks
        val filterStatus = _uiState.value.filterStatus

        return if (filterStatus != null) {
            allTasks.filter { it.status == filterStatus }
        } else {
            allTasks
        }
    }

    fun selectTask(taskId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            getTasksUseCase.getTaskWithPhases(taskId).collect { task ->
                _uiState.value = _uiState.value.copy(
                    selectedTask = task,
                    isLoading = false
                )
            }
        }
    }

    fun createTask(
        title: String,
        description: String,
        assignedTo: String,
        assignedBy: String,
        assignedToName: String,
        assignedByName: String,
        priority: TaskPriority,
        deadline: Long?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val task = Task(
                title = title,
                description = description,
                assignedTo = assignedTo,
                assignedBy = assignedBy,
                assignedToName = assignedToName,
                assignedByName = assignedByName,
                priority = priority,
                deadline = deadline,
                status = TaskStatus.PENDING
            )

            createTaskUseCase(task)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Task created successfully!"
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to create task"
                    )
                }
        }
    }

    // Existing status update (kept for compatibility in dashboards)
    fun updateTaskStatus(taskId: String, status: TaskStatus) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            updateTaskUseCase.updateTaskStatus(taskId, status)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Status updated successfully!"
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to update status"
                    )
                }
        }
    }

    fun updateTaskProgress(taskId: String, percentage: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            updateTaskUseCase.updateTaskProgress(taskId, percentage)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Progress updated!"
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to update progress"
                    )
                }
        }
    }

    fun addPhaseToTask(taskId: String, title: String, description: String, createdBy: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val currentPhases = _uiState.value.selectedTask?.phases ?: emptyList()
            val newOrder = currentPhases.maxOfOrNull { it.order }?.plus(1) ?: 0

            val phase = TaskPhase(
                taskId = taskId,
                title = title,
                description = description,
                order = newOrder,
                isCustom = true,
                createdBy = createdBy
            )

            taskRepository.addPhaseToTask(taskId, phase)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Phase added successfully!"
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to add phase"
                    )
                }
        }
    }

    fun markPhaseCompleted(phaseId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            taskRepository.markPhaseCompleted(phaseId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Phase marked as completed!"
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to update phase"
                    )
                }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            deleteTaskUseCase(taskId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Task deleted successfully!"
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to delete task"
                    )
                }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    fun syncTasks() {
        viewModelScope.launch {
            taskRepository.syncTasks()
        }
    }

    // ===================== Edit Methods (New) =====================

    fun updateTaskStatus(taskId: String, newStatus: TaskStatus, refreshSelected: Boolean = true) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            updateTaskUseCase.updateTaskStatus(taskId, newStatus)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Status updated to ${newStatus.name}"
                    )
                    if (refreshSelected) selectTask(taskId)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to update status"
                    )
                }
        }
    }

    fun updateTaskPriority(taskId: String, newPriority: TaskPriority) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            _uiState.value.selectedTask?.let { task ->
                val updated = task.copy(priority = newPriority, updatedAt = System.currentTimeMillis())
                updateTaskUseCase.updateTask(updated)
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "Priority updated to ${newPriority.name}"
                        )
                        selectTask(taskId)
                    }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Failed to update priority"
                        )
                    }
            } ?: run {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun updateTaskTitle(taskId: String, newTitle: String) {
        viewModelScope.launch {
            if (newTitle.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Title cannot be empty"
                )
                return@launch
            }
            _uiState.value = _uiState.value.copy(isLoading = true)
            _uiState.value.selectedTask?.let { task ->
                val updated = task.copy(title = newTitle, updatedAt = System.currentTimeMillis())
                updateTaskUseCase.updateTask(updated)
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "Title updated"
                        )
                        selectTask(taskId)
                    }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Failed to update title"
                        )
                    }
            } ?: run {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun updateTaskDescription(taskId: String, newDescription: String) {
        viewModelScope.launch {
            if (newDescription.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Description cannot be empty"
                )
                return@launch
            }
            _uiState.value = _uiState.value.copy(isLoading = true)
            _uiState.value.selectedTask?.let { task ->
                val updated = task.copy(description = newDescription, updatedAt = System.currentTimeMillis())
                updateTaskUseCase.updateTask(updated)
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "Description updated"
                        )
                        selectTask(taskId)
                    }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Failed to update description"
                        )
                    }
            } ?: run {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun updateTaskDeadline(taskId: String, newDeadline: Long?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            _uiState.value.selectedTask?.let { task ->
                val updated = task.copy(deadline = newDeadline, updatedAt = System.currentTimeMillis())
                updateTaskUseCase.updateTask(updated)
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "Deadline updated"
                        )
                        selectTask(taskId)
                    }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Failed to update deadline"
                        )
                    }
            } ?: run {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun updateTaskAssignee(taskId: String, newAssigneeId: String, newAssigneeName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            _uiState.value.selectedTask?.let { task ->
                val updated = task.copy(
                    assignedTo = newAssigneeId,
                    assignedToName = newAssigneeName,
                    updatedAt = System.currentTimeMillis()
                )
                updateTaskUseCase.updateTask(updated)
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "Task reassigned to $newAssigneeName"
                        )
                        selectTask(taskId)
                    }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Failed to reassign task"
                        )
                    }
            } ?: run {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}
