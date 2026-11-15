package com.pe.terapia.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.usecase.auth.CerrarSesionUseCase
import com.pe.terapia.domain.usecase.auth.CompletarPerfilUseCase
import com.pe.terapia.domain.model.PerfilPacienteBorrador
import com.pe.terapia.domain.usecase.auth.ObtenerBorradorPerfilUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CompletarPerfilUiState {
    data object Idle : CompletarPerfilUiState
    data object Loading : CompletarPerfilUiState
    data class Error(val mensaje: String) : CompletarPerfilUiState
}

class CompletarPerfilViewModel(
    private val completarPerfilUseCase: CompletarPerfilUseCase,
    private val cerrarSesionUseCase: CerrarSesionUseCase,
    obtenerBorradorPerfilUseCase: ObtenerBorradorPerfilUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<CompletarPerfilUiState>(CompletarPerfilUiState.Idle)
    val uiState: StateFlow<CompletarPerfilUiState> = _uiState.asStateFlow()
    private val _borrador = MutableStateFlow(PerfilPacienteBorrador())
    val borrador: StateFlow<PerfilPacienteBorrador> = _borrador.asStateFlow()
    private val _uiEvents = MutableSharedFlow<CompletarPerfilUiEvent>(extraBufferCapacity = 1)
    val uiEvents: SharedFlow<CompletarPerfilUiEvent> = _uiEvents

    init {
        viewModelScope.launch {
            obtenerBorradorPerfilUseCase().onSuccess { _borrador.value = it }
        }
    }

    fun completar(nombres: String, apellidos: String, telefono: String?, dni: String) {
        viewModelScope.launch {
            _uiState.value = CompletarPerfilUiState.Loading
            completarPerfilUseCase(nombres, apellidos, telefono, dni)
                .onSuccess {
                    _uiState.value = CompletarPerfilUiState.Idle
                    _uiEvents.emit(CompletarPerfilUiEvent.PerfilCompletado(it))
                }
                .onFailure { _uiState.value = CompletarPerfilUiState.Error(it.message ?: "No se pudo completar el perfil.") }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            cerrarSesionUseCase()
                .onSuccess { _uiEvents.emit(CompletarPerfilUiEvent.SesionCerrada) }
                .onFailure {
                    _uiState.value = CompletarPerfilUiState.Error(
                        it.message ?: "No se pudo cerrar la sesión."
                    )
                }
        }
    }
}

sealed interface CompletarPerfilUiEvent {
    data object SesionCerrada : CompletarPerfilUiEvent
    data class PerfilCompletado(val usuario: com.pe.terapia.domain.model.Usuario) : CompletarPerfilUiEvent
}
