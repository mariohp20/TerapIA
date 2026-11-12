package com.pe.terapia.domain.repository

import com.pe.terapia.domain.model.ChatMessagge

interface ChatRepository {
    suspend fun enviarMensaje(userMessage: String, conversationHistory: List<ChatMessagge>): Result<String>
}
