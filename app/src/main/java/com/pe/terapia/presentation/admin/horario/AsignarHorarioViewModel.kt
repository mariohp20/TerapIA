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
    val sedes: List<Sede> = emptyList()
)

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

    fun guardarHorario(
        psicologoId: String,
        sedesIds: Set<String>,
        diaSemana: Int,
        horaInicio: Int,
        horaFin: Int
    ) {
        viewModelScope.launch {
            if (sedesIds.isEmpty()) {
                _uiState.value = _uiState.value.copy(error = "Debe seleccionar al menos una sede")
                return@launch
            }
            if (horaInicio >= horaFin) {
                _uiState.value = _uiState.value.copy(error = "La hora de fin debe ser mayor a la de inicio")
                return@launch
            }

            _uiState.value = _uiState.value.copy(guardando = true, error = null)

            try {
                val inicioMinuto = horaInicio * 60
                val finMinuto = horaFin * 60

                var huboError = false
                var ultimoErrorMsg: String? = null

                for (sedeId in sedesIds) {
                    val result = asignarHorarioPsicologoUseCase(
                        psicologoId = psicologoId,
                        sedeId = sedeId,
                        diaSemana = diaSemana,
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
                    _uiState.value = _uiState.value.copy(guardando = false, error = ultimoErrorMsg ?: "Error al asignar horario")
                } else {
                    _uiState.value = _uiState.value.copy(guardando = false, exito = true)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(guardando = false, error = "Error al procesar el horario")
            }
        }
    }
}