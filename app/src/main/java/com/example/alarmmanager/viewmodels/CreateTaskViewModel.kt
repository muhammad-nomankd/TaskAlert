import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.alarmmanager.dataclasses.Task
import com.example.alarmmanager.repositories.CreateTaskRepository

class CreateTaskViewModel : ViewModel() {

    private val repository = CreateTaskRepository()

    fun saveTask(
        taskTitle: String,
        taskDescription: String,
        startDate: String,
        endDate: String,
        startTime: String,
        endTime: String,
        taskPriority: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
        context: Context,
        status: String
    ) {
        val task = Task(
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
}
