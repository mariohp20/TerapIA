package com.pe.terapia.core.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pe.terapia.domain.model.Rol
import com.pe.terapia.domain.repository.AuthRepository
import com.pe.terapia.presentation.auth.login.LoginScreen
import com.pe.terapia.presentation.auth.recuperar.RecuperarPasswordScreen
import com.pe.terapia.presentation.auth.registro.RegistroScreen
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform.getKoin

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: Route = Route.Login
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable<Route.Login> {
            LoginScreen(
                onLoginExitoso = { usuario ->
                    val destino = when (usuario.rol) {
                        Rol.PACIENTE.valor -> Route.HomePaciente
                        Rol.PSICOLOGO.valor -> Route.HomePsicologo
                        else -> Route.Login
                    }

                    navController.navigate(destino) {
                        popUpTo(Route.Login) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onIrARegistro = {
                    navController.navigate(Route.Registro)
                },
                onIrARecuperar = {
                    navController.navigate(Route.RecuperarPassword)
                }
            )
        }

        composable<Route.Registro> {
            RegistroScreen(
                onRegistroExitoso = {
                    navController.navigate(Route.HomePaciente) {
                        popUpTo(Route.Login) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onIrALogin = {
                    navController.popBackStack()
                }
            )
        }

        composable<Route.RecuperarPassword> {
            RecuperarPasswordScreen(
                onEnviarExitoso = {
                    navController.popBackStack()
                },
                onIrALogin = {
                    navController.popBackStack()
                }
            )
        }

        composable<Route.HomePaciente> {
            val scope = rememberCoroutineScope()
            HomePacienteScreen(
                onCerrarSesion = {
                    val authRepository: AuthRepository = getKoin().get()
                    scope.launch {
                        authRepository.cerrarSesion()
                    }
                    navController.navigate(Route.Login) {
                        popUpTo(Route.HomePaciente) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<Route.HomePsicologo> {
            val scope = rememberCoroutineScope()
            HomePsicologoScreen(
                onCerrarSesion = {
                    val authRepository: AuthRepository = getKoin().get()
                    scope.launch {
                        authRepository.cerrarSesion()
                    }
                    navController.navigate(Route.Login) {
                        popUpTo(Route.HomePsicologo) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@Composable
private fun HomePacienteScreen(onCerrarSesion: () -> Unit) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Home Paciente", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Próxima pantalla del paciente", modifier = Modifier.padding(top = 12.dp))
            Button(onClick = onCerrarSesion, modifier = Modifier.padding(top = 24.dp)) {
                Text("Cerrar sesión")
            }
        }
    }
}

@Composable
private fun HomePsicologoScreen(onCerrarSesion: () -> Unit) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Home Psicólogo", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Próxima pantalla del psicólogo", modifier = Modifier.padding(top = 12.dp))
            Button(onClick = onCerrarSesion, modifier = Modifier.padding(top = 24.dp)) {
                Text("Cerrar sesión")
            }
        }
    }
}