package ee.merelaager.gossip.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ee.merelaager.gossip.PostsViewModel
import ee.merelaager.gossip.data.model.Post
import ee.merelaager.gossip.data.repository.PostsRepository

@Composable
fun PostsScreen(endpoint: String, postsRepo: PostsRepository, modifier: Modifier = Modifier) {
    val viewModel: PostsViewModel = remember(endpoint) {
        PostsViewModel(postsRepo, endpoint)
    }
    val state = viewModel.postsState

    LaunchedEffect(endpoint) {
        viewModel.loadPosts()
    }

    when {
        state.isLoading && state.posts.isEmpty() -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        else -> {
            RefreshablePostList(
                items = state.posts,
                isRefreshing = state.isLoading,
                onRefresh = { viewModel.loadPosts() },
                errorMessage = state.errorMessage,
                modifier = modifier
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefreshablePostList(
    items: List<Post>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            errorMessage?.let { message ->
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text("Viga: $message", color = Color.Red)
                    }
                }
            }

            if (items.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Postitusi pole.")
                        }
                    }
                }
            } else {
                items(items) { post ->
                    ListPost(post, modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
fun ListPost(post: Post, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(text = post.title)
        post.content?.let { Text(text = it) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.Favorite,
                contentDescription = if (post.isLiked) "Liked" else "Not liked",
                tint = if (post.isLiked) Color.Red else Color.Gray
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${post.likeCount}",
                color = if (post.isLiked) Color.Red else Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}
