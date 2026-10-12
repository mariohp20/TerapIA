package com.pe.terapia.presentation.buscadorPacientes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.PacienteResumen
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import com.pe.terapia.domain.usecase.historiaclinica.ObtenerPacientesDePsicologoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BuscadorPacientesViewModel(
    private val obtenerPacientesUseCase: ObtenerPacientesDePsicologoUseCase,
    private val obtenerUsuarioActualUseCase: ObtenerUsuarioActualUseCase
) : ViewModel() {

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _pacientesOriginales = MutableStateFlow<List<PacienteResumen>>(emptyList())

    private val _pacientesFiltrados = MutableStateFlow<List<PacienteResumen>>(emptyList())
    val pacientesFiltrados: StateFlow<List<PacienteResumen>> = _pacientesFiltrados.asStateFlow()

    init {
        cargarPacientes()
    }

    private fun cargarPacientes() {
        viewModelScope.launch {
            val usuarioActual = obtenerUsuarioActualUseCase().getOrNull()
            if (usuarioActual != null) {
                obtenerPacientesUseCase(usuarioActual.uid).fold(
                    onSuccess = { lista ->
                        _pacientesOriginales.value = lista
                        _pacientesFiltrados.value = lista
                    },
                    onFailure = {
                        // Opcional: Manejar estado de error en la UI
                    }
                )
            }
        }
    }

    fun onSearchTextChange(text: String) {
        _searchText.value = text
        if (text.isBlank()) {
            _pacientesFiltrados.value = _pacientesOriginales.value
        } else {
            _pacientesFiltrados.value = _pacientesOriginales.value.filter {
                it.nombreCompleto.contains(text, ignoreCase = true)
            }
        }
    }
}