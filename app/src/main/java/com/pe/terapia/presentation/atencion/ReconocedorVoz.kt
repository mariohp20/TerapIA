package com.pe.terapia.presentation.atencion

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import java.util.concurrent.atomic.AtomicLong

/** Adaptador de Android SpeechRecognizer. No almacena audio ni texto. */
class ReconocedorVoz(context: Context) {
    private val appContext = context.applicationContext
    private var recognizer: SpeechRecognizer? = null
    private val sessionId = AtomicLong(0L)
    private var stopRequested = false
    private var partialBuffer = ""

    fun disponible(): Boolean = SpeechRecognizer.isRecognitionAvailable(appContext)

    fun iniciar(
        alResultado: (String) -> Unit,
        alParcial: (String) -> Unit,
        alError: (String) -> Unit,
        alFin: () -> Unit
    ) {
        cancelar()
        val currentSession = sessionId.incrementAndGet()
        stopRequested = false
        partialBuffer = ""
        var finalizado = false
        fun finalizarSesion() {
            if (sessionId.get() != currentSession || finalizado) return
            finalizado = true
            recognizer?.runCatching { destroy() }
            recognizer = null
            alFin()
        }
        if (!disponible()) {
            alError("El reconocimiento de voz no está disponible en este dispositivo.")
            alFin()
            return
        }

        runCatching {
            SpeechRecognizer.createSpeechRecognizer(appContext).also { servicio ->
                recognizer = servicio
                servicio.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) = Unit
                    override fun onBeginningOfSpeech() = Unit
                    override fun onRmsChanged(rmsdB: Float) = Unit
                    override fun onBufferReceived(buffer: ByteArray?) = Unit
                    override fun onEndOfSpeech() = Unit
                    override fun onPartialResults(partialResults: Bundle?) {
                        if (sessionId.get() != currentSession || finalizado) return
                        val texto = partialResults
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()
                        if (!texto.isNullOrBlank()) {
                            partialBuffer = texto
                            alParcial(texto)
                        }
                    }
                    override fun onEvent(eventType: Int, params: Bundle?) = Unit

                    override fun onResults(results: Bundle?) {
                        if (sessionId.get() != currentSession || finalizado) return
                        val texto = results
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()
                            .orEmpty()
                        val resultadoFinal = texto.ifBlank { partialBuffer }
                        if (resultadoFinal.isBlank()) {
                            if (stopRequested) finalizarSesion()
                            else {
                                alError("No se reconoció voz. Puedes intentarlo otra vez.")
                                finalizarSesion()
                            }
                        } else {
                            alResultado(resultadoFinal)
                            partialBuffer = ""
                            if (stopRequested) finalizarSesion() else escucharSegmentoActual()
                        }
                    }

                    override fun onError(error: Int) {
                        if (sessionId.get() != currentSession || finalizado) return
                        if (stopRequested && error in setOf(
                                SpeechRecognizer.ERROR_NO_MATCH,
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT
                            )
                        ) {
                            val textoReconocido = partialBuffer
                            if (textoReconocido.isNotBlank()) alResultado(textoReconocido)
                            finalizarSesion()
                            return
                        }
                        if (!stopRequested && error in setOf(
                                SpeechRecognizer.ERROR_NO_MATCH,
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT
                            )
                        ) {
                            escucharSegmentoActual()
                            return
                        }
                        alError(mensajeError(error))
                        finalizarSesion()
                    }

                    private fun escucharSegmentoActual() {
                        if (sessionId.get() != currentSession || finalizado || stopRequested) return
                        runCatching {
                            val siguienteIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-US")
                                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                            }
                            servicio.startListening(siguienteIntent)
                        }.onFailure {
                            alError("Se interrumpió el dictado. El texto reconocido hasta ahora se conserva.")
                            finalizarSesion()
                        }
                    }
                })
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    // No fijar una variante regional que el reconocedor OEM quizá no tenga descargada.
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-US")
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                }
                servicio.startListening(intent)
            }
        }.onFailure { error ->
            Log.e("ReconocedorVoz", "No se pudo iniciar SpeechRecognizer", error)
            detener()
            alError("Android no pudo iniciar el servicio de reconocimiento de voz (${error.javaClass.simpleName}). Revisa el servicio de voz predeterminado del teléfono.")
            alFin()
        }
    }

    fun detener() {
        stopRequested = true
        recognizer?.runCatching { stopListening() }
    }

    fun cancelar() {
        sessionId.incrementAndGet()
        recognizer?.runCatching { cancel(); destroy() }
        recognizer = null
    }

    private fun mensajeError(error: Int): String = when (error) {
        SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED -> "El servicio de voz no tiene disponible español. En Ajustes del teléfono, descarga el paquete de reconocimiento de voz en español o prueba con el idioma del sistema."
        SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE -> "El paquete de reconocimiento de voz en español aún no está descargado. Descárgalo desde los ajustes de voz de Google e inténtalo de nuevo."
        SpeechRecognizer.ERROR_NETWORK,
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "No hay conexión con el servicio de voz. Revisa tu internet e inténtalo de nuevo."
        SpeechRecognizer.ERROR_AUDIO -> "No se pudo acceder correctamente al micrófono. Cierra otras apps que lo estén usando e inténtalo de nuevo."
        SpeechRecognizer.ERROR_CLIENT -> "El reconocedor interrumpió la solicitud. Verifica el permiso de micrófono e inténtalo otra vez."
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Se necesita permiso para usar el micrófono."
        SpeechRecognizer.ERROR_NO_MATCH,
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No se detectó voz. Inténtalo de nuevo."
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "El reconocimiento está ocupado. Espera un momento e inténtalo de nuevo."
        SpeechRecognizer.ERROR_SERVER -> "El servicio de voz tuvo un problema temporal. Inténtalo de nuevo."
        SpeechRecognizer.ERROR_TOO_MANY_REQUESTS -> "Se hicieron demasiados intentos seguidos. Espera un momento y vuelve a probar."
        else -> "El servicio de voz devolvió un error ($error). Revisa que Google tenga habilitado el reconocimiento de voz en español e inténtalo otra vez."
    }
}
