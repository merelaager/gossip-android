package ee.merelaager.gossip.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ee.merelaager.gossip.PostsPagingSource
import ee.merelaager.gossip.data.model.JSendResponse
import ee.merelaager.gossip.data.model.Post
import ee.merelaager.gossip.data.network.PostsService
import ee.merelaager.gossip.data.network.executeJSendCall
import kotlinx.coroutines.flow.Flow
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

    fun getPostsPager(endpoint: String): Flow<PagingData<Post>> {
        return Pager(
            config = PagingConfig(
                pageSize = 25,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { PostsPagingSource(this, endpoint) }
        ).flow
    }
}
