package com.durranitech.taskalert.repositories

import com.durranitech.taskalert.modelclasses.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateTaskRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    fun saveTask(task: Task, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        if (currentUser != null) {
            // Convert Task to Map explicitly to include all fields
            val taskMap = mapOf(
                "taskId" to task.taskId,
                "title" to task.title,
                "description" to task.description,
                "startDate" to task.startDate,
                "endDate" to task.endDate,
                "startTime" to task.startTime,
                "endTime" to task.endTime,
                "priority" to task.priority,
                "status" to task.status
            )

            firestore.collection("User")
                .document(currentUser.uid)
                .collection("tasks")
                .add(taskMap)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { exception -> onFailure(exception) }
        } else {
            onFailure(Exception("User not authenticated"))
        }
    }

    fun updateTask(
        task: Task,
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
        updatedFields: MutableMap<String, Any>
    ) {
        val db = FirebaseFirestore.getInstance()
        val uId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val taskRef = db.collection("User").document(uId).collection("tasks")

        taskRef.whereEqualTo("taskId", task.taskId)
            .get()
            .addOnSuccessListener { querySnapShot ->
                for (document in querySnapShot.documents) {
                    taskRef.document(document.id).update(updatedFields)
                        .addOnSuccessListener {
                            onSuccess()
                        }
                        .addOnFailureListener {
                            onFailure()
                        }
                }
            }


    }

}

