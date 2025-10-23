package com.worksync.app.presentation.ui.admin

import com.worksync.app.presentation.viewmodel.UserViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.worksync.app.domain.model.User
import com.worksync.app.domain.model.enums.TaskPriority
import com.worksync.app.presentation.ui.common.components.CustomButton
import com.worksync.app.presentation.ui.common.components.CustomTextField
import com.worksync.app.presentation.viewmodel.TaskViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    currentUser: User,
    taskViewModel: TaskViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onTaskCreated: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedEmployee by remember { mutableStateOf<User?>(null) }
    var selectedPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var showEmployeeDialog by remember { mutableStateOf(false) }

    val taskUiState by taskViewModel.uiState.collectAsState()
    val userUiState by userViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        userViewModel.loadEmployees()
    }

    LaunchedEffect(taskUiState.successMessage) {
        if (taskUiState.successMessage != null) {
            onTaskCreated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Task") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Title Field
            CustomTextField(
                value = title,
                onValueChange = { title = it },
                label = "Task Title",
                leadingIcon = Icons.Default.Title,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description Field
            CustomTextField(
                value = description,
                onValueChange = { description = it },
                label = "Task Description",
                leadingIcon = Icons.Default.Description,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Employee Selection
            OutlinedCard(
                onClick = { showEmployeeDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row {
                        Icon(Icons.Default.Person, "Assign to")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(selectedEmployee?.name ?: "Select Employee")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Priority Selection
            Text("Priority:", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskPriority.values().forEach { priority ->
                    FilterChip(
                        selected = selectedPriority == priority,
                        onClick = { selectedPriority = priority },
                        label = { Text(priority.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Error/Success Messages
            taskUiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Create Button
            CustomButton(
                text = "Create Task",
                onClick = {
                    selectedEmployee?.let { employee ->
                        scope.launch {
                            taskViewModel.createTask(
                                title = title,
                                description = description,
                                assignedTo = employee.id,
                                assignedBy = currentUser.id,
                                assignedToName = employee.name,
                                assignedByName = currentUser.name,
                                priority = selectedPriority,
                                deadline = null
                            )
                        }
                    }
                },
                isLoading = taskUiState.isLoading,
                enabled = title.isNotBlank() && description.isNotBlank() && selectedEmployee != null
            )
        }

        // Employee Selection Dialog
        if (showEmployeeDialog) {
            AlertDialog(
                onDismissRequest = { showEmployeeDialog = false },
                title = { Text("Select Employee") },
                text = {
                    Column {
                        if (userUiState.isLoading) {
                            CircularProgressIndicator()
                        } else if (userUiState.employees.isEmpty()) {
                            Text("No employees found. Please register employees first or refresh.")
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(onClick = { userViewModel.refreshEmployeesOnce() }) {
                                Text("Refresh")
                            }
                        } else {
                            userUiState.employees.forEach { employee ->
                                TextButton(
                                    onClick = {
                                        selectedEmployee = employee
                                        showEmployeeDialog = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "${employee.name} (${employee.email})",
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                Divider()
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showEmployeeDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}
