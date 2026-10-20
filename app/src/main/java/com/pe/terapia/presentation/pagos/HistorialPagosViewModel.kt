package com.pe.terapia.presentation.pagos

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.core.pdf.PdfExporter
import com.pe.terapia.core.pdf.PdfGenerator
import com.pe.terapia.core.pdf.Plantillas.ComprobanteData
import com.pe.terapia.core.pdf.Plantillas.ComprobantePdfTemplate
import com.pe.terapia.domain.model.Comprobante
import com.pe.terapia.domain.model.Pago
import com.pe.terapia.domain.model.Rol
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import com.pe.terapia.domain.usecase.pagos.ObtenerHistorialPagosUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

sealed interface HistorialPagosUiState {
    data object Cargando : HistorialPagosUiState
    data class Exito(val pagosConComprobante: List<Pair<Pago, Comprobante?>>) : HistorialPagosUiState
    data class Error(val mensaje: String) : HistorialPagosUiState
}

class HistorialPagosViewModel(
    private val obtenerHistorialPagosUseCase: ObtenerHistorialPagosUseCase,
    private val obtenerUsuarioActualUseCase: ObtenerUsuarioActualUseCase,
    private val pdfGenerator: PdfGenerator,
    private val pdfExporter: PdfExporter,
    private val comprobantePdfTemplate: ComprobantePdfTemplate
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistorialPagosUiState>(HistorialPagosUiState.Cargando)
    val uiState: StateFlow<HistorialPagosUiState> = _uiState.asStateFlow()

    init {
        cargarHistorial()
    }

    fun cargarHistorial() {
        viewModelScope.launch {
            _uiState.value = HistorialPagosUiState.Cargando
            val usuarioRes = obtenerUsuarioActualUseCase()
            val usuario = usuarioRes.getOrNull()
            if (usuario == null || usuario.rol != Rol.PACIENTE) {
                _uiState.value = HistorialPagosUiState.Error("No se pudo identificar al paciente.")
                return@launch
            }

            obtenerHistorialPagosUseCase(usuario.uid)
                .onSuccess { lista ->
                    _uiState.value = HistorialPagosUiState.Exito(lista)
                }
                .onFailure { error ->
                    _uiState.value = HistorialPagosUiState.Error(error.message ?: "Error al cargar historial de pagos.")
                }
        }
    }

    fun generarArchivoPdf(comprobante: Comprobante, pago: Pago): File {
        val data = ComprobanteData(
            numero = comprobante.numeroComprobante,
            paciente = "Paciente ID: ${pago.pacienteId}",
            fechaPago = comprobante.fechaEmision,
            concepto = comprobante.detalles,
            monto = String.format(Locale.US, "%.2f", pago.monto),
            metodoPago = pago.metodoPago?.name ?: "Efectivo / Online",
            codigoOperacion = pago.referenciaTransaccion ?: "N/A"
        )
        val fileName = "comprobante_${comprobante.numeroComprobante.replace(Regex("[^a-zA-Z0-9_-]"), "_")}.pdf"
        return pdfGenerator.generate(data, comprobantePdfTemplate, fileName)
    }

    fun compartirPdf(comprobante: Comprobante, pago: Pago) {
        viewModelScope.launch {
            runCatching {
                val file = generarArchivoPdf(comprobante, pago)
                pdfExporter.share(file)
            }
        }
    }

    fun guardarPdfEnUri(file: File, uri: Uri) {
        viewModelScope.launch {
            runCatching {
                pdfExporter.saveToUri(file, uri)
            }
        }
    }
}
