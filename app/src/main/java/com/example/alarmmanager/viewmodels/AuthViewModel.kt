package com.example.alarmmanager.viewmodels

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.alarmmanager.repositories.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    fun signin(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: () -> Unit,
        context: Context,
        navController: NavController
    ) {
        repository.SignIn(
            email,
            password,
            onSuccess,
            onError,
            context,
            navController = navController
        )
    }

    fun googleSignIn(
        context: Context,
        activityResultLauncher: ActivityResultLauncher<Intent>,
        data: Intent?,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        repository.initGoogleSignIn(context)
        repository.launchGoogleSignIn(activityResultLauncher)
        repository.handleGoogleSignInResult(data, context, onSuccess, onError)

    }

    fun handleGoogleSignInResult(data: Intent?, context: Context, onSuccess: () -> Unit, onError: () -> Unit) {
        repository.handleGoogleSignInResult(data, context,onSuccess,onError)
    }


}



