package com.pe.terapia.presentation.auth.registro

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pe.terapia.R
import com.pe.terapia.presentation.auth.AuthUiState
import com.pe.terapia.presentation.auth.AuthViewModel
import com.pe.terapia.ui.theme.TerapiaTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun RegistroScreen(
    onRegistroExitoso: () -> Unit,
    onIrALogin: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) onRegistroExitoso()
    }

    RegistroContent(
        uiState = uiState,
        onRegistroClick = { nombre, email, password -> viewModel.registro(email, password, nombre) },
        onIrALogin = onIrALogin
    )
}

@Composable
fun RegistroContent(
    uiState: AuthUiState,
    onRegistroClick: (nombre: String, email: String, password: String) -> Unit,
    onIrALogin: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }
    var errorLocal by remember { mutableStateOf<String?>(null) }

    // Obtenemos el texto de error ANTES del onClick para que Compose no se queje
    val errorPasswordNoCoincide = stringResource(R.string.registro_error_passwords_no_coinciden)

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.registro_titulo),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(R.string.registro_aviso_rol),
            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Nota cómo ahora envolvemos el stringResource dentro de un componente Text()
        OutlinedTextField(nombre, { nombre = it }, label = { Text(stringResource(R.string.registro_nombre)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(email, { email = it }, label = { Text(stringResource(R.string.login_email)) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(password, { password = it }, label = { Text(stringResource(R.string.login_password)) }, singleLine = true, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(confirmarPassword, { confirmarPassword = it }, label = { Text(stringResource(R.string.registro_confirmar_password)) }, singleLine = true, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth())

        val mensajeError = errorLocal ?: (uiState as? AuthUiState.Error)?.mensaje
        if (mensajeError != null) {
            Text(mensajeError, color = MaterialTheme.colorScheme.error, fontSize = 13.sp, modifier = Modifier.padding(top = 12.dp))
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                // Usamos la variable que declaramos más arriba
                errorLocal = if (password != confirmarPassword) errorPasswordNoCoincide else null
                if (errorLocal == null) onRegistroClick(nombre.trim(), email.trim(), password)
            },
            enabled = uiState !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (uiState is AuthUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                // Aquí también envolvemos en un Text y usamos tu string "registro_boton_crear"
                Text(stringResource(R.string.registro_boton_crear))
            }
        }

        Spacer(Modifier.height(24.dp))

        Row {
            Text(stringResource(R.string.registro_ya_tengo_cuenta), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = stringResource(R.string.registro_ir_login),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onIrALogin)
            )
        }
    }
}

@Preview(showBackground = true, name = "Registro - Normal")
@Composable
private fun RegistroPreviewIdle() {
    TerapiaTheme {
        RegistroContent(AuthUiState.Idle, onRegistroClick = { _, _, _ -> }, onIrALogin = {})
    }
}

@Preview(showBackground = true, name = "Registro - Error")
@Composable
private fun RegistroPreviewError() {
    TerapiaTheme {
        RegistroContent(
            AuthUiState.Error("Este correo ya está registrado."),
            onRegistroClick = { _, _, _ -> }, onIrALogin = {}
        )
    }
}