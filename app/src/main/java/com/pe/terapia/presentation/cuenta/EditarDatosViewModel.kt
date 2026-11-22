package com.pe.terapia.presentation.cuenta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.Usuario
import com.pe.terapia.domain.usecase.auth.ActualizarDatosPersonalesUseCase
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditarDatosUiState(
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val usuario: Usuario? = null,
    val error: String? = null,
    val guardado: Boolean = false
)

class EditarDatosViewModel(
    private val obtenerUsuarioActual: ObtenerUsuarioActualUseCase,
    private val actualizarDatosPersonales: ActualizarDatosPersonalesUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditarDatosUiState())
    val uiState = _uiState.asStateFlow()

    init { cargar() }

    fun cargar() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(cargando = true, error = null)
        obtenerUsuarioActual().fold(
            onSuccess = { _uiState.value = EditarDatosUiState(cargando = false, usuario = it) },
            onFailure = { _uiState.value = EditarDatosUiState(cargando = false, error = it.message ?: "No se pudieron cargar tus datos.") }
        )
    }

    fun guardar(nombres: String, apellidos: String, telefono: String) = viewModelScope.launch {
        if (_uiState.value.guardando) return@launch
        _uiState.value = _uiState.value.copy(guardando = true, error = null, guardado = false)
        actualizarDatosPersonales(nombres, apellidos, telefono.ifBlank { null }).fold(
            onSuccess = { _uiState.value = _uiState.value.copy(guardando = false, usuario = it, guardado = true) },
            onFailure = { _uiState.value = _uiState.value.copy(guardando = false, error = it.message ?: "No se pudieron guardar los cambios.") }
        )
    }

    fun limpiarGuardado() { _uiState.value = _uiState.value.copy(guardado = false) }
}
