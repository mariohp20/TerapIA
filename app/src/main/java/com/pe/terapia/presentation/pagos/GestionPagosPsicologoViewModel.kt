package com.pe.terapia.presentation.pagos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.Pago
import com.pe.terapia.domain.model.Rol
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import com.pe.terapia.domain.usecase.pagos.ConfirmarPagoClinicaUseCase
import com.pe.terapia.domain.usecase.pagos.ObtenerPagosPendientesPsicologoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface GestionPagosPsicologoUiState {
    data object Cargando : GestionPagosPsicologoUiState
    data class Exito(val pagosPendientes: List<Pago>) : GestionPagosPsicologoUiState
    data class Error(val mensaje: String) : GestionPagosPsicologoUiState
}

class GestionPagosPsicologoViewModel(
    private val obtenerPagosPendientesPsicologoUseCase: ObtenerPagosPendientesPsicologoUseCase,
    private val confirmarPagoClinicaUseCase: ConfirmarPagoClinicaUseCase,
    private val obtenerUsuarioActualUseCase: ObtenerUsuarioActualUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<GestionPagosPsicologoUiState>(GestionPagosPsicologoUiState.Cargando)
    val uiState: StateFlow<GestionPagosPsicologoUiState> = _uiState.asStateFlow()

    init {
        cargarPagosPendientes()
    }

    fun cargarPagosPendientes() {
        viewModelScope.launch {
            _uiState.value = GestionPagosPsicologoUiState.Cargando
            val usuarioRes = obtenerUsuarioActualUseCase()
            val usuario = usuarioRes.getOrNull()
            if (usuario == null || usuario.rol != Rol.PSICOLOGO) {
                _uiState.value = GestionPagosPsicologoUiState.Error("No se pudo identificar al psicólogo.")
                return@launch
            }

            obtenerPagosPendientesPsicologoUseCase(usuario.uid)
                .onSuccess { lista ->
                    _uiState.value = GestionPagosPsicologoUiState.Exito(lista)
                }
                .onFailure { error ->
                    _uiState.value = GestionPagosPsicologoUiState.Error(error.message ?: "Error al cargar pagos pendientes.")
                }
        }
    }

    fun confirmarPagoFisico(pagoId: String) {
        viewModelScope.launch {
            confirmarPagoClinicaUseCase(pagoId)
                .onSuccess {
                    cargarPagosPendientes()
                }
                .onFailure { error ->
                    _uiState.value = GestionPagosPsicologoUiState.Error(error.message ?: "Error al confirmar el pago en clínica.")
                }
        }
    }
}
