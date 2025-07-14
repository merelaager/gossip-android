package ee.merelaager.gossip

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ee.merelaager.gossip.data.network.ApiClient
import ee.merelaager.gossip.data.network.CookieStorage
import ee.merelaager.gossip.data.repository.AuthRepository
import ee.merelaager.gossip.data.repository.PostsRepository
import ee.merelaager.gossip.ui.AccountScreen
import ee.merelaager.gossip.ui.LoginScreen
import ee.merelaager.gossip.ui.PostsScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Kõlakad", Icons.Default.ChatBubble)
    object Liked : Screen("liked", "Kõva kumu", Icons.Default.Favorite)
    object Mine : Screen("mine", "Minu", Icons.Default.FolderShared)
    object Waitlist : Screen("waitlist", "Ootel", Icons.Default.Schedule)
    object Account : Screen("account", "Konto", Icons.Default.AccountCircle)
}

@Composable
fun GossipApp(context: Context) {
    var isInitialised by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            CookieStorage.init(context)
            ApiClient.init(context)
        }
        isInitialised = true
    }

    if (!isInitialised) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val authRepo = remember { AuthRepository(ApiClient.authService) }
    val authViewModel = remember {
        AuthViewModel(authRepo)
    }

    val postsRepo = remember { PostsRepository(ApiClient.postsService) }

    when (authViewModel.authState.value) {
        is AuthState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is AuthState.Unauthenticated -> {
            LoginScreen(onLoginSuccess = {
                authViewModel.setAuthenticated()
            }, authRepo)
        }

        is AuthState.Authenticated -> {
            GossipMainApp(authViewModel, postsRepo)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GossipMainApp(authViewModel: AuthViewModel, postsRepo: PostsRepository) {
    val navController = rememberNavController()
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    val title = when (currentRoute) {
        Screen.Home.route -> Screen.Home.label
        Screen.Liked.route -> Screen.Liked.label
        Screen.Mine.route -> "Minu postitused"
        Screen.Waitlist.route -> Screen.Waitlist.label
        Screen.Account.route -> Screen.Account.label
        else -> "Gossip"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) }
            )
        },
        bottomBar = {
            GossipBottomNavigation(
                currentRoute = currentRoute,
                onItemSelected = { screen ->
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        GossipNavHost(navController, authViewModel, postsRepo, Modifier.padding(innerPadding))
    }
}

@Composable
fun GossipBottomNavigation(
    currentRoute: String?,
    onItemSelected: (Screen) -> Unit
) {
    val items = listOf(
        Screen.Home,
        Screen.Liked,
        Screen.Mine,
        Screen.Waitlist,
        Screen.Account
    )

    NavigationBar {
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = currentRoute == screen.route,
                onClick = { onItemSelected(screen) }
            )
        }
    }
}

@Composable
fun GossipNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    postsRepo: PostsRepository,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) { PostsScreen("", postsRepo) }
        composable(Screen.Liked.route) { PostsScreen("liked", postsRepo) }
        composable(Screen.Mine.route) { PostsScreen("my", postsRepo) }
        composable(Screen.Waitlist.route) { PostsScreen("waitlist", postsRepo) }
        composable(Screen.Account.route) { AccountScreen(authViewModel) }
    }
}
