package com.durranitech.taskalert.activities

import CreateTaskViewModel
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.durranitech.taskalert.repositories.AuthRepository
import com.durranitech.taskalert.screens.CreatTask
import com.durranitech.taskalert.screens.HomeScreen
import com.durranitech.taskalert.screens.LocationDetailScreen
import com.durranitech.taskalert.screens.PasswordResetScreen
import com.durranitech.taskalert.screens.ProfileScreen
import com.durranitech.taskalert.screens.SignUpActivity
import com.durranitech.taskalert.screens.TaskListScreen
import com.durranitech.taskalert.viewmodels.AuthViewModel
import com.durranitech.taskalert.viewmodels.LocationViewModel

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "home") {
                composable("home") { HomeScreen().HomeScreenUi(navController, this@MainActivity) }
                composable("ResetPassword") {
                    PasswordResetScreen(
                        context = LocalContext.current,
                        navController
                    )
                }
                composable("SignIn") {
                    SignUpActivity().SignUp(viewModel = AuthViewModel(AuthRepository()), navController = navController)
                }
                composable("locationDetailScreen") {
                    LocationDetailScreen().LocationDetailContent(
                        LocationViewModel(),navController
                    )
                }
                composable("signup") {
                    SignUpActivity().SignUp(
                        viewModel = AuthViewModel(AuthRepository()),
                        navController = navController
                    )
                }
                composable("profile") {
                    ProfileScreen().ProfileContent(navController)
                }
                composable(
                    "createTask?taskId={taskId}&taskTitle={taskTitle}&taskDescription={taskDescription}&startDate={startDate}&endDate={endDate}&startTime={startTime}&endTime={endTime}&priority={priority}",
                    arguments = listOf(
                        navArgument("taskId") { nullable = true },
                        navArgument("taskTitle") { nullable = true },
                        navArgument("taskDescription") { nullable = true },
                        navArgument("startDate") { nullable = true },
                        navArgument("endDate") { nullable = true },
                        navArgument("startTime") { nullable = true },
                        navArgument("endTime") { nullable = true },
                        navArgument("priority") { nullable = true }
                    )
                ) { backStackEntry ->
                    val taskId = backStackEntry.arguments?.getString("taskId")
                    val taskTitle = backStackEntry.arguments?.getString("taskTitle")
                    val taskDescription = backStackEntry.arguments?.getString("taskDescription")
                    val startDate = backStackEntry.arguments?.getString("startDate")
                    val endDate = backStackEntry.arguments?.getString("endDate")
                    val startTime = backStackEntry.arguments?.getString("startTime")
                    val endTime = backStackEntry.arguments?.getString("endTime")
                    val priority = backStackEntry.arguments?.getString("priority")

                    CreatTask().CreateTaskcom(
                        navController,
                        CreateTaskViewModel(),
                        taskId,
                        taskTitle,
                        taskDescription,
                        startDate,
                        endDate,
                        startTime,
                        endTime,
                        priority
                    )
                }
                composable("taskListScreen") { TaskListScreen().TaskListScreen(navController) }
            }
        }
    }


}

