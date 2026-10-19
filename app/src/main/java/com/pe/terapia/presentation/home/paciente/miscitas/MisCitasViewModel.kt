package com.pe.terapia.presentation.home.paciente.miscitas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.EstadoCita
import com.pe.terapia.domain.model.ProximaCita
import com.pe.terapia.domain.repository.CitaRepository
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MisCitasUiState(
    val cargando: Boolean = true,
    val proximas: List<ProximaCita> = emptyList(),
    val pasadas: List<ProximaCita> = emptyList(),
    val canceladas: List<ProximaCita> = emptyList(),
    val error: String? = null
)

class MisCitasViewModel(
    private val obtenerUsuarioActualUseCase: ObtenerUsuarioActualUseCase,
    private val citaRepository: CitaRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MisCitasUiState())
    val uiState: StateFlow<MisCitasUiState> = _uiState.asStateFlow()

    fun cargarCitas() {
        viewModelScope.launch {
            val usuario = obtenerUsuarioActualUseCase().getOrNull()
            if (usuario == null) {
                _uiState.value = MisCitasUiState(cargando = false, error = "Usuario no identificado.")
                return@launch
            }
            citaRepository.obtenerCitasPaciente(usuario.uid)
                .onSuccess { _uiState.value = clasificar(it) }
                .onFailure { _uiState.value = MisCitasUiState(cargando = false, error = it.message) }
        }
    }

    private fun clasificar(citas: List<ProximaCita>): MisCitasUiState {
        val ahora = System.currentTimeMillis()
        val canceladas = citas.filter { it.estado == EstadoCita.CANCELADA.name }
        val proximas = citas.filter { it.estado == EstadoCita.PROGRAMADA.name && it.inicioEpochMillis > ahora }
        val pasadas = citas.filter { it !in canceladas && it !in proximas }.sortedByDescending { it.inicioEpochMillis }
        return MisCitasUiState(cargando = false, proximas = proximas, pasadas = pasadas, canceladas = canceladas)
    }
}