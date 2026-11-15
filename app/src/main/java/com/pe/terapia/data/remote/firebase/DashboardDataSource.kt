package com.pe.terapia.data.remote.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.pe.terapia.domain.model.AtencionPsicologoResumen
import com.pe.terapia.domain.model.AtencionResumenDashboard
import com.pe.terapia.domain.model.Cita
import com.pe.terapia.domain.model.EstadoCita
import com.pe.terapia.domain.model.DURACION_CITA_MINUTOS
import com.pe.terapia.domain.model.EstadoEtapa
import com.pe.terapia.domain.model.CitaAdminResumen
import com.pe.terapia.domain.model.PagoAdminResumen
import com.pe.terapia.domain.model.PsicologoAdminResumen
import com.pe.terapia.domain.model.DashboardAdminData
import com.pe.terapia.domain.model.EstadoProceso
import com.pe.terapia.domain.model.EtapaPsicologoResumen
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class DashboardDataSource(
    private val firestore: FirebaseFirestore
) {

    // 1. PSICOLOGO

    // 1.1 ColumnChar: Volumen de Atenciones
    suspend fun obtenerAtencionesPorPsicologo(psicologoId: String): List<AtencionPsicologoResumen> {
        val snapshot = firestore.collection("atenciones")
            .whereEqualTo("psicologoId", psicologoId)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val timestamp = doc.getTimestamp("fechaAtencion") ?: return@mapNotNull null
            AtencionPsicologoResumen(
                citaId = doc.getString("citaId").orEmpty(),
                fechaAtencionEpochMillis = timestamp.toDate().time
            )
        }
    }

    // 1.2 PieChart: Estado de Pacientes Activos
    suspend fun obtenerEtapasActivasPorPsicologo(psicologoId: String): List<EtapaPsicologoResumen> {
        val procesosSnapshot = firestore.collection("procesosTerapeuticos")
            .whereEqualTo("psicologoId", psicologoId)
            .whereEqualTo("estado", EstadoProceso.EN_CURSO.name)
            .get()
            .await()

        val procesoIds = procesosSnapshot.documents.map { it.id }.distinct()

        if (procesoIds.isEmpty()) return emptyList()

        val etapasActivasResumen = mutableListOf<EtapaPsicologoResumen>()

        procesoIds.chunked(10).forEach { chunk ->
            val etapasSnapshot = firestore.collection("etapasClinicas")
                .whereIn("procesoTerapeuticoId", chunk)
                .whereEqualTo("estado", EstadoEtapa.EN_PROGRESO.name)
                .get()
                .await()

            etapasSnapshot.documents.forEach { doc ->
                etapasActivasResumen.add(
                    EtapaPsicologoResumen(
                        tipoEtapaId = doc.getString("tipoEtapaId").orEmpty()
                    )
                )
            }
        }

        return etapasActivasResumen
    }

    // 1.3 LineChart: Adherencia y Ausentismo
    suspend fun obtenerCitasPorPsicologo(psicologoId: String): List<CitaAdminResumen> {
        val snapshot = firestore.collection("citas")
            .whereEqualTo("psicologoId", psicologoId)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            CitaAdminResumen(
                psicologoId = doc.getString("psicologoId") ?: return@mapNotNull null,
                inicioEpochMillis = (doc.getLong("inicioEpochMillis")) ?: return@mapNotNull null,
                estado = doc.getString("estado").orEmpty()
            )
        }
    }

    // 2. PACIENTE

    suspend fun obtenerAtencionesPorPaciente(pacienteId: String): List<AtencionResumenDashboard> {
        require(pacienteId.isNotBlank()) { "No se pudo identificar al paciente." }
        // Las atenciones existentes pueden preceder al nuevo cambio que marca Cita.COMPLETADA.
        // Se leen solo los datos mínimos para el dashboard y siempre se filtra por el paciente autenticado.
        val snapshot = firestore.collection("atenciones")
            .whereEqualTo("pacienteId", pacienteId)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val timestamp = doc.getTimestamp("fechaAtencion") ?: return@mapNotNull null
            AtencionResumenDashboard(
                citaId = doc.getString("citaId").orEmpty(),
                fechaAtencion = java.time.Instant.ofEpochSecond(timestamp.seconds, timestamp.nanoseconds.toLong())
            )
        }
    }

    suspend fun obtenerCitasPorPaciente(pacienteId: String): List<Cita> {
        require(pacienteId.isNotBlank()) { "No se pudo identificar al paciente." }
        val snapshot = firestore.collection("citas")
            .whereEqualTo("pacienteId", pacienteId)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            Cita(
                id = doc.id,
                reservaId = data["reservaId"] as? String ?: "",
                pacienteId = data["pacienteId"] as? String ?: pacienteId,
                psicologoId = data["psicologoId"] as? String ?: "",
                sedeId = data["sedeId"] as? String ?: "",
                especialidadId = data["especialidadId"] as? String ?: "",
                fecha = data["fecha"] as? String ?: "",
                hora = data["hora"] as? String ?: "",
                inicioEpochMillis = (data["inicioEpochMillis"] as? Number)?.toLong() ?: 0L,
                duracionMinutos = (data["duracionMinutos"] as? Number)?.toInt() ?: DURACION_CITA_MINUTOS,
                estado = runCatching { EstadoCita.valueOf(data["estado"] as? String ?: "") }
                    .getOrDefault(EstadoCita.PROGRAMADA)
            )
        }
    }

    // 3. ADMIN

    suspend fun obtenerDatosAdmin(desdeEpochMillis: Long, hastaEpochMillis: Long): DashboardAdminData = coroutineScope {
        val citasDeferred = async {
            firestore.collection("citas")
                .whereGreaterThanOrEqualTo("inicioEpochMillis", desdeEpochMillis)
                .whereLessThanOrEqualTo("inicioEpochMillis", hastaEpochMillis)
                .get().await().documents.mapNotNull { doc ->
                    CitaAdminResumen(
                        psicologoId = doc.getString("psicologoId") ?: return@mapNotNull null,
                        inicioEpochMillis = doc.getLong("inicioEpochMillis") ?: return@mapNotNull null,
                        estado = doc.getString("estado").orEmpty()
                    )
                }
        }
        val pagosDeferred = async {
            val inicio = Timestamp(java.util.Date(desdeEpochMillis))
            val fin = Timestamp(java.util.Date(hastaEpochMillis))
            firestore.collection("pagos")
                .whereEqualTo("estado", "PAGADO")
                .whereGreaterThanOrEqualTo("creadaEn", inicio)
                .whereLessThanOrEqualTo("creadaEn", fin)
                .get().await().documents.mapNotNull { doc ->
                    val monto = doc.getDouble("monto") ?: return@mapNotNull null
                    val timestamp = doc.getTimestamp("creadaEn") ?: return@mapNotNull null
                    PagoAdminResumen(timestamp.toDate().time, monto, doc.getString("moneda") ?: "PEN")
                }
        }
        val psicologosDeferred = async {
            firestore.collection("psicologos").whereEqualTo("activo", true).get().await().documents.map { doc ->
                val usuarioId = doc.getString("usuarioId") ?: doc.id
                val usuario = firestore.collection("usuarios").document(usuarioId).get().await()
                val nombres = usuario.getString("nombres").orEmpty()
                val apellidos = usuario.getString("apellidos").orEmpty()
                PsicologoAdminResumen(usuarioId, "$nombres $apellidos".trim().ifBlank { "Psicólogo ${usuarioId.take(6)}" })
            }
        }
        DashboardAdminData(citasDeferred.await(), pagosDeferred.await(), psicologosDeferred.await())
    }

}
