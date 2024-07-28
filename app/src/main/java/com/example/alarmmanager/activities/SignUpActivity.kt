package com.example.alarmmanager.activities

import SignUp
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.example.alarmmanager.repositories.AuthRepository
import com.example.alarmmanager.viewmodels.AuthViewModel


class SignUpActivity : ComponentActivity() {
    private lateinit var viewModel: AuthViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(AuthViewModel::class.java)


        setContent {
            SignUp(
                AuthViewModel(AuthRepository()),
                navController = NavController(this)
            )
        }
    }
}

