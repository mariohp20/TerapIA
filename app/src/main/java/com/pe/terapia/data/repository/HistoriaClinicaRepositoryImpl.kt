package com.pe.terapia.data.repository

import com.pe.terapia.data.remote.firebase.HistoriaClinicaDataSource
import com.pe.terapia.domain.model.Atencion
import com.pe.terapia.domain.model.TipoEtapa
import com.pe.terapia.domain.repository.HistoriaClinicaRepository
import com.pe.terapia.domain.model.EtapaClinica
import com.pe.terapia.domain.model.ProcesoTerapeutico

class HistoriaClinicaRepositoryImpl(
    private val dataSource: HistoriaClinicaDataSource
) : HistoriaClinicaRepository {

    override suspend fun obtenerAtencionPorCita(citaId: String):Result<Atencion?> = runCatching {
        dataSource.obtenerAtencionPorCita(citaId)
    }

    override suspend fun registrarAtencion(atencion: Atencion): Result<Unit> = runCatching {
        dataSource.registrarAtencionYProcesoSiNoExiste(atencion)
    }

    override suspend fun obtenerTiposEtapas(): Result<List<TipoEtapa>> = runCatching {
        dataSource.obtenerTiposEtapas()
    }

    override suspend fun transicionarEtapaClinica (procesoId: String,etapaActualId: String,nuevoTipoEtapaId: String,observacionesCierre: String): Result<Unit> =runCatching {
        dataSource.transicionarEtapaClinica(procesoId,etapaActualId,nuevoTipoEtapaId,observacionesCierre)
    }

    override suspend fun obtenerProcesoActivo(pacienteId: String): Result<ProcesoTerapeutico?> = runCatching {
        dataSource.obtenerProcesoActivo(pacienteId)
    }

    override suspend fun obtenerEtapasPorProceso(procesoId: String): Result<List<EtapaClinica>> = runCatching {
        dataSource.obtenerEtapasPorProceso(procesoId)
    }

    override suspend fun obtenerAtencionesPorPaciente(pacienteId: String): Result<List<Atencion>> = runCatching {
        dataSource.obtenerAtencionesPorPaciente(pacienteId)
    }
    override suspend fun obtenerPacientesDelPsicologo(psicologoId: String): Result<List<com.pe.terapia.domain.model.PacienteResumen>> = runCatching {
        dataSource.obtenerPacientesDelPsicologo(psicologoId)
    }
}
