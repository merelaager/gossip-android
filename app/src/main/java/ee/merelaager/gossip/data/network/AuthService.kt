package ee.merelaager.gossip.data.network

import ee.merelaager.gossip.data.model.JSendResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<ResponseBody>
}

data class LoginRequest(val username: String, val password: String)
