package com.worksync.app.presentation.ui.common.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.worksync.app.domain.model.User
import com.worksync.app.domain.model.enums.TaskPriority
import com.worksync.app.domain.model.enums.TaskStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditStatusBottomSheet(
    currentStatus: TaskStatus,
    onStatusSelected: (TaskStatus) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Change Status",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn {
                items(TaskStatus.values().size) { index ->
                    val status = TaskStatus.values()[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = status.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        if (currentStatus == status) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            RadioButton(
                                selected = false,
                                onClick = {
                                    onStatusSelected(status)
                                    onDismiss()
                                }
                            )
                        }
                    }
                    if (index < TaskStatus.values().size - 1) {
                        HorizontalDivider()
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPriorityBottomSheet(
    currentPriority: TaskPriority,
    onPrioritySelected: (TaskPriority) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Change Priority",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn {
                items(TaskPriority.values().size) { index ->
                    val priority = TaskPriority.values()[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = priority.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        if (currentPriority == priority) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            RadioButton(
                                selected = false,
                                onClick = {
                                    onPrioritySelected(priority)
                                    onDismiss()
                                }
                            )
                        }
                    }
                    if (index < TaskPriority.values().size - 1) {
                        HorizontalDivider()
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTitleDescriptionDialog(
    currentTitle: String,
    currentDescription: String,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean = false
) {
    var newTitle by remember { mutableStateOf(currentTitle) }
    var newDescription by remember { mutableStateOf(currentDescription) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Task") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text("Title") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = newDescription,
                    onValueChange = { newDescription = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onTitleChange(newTitle)
                    onDescriptionChange(newDescription)
                    onSave()
                },
                enabled = newTitle.isNotBlank() && newDescription.isNotBlank() && !isLoading
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAssigneeBottomSheet(
    employees: List<User>,
    currentAssignee: User?,
    onAssigneeSelected: (User) -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean = false
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Reassign To",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (employees.isEmpty()) {
                Text("No employees available")
            } else {
                LazyColumn {
                    items(employees.size) { index ->
                        val employee = employees[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = employee.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = employee.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (currentAssignee?.id == employee.id) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Assigned",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                RadioButton(
                                    selected = false,
                                    onClick = {
                                        onAssigneeSelected(employee)
                                        onDismiss()
                                    }
                                )
                            }
                        }
                        if (index < employees.size - 1) {
                            HorizontalDivider()
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDeadlineDialog(
    currentDeadline: Long?,
    onDeadlineSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean = false
) {
    var selectedDateMillis by remember { mutableStateOf(currentDeadline) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Deadline") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (selectedDateMillis != null) {
                    Text(
                        text = "Selected: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(selectedDateMillis!!))}"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { selectedDateMillis = null }) {
                        Text("Clear Deadline")
                    }
                } else {
                    Text("No deadline set")
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Button(onClick = {
                    // For now, set deadline to 7 days from today
                    selectedDateMillis = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)
                }) {
                    Text("Set to 7 days from now")
                }
                Button(onClick = {
                    // Set deadline to 30 days from today
                    selectedDateMillis = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000)
                }) {
                    Text("Set to 30 days from now")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDeadlineSelected(selectedDateMillis)
                    onDismiss()
                },
                enabled = !isLoading
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
