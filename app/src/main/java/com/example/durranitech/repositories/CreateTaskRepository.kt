package com.example.durranitech.repositories

import com.example.durranitech.dataclasses.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateTaskRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    fun saveTask(task: Task, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        if (currentUser != null) {
            firestore.collection("User")
                .document(currentUser.uid)
                .collection("tasks")
                .add(task)
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

