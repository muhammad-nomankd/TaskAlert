package com.durrani.taskalert

import AuthRepositoryImpl
import AuthViewModel
import CreateTaskViewModel
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.durrani.taskalert.presentation.screens.AuthenticationActivity
import com.durrani.taskalert.presentation.ui.screens.CreateTask
import com.durrani.taskalert.presentation.screens.HomeScreen
import com.durrani.taskalert.presentation.ui.screens.WeatherDetailScreen
import com.durrani.taskalert.presentation.ui.screens.PasswordResetScreen
import com.durrani.taskalert.presentation.ui.screens.ProfileScreen
import com.durrani.taskalert.presentation.ui.screens.TaskListScreen
import com.durrani.taskalert.presentation.viewmodels.LocationViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val context = LocalContext.current
            val auth = FirebaseAuth.getInstance()
            val firestore = FirebaseFirestore.getInstance()

            NavHost(navController = navController, startDestination = "home") {

                composable("home") {
                    HomeScreen().HomeScreenUi(navController, this@MainActivity)
                }

                composable("ResetPassword") {
                    PasswordResetScreen(context = LocalContext.current, navController)
                }
                composable("taskListScreen") {
                    TaskListScreen().TaskListScreen(navController)
                }

                composable("SignIn") {
                    AuthenticationActivity().SignInScreen(
                       viewModel = AuthViewModel(
                           repository = AuthRepositoryImpl(
                               context = context,
                               auth = auth,
                               firestore =  firestore
                           )
                       )
                    )
                }

                composable("locationDetailScreen") {
                    WeatherDetailScreen().LocationDetailContent(
                        LocationViewModel(), navController
                    )
                }

                composable("signup") {
                    AuthenticationActivity().SignInScreen(
                        viewModel = AuthViewModel(
                            repository = AuthRepositoryImpl(
                                context = context,
                                auth = auth,
                                firestore =  firestore
                            )
                        )
                    )
                }

                composable("profile") {
                    ProfileScreen().ProfileContent(navController)
                }

                composable(
                    "createTask?taskId={taskId}&taskTitle={taskTitle}&taskDescription={taskDescription}&startDate={startDate}&endDate={endDate}&startTime={startTime}&endTime={endTime}&priority={priority}",
                    arguments = listOf(
                        navArgument("taskId") { nullable = true },
                        navArgument("taskTitle") {
                            nullable = true
                            NavType.Companion.StringType
                        },
                        navArgument("taskDescription") {
                            nullable = true
                            NavType.Companion.StringType
                        },
                        navArgument("startDate") {
                            nullable = true
                            NavType.Companion.StringType
                        },
                        navArgument("endDate") {
                            nullable = true
                            NavType.Companion.StringType
                        },
                        navArgument("startTime") {
                            nullable = true
                            NavType.Companion.StringType
                        },
                        navArgument("endTime") {
                            nullable = true
                            NavType.Companion.StringType
                        },
                        navArgument("priority") {
                            nullable = true
                            NavType.Companion.StringType
                        })
                ) { backStackEntry ->
                    val taskId = backStackEntry.arguments?.getString("taskId")
                    val taskTitle = backStackEntry.arguments?.getString("taskTitle")
                    val taskDescription = backStackEntry.arguments?.getString("taskDescription")
                    val startDate = backStackEntry.arguments?.getString("startDate")
                    val endDate = backStackEntry.arguments?.getString("endDate")
                    val startTime = backStackEntry.arguments?.getString("startTime")
                    val endTime = backStackEntry.arguments?.getString("endTime")
                    val priority = backStackEntry.arguments?.getString("priority")

                    CreateTask(
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