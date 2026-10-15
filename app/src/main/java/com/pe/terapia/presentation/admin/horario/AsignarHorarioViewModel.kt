package com.pe.terapia.presentation.admin.horario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.Psicologo // <-- Modelo actualizado
import com.pe.terapia.domain.model.Sede
import com.pe.terapia.domain.usecase.admin.AsignarHorarioPsicologoUseCase
import com.pe.terapia.domain.usecase.admin.ObtenerPsicologosUseCase // Debe retornar List<Psicologo>
import com.pe.terapia.domain.usecase.admin.ObtenerSedesUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AsignarHorarioUiState(
    val cargandoInicial: Boolean = true,
    val guardando: Boolean = false,
    val exito: Boolean = false,
    val error: String? = null,
    val psicologos: List<Psicologo> = emptyList(), // <-- Ahora es lista de Psicologo
    val sedes: List<Sede> = emptyList()
)

class AsignarHorarioViewModel(
    private val asignarHorarioPsicologoUseCase: AsignarHorarioPsicologoUseCase,
    private val obtenerPsicologos: ObtenerPsicologosUseCase,
    private val obtenerSedes: ObtenerSedesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AsignarHorarioUiState())
    val uiState: StateFlow<AsignarHorarioUiState> = _uiState.asStateFlow()

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargandoInicial = true, error = null)

            val psicologosDeferred = async { obtenerPsicologos() }
            val sedesDeferred = async { obtenerSedes() }

            val resultPsicologos = psicologosDeferred.await()
            val resultSedes = sedesDeferred.await()

            if (resultPsicologos.isFailure || resultSedes.isFailure) {
                _uiState.value = _uiState.value.copy(
                    cargandoInicial = false,
                    error = "Hubo un error al cargar los datos del sistema."
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                cargandoInicial = false,
                psicologos = resultPsicologos.getOrNull().orEmpty(),
                sedes = resultSedes.getOrNull().orEmpty()
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

                // Iteramos para guardar el horario en cada sede seleccionada
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