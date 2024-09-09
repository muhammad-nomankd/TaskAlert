package com.durranitech.taskalert.screens

import CreateTaskViewModel
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.icu.util.Calendar
import android.net.ConnectivityManager
import android.os.Bundle
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.durranitech.taskalert.R
import com.durranitech.taskalert.screens.ui.theme.AlarmManagerTheme
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CreatTask : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlarmManagerTheme {
                val navController = rememberNavController()
                CreateTaskcom(
                    navController,
                    CreateTaskViewModel()
                )
            }
        }
    }


    @SuppressLint("SuspiciousIndentation", "UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CreateTaskcom(
        navController: NavController,
        viewmodel: CreateTaskViewModel,
        taskid: String? = null,
        taskTitleArg: String? = null,
        taskDescriptionArg: String? = null,
        startDateArg: String? = null,
        endDateArg: String? = null,
        startTimeArg: String? = null,
        endTimeArg: String? = null,
        priorityArg: String? = null
    ) {
        var taskTitle by rememberSaveable { mutableStateOf(taskTitleArg ?: "") }
        var taskDescription by rememberSaveable { mutableStateOf(taskDescriptionArg ?: "") }
        var startDate by rememberSaveable { mutableStateOf(startDateArg ?: "") }
        var endDate by rememberSaveable { mutableStateOf(endDateArg ?: "") }
        var startTime by rememberSaveable { mutableStateOf(startTimeArg ?: "") }
        var endTime by rememberSaveable { mutableStateOf(endTimeArg ?: "") }
        var titleError by rememberSaveable { mutableStateOf<String?>(null) }
        var selectedPriorityState by rememberSaveable { mutableStateOf(priorityArg ?: "") }
        val context = LocalContext.current
        val scrollState = rememberScrollState()
        var isloading by rememberSaveable { mutableStateOf(false) }
        val status by rememberSaveable { mutableStateOf("") }
        var snackBarHost by remember { mutableStateOf(SnackbarHostState()) }
        val coroutinesScope = rememberCoroutineScope()

        val textFieldColors = TextFieldDefaults.outlinedTextFieldColors(
            unfocusedBorderColor = Color.LightGray,
            focusedBorderColor = Color.DarkGray,
            cursorColor = Color.DarkGray
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
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    val selectedTime = timeFormat.format(calendar.time)
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
                    "Medium" -> colorResource(id = R.color.darkYellow)
                    "Low" -> colorResource(R.color.darkBlue)
                    else -> Color.White
                }
            } else {
                when (text) {
                    "High" -> Color(0x80F5B7B1)
                    "Medium" -> Color(0x80F7E794)
                    "Low" -> Color(0x80B3E5FC)
                    else -> Color.White
                }
            }
            val contentColor = if (isSelected) {
                Color.White
            } else {
                when (text) {
                    "High" -> colorResource(id = R.color.dark_pink)
                    "Medium" -> colorResource(id = R.color.veryDarkYellow)
                    "Low" -> colorResource(R.color.darkBlue)
                    else -> Color.White
                }
            }
            Button(
                onClick = { onClick(text) },
               elevation = ButtonDefaults.buttonElevation(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                    contentColor = contentColor
                )
            ) {
                Text(
                    text,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        fun convertStrToTime(timeStr: String): Date? {
            return try {
                SimpleDateFormat("HH:mm", Locale.getDefault()).parse(timeStr)
            } catch (e: ParseException) {
                null
            }

        }

        fun convertStrToDate(dateStr: String): Date? {
            return try {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
            } catch (e: ParseException) {
                null
            }

        }


        // Create task UI
        Scaffold(modifier = Modifier.fillMaxSize(), snackbarHost = { SnackbarHost(hostState = snackBarHost)}) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorResource(id = R.color.custom_white))
            )
            {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(32.dp)
                        .background(colorResource(id = R.color.custom_white)),
                    horizontalAlignment = Alignment.CenterHorizontally
                )
                {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back",
                            tint = Color.DarkGray,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    navController.navigateUp()
                                })
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = if (taskid == null) "Create Task" else "Update Task",
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 20.sp,
                            color = colorResource(id = R.color.dark_gray),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }


                    Spacer(modifier = Modifier.height(48.dp))
                    Text(
                        text = "Task Title",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 20.sp,
                        modifier = Modifier.align(Alignment.Start),
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.dark_gray)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = taskTitle,
                        onValueChange = { taskTitle = it
                                        titleError = ""},
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors,
                        maxLines = 1,
                        textStyle = TextStyle(fontSize = 16.sp),
                        label = { Text(text = "Title", color = Color.Gray) })
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
                        fontWeight = FontWeight.Bold,
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
                        label = { Text("Write a note...", color = Color.Gray) },
                        colors = textFieldColors
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = ("Deadlines"),
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
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
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = startDate.ifEmpty { "Start Date" },
                            color = colorResource(id = R.color.medium_gray),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable {
                                showDatePicker(true)
                            })
                        Text(text = "  -  ", fontWeight = FontWeight.Bold)

                        Text(text = endDate.ifEmpty { "End Date" },
                            color = colorResource(id = R.color.medium_gray),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
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
                                colorFilter = ColorFilter.tint(Color.Gray),
                                contentDescription = "clock",
                                modifier = Modifier
                                    .size(22.dp)
                                    .padding(start = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = startTime.ifEmpty { "Start Time" },
                                color = colorResource(id = R.color.medium_gray),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable {
                                    showTimePicker(true)
                                })
                            Text(text = "  -  ", fontWeight = FontWeight.Bold)

                            Text(text = endTime.ifEmpty { "End Time" },
                                color = colorResource(id = R.color.medium_gray),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable {
                                    showTimePicker(false)
                                })
                        }


                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "Priorities",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colorResource(id = R.color.dark_gray),
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
                    Button( elevation = ButtonDefaults.buttonElevation(8.dp),
                        onClick = {
                            if(taskTitle.isEmpty()){
                                titleError = "Title cannot be empty"
                                return@Button
                            }
                            if(isNetworkAvailable(context)){
                            if (taskTitle.isEmpty() || startDate.isEmpty() || startTime.isEmpty() || endDate.isEmpty() || endTime.isEmpty()) {
                                coroutinesScope.launch {
                                    snackBarHost.showSnackbar(
                                        message = "Please fill all the fields",
                                        actionLabel = "Close",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                                return@Button
                            } else {
                                if (convertStrToTime(endTime)!! < convertStrToTime(startTime)) {
                                    coroutinesScope.launch {
                                        snackBarHost.showSnackbar(
                                            message = "Start Time shouldn't be after end Time",
                                            actionLabel = "Close",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    return@Button
                                }
                                if (convertStrToDate(endDate)!! < convertStrToDate(startDate)) {
                                    coroutinesScope.launch {
                                        snackBarHost.showSnackbar(
                                            message = "Start Date shouldn't be after end Date",
                                            actionLabel = "Close",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    return@Button
                                }
                                isloading = true
                                if (taskid != null) {
                                    if (convertStrToTime(endTime)!! < convertStrToTime(startTime)) {
                                        coroutinesScope.launch {
                                            snackBarHost.showSnackbar(
                                                message = "Start Time shouldn't be after end Time",
                                                actionLabel = "Close",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                        return@Button
                                    }
                                    if (convertStrToDate(endDate)!! < convertStrToDate(startDate)) {
                                        coroutinesScope.launch {
                                            snackBarHost.showSnackbar(
                                                message = "Start Date shouldn't be after end Date",
                                                actionLabel = "Close",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                        return@Button
                                    }
                                    if (taskTitle.isEmpty() || startDate.isEmpty() || startTime.isEmpty() || endDate.isEmpty() || endTime.isEmpty()) {
                                        return@Button
                                    }
                                    if (isNetworkAvailable(context)) {
                                        viewmodel.updateTask(
                                            taskid = taskid,
                                            taskTitle = taskTitle,
                                            taskDescription = taskDescription,
                                            startDate = startDate,
                                            endDate = endDate,
                                            startTime = startTime,
                                            endTime = endTime,
                                            taskPriority = selectedPriorityState,
                                            onSuccess = {
                                                navController.navigate("home") {
                                                    popUpTo(navController.graph.startDestinationId) {
                                                        inclusive = true
                                                    }
                                                }
                                                isloading = false

                                            },
                                            onFailure = {
                                                isloading = false
                                            }
                                        )
                                    } else {
                                        isloading = false
                                        coroutinesScope.launch {
                                            snackBarHost.showSnackbar(
                                                message = "Please connect to a network and try again",
                                                actionLabel = "Close",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }

                                } else if (isNetworkAvailable(context)) {
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
                                            navController.navigate("home") {
                                                popUpTo("home") {
                                                    inclusive = true
                                                }
                                            }
                                        },
                                        onFailure = {exception ->
                                            isloading = false

                                            coroutinesScope.launch {
                                                snackBarHost.showSnackbar(
                                                    message = exception.toString(),
                                                    actionLabel = "Close",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        },
                                        status
                                    )
                                } else {
                                    isloading = false
                                }

                            }} else {
                                coroutinesScope.launch {
                                    snackBarHost.showSnackbar(
                                        message = "Connect to a network and try again",
                                        actionLabel = "Close",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(colorResource(id = R.color.button_color))
                    ) {
                        Text(
                            if (taskid != null) "Update Task" else "Create Task",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
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
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            if (isloading) {
                CircularProgressIndicator(
                    strokeWidth = 1.dp,
                    modifier = Modifier
                        .align(alignment = Alignment.CenterHorizontally),
                    color = Color.Gray
                )
            }
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }
}