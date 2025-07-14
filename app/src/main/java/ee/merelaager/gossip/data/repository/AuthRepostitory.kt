package ee.merelaager.gossip.data.repository

import ee.merelaager.gossip.data.model.JSendResponse
import ee.merelaager.gossip.data.model.User
import ee.merelaager.gossip.data.network.AuthService
import ee.merelaager.gossip.data.network.LoginRequest
import ee.merelaager.gossip.data.network.executeJSendCall
import kotlinx.serialization.Serializable

@Serializable
data class LoginSuccess(val username: String, val role: String)

@Serializable
data class AuthError(val message: String)

class AuthRepository(private val authService: AuthService) {
    suspend fun login(
        username: String,
        password: String
    ): JSendResponse<LoginSuccess, AuthError>? {
        val request = LoginRequest(username, password)

        return executeJSendCall<LoginSuccess, AuthError> {
            authService.login(request)
        }
    }

    suspend fun identify(): JSendResponse<User, AuthError>? {
        println("MAKING IDENTIFICATION REQUEST")
        return executeJSendCall<User, AuthError> {
            authService.getAccountInfo()
        }
    }
}