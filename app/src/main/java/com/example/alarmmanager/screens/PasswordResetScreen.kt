package com.example.alarmmanager.screens

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.alarmmanager.R
import com.example.alarmmanager.repositories.ResetPasswordRepository
import com.google.firebase.firestore.FirebaseFirestore

@RequiresApi(Build.VERSION_CODES.FROYO)
@Composable
fun PasswordResetScreen(context: Context, navController: NavController) {
    var email by rememberSaveable { mutableStateOf("") }
    val isloading = rememberSaveable {
        mutableStateOf(false)
    }
    val scrollstate = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.custom_white))
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize()
                .verticalScroll(scrollstate),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Reset Password",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Enter the email address associated\n with your account.",
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp, start = 16.dp),
                    label = { Text("username") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Email, contentDescription = null)
                    },
                    shape = RoundedCornerShape(16.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email)
                            .matches() && !checkUser(email)
                    ) {
                        isloading.value = true
                        ResetPasswordRepository(email, context, navController, onSuccess = {
                            email = ""
                            isloading.value = false
                            Toast.makeText(
                                context,
                                "Password Reset Email sent.",
                                Toast.LENGTH_LONG
                            ).show()
                            navController.navigate("SignIn")
                        }, onFailure = {
                            isloading.value = false
                            Toast.makeText(
                                context,
                                "Error sending Password reset email.",
                                Toast.LENGTH_LONG
                            ).show()
                        })
                    } else {
                        Toast.makeText(context, "Enter a valid email address.", Toast.LENGTH_LONG)
                            .show()
                    }
                }, shape = RoundedCornerShape(16.dp), modifier = Modifier
                    .height(55.dp)
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp), colors = ButtonDefaults.buttonColors(
                    colorResource(id = R.color.button_color)
                )
            ) {
                Text(text = "Recover Password", color = Color.White, fontSize = 18.sp)

            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(text = "Go to SignIn",
                color = Color.DarkGray,
                modifier = Modifier.clickable {
                    navController.navigate("SignIn") {
                        popUpTo("SignIn") {
                            inclusive = true
                        }
                    }
                })

        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (isloading.value) {
                CircularProgressIndicator(
                    strokeWidth = 1.dp,
                    modifier = Modifier
                        .align(alignment = Alignment.CenterHorizontally),
                    color = Color.Gray
                )
            }
        }

    }

}


fun checkUser(email: String): Boolean {
    var value = false
    FirebaseFirestore.getInstance().collection("User").whereEqualTo("email", email).get()
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val documents = task.result?.documents
                if (documents.isNullOrEmpty()) {
                    value = false
                } else {
                    value = true
                }
            } else {
                print("something went wrong")
            }

        }
    return value
}