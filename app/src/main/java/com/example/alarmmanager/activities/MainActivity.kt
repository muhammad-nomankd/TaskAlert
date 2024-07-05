package com.example.alarmmanager.activities

import CreateTaskViewModel
import SignUp
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.alarmmanager.repositories.AuthRepository
import com.example.alarmmanager.screens.CreatTask
import com.example.alarmmanager.screens.HomeScreen
import com.example.alarmmanager.screens.TaskListScreen
import com.example.alarmmanager.viewmodels.AuthViewModel

class MainActivity : ComponentActivity() {
    var isloading = mutableStateOf(false)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "home") {
                composable("home") { HomeScreen().HomeScreenUi(navController, this@MainActivity) }
                composable("signup") {
                    SignUp(
                        viewModel = AuthViewModel(AuthRepository()),
                        navController = navController
                    )
                }
                composable("createTask") {
                    CreatTask().createTaskcom(
                        navController,
                        CreateTaskViewModel()
                    )
                }
                composable("taskListScreen") { TaskListScreen().taskListScreen(navController) }
            }
        }
    }


}

