package com.example.alarmmanager.screens

import android.icu.util.Calendar
import android.os.Bundle
import android.app.DatePickerDialog
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import java.text.SimpleDateFormat
import java.util.Locale

class TaskListScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlarmManagerTheme {
                taskListScreen(navController = NavController(LocalContext.current))
            }
        }
    }


    @Composable
    fun taskListScreen(navController: NavController) {
        val viewModel = GetTaskViewModel()
        val tasks by viewModel.tasks.collectAsState()

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        var currentMonth by remember { mutableStateOf(dateFormat.format(calendar.time)) }
        var selectedDay by remember { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }

        Column(modifier = Modifier.fillMaxSize()) {
            header(
                currentMonth = currentMonth,
                onPreviousMonthClick = {
                    calendar.add(Calendar.MONTH, -1)
                    currentMonth = dateFormat.format(calendar.time)
                },
                onNextMonthClick = {
                    calendar.add(Calendar.MONTH, 1)
                    currentMonth = dateFormat.format(calendar.time)
                },
                navController = navController,
                onCalendarIconClick = {
                    (LocalContext.current as TaskListScreen).DatePickerDialogue { year, month, day ->
                        calendar.set(year, month, day)
                        currentMonth = dateFormat.format(calendar.time)
                        selectedDay = day
                        viewModel.fetchTasksForDay(day, month, year)
                    }
                }
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                for (day in 1..daysInMonth) {
                    val dayOfWeek =
                        SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time)
                    item {
                        dayItem(
                            day = dayOfWeek,
                            date = day.toString(),
                            isSelected = day == selectedDay,
                            onClick = {
                                selectedDay = day
                                // Fetch tasks for the selected day from Firestore
                                //viewModel.fetchTasksForDay(day, calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR))
                            }
                        )
                    }
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                }
                calendar.set(Calendar.DAY_OF_MONTH, 1)  // Reset to first day of month
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(tasks) { task ->
                    taskItem(task)
                }
            }
        }

    }

    @Composable
    fun dayItem(day: String, date: String, isSelected: Boolean = false, onClick: () -> Unit) {
        Card(Modifier.padding(8.dp), shape = RoundedCornerShape(8.dp)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .size(width = 60.dp, height = 80.dp)
                    .background(
                        if (isSelected) colorResource(
                            id = R.color.button_color
                        ) else Color.White
                    )
                    .padding(8.dp)
                    .clickable ( onClick = onClick )

                ) {
                Text(
                    text = date,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (!isSelected) Color.Gray else Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = day,
                    fontSize = 16.sp,
                    color = if (!isSelected) Color.Gray else Color.White
                )

            }

        }

    }

    @Composable
    fun taskItem(task: Task) {
        Card(
            modifier = Modifier.padding(4.dp),
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

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = timeFormate(task.startTime) + " - " + timeFormate(task.endTime),
                        color = Color.DarkGray,
                        fontSize = 16.sp
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = dateFormate(task.startDate),
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
                        text = dateFormate(task.endDate),
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

    @Composable
    fun header(
        currentMonth: String,
        onPreviousMonthClick: () -> Unit,
        onNextMonthClick: () -> Unit,
        onCalendarIconClick: @Composable () -> Unit,
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
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go back")

            }

            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPreviousMonthClick ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Previous Month",
                        Modifier.size(32.dp),
                        tint = colorResource(
                            id = R.color.button_color
                        )
                    )
                }
                Text(
                    text = currentMonth,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick =  onNextMonthClick) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Next Month",
                        Modifier.size(32.dp),
                        tint = colorResource(
                            id = R.color.button_color
                        )
                    )
                }
            }

            IconButton(onClick = { onCalendarIconClick}) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Next Month",
                    Modifier.size(28.dp),
                    tint = colorResource(
                        id = R.color.button_color
                    )
                )
            }

        }
    }

    @Composable
    fun DatePickerDialogue(onDateSelected: (Int, Int, Int) -> Unit) {
        val context = LocalContext.current
        val calender = Calendar.getInstance()
        val year = calender.get(Calendar.YEAR)
        val month = calender.get(Calendar.MONTH)
        val day = calender.get(Calendar.DAY_OF_MONTH)

       DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                onDateSelected(selectedYear, selectedMonth, selectedDay)
            },
            year,
            month,
            day
        ).show()
    }


    fun dateFormate(dateString: String): String {
        val fetchedDateFormate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
        return try {
            val date = fetchedDateFormate.parse(dateString)
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

    fun timeFormate(timeString: String): String {

        val fetchedTimeFormate = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dDF = SimpleDateFormat("h.mm a", Locale.getDefault())

        return try {
            val time = fetchedTimeFormate.parse(timeString)
            dDF.format(time)
        } catch (e: Exception) {
            e.printStackTrace()
            "invalid formated time"
        }
    }
}