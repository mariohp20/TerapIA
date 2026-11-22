package com.pe.terapia.presentation.home.psicologo.disponibilidad

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.Sede
import com.pe.terapia.domain.model.SlotDisponible
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import com.pe.terapia.domain.usecase.citas.ObtenerDisponibilidadUseCase
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class DisponibilidadPsicologoUiState(
    val fecha: LocalDate = LocalDate.now(ZoneId.of("America/Lima")),
    val psicologoId: String? = null,
    val sedes: List<Sede> = emptyList(),
    val sedeSeleccionada: Sede? = null,
    val horarios: List<SlotDisponible> = emptyList(),
    val cargando: Boolean = true,
    val error: String? = null
)

class DisponibilidadPsicologoViewModel(
    private val obtenerUsuario: ObtenerUsuarioActualUseCase,
    private val obtenerDisponibilidad: ObtenerDisponibilidadUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(DisponibilidadPsicologoUiState())
    val uiState = _uiState.asStateFlow()
    private var cargaJob: Job? = null

    init {
        viewModelScope.launch {
            val usuario = obtenerUsuario().getOrNull()
            if (usuario == null) {
                _uiState.value = _uiState.value.copy(cargando = false, error = "No se pudo identificar al psicólogo.")
                return@launch
            }
            obtenerDisponibilidad.sedes(usuario.uid).fold(
                onSuccess = { sedes ->
                    _uiState.value = _uiState.value.copy(psicologoId = usuario.uid, sedes = sedes, sedeSeleccionada = sedes.firstOrNull(), cargando = false)
                    cargarTurnos()
                },
                onFailure = { _uiState.value = _uiState.value.copy(cargando = false, error = it.message ?: "No se pudieron cargar las sedes.") }
            )
        }
    }

    fun seleccionarFecha(fecha: LocalDate) {
        _uiState.value = _uiState.value.copy(fecha = fecha)
        solicitarCarga()
    }

    fun seleccionarSede(sede: Sede) {
        _uiState.value = _uiState.value.copy(sedeSeleccionada = sede)
        solicitarCarga()
    }

    private fun solicitarCarga() {
        cargaJob?.cancel()
        cargaJob = viewModelScope.launch { cargarTurnos() }
    }

    private suspend fun cargarTurnos() {
        val state = _uiState.value
        val psicologoId = state.psicologoId
        val sede = state.sedeSeleccionada
        if (psicologoId.isNullOrBlank() || sede == null) {
            _uiState.value = state.copy(cargando = false, horarios = emptyList())
            return
        }
        _uiState.value = state.copy(cargando = true, error = null)
        obtenerDisponibilidad.horariosDisponibles(psicologoId, sede.id, state.fecha.toString()).fold(
            onSuccess = { _uiState.value = _uiState.value.copy(cargando = false, horarios = it) },
            onFailure = { _uiState.value = _uiState.value.copy(cargando = false, error = it.message ?: "No se pudo consultar la disponibilidad.") }
        )
    }
}
