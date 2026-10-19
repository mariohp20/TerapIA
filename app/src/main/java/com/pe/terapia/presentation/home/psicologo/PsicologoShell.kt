package com.pe.terapia.presentation.home.psicologo

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pe.terapia.presentation.home.psicologo.inicio.HomePsicologoScreen
import kotlinx.serialization.Serializable
import com.pe.terapia.presentation.home.psicologo.perfil.PerfilPsicologoScreen

sealed class PsicologoTab {
    @Serializable data object Inicio : PsicologoTab()
    @Serializable data object Agenda : PsicologoTab()
    @Serializable data object Disponibilidad : PsicologoTab()
    @Serializable data object Perfil : PsicologoTab()
}

private data class TabPsicologoItem(
    val ruta: PsicologoTab,
    val etiqueta: String,
    val icono: ImageVector,
    val iconoSeleccionado: ImageVector
)

private val tabsPsicologo = listOf(
    TabPsicologoItem(PsicologoTab.Inicio, "Inicio", Icons.Outlined.Home, Icons.Filled.Home),
    TabPsicologoItem(PsicologoTab.Agenda, "Agenda", Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth),
    TabPsicologoItem(PsicologoTab.Disponibilidad, "Disponibilidad", Icons.Outlined.AccessTime, Icons.Filled.AccessTime),
    TabPsicologoItem(PsicologoTab.Perfil, "Perfil", Icons.Outlined.Person, Icons.Filled.Person)
)

@Composable
fun PsicologoShell(
    onGestionarPagos: () -> Unit,
    onCerrarSesion: () -> Unit,
    onRegistrarAtencion: (citaId: String, pacienteId: String) -> Unit,
    onIrAHistorialClinico: () -> Unit
) {
    val tabNavController = rememberNavController()
    val entradaActual by tabNavController.currentBackStackEntryAsState()
    val destinoActual = entradaActual?.destination

    val irATab: (PsicologoTab) -> Unit = { tab ->
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
                tabsPsicologo.forEach { tab ->
                    val seleccionada = destinoActual?.hierarchy?.any { it.hasRoute(tab.ruta::class) } == true
                    NavigationBarItem(
                        selected = seleccionada,
                        onClick = { irATab(tab.ruta) },
                        icon = { Icon(if (seleccionada) tab.iconoSeleccionado else tab.icono, contentDescription = null) },
                        label = { Text(tab.etiqueta) },
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
            startDestination = PsicologoTab.Inicio,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(200)) },
            popEnterTransition = { fadeIn(tween(200)) },
            popExitTransition = { fadeOut(tween(200)) }
        ) {
            composable<PsicologoTab.Inicio> {
                HomePsicologoScreen(
                    onGestionarPagos = onGestionarPagos,
                    onCerrarSesion = onCerrarSesion,
                    onRegistrarAtencion = onRegistrarAtencion,
                    onIrAHistorialClinico = onIrAHistorialClinico
                )
            }
            composable<PsicologoTab.Agenda> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Mi Agenda", style = MaterialTheme.typography.titleMedium)
                }
            }
            composable<PsicologoTab.Disponibilidad> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Disponibilidad de Horarios", style = MaterialTheme.typography.titleMedium)
                }
            }
            composable<PsicologoTab.Perfil> {
                PerfilPsicologoScreen(
                    onCerrarSesion = onCerrarSesion,
                    onGestionarPagos = onGestionarPagos,
                    onIrAHistorialClinico = onIrAHistorialClinico
                )
            }
        }
    }
}