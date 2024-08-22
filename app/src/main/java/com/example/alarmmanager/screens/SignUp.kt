import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.material.Text
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
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.alarmmanager.viewmodels.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

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

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data: Intent? = result.data
        if (result.resultCode == RESULT_OK && data != null) {
            viewModel.handleGoogleSignInResult(data, context, onSuccess = {
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(intent)
            }, onError = {
                isLoading.value = false
                Toast.makeText(
                    context,
                    "Google authentication Failed Enter a valid email and ",
                    Toast.LENGTH_LONG
                ).show()
            })
        } else {
            isLoading.value = false
            Toast.makeText(context, "Google Sign-In failed. Please try again.", Toast.LENGTH_LONG)
                .show()
        }
    }


    val textColor = TextFieldDefaults.outlinedTextFieldColors(
        unfocusedBorderColor = colorResource(id = R.color.light_pink),
        cursorColor = colorResource(id = R.color.button_color)
    )
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .background(color = colorResource(id = R.color.custom_white))
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SignIn",
            fontSize = 30.sp,
            color = colorResource(id = R.color.black),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(64.dp))
        OutlinedTextField(
            textStyle = TextStyle(color = Color.DarkGray, fontSize = 18.sp),
            shape = RoundedCornerShape(16.dp),
            colors = textColor,
            value = email,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Email
            ),
            onValueChange = {
                email = it
                mailerror = if (it.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(it)
                        .matches()
                ) "Enter a valid Email address." else null
            },
            label = { Text("username") }, modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email, contentDescription = null
                )
            },
        )
        mailerror?.let {
            Text(text = it, fontSize = 12.sp, color = Color.Red)
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
            label = { Text("password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock, contentDescription = null
                )
            })
        passworderror?.let {
            Text(text = it, fontSize = 12.sp, color = Color.Red)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text(
                text = "Forgot Password.",
                color = Color.DarkGray,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .clickable {
                        try {
                            navController.navigate("ResetPassword")
                            Log.d(
                                "Navigation",
                                "Current route: ${navController.currentBackStackEntry?.destination?.route}"
                            )

                        } catch (E: Exception) {
                            E.printStackTrace()
                        }
                    },
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.button_color)),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
            onClick = {
                if (isNetworkAvailable(context)){
                    if (password.isNotEmpty() && email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(
                            email
                        ).matches()
                    ) {
                        isLoading.value = true
                        viewModel.signin(email, password, onSuccess = {
                            val intent1 = Intent(context, MainActivity::class.java).apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            context.startActivity(intent1)
                            Toast.makeText(
                                context,
                                "Welcome ${
                                    if (FirebaseAuth.getInstance().currentUser?.displayName !== null) FirebaseAuth.getInstance().currentUser?.displayName
                                    else FirebaseAuth.getInstance().currentUser?.email?.substringBefore(
                                        "@"
                                    )
                                }",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }, onError = {
                            isLoading.value = false
                            Toast.makeText(
                                context,
                                "Authentication failed Enter a valid email and password.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }, context, navController)
                    } else {
                        Toast.makeText(
                            context,
                            "Please make sure all fields are correctly filled.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@Button
                    }
                }else {
                    Toast.makeText(context,"Please connect to a network and try again",Toast.LENGTH_LONG).show()
                }


            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                text = "SignIn",
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
        Button(
            onClick = {
                if (isNetworkAvailable(context)){
                    isLoading.value = true
                    viewModel.googleSignIn(
                        context,
                        googleSignInLauncher,
                        intent,
                        onSuccess = {
                            val intentn = Intent(context, MainActivity::class.java).apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            context.startActivity(intentn)
                        },
                        onError = {
                            Toast.makeText(
                                context,
                                "Sign in failed please try again",
                                Toast.LENGTH_LONG
                            ).show()
                        })
                } else {
                    Toast.makeText(context,"Please connect to a network and try again",Toast.LENGTH_LONG).show()
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
                Text(
                    text = "SignIn with Google",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge
                )
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
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetworkInfo
    return activeNetwork?.isConnectedOrConnecting == true
}