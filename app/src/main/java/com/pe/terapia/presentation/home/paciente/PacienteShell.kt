package com.pe.terapia.presentation.home.paciente

import androidx.annotation.StringRes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.Alignment
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
import com.pe.terapia.presentation.home.paciente.inicio.HomePacienteScreen
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
    mensajeError: String? = null
) {
    val tabNavController = rememberNavController()
    val entradaActual by tabNavController.currentBackStackEntryAsState()
    val destinoActual = entradaActual?.destination

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                tabsPaciente.forEach { tab ->
                    val seleccionada = destinoActual?.hierarchy?.any { it.hasRoute(tab.ruta::class) } == true
                    NavigationBarItem(
                        selected = seleccionada,
                        onClick = {
                            tabNavController.navigate(tab.ruta) {
                                popUpTo(tabNavController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
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
                    mensajeError = mensajeError
                )
            }
            composable<PacienteTab.MisCitas> { TabEnConstruccion(R.string.paciente_tab_mis_citas) }
            composable<PacienteTab.Asistente> { TabEnConstruccion(R.string.paciente_tab_asistente) }
            composable<PacienteTab.Perfil> { TabEnConstruccion(R.string.paciente_tab_perfil) }
        }
    }
}

@Composable
private fun TabEnConstruccion(@StringRes titulo: Int) {
    Box(Modifier.fillMaxSize().statusBarsPadding(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(titulo), style = MaterialTheme.typography.titleLarge)
            Text(
                stringResource(R.string.paciente_tab_proximamente),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}