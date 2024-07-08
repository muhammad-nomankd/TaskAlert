package com.example.alarmmanager.viewmodels

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarmmanager.dataclasses.Task
import com.example.alarmmanager.repositories.GetTaskRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class GetTaskViewModel : ViewModel() {
    private val repository = GetTaskRepository()
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private val _filteredTasks = MutableLiveData<List<Task>>(emptyList())
    var filteredTasks: LiveData<List<Task>> = _filteredTasks

    private val _fTskfordayandMonth = MutableLiveData<List<Task>>(emptyList())
    var fTskfordayandMonth: LiveData<List<Task>> = _fTskfordayandMonth

    init {
        fetchTasks()
    }

     fun fetchTasks() {
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


    suspend fun updateTaskStatuses(tasklist: List<Task>) {
        val taskrepo = repository.getTask()
        val dateFormats = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val currentTime = System.currentTimeMillis()
        try {

            tasklist.forEach { task ->
                val endT = dateFormats.parse("${task.endDate} ${task.endTime}")?.time ?: 0L
                val startT = dateFormats.parse("${task.startDate} ${task.startTime}")?.time ?: 0L
                task.status =
                    if (currentTime > endT) "Completed" else if (currentTime > startT && currentTime < endT) "In Progress" else "Pending"
                Log.d("filtertask", taskrepo.toString())
            }
        } catch (e: Exception) {
            Log.d("problem", "unable to fileter tasks")
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

    fun fetchTaskForDay(day: Int, Month: Int, Year: Int) {
        val filterList = _tasks.value.filter {
            val taskDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.startDate)
            val calendar = Calendar.getInstance()
            taskDate?.let {
                calendar.time = taskDate
            }
            val isSameDay = calendar.get(Calendar.DAY_OF_MONTH) == day
                    && (calendar.get(Calendar.MONTH) + 1) == Month
                    && calendar.get(Calendar.YEAR) == Year
            isSameDay
        }
        _fTskfordayandMonth.postValue(filterList)
        Log.d("FilterTaskFor Day", filterList.toString())

    }

    fun fetchTaskForMonth(month: Int, year: Int) {
        viewModelScope.launch {
            val filterListForMonth = _tasks.value.filter {
                val taskDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.startDate)
                val calendar2 = Calendar.getInstance()
                taskDate?.let {
                    calendar2.time = taskDate
                }
                val isSameMonth = (calendar2.get(Calendar.MONTH) + 1) == month && calendar2.get(Calendar.YEAR) == year
                isSameMonth
            }
            _fTskfordayandMonth.postValue(filterListForMonth)
        }

    }

     suspend fun deleteTask(taskId: String, status: String, context: Context, category: String) {
        val db = FirebaseFirestore.getInstance()
        val uId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val taskRef = db.collection("User").document(uId).collection("tasks")

        taskRef.whereEqualTo("taskId", taskId)
            .get()
            .addOnSuccessListener { querySnapShot->
                for (document in querySnapShot.documents ) {
                    if (status == "Completed"){
                        taskRef.document(document.id).delete().addOnSuccessListener {
                            Toast.makeText(context, "Task Deleted", Toast.LENGTH_SHORT).show()
                           viewModelScope.launch { filterTasks(category) }
                            Log.d("deleteTask", tasks.toString())
                        }.addOnFailureListener {
                            Toast.makeText(context, "Failed to delete task", Toast.LENGTH_SHORT).show()

                        }
                    }else{
                        Toast.makeText(context,"Task has not completed yet.", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }



}

