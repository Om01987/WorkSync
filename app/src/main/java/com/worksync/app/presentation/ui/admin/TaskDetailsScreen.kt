package com.worksync.app.presentation.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.worksync.app.domain.model.Task
import com.worksync.app.domain.model.User
import com.worksync.app.domain.model.enums.TaskStatus
import com.worksync.app.presentation.ui.common.components.*
import com.worksync.app.presentation.viewmodel.TaskViewModel
import com.worksync.app.presentation.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailsScreen(
    taskId: String,
    currentUser: User,
    taskViewModel: TaskViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    var showAddPhaseDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditStatusSheet by remember { mutableStateOf(false) }
    var showEditPrioritySheet by remember { mutableStateOf(false) }
    var showEditTitleDescDialog by remember { mutableStateOf(false) }
    var showEditDeadlineDialog by remember { mutableStateOf(false) }
    var showEditAssigneeSheet by remember { mutableStateOf(false) }

    val taskUiState by taskViewModel.uiState.collectAsState()
    val userUiState by userViewModel.uiState.collectAsState()
    val task = taskUiState.selectedTask

    // Snackbar host state
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(taskId) {
        taskViewModel.selectTask(taskId)
        userViewModel.loadEmployees()
    }

    LaunchedEffect(taskUiState.successMessage) {
        taskUiState.successMessage?.let { msg ->
            snackbarHostState.showSnackbar(message = msg)
            taskViewModel.clearMessages()
        }
    }

    LaunchedEffect(taskUiState.errorMessage) {
        taskUiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(message = msg)
            taskViewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (currentUser.role.name == "ADMIN") {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "Delete Task")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddPhaseDialog = true }
            ) {
                Icon(Icons.Default.Add, "Add Phase")
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Text(data.visuals.message)
                }
            }
        }
    ) { padding ->
        if (taskUiState.isLoading && task == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (task != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                TaskHeaderCard(task, currentUser) { section ->
                    when (section) {
                        "status" -> showEditStatusSheet = true
                        "priority" -> showEditPrioritySheet = true
                        "title" -> showEditTitleDescDialog = true
                        "assignee" -> showEditAssigneeSheet = true
                        "deadline" -> showEditDeadlineDialog = true
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 16.dp))

                TaskProgressStepper(
                    phases = task.phases,
                    onPhaseClick = { phase ->
                        if (!phase.isCompleted) {
                            taskViewModel.markPhaseCompleted(phase.id)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Edit Dialogs and Sheets
    if (showEditStatusSheet && task != null) {
        EditStatusBottomSheet(
            currentStatus = task.status,
            onStatusSelected = { newStatus ->
                taskViewModel.updateTaskStatus(task.id, newStatus)
            },
            onDismiss = { showEditStatusSheet = false }
        )
    }

    if (showEditPrioritySheet && task != null) {
        EditPriorityBottomSheet(
            currentPriority = task.priority,
            onPrioritySelected = { newPriority ->
                taskViewModel.updateTaskPriority(task.id, newPriority)
            },
            onDismiss = { showEditPrioritySheet = false }
        )
    }

    if (showEditTitleDescDialog && task != null) {
        EditTitleDescriptionDialog(
            currentTitle = task.title,
            currentDescription = task.description,
            onTitleChange = { newTitle ->
                taskViewModel.updateTaskTitle(task.id, newTitle)
            },
            onDescriptionChange = { newDesc ->
                taskViewModel.updateTaskDescription(task.id, newDesc)
            },
            onSave = { showEditTitleDescDialog = false },
            onDismiss = { showEditTitleDescDialog = false },
            isLoading = taskUiState.isLoading
        )
    }

    if (showEditDeadlineDialog && task != null) {
        EditDeadlineDialog(
            currentDeadline = task.deadline,
            onDeadlineSelected = { newDeadline ->
                taskViewModel.updateTaskDeadline(task.id, newDeadline)
            },
            onDismiss = { showEditDeadlineDialog = false },
            isLoading = taskUiState.isLoading
        )
    }

    if (showEditAssigneeSheet && task != null) {
        EditAssigneeBottomSheet(
            employees = userUiState.employees,
            currentAssignee = userUiState.employees.find { it.id == task.assignedTo },
            onAssigneeSelected = { newAssignee ->
                taskViewModel.updateTaskAssignee(task.id, newAssignee.id, newAssignee.name)
            },
            onDismiss = { showEditAssigneeSheet = false },
            isLoading = userUiState.isLoading
        )
    }

    if (showAddPhaseDialog && task != null) {
        AddPhaseDialog(
            onDismiss = { showAddPhaseDialog = false },
            onConfirm = { title, description ->
                taskViewModel.addPhaseToTask(task.id, title, description, currentUser.id)
                showAddPhaseDialog = false
            }
        )
    }

    if (showDeleteDialog && task != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete this task? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        taskViewModel.deleteTask(task.id)
                        onNavigateBack()
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TaskHeaderCard(
    task: Task,
    currentUser: User,
    onSectionClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title (editable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                if (currentUser.role.name == "ADMIN") {
                    IconButton(onClick = { onSectionClick("title") }) {
                        Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Description (editable)
            Text(
                text = task.description,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Status (editable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Status", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(task.status.name, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
                if (currentUser.role.name == "ADMIN") {
                    IconButton(onClick = { onSectionClick("status") }) {
                        Icon(Icons.Default.Edit, "Edit Status", modifier = Modifier.size(20.dp))
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Priority (editable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Priority", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(task.priority.name, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
                if (currentUser.role.name == "ADMIN") {
                    IconButton(onClick = { onSectionClick("priority") }) {
                        Icon(Icons.Default.Edit, "Edit Priority", modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Assignee (editable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Assigned To", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(task.assignedToName, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
                if (currentUser.role.name == "ADMIN") {
                    IconButton(onClick = { onSectionClick("assignee") }) {
                        Icon(Icons.Default.Edit, "Edit Assignee", modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Deadline (editable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Deadline", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        task.deadline?.let { formatDate(it) } ?: "No deadline",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (currentUser.role.name == "ADMIN") {
                    IconButton(onClick = { onSectionClick("deadline") }) {
                        Icon(Icons.Default.Edit, "Edit Deadline", modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Progress
            Column(modifier = Modifier.padding(top = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Progress", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${task.completionPercentage}%", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                LinearProgressIndicator(
                    progress = { task.completionPercentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .height(8.dp)
                )
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
    return format.format(date)
}

@Composable
private fun AddPhaseDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Phase") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Phase Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, description) },
                enabled = title.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
