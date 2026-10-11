package com.pe.terapia.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.Rol
import com.pe.terapia.domain.model.PerfilIncompletoException
import com.pe.terapia.domain.usecase.auth.VerificarSesionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeAccessViewModel(
    private val verificarSesionUseCase: VerificarSesionUseCase,
    private val rolRequerido: Rol
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeAccessUiState>(HomeAccessUiState.Checking)
    val uiState: StateFlow<HomeAccessUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = verificarSesionUseCase().fold(
                onSuccess = { usuario ->
                    when {
                        usuario == null -> HomeAccessUiState.Redirect()
                        usuario.rol == rolRequerido && usuario.activo -> HomeAccessUiState.Authorized
                        else -> HomeAccessUiState.Redirect(rol = usuario.rol)
                    }
                },
                onFailure = { exception ->
                    HomeAccessUiState.Redirect(
                        mensaje = exception.message ?: "No se pudo validar el acceso. Inicia sesión nuevamente.",
                        completarPerfil = exception is PerfilIncompletoException
                    )
                }
            )
        }
    }
}

sealed interface HomeAccessUiState {
    data object Checking : HomeAccessUiState
    data object Authorized : HomeAccessUiState
    data class Redirect(val rol: Rol? = null, val mensaje: String? = null, val completarPerfil: Boolean = false) : HomeAccessUiState
}
