package ee.merelaager.gossip

import androidx.paging.PagingSource
import androidx.paging.PagingState
import ee.merelaager.gossip.data.model.JSendResponse
import ee.merelaager.gossip.data.model.Post
import ee.merelaager.gossip.data.repository.PostsRepository

class PostsPagingSource(
    private val postsRepo: PostsRepository,
    private val endpoint: String
) : PagingSource<Int, Post>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Post> {
        val page = params.key ?: 1
        return try {
            val response = postsRepo.getPosts(endpoint, page, params.loadSize)
            when (response) {
                is JSendResponse.Success -> {
                    val postsResponse = response.data
                    LoadResult.Page(
                        data = postsResponse.posts,
                        prevKey = if (page == 1) null else page - 1,
                        nextKey = if (page < postsResponse.totalPages) page + 1 else null
                    )
                }

                is JSendResponse.Error -> {
                    LoadResult.Error(Exception(response.message))
                }

                else -> {
                    LoadResult.Error(Exception("Unknown error"))
                }
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }


    override fun getRefreshKey(state: PagingState<Int, Post>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}