package com.pe.terapia.presentation.admin.horario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.usecase.admin.AsignarHorarioPsicologoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AsignarHorarioUiState(
    val guardando: Boolean = false,
    val exito: Boolean = false,
    val error: String? = null
)

class AsignarHorarioViewModel(
    private val asignarHorarioPsicologoUseCase: AsignarHorarioPsicologoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AsignarHorarioUiState())
    val uiState: StateFlow<AsignarHorarioUiState> = _uiState.asStateFlow()

    var psicologoId = MutableStateFlow("")
    var sedeId = MutableStateFlow("")
    var diaSemana = MutableStateFlow("1") // 1 = Lunes, 2 = Martes...
    var horaInicio = MutableStateFlow("08:00")
    var horaFin = MutableStateFlow("13:00")

    fun guardarHorario() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardando = true, error = null)

            try {
                // Convertir "08:00" a minutos (ej. 8 * 60 + 0 = 480)
                val inicioParts = horaInicio.value.split(":")
                val finParts = horaFin.value.split(":")

                val inicioMinuto = inicioParts[0].toInt() * 60 + inicioParts[1].toInt()
                val finMinuto = finParts[0].toInt() * 60 + finParts[1].toInt()
                val dia = diaSemana.value.toInt()

                val result = asignarHorarioPsicologoUseCase(
                    psicologoId = psicologoId.value.trim(),
                    sedeId = sedeId.value.trim(),
                    diaSemana = dia,
                    inicioMinuto = inicioMinuto,
                    finMinuto = finMinuto
                )

                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(guardando = false, exito = true)
                    },
                    onFailure = {
                        _uiState.value = _uiState.value.copy(guardando = false, error = it.message)
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    guardando = false,
                    error = "Por favor verifica el formato de los datos (Horas en formato HH:mm y Día en número 1-7)"
                )
            }
        }
    }
}