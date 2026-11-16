package com.pe.terapia.presentation.pagos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.Comprobante
import com.pe.terapia.domain.usecase.pagos.PagarOnlineUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface PasarelaPagoUiState {
    data object Idle : PasarelaPagoUiState
    data object Cargando : PasarelaPagoUiState
    data class Exito(val comprobante: Comprobante) : PasarelaPagoUiState
    data class Error(val mensaje: String) : PasarelaPagoUiState
}

class PasarelaPagoViewModel(
    private val pagoId: String,
    private val pagarOnlineUseCase: PagarOnlineUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PasarelaPagoUiState>(PasarelaPagoUiState.Idle)
    val uiState: StateFlow<PasarelaPagoUiState> = _uiState.asStateFlow()

    fun simularPagoMercadoPago() {
        viewModelScope.launch {
            _uiState.value = PasarelaPagoUiState.Cargando
            val referencia = "MP-TX-" + UUID.randomUUID().toString().take(8).uppercase()

            pagarOnlineUseCase(pagoId, referencia)
                .onSuccess { comprobante ->
                    _uiState.value = PasarelaPagoUiState.Exito(comprobante)
                }
                .onFailure { error ->
                    _uiState.value = PasarelaPagoUiState.Error(error.message ?: "Error al procesar el pago online.")
                }
        }
    }
}
