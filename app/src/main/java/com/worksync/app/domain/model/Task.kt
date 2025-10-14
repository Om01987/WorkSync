package com.worksync.app.domain.model

import com.worksync.app.domain.model.enums.TaskPriority
import com.worksync.app.domain.model.enums.TaskStatus

data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val assignedTo: String = "", // Employee ID
    val assignedBy: String = "", // Admin ID
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val status: TaskStatus = TaskStatus.PENDING,
    val phases: List<TaskPhase> = emptyList(),
    val completionPercentage: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deadline: Long? = null,
    val estimatedHours: Int? = null,
    val tags: List<String> = emptyList(),
    val assignedToName: String = "", // For display purposes
    val assignedByName: String = "" // For display purposes
)
