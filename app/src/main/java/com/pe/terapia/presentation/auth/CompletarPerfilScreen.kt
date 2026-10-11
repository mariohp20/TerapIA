package com.pe.terapia.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pe.terapia.R
import com.pe.terapia.domain.model.Usuario
import com.pe.terapia.domain.usecase.auth.DniValidator
import org.koin.androidx.compose.koinViewModel

@Composable
fun CompletarPerfilScreen(
    onCompletado: (Usuario) -> Unit,
    onSalir: () -> Unit,
    mensajeInicial: String? = null,
    viewModel: CompletarPerfilViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val borrador by viewModel.borrador.collectAsStateWithLifecycle()
    LaunchedEffect(state) {
        if (state is CompletarPerfilUiState.Success) onCompletado((state as CompletarPerfilUiState.Success).usuario)
    }

    var nombres by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    LaunchedEffect(borrador) {
        if (nombres.isBlank()) nombres = borrador.nombres
        if (apellidos.isBlank()) apellidos = borrador.apellidos
        if (telefono.isBlank()) telefono = borrador.telefono
        if (dni.isBlank()) dni = borrador.dni
    }
    val error = (state as? CompletarPerfilUiState.Error)?.mensaje ?: mensajeInicial

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .navigationBarsPadding().imePadding().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.perfil_completar_titulo), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.perfil_completar_subtitulo), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = borrador.correo, onValueChange = {}, modifier = Modifier.fillMaxWidth(),
            singleLine = true, readOnly = true, label = { Text(stringResource(R.string.login_email)) }
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(nombres, { nombres = it }, Modifier.fillMaxWidth(), singleLine = true,
            placeholder = { Text(stringResource(R.string.registro_nombres)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text))
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(apellidos, { apellidos = it }, Modifier.fillMaxWidth(), singleLine = true,
            placeholder = { Text(stringResource(R.string.registro_apellidos)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text))
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(telefono, { telefono = it }, Modifier.fillMaxWidth(), singleLine = true,
            placeholder = { Text(stringResource(R.string.registro_telefono_opcional)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = dni,
            onValueChange = { input -> dni = input.filter(Char::isDigit).take(8) },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            label = { Text("DNI (8 dígitos)") },
            supportingText = { Text("Dato declarado por ti; no se verifica automáticamente.") },
            isError = dni.isNotEmpty() && !DniValidator.esValido(dni),
            enabled = !borrador.dniYaAsociado,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(Modifier.height(12.dp))
        if (error != null) {
            Spacer(Modifier.height(12.dp))
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { viewModel.completar(nombres, apellidos, telefono.ifBlank { null }, dni) },
            enabled = state !is CompletarPerfilUiState.Loading && DniValidator.esValido(dni),
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)
        ) {
            if (state is CompletarPerfilUiState.Loading) CircularProgressIndicator()
            else Text(stringResource(R.string.perfil_completar_boton))
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { viewModel.cerrarSesion(onSalir) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.home_cerrar_sesion))
        }
    }
}
