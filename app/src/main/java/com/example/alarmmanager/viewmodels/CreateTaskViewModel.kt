import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarmmanager.dataclasses.Task
import com.example.alarmmanager.repositories.CreateTaskRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreateTaskViewModel : ViewModel() {

    private val repository = CreateTaskRepository()

    fun saveTask(
        taskid: String,
        taskTitle: String,
        taskDescription: String,
        startDate: String,
        endDate: String,
        startTime: String,
        endTime: String,
        taskPriority: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
        status: String
    ) {
        val task = Task(
            taskId = taskid,
            title = taskTitle,
            description = taskDescription,
            startDate = startDate,
            endDate = endDate,
            startTime = startTime,
            endTime = endTime,
            priority = taskPriority,
            status
        )

        repository.saveTask(
            task,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    fun updateTask(
        taskid: String,
        taskTitle: String,
        taskDescription: String,
        startDate: String,
        endDate: String,
        startTime: String,
        endTime: String,
        taskPriority: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
        context: Context
    ) {
        onSuccess()
    }
}
