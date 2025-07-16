package ee.merelaager.gossip

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FolderShared
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ee.merelaager.gossip.data.network.ApiClient
import ee.merelaager.gossip.data.network.CookieStorage
import ee.merelaager.gossip.data.repository.AuthRepository
import ee.merelaager.gossip.data.repository.PostsRepository
import ee.merelaager.gossip.ui.AccountScreen
import ee.merelaager.gossip.ui.LoginScreen
import ee.merelaager.gossip.ui.PostDetailScreen
import ee.merelaager.gossip.ui.PostsScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class Screen(
    val route: String,
    val label: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector
) {
    object Home : Screen(
        "home", "Kõlakad",
        Icons.Rounded.ChatBubble, Icons.Rounded.ChatBubbleOutline
    )

    object Liked : Screen(
        "liked", "Kõva kumu",
        Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder
    )

    object Mine : Screen(
        "mine", "Minu",
        Icons.Filled.FolderShared, Icons.Outlined.FolderShared
    )

    object Waitlist : Screen(
        "waitlist", "Ootel",
        // Sadly no filled variant of the pending actions.
        Icons.AutoMirrored.Filled.Assignment, Icons.Outlined.PendingActions
    )

    object Account : Screen(
        "account", "Konto",
        Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle
    )
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
    val backStackArgs = navBackStackEntry.value?.arguments

    val currentRoute = navBackStackEntry.value?.destination?.route
    backStackArgs?.getString("from")

    val showBackButton = currentRoute?.startsWith("post/") == true

    val title = when {
        currentRoute == null -> "Gossip"
        currentRoute == Screen.Home.route -> Screen.Home.label
        currentRoute == Screen.Liked.route -> Screen.Liked.label
        currentRoute == Screen.Mine.route -> "Minu postitused"
        currentRoute == Screen.Waitlist.route -> Screen.Waitlist.label
        currentRoute == Screen.Account.route -> Screen.Account.label
        currentRoute.startsWith("post/") -> ""
//        {
//            when (fromRoute) {
//                Screen.Home.route -> Screen.Home.label
//                Screen.Liked.route -> Screen.Liked.label
//                Screen.Mine.route -> "Minu postitused"
//                Screen.Waitlist.route -> Screen.Waitlist.label
//                else -> "Postitus"
//            }
//        }
        else -> "Gossip"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = {
                            navController.popBackStack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        },
        bottomBar = {
            MainNavigationBar(
                currentRoute = currentRoute,
                onDestinationSelected = { screen ->
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
fun MainNavigationBar(
    currentRoute: String?,
    onDestinationSelected: (Screen) -> Unit
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
            val selected = currentRoute == screen.route
            NavigationBarItem(
                selected = selected,
                icon = {
                    Icon(
                        imageVector = if (selected) screen.filledIcon else screen.outlinedIcon,
                        contentDescription = screen.label
                    )
                },
                label = { Text(screen.label) },
                onClick = { onDestinationSelected(screen) }
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
        composable(Screen.Home.route) { PostsScreen("", postsRepo, navController) }
        composable(Screen.Liked.route) { PostsScreen("liked", postsRepo, navController) }
        composable(Screen.Mine.route) { PostsScreen("my", postsRepo, navController) }
        composable(Screen.Waitlist.route) { PostsScreen("waitlist", postsRepo, navController) }
        composable(Screen.Account.route) { AccountScreen(authViewModel) }
        composable(
            "post/{postId}?from={from}", arguments = listOf(
                navArgument("postId") { type = NavType.StringType },
                navArgument("from") {
                    type = NavType.StringType
                    defaultValue = Screen.Home.route
                })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: return@composable
            PostDetailScreen(postId = postId, postsRepo = postsRepo)
        }
    }
}
