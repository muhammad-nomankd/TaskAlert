package com.example.alarmmanager.dataclasses

data class Task(
    val title: String = "",
    val description: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val priority: String = "",
    val status: String = ""
)
