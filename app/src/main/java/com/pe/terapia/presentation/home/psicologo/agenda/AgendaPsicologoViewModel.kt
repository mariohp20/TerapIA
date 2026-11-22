package com.pe.terapia.presentation.home.psicologo.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.CitaPacienteDetalle
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import com.pe.terapia.domain.usecase.citas.ObtenerAgendaPsicologoPorFechaUseCase
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class AgendaPsicologoUiState(
    val fecha: LocalDate = LocalDate.now(ZoneId.of("America/Lima")),
    val cargando: Boolean = true,
    val citas: List<CitaPacienteDetalle> = emptyList(),
    val error: String? = null
)

class AgendaPsicologoViewModel(
    private val obtenerUsuarioActual: ObtenerUsuarioActualUseCase,
    private val obtenerAgenda: ObtenerAgendaPsicologoPorFechaUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(AgendaPsicologoUiState())
    val uiState = _uiState.asStateFlow()
    private var psicologoId: String? = null
    private var cargaJob: Job? = null

    init {
        viewModelScope.launch {
            psicologoId = obtenerUsuarioActual().getOrNull()?.uid
            cargar()
        }
    }

    fun seleccionarFecha(fecha: LocalDate) {
        _uiState.value = _uiState.value.copy(fecha = fecha)
        cargaJob?.cancel()
        cargaJob = viewModelScope.launch { cargar() }
    }

    fun moverDia(dias: Long) = seleccionarFecha(_uiState.value.fecha.plusDays(dias))

    private suspend fun cargar() {
        val uid = psicologoId
        if (uid.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(cargando = false, error = "No se pudo identificar la sesión del psicólogo.")
            return
        }
        _uiState.value = _uiState.value.copy(cargando = true, error = null)
        obtenerAgenda(uid, _uiState.value.fecha.toString()).fold(
            onSuccess = { _uiState.value = _uiState.value.copy(cargando = false, citas = it) },
            onFailure = { _uiState.value = _uiState.value.copy(cargando = false, error = it.message ?: "No se pudo cargar la agenda.") }
        )
    }
}
