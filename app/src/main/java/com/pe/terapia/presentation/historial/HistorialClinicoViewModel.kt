package com.pe.terapia.presentation.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.Atencion
import com.pe.terapia.domain.model.Rol
import com.pe.terapia.domain.model.TipoEtapa
import com.pe.terapia.domain.model.EtapaClinica
import com.pe.terapia.domain.model.EstadoEtapa
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import com.pe.terapia.domain.usecase.historiaclinica.ObtenerAtencionesPacienteUseCase
import com.pe.terapia.domain.usecase.historiaclinica.ObtenerProcesoActivoUseCase
import com.pe.terapia.domain.usecase.historiaclinica.ObtenerEtapasPorProcesoUseCase
import com.pe.terapia.domain.usecase.historiaclinica.ObtenerTiposEtapasUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistorialClinicoUiState(
    val cargando: Boolean = true,
    val atenciones: List<Atencion> = emptyList(),
    val tiposEtapas: List<TipoEtapa> = emptyList(),
    val historialEtapas: List<EtapaClinica> = emptyList(),
    val etapaActivaNombre: String = "Sin etapa asignada",
    val esPsicologo: Boolean = false,
    val error: String? = null
)

class HistorialClinicoViewModel(
    private val obtenerAtencionesUseCase: ObtenerAtencionesPacienteUseCase,
    private val obtenerTiposEtapasUseCase: ObtenerTiposEtapasUseCase,
    private val obtenerProcesoActivoUseCase: ObtenerProcesoActivoUseCase,
    private val obtenerEtapasPorProcesoUseCase: ObtenerEtapasPorProcesoUseCase,
    private val obtenerUsuarioActualUseCase: ObtenerUsuarioActualUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistorialClinicoUiState())
    val uiState: StateFlow<HistorialClinicoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val usuario = obtenerUsuarioActualUseCase().getOrNull()
            _uiState.value = _uiState.value.copy(
                esPsicologo = usuario?.rol == Rol.PSICOLOGO
            )
        }
    }

    fun cargarHistorial(pacienteId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)

            val atencionesDeferred = async { obtenerAtencionesUseCase(pacienteId) }
            val catalogoDeferred = async { obtenerTiposEtapasUseCase() }
            val procesoDeferred = async { obtenerProcesoActivoUseCase(pacienteId) }

            val atencionesResult = atencionesDeferred.await()
            val catalogoResult = catalogoDeferred.await()
            val procesoResult = procesoDeferred.await()

            if (atencionesResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = atencionesResult.exceptionOrNull()?.message ?: "Error al cargar el historial"
                )
                return@launch
            }

            val listaAtenciones = atencionesResult.getOrDefault(emptyList())
            val listaTipos = catalogoResult.getOrDefault(emptyList())
            val proceso = procesoResult.getOrNull()

            var listaEtapasHistorial = emptyList<EtapaClinica>()
            var nombreEtapaActiva = "Sin etapa asignada"

            if (proceso != null) {
                listaEtapasHistorial = obtenerEtapasPorProcesoUseCase(proceso.id).getOrDefault(emptyList())

                val activa = listaEtapasHistorial.find { it.estado == EstadoEtapa.EN_PROGRESO }
                if (activa != null) {
                    val tipoActivo = listaTipos.find { it.id == activa.tipoEtapaId }
                    if (tipoActivo != null) nombreEtapaActiva = tipoActivo.nombre
                }
            }

            _uiState.value = _uiState.value.copy(
                cargando = false,
                atenciones = listaAtenciones,
                tiposEtapas = listaTipos,
                historialEtapas = listaEtapasHistorial,
                etapaActivaNombre = nombreEtapaActiva
            )
        }
    }
}