package com.pe.terapia.core.navigation

import kotlinx.serialization.Serializable

sealed class Route {
    @Serializable
    data object Login : Route()

    @Serializable
    data object Registro : Route()

    @Serializable
    data object RecuperarPassword : Route()

    @Serializable
    data object HomePaciente : Route()

    @Serializable
    data object HomePsicologo : Route()
}