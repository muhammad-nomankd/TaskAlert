package com.durrani.taskalert.presentation.screens

import AuthRepositoryImpl
import AuthViewModel
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.durrani.taskalert.MainActivity
import com.durrani.taskalert.R
import com.durrani.taskalert.presentation.state.AuthState
import com.durrani.taskalert.presentation.ui.theme.AlarmManagerTheme
import com.durrani.taskalert.presentation.viewmodels.AuthViewModelFactory
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class AuthenticationActivity : ComponentActivity() {

    // ─────────────────────────────────────────────
    // ViewModel created properly via factory
    // ─────────────────────────────────────────────
    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(
            AuthRepositoryImpl(
                context = applicationContext,
                auth = FirebaseAuth.getInstance(),
                firestore = FirebaseFirestore.getInstance()
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlarmManagerTheme {
                val isSystemInDarkTheme = isSystemInDarkTheme()
                val systemUiController = rememberSystemUiController()
                systemUiController.setSystemBarsColor(
                    color = Color.Transparent, darkIcons = !isSystemInDarkTheme
                )
                SignInScreen(viewModel = viewModel)
            }
        }
    }

    // ─────────────────────────────────────────────
    // UI
    // ─────────────────────────────────────────────

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun SignInScreen(viewModel: AuthViewModel) {

        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val snackbarHost = remember { SnackbarHostState() }
        val scrollState = rememberScrollState()

        // Collect state from ViewModel — single source of truth
        val authState by viewModel.authState.collectAsState()

        var email by rememberSaveable { mutableStateOf("") }
        var password by rememberSaveable { mutableStateOf("") }
        var emailError by rememberSaveable { mutableStateOf<String?>(null) }
        var passwordError by rememberSaveable { mutableStateOf<String?>(null) }

        val isLoading = authState is AuthState.Loading

        // ── Google Sign-In Launcher ───────────────────────────────────────
        // This is the ONLY place handleGoogleSignInResult is called.
        // It fires after the user picks (or dismisses) the Google account picker.

        // ── React to googleSignInIntent ───────────────────────────────────
        // When the ViewModel emits an intent, launch it once and mark it consumed.
        // LaunchedEffect with the intent as key means it only runs when the value changes.

        // ── React to AuthState ────────────────────────────────────────────
        // Navigate or show snackbar depending on what came back from the ViewModel.
        LaunchedEffect(authState) {
            when (val state = authState) {
                is AuthState.Success -> {
                    snackbarHost.showSnackbar(
                        message = state.message,
                        actionLabel = "OK",
                        duration = SnackbarDuration.Short
                    )
                    // Navigate and clear back stack so user can't go back to login
                    context.startActivity(
                        Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                    viewModel.resetState()
                }

                is AuthState.Error -> {
                    snackbarHost.showSnackbar(
                        message = state.message,
                        actionLabel = "OK",
                        duration = SnackbarDuration.Long
                    )
                    viewModel.resetState()
                }

                else -> Unit // Idle and Loading handled by isLoading flag
            }
        }

        // ── Scaffold ──────────────────────────────────────────────────────
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHost) },
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                // Main content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = colorResource(id = R.color.custom_white))
                        .verticalScroll(scrollState)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    // ── Title ─────────────────────────────────────────────
                    Text(
                        text = "Sign In",
                        fontSize = 20.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(64.dp))

                    // ── Email Field ───────────────────────────────────────
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError =
                                if (Patterns.EMAIL_ADDRESS.matcher(it).matches()) null else ""
                        },
                        label = { Text("Email", color = Color.Black) },
                        textStyle = TextStyle(color = Color.DarkGray, fontSize = 18.sp),
                        shape = RoundedCornerShape(18.dp),
                        singleLine = true,
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    emailError?.let {
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // ── Password Field ────────────────────────────────────
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = if (it.isEmpty()) "Enter password." else null
                        },
                        label = { Text("Password", color = Color.Black) },
                        textStyle = TextStyle(color = Color.DarkGray, fontSize = 18.sp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isLoading,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    passwordError?.let {
                        Text(
                            text = it, fontSize = 12.sp, color = Color.Red
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Forgot Password ───────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Forgot Password?",
                            color = Color.DarkGray,
                            textAlign = TextAlign.End,
                            fontSize = 12.sp,
                            modifier = Modifier.clickable(enabled = !isLoading) {
                                // TODO: navigate to reset password screen
                            })
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Login / SignUp Button ─────────────────────────────
                    Button(
                        enabled = !isLoading,
                        onClick = {
                            // Validate before hitting the network
                            if (!validateInputs(
                                    email,
                                    password,
                                    onEmailError = { emailError = it },
                                    onPasswordError = { passwordError = it })
                            ) return@Button

                            if (!isNetworkAvailable(context)) {
                                coroutineScope.launch {
                                    snackbarHost.showSnackbar(
                                        "No internet connection. Please check your network.",
                                        "OK",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                                return@Button
                            }

                            viewModel.signInWithEmail(email, password)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.button_color)),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                    ) {
                        Text(
                            text = "Login or Sign Up",
                            fontSize = 18.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Or continue with",
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Google Sign-In Button ─────────────────────────────
                    Button(
                        enabled = !isLoading,
                        onClick = {
                            if (!isNetworkAvailable(context)) {
                                coroutineScope.launch {
                                    snackbarHost.showSnackbar("No internet connection.", "OK")
                                }
                                return@Button
                            }
                            // Pass context directly — CredentialManager shows the bottom sheet
                            viewModel.signInWithGoogle(context)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.gsi_bckg)),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxWidth()
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.google),
                                contentDescription = "Google Sign-In",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sign In with Google",
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                // ── Loading Overlay ───────────────────────────────────────
                // Sits on top of everything — blocks interaction while loading
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp, color = colorResource(id = R.color.button_color)
                        )
                    }
                }
            }
        }
    }

    // ─────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────

    /**
     * Returns true if inputs are valid, false otherwise.
     * Calls the lambdas to set error messages in the UI state.
     */
    private fun validateInputs(
        email: String,
        password: String,
        onEmailError: (String) -> Unit,
        onPasswordError: (String) -> Unit
    ): Boolean {
        var valid = true
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            onEmailError("Enter a valid email address")
            valid = false
        }
        if (password.length < 6) {
            onPasswordError("Password must be at least 6 characters")
            valid = false
        }
        return valid
    }

    /**
     * Checks active network connectivity.
     * Uses NetworkCapabilities (the modern API) instead of the deprecated activeNetworkInfo.
     */
    private fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}