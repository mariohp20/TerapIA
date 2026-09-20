package com.pe.terapia.presentation.auth.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pe.terapia.presentation.auth.AuthUiState
import com.pe.terapia.presentation.auth.AuthViewModel
import com.pe.terapia.ui.theme.TerapiaTheme
import com.pe.terapia.R
import org.koin.androidx.compose.koinViewModel
import com.pe.terapia.presentation.auth.utils.GoogleAuthClient
import kotlinx.coroutines.launch

// SCREEN
@Composable
fun LoginScreen(
    onLoginExitoso: () -> Unit,
    onIrARegistro: () -> Unit,
    onIrARecuperar: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Herramientas necesarias para lanzar la interfaz de Google
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val googleAuthClient = remember { GoogleAuthClient(context) }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) onLoginExitoso()
    }

    LoginContent(
        uiState = uiState,
        onLoginClick = { email, password -> viewModel.login(email, password) },
        onGoogleLoginClick = {
            coroutineScope.launch {
                val idToken = googleAuthClient.obtenerIdToken()
                if (idToken != null) {
                    viewModel.loginConGoogle(idToken)
                }
            }
        },
        onIrARegistro = onIrARegistro,
        onIrARecuperar = onIrARecuperar
    )
}

// CONTENT (solo UI pura, sin ViewModel)
@Composable
fun LoginContent(
    uiState: AuthUiState,
    onLoginClick: (email: String, password: String) -> Unit,
    onGoogleLoginClick: () -> Unit,
    onIrARegistro: () -> Unit,
    onIrARecuperar: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.login_titulo), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(
            stringResource(R.string.login_subtitulo),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text(stringResource(R.string.login_email)) }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text(stringResource(R.string.login_password))}, singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        TextButton(onClick = onIrARecuperar, modifier = Modifier.align(Alignment.End)) {
            Text(stringResource(R.string.login_olvide_password))
        }

        if (uiState is AuthUiState.Error) {
            Text(
                uiState.mensaje,
                color = MaterialTheme.colorScheme.error, fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = { onLoginClick(email.trim(), password) },
            enabled = uiState !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (uiState is AuthUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.login_boton_ingresar))
            }
        }

        Spacer(Modifier.height(12.dp))
        // Boton de Google
        OutlinedButton(
            onClick = onGoogleLoginClick,
            enabled = uiState !is AuthUiState.Loading, // Buena práctica: bloquear botón de Google si ya está cargando el otro
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_google),
                contentDescription = stringResource(id = R.string.login_google_continuar),
                modifier = Modifier.size(24.dp),
                tint = androidx.compose.ui.graphics.Color.Unspecified
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(id = R.string.login_google_continuar),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(Modifier.height(24.dp))
        Row {
            Text(stringResource(R.string.login_sin_cuenta), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                stringResource(R.string.login_ir_registro), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onIrARegistro)
            )
        }
    }
}

// PREVIEWS
@Preview(showBackground = true, name = "Login - Normal")
@Composable
private fun LoginPreviewIdle() {
    TerapiaTheme {
        LoginContent(AuthUiState.Idle, onLoginClick = { _, _ -> }, onGoogleLoginClick = {}, onIrARegistro = {}, onIrARecuperar = {})
    }
}

@Preview(showBackground = true, name = "Login - Cargando")
@Composable
private fun LoginPreviewLoading() {
    TerapiaTheme {
        LoginContent(AuthUiState.Loading, onLoginClick = { _, _ -> }, onGoogleLoginClick = {}, onIrARegistro = {}, onIrARecuperar = {})
    }
}

@Preview(showBackground = true, name = "Login - Error")
@Composable
private fun LoginPreviewError() {
    TerapiaTheme {
        LoginContent(
            AuthUiState.Error("Correo o contraseña incorrectos."),
            onLoginClick = { _, _ -> }, onGoogleLoginClick = {}, onIrARegistro = {}, onIrARecuperar = {}
        )
    }
}

@Preview(
    showBackground = true,
    name = "Login - Modo Oscuro",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES // Forzamos el modo oscuro para ver un preview
)
@Composable
private fun LoginPreviewDark() {
    TerapiaTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            LoginContent(AuthUiState.Idle, { _, _ -> }, {}, {}, {})
        }
    }
}