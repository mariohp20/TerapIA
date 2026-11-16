package com.pe.terapia.data.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pe.terapia.MainActivity
import com.pe.terapia.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.pe.terapia.data.local.room.AppDatabase
import com.pe.terapia.data.local.room.NotificacionEntity
import com.pe.terapia.data.repository.NotificacionesPreferenciasRepositoryImpl
import com.pe.terapia.domain.model.Rol
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.tasks.await

class RecordatorioCitaWorker(
    context: Context,
    parameters: WorkerParameters
) : CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result {
        val citaId = inputData.getString(KEY_CITA_ID).orEmpty()
        val usuarioId = inputData.getString(KEY_USUARIO_ID).orEmpty()
        val inicio = inputData.getLong(KEY_INICIO, 0L)
        val anticipacionMillis = inputData.getLong(KEY_ANTICIPACION, 0L)
        val rol = inputData.getString(KEY_ROL).orEmpty()
        if (citaId.isBlank() || usuarioId.isBlank() || inicio < System.currentTimeMillis() - LATE_DELIVERY_TOLERANCE_MILLIS) return Result.success()
        if (!NotificacionesPreferenciasRepositoryImpl(applicationContext).estanActivas(usuarioId)) return Result.success()
        if (FirebaseAuth.getInstance().currentUser?.uid != usuarioId) return Result.success()
        val citaActual = try {
            FirebaseFirestore.getInstance().collection("citas").document(citaId).get().await()
        } catch (_: Exception) {
            return Result.retry()
        }
        if (!citaActual.exists() || citaActual.getString("estado") != "PROGRAMADA") return Result.success()
        val inicioActual = citaActual.getTimestamp("inicioAt")?.toDate()?.time
            ?: citaActual.getLong("inicioEpochMillis")
            ?: return Result.success()
        // Evita notificar con la hora anterior si la cita fue reprogramada
        // mientras el trabajo local seguía esperando.
        if (inicioActual != inicio) return Result.success()

        crearCanalSiHaceFalta()
        val zona = ZoneId.of("America/Lima")
        val fechaHora = DateTimeFormatter.ofPattern("EEE d 'de' MMMM, HH:mm", Locale("es", "PE"))
            .withZone(zona)
            .format(Instant.ofEpochMilli(inicio))
        val cuerpo = if (rol == Rol.PSICOLOGO.valor) {
            "Tienes una cita programada para el $fechaHora."
        } else {
            "Recuerda tu cita del $fechaHora."
        }
        val notificationDao = AppDatabase.getDatabase(applicationContext).notificacionDao()
        val etapaRecordatorio = etapa(anticipacionMillis)
        val notificacionId = "recordatorio:$usuarioId:$citaId:$etapaRecordatorio"
        val titulo = when (etapaRecordatorio) {
            "24h" -> "Tu cita es en 24 horas"
            "1h" -> "Tu cita es en una hora"
            "5m" -> "Tu cita empieza pronto"
            else -> "Recordatorio de cita"
        }
        val cuerpoConAnticipacion = "$cuerpo (${etapaRecordatorio.replace("h", " hora").replace("m", " min")})."
        val existente = notificationDao.obtenerPorId(notificacionId)
        if (existente?.alertaMostrada == true) return Result.success()
        if (existente == null) {
            val ahora = System.currentTimeMillis()
            notificationDao.eliminarAnterioresA(ahora - RETENCION_MILLIS)
            notificationDao.insertarSiNoExiste(
                NotificacionEntity(
                    id = notificacionId,
                    usuarioId = usuarioId,
                    titulo = titulo,
                    mensaje = cuerpoConAnticipacion,
                    tipo = TIPO_RECORDATORIO_CITA,
                    fechaCreacionMillis = ahora,
                    citaId = citaId
                )
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return Result.success()
        if (!NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) return Result.success()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID)?.importance == NotificationManager.IMPORTANCE_NONE) return Result.success()
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            "$citaId:$etapaRecordatorio".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(titulo)
            .setContentText(cuerpoConAnticipacion)
            .setStyle(NotificationCompat.BigTextStyle().bigText(cuerpoConAnticipacion))
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        return try {
            NotificationManagerCompat.from(applicationContext).notify("$citaId:$etapaRecordatorio".hashCode(), notification)
            notificationDao.marcarAlertaMostrada(notificacionId)
            Result.success()
        } catch (_: SecurityException) {
            Result.success()
        }
    }

    private fun crearCanalSiHaceFalta() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Recordatorios de citas", NotificationManager.IMPORTANCE_DEFAULT)
                    .apply { description = "Avisos locales antes de una cita programada" }
            )
        }
    }

    companion object {
        const val KEY_CITA_ID = "cita_id"
        const val KEY_USUARIO_ID = "usuario_id"
        const val KEY_INICIO = "inicio_epoch_millis"
        const val KEY_ANTICIPACION = "anticipacion_millis"
        const val KEY_ROL = "rol"
        const val CHANNEL_ID = "recordatorios_citas"
        const val TIPO_RECORDATORIO_CITA = "RECORDATORIO_CITA"
        private const val LATE_DELIVERY_TOLERANCE_MILLIS = 90 * 60 * 1000L
        private const val RETENCION_MILLIS = 180L * 24 * 60 * 60 * 1000

        private fun etapa(anticipacionMillis: Long): String = when (anticipacionMillis) {
            24L * 60 * 60 * 1000 -> "24h"
            60L * 60 * 1000 -> "1h"
            5L * 60 * 1000 -> "5m"
            else -> "recordatorio"
        }
    }
}
