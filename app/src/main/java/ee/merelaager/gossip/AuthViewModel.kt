package ee.merelaager.gossip

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ee.merelaager.gossip.data.model.JSendResponse
import ee.merelaager.gossip.data.network.CookieStorage
import ee.merelaager.gossip.data.repository.AuthRepository
import kotlinx.coroutines.launch

sealed class AuthState {
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
}

class AuthViewModel(private val authRepo: AuthRepository) : ViewModel() {
    private val _authState = mutableStateOf<AuthState>(AuthState.Loading)
    val authState: State<AuthState> = _authState

    init {
        checkAuthentication()
    }

    private fun checkAuthentication() {
        viewModelScope.launch {
            try {
                val response = authRepo.identify()
                _authState.value = when (response) {
                    is JSendResponse.Success -> AuthState.Authenticated
                    else -> AuthState.Unauthenticated
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun setAuthenticated() {
        _authState.value = AuthState.Authenticated
    }

    suspend fun logout() {
        CookieStorage.cookieDataStore.updateData { emptyList() }
        _authState.value = AuthState.Unauthenticated
    }
}