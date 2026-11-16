package com.pe.terapia.presentation.admin.solicitudes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.SolicitudGestionCita
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import com.pe.terapia.domain.usecase.citas.ObtenerSolicitudesGestionCitaUseCase
import com.pe.terapia.domain.usecase.citas.ResolverSolicitudGestionCitaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SolicitudesGestionCitaUiState(
    val cargando: Boolean = true,
    val procesandoId: String? = null,
    val solicitudes: List<SolicitudGestionCita> = emptyList(),
    val error: String? = null,
    val aviso: String? = null
)

class SolicitudesGestionCitaViewModel(
    private val obtenerUsuarioActualUseCase: ObtenerUsuarioActualUseCase,
    private val obtenerSolicitudesUseCase: ObtenerSolicitudesGestionCitaUseCase,
    private val resolverSolicitudUseCase: ResolverSolicitudGestionCitaUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(SolicitudesGestionCitaUiState())
    val uiState = _uiState.asStateFlow()
    private var adminUid: String? = null

    fun cargar() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(cargando = true, error = null)
        val user = obtenerUsuarioActualUseCase().getOrNull()
        if (user == null) {
            _uiState.value = _uiState.value.copy(cargando = false, error = "No se pudo identificar al administrador.")
            return@launch
        }
        adminUid = user.uid
        obtenerSolicitudesUseCase().fold(
            onSuccess = { _uiState.value = _uiState.value.copy(cargando = false, solicitudes = it) },
            onFailure = { _uiState.value = _uiState.value.copy(cargando = false, error = it.message ?: "No se cargaron las solicitudes.") }
        )
    }

    fun resolver(id: String, decision: String) = viewModelScope.launch {
        val uid = adminUid ?: obtenerUsuarioActualUseCase().getOrNull()?.uid
        if (uid.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(error = "No se pudo identificar al administrador.")
            return@launch
        }
        _uiState.value = _uiState.value.copy(procesandoId = id, error = null, aviso = null)
        resolverSolicitudUseCase(id, uid, decision).fold(
            onSuccess = {
                val msg = when (decision) {
                    "ACEPTAR_CANCELACION" -> "Cita cancelada y turno liberado. Revisa manualmente cualquier pago asociado."
                    "ACEPTAR_REPROGRAMACION" -> "La cita se movió al horario propuesto y conservó sus datos de pago e historial. El recordatorio se actualizará cuando el paciente vuelva a abrir la app."
                    else -> "Solicitud rechazada."
                }
                _uiState.value = _uiState.value.copy(procesandoId = null, aviso = msg)
                cargar()
            },
            onFailure = { _uiState.value = _uiState.value.copy(procesandoId = null, error = it.message ?: "No se pudo resolver la solicitud.") }
        )
    }
}
