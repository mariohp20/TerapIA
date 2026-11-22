package com.pe.terapia.presentation.cuenta

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun EditarDatosScreen(onVolver: () -> Unit, viewModel: EditarDatosViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var nombres by rememberSaveable { mutableStateOf("") }
    var apellidos by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(state.usuario?.uid) {
        state.usuario?.let { nombres = it.nombres; apellidos = it.apellidos; telefono = it.telefono.orEmpty() }
    }
    LaunchedEffect(state.guardado) { if (state.guardado) { viewModel.limpiarGuardado(); onVolver() } }

    Scaffold(topBar = {
        TopAppBar(title = { Text("Mis datos") }, navigationIcon = {
            IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver") }
        })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Mantén actualizados tus datos de contacto.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (state.cargando) CircularProgressIndicator()
            else {
                OutlinedTextField(nombres, { nombres = it }, label = { Text("Nombres") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(apellidos, { apellidos = it }, label = { Text("Apellidos") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(telefono, { telefono = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                HorizontalDivider()
                OutlinedTextField(state.usuario?.correo.orEmpty(), {}, label = { Text("Correo") }, modifier = Modifier.fillMaxWidth(), enabled = false, singleLine = true)
                state.usuario?.dni?.takeIf(String::isNotBlank)?.let { dni ->
                    OutlinedTextField(dni, {}, label = { Text("DNI") }, modifier = Modifier.fillMaxWidth(), enabled = false, singleLine = true)
                }
                Text("Para cambiar el correo o DNI, comunícate con la clínica.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = { viewModel.guardar(nombres, apellidos, telefono) }, enabled = !state.cargando && !state.guardando, modifier = Modifier.fillMaxWidth()) {
                if (state.guardando) CircularProgressIndicator(Modifier.padding(end = 8.dp))
                Text(if (state.guardando) "Guardando…" else "Guardar cambios")
            }
        }
    }
}
