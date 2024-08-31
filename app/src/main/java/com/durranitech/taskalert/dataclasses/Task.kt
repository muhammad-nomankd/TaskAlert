package com.durranitech.taskalert.dataclasses
import androidx.compose.runtime.Immutable

@Immutable
data class Task(
    val taskId: String = "",
    val title: String = "",
    val description: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val priority: String = "",
    var status: String = ""
)
