package com.example.alarmmanager.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarmmanager.dataclasses.Task
import com.example.alarmmanager.repositories.GetTaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale


class GetTaskViewModel : ViewModel() {
    private val repository = GetTaskRepository()
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private val _filteredTasks = MutableLiveData<List<Task>>(emptyList())
    var filteredTasks: LiveData<List<Task>> = _filteredTasks

    init {
        fetchTasks()
    }

    private fun fetchTasks() {
        viewModelScope.launch {
            try {
                val taskList = repository.getTask()
                _tasks.value = taskList
                _filteredTasks.value = taskList // Initialize filtered tasks
            } catch (e: Exception) {
                emptyList<Task>()
            }
        }
    }


    suspend fun filterTasks(category: String) {
        val taskrepo = repository.getTask()
        val dateFormats = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val currentTime = System.currentTimeMillis()
        try {

            taskrepo.forEach { task ->
                val endT = dateFormats.parse("${task.endDate} ${task.endTime}", )?.time ?: 0L
                task.status = if (currentTime > endT) "Completed" else "In Progress"
                Log.d("filtertask", taskrepo.toString())
            }
        } catch (e: Exception) {
            Log.d("problem", "unable to fileter tasks")
        }

        try {
            val taskfilter = when (category) {
                "All" -> taskrepo
                "In Progress" -> taskrepo.filter { it.status == "In Progress" }
                "Completed" -> taskrepo.filter { it.status == "Completed" }
                else -> taskrepo
            }
            _filteredTasks.value = taskfilter
        } catch (e: Exception) {
            emptyList<Task>()
        }
    }


}