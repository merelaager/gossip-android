package ee.merelaager.gossip.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import ee.merelaager.gossip.PostsViewModel
import ee.merelaager.gossip.PostsViewModelFactory
import ee.merelaager.gossip.data.model.Post
import ee.merelaager.gossip.data.repository.PostsRepository

@Composable
fun PostsScreen(
    endpoint: String,
    postsRepo: PostsRepository,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val factory = remember(endpoint, postsRepo) {
        PostsViewModelFactory(postsRepo, endpoint)
    }
    val viewModel: PostsViewModel = viewModel(factory = factory)
    val pagingItems: LazyPagingItems<Post> = viewModel.postsFlow.collectAsLazyPagingItems()

    val isInitialLoading = pagingItems.loadState.refresh is LoadState.Loading
    val isEmpty = pagingItems.itemCount == 0


    if (isInitialLoading && isEmpty) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        PostsPagingList(pagingItems, navController, modifier)
    }
}


