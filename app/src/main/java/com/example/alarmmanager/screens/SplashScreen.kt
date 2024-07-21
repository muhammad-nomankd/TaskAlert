package com.example.alarmmanager.screens

import SignUp
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.alarmmanager.R
import com.example.alarmmanager.activities.MainActivity
import com.example.alarmmanager.repositories.AuthRepository
import com.example.alarmmanager.viewmodels.AuthViewModel
import com.example.alarmmanager.ui.theme.AlarmManagerTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         enableEdgeToEdge()
        setContent {
            AlarmManagerTheme {
                val navController = rememberNavController()
                val startDestination = "splash"
                NavHost(
                    navController = navController, startDestination = startDestination
                ) {
                    composable("SignIn") {
                        SignUp(
                            viewModel = AuthViewModel(AuthRepository()),
                            navController = navController
                        )
                    }
                    composable("splash") {
                        authentication(navController = navController)
                    }
                    composable("ResetPassword") {
                        PasswordResetScreen(context = LocalContext.current, navController)
                    }
                    composable("HomeScreen"){
                        HomeScreen()
                    }
                    composable("MainActivity"){
                       MainActivity()
                    }
                }
            }
        }
    }


    @Composable
    fun authentication(navController: NavHostController) {
        val coroutinescope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            coroutinescope.launch {
                delay(1000)
                if (FirebaseAuth.getInstance().currentUser?.uid != null) {
                    val intent= Intent(this@SplashScreen,
                        com.example.alarmmanager.activities.MainActivity::class.java).apply{
                        flags= Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                } else {
                    navController.navigate("SignIn") {
                        popUpTo("splash") {
                            inclusive = true
                        }
                    }
                }
            }
        }
        splash()
    }

    @Composable
    fun splash() {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.button_color)))
    }


}