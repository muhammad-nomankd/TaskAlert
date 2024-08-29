package com.example.alarmmanager.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import androidx.navigation.NavController
import com.example.alarmmanager.R
import com.example.alarmmanager.activities.MainActivity
import com.example.alarmmanager.repositories.AuthRepository
import com.example.alarmmanager.screens.ui.theme.AlarmManagerTheme
import com.example.alarmmanager.viewmodels.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch


class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlarmManagerTheme {
                SignUp(
                    viewModel = AuthViewModel(AuthRepository()), navController = NavController(
                        LocalContext.current
                    )
                )
            }
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SignUp(
        viewModel: AuthViewModel,
        navController: NavController
    ) {
        var email by rememberSaveable { mutableStateOf("") }
        var password by rememberSaveable { mutableStateOf("") }
        var mailerror by rememberSaveable { mutableStateOf<String?>(null) }
        var passworderror by rememberSaveable { mutableStateOf<String?>(null) }
        val context = LocalContext.current
        val isLoading = rememberSaveable { mutableStateOf(false) }
        val scrollState = rememberScrollState()
        val intent = Intent()
        var snackBarHost by remember { mutableStateOf(SnackbarHostState()) }
        val coroutinesScope = rememberCoroutineScope()

        val googleSignInLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val data: Intent? = result.data
            if (result.resultCode == RESULT_OK && data != null) {
                viewModel.handleGoogleSignInResult(data, onSuccess = {
                    val intentn = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    context.startActivity(intentn)
                }, onError = { errorMessage ->
                    coroutinesScope.launch {
                        snackBarHost.showSnackbar(message = errorMessage, actionLabel = "Close")
                    }
                }, context)
            } else {
                isLoading.value = false
                coroutinesScope.launch {
                    snackBarHost.showSnackbar(
                        message = "Error occurred please try again",
                        actionLabel = "Close"
                    )
                }
            }
        }


        val textColor = TextFieldDefaults.outlinedTextFieldColors(
            unfocusedBorderColor = Color.LightGray,
            focusedBorderColor = colorResource(id = R.color.dark_gray),
            cursorColor = colorResource(id = R.color.dark_gray)
        )
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackBarHost) },
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = colorResource(id = R.color.custom_white))
                    .verticalScroll(scrollState)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                androidx.compose.material.Text(
                    text = "SignIn",
                    fontSize = 20.sp,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(64.dp))
                OutlinedTextField(
                    textStyle = TextStyle(color = Color.DarkGray, fontSize = 18.sp),
                    shape = RoundedCornerShape(18.dp),
                    colors = textColor,
                    value = email,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Email
                    ),
                    onValueChange = {
                        email = it
                        mailerror =
                            if (android.util.Patterns.EMAIL_ADDRESS.matcher(it)
                                    .matches()
                            ) null else ""
                    },
                    label = { androidx.compose.material.Text("username") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    },
                )
                mailerror?.let {
                    androidx.compose.material.Text(
                        text = it, fontSize = 12.sp, color = Color.Red, modifier = Modifier.align(
                            Alignment.End
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    shape = RoundedCornerShape(16.dp),
                    value = password,
                    colors = textColor,
                    textStyle = TextStyle(color = Color.DarkGray, fontSize = 18.sp),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Password
                    ),
                    onValueChange = {
                        password = it
                        passworderror = if (it.isEmpty()) "Enter Password." else null
                    },
                    label = { androidx.compose.material.Text("password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    })
                passworderror?.let {
                    androidx.compose.material.Text(text = it, fontSize = 12.sp, color = Color.Red)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    androidx.compose.material.Text(
                        text = "Forgot Password.",
                        color = Color.DarkGray,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .clickable {
                                try {
                                    navController.navigate("ResetPassword")
                                } catch (E: Exception) {
                                    E.printStackTrace()
                                }
                            },
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.button_color)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                    onClick = {
                        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email)
                                .matches()
                        ) {
                            mailerror = "Enter a valid email"
                            return@Button
                        }
                        if (isNetworkAvailable(context)) {
                            if (password.isNotEmpty() && email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(
                                    email
                                ).matches()
                            ) {
                                isLoading.value = true
                                viewModel.signin(email, password, onSuccess = { successMessege ->
                                    coroutinesScope.launch {
                                        snackBarHost.showSnackbar(
                                            message = successMessege,
                                            actionLabel = "Close",
                                            duration = SnackbarDuration.Short
                                        )
                                    }

                                    val intent1 = Intent(context, MainActivity::class.java).apply {
                                        flags =
                                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    }
                                    context.startActivity(intent1)
                                    coroutinesScope.launch {
                                        snackBarHost.showSnackbar(message = "Welcome ${
                                            if (FirebaseAuth.getInstance().currentUser?.displayName !== null) FirebaseAuth.getInstance().currentUser?.displayName
                                            else FirebaseAuth.getInstance().currentUser?.email?.substringBefore(
                                                "@"
                                            )
                                        }", actionLabel = "Close")
                                    }
                                }, onError = { signInError ->
                                    isLoading.value = false
                                    coroutinesScope.launch {
                                        snackBarHost.showSnackbar(
                                            message = signInError,
                                            actionLabel = "Close",
                                            duration = SnackbarDuration.Short
                                        )
                                    }

                                }, context, navController)
                            } else {
                                coroutinesScope.launch {
                                    snackBarHost.showSnackbar(
                                        message = "Make sure all fields are correctly filled",
                                        actionLabel = "Close",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                                return@Button
                            }
                        } else {
                            coroutinesScope.launch {
                                snackBarHost.showSnackbar(
                                    message = "Connect to a network and try again",
                                    actionLabel = "Close",
                                    duration = SnackbarDuration.Short
                                )
                            }

                        }


                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    androidx.compose.material.Text(
                        text = "SignIn",
                        fontSize = 18.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
                androidx.compose.material.Text(
                    text = "Or continue with",
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (isNetworkAvailable(context)) {
                            isLoading.value = true
                            viewModel.googleSignIn(
                                context,
                                googleSignInLauncher,
                                intent,
                                onSuccess = {
                                    isLoading.value = false
                                    val intentn = Intent(context, MainActivity::class.java).apply {
                                        flags =
                                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    }
                                    context.startActivity(intentn)
                                },
                                onError = { errorMessege ->
                                    isLoading.value = false
                                    coroutinesScope.launch {
                                        val snackBarResult =
                                            snackBarHost.showSnackbar(message = errorMessege)
                                        when (snackBarResult) {
                                            SnackbarResult.Dismissed -> {

                                            }

                                            SnackbarResult.ActionPerformed -> {

                                            }

                                        }
                                    }
                                })
                        } else {
                            coroutinesScope.launch {
                                snackBarHost.showSnackbar(
                                    message = "Connect to a network and try again",
                                    actionLabel = "Close",
                                    duration = SnackbarDuration.Short
                                )
                            }

                        }

                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.gsi_bckg)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
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
                        androidx.compose.material.Text(
                            text = "SignIn with Google",
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            if (isLoading.value) {
                CircularProgressIndicator(
                    strokeWidth = 1.dp,
                    modifier = Modifier
                        .align(alignment = Alignment.CenterHorizontally),
                    color = Color.Gray
                )
            }
        }


    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }
}
