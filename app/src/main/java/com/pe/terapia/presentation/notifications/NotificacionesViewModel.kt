package com.pe.terapia.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.NotificacionLocal
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import com.pe.terapia.domain.usecase.notificaciones.MarcarNotificacionLeidaUseCase
import com.pe.terapia.domain.usecase.notificaciones.MarcarTodasNotificacionesLeidasUseCase
import com.pe.terapia.domain.usecase.notificaciones.ObservarNotificacionesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class NotificacionesUiState(
    val cargando: Boolean = true,
    val notificaciones: List<NotificacionLocal> = emptyList(),
    val error: String? = null
) {
    val noLeidas: Int get() = notificaciones.count { !it.leida }
}

class NotificacionesViewModel(
    private val obtenerUsuarioActualUseCase: ObtenerUsuarioActualUseCase,
    private val observarNotificacionesUseCase: ObservarNotificacionesUseCase,
    private val marcarNotificacionLeidaUseCase: MarcarNotificacionLeidaUseCase,
    private val marcarTodasNotificacionesLeidasUseCase: MarcarTodasNotificacionesLeidasUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificacionesUiState())
    val uiState: StateFlow<NotificacionesUiState> = _uiState.asStateFlow()
    private var usuarioId: String? = null

    init {
        viewModelScope.launch {
            val usuario = obtenerUsuarioActualUseCase().getOrNull()
            if (usuario == null) {
                _uiState.value = NotificacionesUiState(cargando = false, error = "No se pudo identificar la sesión.")
                return@launch
            }
            usuarioId = usuario.uid
            runCatching { observarNotificacionesUseCase.sincronizar(usuario.uid, usuario.rol) }
            observarNotificacionesUseCase(usuario.uid)
                .catch { error ->
                    _uiState.value = _uiState.value.copy(cargando = false, error = error.message ?: "No se pudieron cargar las notificaciones.")
                }
                .collect { items ->
                    _uiState.value = NotificacionesUiState(cargando = false, notificaciones = items)
                }
        }
    }

    suspend fun marcarLeida(id: String) {
        val uid = usuarioId ?: return
        marcarNotificacionLeidaUseCase(id, uid)
    }

    fun marcarTodasLeidas() {
        val uid = usuarioId ?: return
        viewModelScope.launch { marcarTodasNotificacionesLeidasUseCase(uid) }
    }
}
