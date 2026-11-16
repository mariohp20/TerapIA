package com.pe.terapia.domain.repository

import com.pe.terapia.domain.model.Rol

/** Puerto para programar recordatorios locales sin acoplar los casos de uso a WorkManager. */
interface RecordatorioCitasScheduler {
    fun programar(citaId: String, usuarioId: String, inicioEpochMillis: Long, rol: Rol)
    fun cancelar(citaId: String)
    fun cancelarDeUsuario(usuarioId: String)
    fun programarFidelizacionLocal(usuarioId: String)
}
