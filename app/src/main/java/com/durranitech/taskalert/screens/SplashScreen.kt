package com.durranitech.taskalert.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.durranitech.taskalert.R
import com.durranitech.taskalert.activities.MainActivity
import com.durranitech.taskalert.repositories.AuthRepository
import com.durranitech.taskalert.ui.theme.AlarmManagerTheme
import com.durranitech.taskalert.viewmodels.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            AlarmManagerTheme {
                val navController = rememberNavController()
                val startDestination = "splash"
                NavHost(
                    navController = navController, startDestination = startDestination
                ) {
                    composable("SignIn") {
                        SignUpActivity().SignUp(viewModel = AuthViewModel(AuthRepository()), navController = navController)
                    }
                    composable("splash") {
                        authentication(navController = navController)
                    }
                    composable("ResetPassword") {
                        PasswordResetScreen(context = LocalContext.current, navController)
                    }
                    composable("HomeScreen") {
                        HomeScreen()
                    }
                    composable("MainActivity") {
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
                    val intent = Intent(
                        this@SplashScreen,
                        com.durranitech.taskalert.activities.MainActivity::class.java
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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
        Splash()
    }

    @Composable
    fun Splash() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFB2EBF2),
                            Color(0xFF9575CD)
                        )
                    )
                )

        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.applogo),
                    contentDescription = "App logo",
                    modifier = Modifier
                        .size(150.dp)
                )

            }
            Text(
                text = "Stay on time, stay organized.",
                fontSize = 16.sp,
                color = Color(0x80FFFFFF),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            )

        }
    }


}