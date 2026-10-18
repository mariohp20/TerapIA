package com.pe.terapia.data.remote.firebase

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import com.pe.terapia.domain.model.Atencion
import com.pe.terapia.domain.model.EstadoEtapa
import com.pe.terapia.domain.model.EstadoProceso
import com.pe.terapia.domain.model.TipoEtapa
import kotlinx.coroutines.tasks.await
import android.util.Log


class HistoriaClinicaDataSource(private val firestore: FirebaseFirestore) {

    suspend fun obtenerAtencionPorCita(citaId: String): Atencion? {
        val snapshot = firestore.collection("atenciones")
            .whereEqualTo("citaId", citaId)
            .limit(1)
            .get()
            .await()

        if (snapshot.isEmpty) return null

        val doc = snapshot.documents.first()
        val data = doc.data ?: return null

        val timestamp = data["fechaAtencion"] as? com.google.firebase.Timestamp
        val fechaInstant = timestamp?.let { java.time.Instant.ofEpochSecond(it.seconds, it.nanoseconds.toLong()) } ?: java.time.Instant.now()

        return Atencion(
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


    suspend fun registrarAtencionYProcesoSiNoExiste(atencion: Atencion) {
        val batch = firestore.batch()

        try {
            // 1. Obtener o crear Historia Clínica
            val historiaClinicaId = gestionarHistoriaClinica(batch, atencion.pacienteId)
            Log.d("FirebaseDebug", "Paso 1 Completado. ID: $historiaClinicaId")

            // 2. Obtener o crear Proceso Terapéutico
            val procesoId = gestionarProcesoTerapeutico(
                batch = batch,
                pacienteId = atencion.pacienteId,
                psicologoId = atencion.psicologoId,
                historiaClinicaId = historiaClinicaId,
                motivoConsulta = atencion.motivoConsulta
            )
            Log.d("FirebaseDebug", "Paso 2 Completado. ID: $procesoId")

            // 3. Obtener o crear Etapa Clínica Activa
            val etapaId = gestionarEtapaClinica(batch, procesoId)
            Log.d("FirebaseDebug", "Paso 3 Completado. ID: $etapaId")

            // 4. Crear o actualizar  la Atención Médica
            hacerUpsertAtencion(
                batch = batch,
                atencion = atencion,
                historiaClinicaId = historiaClinicaId,
                procesoId = procesoId,
                etapaId = etapaId
            )
            Log.d("FirebaseDebug", "Paso 4 Completado.")

            // 5. Ejecutar toda la transacción junta
            batch.commit().await()
            Log.d("FirebaseDebug", "Paso 5 Completado.")
        } catch (e: Exception) {
            Log.e("FirebaseDebug", "ERROR EN FIREBASE: ${e.message}", e)
            throw e
        }
    }

    suspend fun obtenerTiposEtapas(): List<TipoEtapa> {
        val snapshot = firestore.collection("tiposEtapas")
            .whereEqualTo("activo", true)
            .orderBy("orden")
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            TipoEtapa(
                id = doc.id,
                nombre = doc.getString("nombre") ?: "",
                descripcion = doc.getString("descripcion") ?: "",
                orden = doc.getLong("orden")?.toInt() ?: 0,
                activo = doc.getBoolean("activo") ?: true
            )
        }
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


    // FUNCIONES AUXILIARES
    private suspend fun gestionarHistoriaClinica(batch: WriteBatch, pacienteId: String): String {
        val hcSnapshot = firestore.collection("historiasClinicas")
            .whereEqualTo("pacienteId", pacienteId)
            .limit(1)
            .get()
            .await()

        return if (hcSnapshot.isEmpty) {
            val nuevaHcRef = firestore.collection("historiasClinicas").document()
            val hcData = hashMapOf(
                "id" to nuevaHcRef.id,
                "pacienteId" to pacienteId,
                "fechaCreacion" to FieldValue.serverTimestamp(),
                "fechaActualizacion" to FieldValue.serverTimestamp(),
                "activo" to true
            )
            batch.set(nuevaHcRef, hcData)
            nuevaHcRef.id // Retorna el ID nuevo
        } else {
            val hcExistente = hcSnapshot.documents.first()
            batch.update(hcExistente.reference, "fechaActualizacion", FieldValue.serverTimestamp())
            hcExistente.id // Retorna el ID existente
        }
    }

    private suspend fun gestionarProcesoTerapeutico(
        batch: WriteBatch,
        pacienteId: String,
        psicologoId: String,
        historiaClinicaId: String,
        motivoConsulta: String
    ): String {
        val procesoSnapshot = firestore.collection("procesosTerapeuticos")
            .whereEqualTo("pacienteId", pacienteId)
            .whereEqualTo("psicologoId", psicologoId)
            .whereEqualTo("estado", EstadoProceso.EN_CURSO.name)
            .limit(1)
            .get()
            .await()

        return if (procesoSnapshot.isEmpty) {
            val nuevoProcesoRef = firestore.collection("procesosTerapeuticos").document()
            val procesoData = hashMapOf(
                "id" to nuevoProcesoRef.id,
                "historiaClinicaId" to historiaClinicaId,
                "pacienteId" to pacienteId,
                "psicologoId" to psicologoId,
                "estado" to EstadoProceso.EN_CURSO.name,
                "fechaInicio" to FieldValue.serverTimestamp(),
                "fechaFin" to null,
                "motivoConsultaPrincipal" to motivoConsulta
            )
            batch.set(nuevoProcesoRef, procesoData)
            nuevoProcesoRef.id
        } else {
            procesoSnapshot.documents.first().id
        }
    }

    private suspend fun gestionarEtapaClinica(batch: WriteBatch, procesoId: String): String {
        val etapaSnapshot = firestore.collection("etapasClinicas")
            .whereEqualTo("procesoTerapeuticoId", procesoId)
            .whereEqualTo("estado", EstadoEtapa.EN_PROGRESO.name)
            .limit(1)
            .get()
            .await()

        return if (etapaSnapshot.isEmpty) {
            // Buscar el ID en el catálogo de tiposEtapas
            val tipoEtapaSnapshot = firestore.collection("tiposEtapas")
                .orderBy("orden")
                .limit(1)
                .get()
                .await()

            val tipoEtapaId = if (!tipoEtapaSnapshot.isEmpty) tipoEtapaSnapshot.documents.first().id else "tipo-1-evaluacion"

            val nuevaEtapaRef = firestore.collection("etapasClinicas").document()
            val etapaData = hashMapOf(
                "id" to nuevaEtapaRef.id,
                "procesoTerapeuticoId" to procesoId,
                "tipoEtapaId" to tipoEtapaId,
                "estado" to EstadoEtapa.EN_PROGRESO.name,
                "fechaInicio" to FieldValue.serverTimestamp(),
                "fechaFin" to null
            )
            batch.set(nuevaEtapaRef, etapaData)
            nuevaEtapaRef.id
        } else {
            etapaSnapshot.documents.first().id
        }
    }

    private suspend fun hacerUpsertAtencion(
        batch: WriteBatch,
        atencion: Atencion,
        historiaClinicaId: String,
        procesoId: String,
        etapaId: String
    ) {
        val atencionSnapshot = firestore.collection("atenciones")
            .whereEqualTo("citaId", atencion.citaId)
            .limit(1)
            .get()
            .await()

        val atencionRef = if (atencionSnapshot.isEmpty) {
            firestore.collection("atenciones").document()
        } else {
            atencionSnapshot.documents.first().reference
        }

        val atencionData = mutableMapOf<String, Any>(
            "id" to atencionRef.id,
            "citaId" to atencion.citaId,
            "pacienteId" to atencion.pacienteId,
            "psicologoId" to atencion.psicologoId,
            "historiaClinicaId" to historiaClinicaId,
            "procesoTerapeuticoId" to procesoId,
            "etapaClinicaId" to etapaId,
            "motivoConsulta" to atencion.motivoConsulta,
            "observaciones" to atencion.observaciones,
            "evaluacion" to atencion.evaluacion,
            "evolucion" to atencion.evolucion,
            "indicaciones" to atencion.indicaciones
        )

        if (atencionSnapshot.isEmpty) {
            atencionData["fechaAtencion"] = FieldValue.serverTimestamp()
            batch.set(atencionRef, atencionData)
        } else {
            batch.update(atencionRef, atencionData)
        }
    }


}