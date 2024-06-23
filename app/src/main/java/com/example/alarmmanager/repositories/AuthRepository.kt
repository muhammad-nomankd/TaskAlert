package com.example.alarmmanager.repositories

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.navigation.NavController
import com.example.alarmmanager.R
import com.example.alarmmanager.activities.MainActivity
import com.example.alarmmanager.dataclasses.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository() {
    private val auth = FirebaseAuth.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient
    fun SignIn(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        context: Context,
        navController: NavController
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
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
                                Toast.makeText(
                                    context,
                                    "You are successfully registered.",
                                    Toast.LENGTH_LONG
                                ).show()
                                onSuccess()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Something went wrong please try again.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                        }.addOnFailureListener { e ->
                            onError(e.message ?: "Registration failed")
                        }
                }

            }.addOnFailureListener {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            onSuccess()
                            Toast.makeText(
                                context,
                                "Welcome back ${if (FirebaseAuth.getInstance().currentUser?.displayName !== null) FirebaseAuth.getInstance().currentUser?.displayName else FirebaseAuth.getInstance().currentUser?.email}.",
                                Toast.LENGTH_SHORT
                            )
                                .show()

                        } else {
                            Toast.makeText(
                                context,
                                "Authentication failed Enter a valid email and password.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Sign-in failed")
            }

    }

    fun ResetPasswordRepository(email: String, context: Context, navController: NavController) {
        if (email.isEmpty()) {
            return
        }
        val auth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(email, "random password")
            .addOnCompleteListener { task1 ->
                if (!task1.isSuccessful) {
                    if (email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email)
                            .matches()
                    ) {
                        auth.sendPasswordResetEmail(email)
                            .addOnCompleteListener { task2 ->
                                if (task2.isSuccessful) {
                                    Toast.makeText(
                                        context,
                                        "Password Reset Email sent.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    navController.navigate("SignIn")

                                } else {
                                    Toast.makeText(
                                        context,
                                        "Error sending Password reset email.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Invalid Email address", Toast.LENGTH_LONG)
                            .show()
                    }

                } else {
                    val user: FirebaseUser? = auth.currentUser
                    user?.delete()
                        ?.addOnCompleteListener { it2 ->
                            if (it2.isSuccessful) {
                                println("User deleted successfully")
                            } else {
                                println("User deletion failed")
                            }
                        }
                    Toast.makeText(
                        context,
                        "The email provided doesn't seems to be associated with any of your account.",
                        Toast.LENGTH_LONG
                    ).show()
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

    fun handleGoogleSignInResult(data: Intent?, context: Context) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account, context)
        } catch (e: ApiException) {
            Toast.makeText(context, "Google Sign in failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun firebaseAuthWithGoogle(
        account: GoogleSignInAccount,
        context: Context
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
                                Toast.makeText(
                                    context,
                                    "Welcome ${if (FirebaseAuth.getInstance().currentUser?.displayName !== null) FirebaseAuth.getInstance().currentUser?.displayName else FirebaseAuth.getInstance().currentUser?.email} You are successfully signed in with Google.",
                                    Toast.LENGTH_LONG
                                ).show()
                                val intent = Intent(context, MainActivity::class.java).apply {
                                    flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(
                                    context,
                                    "Something went wrong, please try again.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(context, "Authentication Failed.", Toast.LENGTH_LONG).show()
                }
            }
    }
}





