package com.pe.terapia.data.remote.firebase

import com.google.firebase.firestore.FieldValue
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
}