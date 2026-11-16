package com.pe.terapia.domain.repository

import com.pe.terapia.domain.model.Feriado

interface FeriadosRepository {
    suspend fun obtenerFeriados(year: Int, countryCode: String = "PE"): Result<List<Feriado>>
}
