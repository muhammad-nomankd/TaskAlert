package com.example.alarmmanager.repositories

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.navigation.NavController
import com.example.alarmmanager.R
import com.example.alarmmanager.dataclasses.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository() {
    private val auth = FirebaseAuth.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient
    fun SignIn(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: () -> Unit,
        context: Context,
        navController: NavController
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        context,
                        "You are successfully registered.",
                        Toast.LENGTH_LONG
                    ).show()
                    val user = User(
                        id = auth.currentUser?.uid ?: "",
                        email = auth.currentUser?.email ?: "",
                        name = auth.currentUser?.displayName ?: "",
                        imageUrl = ""
                    )
                    val firestore = FirebaseFirestore.getInstance().collection("User")
                    firestore.document(auth.currentUser?.uid!!).set(user)
                        .addOnCompleteListener { task2 ->
                            if (task2.isSuccessful) {
                                onSuccess()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Something went wrong please try again.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                        }.addOnFailureListener {
                            onError()
                        }
                }

            }.addOnFailureListener {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            onSuccess()
                        } else {
                            onError()
                        }
                    }
            }
    }



    fun initGoogleSignIn(context: Context) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    fun launchGoogleSignIn(activityResultLauncher: ActivityResultLauncher<Intent>) {
        googleSignInClient.signOut().addOnCompleteListener {
            val signInClient = googleSignInClient.signInIntent
            activityResultLauncher.launch(signInClient)
        }
    }

    fun handleGoogleSignInResult(data: Intent?, context: Context,onSuccess: () -> Unit, onError: () -> Unit,) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account, context,onSuccess,onError)
        } catch (e: ApiException) {
            Log.d("api exception", e.message.toString())
        }
    }
    fun firebaseAuthWithGoogle(
        account: GoogleSignInAccount,
        context: Context,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {
        val credentials = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credentials)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val user = User(
                        id = auth.currentUser?.uid ?: "",
                        email = account.email ?: "",
                        name = account.displayName ?: "",
                        imageUrl = account.photoUrl.toString()
                    )
                    val firestore = FirebaseFirestore.getInstance().collection("User")
                    firestore.document(auth.currentUser?.uid!!).set(user)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                onSuccess()
                            } else {
                                onError()
                            }
                        }
                } else {
                    Toast.makeText(context, "Authentication Failed.", Toast.LENGTH_LONG).show()
                }
            }
    }
}





