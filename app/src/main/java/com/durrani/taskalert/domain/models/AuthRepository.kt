package com.durrani.taskalert.domain.models

import android.content.Context

interface AuthRepository {
        suspend fun signInWithEmail(email: String, password: String): com.durrani.taskalert.domain.util.AuthResult<String>
        suspend fun signInWithGoogle(activityContext: Context): com.durrani.taskalert.domain.util.AuthResult<String>
        suspend fun signOut()
    }
