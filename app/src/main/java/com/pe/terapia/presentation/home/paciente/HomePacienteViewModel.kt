package com.pe.terapia.presentation.home.paciente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.data.local.room.CitaDao
import com.pe.terapia.data.local.room.CitaEntity
import com.pe.terapia.domain.model.ProximaCita
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import com.pe.terapia.domain.usecase.citas.ObtenerProximaCitaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomePacienteUiState(
    val nombrePaciente: String = "",
    val proximaCita: ProximaCita? = null,
    val cargando: Boolean = true,
    val error: String? = null,
    val mostrarQr: Boolean = false
)

class HomePacienteViewModel(
    private val obtenerUsuarioActual: ObtenerUsuarioActualUseCase,
    private val obtenerProximaCita: ObtenerProximaCitaUseCase,
    private val citaDao: CitaDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomePacienteUiState())
    val uiState: StateFlow<HomePacienteUiState> = _uiState.asStateFlow()

    init {
        cargarHome()
    }

    private fun cargarHome() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            obtenerUsuarioActual().fold(
                onSuccess = { usuario ->
                    if (usuario == null) {
                        _uiState.update { it.copy(cargando = false, error = "No se pudo identificar la sesión del paciente.") }
                        return@fold
                    }
                    val currentName = usuario.nombres.ifBlank { "" }
                    obtenerProximaCita(usuario.uid).fold(
                        onSuccess = { cita ->
                            if (cita != null) {
                                runCatching {
                                    val entity = CitaEntity(
                                        id = cita.id.ifBlank { "${usuario.uid}_${cita.fecha}_${cita.hora}" },
                                        pacienteId = usuario.uid,
                                        psicologoNombre = cita.psicologoNombre,
                                        especialidadNombre = cita.especialidadNombre,
                                        sedeNombre = cita.sedeNombre,
                                        sedeDireccion = cita.sedeDireccion,
                                        fecha = cita.fecha,
                                        hora = cita.hora,
                                        inicioEpochMillis = System.currentTimeMillis() + 86400000
                                    )
                                    citaDao.insertarCita(entity)
                                }
                            }
                            _uiState.update { it.copy(nombrePaciente = currentName, proximaCita = cita, cargando = false) }
                        },
                        onFailure = { _ ->
                            // Fallback a Room (Caché local) cuando no hay internet
                            val local = runCatching { citaDao.obtenerProximaCitaLocal(usuario.uid, 0L) }.getOrNull()
                            val proximaLocal = local?.let {
                                ProximaCita(
                                    id = it.id,
                                    psicologoNombre = it.psicologoNombre,
                                    especialidadNombre = it.especialidadNombre,
                                    fecha = it.fecha,
                                    hora = it.hora,
                                    sedeNombre = it.sedeNombre,
                                    sedeDireccion = it.sedeDireccion
                                )
                            }
                            _uiState.update {
                                it.copy(
                                    nombrePaciente = currentName,
                                    proximaCita = proximaLocal,
                                    cargando = false,
                                    error = if (proximaLocal == null) "Sin conexión a internet y sin citas en caché local." else "Modo sin conexión (Datos de caché local Room)"
                                )
                            }
                        }
                    )
                },
                onFailure = { error ->
                    _uiState.update { it.copy(cargando = false, error = error.message) }
                }
            )
        }
    }

    fun setMostrarQr(mostrar: Boolean) {
        _uiState.update { it.copy(mostrarQr = mostrar) }
    }

    fun verHistorial(onNavegarAHistorial: (String, String) -> Unit) {
        viewModelScope.launch {
            val usuario = obtenerUsuarioActual().getOrNull()
            if (usuario != null) {
                onNavegarAHistorial(
                    usuario.uid,
                    "${usuario.nombres} ${usuario.apellidos}"
                )
            }
        }
    }
}
