package com.pe.terapia.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Path

data class FeriadoDto(
    val date: String? = null,
    val localName: String? = null,
    val name: String? = null,
    val types: List<String>? = null
)

interface FeriadosApi {
    @GET("api/v3/PublicHolidays/{year}/{countryCode}")
    suspend fun obtenerFeriados(
        @Path("year") year: Int,
        @Path("countryCode") countryCode: String
    ): List<FeriadoDto>
}
