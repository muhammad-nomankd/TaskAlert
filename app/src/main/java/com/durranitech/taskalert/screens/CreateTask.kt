package com.durranitech.taskalert.screens

import CreateTaskViewModel
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.icu.util.Calendar
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.durranitech.taskalert.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt


@SuppressLint("SuspiciousIndentation", "UnusedMaterial3ScaffoldPaddingParameter", "NotConstructor")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTask(
    navController: NavController,
    viewmodel: CreateTaskViewModel,
    taskId: String? = null,
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
    var selectedPriorityState by rememberSaveable { mutableStateOf(priorityArg ?: "") }

    // Error states for validation
    var titleError by rememberSaveable { mutableStateOf<String?>(null) }
    var startDateError by rememberSaveable { mutableStateOf(false) }
    var endDateError by rememberSaveable { mutableStateOf(false) }
    var startTimeError by rememberSaveable { mutableStateOf(false) }
    var endTimeError by rememberSaveable { mutableStateOf(false) }
    var priorityError by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var isloading by rememberSaveable { mutableStateOf(false) }
    val status by rememberSaveable { mutableStateOf("") }
    var snackBarHost = remember { SnackbarHostState() }
    val coroutinesScope = rememberCoroutineScope()

    var shakeTitle by remember { mutableStateOf(false) }
    var shakeStartTime by remember { mutableStateOf(false) }
    var shakeEndTime by remember { mutableStateOf(false) }
    var shakeStartDate by remember { mutableStateOf(false) }
    var shakeEndDate by remember { mutableStateOf(false) }

    // Validation function
    fun validateForm(): Boolean {
        var isValid = true

        if (taskTitle.isEmpty()) {
            titleError = "Title cannot be empty"
            shakeTitle = true
            coroutinesScope.launch {
                delay(500)
                shakeTitle = false
            }
            isValid = false
        } else {
            titleError = null
        }

        startDateError = startDate.isEmpty()
        endDateError = endDate.isEmpty()
        startTimeError = startTime.isEmpty()
        endTimeError = endTime.isEmpty()
        priorityError = selectedPriorityState.isEmpty()

        if (startDate.isEmpty() || endDate.isEmpty() || startTime.isEmpty() || endTime.isEmpty() || selectedPriorityState.isEmpty()) {
            isValid = false
        }
        if (startTime.isEmpty()){
            shakeStartTime = true
            coroutinesScope.launch {
                delay(500)
                shakeStartTime = false
            }
        }
        if (endTime.isEmpty()){
            shakeEndTime = true
            coroutinesScope.launch {
                delay(500)
                shakeEndTime = false
            }
        }
        if (startDate.isEmpty()){
            shakeStartDate = true
            coroutinesScope.launch {
                delay(500)
                shakeStartDate = false
            }
        }
        if (endDate.isEmpty()){
            shakeEndDate = true
            coroutinesScope.launch {
                delay(500)
                shakeEndDate = false
            }
        }


        if (isValid) {
            try {
                if (convertStrToDate(endDate)!! < convertStrToDate(startDate)) {
                    coroutinesScope.launch {
                        snackBarHost.showSnackbar(
                            message = "End date must be after start date",
                            actionLabel = "Close",
                            duration = SnackbarDuration.Short
                        )
                    }
                    return false
                }

                if (convertStrToDate(endDate)!! == convertStrToDate(startDate) && convertStrToTime(
                        endTime
                    )!! < convertStrToTime(startTime)
                ) {
                    coroutinesScope.launch {
                        snackBarHost.showSnackbar(
                            message = "For same day tasks, end time must be after start time",
                            actionLabel = "Close",
                            duration = SnackbarDuration.Short
                        )
                    }
                    return false
                }
            } catch (e: Exception) {
                coroutinesScope.launch {
                    snackBarHost.showSnackbar(
                        message = "Invalid date or time format",
                        actionLabel = "Close",
                        duration = SnackbarDuration.Short
                    )
                }
                return false
            }
        }

        return isValid
    }

    // Date and time picker functions
    @RequiresApi(Build.VERSION_CODES.N)
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
                    startDateError = false
                } else {
                    endDate = selectedDate
                    endDateError = false
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun showTimePicker(isStartTime: Boolean) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            context, { _, hourOfDay, minute ->
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                val selectedTime = timeFormat.format(calendar.time)
                if (isStartTime) {
                    startTime = selectedTime
                    startTimeError = false
                } else {
                    endTime = selectedTime
                    endTimeError = false
                }
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false
        ).show()
    }

    // Create task UI
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackBarHost) }) {
        if (isloading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x80FFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                ShimmerEffect()
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorResource(id = R.color.custom_white))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(12.dp)
                        .background(colorResource(id = R.color.custom_white)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // App Bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back",
                            tint = Color.DarkGray,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { navController.navigateUp() })
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = if (taskId == null) "Create Task" else "Update Task",
                            style = MaterialTheme.typography.headlineSmall,
                            color = colorResource(id = R.color.dark_gray),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Task Title Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFFF9F9F9), shape = RoundedCornerShape(16.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Task Title",
                                style = MaterialTheme.typography.bodyLarge,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorResource(id = R.color.dark_gray)
                            )
                            Text(
                                text = " *",
                                color = colorResource(id = R.color.dark_pink),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = taskTitle,
                            onValueChange = {
                                taskTitle = it
                                if (it.isNotEmpty()) titleError = null
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .shake(shakeTitle),
                            isError = titleError != null,
                            maxLines = 1,
                            textStyle = TextStyle(fontSize = 16.sp),
                            label = { Text(text = "Title", color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = if (titleError != null) colorResource(id = R.color.dark_pink) else Color.LightGray,
                                focusedBorderColor = colorResource(id = R.color.button_color)
                            )
                        )

                        titleError?.let {
                            Text(
                                text = it,
                                fontSize = 12.sp,
                                color = colorResource(id = R.color.dark_pink),
                                modifier = Modifier
                                    .align(Alignment.Start)
                                    .padding(start = 8.dp, top = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFFF9F9F9), shape = RoundedCornerShape(16.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.DarkGray,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = taskDescription,
                            onValueChange = { taskDescription = it },
                            shape = RoundedCornerShape(12.dp),
                            textStyle = TextStyle(fontSize = 16.sp),
                            maxLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            label = { Text("Write a note...", color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.LightGray,
                                focusedBorderColor = colorResource(id = R.color.button_color)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Deadlines Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFFF9F9F9), shape = RoundedCornerShape(16.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Deadlines",
                                style = MaterialTheme.typography.bodyLarge,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )
                            Text(
                                text = " *",
                                color = colorResource(id = R.color.dark_pink),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Date Selectors
                        Text(
                            text = "Date Range",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.DarkGray
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DateSelectButton(
                                label = "Start Date",
                                value = startDate,
                                isError = startDateError,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                                    .shake(shakeStartDate),
                                onClick = { showDatePicker(true) })

                            DateSelectButton(
                                label = "End Date",
                                value = endDate,
                                isError = endDateError,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp)
                                    .shake(shakeEndDate),
                                onClick = { showDatePicker(false) })
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Time Selectors
                        Text(
                            text = "Time Range",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.DarkGray
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TimeSelectButton(
                                label = "Start Time",
                                value = startTime,
                                isError = startTimeError,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                                    .shake(shakeStartTime),
                                onClick = { showTimePicker(true) })

                            TimeSelectButton(
                                label = "End Time",
                                value = endTime,
                                isError = endTimeError,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp)
                                    .shake(shakeEndTime),
                                onClick = { showTimePicker(false) })
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Priority Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFFF9F9F9), shape = RoundedCornerShape(16.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Priority",
                                style = MaterialTheme.typography.bodyLarge,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray,
                            )
                        }

                        if (priorityError) {
                            Text(
                                text = "Please select a priority",
                                fontSize = 12.sp,
                                color = colorResource(id = R.color.dark_pink),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PriorityButton(
                                text = "High",
                                selectedPriority = selectedPriorityState,
                                onSelect = {
                                    selectedPriorityState = it
                                    priorityError = false
                                },
                                modifier = Modifier.weight(1f)
                            )

                            PriorityButton(
                                text = "Medium",
                                selectedPriority = selectedPriorityState,
                                onSelect = {
                                    selectedPriorityState = it
                                    priorityError = false
                                },
                                modifier = Modifier.weight(1f)
                            )

                            PriorityButton(
                                text = "Low", selectedPriority = selectedPriorityState, onSelect = {
                                    selectedPriorityState = it
                                    priorityError = false
                                }, modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Submit Button
                    Button(
                        onClick = {
                            if (validateForm()) {
                                if (isNetworkAvailable(context)) {
                                    isloading = true
                                    if (taskId != null) {
                                        viewmodel.updateTask(
                                            taskid = taskId,
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
                                            })
                                    } else {
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
                                            onFailure = { exception ->
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
                                    }
                                } else {
                                    coroutinesScope.launch {
                                        snackBarHost.showSnackbar(
                                            message = "Connect to a network and try again",
                                            actionLabel = "Close",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.button_color)
                        ),
                        elevation = ButtonDefaults.buttonElevation(8.dp)
                    ) {
                        Text(
                            if (taskId != null) "Update Task" else "Create Task",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                if (isloading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x80FFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = colorResource(id = R.color.button_color), strokeWidth = 2.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DateSelectButton(
    label: String,
    value: String,
    isError: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundColor = if (isError) Color(0x15FF0000) else Color(0xFFF5F5F5)
    val borderColor = if (isError) colorResource(id = R.color.dark_pink) else Color.Transparent
    val textColor = if (value.isEmpty()) Color.Gray else Color.Black

    Box(
        modifier = modifier
            .background(
                color = backgroundColor, shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Calendar",
                tint = if (isError) colorResource(id = R.color.dark_pink) else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = if (value.isEmpty()) label else value,
                    color = if (isError && value.isEmpty()) colorResource(id = R.color.dark_pink) else textColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                if (isError && value.isEmpty()) {
                    Text(
                        text = "Required",
                        color = colorResource(id = R.color.dark_pink),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun TimeSelectButton(
    label: String,
    value: String,
    isError: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundColor = if (isError) Color(0x15FF0000) else Color(0xFFF5F5F5)
    val borderColor = if (isError) colorResource(id = R.color.dark_pink) else Color.Transparent
    val textColor = if (value.isEmpty()) Color.Gray else Color.Black

    Box(
        modifier = modifier
            .background(
                color = backgroundColor, shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.clock),
                colorFilter = ColorFilter.tint(if (isError) colorResource(id = R.color.dark_pink) else Color.Gray),
                contentDescription = "Clock",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = if (value.isEmpty()) label else value,
                    color = if (isError && value.isEmpty()) colorResource(id = R.color.dark_pink) else textColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                if (isError && value.isEmpty()) {
                    Text(
                        text = "Required",
                        color = colorResource(id = R.color.dark_pink),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun PriorityButton(
    text: String,
    selectedPriority: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = selectedPriority == text

    val backgroundColor = when {
        isSelected && text == "High" -> colorResource(id = R.color.dark_pink)
        isSelected && text == "Medium" -> colorResource(id = R.color.darkYellow)
        isSelected && text == "Low" -> colorResource(R.color.darkBlue)
        !isSelected && text == "High" -> Color(0x25F5B7B1)
        !isSelected && text == "Medium" -> Color(0x25F7E794)
        !isSelected && text == "Low" -> Color(0x25B3E5FC)
        else -> Color.White
    }

    val textColor = when {
        isSelected -> Color.White
        text == "High" -> colorResource(id = R.color.dark_pink)
        text == "Medium" -> colorResource(id = R.color.veryDarkYellow)
        text == "Low" -> colorResource(R.color.darkBlue)
        else -> Color.Black
    }

    Box(
        modifier = modifier
            .background(
                color = backgroundColor, shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelect(text) }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center) {
        Text(
            text = text, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Bold
        )
    }
}

// Animation for validation errors
@Composable
fun ValidationErrorAnimation(
    isVisible: Boolean, modifier: Modifier = Modifier, content: @Composable () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f, label = "errorAlpha"
    )

    Box(
        modifier = modifier.alpha(alpha)
    ) {
        if (isVisible) {
            content()
        }
    }
}

// Add a field label composable that includes the required indicator
@Composable
fun FieldLabel(
    text: String,
    isRequired: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.dark_gray)
            )

            if (isRequired) {
                Text(
                    text = " *",
                    color = colorResource(id = R.color.dark_pink),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (isError && errorMessage != null) {
            ValidationErrorAnimation(isVisible = isError) {
                Text(
                    text = errorMessage,
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.dark_pink),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

// A section container for better visual organization
@Composable
fun FormSection(
    modifier: Modifier = Modifier, content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFF9F9F9), shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        content()
    }
}

// A composable for real-time status messages
@Composable
fun ValidationStatusMessage(
    validationState: Boolean?,
    successMessage: String = "Looks good!",
    errorMessage: String = "Please check this field"
) {
    val message = when (validationState) {
        true -> successMessage
        false -> errorMessage
        null -> ""
    }

    val color = when (validationState) {
        true -> Color.Green
        false -> colorResource(id = R.color.dark_pink)
        null -> Color.Transparent
    }

    AnimatedVisibility(
        visible = validationState != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Text(
            text = message,
            color = color,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp, start = 8.dp)
        )
    }
}

// Improved animated submit button
@Composable
fun SubmitTaskButton(
    onClick: () -> Unit, isLoading: Boolean, buttonText: String
) {
    Button(
        onClick = { if (!isLoading) onClick() },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(id = R.color.button_color),
            disabledContainerColor = colorResource(id = R.color.button_color).copy(alpha = 0.6f)
        ),
        enabled = !isLoading,
        elevation = ButtonDefaults.buttonElevation(8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = buttonText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// A better error message display for network or other errors
@Composable
fun ErrorMessage(
    message: String, onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFFFEBEE), shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                tint = colorResource(id = R.color.dark_pink),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = message,
                color = Color.Black,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Dismiss",
                tint = Color.Gray,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onDismiss() })
        }
    }
}

// Implementation of a date/time validator
fun validateDateTime(
    startDate: String, endDate: String, startTime: String, endTime: String
): Pair<Boolean, String?> {
    if (startDate.isEmpty() || endDate.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
        return Pair(false, "All date and time fields are required")
    }

    try {
        val sDate = convertStrToDate(startDate)
        val eDate = convertStrToDate(endDate)
        val sTime = convertStrToTime(startTime)
        val eTime = convertStrToTime(endTime)

        if (eDate!! < sDate) {
            return Pair(false, "End date must be after or equal to start date")
        }

        if (eDate == sDate && eTime!! < sTime) {
            return Pair(false, "For same day tasks, end time must be after start time")
        }

        return Pair(true, null)
    } catch (e: Exception) {
        return Pair(false, "Invalid date or time format")
    }
}

// Add themed toast messages for better user feedback
fun showToast(context: Context, message: String, isError: Boolean = false) {
    val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
    val view = toast.view

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        // For Android 11+, we can't customize toasts directly
        toast.show()
    } else {
        // For Android 10 and below, we can still customize toast appearance
        view?.background?.setTint(
            if (isError) ContextCompat.getColor(context, R.color.dark_pink)
            else ContextCompat.getColor(context, R.color.darkBlue)
        )

        val text = view?.findViewById<TextView>(android.R.id.message)
        text?.setTextColor(android.graphics.Color.WHITE)
        toast.show()
    }
}

// Create a shimmer loading effect for the form while data is loading
@Composable
fun ShimmerEffect() {
    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = 0f, targetValue = 1000f, animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000, easing = FastOutSlowInEasing
            ), repeatMode = RepeatMode.Restart
        ), label = "shimmer"
    )

    val shimmerColorShades = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f)
    )

    val brush = Brush.linearGradient(
        colors = shimmerColorShades,
        start = Offset(translateAnim, translateAnim),
        end = Offset(translateAnim + 100f, translateAnim + 100f)
    )

    // Shimmer layout items
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Title shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(24.dp)
                .background(brush, RoundedCornerShape(4.dp))
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Input field shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(brush, RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Another title shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(24.dp)
                .background(brush, RoundedCornerShape(4.dp))
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Text area shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(brush, RoundedCornerShape(8.dp))
        )
    }
}


// Function to convert string date to Date object
fun convertStrToDate(dateStr: String): Date? {
    return try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dateFormat.parse(dateStr)
    } catch (e: ParseException) {
        null
    }
}

// Function to convert string time to Date object
fun convertStrToTime(timeStr: String): Date? {
    return try {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        timeFormat.parse(timeStr)
    } catch (e: ParseException) {
        null
    }
}

// Function to check network availability
fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

fun Modifier.shake(enabled: Boolean = true) = composed {
    var offsetX by remember { mutableStateOf(0f) }

    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX, animationSpec = spring(
            dampingRatio = 0.2f, stiffness = Spring.StiffnessHigh
        ), label = "shake"
    )
    LaunchedEffect(key1 = enabled) {
        if (enabled) {
            offsetX = 10f
            delay(50)
            offsetX = -10f
            delay(50)
            offsetX = 5f
            delay(50)
            offsetX = -5f
            delay(50)
            offsetX = 0f
        }
    }
    Modifier.offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
}

