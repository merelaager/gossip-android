package ee.merelaager.gossip

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ee.merelaager.gossip.data.model.JSendResponse
import ee.merelaager.gossip.data.model.Post
import ee.merelaager.gossip.data.repository.PostsRepository
import kotlinx.coroutines.launch

class PostsViewModel(
    private val postsRepo: PostsRepository,
    private val endpoint: String
) : ViewModel() {

    var postsState by mutableStateOf(PostsUiState(isLoadingInitial = true))
        private set

    fun loadPosts() {
        viewModelScope.launch {
            postsState = postsState.copy(isLoadingInitial = true, errorMessage = null)
            try {
                val response = postsRepo.getPosts(endpoint, 1, 25)
                when (response) {
                    is JSendResponse.Success -> {
                        postsState = PostsUiState(
                            posts = response.data.posts,
                            currentPage = response.data.currentPage,
                            totalPages = response.data.totalPages,
                            isLoadingInitial = false
                        )
                    }

                    is JSendResponse.Error -> {
                        postsState = postsState.copy(
                            isLoadingInitial = false,
                            errorMessage = response.message
                        )
                    }

                    else -> {
                        postsState = postsState.copy(
                            isLoadingInitial = false,
                            errorMessage = "Midagi läks nihu"
                        )
                    }
                }
            } catch (e: Exception) {
                postsState = postsState.copy(
                    isLoadingInitial = false,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun loadMorePosts() {
        if (postsState.isLoadingMore || !postsState.hasMore) return

        viewModelScope.launch {
            postsState = postsState.copy(isLoadingMore = true, errorMessage = null)
            try {
                val nextPage = postsState.currentPage + 1
                val response = postsRepo.getPosts(endpoint, nextPage, 25)
                when (response) {
                    is JSendResponse.Success -> {
                        postsState = postsState.copy(
                            posts = postsState.posts + response.data.posts,
                            currentPage = response.data.currentPage,
                            totalPages = response.data.totalPages,
                            isLoadingMore = false
                        )
                    }

                    is JSendResponse.Error -> {
                        postsState = postsState.copy(
                            isLoadingMore = false,
                            errorMessage = response.message
                        )
                    }

                    else -> {
                        postsState = postsState.copy(
                            isLoadingMore = false,
                            errorMessage = "Midagi läks nihu"
                        )
                    }
                }
            } catch (e: Exception) {
                postsState = postsState.copy(
                    isLoadingMore = false,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }
}

data class PostsUiState(
    val posts: List<Post> = emptyList(),
    val isLoadingInitial: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1
) {
    val hasMore: Boolean get() = currentPage < totalPages
}