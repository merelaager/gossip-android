package ee.merelaager.gossip.data.repository

import ee.merelaager.gossip.data.model.JSendResponse
import ee.merelaager.gossip.data.model.Post
import ee.merelaager.gossip.data.network.PostsService
import ee.merelaager.gossip.data.network.executeJSendCall
import kotlinx.serialization.Serializable

@Serializable
data class PostsResponse(val posts: List<Post>, val currentPage: Int, val totalPages: Int)

@Serializable
data class PostsError(val message: String)

class PostsRepository(private val postsService: PostsService) {
    suspend fun getPosts(
        endpoint: String,
        page: Int,
        limit: Int
    ): JSendResponse<PostsResponse, PostsError>? {
        return executeJSendCall<PostsResponse, PostsError> {
            postsService.getPosts(endpoint, page, limit)
        }
    }
}