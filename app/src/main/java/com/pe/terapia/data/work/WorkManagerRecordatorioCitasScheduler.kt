package com.pe.terapia.data.work

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.pe.terapia.domain.model.Rol
import com.pe.terapia.domain.repository.RecordatorioCitasScheduler
import java.util.concurrent.TimeUnit

class WorkManagerRecordatorioCitasScheduler(context: Context) : RecordatorioCitasScheduler {
    private val workManager = WorkManager.getInstance(context.applicationContext)

    override fun programar(citaId: String, inicioEpochMillis: Long, rol: Rol) {
        if (citaId.isBlank()) return
        val restante = inicioEpochMillis - System.currentTimeMillis()
        val anticipacion = when {
            restante > DAY_MILLIS -> DAY_MILLIS
            restante > HOUR_MILLIS -> HOUR_MILLIS
            restante > FIVE_MINUTES_MILLIS -> FIVE_MINUTES_MILLIS
            else -> return
        }
        val demora = (restante - anticipacion).coerceAtLeast(0L)
        val input = Data.Builder()
            .putString(RecordatorioCitaWorker.KEY_CITA_ID, citaId)
            .putLong(RecordatorioCitaWorker.KEY_INICIO, inicioEpochMillis)
            .putString(RecordatorioCitaWorker.KEY_ROL, rol.valor)
            .build()
        val request = OneTimeWorkRequestBuilder<RecordatorioCitaWorker>()
            .setInputData(input)
            .setInitialDelay(demora, TimeUnit.MILLISECONDS)
            .addTag("recordatorio_cita")
            .build()

        workManager.enqueueUniqueWork(workName(citaId, rol), ExistingWorkPolicy.KEEP, request)
    }

    override fun cancelar(citaId: String) {
        if (citaId.isNotBlank()) {
            Rol.entries.forEach { rol -> workManager.cancelUniqueWork(workName(citaId, rol)) }
        }
    }

    private fun workName(citaId: String, rol: Rol) = "recordatorio_cita_${rol.valor}_$citaId"

    private companion object {
        const val DAY_MILLIS = 24 * 60 * 60 * 1000L
        const val HOUR_MILLIS = 60 * 60 * 1000L
        const val FIVE_MINUTES_MILLIS = 5 * 60 * 1000L
    }
}
