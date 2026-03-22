import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.durrani.taskalert.domain.models.AuthRepository
import com.durrani.taskalert.domain.util.AuthResult
import com.durrani.taskalert.presentation.state.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// presentation/viewmodels/AuthViewModel.kt

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    private val _googleSignInTrigger = MutableStateFlow(false)
    val googleSignInTrigger: StateFlow<Boolean> = _googleSignInTrigger.asStateFlow()

    fun triggerGoogleSignIn() {
        _googleSignInTrigger.value = true
    }
    

    fun onGoogleSignInTriggered() {
        _googleSignInTrigger.value = false
    }
    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            _authState.value = when (val result = repository.signInWithEmail(email, password)) {
                is AuthResult.Success -> AuthState.Success(result.data)
                is AuthResult.Error   -> AuthState.Error(result.message)
                is AuthResult.Loading -> AuthState.Loading
            }
        }
    }

    // activityContext passed from the composable — needed by CredentialManager
    fun signInWithGoogle(activityContext: Context) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            _authState.value = when (val result = repository.signInWithGoogle(activityContext)) {
                is AuthResult.Success -> AuthState.Success(result.data)
                is AuthResult.Error   -> AuthState.Error(result.message)
                is AuthResult.Loading -> AuthState.Loading
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}