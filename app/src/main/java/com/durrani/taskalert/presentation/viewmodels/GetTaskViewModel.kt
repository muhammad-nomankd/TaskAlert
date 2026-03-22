package com.durrani.taskalert.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.durrani.taskalert.domain.models.Task
import com.durrani.taskalert.data.repository.GetTaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class GetTaskViewModel : ViewModel() {
    private val repository = GetTaskRepository()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())

    private val _tasksForUpComingCategory = MutableStateFlow<List<Task>>(emptyList())
    val tasksForUpComingCategory: StateFlow<List<Task>> = _tasksForUpComingCategory

    private val _filteredTasks = MutableLiveData<List<Task>>(emptyList())
    var filteredTasks: LiveData<List<Task>> = _filteredTasks

    private val _filteredTasksofMonth = MutableStateFlow<List<Task>>(emptyList())
    var filteredTasksofMonth: StateFlow<List<Task>> = _filteredTasksofMonth

    private val _filteredTasksofDay = MutableLiveData<List<Task>>(emptyList())
    var filteredTasksofDay: LiveData<List<Task>> = _filteredTasksofDay

    var _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading



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


     fun filterTasks(category: String) {
        viewModelScope.launch {
            _isLoading.value = true
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
                _isLoading.value = false
            } catch (e: Exception) {
                emptyList<Task>()
                _isLoading.value = false
            }
        }
    }

    fun filterTasksForUpComingCategory(category: String) {
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
                _tasksForUpComingCategory.value = taskFilter
            } catch (e: Exception) {
                _tasksForUpComingCategory.value = emptyList()
            }
        }
    }

    fun fetchTaskForDay(day: Int, month: Int, year: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val filterList = _tasks.value.filter {
                val taskDate =
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.startDate)
                val calendar = Calendar.getInstance()
                taskDate?.let {
                    calendar.time = it
                }
                val isSameDay =
                    calendar.get(Calendar.DAY_OF_MONTH) == day && calendar.get(Calendar.MONTH) + 1 == month && calendar.get(
                        Calendar.YEAR
                    ) == year
                isSameDay
            }
            _filteredTasksofDay.postValue(filterList)
        }
    }


    fun fetchTaskForMonth(month: Int, year: Int): List<Task> {
        Log.d("bug fixing month", "$month $year")
        viewModelScope.launch {
            _isLoading.value = true
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
        return _filteredTasksofMonth.value
    }

}

