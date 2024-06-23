package com.example.alarmmanager.viewmodels

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.alarmmanager.activities.MainActivity
import com.example.alarmmanager.repositories.AuthRepository

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _signinresult = MutableLiveData<SignInResult>()
    val signinresult: LiveData<SignInResult> = _signinresult

    fun signin(
        email: String,
        password: String,
        context: Context,
        navController: NavController
    ) {
        repository.SignIn(
            email,
            password,
            {
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(intent)
            },
            { errorMessage ->
                _signinresult.value = SignInResult.Error(errorMessage)
            },
            context,
            navController = navController
        )
    }

    fun initGoogleSignIn(context: Context) {
        repository.initGoogleSignIn(context)
    }

    fun launchGoogleSignIn(activityResultLauncher: ActivityResultLauncher<Intent>){
        repository.launchGoogleSignIn(activityResultLauncher)
    }

    fun handleGoogleSignInResult(data: Intent?, context: Context) {
        repository.handleGoogleSignInResult(data, context)
    }


    }

    sealed class SignInResult {
        object Success : SignInResult()
        data class Error(val errorMessage: String) : SignInResult()
    }

