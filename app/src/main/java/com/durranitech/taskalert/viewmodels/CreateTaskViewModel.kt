import androidx.lifecycle.ViewModel
import com.durranitech.taskalert.dataclasses.Task
import com.durranitech.taskalert.repositories.CreateTaskRepository

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
        onFailure: () -> Unit
    ) {
        val task = Task(
            taskId = taskid,
            title = taskTitle,
            description = taskDescription,
            startDate = startDate,
            endDate = endDate,
            startTime = startTime,
            endTime = endTime,
            priority = taskPriority
        )

        val updatedFields = mutableMapOf<String, Any>()
        taskTitle.let { updatedFields["title"] = it }
        taskDescription.let { updatedFields["description"] = it }
        startDate.let { updatedFields["startDate"] = it }
        endDate.let { updatedFields["endDate"] = it }
        startTime.let { updatedFields["startTime"] = it }
        endTime.let { updatedFields["endTime"] = it }
        taskPriority.let { updatedFields["priority"] = it }

        repository.updateTask(task, onSuccess, onFailure, updatedFields)

    }
}
