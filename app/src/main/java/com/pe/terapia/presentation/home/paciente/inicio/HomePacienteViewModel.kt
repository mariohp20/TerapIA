package com.pe.terapia.presentation.home.paciente.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.ProximaCita
import com.pe.terapia.domain.usecase.auth.ObtenerUidSesionActualUseCase
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import com.pe.terapia.domain.usecase.citas.GuardarProximaCitaEnCacheUseCase
import com.pe.terapia.domain.usecase.citas.ObtenerProximaCitaDeCacheUseCase
import com.pe.terapia.domain.usecase.citas.ObtenerCitasPacienteUseCase
import com.pe.terapia.domain.model.EstadoCita
import com.pe.terapia.domain.model.Rol
import com.pe.terapia.domain.repository.RecordatorioCitasScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
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

sealed interface HomePacienteUiEvent {
    data class AbrirHistorial(val pacienteId: String, val pacienteNombre: String) : HomePacienteUiEvent
}

class HomePacienteViewModel(
    private val obtenerUidSesionActual: ObtenerUidSesionActualUseCase,
    private val obtenerUsuarioActual: ObtenerUsuarioActualUseCase,
    private val obtenerCitasPaciente: ObtenerCitasPacienteUseCase,
    private val guardarProximaCitaEnCache: GuardarProximaCitaEnCacheUseCase,
    private val obtenerProximaCitaDeCache: ObtenerProximaCitaDeCacheUseCase,
    private val recordatorioCitasScheduler: RecordatorioCitasScheduler
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomePacienteUiState())
    val uiState: StateFlow<HomePacienteUiState> = _uiState.asStateFlow()
    private val _uiEvents = MutableSharedFlow<HomePacienteUiEvent>(extraBufferCapacity = 1)
    val uiEvents: SharedFlow<HomePacienteUiEvent> = _uiEvents

    init {
        cargarHome()
    }

    private fun cargarHome() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            val pacienteId = obtenerUidSesionActual()
            if (pacienteId.isNullOrBlank()) {
                _uiState.update { it.copy(cargando = false, error = "No se pudo identificar la sesión del paciente.") }
                return@launch
            }

            // Room se consulta primero con el UID local de Firebase Auth, sin esperar a Firestore.
            val cacheLocal = obtenerProximaCitaDeCache(
                pacienteId,
                System.currentTimeMillis()
            ).getOrNull()
            _uiState.update {
                it.copy(
                    proximaCita = cacheLocal,
                    cargando = cacheLocal == null,
                    error = null
                )
            }

            // El perfil y la cita se refrescan de manera independiente; si la red está lenta,
            // el dato local ya está visible y no bloquea la pantalla de Inicio.
            launch {
                obtenerUsuarioActual().onSuccess { usuario ->
                    if (usuario != null) {
                        _uiState.update { it.copy(nombrePaciente = usuario.nombres.ifBlank { "" }) }
                    }
                }
            }

            launch {
                obtenerCitasPaciente(pacienteId).fold(
                    onSuccess = { citas ->
                        val ahora = System.currentTimeMillis()
                        citas.asSequence()
                            .filter { it.estado == EstadoCita.PROGRAMADA.name && it.inicioEpochMillis > ahora }
                            .forEach { cita ->
                                runCatching {
                                    recordatorioCitasScheduler.programar(cita.id, cita.inicioEpochMillis, Rol.PACIENTE)
                                }
                            }
                        val cita = citas.firstOrNull {
                            it.estado == EstadoCita.PROGRAMADA.name && it.inicioEpochMillis > ahora
                        }
                        guardarProximaCitaEnCache(pacienteId, cita)
                        _uiState.update {
                            it.copy(
                                proximaCita = cita,
                                cargando = false,
                                error = null
                            )
                        }
                    },
                    onFailure = {
                        _uiState.update { state ->
                            state.copy(
                                cargando = false,
                                error = if (state.proximaCita == null) {
                                    "Sin conexión y sin una cita guardada localmente."
                                } else {
                                    "Sin conexión. Mostrando la última cita guardada en este dispositivo."
                                }
                            )
                        }
                    }
                )
            }
        }
    }

    fun setMostrarQr(mostrar: Boolean) {
        _uiState.update { it.copy(mostrarQr = mostrar) }
    }

    fun verHistorial() {
        viewModelScope.launch {
            obtenerUsuarioActual().fold(
                onSuccess = { usuario ->
                    if (usuario == null) {
                        _uiState.update { it.copy(error = "No se pudo identificar la sesión del paciente.") }
                    } else {
                        _uiEvents.emit(
                            HomePacienteUiEvent.AbrirHistorial(
                                usuario.uid,
                                "${usuario.nombres} ${usuario.apellidos}"
                            )
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(error = error.message ?: "No se pudo abrir el historial clínico.")
                    }
                }
            )
        }
    }
}
