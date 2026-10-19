package com.pe.terapia.presentation.citas.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.ProximaCita
import com.pe.terapia.domain.repository.CitaRepository
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetalleCitaUiState(
    val cargando: Boolean = true,
    val cita: ProximaCita? = null,
    val error: String? = null
)

class DetalleCitaViewModel(
    private val citaId: String,
    private val obtenerUsuarioActualUseCase: ObtenerUsuarioActualUseCase,
    private val citaRepository: CitaRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DetalleCitaUiState())
    val uiState: StateFlow<DetalleCitaUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val usuario = obtenerUsuarioActualUseCase().getOrNull()
            if (usuario == null) {
                _uiState.value = DetalleCitaUiState(cargando = false, error = "Usuario no identificado.")
                return@launch
            }
            citaRepository.obtenerCitasPaciente(usuario.uid)
                .onSuccess { lista ->
                    val cita = lista.firstOrNull { it.id == citaId }
                    _uiState.value = DetalleCitaUiState(
                        cargando = false,
                        cita = cita,
                        error = if (cita == null) "No se encontró la cita." else null
                    )
                }
                .onFailure {
                    _uiState.value = DetalleCitaUiState(cargando = false, error = it.message)
                }
        }
    }
}