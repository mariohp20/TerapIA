package com.pe.terapia.data.repository

import com.pe.terapia.data.remote.firebase.GeminiDataSource
import com.pe.terapia.domain.model.ChatMessagge
import com.pe.terapia.domain.repository.ChatRepository
import kotlinx.coroutines.CancellationException

class ChatRepositoryImpl(
    private val geminiDataSource: GeminiDataSource
) : ChatRepository {

    override suspend fun enviarMensaje(userMessage: String, conversationHistory: List<ChatMessagge>): Result<String> = try {
        val clinicContext = """
            TerapIA es una clínica psicológica moderna en Perú.
            - Servicios: Psicología clínica, terapia de pareja, terapia infantil, orientación vocacional, manejo de ansiedad y estrés.
            - Costo estándar de consulta: S/ 50.00 PEN.
            - Métodos de pago: Efectivo en clínica (confirmado por el psicólogo tras la atención) o pago online simulado con pasarela tipo Mercado Pago.
            - Trámites en la app: Reserva de citas por especialidad y sede, visualización del código QR de la cita para el ingreso en clínica, consulta de historial de pagos y emisión de comprobantes (boletas) en PDF, así como visualización del historial clínico personal.
            - Regla estricta: No realizamos diagnósticos clínicos, terapia ni recetas médicas por chat. En caso de emergencia o crisis emocional, indicar inmediatamente que se comuniquen con líneas de emergencia locales o servicios médicos de urgencia.
        """.trimIndent()

        val conversationContext = conversationHistory.takeLast(6).joinToString("\n") { msg ->
            val sender = if (msg.fromUser == "user") "Paciente" else "Asistente"
            "$sender: ${msg.content}"
        }

        val respuesta = geminiDataSource.generateResponse(userMessage, clinicContext, conversationContext)
        Result.success(respuesta)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
