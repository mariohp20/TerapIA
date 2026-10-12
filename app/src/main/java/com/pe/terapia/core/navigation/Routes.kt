package com.pe.terapia.core.navigation

import kotlinx.serialization.Serializable

sealed class Route {

    @Serializable
    data object Splash : Route()
    @Serializable
    data class Login(val mensajeInicial: String? = null) : Route()

    @Serializable
    data object Registro : Route()

    @Serializable
    data class CompletarPerfil(val mensajeInicial: String? = null) : Route()

    @Serializable
    data object RecuperarPassword : Route()

    @Serializable
    data object HomePaciente : Route()

    @Serializable
    data object HomePsicologo : Route()

    @Serializable
    data object HomeAdmin : Route()

    @Serializable
    data object ReservaCita : Route()

    @Serializable
    data class ConfirmacionCita(
        val psicologoId: String,
        val psicologoNombre: String,
        val especialidadId: String,
        val especialidadNombre: String,
        val sedeId: String,
        val sedeNombre: String,
        val sedeDireccion: String = "",
        val fecha: String,
        val hora: String,
        val horarioId: String,
        val bloqueId: String,
        val inicioEpochMillis: Long
    ) : Route()

    @Serializable
    data object PromoverUsuario : Route()
}
