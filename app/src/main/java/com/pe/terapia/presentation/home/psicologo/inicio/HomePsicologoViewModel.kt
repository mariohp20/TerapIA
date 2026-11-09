package com.pe.terapia.presentation.home.psicologo.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.CitaPacienteDetalle
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import com.pe.terapia.domain.usecase.citas.ObtenerCitasHoyPsicologoUseCase
import com.pe.terapia.domain.usecase.citas.ObtenerCitasPendientesResultadoUseCase
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomePsicologoUiState(
    val cargando: Boolean = true,
    val datosCargados: Boolean = false,
    val error: String? = null,
    val nombrePsicologo: String = "",
    val citasHoy: Int = 0,
    val pendientes: Int = 0,
    val pacientesActivos: Int = 0,
    val proximasCitas: List<CitaPacienteDetalle> = emptyList(),
    val citasPendientesResultado: List<CitaPacienteDetalle> = emptyList()
)

class HomePsicologoViewModel(
    private val obtenerCitasHoyPsicologoUseCase: ObtenerCitasHoyPsicologoUseCase,
    private val obtenerUsuarioActualUseCase: ObtenerUsuarioActualUseCase,
    private val obtenerCitasPendientesResultadoUseCase: ObtenerCitasPendientesResultadoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomePsicologoUiState())
    val uiState: StateFlow<HomePsicologoUiState> = _uiState.asStateFlow()

    private var cargaEnCurso = false

    fun cargarDatosHoy() {
        if (cargaEnCurso) return
        cargaEnCurso = true
        viewModelScope.launch {
            val yaHayDatos = _uiState.value.datosCargados
            _uiState.value = _uiState.value.copy(cargando = !yaHayDatos, error = null)

            try {
                val resultUsuario = obtenerUsuarioActualUseCase()
                val usuarioActual = resultUsuario.getOrNull()

                if (usuarioActual == null) {
                    _uiState.value = _uiState.value.copy(cargando = false, error = "No se detectó sesión")
                    return@launch
                }

                val primerNombre = usuarioActual.nombres.split(" ").firstOrNull() ?: ""
                val fechaHoy = LocalDate.now(ZoneId.of("America/Lima")).toString()

                val resultado = obtenerCitasHoyPsicologoUseCase(usuarioActual.uid, fechaHoy)
                val pendientesResultado = obtenerCitasPendientesResultadoUseCase(usuarioActual.uid)

                resultado.fold(
                    onSuccess = { citas ->
                        _uiState.value = _uiState.value.copy(
                            cargando = false,
                            datosCargados = true,
                            nombrePsicologo = primerNombre,
                            proximasCitas = citas,
                            citasPendientesResultado = pendientesResultado.getOrDefault(emptyList()),
                            error = pendientesResultado.exceptionOrNull()?.message,
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
            } finally {
                cargaEnCurso = false
            }

        }
    }

}
