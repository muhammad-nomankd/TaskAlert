package com.example.durranitech.screens

//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.alarmmanager.R
import com.example.durranitech.dataclasses.Task
import com.example.durranitech.screens.ui.theme.AlarmManagerTheme
import com.example.durranitech.viewmodels.GetTaskViewModel
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
        val viewModel: GetTaskViewModel = viewModel()
        val filteredTasksForMonth by viewModel.filteredTasksofMonth.observeAsState(emptyList())
        val filteredTasksForDay by viewModel.filteredTasksofDay.observeAsState(emptyList())
        val calendar = rememberSaveable { Calendar.getInstance() }
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        var showPopUp by rememberSaveable { mutableStateOf(false) }
        var currenttask by rememberSaveable { mutableStateOf("") }
        var taskStatus by rememberSaveable { mutableStateOf("") }
        var deleteTask by rememberSaveable { mutableStateOf(false) }
        val selectedCategoryState by remember { mutableStateOf("All") }
        var currentMonth by rememberSaveable { mutableStateOf(dateFormat.format(calendar.time)) }
        var selectedDay by rememberSaveable { mutableIntStateOf(-1) }
        var showPickerDialogue: Boolean by rememberSaveable { mutableStateOf(false) }
        val context = LocalContext.current
        var refreshtaskforday by rememberSaveable { mutableStateOf(false) }
        var isLoading by rememberSaveable { mutableStateOf(false) }


        val coroutines = rememberCoroutineScope()
        coroutines.launch {
            isLoading = true
            currentMonth = dateFormat.format(calendar.time)
            viewModel.fetchTaskForMonth(
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.YEAR)
            )
            isLoading = false
        }

        LaunchedEffect(calendar.time) {
            viewModel.fetchTaskForMonth(
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.YEAR)
            )
        }

        if (isLoading){
            Box(modifier = Modifier.fillMaxSize()){
                CircularProgressIndicator( color = Color.Gray, strokeWidth = 2.dp)
            }
        }

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
                                deleteTask = true
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
                viewModel.filterTasks(selectedCategoryState)
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

        // Main Screen
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Header(
                currentMonth,
                onPreviousMonthClick = {
                    calendar.add(Calendar.MONTH, -1)
                    selectedDay = -1
                    currentMonth = dateFormat.format(calendar.time)
                    refreshtaskforday = false
                    viewModel.fetchTaskForMonth(
                        calendar.get(Calendar.MONTH) + 1,
                        calendar.get(Calendar.YEAR)
                    )
                },
                onNextMonthClick = {
                    calendar.add(Calendar.MONTH, 1)
                    selectedDay = -1
                    currentMonth = dateFormat.format(calendar.time)
                    refreshtaskforday = false
                    viewModel.fetchTaskForMonth(
                        calendar.get(Calendar.MONTH) + 1,
                        calendar.get(Calendar.YEAR)
                    )
                },
                onCalendarIconClick = { showPickerDialogue = true },
                navController = navController
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
                                refreshtaskforday = true
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
                    onDateSelected = { year, month, day ->
                        calendar.set(year, month, day)
                        selectedDay = day
                        currentMonth = dateFormat.format(calendar.time)
                        viewModel.fetchTaskForDay(
                            calendar.get(Calendar.DAY_OF_MONTH),
                            (calendar.get(Calendar.MONTH) + 1),
                            calendar.get(Calendar.YEAR)
                        )
                        refreshtaskforday = true
                    },
                    context = context,
                    calendar = calendar
                )
                showPickerDialogue = false
            }

           // Task List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(
                    if (refreshtaskforday) filteredTasksForDay else filteredTasksForMonth,
                    key = { it.taskId }) { task ->
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
        currentmonth: String,
        onPreviousMonthClick: () -> Unit,
        onNextMonthClick: () -> Unit,
        onCalendarIconClick: () -> Unit,
        navController: NavController
    ) {
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
                    tint = Color.DarkGray,
                    modifier = Modifier.size(24.dp)
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
                        tint = Color.DarkGray
                    )
                }
                Text(
                    text = currentmonth,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 20.sp,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onNextMonthClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Month",
                        Modifier.size(28.dp),
                        tint = Color.DarkGray
                    )
                }
            }

            IconButton(onClick = onCalendarIconClick) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Next Month",
                    Modifier.size(28.dp),
                    tint = Color.DarkGray
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

        //Default values passed to calendar in the beginning which current date
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Adjust the month from zero-based to one-based
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
                                color = colorResource(id = R.color.dark_gray),
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .width(IntrinsicSize.Max)
                                    .widthIn(0.dp, 150.dp),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            if (task.priority.isEmpty()) {
                                Text(text = "")
                            } else {
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    backgroundColor = when (task.priority) {
                                        "High" -> colorResource(id = R.color.dark_pink)
                                        "Medium" -> colorResource(id = R.color.darkYellow)
                                        "Low" -> colorResource(id = R.color.darkBlue)
                                        else -> Color.White
                                    },
                                    contentColor = when (task.priority) {
                                        "High" -> colorResource(id = R.color.dark_pink)
                                        "Medium" -> colorResource(id = R.color.darkBlue)
                                        "Low" -> colorResource(id = R.color.darkYellow)
                                        else -> Color.Black
                                    },
                                    modifier = Modifier
                                        .padding(start = 4.dp, top = 6.dp)
                                ) {
                                    Text(
                                        text = task.priority,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(
                                            start = 4.dp, end = 4.dp
                                        )
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = task.description,
                        fontSize = 14.sp,
                        color = colorResource(id = R.color.medium_gray),
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier
                            .padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = timeFormated(task.startTime) + " - " + timeFormated(task.endTime),
                        color = colorResource(id = R.color.medium_gray),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = dateFormater(task.startDate),
                        color = colorResource(id = R.color.medium_gray),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .wrapContentWidth(Alignment.End)
                    )
                    Text(
                        text = "To", color = colorResource(id = R.color.medium_gray),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = dateFormater(task.endDate),
                        color = colorResource(id = R.color.medium_gray),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .wrapContentWidth(Alignment.End)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    when (task.status) {
                        "In Progress" ->
                            Image(
                                painter = painterResource(id = R.drawable.inprogressicon),
                                contentDescription = "check icon",
                                modifier = Modifier
                                    .size(24.dp)
                            )

                        "Pending" ->
                            Image(
                                painter = painterResource(id = R.drawable.pending),
                                modifier = Modifier.size(24.dp),
                                contentDescription = "pending",
                                colorFilter = ColorFilter.tint(Color(0xFFFFA500))
                            )
                        "Completed" ->
                            Image(painter = painterResource(id = R.drawable.checkicon), contentDescription = "Completed",
                                modifier = Modifier.size(24.dp))
                    }
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