package com.pe.terapia.presentation.home.paciente.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.ChatMessagge
import com.pe.terapia.domain.usecase.chat.EnviarMensajeChatUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class TerapiaChatUiState(
    val mensajes: List<ChatMessagge> = emptyList(),
    val mensajeInput: String = "",
    val cargando: Boolean = false,
    val error: String? = null
)

class TerapiaChatViewModel(
    private val enviarMensajeChatUseCase: EnviarMensajeChatUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TerapiaChatUiState())
    val uiState: StateFlow<TerapiaChatUiState> = _uiState.asStateFlow()

    init {
        val bienvenida = ChatMessagge(
            id = UUID.randomUUID().toString(),
            content = "¡Hola! Soy el asistente virtual de TerapIA. ¿En qué puedo orientarte hoy sobre nuestros servicios, trámites o preparación de citas?",
            fromUser = "bot",
            timestamp = System.currentTimeMillis()
        )
        _uiState.update { it.copy(mensajes = listOf(bienvenida)) }
    }

    fun actualizarMensaje(texto: String) {
        _uiState.update { it.copy(mensajeInput = texto) }
    }

    fun enviarMensaje(texto: String) {
        val prompt = texto.trim()
        if (prompt.isBlank() || _uiState.value.cargando) return

        val userMsg = ChatMessagge(
            id = UUID.randomUUID().toString(),
            content = prompt,
            fromUser = "user",
            timestamp = System.currentTimeMillis()
        )

        val currentHistory = _uiState.value.mensajes
        _uiState.update { state ->
            state.copy(
                mensajes = state.mensajes + userMsg,
                mensajeInput = "",
                cargando = true,
                error = null
            )
        }

        viewModelScope.launch {
            enviarMensajeChatUseCase(prompt, currentHistory)
                .onSuccess { reply ->
                    val botMsg = ChatMessagge(
                        id = UUID.randomUUID().toString(),
                        content = reply,
                        fromUser = "bot",
                        timestamp = System.currentTimeMillis()
                    )
                    _uiState.update { it.copy(mensajes = it.mensajes + botMsg, cargando = false) }
                }
                .onFailure { error ->
                    val errorMsg = ChatMessagge(
                        id = UUID.randomUUID().toString(),
                        content = "Lo siento, ocurrió un error al procesar tu consulta: ${error.localizedMessage ?: "Inténtalo de nuevo."}",
                        fromUser = "bot",
                        timestamp = System.currentTimeMillis()
                    )
                    _uiState.update { it.copy(mensajes = it.mensajes + errorMsg, cargando = false, error = error.message) }
                }
        }
    }
}
