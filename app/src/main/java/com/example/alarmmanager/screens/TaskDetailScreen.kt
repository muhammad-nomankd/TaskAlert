package com.example.alarmmanager.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

class TaskDetailScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlarmManagerTheme {
                TaskList(navController = NavController(LocalContext.current))
            }
        }
    }


    @Composable
    fun TaskList(navController: NavController) {
        val viewModel = GetTaskViewModel()
        val tasks by viewModel.tasks.collectAsState()
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Go back",
                    modifier = Modifier.clickable {
                        navController.navigateUp()
                    })
                Text(
                    text = "Task List",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
                )
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Go back",
                    modifier = Modifier.clickable {
                        navController.navigateUp()
                    })

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
                    Text(text = task.startTime + " - " + task.endTime, color = Color.DarkGray, fontSize = 16.sp)
                }

                Text(
                    text = dateFormate(task.startDate),
                    color = Color.DarkGray,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .wrapContentWidth(Alignment.End)
                )
            }
        }
    }

    fun dateFormate(dateString: String): String {
        val fetchedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdf = SimpleDateFormat("MMM d", Locale.getDefault())

        return try {
            val date = fetchedDate.parse(dateString)
            val day = date?.let { SimpleDateFormat("d", Locale.getDefault()).format(it).toInt() }

            val suffix = when (day) {
                1, 21, 31 -> "st"
                2, 22 -> "nd"
                3,23 -> "rd"
                else -> "th"
            }

            date?.let {sdf.format(date) } + suffix
        } catch (e:Exception) {
            e.printStackTrace()
            "Invalid Date formate found"
        }
    }

}