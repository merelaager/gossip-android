package ee.merelaager.gossip.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import ee.merelaager.gossip.PostsViewModel
import ee.merelaager.gossip.PostsViewModelFactory
import ee.merelaager.gossip.data.model.Post
import ee.merelaager.gossip.data.repository.PostsRepository
import ee.merelaager.gossip.ui.theme.GossipPink
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showCreatePost by remember { mutableStateOf(false) }

    if (isInitialLoading && isEmpty) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Box(modifier = modifier.fillMaxSize()) {
            PostsPagingList(pagingItems, navController, modifier)

            FloatingActionButton(
                onClick = { showCreatePost = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = GossipPink
            ) {
                Icon(Icons.Default.Add, contentDescription = "Loo postitus")
            }

            if (showCreatePost) {
                ModalBottomSheet(
                    onDismissRequest = { showCreatePost = false },
                    sheetState = sheetState
                ) {
                    CreatePostSheetContent(
                        onSubmit = { title, content ->
                            // TODO: Call viewModel.createPost(title, content)
                            println("New Post: $title / $content")
                            scope.launch {
                                sheetState.hide()
                                showCreatePost = false
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CreatePostSheetContent(
    onSubmit: (title: String, content: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Loo postitus", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Pealkiri") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Sisu") },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            maxLines = 5
        )


        Button(onClick = { imagePickerLauncher.launch("image/*") }) {
            Text("Vali pilt")
        }

        imageUri?.let { uri ->
            AsyncImage(
                model = uri,
                contentDescription = "Valitud pilt",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
        }

        Button(
            onClick = { onSubmit(title, content) },
            enabled = title.isNotBlank() && content.isNotBlank(),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Postita")
        }
    }
}


