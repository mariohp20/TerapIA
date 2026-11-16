package com.pe.terapia.data.repository

import com.pe.terapia.data.remote.api.FeriadosApi
import com.pe.terapia.domain.model.Feriado
import com.pe.terapia.domain.repository.FeriadosRepository
import kotlinx.coroutines.CancellationException

class FeriadosRepositoryImpl(private val api: FeriadosApi) : FeriadosRepository {
    override suspend fun obtenerFeriados(year: Int, countryCode: String): Result<List<Feriado>> = try {
        require(year in 2020..2100 && countryCode.length == 2)
        Result.success(api.obtenerFeriados(year, countryCode).mapNotNull { dto ->
            dto.date?.take(10)?.let { Feriado(it, dto.localName ?: dto.name ?: "Feriado") }
        })
    } catch (cancel: CancellationException) {
        throw cancel
    } catch (error: Exception) {
        Result.failure(error)
    }
}
