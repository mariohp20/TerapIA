package com.pe.terapia.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.pe.terapia.domain.repository.HistoriaClinicaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class HistoriaClinicaRepositoryImpl(
    private val firestore: FirebaseFirestore
) : HistoriaClinicaRepository {
    override suspend fun verificarOCrearHistoria(pacienteId: String): Result<Boolean> = try {
        val historiaRef = firestore.collection("historiasClinicas").document(pacienteId)
        val snapshot = historiaRef.get().await()
        if (!snapshot.exists()) {
            historiaRef.set(
                mapOf(
                    "pacienteId" to pacienteId,
                    "fechaApertura" to System.currentTimeMillis(),
                    "notas" to emptyList<String>()
                )
            ).await()
        }
        Result.success(true)
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }
}
