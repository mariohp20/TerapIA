package com.pe.terapia.presentation.home.paciente

import androidx.annotation.StringRes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pe.terapia.R
import com.pe.terapia.presentation.home.paciente.chat.TerapiaChatScreen
import com.pe.terapia.presentation.home.paciente.inicio.HomePacienteScreen
import com.pe.terapia.presentation.home.paciente.miscitas.MisCitasScreen
import com.pe.terapia.presentation.home.paciente.perfil.PerfilScreen
import kotlinx.serialization.Serializable

sealed class PacienteTab {
    @Serializable data object Inicio : PacienteTab()
    @Serializable data object MisCitas : PacienteTab()
    @Serializable data object Asistente : PacienteTab()
    @Serializable data object Perfil : PacienteTab()
}

private data class TabPaciente(
    val ruta: PacienteTab,
    @StringRes val etiqueta: Int,
    val icono: ImageVector,
    val iconoSeleccionado: ImageVector
)

private val tabsPaciente = listOf(
    TabPaciente(PacienteTab.Inicio, R.string.paciente_tab_inicio, Icons.Outlined.Home, Icons.Filled.Home),
    TabPaciente(PacienteTab.MisCitas, R.string.paciente_tab_mis_citas, Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth),
    TabPaciente(PacienteTab.Asistente, R.string.paciente_tab_asistente, Icons.Outlined.Chat, Icons.Filled.Chat),
    TabPaciente(PacienteTab.Perfil, R.string.paciente_tab_perfil, Icons.Outlined.Person, Icons.Filled.Person)
)

@Composable
fun PacienteShell(
    onReservarCita: () -> Unit,
    onVerHistorialPagos: () -> Unit,
    onCerrarSesion: () -> Unit,
    onVerHistorial: (String, String) -> Unit,
    onVerDetalleCita: (String) -> Unit,
    mensajeError: String? = null
) {
    val tabNavController = rememberNavController()
    val entradaActual by tabNavController.currentBackStackEntryAsState()
    val destinoActual = entradaActual?.destination

    val irATab: (PacienteTab) -> Unit = { tab ->
        tabNavController.navigate(tab) {
            popUpTo(tabNavController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                tabsPaciente.forEach { tab ->
                    val seleccionada = destinoActual?.hierarchy?.any { it.hasRoute(tab.ruta::class) } == true
                    NavigationBarItem(
                        selected = seleccionada,
                        onClick = { irATab(tab.ruta) },
                        icon = { Icon(if (seleccionada) tab.iconoSeleccionado else tab.icono, contentDescription = null) },
                        label = { Text(stringResource(tab.etiqueta)) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = tabNavController,
            startDestination = PacienteTab.Inicio,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(200)) },
            popEnterTransition = { fadeIn(tween(200)) },
            popExitTransition = { fadeOut(tween(200)) }
        ) {
            composable<PacienteTab.Inicio> {
                HomePacienteScreen(
                    onReservarCita = onReservarCita,
                    onVerHistorialPagos = onVerHistorialPagos,
                    onCerrarSesion = onCerrarSesion,
                    onVerHistorial = onVerHistorial,
                    onVerTodas = { irATab(PacienteTab.MisCitas) }, // Conectamos Ver Todas -> Mis Citas
                    onVerDetalle = onVerDetalleCita, // Conectamos el clic en la tarjeta
                    mensajeError = mensajeError
                )
            }
            composable<PacienteTab.MisCitas> {
                MisCitasScreen(
                    onGestionarCita = onReservarCita,
                    onVerDetalleCita = onVerDetalleCita
                )
            }
            composable<PacienteTab.Asistente> {
                TerapiaChatScreen()
            }
            composable<PacienteTab.Perfil> {
                PerfilScreen(
                    onCerrarSesion = onCerrarSesion,
                    onVerHistorialClinico = onVerHistorial,
                    onVerPagos = onVerHistorialPagos
                )
            }
        }
    }
}