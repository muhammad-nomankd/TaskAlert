package com.example.alarmmanager.screens

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
import androidx.compose.runtime.remember
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
                LocationDetailScreenContent(navControler)
            }

        }
    }

    @Composable
    fun LocationDetailScreenContent(navController: NavController) {

        val apiKey = "e6844bc411msh69a178d35f2fabbp1e01fbjsnc9a755db3e73"

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .background(colorResource(id = R.color.custom_white))
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
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .size(0.8.dp)
                    .background(Color.LightGray)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Currently Selected Location", modifier = Modifier.padding(start = 32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            CityDropdownMenu(apiKey = apiKey)

        }

    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CityDropdownMenu(apiKey: String, viewModel: LocationViewModel = viewModel()) {
        var cityInput by remember { mutableStateOf("") }
        var expanded by remember { mutableStateOf(false) }
        val context = LocalContext.current
        var currentlySelectedLocation by rememberSaveable { mutableStateOf("") }
        val firestore = FirebaseFirestore.getInstance()
        val forecastViewModel: WeatherViewModel = viewModel()
        val forecasteIcon by forecastViewModel.weatherIconForForecaste.observeAsState()
        val forecasteTempMin by forecastViewModel.weatherForecasteTempMin.observeAsState()
        val forecasteTempMax by forecastViewModel.weatherForecasteTempMax.observeAsState()
        val forecastePrec by forecastViewModel.weatherForecastePrecipitation.observeAsState()
        var isLoading by rememberSaveable { mutableStateOf(false) }
        val fiveDayWeatherList by forecastViewModel.fiveDaysWeatherList.observeAsState()


        LaunchedEffect(Unit) {
            isLoading = true
            firestore.collection("User").document(FirebaseAuth.getInstance().currentUser?.uid ?: "")
                .collection("location")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        currentlySelectedLocation = document.getString("location") ?: ""
                        forecastViewModel.fetchForecast(currentlySelectedLocation)
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                    Toast.makeText(context, "Failed to get location", Toast.LENGTH_SHORT).show()
                }
            Log.d("temp", forecasteTempMin.toString())
        }
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column() {

                Text(
                    text = currentlySelectedLocation,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(start = 32.dp),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }, modifier = Modifier
                        .background(
                            colorResource(id = R.color.custom_white),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 32.dp, vertical = 8.dp)
                        .fillMaxWidth()
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
                            Text(text = "Change location..")
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .background(Color.White, shape = RoundedCornerShape(8.dp))
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
                            .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(8.dp))
                    ) {
                        viewModel.cities.forEach {
                            DropdownMenuItem(
                                text = {
                                    androidx.wear.compose.material.Text(
                                        text = "${it.name}, ${it.country}",
                                        color = Color.DarkGray
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = Color.White,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp),
                                onClick = {
                                    expanded = false
                                    viewModel.saveUserLocation(
                                        it.name,
                                        "12345",
                                        it.country,
                                        context = context
                                    )
                                    currentlySelectedLocation = it.name
                                    cityInput = ""
                                    Toast.makeText(
                                        context,
                                        "Location changed to ${it.name} ${it.country}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    modifier = Modifier.padding(
                        start = 12.dp,
                        end = 32.dp,
                        top = 8.dp,
                        bottom = 32.dp
                    ), colors = CardDefaults.cardColors(
                        containerColor = colorResource(id = R.color.task_color)
                    ), shape = RoundedCornerShape(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Upcoming forecast of $currentlySelectedLocation",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 20.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Medium,
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
                        ) // EEEE gives full day name
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
                                color = Color.DarkGray,
                                modifier = Modifier
                                    .padding(start = 32.dp)
                                    .weight(1.3f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Row(Modifier.weight(1f)) {
                                Image(
                                    painter = painterResource(if (forecastItem.pop > 0.7) R.drawable.drop else if (forecastItem.pop > 0.3 && forecastItem.pop < 0.7) R.drawable.halffilleddrop else R.drawable.emptydrop),
                                    contentDescription = "chance of rain",
                                    Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${(forecastItem.pop * 100).toInt()}%",
                                    fontWeight = FontWeight.Normal
                                )
                            }
                            AsyncImage(
                                model = "https://openweathermap.org/img/wn/${forecastItem.weather[0].icon}@2x.png",
                                contentDescription = "Weather Icon",
                                modifier = Modifier.size(20.dp),
                                placeholder = painterResource(R.drawable.weather_icon), // Use anactual placeholder resource
                                error = painterResource(R.drawable.cloudy) // Use an actual error resource
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
        }
    }
}
