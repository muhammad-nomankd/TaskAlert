package com.example.alarmmanager.dataclasses

data class Task(
    val taskId: String ="",
    val title: String = "",
    val description: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val priority: String = "",
    var status: String = ""
)
