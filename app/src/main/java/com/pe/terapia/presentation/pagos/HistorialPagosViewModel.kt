package com.pe.terapia.presentation.pagos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.Comprobante
import com.pe.terapia.domain.model.Pago
import com.pe.terapia.domain.model.Rol
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import com.pe.terapia.domain.usecase.pagos.ObtenerHistorialPagosUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HistorialPagosUiState {
    data object Cargando : HistorialPagosUiState
    data class Exito(val pagosConComprobante: List<Pair<Pago, Comprobante?>>) : HistorialPagosUiState
    data class Error(val mensaje: String) : HistorialPagosUiState
}

class HistorialPagosViewModel(
    private val obtenerHistorialPagosUseCase: ObtenerHistorialPagosUseCase,
    private val obtenerUsuarioActualUseCase: ObtenerUsuarioActualUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistorialPagosUiState>(HistorialPagosUiState.Cargando)
    val uiState: StateFlow<HistorialPagosUiState> = _uiState.asStateFlow()

    init {
        cargarHistorial()
    }

    fun cargarHistorial() {
        viewModelScope.launch {
            _uiState.value = HistorialPagosUiState.Cargando
            val usuarioRes = obtenerUsuarioActualUseCase()
            val usuario = usuarioRes.getOrNull()
            if (usuario == null || usuario.rol != Rol.PACIENTE) {
                _uiState.value = HistorialPagosUiState.Error("No se pudo identificar al paciente.")
                return@launch
            }

            obtenerHistorialPagosUseCase(usuario.uid)
                .onSuccess { lista ->
                    _uiState.value = HistorialPagosUiState.Exito(lista)
                }
                .onFailure { error ->
                    _uiState.value = HistorialPagosUiState.Error(error.message ?: "Error al cargar historial de pagos.")
                }
        }
    }
}
