package ee.merelaager.gossip.data.network

import kotlinx.serialization.Serializable
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthService {
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<ResponseBody>

    @GET("account")
    suspend fun getAccountInfo(): Response<ResponseBody>
}

@Serializable
data class LoginRequest(val username: String, val password: String)
