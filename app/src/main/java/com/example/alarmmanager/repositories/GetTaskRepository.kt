package com.example.alarmmanager.repositories

import com.example.alarmmanager.dataclasses.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class GetTaskRepository {

    val firestore = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser

    suspend fun getTask(): List<Task> {
        return if (currentUser != null) {
            try {
                val taskCollection = firestore.collection("User")
                    .document(FirebaseAuth.getInstance().currentUser?.uid ?: "")
                    .collection("tasks").orderBy("startDate")
                taskCollection.get().await().toObjects(Task::class.java)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
}
