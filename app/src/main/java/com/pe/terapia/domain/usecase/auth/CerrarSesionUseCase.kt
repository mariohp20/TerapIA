package com.pe.terapia.domain.usecase.auth

import com.pe.terapia.domain.repository.AuthRepository
import kotlinx.coroutines.CancellationException

class CerrarSesionUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): Result<Unit> = try {
        repository.cerrarSesion()
        Result.success(Unit)
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }
}
