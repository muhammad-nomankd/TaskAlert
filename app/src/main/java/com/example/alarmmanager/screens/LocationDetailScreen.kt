package com.example.alarmmanager.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.alarmmanager.R
import com.example.alarmmanager.screens.ui.theme.AlarmManagerTheme
import com.example.alarmmanager.viewmodels.LocationViewModel
import com.example.alarmmanager.viewmodels.WeatherViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class LocationDetailScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            AlarmManagerTheme {
                val navControler = rememberNavController()
                LocationDetailContent(navController = navControler)
            }

        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun LocationDetailContent(
        viewModel: LocationViewModel = viewModel(),
        navController: NavController
    ) {
        var cityInput by rememberSaveable { mutableStateOf("") }
        var expanded by rememberSaveable { mutableStateOf(false) }
        val context = LocalContext.current
        var currentlySelectedLocation by rememberSaveable { mutableStateOf("") }
        var currentlySelectedCountry by rememberSaveable { mutableStateOf("") }
        var city by rememberSaveable { mutableStateOf("") }
        var country by rememberSaveable { mutableStateOf("") }
        val firestore = FirebaseFirestore.getInstance()
        val forecastViewModel: WeatherViewModel = viewModel()
        var isLoading by rememberSaveable { mutableStateOf(false) }
        val fiveDayWeatherList by forecastViewModel.fiveDaysWeatherList.observeAsState()
        var isShowPopUp by rememberSaveable { mutableStateOf(false) }
        var isUpdated by rememberSaveable {
            mutableStateOf(false)
        }
        val apiKey = "e6844bc411msh69a178d35f2fabbp1e01fbjsnc9a755db3e73"


        LaunchedEffect(Unit, currentlySelectedLocation) {
            isLoading = true
            firestore.collection("User").document(FirebaseAuth.getInstance().currentUser?.uid ?: "")
                .collection("location")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        currentlySelectedLocation = document.getString("location") ?: ""
                        currentlySelectedCountry = document.getString("country") ?: ""
                        forecastViewModel.fetchForecast(currentlySelectedLocation)
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        }
        LaunchedEffect(isUpdated) {
            firestore.collection("User").document(FirebaseAuth.getInstance().currentUser?.uid ?: "")
                .collection("location")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        currentlySelectedLocation = document.getString("location") ?: ""
                        currentlySelectedCountry = document.getString("country") ?: ""
                        forecastViewModel.fetchForecast(currentlySelectedLocation)
                    }
                    isUpdated = false
                }
                .addOnFailureListener {
                    isUpdated = false
                }
        }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(Color(0xFFE1E4E8))
            ) {

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, top = 18.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go back",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { navController.navigateUp() })
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Location Detail Screen",
                        color = colorResource(id = R.color.dark_gray),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(0.5.dp)
                        .background(Color.LightGray)
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }, modifier = Modifier
                        .padding(horizontal = 32.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .background(color = Color.Gray, shape = RoundedCornerShape(16.dp))
                ) {
                    TextField(
                        value = cityInput,
                        onValueChange = {
                            cityInput = it
                            if (cityInput.isNotEmpty()) {
                                viewModel.fetchCities(apiKey, cityInput)
                                expanded = true
                            }
                        }, readOnly = false,
                        label = {
                            Text(
                                text = if (currentlySelectedLocation.isNotEmpty()) "Change location.." else "select Location",
                                color = colorResource(
                                    id = R.color.medium_gray
                                ),
                                fontSize = 18.sp
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = Color.LightGray,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(Color(0xFFFAFAFA), shape = RoundedCornerShape(16.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
                            cursorColor = Color.DarkGray,
                            focusedIndicatorColor = Color(0xFFDADADA),
                            unfocusedIndicatorColor = Color(0xFFDADADA)
                        ),
                        textStyle = TextStyle(Color.DarkGray, fontSize = 16.sp)
                    )

                    ExposedDropdownMenu(
                        expanded = expanded, onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .background(
                                colorResource(id = R.color.custom_white),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .height(160.dp)
                            .animateContentSize()
                    ) {
                        viewModel.cities.forEach {
                            DropdownMenuItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .padding(start = 10.dp, end = 10.dp, bottom = 4.dp)
                                    .background(
                                        color = Color.White,
                                        shape = RoundedCornerShape(32.dp)
                                    ),
                                text = {
                                    androidx.wear.compose.material.Text(
                                        text = "${it.name}, ${it.country}",
                                        color = colorResource(id = R.color.medium_gray),
                                        fontSize = 18.sp
                                    )
                                },
                                onClick = {
                                    country = it.country
                                    city = it.name
                                    isShowPopUp = true
                                    expanded = false
                                })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier
                        .padding(
                            start = 32.dp,
                            end = 32.dp,
                            top = 8.dp,
                            bottom = 32.dp
                        )
                        .shadow(
                            8.dp,
                            RoundedCornerShape(16.dp),
                            clip = false,
                            spotColor = Color(0x30000000)
                        ), colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE0F7FA)
                    ), shape = RoundedCornerShape(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (currentlySelectedLocation.isNotEmpty()) "Upcoming forecast of $currentlySelectedLocation" else "No Location selected",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 20.sp,
                        color = colorResource(id = R.color.dark_gray),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    fiveDayWeatherList?.forEachIndexed() { index, forecastItem ->
                        val inputFormat =
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        val outputFormat = SimpleDateFormat(
                            "EEEE ha",
                            Locale.getDefault()
                        )
                        val parsedDate = inputFormat.parse(forecastItem.dt_txt)
                        val dayOfWeek = parsedDate?.let {
                            outputFormat.format(it)
                        } ?: forecastItem.dt_txt

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, start = 8.dp, end = 32.dp),
                            horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dayOfWeek,
                                style = MaterialTheme.typography.bodyLarge,
                                color = colorResource(id = R.color.medium_gray),
                                modifier = Modifier
                                    .padding(start = 32.dp)
                                    .weight(1.6f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Row(Modifier.weight(1f)) {
                                Image(
                                    painter = painterResource(
                                        if (forecastItem.pop > 0.7) R.drawable.drop
                                        else if (forecastItem.pop > 0.3 && forecastItem.pop < 0.7) R.drawable.halffilleddrop
                                        else R.drawable.emptydrop
                                    ),
                                    contentDescription = "chance of rain",
                                    Modifier.size(12.dp),
                                    colorFilter = ColorFilter.tint(Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${(forecastItem.pop * 100).toInt()}%",
                                    fontWeight = FontWeight.Normal
                                )
                            }
                            AsyncImage(
                                model = if(forecastItem.weather[0].icon.isEmpty())"https://openweathermap.org/img/wn/${forecastItem.weather[0].icon}" else R.drawable.weather_icon,
                                contentDescription = "Weather Icon",
                                modifier = Modifier.size(20.dp),
                            )

                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${forecastItem.main.temp_min.toInt()}°/${forecastItem.main.temp_max.toInt()}°",
                                color = Color.DarkGray, fontWeight = FontWeight.Normal
                            )

                        }
                    }


                }


            }
        if (isLoading || isUpdated){
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    strokeWidth = 1.dp,
                    color = Color.Gray
                )
            }
        }

        if (isShowPopUp) {
            AlertDialog(onDismissRequest = { isShowPopUp = false },
                shape = RoundedCornerShape(16.dp),
                backgroundColor = colorResource(
                    id = R.color.task_color
                ),
                confirmButton = {
                    Button(
                        onClick = {
                            isShowPopUp = false
                            viewModel.saveUserLocation(
                                city,
                                "12345",
                                country,
                                context = context
                            )
                            WeatherViewModel().fetchWeather(city)
                            cityInput = ""
                            isUpdated = true


                        },
                        elevation = ButtonDefaults.elevatedButtonElevation(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(
                                id = R.color.button_color
                            )
                        )
                    ) {
                        Text(
                            text = "Change",
                            color = Color.White, modifier = Modifier
                        )
                    }

                },
                title = {
                    Text(
                        text = "Change Location",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you  want to change location?",
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                dismissButton = {
                    Button(
                        onClick = { isShowPopUp = false },
                        elevation = ButtonDefaults.elevatedButtonElevation(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            colorResource(id = R.color.darkBlue)
                        )
                    ) {
                        Text(text = "Cancel", color = Color.White)
                    }
                })
        }


    }
}
