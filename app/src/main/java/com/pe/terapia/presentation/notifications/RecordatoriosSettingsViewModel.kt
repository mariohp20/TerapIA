package com.pe.terapia.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.EstadoCita
import com.pe.terapia.domain.model.Rol
import com.pe.terapia.domain.repository.NotificacionesPreferenciasRepository
import com.pe.terapia.domain.repository.RecordatorioCitasScheduler
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import com.pe.terapia.domain.usecase.citas.ObtenerCitasPacienteUseCase
import com.pe.terapia.domain.usecase.citas.ObtenerCitasPsicologoResumenUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecordatoriosSettingsUiState(
    val cargando: Boolean = true,
    val activos: Boolean = true,
    val error: String? = null
)

class RecordatoriosSettingsViewModel(
    private val obtenerUsuarioActualUseCase: ObtenerUsuarioActualUseCase,
    private val preferencias: NotificacionesPreferenciasRepository,
    private val recordatorioCitasScheduler: RecordatorioCitasScheduler,
    private val obtenerCitasPacienteUseCase: ObtenerCitasPacienteUseCase,
    private val obtenerCitasPsicologoResumenUseCase: ObtenerCitasPsicologoResumenUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecordatoriosSettingsUiState())
    val uiState: StateFlow<RecordatoriosSettingsUiState> = _uiState.asStateFlow()
    private var usuarioId: String? = null
    private var rol: Rol? = null

    init {
        viewModelScope.launch {
            val usuario = obtenerUsuarioActualUseCase().getOrNull()
            if (usuario == null) {
                _uiState.value = RecordatoriosSettingsUiState(cargando = false, error = "No se pudo cargar la preferencia.")
                return@launch
            }
            usuarioId = usuario.uid
            rol = usuario.rol
            preferencias.observarActivas(usuario.uid).collect { activo ->
                _uiState.value = _uiState.value.copy(cargando = false, activos = activo)
            }
        }
    }

    fun establecerActivos(activos: Boolean) {
        val uid = usuarioId ?: return
        val rolActual = rol ?: return
        viewModelScope.launch {
            runCatching { preferencias.establecerActivas(uid, activos) }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = error.message ?: "No se pudo guardar la preferencia.")
                    return@launch
                }
            _uiState.value = _uiState.value.copy(activos = activos, error = null)
            if (!activos) {
                recordatorioCitasScheduler.cancelarDeUsuario(uid)
                return@launch
            }
            sincronizarRecordatorios(uid, rolActual)
        }
    }

    private suspend fun sincronizarRecordatorios(uid: String, rol: Rol) {
        val ahora = System.currentTimeMillis()
        when (rol) {
            Rol.PACIENTE -> obtenerCitasPacienteUseCase(uid).fold(
                onSuccess = { citas ->
                    citas.forEach { cita ->
                        if (cita.estado == EstadoCita.PROGRAMADA.name && cita.inicioEpochMillis > ahora) {
                            runCatching { recordatorioCitasScheduler.programar(cita.id, uid, cita.inicioEpochMillis, rol) }
                        } else {
                            recordatorioCitasScheduler.cancelar(cita.id)
                        }
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(error = error.message ?: "No se pudieron sincronizar las próximas citas.")
                }
            )
            Rol.PSICOLOGO -> obtenerCitasPsicologoResumenUseCase(uid, ahora).fold(
                onSuccess = { resumen ->
                    resumen.citasSinRecordatorio.forEach(recordatorioCitasScheduler::cancelar)
                    resumen.futurasProgramadas.forEach { cita ->
                        runCatching { recordatorioCitasScheduler.programar(cita.citaId, uid, cita.inicioEpochMillis, rol) }
                    }
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(error = error.message ?: "No se pudieron sincronizar las próximas citas.")
                }
            )
            Rol.ADMIN -> Unit
        }
    }
}
