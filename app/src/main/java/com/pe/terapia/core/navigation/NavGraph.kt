package com.pe.terapia.core.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import androidx.navigation.toRoute
import com.pe.terapia.domain.model.Rol
import com.pe.terapia.domain.model.Usuario
import com.pe.terapia.presentation.auth.AuthUiState
import com.pe.terapia.presentation.auth.AuthViewModel
import com.pe.terapia.presentation.auth.login.LoginScreen
import com.pe.terapia.presentation.auth.CompletarPerfilScreen
import com.pe.terapia.presentation.auth.recuperar.RecuperarPasswordScreen
import com.pe.terapia.presentation.auth.registro.RegistroScreen
import com.pe.terapia.presentation.citas.confirmacion.ConfirmacionCitaScreen
import com.pe.terapia.presentation.citas.reserva.ReservaCitaScreen
import com.pe.terapia.presentation.home.admin.HomeAdminScreen
import com.pe.terapia.presentation.admin.promover.PromoverUsuarioScreen
import com.pe.terapia.presentation.home.paciente.HomePacienteScreen
import com.pe.terapia.presentation.home.psicologo.HomePsicologoScreen
import com.pe.terapia.presentation.navigation.HomeAccessUiState
import com.pe.terapia.presentation.navigation.HomeAccessViewModel
import com.pe.terapia.presentation.splash.SplashScreen
import com.pe.terapia.presentation.splash.SplashUiState
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: Route = Route.Splash
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable<Route.Splash>(
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { -it / 8 },
                    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(600))
            }
        ) {
            SplashScreen(onSessionResolved = { state ->
                val destination = when (state) {
                    is SplashUiState.SessionResolved -> state.usuario?.let(::homeRoute) ?: Route.Login()
                    is SplashUiState.SessionError -> Route.Login(state.mensaje)
                    SplashUiState.ProfileIncomplete -> Route.CompletarPerfil()
                    SplashUiState.Loading -> null
                }
                destination?.let { navController.navigateReplacingCurrent(it) }
            })
        }

        composable<Route.Login>(
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it / 8 },
                    animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(800))
            },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) }
        ) { entry ->
            val route = entry.toRoute<Route.Login>()
            LoginScreen(
                onLoginExitoso = { usuario -> navController.navigateHome(usuario) },
                onPerfilPendiente = { message -> navController.navigateReplacingCurrent(Route.CompletarPerfil(message)) },
                onIrARegistro = { navController.navigate(Route.Registro) },
                onIrARecuperar = { navController.navigate(Route.RecuperarPassword) },
                mensajeInicial = route.mensajeInicial
            )
        }

        composable<Route.CompletarPerfil> { entry ->
            val route = entry.toRoute<Route.CompletarPerfil>()
            CompletarPerfilScreen(
                onCompletado = { usuario -> navController.navigateHome(usuario) },
                onSalir = { navController.navigateReplacingCurrent(Route.Login()) },
                mensajeInicial = route.mensajeInicial
            )
        }

        composable<Route.Registro>(
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            RegistroScreen(
                onRegistroExitoso = {
                    navController.navigate(Route.HomePaciente) {
                        popUpTo<Route.Login> { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onPerfilPendiente = { message -> navController.navigateReplacingCurrent(Route.CompletarPerfil(message)) },
                onIrALogin = { navController.popBackStack() }
            )
        }

        composable<Route.RecuperarPassword>(
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            RecuperarPasswordScreen(
                onEnviarExitoso = { navController.popBackStack() },
                onIrALogin = { navController.popBackStack() }
            )
        }

        composable<Route.HomePaciente>(
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it / 8 },
                    animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(800))
            },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) }
        ) {
            ProtectedHomeRoute(Rol.PACIENTE, navController) { authViewModel, error ->
                HomePacienteScreen(
                    onReservarCita = { navController.navigate(Route.ReservaCita) },
                    onCerrarSesion = authViewModel::cerrarSesion,
                    mensajeError = error
                )
            }
        }

        composable<Route.ReservaCita>(
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            RoleProtectedRoute(Rol.PACIENTE, navController) {
                ReservaCitaScreen(
                    onContinuar = { psicologo, especialidadId, especialidadNombre, sede, slot ->
                        navController.navigate(
                            Route.ConfirmacionCita(
                                psicologoId = psicologo.id,
                                psicologoNombre = "${psicologo.nombres} ${psicologo.apellidos}",
                                especialidadId = especialidadId,
                                especialidadNombre = especialidadNombre,
                                sedeId = sede.id,
                                sedeNombre = sede.nombre,
                                sedeDireccion = listOf(sede.direccion, sede.distrito, sede.ciudad)
                                    .filter(String::isNotBlank).distinct().joinToString(", "),
                                fecha = slot.fecha,
                                hora = slot.hora,
                                horarioId = slot.horarioId,
                                bloqueId = slot.bloqueId,
                                inicioEpochMillis = slot.inicioEpochMillis
                            )
                        )
                    },
                    onVolver = { navController.popBackStack() }
                )
            }
        }

        composable<Route.ConfirmacionCita>(
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) { entry ->
            val args = entry.toRoute<Route.ConfirmacionCita>()
            RoleProtectedRoute(Rol.PACIENTE, navController) {
                ConfirmacionCitaScreen(
                    psicologoId = args.psicologoId,
                    psicologoNombre = args.psicologoNombre,
                    especialidadId = args.especialidadId,
                    especialidadNombre = args.especialidadNombre,
                    sedeId = args.sedeId,
                    sedeNombre = args.sedeNombre,
                    sedeDireccion = args.sedeDireccion,
                    fecha = args.fecha,
                    hora = args.hora,
                    horarioId = args.horarioId,
                    bloqueId = args.bloqueId,
                    inicioEpochMillis = args.inicioEpochMillis,
                    onConfirmada = {
                        navController.navigate(Route.HomePaciente) {
                            popUpTo<Route.HomePaciente> { inclusive = true }
                        }
                    },
                    onVolver = { navController.popBackStack() }
                )
            }
        }

        composable<Route.HomePsicologo>(
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it / 8 },
                    animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(800))
            },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            ProtectedHomeRoute(Rol.PSICOLOGO, navController) { authViewModel, _ ->
                HomePsicologoScreen(
                    onCerrarSesion = authViewModel::cerrarSesion,
                    onRegistrarAtencion = { citaId, pacienteId ->
                        navController.navigate(Route.RegistrarAtencion(citaId, pacienteId))
                    },
                    onIrAHistorialClinico = {
                        navController.navigate(Route.BuscadorPacientes)
                    }
                )
            }
        }

        composable<Route.RegistrarAtencion>(
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) { entry ->
            val args = entry.toRoute<Route.RegistrarAtencion>()

            RoleProtectedRoute(Rol.PSICOLOGO, navController) {
                com.pe.terapia.presentation.atencion.RegistrarAtencionScreen(
                    citaId = args.citaId,
                    pacienteId = args.pacienteId,
                    onVolver = { navController.popBackStack() },
                    onGuardadoExitoso = { navController.popBackStack() }
                )
            }
        }

        composable<Route.BuscadorPacientes>(
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            RoleProtectedRoute(Rol.PSICOLOGO, navController) {
                com.pe.terapia.presentation.buscadorPacientes.BuscadorPacientesScreen(
                    onVolver = { navController.popBackStack() },
                    onPacienteSeleccionado = { pacienteId, pacienteNombre ->
                        navController.navigate(Route.HistorialClinico(pacienteId, pacienteNombre))
                    }
                )
            }
        }

        composable<Route.HistorialClinico>(
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) { entry ->
            val args = entry.toRoute<Route.HistorialClinico>()

            RoleProtectedRoute(Rol.PSICOLOGO, navController) {
                com.pe.terapia.presentation.historial.HistorialClinicoScreen(
                    pacienteId = args.pacienteId,
                    pacienteNombre = args.pacienteNombre,
                    onVolver = { navController.popBackStack() }
                )
            }
        }

        composable<Route.HomeAdmin> (
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it / 8 },
                    animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(800))
            },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            ProtectedHomeRoute(Rol.ADMIN, navController) { authViewModel, error ->
                HomeAdminScreen(
                    onPromoverUsuario = { navController.navigate(Route.PromoverUsuario) },
                    onAsignarHorario = { navController.navigate(Route.AsignarHorario) },
                    onCerrarSesion = authViewModel::cerrarSesion,
                    mensajeError = error
                )
            }
        }

        composable<Route.PromoverUsuario>(
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            RoleProtectedRoute(Rol.ADMIN, navController) {
                // Asumiendo que crearás este Composable próximamente
                PromoverUsuarioScreen(
                    onVolver = { navController.popBackStack() },
                    onPromocionExitosa = {
                        // Opcional: mostrar un mensaje y regresar
                        navController.popBackStack()
                    }
                )
            }
        }

        composable<Route.AsignarHorario>(
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            RoleProtectedRoute(Rol.ADMIN, navController) {
                com.pe.terapia.presentation.admin.horario.AsignarHorarioScreen(
                    onVolver = { navController.popBackStack() }
                )
            }
        }


    }
}

@Composable
private fun ProtectedHomeRoute(
    rolRequerido: Rol,
    navController: NavHostController,
    content: @Composable (AuthViewModel, String?) -> Unit
) {
    val authViewModel: AuthViewModel = koinViewModel()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(authState) {
        if (authState is AuthUiState.SignedOut) {
            navController.navigateReplacingCurrent(Route.Login())
        }
    }

    RoleProtectedRoute(rolRequerido, navController) {
        when {
            authState !is AuthUiState.Loading && authState !is AuthUiState.SignedOut ->
                content(authViewModel, (authState as? AuthUiState.Error)?.mensaje)
            else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun RoleProtectedRoute(
    rolRequerido: Rol,
    navController: NavHostController,
    content: @Composable () -> Unit
) {
    val accessViewModel: HomeAccessViewModel = koinViewModel { parametersOf(rolRequerido) }
    val accessState by accessViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(accessState) {
        val redirect = accessState as? HomeAccessUiState.Redirect ?: return@LaunchedEffect
        val destination = when {
            redirect.completarPerfil -> Route.CompletarPerfil(redirect.mensaje)
            redirect.rol != null -> homeRoute(redirect.rol)
            else -> Route.Login(redirect.mensaje)
        }
        navController.navigateReplacingCurrent(destination)
    }

    if (accessState is HomeAccessUiState.Authorized) {
        content()
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

private fun homeRoute(usuario: Usuario): Route = homeRoute(usuario.rol)

private fun homeRoute(rol: Rol): Route = when (rol) {
    Rol.PACIENTE -> Route.HomePaciente
    Rol.PSICOLOGO -> Route.HomePsicologo
    Rol.ADMIN -> Route.HomeAdmin
}

private fun NavHostController.navigateReplacingCurrent(destination: Route) {
    val currentDestinationId = currentDestination?.id
    val options = navOptions {
        if (currentDestinationId != null) popUpTo(currentDestinationId) { inclusive = true }
        launchSingleTop = true
    }
    when (destination) {
        is Route.Login -> navigate(destination, options)
        is Route.CompletarPerfil -> navigate(destination, options)
        Route.HomePaciente -> navigate(Route.HomePaciente, options)
        Route.HomePsicologo -> navigate(Route.HomePsicologo, options)
        Route.HomeAdmin -> navigate(Route.HomeAdmin, options)
        else -> error("Destino no permitido para la navegación de sesión: $destination")
    }
}

private fun NavHostController.navigateHome(usuario: Usuario) {
    navigateReplacingCurrent(homeRoute(usuario))
}
