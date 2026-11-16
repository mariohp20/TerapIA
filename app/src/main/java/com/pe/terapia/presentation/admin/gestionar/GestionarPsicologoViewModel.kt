package com.pe.terapia.presentation.admin.gestionar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.Especialidad
import com.pe.terapia.domain.model.Psicologo
import com.pe.terapia.domain.model.Sede
import com.pe.terapia.domain.usecase.admin.ActualizarPsicologoUseCase
import com.pe.terapia.domain.usecase.admin.ObtenerDatosGestionarPsicologoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GestionarPsicologoUiState(
    val cargandoInicial: Boolean = true,
    val guardando: Boolean = false,
    val error: String? = null,
    val exito: Boolean = false,

    // Datos globales
    val psicologos: List<Psicologo> = emptyList(),
    val especialidades: List<Especialidad> = emptyList(),
    val sedes: List<Sede> = emptyList(),

    // Estado de la UI
    val psicologoSeleccionado: Psicologo? = null,

    // Formulario de edicion
    val formDescripcion: String = "",
    val formEspecialidades: Set<String> = emptySet(),
    val formSedes: Set<String> = emptySet()
)

class GestionarPsicologoViewModel(
    val obtenerDatosGestionarPsicologoUseCase: ObtenerDatosGestionarPsicologoUseCase,
    private val actualizarPsicologoUseCase: ActualizarPsicologoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GestionarPsicologoUiState())
    val uiState: StateFlow<GestionarPsicologoUiState> = _uiState.asStateFlow()

    init { cargarDatos() }

    private fun cargarDatos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargandoInicial = true, error = null)

            obtenerDatosGestionarPsicologoUseCase().fold(
                onSuccess = { datos ->
                    _uiState.value = _uiState.value.copy(
                        cargandoInicial = false,
                        psicologos = datos.psicologos,
                        sedes = datos.sedes,
                        especialidades = datos.especialidades
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        cargandoInicial = false,
                        error = error.message ?: "Error al cargar datos del sistema."
                    )
                }
            )
        }
    }

    fun seleccionarPsicologo(psicologo: Psicologo?) {
        _uiState.value = if (psicologo == null) {
            _uiState.value.copy(psicologoSeleccionado = null, formDescripcion = "", formEspecialidades = emptySet(), formSedes = emptySet(), exito = false)
        } else {
            _uiState.value.copy(
                psicologoSeleccionado = psicologo,
                formDescripcion = psicologo.descripcionProfesional,
                formEspecialidades = psicologo.especialidadIds.toSet(),
                formSedes = psicologo.sedeIds.toSet(),
                exito = false
            )
        }
    }

    fun onDescripcionChange(valor: String) { _uiState.value = _uiState.value.copy(formDescripcion = valor) }

    fun toggleEspecialidad(id: String) {
        val actuales = _uiState.value.formEspecialidades.toMutableSet()
        if (actuales.contains(id)) actuales.remove(id) else actuales.add(id)
        _uiState.value = _uiState.value.copy(formEspecialidades = actuales)
    }

    fun toggleSede(id: String) {
        val actuales = _uiState.value.formSedes.toMutableSet()
        if (actuales.contains(id)) actuales.remove(id) else actuales.add(id)
        _uiState.value = _uiState.value.copy(formSedes = actuales)
    }

    fun guardarCambios() {
        val actual = _uiState.value
        val psi = actual.psicologoSeleccionado ?: return

        viewModelScope.launch {
            _uiState.value = actual.copy(guardando = true, error = null)
            val result = actualizarPsicologoUseCase(
                psicologoId = psi.id,
                descripcionProfesional = actual.formDescripcion,
                especialidadIds = actual.formEspecialidades.toList(),
                sedeIds = actual.formSedes.toList()
            )

            result.fold(
                onSuccess = {
                    cargarDatos()
                    _uiState.value = _uiState.value.copy(guardando = false, exito = true)
                },
                onFailure = { _uiState.value = _uiState.value.copy(guardando = false, error = it.message) }
            )
        }
    }
}