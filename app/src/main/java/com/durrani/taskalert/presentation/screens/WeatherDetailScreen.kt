package com.durrani.taskalert.presentation.ui.screens

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.durrani.taskalert.R
import com.durrani.taskalert.presentation.ui.theme.AlarmManagerTheme
import com.durrani.taskalert.presentation.viewmodels.LocationViewModel
import com.durrani.taskalert.presentation.viewmodels.WeatherViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class WeatherDetailScreen : androidx.activity.ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            AlarmManagerTheme {
                val navController = rememberNavController()
                LocationDetailContent(navController = navController)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun LocationDetailContent(
        viewModel: LocationViewModel = viewModel(),
        navController: NavController
    ) {
        val context = androidx.compose.ui.platform.LocalContext.current
        val firestore = FirebaseFirestore.getInstance()
        val forecastViewModel: WeatherViewModel =
            viewModel()
        val isWeatherLoading by forecastViewModel.isLoading.collectAsState()
        val hasDataBeenFetched by forecastViewModel.dataFetched.collectAsState()
        val forecastData by forecastViewModel.fiveDaysWeatherList.collectAsState()
        val apiKey = "e6844bc411msh69a178d35f2fabbp1e01fbjsnc9a755db3e73"

        // States
        var searchQuery by rememberSaveable {
            mutableStateOf(
                ""
            )
        }
        var isShowingResults by rememberSaveable {
            mutableStateOf(
                false
            )
        }
        var currentLocation by rememberSaveable {
            mutableStateOf(
                ""
            )
        }
        var currentCountry by rememberSaveable {
            mutableStateOf(
                ""
            )
        }
        var isLoadingFirestore by rememberSaveable {
            mutableStateOf(
                false
            )
        }
        var isShowConfirmDialog by rememberSaveable {
            mutableStateOf(
                false
            )
        }
        var selectedCity by rememberSaveable {
            mutableStateOf(
                ""
            )
        }
        var selectedCountry by rememberSaveable {
            mutableStateOf(
                ""
            )
        }
        var refreshData by rememberSaveable {
            mutableStateOf(
                false
            )
        }

        // Weather data
        val fiveDayWeatherList = forecastData


        // Fetch location data
        androidx.compose.runtime.LaunchedEffect(Unit) {
            firestore.collection("User").document(
                FirebaseAuth.getInstance().currentUser?.uid ?: ""
            ).collection("location").get().addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    for (document in querySnapshot.documents) {
                        currentLocation = document.getString("location") ?: ""
                        currentCountry = document.getString("country") ?: ""

                        forecastViewModel.fetchForecast(currentLocation)

                    }
                }
                isLoadingFirestore = false
                refreshData = false
            }.addOnFailureListener {
                isLoadingFirestore = false
                refreshData = false
            }
        }

        // Search for cities when query changes
        androidx.compose.runtime.LaunchedEffect(searchQuery) {
            if (searchQuery.length >= 2) {
                viewModel.fetchCities(apiKey, searchQuery)
                isShowingResults = true
            } else {
                isShowingResults = false
            }
        }

        androidx.compose.material3.Scaffold(
            topBar = {
                androidx.compose.material3.TopAppBar(
                    title = {
                        Text(
                            "Weather Forecast",
                            fontWeight = FontWeight.Companion.Bold,
                            color = Color.Companion.White
                        )
                    }, navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Go back",
                                tint = Color.White
                            )
                        }
                    }, colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colorResource(R.color.green)
                    )
                )
            }) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFA8BCEA), Color(0xFF454F65)
                            )
                        )
                    )
                    .padding(paddingValues)
            ) {
                // Loading shimmer effect
                if (isLoadingFirestore || fiveDayWeatherList.isEmpty() || isWeatherLoading && !hasDataBeenFetched) {
                    ShimmerLocationScreen()
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        // Location header
                        LocationHeader(
                            location = if (currentLocation.isNotEmpty()) "$currentLocation, $currentCountry" else "No location selected",
                            isLocationSet = currentLocation.isNotEmpty()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Search location field
                        SearchLocationField(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            onClearClick = { searchQuery = "" })

                        Spacer(modifier = Modifier.height(8.dp))

                        // Search results
                        AnimatedVisibility(
                            visible = isShowingResults && searchQuery.isNotEmpty(),
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            SearchResultsList(
                                cities = viewModel.cities, onCitySelected = { city, country ->
                                    selectedCity = city
                                    selectedCountry = country
                                    isShowConfirmDialog = true
                                    isShowingResults = false
                                    searchQuery = ""
                                })
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Weather forecast
                        if (currentLocation.isNotEmpty() && !isLoadingFirestore) {
                            WeatherForecastCard(
                                location = currentLocation,
                                forecasts = fiveDayWeatherList ?: emptyList()
                            )
                        } else if (!isLoadingFirestore && currentLocation.isEmpty()) {
                            ShimmerLocationScreen()
                        }
                    }
                }


                // Confirmation dialog
                if (isShowConfirmDialog) {
                    LocationChangeConfirmationDialog(
                        cityName = selectedCity,
                        countryName = selectedCountry,
                        onConfirm = {
                            currentLocation = selectedCity
                            currentCountry = selectedCountry
                            viewModel.saveUserLocation(
                                selectedCity, "12345", // ZIP code placeholder
                                selectedCountry, context = context
                            )
                            forecastViewModel.fetchWeather(selectedCity)
                            isShowConfirmDialog = false
                            refreshData = true
                        },
                        onDismiss = {
                            isShowConfirmDialog = false
                        })
                }
            }
        }
    }

    @Composable
    fun LocationHeader(location: String, isLocationSet: Boolean) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 4.dp
            ), colors = CardDefaults.elevatedCardColors(
                containerColor = if (isLocationSet) colorResource(R.color.green) else Color(
                    0xFFE57373
                )
            ), shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = location,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SearchLocationField(
        query: String, onQueryChange: (String) -> Unit, onClearClick: () -> Unit
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(12.dp)),
            placeholder = { Text("Search for a location...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClearClick) {
                        Icon(
                            imageVector = Icons.Rounded.Clear,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
           /* colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = Color.White,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.LightGray
            )*/
            singleLine = true
        )
    }

    @Composable
    fun SearchResultsList(cities: List<Any>, onCitySelected: (String, String) -> Unit) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 2.dp
            ),
            colors = CardDefaults.elevatedCardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (cities.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No cities found. Try a different search term.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(cities) { city ->
                        // Assuming city class has name and country properties
                        val cityName = city::class.java.getMethod("getName").invoke(city) as String
                        val countryName =
                            city::class.java.getMethod("getCountry").invoke(city) as String

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCitySelected(cityName, countryName) }
                                .padding(horizontal = 16.dp, vertical = 12.dp)) {
                            Text(
                                text = cityName,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = countryName,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }

                        Divider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = Color.LightGray.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun WeatherForecastCard(location: String, forecasts: List<Any>) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 4.dp
            ),
            colors = CardDefaults.elevatedCardColors(
                containerColor = Color(0xFFE1F5FE)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Text(
                    text = "5-Day Weather Forecast",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D47A1)
                )

                Text(
                    text = location,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF1565C0),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Forecast items in a scrollable container
                if (forecasts.isEmpty()) {
                    Text(
                        text = "No forecast data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                    )
                } else {
                    // Set a fixed height for the forecasts container and make it scrollable
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(forecasts.size) { index ->
                            val forecast = forecasts[index]
                            // Method calls without knowing the forecast class structure

                            val forecastClass = forecast::class.java
                            val getDtTxt = forecastClass.getMethod("getDt_txt")
                            val getWeather = forecastClass.getMethod("getWeather")
                            val getPop = forecastClass.getMethod("getPop")
                            val getMain = forecastClass.getMethod("getMain")

                            val dtTxt = getDtTxt.invoke(forecast) as String
                            val weather = getWeather.invoke(forecast) as List<*>
                            val pop = getPop.invoke(forecast) as Double
                            val main = getMain.invoke(forecast)

                            val tempMaxMethod = main::class.java.getMethod("getTemp_max")
                            val tempMinMethod = main::class.java.getMethod("getTemp_min")
                            val tempMax = (tempMaxMethod.invoke(main) as Double).toInt()
                            val tempMin = (tempMinMethod.invoke(main) as Double).toInt()

                            // Weather icon from first weather item
                            val weatherItem = weather.firstOrNull()
                            val icon = if (weatherItem != null) {
                                val iconMethod = weatherItem::class.java.getMethod("getIcon")
                                iconMethod.invoke(weatherItem) as String
                            } else {
                                ""
                            }

                            // Format the date
                            val inputFormat =
                                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            val outputFormat = SimpleDateFormat("EEEE, ha", Locale.getDefault())
                            val parsedDate = inputFormat.parse(dtTxt)
                            val formattedDate = parsedDate?.let { outputFormat.format(it) } ?: dtTxt

                            WeatherForecastItem(
                                dayTime = formattedDate,
                                icon = icon,
                                tempMin = tempMin,
                                tempMax = tempMax,
                                rainProbability = pop,
                                isLastItem = index == forecasts.size - 1
                            )


                        }
                    }
                }
            }
        }
    }

    @Composable
    fun WeatherForecastItem(
        dayTime: String,
        icon: String,
        tempMin: Int,
        tempMax: Int,
        rainProbability: Double,
        isLastItem: Boolean
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Day and time
            Text(
                text = dayTime,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF424242),
                modifier = Modifier.weight(1.5f)
            )

            // Rain probability
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(0.8f)
            ) {
                // Rain icon based on probability
                val iconId = when {
                    rainProbability > 0.7 -> R.drawable.drop
                    rainProbability > 0.3 -> R.drawable.halffilleddrop
                    else -> R.drawable.emptydrop
                }

                Icon(
                    painter = painterResource(id = iconId),
                    contentDescription = "Rain probability",
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF2196F3)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "${(rainProbability * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF424242)
                )
            }

            // Weather icon
            AsyncImage(
                model = if (icon.isNotEmpty()) "https://openweathermap.org/img/wn/$icon@2x.png" else R.drawable.weather_icon,
                contentDescription = "Weather Icon",
                modifier = Modifier
                    .size(28.dp)
                    .weight(0.7f),
                contentScale = ContentScale.Fit
            )

            // Temperature range
            Text(
                text = "$tempMin°/$tempMax°",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF212121),
                modifier = Modifier.weight(0.8f),
                textAlign = TextAlign.End
            )
        }

        if (!isLastItem) {
            Divider(
                color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp
            )
        }
    }


    /* @Composable
     fun EmptyLocationState() {
         Box(
             modifier = Modifier
                 .fillMaxWidth()
                 .padding(top = 32.dp),
             contentAlignment = Alignment.Center
         ) {
             Column(
                 horizontalAlignment = Alignment.CenterHorizontally,
                 verticalArrangement = Arrangement.Center
             ) {
                 Icon(
                     imageVector = Icons.Default.LocationOn,
                     contentDescription = "Location",
                     tint = Color.Gray,
                     modifier = Modifier.size(64.dp)
                 )

                 Spacer(modifier = Modifier.height(16.dp))

                 Text(
                     text = "No Location Selected",
                     style = MaterialTheme.typography.titleLarge,
                     color = Color.Gray,
                     fontWeight = FontWeight.Medium
                 )

                 Spacer(modifier = Modifier.height(8.dp))

                 Text(
                     text = "Search for a location to see weather forecasts",
                     style = MaterialTheme.typography.bodyLarge,
                     color = Color.Gray,
                     textAlign = TextAlign.Center,
                     modifier = Modifier.padding(horizontal = 32.dp)
                 )
             }
         }
     }*/

    /* @Composable
     fun LoadingIndicator() {
         Box(
             modifier = Modifier
                 .fillMaxSize()
                 .background(Color.Black.copy(alpha = 0.4f)),
             contentAlignment = Alignment.Center
         ) {
             Card(
                 modifier = Modifier.size(80.dp),
                 shape = RoundedCornerShape(16.dp),
                 colors = CardDefaults.cardColors(
                     containerColor = Color.White
                 )
             ) {
                 Box(
                     modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                 ) {
                     CircularProgressIndicator(
                         color = MaterialTheme.colorScheme.primary,
                         modifier = Modifier.size(40.dp),
                         strokeWidth = 3.dp
                     )
                 }
             }
         }
     }*/

    @Composable
    fun LocationChangeConfirmationDialog(
        cityName: String, countryName: String, onConfirm: () -> Unit, onDismiss: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss, title = {
                Text(
                    text = "Change Location", fontWeight = FontWeight.Bold
                )
            }, text = {
                Column {
                    Text(
                        text = "Are you sure you want to change your location to:", fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "$cityName, $countryName",
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }, confirmButton = {
                Button(
                    onClick = onConfirm, colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Confirm")
                }
            }, dismissButton = {
                Button(
                    onClick = onDismiss, colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray
                    )
                ) {
                    Text("Cancel")
                }
            }, containerColor = Color.White, shape = RoundedCornerShape(16.dp)
        )
    }


    /**
     * A shimmer effect that can be applied to any component to create a loading animation.
     * @param content The content to apply the shimmer effect to.
     */
    @Composable
    fun ShimmerEffect(content: @Composable () -> Unit) {
        // Create infinite transition for shimmer effect
        val transition = rememberInfiniteTransition(label = "shimmer")
        val translateAnim = transition.animateFloat(
            initialValue = 0f, targetValue = 1000f, animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1000, easing = FastOutSlowInEasing
                ), repeatMode = RepeatMode.Restart
            ), label = "shimmer"
        )

        // Create gradient brush for shimmer effect
        val shimmerColors = listOf(
            Color.LightGray.copy(alpha = 0.5f),
            Color.LightGray.copy(alpha = 0.3f),
            Color.LightGray.copy(alpha = 0.5f)
        )

        val brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnim.value, y = translateAnim.value)
        )

        // Wrap content in Box with shimmer background
        Box(
            modifier = Modifier
                .background(brush)
                .fillMaxSize()
        ) {
            content()
        }
    }

    /**
     * A shimmer location header that matches the design of the real location header.
     */
    @Composable
    fun ShimmerLocationHeader() {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 4.dp
            ), colors = CardDefaults.elevatedCardColors(
                containerColor = Color.White
            ), shape = RoundedCornerShape(12.dp)
        ) {
            ShimmerEffect {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Location icon placeholder
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color.Transparent)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Location text placeholder
                    Box(
                        modifier = Modifier
                            .height(20.dp)
                            .width(200.dp)
                            .background(Color.Transparent)
                    )
                }
            }
        }
    }

    /**
     * A shimmer search field that matches the design of the real search field.
     */
    @Composable
    fun ShimmerSearchField() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            ShimmerEffect {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent)
                )
            }
        }
    }

    /**
     * A shimmer weather forecast card that matches the design of the real weather forecast card.
     */
    @Composable
    fun ShimmerWeatherForecastCard() {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 4.dp
            ), colors = CardDefaults.elevatedCardColors(
                containerColor = Color.White
            ), shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                ShimmerEffect {
                    // Title placeholder
                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .width(200.dp)
                            .background(Color.Transparent)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Subtitle placeholder
                    Box(
                        modifier = Modifier
                            .height(20.dp)
                            .width(150.dp)
                            .background(Color.Transparent)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Forecast items placeholder
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                    ) {
                        items(8) {
                            ShimmerForecastItem()
                        }
                    }
                }
            }
        }
    }

    /**
     * A shimmer forecast item that matches the design of the real forecast item.
     */
    @Composable
    fun ShimmerForecastItem() {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Date and time placeholder
                Box(
                    modifier = Modifier
                        .height(16.dp)
                        .width(120.dp)
                        .background(Color.Transparent)
                        .weight(1.5f)
                )

                // Rain probability placeholder
                Row(
                    verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(0.8f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color.Transparent)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Box(
                        modifier = Modifier
                            .height(16.dp)
                            .width(30.dp)
                            .background(Color.Transparent)
                    )
                }

                // Weather icon placeholder
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                        .weight(0.7f)
                )

                // Temperature placeholder
                Box(
                    modifier = Modifier
                        .height(16.dp)
                        .width(60.dp)
                        .background(Color.Transparent)
                        .weight(0.8f)
                )
            }

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.Transparent)
            )
        }
    }

    /**
     * A full shimmer screen that combines all shimmer components to create a complete loading UI.
     */
    @Composable
    fun ShimmerLocationScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ShimmerLocationHeader()
            ShimmerSearchField()
            ShimmerWeatherForecastCard()
        }
    }
}