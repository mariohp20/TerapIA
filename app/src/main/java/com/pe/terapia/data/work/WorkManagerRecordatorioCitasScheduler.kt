package com.pe.terapia.data.work

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.pe.terapia.data.repository.NotificacionesPreferenciasRepositoryImpl
import com.pe.terapia.domain.model.Rol
import com.pe.terapia.domain.repository.RecordatorioCitasScheduler
import java.util.concurrent.TimeUnit

class WorkManagerRecordatorioCitasScheduler(context: Context) : RecordatorioCitasScheduler {
    private val appContext = context.applicationContext
    private val workManager = WorkManager.getInstance(appContext)

    override fun programar(citaId: String, usuarioId: String, inicioEpochMillis: Long, rol: Rol) {
        if (citaId.isBlank() || usuarioId.isBlank()) return
        if (!NotificacionesPreferenciasRepositoryImpl(appContext).estanActivas(usuarioId)) return
        val restante = inicioEpochMillis - System.currentTimeMillis()
        workManager.cancelUniqueWork(legacyWorkName(citaId, rol))
        REMINDER_LEAD_TIMES.forEach { (stage, leadMillis) ->
            val uniqueName = workName(citaId, rol, stage)
            if (restante <= leadMillis) {
                workManager.cancelUniqueWork(uniqueName)
                return@forEach
            }
            val input = Data.Builder()
                .putString(RecordatorioCitaWorker.KEY_CITA_ID, citaId)
                .putString(RecordatorioCitaWorker.KEY_USUARIO_ID, usuarioId)
                .putLong(RecordatorioCitaWorker.KEY_INICIO, inicioEpochMillis)
                .putLong(RecordatorioCitaWorker.KEY_ANTICIPACION, leadMillis)
                .putString(RecordatorioCitaWorker.KEY_ROL, rol.valor)
                .build()
            val request = OneTimeWorkRequestBuilder<RecordatorioCitaWorker>()
                .setInputData(input)
                .setInitialDelay((restante - leadMillis).coerceAtLeast(0L), TimeUnit.MILLISECONDS)
                .addTag("recordatorio_cita")
                .addTag(userTag(usuarioId))
                .build()
            // Al cambiar la cita se sustituye la hora programada para cada aviso.
            workManager.enqueueUniqueWork(uniqueName, ExistingWorkPolicy.REPLACE, request)
        }
    }

    override fun cancelar(citaId: String) {
        if (citaId.isNotBlank()) {
            Rol.entries.forEach { rol ->
                REMINDER_LEAD_TIMES.forEach { (stage, _) ->
                    workManager.cancelUniqueWork(workName(citaId, rol, stage))
                }
                workManager.cancelUniqueWork(legacyWorkName(citaId, rol))
            }
        }
    }

    override fun cancelarDeUsuario(usuarioId: String) {
        if (usuarioId.isNotBlank()) workManager.cancelAllWorkByTag(userTag(usuarioId))
    }

    override fun programarFidelizacionLocal(usuarioId: String) {
        if (usuarioId.isBlank() || !NotificacionesPreferenciasRepositoryImpl(appContext).estanActivas(usuarioId)) return
        val request = PeriodicWorkRequestBuilder<RetentionWorker>(24, TimeUnit.HOURS)
            .setInputData(Data.Builder().putString(RetentionWorker.KEY_USUARIO_ID, usuarioId).build())
            .addTag("fidelizacion_local")
            .addTag(userTag(usuarioId))
            .build()
        workManager.enqueueUniquePeriodicWork(
            "fidelizacion_local_$usuarioId",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun workName(citaId: String, rol: Rol, stage: String) = "recordatorio_v3_cita_${rol.valor}_${stage}_$citaId"
    private fun legacyWorkName(citaId: String, rol: Rol) = "recordatorio_cita_${rol.valor}_$citaId"
    private fun userTag(usuarioId: String) = "recordatorio_usuario_$usuarioId"

    private companion object {
        val REMINDER_LEAD_TIMES = listOf(
            "24h" to 24L * 60 * 60 * 1000,
            "1h" to 60L * 60 * 1000,
            "5m" to 5L * 60 * 1000
        )
    }
}
