package ee.merelaager.gossip

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.State
import ee.merelaager.gossip.data.network.ApiClient

sealed class AuthState {
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
}

class AuthViewModel : ViewModel() {
    private val _authState = mutableStateOf<AuthState>(AuthState.Loading)
    val authState: State<AuthState> = _authState

    init {
        checkAuthentication()
    }

    private fun checkAuthentication() {
        viewModelScope.launch {
            _authState.value = AuthState.Unauthenticated
//            try {
//                val user = ApiClient.userService.getMe() // your endpoint
//                _authState.value = AuthState.Authenticated
//            } catch (e: Exception) {
//                _authState.value = AuthState.Unauthenticated
//            }
        }
    }

    fun setAuthenticated() {
        _authState.value = AuthState.Authenticated
    }

    fun logout() {
        _authState.value = AuthState.Unauthenticated
        // Optionally: clear cookies here
    }
}