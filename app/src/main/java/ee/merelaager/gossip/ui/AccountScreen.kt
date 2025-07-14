package ee.merelaager.gossip.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ee.merelaager.gossip.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun AccountScreen(authViewModel: AuthViewModel) {
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            scope.launch {
                authViewModel.logout()
            }
        }) {
            Text("Logi välja")
        }
    }
}
