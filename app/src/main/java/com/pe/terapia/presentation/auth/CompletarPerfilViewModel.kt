package com.pe.terapia.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.Usuario
import com.pe.terapia.domain.usecase.auth.CerrarSesionUseCase
import com.pe.terapia.domain.usecase.auth.CompletarPerfilUseCase
import com.pe.terapia.domain.model.PerfilPacienteBorrador
import com.pe.terapia.domain.usecase.auth.ObtenerBorradorPerfilUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CompletarPerfilUiState {
    data object Idle : CompletarPerfilUiState
    data object Loading : CompletarPerfilUiState
    data class Success(val usuario: Usuario) : CompletarPerfilUiState
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

    init {
        viewModelScope.launch {
            obtenerBorradorPerfilUseCase().onSuccess { _borrador.value = it }
        }
    }

    fun completar(nombres: String, apellidos: String, telefono: String?, dni: String) {
        viewModelScope.launch {
            _uiState.value = CompletarPerfilUiState.Loading
            completarPerfilUseCase(nombres, apellidos, telefono, dni)
                .onSuccess { _uiState.value = CompletarPerfilUiState.Success(it) }
                .onFailure { _uiState.value = CompletarPerfilUiState.Error(it.message ?: "No se pudo completar el perfil.") }
        }
    }

    fun cerrarSesion(onCerrada: () -> Unit) {
        viewModelScope.launch {
            cerrarSesionUseCase().onSuccess { onCerrada() }
        }
    }
}
