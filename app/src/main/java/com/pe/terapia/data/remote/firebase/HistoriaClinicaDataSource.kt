package com.pe.terapia.data.remote.firebase

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.pe.terapia.domain.model.Atencion
import kotlinx.coroutines.tasks.await

class HistoriaClinicaDataSource(private val firestore: FirebaseFirestore) {

    suspend fun registrarAtencionYCrearHistoriaSiNoExiste(atencion: Atencion) {
        // 1. Buscamos si el paciente ya tiene una historia clínica
        val hcSnapshot = firestore.collection("historiasClinicas")
            .whereEqualTo("pacienteId", atencion.pacienteId)
            .limit(1)
            .get()
            .await()

        val batch = firestore.batch()
        val historiaClinicaId: String

        // 2. Creación condicional
        if (hcSnapshot.isEmpty) {
            // Si no tiene, se crea
            val nuevaHcRef = firestore.collection("historiasClinicas").document()
            historiaClinicaId = nuevaHcRef.id

            val hcData = hashMapOf(
                "id" to historiaClinicaId,
                "pacienteId" to atencion.pacienteId,
                "fechaCreacion" to FieldValue.serverTimestamp(),
                "fechaActualizacion" to FieldValue.serverTimestamp(),
                "activo" to true
            )
            batch.set(nuevaHcRef, hcData)
        } else {
            // Si tiene, solo se actualiza
            val hcExistente = hcSnapshot.documents.first()
            historiaClinicaId = hcExistente.id
            batch.update(hcExistente.reference, "fechaActualizacion", FieldValue.serverTimestamp())
        }

        // 3. Creamos el registro de la Atención Médica
        val nuevaAtencionRef = firestore.collection("atenciones").document()
        val atencionData = hashMapOf(
            "id" to nuevaAtencionRef.id,
            "citaId" to atencion.citaId,
            "pacienteId" to atencion.pacienteId,
            "psicologoId" to atencion.psicologoId,
            "historiaClinicaId" to historiaClinicaId,
            "fechaAtencion" to FieldValue.serverTimestamp(),
            "motivoConsulta" to atencion.motivoConsulta,
            "observaciones" to atencion.observaciones,
            "evaluacion" to atencion.evaluacion,
            "evolucion" to atencion.evolucion,
            "indicaciones" to atencion.indicaciones
        )
        batch.set(nuevaAtencionRef, atencionData)

        // Ejecutamos todo de golpe
        batch.commit().await()
    }

    suspend fun obtenerAtencionesPorPaciente(pacienteId: String): List<Atencion> {
        val snapshot = firestore.collection("atenciones")
            .whereEqualTo("pacienteId", pacienteId)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null

            val timestamp = data["fechaAtencion"] as? com.google.firebase.Timestamp
            val fechaInstant = timestamp?.let { java.time.Instant.ofEpochSecond(it.seconds, it.nanoseconds.toLong()) } ?: java.time.Instant.now()

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
        }.sortedByDescending { it.fechaAtencion }
    }

    suspend fun obtenerPacientesDelPsicologo(psicologoId: String): List<com.pe.terapia.domain.model.PacienteResumen> {
        val citasSnapshot = firestore.collection("citas")
            .whereEqualTo("psicologoId", psicologoId)
            .get()
            .await()

        val pacienteIds = citasSnapshot.documents.mapNotNull { it.getString("pacienteId") }.distinct()

        if (pacienteIds.isEmpty()) return emptyList()

        val pacientes = mutableListOf<com.pe.terapia.domain.model.PacienteResumen>()

        pacienteIds.chunked(10).forEach { chunk ->
            val usuariosSnapshot = firestore.collection("usuarios")
                .whereIn(FieldPath.documentId(), chunk)
                .get()
                .await()

            usuariosSnapshot.documents.forEach { doc ->
                val nombres = doc.getString("nombres") ?: ""
                val apellidos = doc.getString("apellidos") ?: ""
                val nombreCompleto = "$nombres $apellidos".trim()

                pacientes.add(
                    com.pe.terapia.domain.model.PacienteResumen(
                        id = doc.id,
                        nombreCompleto = nombreCompleto.ifBlank { "Paciente Desconocido" }
                    )
                )
            }
        }
        return pacientes
    }
}