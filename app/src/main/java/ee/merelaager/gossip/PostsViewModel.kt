package ee.merelaager.gossip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import ee.merelaager.gossip.data.model.Post
import ee.merelaager.gossip.data.repository.PostsRepository
import kotlinx.coroutines.flow.Flow


class PostsViewModelFactory(
    private val postsRepo: PostsRepository,
    private val endpoint: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PostsViewModel::class.java)) {
            return PostsViewModel(postsRepo, endpoint) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class PostsViewModel(
    private val postsRepo: PostsRepository,
    private val endpoint: String
) : ViewModel() {

    val postsFlow: Flow<PagingData<Post>> = postsRepo.getPostsPager(endpoint)
        .cachedIn(viewModelScope)
}