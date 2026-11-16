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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.pe.terapia.MainActivity
import com.pe.terapia.R
import com.pe.terapia.data.local.room.AppDatabase
import com.pe.terapia.data.local.room.NotificacionEntity
import com.pe.terapia.data.repository.NotificacionesPreferenciasRepositoryImpl
import kotlinx.coroutines.tasks.await

/** Seguimiento local de demostración: procesa solamente al paciente de este dispositivo. */
class RetentionWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val uid = inputData.getString(KEY_USUARIO_ID).orEmpty()
        if (uid.isBlank() || !NotificacionesPreferenciasRepositoryImpl(applicationContext).estanActivas(uid)) return Result.success()
        if (FirebaseAuth.getInstance().currentUser?.uid != uid) return Result.success()
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val profile = firestore.collection("usuarios").document(uid).get().await()
            if (!profile.exists() || profile.getString("rol") != "paciente" || profile.getBoolean("activo") != true) return Result.success()

            val appointments = firestore.collection("citas").whereEqualTo("pacienteId", uid).get().await().documents
            val now = System.currentTimeMillis()
            val hasUpcomingAppointment = appointments.any {
                it.getString("estado") == "PROGRAMADA" && (it.getLong("inicioEpochMillis") ?: 0L) > now
            }
            if (hasUpcomingAppointment) return Result.success()

            val lastAttended = appointments
                .filter { it.getString("estado") == "COMPLETADA" }
                .maxByOrNull { it.getLong("inicioEpochMillis") ?: 0L }
                ?: return Result.success()
            val lastSessionAt = lastAttended.getLong("inicioEpochMillis") ?: return Result.success()
            if (now - lastSessionAt < THREE_WEEKS_MILLIS) return Result.success()

            val notificationId = "fidelizacion:$uid:${lastAttended.id}"
            val dao = AppDatabase.getDatabase(applicationContext).notificacionDao()
            if (dao.obtenerPorId(notificationId) != null) return Result.success()
            dao.eliminarAnterioresA(now - RETENCION_MILLIS)
            dao.insertarSiNoExiste(
                NotificacionEntity(
                    id = notificationId,
                    usuarioId = uid,
                    titulo = "Seguimiento de bienestar",
                    mensaje = "Hace un tiempo que no registras una sesión. Si deseas retomar tu atención, puedes revisar tus opciones en TerapIA.",
                    tipo = TIPO_FIDELIZACION,
                    fechaCreacionMillis = now
                )
            )
            mostrarAvisoSistema(notificationId)
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private fun mostrarAvisoSistema(id: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        if (!NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) return
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(NotificationChannel(CHANNEL_ID, "Seguimiento de bienestar", NotificationManager.IMPORTANCE_DEFAULT))
        }
        val intent = PendingIntent.getActivity(
            applicationContext, id.hashCode(), Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        runCatching {
            NotificationManagerCompat.from(applicationContext).notify(
                id.hashCode(),
                NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Seguimiento de bienestar")
                    .setContentText("TerapIA tiene un mensaje para ti.")
                    .setContentIntent(intent)
                    .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                    .setAutoCancel(true)
                    .build()
            )
        }
    }

    companion object {
        const val KEY_USUARIO_ID = "retention_user_id"
        const val TIPO_FIDELIZACION = "SEGUIMIENTO_BIENESTAR"
        private const val CHANNEL_ID = "seguimiento_bienestar"
        private const val THREE_WEEKS_MILLIS = 21L * 24 * 60 * 60 * 1000
        private const val RETENCION_MILLIS = 180L * 24 * 60 * 60 * 1000
    }
}
