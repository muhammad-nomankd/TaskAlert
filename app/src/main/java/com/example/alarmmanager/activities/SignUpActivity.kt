package com.example.alarmmanager.activities

import SignUp
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.example.alarmmanager.repositories.AuthRepository
import com.example.alarmmanager.viewmodels.AuthViewModel
import com.example.alarmmanager.viewmodels.SignInViewModelFactory


class SignUpActivity : ComponentActivity() {
    private lateinit var viewModel: AuthViewModel
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = AuthRepository()

        viewModel = ViewModelProvider(
            this, SignInViewModelFactory(repository)
        ).get(AuthViewModel::class.java)
        viewModel.initGoogleSignIn(this)

        val googleSignInLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                viewModel.handleGoogleSignInResult(result.data, this)

            }
        viewModel = ViewModelProvider(this).get(AuthViewModel::class.java)


        setContent {
            SignUp(
                AuthViewModel(AuthRepository()),
                navController = NavController(this),
                googleSignInLauncher
            )
        }
    }
}

