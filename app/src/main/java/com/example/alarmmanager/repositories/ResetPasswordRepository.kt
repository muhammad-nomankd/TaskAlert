package com.example.alarmmanager.repositories

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

fun ResetPasswordRepository(
    email: String, context: Context, navController: NavController, onSuccess: () -> Unit,
    onFailure: () -> Unit,
) {
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
                                onSuccess()
                            } else {
                                onFailure()
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
                            Log.d("User deletion", "User Deletion is Successfull")
                        } else {
                            Log.d("User deletion", "User Deletion failed")
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
