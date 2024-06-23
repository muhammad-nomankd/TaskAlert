package com.example.alarmmanager.activities

import CreateTaskViewModel
import SignUp
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.alarmmanager.repositories.AuthRepository
import com.example.alarmmanager.screens.CreatTask
import com.example.alarmmanager.screens.HomeScreen
import com.example.alarmmanager.screens.TaskDetailScreen
import com.example.alarmmanager.ui.theme.AlarmManagerTheme
import com.example.alarmmanager.viewmodels.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    var isloading= mutableStateOf(false)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val googleSignInLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                AuthViewModel(AuthRepository()).handleGoogleSignInResult(result.data, this)

            }


        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "home") {
                composable("home") { HomeScreen().HomeScreenUi(navController, this@MainActivity) }
                composable("signup") { SignUp(
                    viewModel = AuthViewModel(AuthRepository()),
                    navController = navController,
                    googleSignInLauncher = googleSignInLauncher
                ) }
                composable("createTask") { CreatTask().createTaskcom(navController, CreateTaskViewModel()) }
                composable("taskListScreen"){ TaskDetailScreen().TaskList()}
            }
        }

    }
    @Composable
    fun logout(context: Context) {
        Button(onClick = { signout(context) }) {
            androidx.compose.material.Text(text = "logout")
        }

    }
    @Composable
    fun WelcomeMessege(context: Context) {
        isloading = rememberSaveable{ mutableStateOf(false) }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.padding(16.dp)) {
            androidx.compose.material.Text(text = " ")
            Spacer(modifier = Modifier.height(200.dp))
            androidx.compose.material.Text(
                text = "Welcome To MainActivity",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Blue
            )

             logout(context)

            if (isloading.value == true) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier
                        .height(32.dp)
                        .width(32.dp)
                        .align(alignment = Alignment.CenterHorizontally), color = Color.Gray
                )
            }
        }

    }
    val id = FirebaseAuth.getInstance().currentUser ?.uid
    val username = FirebaseAuth.getInstance().currentUser ?.displayName
    val usermail = FirebaseAuth.getInstance().currentUser ?.email
    fun signout(context: Context) {
        isloading.value = true
        FirebaseAuth.getInstance().signOut()
        if (FirebaseAuth.getInstance().currentUser?.uid == null) {
            val intent = Intent(context, SignUpActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
            Toast.makeText(context, "You are Successfully Signed Out ${if (username !== null) username else usermail}.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Unable to sign out please try again.", Toast.LENGTH_LONG).show()
        }

    }


}

