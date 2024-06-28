package com.example.alarmmanager.screens

import CreateTaskViewModel
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.alarmmanager.R
import com.example.alarmmanager.screens.ui.theme.AlarmManagerTheme
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

class CreatTask : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlarmManagerTheme {
                val navController = rememberNavController()
                createTaskcom(navController, CreateTaskViewModel())
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun createTaskcom(navController: NavController, viewmodel: CreateTaskViewModel) {
        var taskTitle by rememberSaveable { mutableStateOf("") }
        var taskDescription by rememberSaveable { mutableStateOf("") }
        var startDate by rememberSaveable { mutableStateOf("") }
        var endDate by rememberSaveable { mutableStateOf("") }
        var startTime by rememberSaveable { mutableStateOf("") }
        var endTime by rememberSaveable { mutableStateOf("") }
        var titleError by rememberSaveable { mutableStateOf<String?>(null) }
        var selectedPriorityState by rememberSaveable { mutableStateOf("") }
        val context = LocalContext.current
        val scrollState = rememberScrollState()
        var isloading by rememberSaveable { mutableStateOf(false) }


        val textFieldColors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = colorResource(id = R.color.light_pink),
            unfocusedBorderColor = colorResource(id = R.color.light_pink),
            cursorColor = colorResource(id = R.color.button_color),
            focusedLabelColor = colorResource(id = R.color.button_color)
        )

        fun showDatePicker(isStartDate: Boolean) {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val selectedDate = dateFormat.format(calendar.time)
                    if (isStartDate) {
                        startDate = selectedDate
                    } else {
                        endDate = selectedDate
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        @SuppressLint("DefaultLocale")
        fun showTimePicker(isStartTime: Boolean) {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    val hour = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
                    val amPm = if (hourOfDay >= 12) "PM" else "AM"
                    val selectedTime = String.format("%02d:%02d %s", hour, minute, amPm)
                    if (isStartTime) {
                        startTime = selectedTime
                    } else {
                        endTime = selectedTime
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            ).show()
        }

        @Composable
        fun priorityButton(text: String, selectedCategory: String, onClick: (String) -> Unit) {

            val isSelected = selectedCategory == text

            val containerColor = if (isSelected) {
                when (text) {
                    "High" -> colorResource(id = R.color.dark_pink)
                    "Medium" -> colorResource(id = R.color.darkBlue)
                    "Low" -> colorResource(R.color.darkYellow)
                    else -> Color.White
                }

            } else {
                when (text) {
                    "High" -> colorResource(id = R.color.light_pink)
                    "Medium" -> colorResource(id = R.color.lightBlue)
                    "Low" -> colorResource(id = R.color.lightYellow)
                    else -> Color.White
                }
            }
            val contentColor = if (isSelected) {
                Color.White
            } else {
                when (text) {
                    "High" -> colorResource(id = R.color.dark_pink)
                    "Medium" -> colorResource(id = R.color.darkBlue)
                    "Low" -> colorResource(id = R.color.darkYellow)
                    else -> Color.White
                }
            }
            Button(
                onClick = { onClick(text) },

                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                    contentColor = contentColor,
                )
            ) {
                Text(
                    text,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(32.dp)
                .background(colorResource(id = R.color.custom_white)),
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()
            ) {

                Icon(imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Go back",
                    modifier = Modifier.clickable {
                        navController.navigateUp()
                    })

                Text(
                    text = "Create Task",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 24.sp,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Bold
                )

                Icon(imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    modifier = Modifier.clickable {
                        navController.navigateUp()
                    })
            }


            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "Task Title",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.Start),
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = taskTitle,
                onValueChange = { taskTitle = it },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors,
                maxLines = 1,
                textStyle = TextStyle(fontSize = 18.sp),
                label = { Text(text = "Title") })
            titleError?.let {
                Text(
                    text = it, fontSize = 12.sp, color = Color.Red,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Description",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.DarkGray,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = taskDescription,
                onValueChange = { taskDescription = it },
                shape = RoundedCornerShape(18.dp),
                textStyle = TextStyle(fontSize = 18.sp),
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                label = { Text("Write a note...") },
                colors = textFieldColors
            )

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = ("Deadlines"),
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start),
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Calender",
                    tint = colorResource(
                        id = R.color.button_color
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (startDate.isEmpty()) "Start Date" else startDate,
                    color = Color.DarkGray,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable {
                        showDatePicker(true)
                    })
                Text(text = "  -  ", fontWeight = FontWeight.Bold)

                Text(text = if (endDate.isEmpty()) "End Date" else endDate,
                    color = Color.DarkGray,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable {
                        showDatePicker(false)
                    })
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.clock),
                        contentDescription = "clock",
                        modifier = Modifier
                            .size(22.dp)
                            .padding(start = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (startTime.isEmpty()) "Start Time" else startTime,
                        color = Color.DarkGray,
                        fontSize = 16.sp,
                        modifier = Modifier.clickable {
                            showTimePicker(true)
                        })
                    Text(text = "  -  ", fontWeight = FontWeight.Bold)

                    Text(text = if (endTime.isEmpty()) "End Time" else startTime,
                        color = Color.DarkGray,
                        fontSize = 16.sp,
                        modifier = Modifier.clickable {
                            showTimePicker(false)
                        })
                }


            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Priorities",
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.DarkGray,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                priorityButton(text = "High", selectedPriorityState) {
                    selectedPriorityState = it
                }
                priorityButton(
                    text = "Medium", selectedPriorityState
                ) {
                    selectedPriorityState = it
                }
                priorityButton(text = "Low", selectedPriorityState) {
                    selectedPriorityState = it
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = {
                    titleError = if (taskTitle.isEmpty()) "Enter the title" else null

                    if (taskTitle.isEmpty() || startDate.isEmpty() || startTime.isEmpty()) {
                        Toast.makeText(context, "Please fill all the fields", Toast.LENGTH_LONG)
                            .show()
                        return@Button
                    } else {

                        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        val endT = dateFormat.parse("$endDate $endTime")?.time ?: 0L
                        val currentTime = System.currentTimeMillis()
                        val taskStatus = if (currentTime > endT) "Completed" else "In Progress"
                       Log.d("endT", endT.toString())
                        isloading = true
                        viewmodel.saveTask(
                            taskid = UUID.randomUUID().toString(),
                            taskTitle = taskTitle,
                            taskDescription = taskDescription,
                            startDate = startDate,
                            endDate = endDate,
                            startTime = startTime,
                            endTime = endTime,
                            taskPriority = selectedPriorityState,
                            onSuccess = {
                                Toast.makeText(
                                    context,
                                    "Task added successfully",
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                                navController.navigate("home")
                            },
                            onFailure = {
                                isloading = false
                                Toast.makeText(context, "Error saving task", Toast.LENGTH_LONG)
                                    .show()
                            },
                            context,
                            taskStatus
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(colorResource(id = R.color.button_color))
            ) {
                Text(
                    "Create task", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {

            }
        }
        if (isloading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray), // Semi-transparent black background
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp, color = colorResource(id = R.color.button_color),
                    modifier = Modifier
                        .height(32.dp)
                        .width(32.dp)
                        .align(alignment = Alignment.Center)
                )
            }
        }
    }
}