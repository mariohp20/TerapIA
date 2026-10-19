package com.pe.terapia.presentation.citas.reserva

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.Especialidad
import com.pe.terapia.domain.model.Psicologo
import com.pe.terapia.domain.model.Sede
import com.pe.terapia.domain.model.SlotDisponible
import com.pe.terapia.domain.usecase.citas.ObtenerDisponibilidadUseCase
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReservaCitaViewModel(
    private val obtenerDisponibilidadUseCase: ObtenerDisponibilidadUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservaCitaUiState())
    val uiState: StateFlow<ReservaCitaUiState> = _uiState.asStateFlow()
    private var cargarPsicologosJob: Job? = null
    private var cargarSedesJob: Job? = null
    private var cargarHorariosJob: Job? = null
    private var cargarFechasJob: Job? = null

    init {
        cargarEspecialidades()
    }

    private fun cargarEspecialidades() {
        _uiState.value = _uiState.value.copy(cargandoEspecialidades = true, error = null)
        viewModelScope.launch {
            obtenerDisponibilidadUseCase.especialidades()
                .onSuccess { especialidades ->
                    _uiState.value = _uiState.value.copy(
                        especialidades = especialidades,
                        cargandoEspecialidades = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        cargandoEspecialidades = false,
                        error = error.message
                    )
                }
        }
    }

    fun seleccionarEspecialidad(especialidad: Especialidad) {
        cargarPsicologosJob?.cancel()
        cargarSedesJob?.cancel()
        cargarHorariosJob?.cancel()
        cargarFechasJob?.cancel()
        _uiState.value = _uiState.value.copy(
            especialidadSeleccionada = especialidad,
            psicologoSeleccionado = null,
            sedes = emptyList(),
            sedeSeleccionada = null,
            cargandoSedes = false,
            fechaSeleccionada = null,
            fechasConsultadas = emptySet(),
            fechasConDisponibilidad = emptySet(),
            disponibilidadPorFecha = emptyMap(),
            cargandoFechas = false,
            horaSeleccionada = null,
            horariosDisponibles = emptyList(),
            cargandoPsicologos = true,
            cargandoHorarios = false,
            error = null
        )
        cargarPsicologosJob = viewModelScope.launch {
            obtenerDisponibilidadUseCase.psicologosPorEspecialidad(especialidad.id)
                .onSuccess { psicologos ->
                    if (_uiState.value.especialidadSeleccionada?.id == especialidad.id) {
                        _uiState.value = _uiState.value.copy(
                            psicologos = psicologos,
                            cargandoPsicologos = false
                        )
                    }
                }
                .onFailure { error ->
                    if (_uiState.value.especialidadSeleccionada?.id == especialidad.id) {
                        _uiState.value = _uiState.value.copy(
                            cargandoPsicologos = false,
                            error = error.message
                        )
                    }
                }
        }
    }

    fun seleccionarPsicologo(psicologo: Psicologo) {
        cargarSedesJob?.cancel()
        cargarHorariosJob?.cancel()
        cargarFechasJob?.cancel()
        _uiState.value = _uiState.value.copy(
            psicologoSeleccionado = psicologo,
            sedes = emptyList(),
            sedeSeleccionada = null,
            fechaSeleccionada = null,
            fechasConsultadas = emptySet(),
            fechasConDisponibilidad = emptySet(),
            disponibilidadPorFecha = emptyMap(),
            cargandoFechas = false,
            horaSeleccionada = null,
            horariosDisponibles = emptyList(),
            slotsDisponibles = emptyList(),
            cargandoSedes = true,
            cargandoHorarios = false,
            error = null
        )
        cargarSedesJob = viewModelScope.launch {
            obtenerDisponibilidadUseCase.sedes(psicologo.id)
                .onSuccess { sedes ->
                    if (_uiState.value.psicologoSeleccionado?.id == psicologo.id) {
                        _uiState.value = _uiState.value.copy(sedes = sedes, cargandoSedes = false)
                    }
                }
                .onFailure { error ->
                    if (_uiState.value.psicologoSeleccionado?.id == psicologo.id) {
                        _uiState.value = _uiState.value.copy(cargandoSedes = false, error = error.message)
                    }
                }
        }
    }

    fun seleccionarSede(sede: Sede) {
        cargarHorariosJob?.cancel()
        cargarFechasJob?.cancel()
        _uiState.value = _uiState.value.copy(
            sedeSeleccionada = sede,
            fechaSeleccionada = null,
            fechasConsultadas = emptySet(),
            fechasConDisponibilidad = emptySet(),
            disponibilidadPorFecha = emptyMap(),
            cargandoFechas = true,
            horaSeleccionada = null,
            horariosDisponibles = emptyList(),
            slotsDisponibles = emptyList(),
            cargandoHorarios = false,
            error = null
        )
        val psicologoId = _uiState.value.psicologoSeleccionado?.id ?: return
        val sedeId = sede.id
        val hoy = LocalDate.now(ZoneId.of("America/Lima"))
        val fechas = (0..13).map { hoy.plusDays(it.toLong()).toString() }

        // PARALELIZACION DE CONSULTAS
        cargarFechasJob = viewModelScope.launch {
            try {
                val resultados = coroutineScope {
                    fechas.map { fecha ->
                        async {
                            fecha to obtenerDisponibilidadUseCase.horariosDisponibles(psicologoId, sedeId, fecha)
                        }
                    }.awaitAll()
                }

                val disponibles = mutableSetOf<String>()
                val slotsPorFecha = mutableMapOf<String, List<SlotDisponible>>()
                var errorDisponibilidad: String? = null

                resultados.forEach { (fecha, result) ->
                    result.onSuccess { slots ->
                        slotsPorFecha[fecha] = slots
                        if (slots.isNotEmpty()) disponibles += fecha
                    }.onFailure { err ->
                        if (errorDisponibilidad == null) errorDisponibilidad = err.message
                    }
                }

                if (_uiState.value.psicologoSeleccionado?.id == psicologoId && _uiState.value.sedeSeleccionada?.id == sedeId) {
                    _uiState.value = _uiState.value.copy(
                        fechasConsultadas = fechas.toSet(),
                        fechasConDisponibilidad = disponibles,
                        disponibilidadPorFecha = slotsPorFecha,
                        cargandoFechas = false,
                        error = errorDisponibilidad
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(cargandoFechas = false, error = e.message)
            }
        }
    }

    fun seleccionarFecha(fecha: String) {
        val psicologoId = _uiState.value.psicologoSeleccionado?.id ?: return
        val sedeId = _uiState.value.sedeSeleccionada?.id ?: return
        cargarHorariosJob?.cancel()
        _uiState.value = _uiState.value.copy(
            fechaSeleccionada = fecha,
            horaSeleccionada = null,
            cargandoHorarios = true,
            horariosDisponibles = emptyList(),
            slotsDisponibles = emptyList(),
            error = null
        )
        _uiState.value.disponibilidadPorFecha[fecha]?.let { cached ->
            _uiState.value = _uiState.value.copy(
                horariosDisponibles = cached.map { it.hora },
                slotsDisponibles = cached,
                cargandoHorarios = false
            )
            return
        }
        cargarHorariosJob = viewModelScope.launch {
            obtenerDisponibilidadUseCase.horariosDisponibles(psicologoId, sedeId, fecha)
                .onSuccess { slots ->
                    if (_uiState.value.psicologoSeleccionado?.id == psicologoId &&
                        _uiState.value.sedeSeleccionada?.id == sedeId &&
                        _uiState.value.fechaSeleccionada == fecha
                    ) {
                        _uiState.value = _uiState.value.copy(
                            horariosDisponibles = slots.map { it.hora },
                            slotsDisponibles = slots,
                            cargandoHorarios = false
                        )
                    }
                }
                .onFailure { error ->
                    if (_uiState.value.psicologoSeleccionado?.id == psicologoId &&
                        _uiState.value.sedeSeleccionada?.id == sedeId &&
                        _uiState.value.fechaSeleccionada == fecha
                    ) {
                        _uiState.value = _uiState.value.copy(
                            cargandoHorarios = false,
                            error = error.message
                        )
                    }
                }
        }
    }

    fun seleccionarHora(hora: String) {
        val slot = _uiState.value.slotsDisponibles.firstOrNull { it.hora == hora }
        if (slot != null) {
            _uiState.value = _uiState.value.copy(horaSeleccionada = hora)
        }
    }

    fun slotSeleccionado() = _uiState.value.slotsDisponibles.firstOrNull { it.hora == _uiState.value.horaSeleccionada }
}