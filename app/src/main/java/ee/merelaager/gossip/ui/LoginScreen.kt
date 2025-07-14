package ee.merelaager.gossip.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import ee.merelaager.gossip.data.model.JSendResponse
import ee.merelaager.gossip.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    authRepository: AuthRepository,
    modifier: Modifier = Modifier
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    val gossipPink = Color(0xFFFF2D55)

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Merelaagri gossip",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
                modifier = modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Logi sisse",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                modifier = modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                textAlign = TextAlign.Center
            )

            Column(modifier = modifier.fillMaxWidth()) {
                StyledField(
                    value = username,
                    onValueChange = { username = it },
                    labelText = "Kasutajanimi"
                )
                Spacer(modifier = modifier.height(16.dp))
                StyledField(
                    value = password,
                    onValueChange = { password = it },
                    labelText = "Salasõna",
                    isPassword = true
                )

                if (error != null) {
                    Spacer(modifier = modifier.height(16.dp))
                    Text(text = error!!, color = Color.Red)
                    Spacer(modifier = modifier.height(16.dp))
                } else {
                    Spacer(modifier = modifier.height(32.dp))
                }


                Button(
                    onClick = {
                        loading = true
                        error = null
                        coroutineScope.launch {
                            try {
                                val response = authRepository.login(username, password)
                                when (response) {
                                    is JSendResponse.Success -> {
                                        withContext(Dispatchers.Main) {
                                            loading = false
                                            onLoginSuccess()
                                        }
                                    }

                                    is JSendResponse.Fail -> {
                                        withContext(Dispatchers.Main) {
                                            loading = false
                                            error = response.data.message
                                        }
                                    }

                                    else -> {
                                        withContext(Dispatchers.Main) {
                                            loading = false
                                            error = "Serveripoolne viga."
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                println(e)
                                withContext(Dispatchers.Main) {
                                    loading = false
                                    error = "Serveriga ei õnnestunud ühendust luua."
                                }
                            }
                        }
                    },
                    enabled = !loading,
                    modifier = modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = gossipPink),
                ) {
                    Text(
                        text = "Logi sisse",
                        fontWeight = FontWeight.Medium,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                        color = Color.White
                    )
                }

                Spacer(modifier = modifier.height(24.dp))

                val footnoteColor = MaterialTheme.colorScheme.onSurfaceVariant

                Text(
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(color = footnoteColor)) {
                            append("Pole kontot? ")
                        }
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Medium,
                                color = footnoteColor
                            )
                        ) {
                            append("Loo konto.")
                        }
                    },
                    textAlign = TextAlign.Center,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    modifier = modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun StyledField(
    value: String,
    onValueChange: (String) -> Unit,
    labelText: String,
    isPassword: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderWidth = if (isFocused) 3.dp else 1.dp

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = labelText,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = borderWidth,
                    color = Color.Black,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(1.dp)
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                interactionSource = interactionSource,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text,
                ),
                visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = Color.Black
                ),
                textStyle = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

