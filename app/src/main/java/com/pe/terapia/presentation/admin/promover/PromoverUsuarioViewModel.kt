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
    val promocionExitosa: Boolean = false
)

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

    fun promover(
        usuario: Usuario,
        descripcionProfesional: String,
        especialidadIds: List<String>,
        sedeIds: List<String>
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardando = true, error = null)

            val result = promoverUsuarioUseCase(
                usuarioId = usuario.uid,
                nombres = usuario.nombres,
                apellidos = usuario.apellidos,
                descripcionProfesional = descripcionProfesional,
                especialidadIds = especialidadIds,
                sedeIds = sedeIds
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

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
