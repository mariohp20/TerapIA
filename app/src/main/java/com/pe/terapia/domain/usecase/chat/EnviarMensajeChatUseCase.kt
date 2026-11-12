package com.pe.terapia.domain.usecase.chat

import com.pe.terapia.domain.model.ChatMessagge
import com.pe.terapia.domain.repository.ChatRepository
import kotlinx.coroutines.CancellationException

class EnviarMensajeChatUseCase(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(userMessage: String, history: List<ChatMessagge>): Result<String> {
        if (userMessage.isBlank()) {
            return Result.failure(IllegalArgumentException("El mensaje no puede estar vacío."))
        }
        return try {
            repository.enviarMensaje(userMessage, history)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
