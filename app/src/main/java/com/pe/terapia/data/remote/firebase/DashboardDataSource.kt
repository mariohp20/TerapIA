package com.pe.terapia.data.remote.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.pe.terapia.domain.model.Atencion
import com.pe.terapia.domain.model.Cita
import com.pe.terapia.domain.model.EstadoCita
import com.pe.terapia.domain.model.DURACION_CITA_MINUTOS
import com.pe.terapia.domain.model.EstadoEtapa
import com.pe.terapia.domain.model.EstadoProceso
import com.pe.terapia.domain.model.EtapaClinica
import kotlinx.coroutines.tasks.await

class DashboardDataSource(
    private val firestore: FirebaseFirestore
) {

    // 1. PSICOLOGO

    // 1.1 ColumnChar: Volumen de Atenciones
    suspend fun obtenerAtencionesPorPsicologo(psicologoId: String): List<Atencion> {
        val snapshot = firestore.collection("atenciones")
            .whereEqualTo("psicologoId", psicologoId)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null

            val timestamp = data["fechaAtencion"] as? com.google.firebase.Timestamp
            val fechaInstant = timestamp?.let {
                java.time.Instant.ofEpochSecond(it.seconds, it.nanoseconds.toLong())
            } ?: java.time.Instant.now()

            Atencion(
                citaId = data["citaId"] as? String ?: "",
                pacienteId = data["pacienteId"] as? String ?: "",
                psicologoId = data["psicologoId"] as? String ?: "",
                historiaClinicaId = data["historiaClinicaId"] as? String ?: "",
                fechaAtencion = fechaInstant,
                motivoConsulta = data["motivoConsulta"] as? String ?: "",
                observaciones = data["observaciones"] as? String ?: "",
                evaluacion = data["evaluacion"] as? String ?: "",
                evolucion = data["evolucion"] as? String ?: "",
                indicaciones = data["indicaciones"] as? String ?: ""
            )
        }
    }

    // 1.2 PieChart: Estado de Pacientes Activos
    suspend fun obtenerEtapasActivasPorPsicologo(psicologoId: String): List<EtapaClinica> {
        val procesosSnapshot = firestore.collection("procesosTerapeuticos")
            .whereEqualTo("psicologoId", psicologoId)
            .whereEqualTo("estado", EstadoProceso.EN_CURSO.name)
            .get()
            .await()

        val procesoIds = procesosSnapshot.documents.map { it.id }.distinct()

        if (procesoIds.isEmpty()) return emptyList()

        val etapasActivas = mutableListOf<EtapaClinica>()

        procesoIds.chunked(10).forEach { chunk ->
            val etapasSnapshot = firestore.collection("etapasClinicas")
                .whereIn("procesoTerapeuticoId", chunk)
                .whereEqualTo("estado", EstadoEtapa.EN_PROGRESO.name)
                .get()
                .await()

            etapasSnapshot.documents.forEach { doc ->
                val data = doc.data ?: return@forEach

                val timestampInicio = data["fechaInicio"] as? com.google.firebase.Timestamp
                val fechaInicioInstant = timestampInicio?.let {
                    java.time.Instant.ofEpochSecond(it.seconds, it.nanoseconds.toLong())
                }

                etapasActivas.add(
                    EtapaClinica(
                        id = doc.id,
                        procesoTerapeuticoId = data["procesoTerapeuticoId"] as? String ?: "",
                        tipoEtapaId = data["tipoEtapaId"] as? String ?: "",
                        estado = EstadoEtapa.valueOf(data["estado"] as? String ?: EstadoEtapa.PENDIENTE.name),
                        fechaInicio = fechaInicioInstant,
                        fechaFin = null,
                        objetivosEspecificos = data["objetivosEspecificos"] as? String,
                        observaciones = data["observaciones"] as? String
                    )
                )
            }
        }

        return etapasActivas
    }
    // 1.3 LineChart: Adherencia y Ausentismo
    suspend fun obtenerCitasPorPsicologo(psicologoId: String): List<Cita> {
        val snapshot = firestore.collection("citas")
            .whereEqualTo("psicologoId", psicologoId)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null

            Cita(
                id = doc.id,
                reservaId = data["reservaId"] as? String ?: "",
                pacienteId = data["pacienteId"] as? String ?: "",
                psicologoId = data["psicologoId"] as? String ?: "",
                sedeId = data["sedeId"] as? String ?: "",
                especialidadId = data["especialidadId"] as? String ?: "",
                fecha = data["fecha"] as? String ?: "",
                hora = data["hora"] as? String ?: "",
                inicioEpochMillis = data["inicioEpochMillis"] as? Long ?: 0L,
                duracionMinutos = (data["duracionMinutos"] as? Number)?.toInt() ?: DURACION_CITA_MINUTOS,
                estado = runCatching { EstadoCita.valueOf(data["estado"] as? String ?: "") }
                    .getOrDefault(EstadoCita.PROGRAMADA)
            )
        }
    }

    // 2. PACIENTE

    // 3. ADMIN

}