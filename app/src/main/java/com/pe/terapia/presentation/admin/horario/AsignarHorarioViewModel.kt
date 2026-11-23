package com.pe.terapia.presentation.admin.horario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.Psicologo
import com.pe.terapia.domain.model.Sede
import com.pe.terapia.domain.usecase.admin.AsignarHorarioPsicologoUseCase
import com.pe.terapia.domain.usecase.admin.ObtenerDatosAsignarHorarioUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AsignarHorarioUiState(
    val cargandoInicial: Boolean = true,
    val guardando: Boolean = false,
    val exito: Boolean = false,
    val error: String? = null,
    val psicologos: List<Psicologo> = emptyList(),
    val sedes: List<Sede> = emptyList(),

    val psicologoSeleccionado: Psicologo? = null,
    val sedesSeleccionadas: Set<String> = emptySet(),
    val diaSeleccionado: Int = 1, // 1 = Lunes
    val horaInicioSeleccionada: Int = 8,
    val horaFinSeleccionada: Int = 9
) {
    val psicologosActivos: List<Psicologo>
        get() = psicologos.filter { it.activo }

    val sedesDelPsicologo: List<Sede>
        get() {
            val psicologo = psicologoSeleccionado ?: return emptyList()
            return sedes.filter { psicologo.sedeIds.contains(it.id) }
        }
}

class AsignarHorarioViewModel(
    private val asignarHorarioPsicologoUseCase: AsignarHorarioPsicologoUseCase,
    private val obtenerDatosAsignarHorarioUseCase: ObtenerDatosAsignarHorarioUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AsignarHorarioUiState())
    val uiState: StateFlow<AsignarHorarioUiState> = _uiState.asStateFlow()

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargandoInicial = true, error = null)

            obtenerDatosAsignarHorarioUseCase().fold(
                onSuccess = { datos ->
                    _uiState.value = _uiState.value.copy(
                        cargandoInicial = false,
                        psicologos = datos.psicologos,
                        sedes = datos.sedes
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        cargandoInicial = false,
                        error = error.message ?: "Hubo un error al cargar los datos del sistema."
                    )
                }
            )
        }
    }

    fun seleccionarPsicologo(psicologo: Psicologo) {
        val actual = _uiState.value
        val nuevasSedes = if (actual.psicologoSeleccionado?.id != psicologo.id) {
            emptySet()
        } else {
            actual.sedesSeleccionadas
        }

        _uiState.value = actual.copy(
            psicologoSeleccionado = psicologo,
            sedesSeleccionadas = nuevasSedes,
            error = null
        )
    }

    fun toggleSede(sedeId: String) {
        val actuales = _uiState.value.sedesSeleccionadas.toMutableSet()
        if (actuales.contains(sedeId)) actuales.remove(sedeId) else actuales.add(sedeId)
        _uiState.value = _uiState.value.copy(sedesSeleccionadas = actuales, error = null)
    }

    fun seleccionarDia(dia: Int) {
        _uiState.value = _uiState.value.copy(diaSeleccionado = dia, error = null)
    }

    fun seleccionarHoraInicio(hora: Int) {
        val actual = _uiState.value
        val nuevaHoraFin = if (actual.horaFinSeleccionada <= hora) hora + 1 else actual.horaFinSeleccionada
        _uiState.value = actual.copy(
            horaInicioSeleccionada = hora,
            horaFinSeleccionada = nuevaHoraFin,
            error = null
        )
    }

    fun seleccionarHoraFin(hora: Int) {
        _uiState.value = _uiState.value.copy(horaFinSeleccionada = hora, error = null)
    }

    fun guardarHorario() {
        val actual = _uiState.value
        val psicologo = actual.psicologoSeleccionado ?: run {
            _uiState.value = actual.copy(error = "Debe seleccionar un psicólogo")
            return
        }

        viewModelScope.launch {
            if (actual.sedesSeleccionadas.isEmpty()) {
                _uiState.value = actual.copy(error = "Debe seleccionar al menos una sede")
                return@launch
            }
            if (actual.horaInicioSeleccionada >= actual.horaFinSeleccionada) {
                _uiState.value = actual.copy(error = "La hora de fin debe ser mayor a la de inicio")
                return@launch
            }

            _uiState.value = actual.copy(guardando = true, error = null)

            try {
                val inicioMinuto = actual.horaInicioSeleccionada * 60
                val finMinuto = actual.horaFinSeleccionada * 60

                var huboError = false
                var ultimoErrorMsg: String? = null

                for (sedeId in actual.sedesSeleccionadas) {
                    val result = asignarHorarioPsicologoUseCase(
                        psicologoId = psicologo.id,
                        sedeId = sedeId,
                        diaSemana = actual.diaSeleccionado,
                        inicioMinuto = inicioMinuto,
                        finMinuto = finMinuto
                    )

                    if (result.isFailure) {
                        huboError = true
                        ultimoErrorMsg = result.exceptionOrNull()?.message
                        break
                    }
                }

                if (huboError) {
                    _uiState.value = _uiState.value.copy(
                        guardando = false,
                        error = ultimoErrorMsg ?: "Error al asignar horario"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(guardando = false, exito = true)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    guardando = false,
                    error = "Error al procesar el horario"
                )
            }
        }
    }
}