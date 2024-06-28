package com.example.alarmmanager.repositories

import com.example.alarmmanager.dataclasses.Task
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
}

