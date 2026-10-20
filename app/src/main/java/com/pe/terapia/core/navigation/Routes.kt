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
    data class DetalleCita(val citaId: String) : Route()

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
        val costoCita: Float,
        val inicioEpochMillis: Long
    ) : Route()

    @Serializable

    data object HistorialPagos : Route()

    @Serializable
    data class PasarelaPago(val pagoId: String) : Route()

    @Serializable
    data object GestionPagosPsicologo : Route()

    @Serializable
    data object PromoverUsuario : Route()

    @Serializable
    data object AsignarHorario : Route()

    @Serializable
    data class SalaConsulta(
        val citaId: String,
        val pacienteId: String,
        val pacienteNombre: String
    ) : Route()
    @Serializable
    data class RegistrarAtencion(
        val citaId: String,
        val pacienteId: String
    ) : Route()

    @Serializable
    data class GestionEtapas(
        val procesoId: String
    ) : Route()

    @Serializable
    data class HistorialClinico(
        val pacienteId: String,
        val pacienteNombre: String
    ) : Route()

    @Serializable
    data object BuscadorPacientes : Route()


}
