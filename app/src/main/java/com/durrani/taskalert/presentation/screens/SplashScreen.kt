package com.durrani.taskalert.presentation.ui.screens

import AuthRepositoryImpl
import AuthViewModel
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.durrani.taskalert.MainActivity
import com.durrani.taskalert.R
import com.durrani.taskalert.presentation.screens.AuthenticationActivity
import com.durrani.taskalert.presentation.screens.HomeScreen
import com.durrani.taskalert.presentation.ui.theme.AlarmManagerTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
                val auth = FirebaseAuth.getInstance()
                val firestore = FirebaseFirestore.getInstance()
                val context = LocalContext.current      
                val authViewModel = AuthViewModel(
                    repository = AuthRepositoryImpl(context,auth,firestore)
                )
                NavHost(
                    navController = navController, startDestination = startDestination
                ) {
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
                    composable("splash") {
                        SplashScreenComposable(navController = navController)
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
    fun SplashScreenComposable(navController: NavHostController) {
        val coroutineScope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            coroutineScope.launch {
                delay(1000)
                if (FirebaseAuth.getInstance().currentUser?.uid != null) {
                    val intent = Intent(
                        this@SplashScreen,
                        MainActivity::class.java
                    ).apply {
                        flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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
            modifier = Modifier.Companion
                .fillMaxSize()
                .background(
                    brush = Brush.Companion.linearGradient(
                        colors = listOf(
                            Color(0xFFB2EBF2),
                            Color(0xFF9575CD)
                        )
                    )
                )

        ) {
            Column(
                modifier = Modifier.Companion.align(Alignment.Companion.Center),
                horizontalAlignment = Alignment.Companion.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.appicon),
                    contentDescription = "App logo",
                    modifier = Modifier.Companion
                        .size(150.dp)
                )

            }
            Text(
                text = "Stay on time, stay organized.",
                fontSize = 16.sp,
                color = Color(0x80FFFFFF),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.Companion
                    .align(Alignment.Companion.BottomCenter)
                    .padding(bottom = 32.dp)
            )

        }
    }


}