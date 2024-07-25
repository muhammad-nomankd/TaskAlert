package com.example.alarmmanager.screens

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.wear.compose.material.Text
import coil.compose.rememberAsyncImagePainter
import com.example.alarmmanager.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ProfileScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProfileContent(navControler = NavController(LocalContext.current))
        }
    }

    @Composable
    fun ProfileContent(navControler: NavController) {

        var userName by rememberSaveable { mutableStateOf("") }
        var userEmail by rememberSaveable { mutableStateOf("") }
        var profileImageUrl by rememberSaveable { mutableStateOf("") }
        var context = LocalContext.current
        var firestore = Firebase.firestore
        var userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val coroutinescope = rememberCoroutineScope()
        var isLoading by rememberSaveable { mutableStateOf(false) }


        LaunchedEffect(Unit) {
                 isLoading = true
            firestore.collection("User").document(userId)
                .get().addOnSuccessListener { document ->
                    userName = document.getString("name") ?: ""
                    profileImageUrl = document.getString("imageUrl") ?: ""
                    userEmail = document.getString("email") ?: ""
                }.addOnSuccessListener {
                    isLoading = false
                }.addOnFailureListener {
                    Toast.makeText(context, "Failed to get user details", Toast.LENGTH_SHORT).show()
                    isLoading = false

                }
        }
        Column(
            verticalArrangement = Arrangement.Top, modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {


            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "back arrow",
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 32.dp, start = 18.dp)
                    .clickable { navControler.navigateUp() })
            Spacer(modifier = Modifier.height(32.dp))

            Image(
                painter = rememberAsyncImagePainter(model = profileImageUrl.ifEmpty { R.drawable.person }),
                contentDescription = "profile image",
                modifier = Modifier
                    .padding(start = 32.dp)
                    .shadow(0.5.dp, CircleShape)
                    .clip(CircleShape)
                    .size(75.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                userName, fontSize = 16.sp, color = Color.Black, modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 32.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                userEmail, fontSize = 16.sp, color = Color.Black, modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 32.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))


            Button(
                onClick = {
                    coroutinescope.launch {
                        isLoading = true
                        delay(1000)
                        FirebaseAuth.getInstance().signOut()
                        if (FirebaseAuth.getInstance().currentUser == null) {
                            navControler.navigate("signup")
                            isLoading = false
                        } else {
                            Toast.makeText(
                                context, "Sign-out failed. Please try again.", Toast.LENGTH_SHORT
                            ).show()
                                 isLoading = false
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.elevatedButtonElevation(8.dp),
                colors = ButtonDefaults.buttonColors(colorResource(id = R.color.button_color)),
                modifier = Modifier
                    .wrapContentWidth()
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Sign Out", fontSize = 16.sp
                )
            }
        }

        //ProgressBar
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier
                            .height(32.dp)
                            .width(32.dp)
                            .align(alignment = Alignment.CenterHorizontally),
                        color = Color.Gray
                    )
                }
            }
        }

    }
}
