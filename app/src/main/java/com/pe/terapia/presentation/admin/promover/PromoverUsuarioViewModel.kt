package com.pe.terapia.presentation.admin.promover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.Especialidad
import com.pe.terapia.domain.model.Sede
import com.pe.terapia.domain.model.Usuario
import com.pe.terapia.domain.usecase.admin.ObtenerEspecialidadesUseCase
import com.pe.terapia.domain.usecase.admin.ObtenerPacientesUseCase
import com.pe.terapia.domain.usecase.admin.ObtenerSedesUseCase
import com.pe.terapia.domain.usecase.admin.PromoverUsuarioUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

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
    private val obtenerPacientes: ObtenerPacientesUseCase,
    private val obtenerSedes: ObtenerSedesUseCase,
    private val obtenerEspecialidades: ObtenerEspecialidadesUseCase,
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

            // Ejecutamos las 3 peticiones en paralelo para cargar más rápido
            val pacientesDeffered = async { obtenerPacientes() }
            val sedesDeffered = async { obtenerSedes() }
            val especialidadesDeffered = async { obtenerEspecialidades() }

            val resultPacientes = pacientesDeffered.await()
            val resultSedes = sedesDeffered.await()
            val resultEspecialidades = especialidadesDeffered.await()

            if (resultPacientes.isFailure) {
                Log.e("PromoverAdmin", "Error cargando pacientes", resultPacientes.exceptionOrNull())
            }
            if (resultSedes.isFailure) {
                Log.e("PromoverAdmin", "Error cargando sedes", resultSedes.exceptionOrNull())
            }
            if (resultEspecialidades.isFailure) {
                Log.e("PromoverAdmin", "Error cargando especialidades", resultEspecialidades.exceptionOrNull())
            }

            if (resultPacientes.isFailure || resultSedes.isFailure || resultEspecialidades.isFailure) {
                _uiState.value = _uiState.value.copy(
                    cargandoInicial = false,
                    error = "Hubo un error al cargar los datos del sistema. Intente nuevamente."
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                cargandoInicial = false,
                pacientes = resultPacientes.getOrNull().orEmpty(),
                sedes = resultSedes.getOrNull().orEmpty(),
                especialidades = resultEspecialidades.getOrNull().orEmpty()
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