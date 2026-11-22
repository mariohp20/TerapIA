package com.pe.terapia.presentation.cuenta

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pe.terapia.presentation.notifications.RecordatoriosSettingsViewModel
import com.pe.terapia.domain.model.ModoTemaApp
import com.pe.terapia.ui.components.BiometricoSwitchItem
import com.pe.terapia.ui.components.NotificacionesSwitchItem
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionScreen(
    onVolver: () -> Unit,
    viewModel: RecordatoriosSettingsViewModel = koinViewModel(),
    temaViewModel: TemaViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val modoTema by temaViewModel.modo.collectAsStateWithLifecycle()
    Scaffold(topBar = {
        TopAppBar(title = { Text("Configuración") }, navigationIcon = {
            IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver") }
        })
    }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Apariencia", style = MaterialTheme.typography.titleMedium)
            ModoTemaApp.entries.forEach { modo ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { temaViewModel.establecer(modo) }.padding(vertical = 2.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    RadioButton(selected = modoTema == modo, onClick = { temaViewModel.establecer(modo) })
                    Text(modo.etiqueta, style = MaterialTheme.typography.bodyLarge)
                }
            }
            Text("Seguridad y avisos", style = MaterialTheme.typography.titleMedium)
            BiometricoSwitchItem()
            NotificacionesSwitchItem(
                activas = state.activos,
                cargando = state.cargando,
                error = state.error,
                onCambio = viewModel::establecerActivos
            )
            Text("Los recordatorios locales dependen de que las notificaciones estén permitidas en el teléfono.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
