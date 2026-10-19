package com.pe.terapia.presentation.buscadorPacientes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.PacienteResumen
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import com.pe.terapia.domain.usecase.historiaclinica.ObtenerPacientesDePsicologoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BuscadorPacientesUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val searchText: String = "",
    val pacientesOriginales: List<PacienteResumen> = emptyList(),
    val pacientesFiltrados: List<PacienteResumen> = emptyList()
)

class BuscadorPacientesViewModel(
    private val obtenerPacientesUseCase: ObtenerPacientesDePsicologoUseCase,
    private val obtenerUsuarioActualUseCase: ObtenerUsuarioActualUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BuscadorPacientesUiState())
    val uiState: StateFlow<BuscadorPacientesUiState> = _uiState.asStateFlow()
    init {
        cargarPacientes()
    }

    private fun cargarPacientes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null)

            val usuarioActual = obtenerUsuarioActualUseCase().getOrNull()

            if (usuarioActual != null) {
                obtenerPacientesUseCase(usuarioActual.uid).fold(
                    onSuccess = { lista ->
                        _uiState.value = _uiState.value.copy(
                            pacientesOriginales = lista,
                            pacientesFiltrados = lista,
                            cargando = false
                        )
                    },
                    onFailure = { excepcion ->
                        _uiState.value = _uiState.value.copy(
                            error = excepcion.message ?: "Ocurrió un problema al cargar tus pacientes.",
                            cargando = false
                        )
                    }
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    error = "No se pudo identificar tu sesión de psicólogo.",
                    cargando = false
                )
            }
        }
    }

    fun onSearchTextChange(text: String) {
        val filtrados = if (text.isBlank()) {
            _uiState.value.pacientesOriginales
        } else {
            _uiState.value.pacientesOriginales.filter {
                it.nombreCompleto.contains(text, ignoreCase = true)
            }
        }

        _uiState.value = _uiState.value.copy(
            searchText = text,
            pacientesFiltrados = filtrados
        )
    }
}