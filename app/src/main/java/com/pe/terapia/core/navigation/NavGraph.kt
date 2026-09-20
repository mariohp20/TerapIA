package com.pe.terapia.core.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pe.terapia.presentation.auth.login.LoginScreen

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    // Usamos Route.Login como objeto inicial
    NavHost(navController = navController, startDestination = Route.Login) {

        composable<Route.Login> {
            LoginScreen(
                onLoginExitoso = {
                    // Si entra aquí significa que el token
                    // se validó correctamente en Firebase.
                    println("¡ÉXITO! Google Sign In funcionó.")
                    // Si quieres ver si navega, puedes forzarlo a una ruta de prueba:
                    // navController.navigate(Route.HomePsicologo)
                },
                onIrARegistro = {
                    navController.navigate(Route.Registro)
                },
                onIrARecuperar = {
                    /* No haces nada por ahora */
                }
            )

        }

        composable<Route.Registro> {
            Text("Registro - pendiente (Persona 1)")
        }

        composable<Route.HomePaciente> {
            Text("Home Paciente - pendiente")
        }

        composable<Route.HomePsicologo> {
            Text("Home Psicólogo - pendiente")
        }
    }
}