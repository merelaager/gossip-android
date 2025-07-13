package ee.merelaager.gossip.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ee.merelaager.gossip.data.network.ApiClient
import ee.merelaager.gossip.data.network.LoginRequest
import ee.merelaager.gossip.data.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, authRepository: AuthRepository) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TextField(value = username, onValueChange = { username = it }, label = { Text("Username") })
        TextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation())

        if (error != null) {
            Text(text = error!!, color = Color.Red)
        }

        Button(
            onClick = {
                loading = true
                error = null
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // ApiClient.authService.login(LoginRequest(username, password))
                        val res = authRepository.login(username, password)
                        println(res)
                        withContext(Dispatchers.Main) {
                            loading = false
                            onLoginSuccess()
                        }
                    } catch (e: Exception) {
                        println(e)
                        withContext(Dispatchers.Main) {
                            loading = false
                            error = "Login failed"
                        }
                    }
                }
            },
            enabled = !loading
        ) {
            Text("Login")
        }
    }
}
