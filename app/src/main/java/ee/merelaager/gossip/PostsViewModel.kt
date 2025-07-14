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

    var postsState by mutableStateOf(PostsUiState(isLoading = true))
        private set

    fun loadPosts() {
        viewModelScope.launch {
            postsState = postsState.copy(isLoading = true, errorMessage = null)
            try {
                val response = postsRepo.getPosts(endpoint, 1, 25)
                postsState = when (response) {
                    is JSendResponse.Success -> PostsUiState(posts = response.data.posts)
                    is JSendResponse.Error -> postsState.copy(
                        isLoading = false,
                        errorMessage = response.message
                    )

                    else -> postsState.copy(isLoading = false, errorMessage = "Midagi läks nihu")
                }
            } catch (e: Exception) {
                postsState =
                    postsState.copy(isLoading = false, errorMessage = e.message ?: "Unknown error")
            }
        }
    }
}

data class PostsUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)