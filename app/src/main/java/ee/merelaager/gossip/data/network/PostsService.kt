package ee.merelaager.gossip.data.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PostsService {
    @GET("/posts/{endpoint}")
    suspend fun getPosts(
        @Path("endpoint") endpoint: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 25
    ): Response<ResponseBody>
}

