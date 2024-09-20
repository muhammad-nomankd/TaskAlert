package com.durranitech.taskalert

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class NotificationWorker(context:Context, params: WorkerParameters):CoroutineWorker(context,params) {
    override suspend fun doWork(): Result {
        val taskTitle = inputData.getString("TASK_TITLE")?: "Task Title"
        val taskDescription = inputData.getString("TASK_DESCRIPTION")?:"Task Description"

        TaskNotificationManager(applicationContext).showNotificationImmediately(taskTitle,taskDescription)
        return Result.success()

    }
}