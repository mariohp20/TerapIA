package com.pe.terapia.domain.usecase.citas

import com.pe.terapia.domain.repository.FeriadosRepository
import java.time.LocalDate

class ObtenerFeriadosUseCase(private val repository: FeriadosRepository) {
    suspend operator fun invoke(fecha: LocalDate) = repository.obtenerFeriados(fecha.year, "PE")
}
