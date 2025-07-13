package ee.merelaager.gossip

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Kõlakad", Icons.Default.ChatBubble)
    object Liked : Screen("liked", "Kõva kumu", Icons.Default.Favorite)
    object Mine : Screen("mine", "Minu", Icons.Default.FolderShared)
    object Waitlist : Screen("waitlist", "Ootel", Icons.Default.Schedule)
    object Account : Screen("account", "Konto", Icons.Default.AccountCircle)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GossipApp() {
    val navController = rememberNavController()
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    val items = listOf(
        Screen.Home,
        Screen.Liked,
        Screen.Mine,
        Screen.Waitlist,
        Screen.Account
    )

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
            NavigationBar {
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentRoute == screen.route,
                        onClick = {
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
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { PostsScreen("Kõlakad") }
            composable(Screen.Liked.route) { PostsScreen("Kõva kumu") }
            composable(Screen.Mine.route) { PostsScreen("Minu") }
            composable(Screen.Waitlist.route) { PostsScreen("Ootel") }
            composable(Screen.Account.route) { AccountScreen() }
        }
    }
}

@Composable
fun PostsScreen(title: String) {
   Text("Display $title")
}

@Composable
fun AccountScreen() {
    Text("Konto")
}
