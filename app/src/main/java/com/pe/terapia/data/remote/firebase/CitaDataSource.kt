package com.pe.terapia.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.pe.terapia.domain.model.Cita
import kotlinx.coroutines.tasks.await

class CitaDataSource (private val firestore: FirebaseFirestore) {
    suspend fun obtenerDisponibilidad(psicologoId: String, fecha: String): List<String> {
        val snapshot = firestore.collection("citas")
            .whereEqualTo("psicologoId", psicologoId)
            .whereEqualTo("fecha", fecha)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.getString("hora") }
    }

    suspend fun reservarCita(cita: Cita): Boolean {
        val documentoRef = firestore.collection("citas").document()
        val citaConId = cita.copy(id = documentoRef.id)
        documentoRef.set(citaConId).await()
        return true
    }
}