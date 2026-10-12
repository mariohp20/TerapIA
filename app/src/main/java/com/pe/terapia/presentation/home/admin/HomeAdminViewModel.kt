package com.pe.terapia.presentation.home.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeAdminUiState(
    val nombreAdmin: String = "",
    val cargando: Boolean = true,
    val error: String? = null
)

class HomeAdminViewModel(
    private val obtenerUsuarioActual: ObtenerUsuarioActualUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeAdminUiState())
    val uiState: StateFlow<HomeAdminUiState> = _uiState.asStateFlow()

    init {
        cargarDatosAdmin()
    }

    private fun cargarDatosAdmin() {
        viewModelScope.launch {
            _uiState.value = HomeAdminUiState(cargando = true)
            obtenerUsuarioActual().fold(
                onSuccess = { usuario ->
                    if (usuario == null) {
                        _uiState.value = HomeAdminUiState(
                            cargando = false,
                            error = "No se pudo identificar la sesión del administrador."
                        )
                        return@fold
                    }
                    _uiState.value = HomeAdminUiState(
                        nombreAdmin = usuario.nombres.ifBlank { "" },
                        cargando = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = HomeAdminUiState(
                        cargando = false,
                        error = error.message
                    )
                }
            )
        }
    }
}