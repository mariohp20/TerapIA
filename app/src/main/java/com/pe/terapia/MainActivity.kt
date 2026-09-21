package com.pe.terapia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.pe.terapia.core.navigation.NavGraph
import com.pe.terapia.core.navigation.Route
import com.pe.terapia.domain.model.Rol
import com.pe.terapia.domain.repository.AuthRepository
import com.pe.terapia.ui.theme.TerapiaTheme
import org.koin.mp.KoinPlatform.getKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var startDestination by remember { mutableStateOf<Route>(Route.Login) }

            LaunchedEffect(Unit) {
                startDestination = resolverRutaInicio()
            }

            TerapiaTheme {
                NavGraph(startDestination = startDestination)
            }
        }
    }

    private suspend fun resolverRutaInicio(): Route {
        val authRepository: AuthRepository = getKoin().get()
        val usuario = authRepository.obtenerUsuarioActual() ?: return Route.Login

        return when (usuario.rol) {
            Rol.PACIENTE.valor -> Route.HomePaciente
            Rol.PSICOLOGO.valor -> Route.HomePsicologo
            else -> Route.Login
        }
    }
}