package com.pe.terapia.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.pe.terapia.domain.repository.HistoriaClinicaRepository
import kotlinx.coroutines.tasks.await

class HistoriaClinicaRepositoryImpl (private val firestore: FirebaseFirestore) : HistoriaClinicaRepository {
    override suspend fun verificarOCrearHistoria(pacienteId: String): Result<Boolean> {
        return try {
            // Usamos el ID del paciente como el ID del documento de su historia clínica
            val historiaRef = firestore.collection("historias").document(pacienteId)
            val snapshot = historiaRef.get().await()

            // Si el documento NO existe, significa que es un paciente nuevo
            if (!snapshot.exists()) {
                val nuevaHistoria = hashMapOf(
                    "pacienteId" to pacienteId,
                    "fechaApertura" to System.currentTimeMillis(),
                    "notas" to emptyList<String>() // Lista vacía para futuras citas
                )
                historiaRef.set(nuevaHistoria).await()
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}