package com.pe.terapia.presentation.home.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.SupervisedUserCircle
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pe.terapia.presentation.home.admin.components.AdminHeader
import com.pe.terapia.presentation.home.admin.components.AdminHerramientaButton
import com.pe.terapia.ui.theme.TerapiaTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeAdminScreen(
    onPromoverUsuario: () -> Unit,
    onCerrarSesion: () -> Unit,
    onAsignarHorario: () -> Unit,
    onDashboard: () -> Unit,
    onGestionarPsicologo: () -> Unit,
    mensajeError: String? = null,
    viewModel: HomeAdminViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeAdminContent(
        uiState = uiState,
        mensajeError = mensajeError,
        onPromoverUsuario = onPromoverUsuario,
        onAsignarHorario = onAsignarHorario,
        onDashboard = onDashboard,
        onGestionarPsicologo = onGestionarPsicologo,
        onCerrarSesion = onCerrarSesion
    )
}

@Composable
private fun HomeAdminContent(
    uiState: HomeAdminUiState,
    mensajeError: String?,
    onPromoverUsuario: () -> Unit,
    onAsignarHorario: () -> Unit,
    onDashboard: () -> Unit,
    onGestionarPsicologo: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {

        AdminHeader(
            nombreAdmin = uiState.nombreAdmin,
            onCerrarSesion = onCerrarSesion
        )

        if (uiState.cargando) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            (mensajeError ?: uiState.error)?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Herramientas de Gestión",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold
                )

                AdminHerramientaButton(
                    titulo = "Promover Usuario a Psicólogo",
                    icono = Icons.Outlined.SupervisedUserCircle,
                    onClick = onPromoverUsuario
                )

                AdminHerramientaButton(
                    titulo = "Asignar Horarios a Psicólogos",
                    icono = Icons.Outlined.CalendarMonth,
                    onClick = onAsignarHorario
                )

                AdminHerramientaButton(
                    titulo = "Gestionar Perfiles de Psicólogos",
                    icono = Icons.Outlined.ManageAccounts,
                    onClick = onGestionarPsicologo
                )

                AdminHerramientaButton(
                    titulo = "Dashboard de la clínica",
                    icono = Icons.Outlined.BarChart,
                    onClick = onDashboard
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeAdminPreview() {
    TerapiaTheme {
        HomeAdminContent(
            uiState = HomeAdminUiState(nombreAdmin = "Carlos", cargando = false),
            mensajeError = null,
            onPromoverUsuario = {},
            onAsignarHorario = {},
            onDashboard = {},
            onGestionarPsicologo = {},
            onCerrarSesion = {}
        )
    }
}