package com.pe.terapia.presentation.atencion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.Atencion
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import com.pe.terapia.domain.usecase.historiaclinica.RegistrarAtencionUseCase
import java.time.Instant
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.pe.terapia.domain.usecase.historiaclinica.ObtenerAtencionPorCitaUseCase
import com.pe.terapia.domain.repository.RecordatorioCitasScheduler

data class RegistrarAtencionUiState(
    val cargandoInicial: Boolean = true,
    val guardando: Boolean = false,
    val exito: Boolean = false,
    val error: String? = null,
    val errorDictado: String? = null,
    val transcripcionParcial: String? = null,
    val puedeGuardar: Boolean = false,
    // Campos del formulario
    val motivoConsulta: String = "",
    val observaciones: String = "",
    val evaluacion: String = "",
    val evolucion: String = "",
    val indicaciones: String = ""
)

class RegistrarAtencionViewModel(
    private val registrarAtencionUseCase: RegistrarAtencionUseCase,
    private val obtenerAtencionPorCitaUseCase: ObtenerAtencionPorCitaUseCase,
    private val obtenerUsuarioActualUseCase: ObtenerUsuarioActualUseCase,
    private val recordatorioCitasScheduler: RecordatorioCitasScheduler
) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(RegistrarAtencionUiState())
    val uiState: StateFlow<RegistrarAtencionUiState> = _uiState.asStateFlow()

    // Funciones para actualizar los campos del formulario
    fun onMotivoConsultaChange(valor: String) { _uiState.value = _uiState.value.copy(motivoConsulta = valor) }
    fun onObservacionesChange(valor: String) { _uiState.value = _uiState.value.copy(observaciones = valor) }
    fun onEvaluacionChange(valor: String) { _uiState.value = _uiState.value.copy(evaluacion = valor) }
    fun onEvolucionChange(valor: String) { _uiState.value = _uiState.value.copy(evolucion = valor) }
    fun onIndicacionesChange(valor: String) { _uiState.value = _uiState.value.copy(indicaciones = valor) }
    fun agregarTranscripcion(campo: String, texto: String) {
        val actual = _uiState.value
        fun combinar(anterior: String): String = listOf(anterior.trim(), texto.trim())
            .filter(String::isNotEmpty).joinToString(" ")
        _uiState.value = when (campo) {
            "motivo" -> actual.copy(motivoConsulta = combinar(actual.motivoConsulta), errorDictado = null, transcripcionParcial = null)
            "observaciones" -> actual.copy(observaciones = combinar(actual.observaciones), errorDictado = null, transcripcionParcial = null)
            "evaluacion" -> actual.copy(evaluacion = combinar(actual.evaluacion), errorDictado = null, transcripcionParcial = null)
            "evolucion" -> actual.copy(evolucion = combinar(actual.evolucion), errorDictado = null, transcripcionParcial = null)
            "indicaciones" -> actual.copy(indicaciones = combinar(actual.indicaciones), errorDictado = null, transcripcionParcial = null)
            else -> actual
        }
    }
    fun actualizarTranscripcionParcial(texto: String) { _uiState.value = _uiState.value.copy(transcripcionParcial = texto) }
    fun finalizarDictado() { _uiState.value = _uiState.value.copy(transcripcionParcial = null) }
    fun mostrarErrorDictado(mensaje: String) { _uiState.value = _uiState.value.copy(errorDictado = mensaje, transcripcionParcial = null) }
    fun limpiarErrorDictado() { _uiState.value = _uiState.value.copy(errorDictado = null, transcripcionParcial = null) }
    fun cargarDatosPrevios(citaId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargandoInicial = true, puedeGuardar = false)
            val psicologoId = obtenerUsuarioActualUseCase().getOrNull()?.uid
            if (psicologoId == null) {
                _uiState.value = _uiState.value.copy(cargandoInicial = false, error = "Sesión inválida")
                return@launch
            }
            val result = obtenerAtencionPorCitaUseCase(citaId, psicologoId)

            runCatching {
                val cita = firestore.collection("citas").document(citaId).get().await()
                val inicioMillis = cita.getTimestamp("inicioAt")?.toDate()?.time
                    ?: cita.getLong("inicioEpochMillis")
                    ?: error("La cita no tiene una hora de inicio válida.")
                val puedeGuardar = System.currentTimeMillis() >= inicioMillis
                _uiState.value = _uiState.value.copy(puedeGuardar = puedeGuardar)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(error = error.message ?: "No se pudo validar la hora de la cita.")
            }

            result.onSuccess { atencionPrevia ->
                if (atencionPrevia != null) {
                    _uiState.value = _uiState.value.copy(
                        motivoConsulta = atencionPrevia.motivoConsulta,
                        observaciones = atencionPrevia.observaciones,
                        evaluacion = atencionPrevia.evaluacion,
                        evolucion = atencionPrevia.evolucion,
                        indicaciones = atencionPrevia.indicaciones
                    )
                }
            }

            _uiState.value = _uiState.value.copy(cargandoInicial = false)
        }
    }

    fun guardarAtencion(citaId: String, pacienteId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardando = true, error = null)

            val citaValidada = runCatching { firestore.collection("citas").document(citaId).get().await() }
                .getOrElse { error ->
                    _uiState.value = _uiState.value.copy(guardando = false, error = error.message ?: "No se pudo validar la cita.")
                    return@launch
                }
            val inicioMillis = citaValidada.getTimestamp("inicioAt")?.toDate()?.time
                ?: citaValidada.getLong("inicioEpochMillis")
            if (inicioMillis == null || System.currentTimeMillis() < inicioMillis) {
                _uiState.value = _uiState.value.copy(guardando = false, puedeGuardar = false, error = "Solo puedes guardar la atención desde la hora programada de la cita.")
                return@launch
            }

            val usuarioActual = obtenerUsuarioActualUseCase().getOrNull()
            if (usuarioActual == null) {
                _uiState.value = _uiState.value.copy(guardando = false, error = "Sesión inválida")
                return@launch
            }

            val estadoActual = _uiState.value
            val nuevaAtencion = Atencion(
                citaId = citaId,
                pacienteId = pacienteId,
                psicologoId = usuarioActual.uid,
                historiaClinicaId = "",
                fechaAtencion = Instant.now(),
                motivoConsulta = estadoActual.motivoConsulta,
                observaciones = estadoActual.observaciones,
                evaluacion = estadoActual.evaluacion,
                evolucion = estadoActual.evolucion,
                indicaciones = estadoActual.indicaciones
            )

            val result = registrarAtencionUseCase(nuevaAtencion)

            result.fold(
                onSuccess = {
                    runCatching { recordatorioCitasScheduler.cancelar(citaId) }
                    _uiState.value = _uiState.value.copy(guardando = false, exito = true, puedeGuardar = false)
                },
                onFailure = { excepcion ->
                    _uiState.value = _uiState.value.copy(guardando = false, error = excepcion.message)
                }
            )
        }
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
