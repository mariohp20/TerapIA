package com.pe.terapia.presentation.home.psicologo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.CitaPacienteDetalle
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import com.pe.terapia.domain.usecase.citas.ObtenerCitasHoyPsicologoUseCase
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.pe.terapia.domain.model.Usuario

data class HomePsicologoUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val citasHoy: Int = 0,
    val pendientes: Int = 0,
    val pacientesActivos: Int = 0,
    val proximasCitas: List<CitaPacienteDetalle> = emptyList()
)

class HomePsicologoViewModel(
    private val obtenerCitasHoyPsicologoUseCase: ObtenerCitasHoyPsicologoUseCase,
    private val obtenerUsuarioActualUseCase: ObtenerUsuarioActualUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomePsicologoUiState())
    val uiState: StateFlow<HomePsicologoUiState> = _uiState.asStateFlow()

    init {
        cargarDatosHoy()
    }

    private fun cargarDatosHoy() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null)

            // 1. Obtener quién es el psicólogo logueado extrayéndolo del Result
            val resultUsuario = obtenerUsuarioActualUseCase()
            val usuarioActual = resultUsuario.getOrNull() // Sacamos el usuario del envoltorio Result

            if (usuarioActual == null) {
                _uiState.value = _uiState.value.copy(cargando = false, error = "No se detectó sesión")
                return@launch
            }

            // 2. Obtener la fecha de hoy en Perú
            val fechaHoy = LocalDate.now(ZoneId.of("America/Lima")).toString()

            // 3. Consultar las citas a Firebase (ahora sí podemos usar .uid porque usuarioActual es de tipo Usuario)
            val resultado = obtenerCitasHoyPsicologoUseCase(usuarioActual.uid, fechaHoy)

            resultado.fold(
                onSuccess = { citas ->
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        proximasCitas = citas,
                        citasHoy = citas.size,
                        pendientes = citas.size,
                        pacientesActivos = citas.map { it.pacienteId }.distinct().size
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        error = "No se pudieron cargar las citas: ${error.message}"
                    )
                }
            )
        }
    }

}