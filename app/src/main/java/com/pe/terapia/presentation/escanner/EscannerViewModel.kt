package com.pe.terapia.presentation.escanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

sealed interface EscannerUiState {
    data object Idle : EscannerUiState
    data object Cargando : EscannerUiState
    data class CitaEscaneada(
        val citaId: String,
        val pacienteId: String,
        val fecha: String,
        val hora: String,
        val especialidadId: String,
        val estado: String
    ) : EscannerUiState
    data class AsistenciaConfirmada(val mensaje: String) : EscannerUiState
    data class Error(val mensaje: String) : EscannerUiState
}

class EscannerViewModel(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow<EscannerUiState>(EscannerUiState.Idle)
    val uiState: StateFlow<EscannerUiState> = _uiState.asStateFlow()

    fun procesarCodigoScaneado(codigoCita: String) {
        if (codigoCita.isBlank()) {
            _uiState.value = EscannerUiState.Error("Código QR vacío o inválido.")
            return
        }

        viewModelScope.launch {
            _uiState.value = EscannerUiState.Cargando
            try {
                val docRef = firestore.collection("citas").document(codigoCita.trim())
                val snapshot = docRef.get().await()

                if (!snapshot.exists()) {
                    _uiState.value = EscannerUiState.Error("Cita no encontrada con el código: $codigoCita")
                    return@launch
                }

                val citaId = snapshot.id
                val pacienteId = snapshot.getString("pacienteId").orEmpty()
                val fecha = snapshot.getString("fecha").orEmpty()
                val hora = snapshot.getString("hora").orEmpty()
                val especialidadId = snapshot.getString("especialidadId").orEmpty()
                val estado = snapshot.getString("estado") ?: "PROGRAMADA"

                _uiState.value = EscannerUiState.CitaEscaneada(
                    citaId = citaId,
                    pacienteId = pacienteId,
                    fecha = fecha,
                    hora = hora,
                    especialidadId = especialidadId,
                    estado = estado
                )
            } catch (e: Exception) {
                _uiState.value = EscannerUiState.Error(e.message ?: "Error al verificar la cita.")
            }
        }
    }

    fun confirmarAsistencia(citaId: String) {
        viewModelScope.launch {
            _uiState.value = EscannerUiState.Cargando
            try {
                firestore.collection("citas").document(citaId)
                    .update("estado", "COMPLETADA")
                    .await()

                _uiState.value = EscannerUiState.AsistenciaConfirmada("¡Asistencia confirmada y cita marcada como completada exitosamente!")
            } catch (e: Exception) {
                _uiState.value = EscannerUiState.Error(e.message ?: "Error al confirmar asistencia.")
            }
        }
    }

    fun reiniciar() {
        _uiState.value = EscannerUiState.Idle
    }
}
