package com.pe.terapia.presentation.admin.promover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.Especialidad
import com.pe.terapia.domain.model.Sede
import com.pe.terapia.domain.model.Usuario
import com.pe.terapia.domain.usecase.admin.ObtenerDatosPromoverUsuarioUseCase
import com.pe.terapia.domain.usecase.admin.PromoverUsuarioUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PromoverUsuarioUiState(
    val cargandoInicial: Boolean = true,
    val guardando: Boolean = false,
    val error: String? = null,
    val pacientes: List<Usuario> = emptyList(),
    val especialidades: List<Especialidad> = emptyList(),
    val sedes: List<Sede> = emptyList(),
    val promocionExitosa: Boolean = false,

    val queryBusqueda: String = "",
    val pacienteSeleccionado: Usuario? = null,
    val formDescripcion: String = "",
    val formEspecialidades: Set<String> = emptySet(),
    val formSedes: Set<String> = emptySet()
) {
    val pacientesFiltrados: List<Usuario>
        get() {
            val query = queryBusqueda.trim().lowercase()
            if (query.isBlank()) return pacientes
            return pacientes.filter { paciente ->
                "${paciente.nombres} ${paciente.apellidos}".lowercase().contains(query) ||
                        (paciente.dni?.contains(query) == true)
            }
        }
}

class PromoverUsuarioViewModel(
    private val obtenerDatosPromoverUsuarioUseCase: ObtenerDatosPromoverUsuarioUseCase,
    private val promoverUsuarioUseCase: PromoverUsuarioUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PromoverUsuarioUiState())
    val uiState: StateFlow<PromoverUsuarioUiState> = _uiState.asStateFlow()

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargandoInicial = true, error = null)

            obtenerDatosPromoverUsuarioUseCase().fold(
                onSuccess = { datos ->
                    _uiState.value = _uiState.value.copy(
                        cargandoInicial = false,
                        pacientes = datos.pacientes,
                        sedes = datos.sedes,
                        especialidades = datos.especialidades
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        cargandoInicial = false,
                        error = error.message ?: "Hubo un error al cargar los datos del sistema. Intente nuevamente."
                    )
                }
            )
        }
    }

    fun onQueryBusquedaChange(valor: String) {
        _uiState.value = _uiState.value.copy(
            queryBusqueda = valor,
            pacienteSeleccionado = null,
            error = null
        )
    }

    fun seleccionarPaciente(paciente: Usuario) {
        _uiState.value = _uiState.value.copy(
            pacienteSeleccionado = paciente,
            queryBusqueda = "${paciente.nombres} ${paciente.apellidos}",
            error = null
        )
    }

    fun onDescripcionChange(valor: String) {
        _uiState.value = _uiState.value.copy(formDescripcion = valor)
    }

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

    fun promover() {
        val actual = _uiState.value
        val usuario = actual.pacienteSeleccionado ?: run {
            _uiState.value = actual.copy(error = "Debe seleccionar un paciente de la lista.")
            return
        }

        viewModelScope.launch {
            _uiState.value = actual.copy(guardando = true, error = null)

            val result = promoverUsuarioUseCase(
                usuarioId = usuario.uid,
                nombres = usuario.nombres,
                apellidos = usuario.apellidos,
                descripcionProfesional = actual.formDescripcion,
                especialidadIds = actual.formEspecialidades.toList(),
                sedeIds = actual.formSedes.toList()
            )

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(guardando = false, promocionExitosa = true)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(guardando = false, error = error.message)
                }
            )
        }
    }
}