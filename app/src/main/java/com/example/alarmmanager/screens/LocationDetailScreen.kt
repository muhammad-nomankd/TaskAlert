package com.example.alarmmanager.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alarmmanager.screens.ui.theme.AlarmManagerTheme
import com.example.alarmmanager.viewmodels.LocationViewModel

class LocationDetailScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlarmManagerTheme {
                LocationDetailScreenContent()
            }

        }
    }

    @Composable
    fun LocationDetailScreenContent(){
        val apiKey = "e6844bc411msh69a178d35f2fabbp1e01fbjsnc9a755db3e73"

        Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top) {
            Spacer(modifier = Modifier.height(48.dp))
            Text(text = "Location Detail Screen", color = Color.DarkGray, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(32.dp))
            Text(text = "Selected Location")
            Spacer(modifier = Modifier.height(16.dp))
            CityDropdownMenu(apiKey = apiKey )
        }

    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CityDropdownMenu(apiKey: String, viewModel: LocationViewModel = viewModel()) {
        var cityInput by remember { mutableStateOf("") }
        var expanded by remember { mutableStateOf(false) }
        val context = LocalContext.current



        Column {

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }) {

                TextField(
                    value = cityInput,
                    onValueChange = {
                        cityInput = it
                        if (cityInput.isNotEmpty()) {
                            viewModel.fetchCities(apiKey, cityInput)
                            expanded = true


                        }

                    }, readOnly = false,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expanded, onDismissRequest = { expanded = false },
                ) {
                    viewModel.cities.forEach {
                        DropdownMenuItem(
                            text = { androidx.wear.compose.material.Text(text = "${it.name}, ${it.country}", color = Color.Gray) },
                            onClick = {
                                val locationId = "12345"
                                cityInput = it.name
                                expanded = false
                                viewModel.saveUserLocation(it.name,locationId,it.country, context = context)
                            })
                    }
                }
            }


        }
    }
}
