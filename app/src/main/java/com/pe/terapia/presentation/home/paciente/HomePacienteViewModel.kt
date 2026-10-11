package com.pe.terapia.presentation.home.paciente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.ProximaCita
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import com.pe.terapia.domain.usecase.citas.ObtenerProximaCitaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomePacienteUiState(
    val nombrePaciente: String = "",
    val proximaCita: ProximaCita? = null,
    val cargando: Boolean = true,
    val error: String? = null
)

class HomePacienteViewModel(
    private val obtenerUsuarioActual: ObtenerUsuarioActualUseCase,
    private val obtenerProximaCita: ObtenerProximaCitaUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomePacienteUiState())
    val uiState: StateFlow<HomePacienteUiState> = _uiState.asStateFlow()

    init {
        cargarHome()
    }

    private fun cargarHome() {
        viewModelScope.launch {
            _uiState.value = HomePacienteUiState(cargando = true)
            obtenerUsuarioActual().fold(
                onSuccess = { usuario ->
                    if (usuario == null) {
                        _uiState.value = HomePacienteUiState(cargando = false, error = "No se pudo identificar la sesión del paciente.")
                        return@fold
                    }
                    val currentName = usuario.nombres.ifBlank { "" }
                    obtenerProximaCita(usuario.uid).fold(
                        onSuccess = { cita ->
                            _uiState.value = HomePacienteUiState(nombrePaciente = currentName, proximaCita = cita, cargando = false)
                        },
                        onFailure = { error ->
                            _uiState.value = HomePacienteUiState(nombrePaciente = currentName, cargando = false, error = error.message)
                        }
                    )
                },
                onFailure = { error ->
                    _uiState.value = HomePacienteUiState(cargando = false, error = error.message)
                }
            )
        }
    }
}
