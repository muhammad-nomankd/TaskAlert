package com.durranitech.taskalert.repositories
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.navigation.NavController
import com.durranitech.taskalert.R
import com.durranitech.taskalert.dataclasses.User
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firestore.v1.BeginTransactionRequest

class AuthRepository() {
    private val auth = FirebaseAuth.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient
    fun SignIn(
        email: String,
        password: String,
        onSuccess: (String) -> Unit,
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
                                onSuccess("You are successfully registered")
                            } else {
                                onError("Something went wrong please try again.")
                            }

                        }.addOnFailureListener {
                            onError("Error saving user data")
                        }
                }

            }.addOnFailureListener {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            onSuccess("You are successfully signed In")
                        } else {
                            onError("Error signing In enter a valid email and password")
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

    fun handleGoogleSignInResult(
        data: Intent?,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                firebaseAuthWithGoogle(account, onSuccess, onError)
            } else {
                onError("Failed to get account")
            }
        } catch (e: ApiException) {
           Log.d("TAG", "handleGoogleSignInResult: ${e.message}")
        }
    }

    fun firebaseAuthWithGoogle(
        account: GoogleSignInAccount,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        val firestore = FirebaseFirestore.getInstance().collection("User")
        val credentials = GoogleAuthProvider.getCredential(account.idToken, null)

        auth.signInWithCredential(credentials)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        val user = User(
                            id = currentUser.uid,
                            email = account.email ?: "",
                            name = account.displayName ?: "",
                            imageUrl = account.photoUrl?.toString() ?: ""
                        )
                        firestore.document(currentUser.uid).set(user)
                            .addOnCompleteListener { saveTask ->
                                if (saveTask.isSuccessful) {
                                    onSuccess("Signed in with Google")
                                } else {
                                    onError("Error saving user data")
                                }
                            }
                    } else {
                        onError("Authentication succeeded but user is null.")
                    }
                } else {
                    // Handle exceptions like ApiException 8 here
                    val exception = task.exception
                    if (exception is ApiException && exception.statusCode == 8) {
                        onError("Google API error: Check your OAuth configuration.")
                    } else {
                        onError("Something went wrong, please try again.")
                    }
                }
            }
    }



}

