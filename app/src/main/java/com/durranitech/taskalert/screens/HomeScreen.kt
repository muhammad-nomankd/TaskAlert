package com.durranitech.taskalert.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.durranitech.taskalert.R
import com.durranitech.taskalert.dataclasses.Task
import com.durranitech.taskalert.viewmodels.GetTaskViewModel
import com.durranitech.taskalert.viewmodels.WeatherViewModel
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class HomeScreen : ComponentActivity() {
    var loading: Boolean by mutableStateOf(false)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HomeScreenUi(navController = NavController(LocalContext.current), LocalContext.current)
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("NotConstructor", "CoroutineCreationDuringComposition", "MissingPermission")
    @Composable
    fun HomeScreenUi(navController: NavController, context: Context) {
        var profileImageUrl by rememberSaveable { mutableStateOf("") }
        var userName by rememberSaveable { mutableStateOf("") }
        var userEmail by rememberSaveable { mutableStateOf("") }
        var selectedCategoryState by remember { mutableStateOf("All") }
        val firestore = FirebaseFirestore.getInstance()
        val viewmodel: GetTaskViewModel = viewModel()
        val weatherViewModel: WeatherViewModel = viewModel()
        val tasks by viewmodel.filteredTasks.observeAsState(emptyList())
        val nonfilterTasks by viewmodel.tasksForUpCommingCategory.collectAsState()
        var showPopUp by rememberSaveable { mutableStateOf(false) }
        var currenttask by rememberSaveable { mutableStateOf("") }
        var taskStatus by rememberSaveable { mutableStateOf("") }
        var deleteTask by rememberSaveable { mutableStateOf(false) }
        var isLoading by rememberSaveable { mutableStateOf(false) }
        var selectedWeatherLocation by rememberSaveable { mutableStateOf("") }
        val currentTemprature by weatherViewModel.temperature.observeAsState()
        val currentWeatherDescription by weatherViewModel.weatherDescription.observeAsState()
        val currentHumidity by weatherViewModel.weatherHumidity.observeAsState()
        var isWeatherLoading by rememberSaveable { mutableStateOf(false) }
        var isPressed by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()
        val scale by animateFloatAsState(if (isPressed) 1.1f else 1.0f)
        var isPermisionGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isLocationSaved = sharedPreferences.getBoolean("location_saved", false)

        val locationPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                isWeatherLoading = true
                getUserLocation(context)
            } else {
                isWeatherLoading = false
            }
        }
        LaunchedEffect(Unit, loading) {
            // Getting User Detail from FireStore
            isLoading = true
            firestore.collection("User").document(FirebaseAuth.getInstance().currentUser?.uid ?: "")
                .get().addOnSuccessListener { document ->
                    userName = document.getString("name") ?: ""
                    profileImageUrl = document.getString("imageUrl") ?: ""
                    userEmail = document.getString("email") ?: ""
                }
                .addOnSuccessListener {
                    isLoading = false
                }


            // Fetch tasks for UpComing Category
            viewmodel.filterTasksForUpCommingCategory("In Progress and Pending")

            // Fetching Weather Location and data from FireStore
            isWeatherLoading = true
            firestore.collection("User").document(FirebaseAuth.getInstance().currentUser?.uid ?: "")
                .collection("location")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    isWeatherLoading = false
                    for (document in querySnapshot.documents) {
                        selectedWeatherLocation = document.getString("location") ?: ""
                    }
                    if (selectedWeatherLocation.isNotBlank()) {
                        weatherViewModel.fetchWeather(selectedWeatherLocation)
                    } else {

                        Log.e("Weather Error", "Selected weather location is blank")
                    }
                }
                .addOnFailureListener {
                    isWeatherLoading = false
                    Log.d("weather data error", "error fetching weather data from firestore")
                }


            //Ask for location permission
            if (!isLocationEnabled(context)) {
                Toast.makeText(
                    context,
                    "Please enable device location to access weather services",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                if (isPermisionGranted && !isLocationSaved) {
                    getUserLocation(context)
                    with(sharedPreferences.edit()) {
                        putBoolean("location_saved", true)
                        apply()
                    }

                } else if (!isPermisionGranted) {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }


        }


        // Getting filtered Tasks based on selected Category Button
        LaunchedEffect(selectedCategoryState) {
            viewmodel.filterTasks(selectedCategoryState)
        }

        //Delete task logic
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

        // Refresh tasks Category after deletion
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
                backgroundColor = Color.White,
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
                        color = Color.Black,
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you  want to delete this task?",
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        color = Color.DarkGray,
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

        // Category Button for selecting specific category like All, In Progress or Completed
        @Composable
        fun categoryButton(text: String, selectedCategory: String, onClick: (String) -> Unit) {
            val isSelected = selectedCategory == text
            Button(
                onClick = { onClick(text) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) colorResource(id = R.color.button_color) else Color.White,
                    contentColor = if (isSelected) Color.White else colorResource(id = R.color.button_color),
                )
            ) {
                Text(
                    text,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }

        // Adding task Icons to each task
        @Composable
        fun getTaskIcon(title: String): Int {
            return when {
                title.lowercase(Locale.ROOT)
                    .contains("office", ignoreCase = true) || title.lowercase(
                    Locale.ROOT
                )
                    .contains("work", ignoreCase = true) || title.lowercase(Locale.ROOT)
                    .contains("job", ignoreCase = true) -> R.drawable.office

                title.lowercase(Locale.ROOT).contains("home", ignoreCase = true) || title.lowercase(
                    Locale.ROOT
                )
                    .contains("house", ignoreCase = true) || title.lowercase(Locale.ROOT)
                    .contains("building", ignoreCase = true) -> R.drawable.house

                title.lowercase(Locale.ROOT)
                    .contains("shopping", ignoreCase = true) -> R.drawable.shopping

                title.lowercase(Locale.ROOT).contains("food", ignoreCase = true) || title.lowercase(
                    Locale.ROOT
                )
                    .contains("lunch", ignoreCase = true) || title.lowercase(Locale.ROOT)
                    .contains("dinner", ignoreCase = true) || title.lowercase(Locale.ROOT)
                    .contains("breakfast", ignoreCase = true) -> R.drawable.lunch

                title.lowercase(Locale.ROOT)
                    .contains("medicine", ignoreCase = true) || title.lowercase(
                    Locale.ROOT
                )
                    .contains("tablets", ignoreCase = true) || title.lowercase(Locale.ROOT)
                    .contains("doctor", ignoreCase = true) || title.lowercase(Locale.ROOT)
                    .contains("hospital", ignoreCase = true) || title.lowercase(Locale.ROOT)
                    .contains("appointment", ignoreCase = true) -> R.drawable.medicine

                title.lowercase(Locale.ROOT).contains("Yoga", ignoreCase = true) || title.lowercase(
                    Locale.ROOT
                )
                    .contains("Gym", ignoreCase = true) || title.lowercase(Locale.ROOT)
                    .contains("Exercise", ignoreCase = true) -> R.drawable.house

                else -> R.drawable.taskicon
            }
        }

        // Task Item for LazyRow
        @Composable
        fun taskItem(task: Task, longClick: () -> Unit, onClick: () -> Unit) {
            Card(
                backgroundColor = Color.White,
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp), clip = false, spotColor = Color.Black)
                    .width(160.dp)
                    .scale(scale)
                    .clip(shape = RoundedCornerShape(16.dp))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                tryAwaitRelease()
                                isPressed = false
                            },
                            onLongPress = { longClick() },
                            onTap = { onClick() }
                        )
                    }
                    .combinedClickable(
                        onClick = { onClick() },
                        onLongClick = { longClick() },
                    ), elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Image(
                        painter = painterResource(id = getTaskIcon(task.title)),
                        contentDescription = "Task Icon",
                        Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.dark_gray),
                        fontSize = 20.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dateFormater(task.startDate),
                        fontSize = 16.sp,
                        color = colorResource(id = R.color.medium_gray),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        when (task.status) {
                            "Completed" -> Image(
                                painter = painterResource(id = R.drawable.checkicon),
                                contentDescription = "check icon",
                                modifier = Modifier
                                    .size(24.dp)

                            )

                            "Pending" -> Image(
                                painter = painterResource(id = R.drawable.pending),
                                contentDescription = "pending",
                                colorFilter = ColorFilter.tint(Color(0xFFFFA500)),
                                modifier = Modifier.size(24.dp)
                            )

                            "In Progress" -> Image(
                                painter = painterResource(id = R.drawable.inprogressicon),
                                contentDescription = "In Progress",
                                modifier = Modifier.size(24.dp)
                            )
                        }
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
                                    .align(Alignment.CenterEnd)
                                    .padding(start = 4.dp)
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
            }
        }

        // Task Item for UpComing tasks
        @OptIn(ExperimentalFoundationApi::class)
        @Composable
        fun upComingTasksItem(task: Task, longClick: () -> Unit, onClick: () -> Unit) {
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
                            text = timeFormater(task.startTime) + " - " + timeFormater(task.endTime),
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
                        }
                    }
                }
            }
        }

        // MainHome Screen UI
        LazyColumn(
            modifier = Modifier
                .background(colorResource(id = R.color.custom_white))
                .fillMaxSize()

        ) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp, start = 32.dp, end = 32.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Box(modifier = Modifier
                        .clickable {
                            navController.navigate("profile")
                        }) {
                        Image(
                            painter = rememberAsyncImagePainter(model = profileImageUrl.ifEmpty { R.drawable.person }),
                            contentDescription = "Profile Image",
                            Modifier
                                .clip(CircleShape)
                                .size(60.dp)
                                .border(1.dp, Color.White, CircleShape)
                                .background(Color.LightGray),
                            contentScale = ContentScale.Crop
                        )

                        // Show badge if user name is empty show a dot on profile icon
                        if (userName.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray)
                                    .align(Alignment.BottomEnd)
                            )
                        }
                    }
                    if (isWeatherLoading) {
                        coroutineScope.launch {
                            delay(1000)
                        }
                        Box {
                            CircularProgressIndicator(strokeWidth = 1.dp, color = Color.Gray)
                        }

                    } else if (selectedWeatherLocation.isEmpty()) {
                        Text(text = "select location to view weather", modifier = Modifier
                            .clickable { navController.navigate("locationDetailScreen") })
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {

                            Text(
                                text = selectedWeatherLocation.ifEmpty { "Loading..." },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorResource(id = R.color.dark_gray),
                                modifier = Modifier
                                    .clickable { navController.navigate("locationDetailScreen") }

                            )
                            Spacer(modifier = Modifier.height(4.dp))


                            Text(
                                text = if (currentTemprature != null) "${currentTemprature!!.toInt()}°" else "Loading...",
                                fontFamily = if (currentTemprature != null) FontFamily(Font(R.font.roboto_light)) else FontFamily.Default,
                                fontSize = if (currentTemprature != null) 32.sp else 12.sp,
                                color = colorResource(id = R.color.dark_gray),
                                fontWeight = FontWeight.Bold
                            )



                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = currentWeatherDescription ?: "Loading...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorResource(id = R.color.medium_gray)
                            )
                            Text(
                                text = "Humidity ${currentHumidity ?: ""}%",
                                fontSize = 14.sp,
                                color = colorResource(id = R.color.medium_gray),
                            )
                        }
                    }


                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
            item {
                Text(
                    "Categories",
                    color = colorResource(id = R.color.dark_gray),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(start = 32.dp)
                )
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    categoryButton(text = "All", selectedCategoryState) {
                        selectedCategoryState = it
                    }
                    categoryButton(
                        text = "In Progress", selectedCategoryState
                    ) {
                        selectedCategoryState = it
                    }
                    categoryButton(text = "Completed", selectedCategoryState) {
                        selectedCategoryState = it
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
            // LazyRow for horizontal scrollable tasks
            item {
                LazyRow(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, end = 32.dp)
                ) {
                    items(tasks, key = { it.taskId }) { task ->
                        taskItem(task, longClick = {
                            currenttask = task.taskId
                            showPopUp = true
                            taskStatus = task.status

                        }, onClick = {
                            navController.navigate("createTask?taskId=${task.taskId}&taskTitle=${task.title}&taskDescription=${task.description}&startDate=${task.startDate}&endDate=${task.endDate}&startTime=${task.startTime}&endTime=${task.endTime}&priority=${task.priority}")
                        })
                    }
                }
                if (tasks.isEmpty()){
                    Spacer(modifier = Modifier.height(168.dp))
                }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            // View all tasks
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(text = if (tasks.size > 2) "View all" else "",
                        color = colorResource(id = R.color.button_color),
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(end = 32.dp)
                            .align(Alignment.CenterEnd)
                            .clickable {
                                navController.navigate("taskListScreen")
                            })
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            // UpComing tasks List
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = colorResource(
                        id = R.color.task_color
                    )
                ) {
                    Column(Modifier.padding(start = 24.dp, end = 24.dp, top = 12.dp)) {
                        Text(
                            text = if (nonfilterTasks.isEmpty()) "No Upcoming Tasks" else "Upcoming Tasks",
                            fontSize = 20.sp,
                            color = colorResource(id = R.color.dark_gray),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        nonfilterTasks.forEach { task ->
                            upComingTasksItem(task,
                                longClick = {
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
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(32.dp))

            // Floating Action Button for Creating task Screen navigation
            FloatingActionButton(
                onClick = { navController.navigate("createTask") },
                containerColor = colorResource(id = R.color.fab_color),
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(48.dp)

            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add task",
                    modifier = Modifier.size(32.dp)
                )
            }

        }
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        strokeWidth = 1.dp,
                        modifier = Modifier
                            .align(alignment = Alignment.CenterHorizontally),
                        color = Color.Gray
                    )
                }
            }
        }

    }

    // Formating Date
    private fun dateFormater(dateString: String): String {
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

    // Formating Time
    private fun timeFormater(timeString: String): String {

        val fetchedTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dDF = SimpleDateFormat("h.mm a", Locale.getDefault())
        return try {
            val time = fetchedTime.parse(timeString)
            dDF.format(time ?: "")
        } catch (e: Exception) {
            e.printStackTrace()
            "invalid formated time"
        }
    }

    @SuppressLint("MissingPermission")
    private fun getUserLocation(context: Context) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                location?.let {
                    if (isNetworkAvailable(context)) {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        try {
                            val addresses =
                                geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            if (addresses != null) {
                                if (addresses.isNotEmpty()) {
                                    val locationName = addresses[0].locality ?: "Unknown location"
                                    saveLocationToFireStore(locationName, context)
                                    loading = true
                                }
                            } else {
                                Log.d("null address", "null address")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "No network connection available",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                }
            }
    }

    private fun saveLocationToFireStore(locationName: String, context: Context) {
        val firestore = FirebaseFirestore.getInstance()
        val docRef =
            firestore.collection("User").document(FirebaseAuth.getInstance().currentUser?.uid ?: "")
                .collection("location").document("12345")
        docRef.set(
            mapOf(
                "location" to locationName,
                "locationId" to "12345",
                "country" to ""

            )
        )

    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }

    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

}