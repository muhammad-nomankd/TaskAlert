package com.example.alarmmanager.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarmmanager.R
import com.example.alarmmanager.dataclasses.Task
import com.example.alarmmanager.screens.ui.theme.AlarmManagerTheme
import com.example.alarmmanager.viewmodels.GetTaskViewModel

class TaskDetailScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlarmManagerTheme {
                TaskList()
            }
        }
    }


    @Composable
    fun TaskList() {
        val viewModel = GetTaskViewModel()
        val tasks by viewModel.tasks.collectAsState()

        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(tasks) { task ->
                TaskItem(task)
            }
        }
    }

    @Composable
    fun TaskItem(task: Task) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = 8.dp,
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
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = task.title,
                            fontSize = 20.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Card(
                            modifier = Modifier.padding(top = 8.dp),
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
                                modifier = Modifier.padding(
                                    start = 6.dp,
                                    end = 6.dp,
                                    top = 3.dp,
                                    bottom = 3.dp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = task.startTime + " - " + task.endTime,
                        color = Color.DarkGray
                    )
                }

                Text(
                    text = task.startDate,
                    color = Color.DarkGray,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .padding(start = 100.dp)
                )
            }
        }
    }
}