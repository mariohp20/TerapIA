package com.pe.terapia.data.repository

import com.pe.terapia.data.remote.firebase.GeminiDataSource
import com.pe.terapia.domain.model.ChatMessagge
import com.pe.terapia.domain.repository.ChatRepository
import kotlinx.coroutines.CancellationException

class ChatRepositoryImpl(
    private val geminiDataSource: GeminiDataSource
) : ChatRepository {

    override suspend fun enviarMensaje(userMessage: String, conversationHistory: List<ChatMessagge>): Result<String> = try {
        val respuesta = geminiDataSource.generateResponse(userMessage, conversationHistory)
        Result.success(respuesta)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
