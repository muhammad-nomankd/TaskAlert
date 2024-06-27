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
                Log.d("tasks" , "no task found")
            }
        }
    }


    suspend fun filterTasks(category: String) {
        val taskrepo = repository.getTask()
        try {
            val taskfilter = when(category){
                "All" ->taskrepo
                "In Progress" -> taskrepo.filter { it.status == "In Progress" }
                "Completed" -> taskrepo.filter { it.status == "Completed"}
                else -> taskrepo
            }
            _filteredTasks.value = taskfilter
        } catch (e:Exception){
            emptyList<Task>()
        }
    }
}