package com.example.alarmmanager.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.alarmmanager.R
import com.example.alarmmanager.screens.ui.theme.AlarmManagerTheme
import com.example.alarmmanager.viewmodels.LocationViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
                .padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go back",
                    modifier = Modifier
                        .clickable { navController.navigateUp() })
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Location Detail Screen",
                    color = Color.DarkGray,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(48.dp))
            Text(text = "Selected Location")
            Spacer(modifier = Modifier.height(16.dp))
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

        LaunchedEffect(Unit) {
            firestore.collection("User").document(FirebaseAuth.getInstance().currentUser?.uid ?: "")
                .collection("location")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        currentlySelectedLocation = document.getString("location") ?: ""
                    }
                    cityInput = currentlySelectedLocation
                }
        }
        Column() {
            val borderColor =

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }, modifier = Modifier
                        .background(
                            colorResource(id = R.color.custom_white),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()) {
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
                            Text(text = "Enter city name..")
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
                            focusedIndicatorColor =  Color(0xFFDADADA),
                            unfocusedIndicatorColor =  Color(0xFFDADADA)
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
                                    val locationId = "12345"
                                    cityInput = it.name
                                    expanded = false
                                    viewModel.saveUserLocation(
                                        it.name,
                                        locationId,
                                        it.country,
                                        context = context
                                    )
                                })
                        }
                    }
                }


        }
    }
}
