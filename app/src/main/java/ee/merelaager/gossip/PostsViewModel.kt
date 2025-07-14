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

    var postsState by mutableStateOf<PostsUiState>(PostsUiState.Loading)
        private set

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            postsState = PostsUiState.Loading
            try {
                val response = postsRepo.getPosts(endpoint, 1, 25)
                postsState = when (response) {
                    is JSendResponse.Success -> PostsUiState.Success(response.data.posts)
                    is JSendResponse.Error -> PostsUiState.Error(response.message)
                    else -> PostsUiState.Error("Midagi läks nihu")
                }
            } catch (e: Exception) {
                postsState = PostsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class PostsUiState {
    object Loading : PostsUiState()
    data class Success(val posts: List<Post>) : PostsUiState()
    data class Error(val message: String) : PostsUiState()
}