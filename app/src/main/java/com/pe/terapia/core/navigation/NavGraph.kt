package com.pe.terapia.core.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    // Usamos Route.Login como objeto inicial
    NavHost(navController = navController, startDestination = Route.Login) {

        composable<Route.Login> {
            Text("Login - pendiente (Persona 1)")
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