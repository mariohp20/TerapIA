package com.pe.terapia.domain.usecase.auth

import com.pe.terapia.domain.model.Usuario
import com.pe.terapia.domain.repository.AuthRepository

class ActualizarDatosPersonalesUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(nombres: String, apellidos: String, telefono: String?): Result<Usuario> =
        repository.actualizarDatosPersonales(nombres, apellidos, telefono)
}
