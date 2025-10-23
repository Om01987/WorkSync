package com.worksync.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worksync.app.domain.model.Task
import com.worksync.app.domain.model.TaskPhase
import com.worksync.app.domain.model.enums.TaskPriority
import com.worksync.app.domain.model.enums.TaskStatus
import com.worksync.app.domain.usecase.task.CreateTaskUseCase
import com.worksync.app.domain.usecase.task.DeleteTaskUseCase
import com.worksync.app.domain.usecase.task.GetTasksUseCase
import com.worksync.app.domain.usecase.task.UpdateTaskUseCase
import com.worksync.app.domain.repository.TaskRepository
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
}
