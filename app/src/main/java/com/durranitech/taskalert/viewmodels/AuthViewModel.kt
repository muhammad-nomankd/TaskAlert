package com.durranitech.taskalert.viewmodels

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.durranitech.taskalert.repositories.AuthRepository

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    fun signin(
        email: String,
        password: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
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
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        repository.initGoogleSignIn(context)
        repository.launchGoogleSignIn(activityResultLauncher)
        repository.handleGoogleSignInResult(data, onSuccess, onError)

    }

    fun handleGoogleSignInResult(
        data: Intent?,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
        context: Context
    ) {
        repository.handleGoogleSignInResult(data, onSuccess, onError)
    }


}



