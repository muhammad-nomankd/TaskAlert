package com.durranitech.taskalert.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.durranitech.taskalert.dataclasses.Task
import com.durranitech.taskalert.repositories.GetTaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class GetTaskViewModel : ViewModel() {
    private val repository = GetTaskRepository()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private val _tasksForUpCommingCategory = MutableStateFlow<List<Task>>(emptyList())
    val tasksForUpCommingCategory: StateFlow<List<Task>> = _tasksForUpCommingCategory

    private val _filteredTasks = MutableLiveData<List<Task>>(emptyList())
    var filteredTasks: LiveData<List<Task>> = _filteredTasks

    private val _filteredTasksofMonth = MutableLiveData<List<Task>>(emptyList())
    var filteredTasksofMonth: LiveData<List<Task>> = _filteredTasksofMonth

    private val _filteredTasksofDay = MutableLiveData<List<Task>>(emptyList())
    var filteredTasksofDay: LiveData<List<Task>> = _filteredTasksofDay

    var isloading by mutableStateOf(false)


    init {
        fetchTasks()
    }

    private fun fetchTasks() {
        viewModelScope.launch {
            try {
                val taskList = repository.getTask()
                updateTaskStatuses(taskList)
                _tasks.value = taskList
                _filteredTasks.value = taskList
            } catch (e: Exception) {
                emptyList<Task>()
            }
        }
    }


    private fun updateTaskStatuses(taskList: List<Task>) {
        val dateFormats = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val currentTime = System.currentTimeMillis()
        try {

            taskList.forEach { task ->
                val endT = dateFormats.parse("${task.endDate} ${task.endTime}")?.time ?: 0L
                val startT = dateFormats.parse("${task.startDate} ${task.startTime}")?.time ?: 0L
                task.status =
                    if (currentTime > endT) "Completed" else if (currentTime in (startT + 1)..<endT) "In Progress" else "Pending"
            }
        } catch (e: Exception) {
            Log.d("problem", "unable to filter tasks")
        }
    }


    suspend fun filterTasks(category: String) {
        viewModelScope.launch {

            try {
                val taskrepo = repository.getTask()
                updateTaskStatuses(taskrepo)
                val taskfilter = when (category) {
                    "All" -> taskrepo
                    "In Progress" -> taskrepo.filter { it.status == category }
                    "Completed" -> taskrepo.filter { it.status == category }
                    else -> taskrepo
                }
                _filteredTasks.value = taskfilter
            } catch (e: Exception) {
                emptyList<Task>()
            }
        }
    }

    suspend fun filterTasksForUpCommingCategory(category: String) {
        viewModelScope.launch {
            try {
                val taskRepo = repository.getTask()
                updateTaskStatuses(taskRepo)
                val taskFilter = when (category) {
                    "In Progress and Pending" -> taskRepo.filter {
                        it.status == "In Progress" || it.status == "Pending"
                    }
                    else -> taskRepo
                }
                _tasksForUpCommingCategory.value = taskFilter
            } catch (e: Exception) {
                _tasksForUpCommingCategory.value = emptyList()
                // You might want to log the exception here or handle it appropriately
            }
        }
    }

    fun fetchTaskForDay(day: Int, month: Int, year: Int) {
        viewModelScope.launch {
            isloading = true
            val filterList = _tasks.value.filter {
                val taskDate =
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.startDate)
                val calendar = Calendar.getInstance()
                taskDate?.let {
                    calendar.time = it
                }
                val isSameDay = calendar.get(Calendar.DAY_OF_MONTH) == day
                        && calendar.get(Calendar.MONTH) + 1 == month
                        && calendar.get(Calendar.YEAR) == year
                isSameDay
            }
            _filteredTasksofDay.postValue(filterList)
        }
    }


    fun fetchTaskForMonth(month: Int, year: Int): List<Task> {
        Log.d("bug fixing month", "$month $year")
        viewModelScope.launch {
            isloading = true
            val filterListForMonth = _tasks.value.filter {
                val taskDate =
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.startDate)
                val calendar = Calendar.getInstance()
                taskDate?.let {
                    calendar.time = it
                }
                val isSameMonth =
                    calendar.get(Calendar.MONTH) + 1 == month && calendar.get(Calendar.YEAR) == year
                isSameMonth
            }
            _filteredTasksofMonth.value = (filterListForMonth)
            Log.d("bug fixing month", filterListForMonth.toString())
        }
        return _filteredTasksofMonth.value ?: emptyList()
    }

}

