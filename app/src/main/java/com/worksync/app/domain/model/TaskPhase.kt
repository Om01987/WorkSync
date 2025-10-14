package com.worksync.app.domain.model

data class TaskPhase(
    val id: String = "",
    val taskId: String = "",
    val title: String = "",
    val description: String = "",
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val order: Int = 0,
    val isCustom: Boolean = false,
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
