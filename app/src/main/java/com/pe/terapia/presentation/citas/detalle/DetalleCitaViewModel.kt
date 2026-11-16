package com.pe.terapia.presentation.citas.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.ProximaCita
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import com.pe.terapia.domain.usecase.citas.ObtenerCitasPacienteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DetalleCitaUiState {
    data object Cargando : DetalleCitaUiState
    data class Contenido(
        val cita: ProximaCita,
        val pacienteId: String,
        val pacienteNombre: String
    ) : DetalleCitaUiState
    data class Error(val mensaje: String) : DetalleCitaUiState
}

class DetalleCitaViewModel(
    private val citaId: String,
    private val obtenerUsuarioActualUseCase: ObtenerUsuarioActualUseCase,
    private val obtenerCitasPacienteUseCase: ObtenerCitasPacienteUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<DetalleCitaUiState>(DetalleCitaUiState.Cargando)
    val uiState: StateFlow<DetalleCitaUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val usuario = obtenerUsuarioActualUseCase().getOrNull()
            if (usuario == null) {
                _uiState.value = DetalleCitaUiState.Error("Usuario no identificado.")
                return@launch
            }
            obtenerCitasPacienteUseCase(usuario.uid)
                .onSuccess { lista ->
                    val cita = lista.firstOrNull { it.id == citaId }
                    _uiState.value = cita?.let {
                        DetalleCitaUiState.Contenido(it, usuario.uid, usuario.nombreCompleto)
                    } ?: DetalleCitaUiState.Error("No se encontró la cita.")
                }
                .onFailure {
                    _uiState.value = DetalleCitaUiState.Error(it.message ?: "No se pudo cargar la cita.")
                }
        }
    }
}
