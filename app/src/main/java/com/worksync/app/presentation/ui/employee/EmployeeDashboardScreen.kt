package com.worksync.app.presentation.ui.employee

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.worksync.app.domain.model.Task
import com.worksync.app.domain.model.User
import com.worksync.app.domain.model.enums.TaskStatus
import com.worksync.app.presentation.ui.common.components.DropdownMenuTrigger
import com.worksync.app.presentation.ui.common.components.TaskCard
import com.worksync.app.presentation.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeDashboardScreen(
    currentUser: User,
    taskViewModel: TaskViewModel = hiltViewModel(),
    onLogout: () -> Unit,
    onTaskClick: (Task) -> Unit
) {
    val taskUiState by taskViewModel.uiState.collectAsState()
    var filterStatus by remember { mutableStateOf<TaskStatus?>(null) }

    LaunchedEffect(Unit) {
        taskViewModel.loadTasksForUser(currentUser.id)
        taskViewModel.syncTasks()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Employee Dashboard") },
                actions = {
                    Button(onClick = onLogout) {
                        Text("Logout")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Filter row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Filter:", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.width(8.dp))

                DropdownMenuTrigger<TaskStatus>(
                    label = filterStatus?.name ?: "All",
                    items = TaskStatus.values().toList(),
                    selected = filterStatus,
                    onSelected = { status ->
                        filterStatus = status
                        taskViewModel.filterTasksByStatus(status)
                    }
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            // Task list
            if (taskUiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                val tasks = taskViewModel.getFilteredTasks()
                if (tasks.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No tasks found.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    Column {
                        tasks.forEach { task ->
                            TaskCard(
                                task = task,
                                onClick = { onTaskClick(task) }
                            )
                        }
                    }
                }
            }
        }
    }
}
