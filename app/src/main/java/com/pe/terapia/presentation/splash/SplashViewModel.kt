package com.pe.terapia.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.usecase.auth.VerificarSesionUseCase
import com.pe.terapia.domain.model.PerfilIncompletoException
import com.pe.terapia.domain.model.Usuario
import com.pe.terapia.domain.usecase.auth.CerrarSesionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel(
    private val verificarSesionUseCase: VerificarSesionUseCase,
    private val cerrarSesionUseCase: CerrarSesionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()
    private val _uiEvents = MutableSharedFlow<SplashUiEvent>(extraBufferCapacity = 1)
    val uiEvents: SharedFlow<SplashUiEvent> = _uiEvents

    init {
        verificarSesion()
    }

    private fun verificarSesion() {
        viewModelScope.launch {
            _uiState.value = verificarSesionUseCase().fold(
                onSuccess = { usuario -> SplashUiState.SessionResolved(usuario) },
                onFailure = { exception ->
                    if (exception is PerfilIncompletoException) SplashUiState.ProfileIncomplete
                    else SplashUiState.SessionError(exception.message ?: "No se pudo validar la sesión. Inicia sesión nuevamente.")
                }
            )
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            cerrarSesionUseCase()
                .onSuccess { _uiEvents.emit(SplashUiEvent.SesionCerrada) }
                .onFailure {
                    _uiEvents.emit(SplashUiEvent.ErrorAlCerrarSesion(
                        it.message ?: "No se pudo cerrar la sesión."
                    ))
                }
        }
    }
}

sealed interface SplashUiEvent {
    data object SesionCerrada : SplashUiEvent
    data class ErrorAlCerrarSesion(val mensaje: String) : SplashUiEvent
}

sealed interface SplashUiState {
    data object Loading : SplashUiState
    data object ProfileIncomplete : SplashUiState
    data class SessionResolved(val usuario: Usuario?) : SplashUiState
    data class SessionError(val mensaje: String) : SplashUiState
}
