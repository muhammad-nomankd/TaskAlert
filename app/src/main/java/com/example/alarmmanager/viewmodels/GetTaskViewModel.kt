package com.example.alarmmanager.viewmodels

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

    private val _filteredTasks = MutableStateFlow<List<Task>>(emptyList())
    val filteredTasks: StateFlow<List<Task>> = _filteredTasks

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
            }
        }
    }

    fun filterTasks(category: String) {
        _filteredTasks.value = if (category == "All") {
            _tasks.value
        } else {
            _tasks.value.filter { it.status == category }
        }
    }
}