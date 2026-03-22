import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.durrani.taskalert.domain.models.AuthRepository
import com.durrani.taskalert.domain.util.AuthResult
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.durrani.taskalert.R
import com.durrani.taskalert.domain.models.User
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption

// data/repository/AuthRepositoryImpl.kt

class AuthRepositoryImpl(
    private val context: Context,              // ApplicationContext for init
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    private val TAG = "AuthRepositoryImpl"

    // ── Email Sign In (unchanged) ─────────────────────────────────────────
    override suspend fun signInWithEmail(
        email: String,
        password: String
    ): AuthResult<String> {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            val uid = auth.currentUser?.uid
                ?: return AuthResult.Error("User is null after registration.")

            saveUserToFirestore(
                id       = uid,
                email    = auth.currentUser?.email ?: email,
                name     = auth.currentUser?.displayName ?: "",
                imageUrl = ""
            )
            AuthResult.Success("Account created successfully!")

        } catch (e: FirebaseAuthUserCollisionException) {
            // Account exists → sign in instead
            signInExistingUser(email, password)

        } catch (e: FirebaseAuthWeakPasswordException) {
            AuthResult.Error("Password is too weak. Use at least 6 characters.")

        } catch (e: Exception) {
            Log.e(TAG, "signInWithEmail: ${e.message}", e)
            AuthResult.Error("Something went wrong. Please try again.")
        }
    }

    private suspend fun signInExistingUser(
        email: String,
        password: String
    ): AuthResult<String> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            AuthResult.Success("Welcome back!")
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            AuthResult.Error("Incorrect password. Please try again.")
        } catch (e: FirebaseAuthInvalidUserException) {
            AuthResult.Error("No account found with this email.")
        } catch (e: Exception) {
            Log.e(TAG, "signInExistingUser: ${e.message}", e)
            AuthResult.Error("Sign in failed. Please try again.")
        }
    }

    // ── Google Sign In (NEW WAY) ───────────────────────────────────────────
    override suspend fun signInWithGoogle(activityContext: Context): AuthResult<String> {

        // GetSignInWithGoogleOption is specifically designed for button-triggered flows
        // It avoids the filterByAuthorizedAccounts/autoSelect conflicts entirely
        val signInWithGoogleOption = GetSignInWithGoogleOption
            .Builder(serverClientId = context.getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        val credentialManager = CredentialManager.create(activityContext)

        return try {
            val result = credentialManager.getCredential(
                request = request,
                context = activityContext
            )
            processGoogleCredential(result)

        } catch (e: GetCredentialCancellationException) {
            Log.e(TAG, "User cancelled: ${e.message}")
            AuthResult.Error("Sign in cancelled.")

        } catch (e: GetCredentialException) {
            Log.e(TAG, "GetCredentialException type: ${e.type} msg: ${e.message}")
            AuthResult.Error("Google Sign-In failed: ${e.message}")

        } catch (e: Exception) {
            Log.e(TAG, "signInWithGoogle unexpected: ${e.message}", e)
            AuthResult.Error("An unexpected error occurred.")
        }
    }    // Called when no previously-used accounts exist → show ALL Google accounts
    private suspend fun signInWithGoogleAllAccounts(
        activityContext: Context,
        credentialManager: CredentialManager
    ): AuthResult<String> {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false) // ← key difference
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(
                request = request,
                context = activityContext
            )
            processGoogleCredential(result)

        } catch (e: GetCredentialCancellationException) {
            AuthResult.Error("Sign in cancelled.")

        } catch (e: GetCredentialException) {
            Log.e(TAG, "signInWithGoogleAllAccounts: ${e.message}", e)
            AuthResult.Error("Google Sign-In failed. Please try again.")
        }
    }

    // Step D: Extract the token from the credential and sign into Firebase
    private suspend fun processGoogleCredential(
        result: GetCredentialResponse
    ): AuthResult<String> {
        val credential = result.credential

        // The credential comes back as CustomCredential
        // We check its type to make sure it's a Google ID token
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            return try {
                // Parse the credential data into a GoogleIdTokenCredential
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                // Get the raw ID token string
                val idToken = googleIdTokenCredential.idToken

                // Use this token to authenticate with Firebase
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val user = authResult.user
                    ?: return AuthResult.Error("Authentication succeeded but user is null.")

                // Save to Firestore
                saveUserToFirestore(
                    id       = user.uid,
                    email    = user.email ?: "",
                    name     = user.displayName ?: "",
                    imageUrl = user.photoUrl?.toString() ?: ""
                )

                AuthResult.Success("Signed in with Google successfully!")

            } catch (e: GoogleIdTokenParsingException) {
                Log.e(TAG, "Invalid Google ID token: ${e.message}", e)
                AuthResult.Error("Invalid Google token. Please try again.")

            } catch (e: Exception) {
                Log.e(TAG, "processGoogleCredential: ${e.message}", e)
                AuthResult.Error("Firebase authentication failed.")
            }

        } else {
            Log.e(TAG, "Unexpected credential type: ${credential.type}")
            return AuthResult.Error("Unexpected credential type.")
        }
    }

    // ── Sign Out ──────────────────────────────────────────────────────────
    override suspend fun signOut() {
        try {
            auth.signOut()
            // Clear stored credential session so next sign-in shows account picker
            val credentialManager = CredentialManager.create(context)
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e(TAG, "signOut: ${e.message}", e)
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private suspend fun saveUserToFirestore(
        id: String, email: String, name: String, imageUrl: String
    ) {
        try {
            val user = User(id = id, email = email, name = name, imageUrl = imageUrl)
            firestore.collection("User").document(id).set(user).await()
        } catch (e: Exception) {
            Log.e(TAG, "saveUserToFirestore: ${e.message}", e)
        }
    }
}