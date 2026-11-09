package com.pe.terapia.domain.repository

import com.pe.terapia.domain.model.Atencion
import com.pe.terapia.domain.model.EtapaClinica

interface DashboardRepository {
    // Métodos para el Psicólogo
    suspend fun obtenerAtencionesPsicologo(psicologoId: String): Result<List<Atencion>>
    suspend fun obtenerEtapasActivasPsicologo(psicologoId: String): Result<List<EtapaClinica>>

    // Métodos para el Paciente


    // Métodos para el Administrador (Sin filtros de ID, trae todo)

}