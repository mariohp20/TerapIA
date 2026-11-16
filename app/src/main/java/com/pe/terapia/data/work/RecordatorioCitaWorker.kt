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
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.pe.terapia.MainActivity
import com.pe.terapia.R
import com.pe.terapia.domain.model.Rol
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class RecordatorioCitaWorker(
    context: Context,
    parameters: WorkerParameters
) : Worker(context, parameters) {

    override fun doWork(): Result {
        val citaId = inputData.getString(KEY_CITA_ID).orEmpty()
        val inicio = inputData.getLong(KEY_INICIO, 0L)
        val rol = inputData.getString(KEY_ROL).orEmpty()
        if (citaId.isBlank() || inicio < System.currentTimeMillis() - LATE_DELIVERY_TOLERANCE_MILLIS) return Result.success()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return Result.success()

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
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            citaId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Recordatorio de cita")
            .setContentText(cuerpo)
            .setStyle(NotificationCompat.BigTextStyle().bigText(cuerpo))
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        return try {
            NotificationManagerCompat.from(applicationContext).notify(citaId.hashCode(), notification)
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
        const val KEY_INICIO = "inicio_epoch_millis"
        const val KEY_ROL = "rol"
        const val CHANNEL_ID = "recordatorios_citas"
        private const val LATE_DELIVERY_TOLERANCE_MILLIS = 30 * 60 * 1000L
    }
}
