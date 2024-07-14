package com.example.alarmmanager.screens

//noinspection UsingMaterialAndMaterial3Libraries
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.alarmmanager.R
import com.example.alarmmanager.dataclasses.Task
import com.example.alarmmanager.screens.ui.theme.AlarmManagerTheme
import com.example.alarmmanager.viewmodels.GetTaskViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class TaskListScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlarmManagerTheme {
                TaskListScreen(navController = NavController(LocalContext.current))
            }
        }
    }


    @SuppressLint("NotConstructor", "CoroutineCreationDuringComposition")
    @Composable
    fun TaskListScreen(navController: NavController) {
        val viewModel = GetTaskViewModel()
        val filteredTasks by viewModel.filteredTasksofMonth.observeAsState(initial = emptyList())
        val calendar = rememberSaveable { Calendar.getInstance() }
        val calendar2 = rememberSaveable { Calendar.getInstance() }
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        var showPopUp by rememberSaveable { mutableStateOf(false) }
        var currenttask by rememberSaveable { mutableStateOf("") }
        var taskStatus by rememberSaveable { mutableStateOf("") }
        var deleteTask by rememberSaveable { mutableStateOf(false) }
        var selectedCategoryState by remember { mutableStateOf("All") }
        var currentMonth by rememberSaveable { mutableStateOf(dateFormat.format(calendar.time)) }
        var selectedDay by rememberSaveable { mutableIntStateOf(-1) }
        var showPickerDialogue: Boolean by rememberSaveable { mutableStateOf(false) }
        val context = LocalContext.current
        val viewmodel = GetTaskViewModel()



        fun deleteTask(taskId: String, status: String, context: Context) {
            val db = FirebaseFirestore.getInstance()
            val uId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val taskRef = db.collection("User").document(uId).collection("tasks")
            taskRef.whereEqualTo("taskId", taskId)
                .get()
                .addOnSuccessListener { querySnapShot ->
                    for (document in querySnapShot.documents) {
                        if (status == "Completed") {
                            taskRef.document(document.id).delete().addOnSuccessListener {
                                Toast.makeText(context, "Task Deleted", Toast.LENGTH_SHORT).show()
                                deleteTask = true
                            }.addOnFailureListener {
                                Toast.makeText(context, "Failed to delete task", Toast.LENGTH_SHORT)
                                    .show()

                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Task has not completed yet.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
        }


        val coroutineScope = rememberCoroutineScope()
        if (deleteTask) {
            coroutineScope.launch {
                viewmodel.filterTasks(selectedCategoryState)
                deleteTask = false
            }
        }

        // PopUp for Deleting task
        if (showPopUp) {
            AlertDialog(onDismissRequest = { showPopUp = false },
                shape = RoundedCornerShape(16.dp),
                backgroundColor = colorResource(
                    id = R.color.task_color
                ),
                confirmButton = {
                    Button(
                        onClick = {
                            showPopUp = false
                            deleteTask(currenttask, taskStatus, context)

                        },
                        elevation = ButtonDefaults.elevatedButtonElevation(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(
                                id = R.color.button_color
                            )
                        )
                    ) {
                        Text(
                            text = "Delete",
                            color = Color.White, modifier = Modifier
                        )
                    }

                },
                title = {
                    Text(
                        text = "Delete Task",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you  want to delete this task?",
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                dismissButton = {
                    Button(
                        onClick = { showPopUp = false },
                        elevation = ButtonDefaults.elevatedButtonElevation(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            colorResource(id = R.color.darkBlue)
                        )
                    ) {
                        Text(text = "Cancel", color = Color.White)
                    }
                })
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Header(

                onPreviousMonthClick = {
                    calendar.add(Calendar.MONTH, -1)
                    selectedDay = -1
                    viewModel.fetchTaskForMonth(
                        calendar.get(Calendar.MONTH) + 1,
                        calendar.get(Calendar.YEAR)
                    )
                },
                onNextMonthClick = {
                    calendar.add(Calendar.MONTH, 1)
                    selectedDay = -1
                    viewModel.fetchTaskForMonth(
                        calendar.get(Calendar.MONTH) + 1,
                        calendar.get(Calendar.YEAR)
                    )
                },
                onCalendarIconClick = { showPickerDialogue = true },
                navController = navController,
                calendar = calendar2
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val calendarSecond = calendar.clone() as Calendar
                val daysInMonth = calendarSecond.getActualMaximum(Calendar.DAY_OF_MONTH)
                calendarSecond.set(Calendar.DAY_OF_MONTH, 1)
                (1..daysInMonth).forEach { day ->
                    val dayOfWeek =
                        SimpleDateFormat("EEE", Locale.getDefault()).format(calendarSecond.time)
                    item {
                        DayItem(
                            day = dayOfWeek,
                            date = day.toString(),
                            isSelected = (day == selectedDay),
                            onclick = {
                                selectedDay = day
                                viewModel.fetchTaskForDay(
                                    day,
                                    calendarSecond.get(Calendar.MONTH),
                                    calendarSecond.get(Calendar.YEAR)
                                )
                            }
                        )
                    }
                    calendarSecond.add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            if (showPickerDialogue) {
                DatePickerDialogueShow(
                    //callback function it contain the values that is selected in the DatePickerDialogueShow and passed to it
                    onDateSelected = { year, month, day ->
                        calendar.set(year, month, day)

                        selectedDay = day
                        currentMonth = dateFormat.format(calendar.time)
                        Log.d("bug fixing current month", currentMonth)
                        viewModel.fetchTaskForDay(
                            day,
                            (calendar.get(Calendar.MONTH) + 1),
                            calendar.get(Calendar.YEAR)
                        )
                    },
                    context = context,
                    calendar = calendar
                )
                showPickerDialogue = false
            }



            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(filteredTasks) { task ->
                    TaskItem(task, longClick = {
                        currenttask = task.taskId
                        showPopUp = true
                        taskStatus = task.status
                    }, onClick = {
                        navController.navigate("createTask?taskId=${task.taskId}&taskTitle=${task.title}&taskDescription=${task.description}&startDate=${task.startDate}&endDate=${task.endDate}&startTime=${task.startTime}&endTime=${task.endTime}&priority=${task.priority}")
                    })
                }
            }
        }
    }



    @Composable
    fun Header(
        calendar: Calendar,
        onPreviousMonthClick: () -> Unit,
        onNextMonthClick: () -> Unit,
        onCalendarIconClick: () -> Unit,
        navController: NavController
    ) {
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val currentMonth = dateFormat.format(calendar.time)
        Log.d(" bug fixing current month", currentMonth)
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
                .padding(top = 32.dp, start = 8.dp, end = 8.dp)
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Go back",
                    tint = Color.Gray
                )
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonthClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Month",
                        Modifier.size(28.dp),
                        tint = colorResource(id = R.color.button_color)
                    )
                }
                Text(
                    text = currentMonth,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onNextMonthClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Month",
                        Modifier.size(28.dp),
                        tint = colorResource(id = R.color.button_color)
                    )
                }
            }

            IconButton(onClick = onCalendarIconClick) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Next Month",
                    Modifier.size(28.dp),
                    tint = colorResource(id = R.color.button_color)
                )
            }
        }
    }

    @Composable
    fun DatePickerDialogueShow(
        onDateSelected: (Int, Int, Int) -> Unit,
        context: Context,
        calendar: Calendar
    ) {

        //Default values passed to calender in the beginning which current date
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                //Selected values passed to the callback function
                onDateSelected(selectedYear, selectedMonth, selectedDay)
            },
            year,
            month,
            day
        ).show()

    }

    @Composable
    fun DayItem(day: String, date: String, isSelected: Boolean = false, onclick: () -> Unit) {
        Card(Modifier.padding(4.dp), shape = RoundedCornerShape(8.dp)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .size(width = 60.dp, height = 80.dp)
                    .background(
                        if (isSelected) colorResource(id = R.color.button_color) else Color.White
                    )
                    .clickable(onClick = onclick)
            ) {
                Text(
                    text = date,
                    fontSize = 20.sp,
                    color = if (!isSelected) Color.Black else Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = day,
                    fontSize = 14.sp,
                    color = if (!isSelected) Color.Gray else Color.White
                )
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun TaskItem(task: Task, longClick: () -> Unit, onClick: () -> Unit) {
        Card(
            modifier = Modifier
                .padding(4.dp)
                .combinedClickable(
                    onClick = { onClick() },
                    onLongClick = { longClick() },
                ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .height(90.dp)
                        .width(4.dp)
                        .background(
                            colorResource(id = R.color.box_color),
                            shape = RoundedCornerShape(4.dp)
                        )
                )
                Column(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = task.title,
                                fontSize = 20.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .width(IntrinsicSize.Max)
                                    .widthIn(0.dp, 150.dp),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Card(
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .wrapContentWidth(),
                                backgroundColor = when (task.priority) {
                                    "Low" -> colorResource(id = R.color.lightYellow)
                                    "Medium" -> colorResource(id = R.color.lightBlue)
                                    "High" -> colorResource(id = R.color.light_pink)
                                    else -> Color.White
                                },
                                contentColor = when (task.priority) {
                                    "Low" -> colorResource(id = R.color.darkYellow)
                                    "Medium" -> colorResource(id = R.color.darkBlue)
                                    "High" -> colorResource(id = R.color.dark_pink)
                                    else -> Color.White
                                }
                            ) {
                                Text(
                                    text = task.priority,
                                    fontSize = 12.sp,
                                    color = when (task.priority) {
                                        "High" -> colorResource(id = R.color.dark_pink)
                                        "Medium" -> colorResource(id = R.color.darkBlue)
                                        "Low" -> colorResource(id = R.color.darkYellow)
                                        else -> Color.Black
                                    },
                                    modifier = Modifier.padding(
                                        start = 6.dp,
                                        end = 6.dp,
                                        top = 3.dp,
                                        bottom = 3.dp
                                    )
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = task.description,
                        fontSize = 16.sp,
                        color = Color.DarkGray,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = timeFormated(task.startTime) + " - " + timeFormated(task.endTime),
                        color = Color.DarkGray,
                        fontSize = 16.sp
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = dateFormater(task.startDate),
                        color = Color.DarkGray,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .wrapContentWidth(Alignment.End)
                    )
                    Text(
                        text = "To", color = Color.DarkGray,
                        fontSize = 12.sp,
                    )
                    Text(
                        text = dateFormater(task.endDate),
                        color = Color.DarkGray,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .wrapContentWidth(Alignment.End)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = task.status,
                        fontSize = 14.sp,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = when (task.status) {
                            "Completed" -> colorResource(id = R.color.green)
                            "In Progress" -> colorResource(id = R.color.darkYellow)
                            "Pending" -> Color.Black
                            else -> Color.Gray
                        }
                    )
                }
            }
        }
    }

    private fun dateFormater(dateString: String): String {
        val fetchedDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
        return try {
            val date = fetchedDateFormat.parse(dateString)
            val day = date?.let { SimpleDateFormat("d", Locale.getDefault()).format(it).toInt() }
            val suffix = when (day) {
                1, 21, 31 -> "st"
                2, 22 -> "nd"
                3, 23 -> "rd"
                else -> "th"
            }
            date?.let { sdf.format(it) } + suffix
        } catch (e: Exception) {
            e.printStackTrace()
            "Invalid date"
        }
    }

    private fun timeFormated(timeString: String): String {
        val fetchedTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dDF = SimpleDateFormat("h.mm a", Locale.getDefault())

        return try {
            val time = fetchedTimeFormat.parse(timeString)
            dDF.format(time ?: "No time")
        } catch (e: Exception) {
            e.printStackTrace()
            "invalid formatted time"
        }
    }
}
