package com.durranitech.taskalert

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.compose.ui.util.trace
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class TaskNotificationManager(private val context: Context) {

    fun checkTasksAndScheduleNotifications(userId: String) {
        Log.d("TaskNotification", "checkTasksAndScheduleNotifications called")
        val db = FirebaseFirestore.getInstance()
        val reference = db.collection("User").document(userId).collection("tasks")
        reference.get().addOnSuccessListener { documents ->
            for (document in documents) {
                val taskTitle = document.getString("title") ?: "task"
                val taskDescription = document.getString("description") ?: "some important task"
                val startTime = document.getString("startTime") ?: "00:00"
                val startDate = document.getString("startDate") ?: "1970-01-01"

                val taskStartTimeInMillis = getTaskStartTimeInMillis(startDate,startTime)
                val currentTime = System.currentTimeMillis()

                val timeDifference = Math.abs(currentTime - taskStartTimeInMillis)

                if (timeDifference <= 6000){
                    showNotificationImmediately(taskTitle,taskDescription)
                } else if (taskStartTimeInMillis > currentTime) {
                    scheduleNotification(taskTitle,taskDescription,taskStartTimeInMillis-currentTime)
                }
            }
        }
    }

    fun showNotificationImmediately(taskTitle: String, taskDescription: String) {
        val notificationId = System.currentTimeMillis().toInt()

        // Create the notification builder
        val notificationBuilder = NotificationCompat.Builder(context, "taskAlertChannel")
            .setSmallIcon(R.drawable.appicon) // Replace with your actual app icon
            .setContentTitle(taskTitle)
            .setContentText(taskDescription)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.d("TaskNotification", "Permission not granted")
                return
            }
        }

        // Post the notification if permission is granted or not required
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    fun scheduleNotification(taskTitle:String, taskDescription:String,delay:Long){
        val data = workDataOf(
            "TASK_TITLE" to taskTitle,
            "TASK_DESCRIPTION" to taskDescription
        )

        val notificationWork = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delay,TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueue(notificationWork)
    }

    private fun getTaskStartTimeInMillis(startDate: String, startTime: String): Long {
        val taskStartDateAndTime = "$startDate $startTime"
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return try {
            Log.d("task start time",taskStartDateAndTime)
            val date = sdf.parse(taskStartDateAndTime)
            date.time ?: 0L
        } catch (e:Exception){
            e.printStackTrace()
            0L
        }

    }
}