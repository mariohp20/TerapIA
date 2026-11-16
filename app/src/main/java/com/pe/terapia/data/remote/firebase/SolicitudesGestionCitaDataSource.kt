package com.pe.terapia.data.remote.firebase

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Transaction
import com.google.firebase.Timestamp
import com.pe.terapia.domain.model.SolicitudGestionCita
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import kotlinx.coroutines.tasks.await

class SolicitudesGestionCitaDataSource(private val firestore: FirebaseFirestore) {
    suspend fun obtenerPendientes(): List<SolicitudGestionCita> {
        val requests = firestore.collection("solicitudesGestionCitas")
            .whereEqualTo("estado", "PENDIENTE").get().await().documents
        return requests.map { request ->
            val cita = request.getString("citaId")?.let { firestore.collection("citas").document(it).get().await() }
            SolicitudGestionCita(
                id = request.id,
                citaId = request.getString("citaId").orEmpty(),
                pacienteNombre = cita?.getString("pacienteNombre").orEmpty().ifBlank { "Paciente" },
                tipo = request.getString("tipo").orEmpty(),
                fecha = cita?.getString("fecha").orEmpty(),
                hora = cita?.getString("hora").orEmpty(),
                pagada = cita?.getString("estadoPago") == "PAGADO" || cita?.getBoolean("pagado") == true,
                fechaSolicitada = request.getString("fechaSolicitada"),
                horaSolicitada = request.getString("horaSolicitada")
            )
        }.sortedWith(compareBy({ it.fecha }, { it.hora }))
    }

    suspend fun resolver(solicitudId: String, adminUid: String, decision: String) {
        require(solicitudId.isNotBlank() && adminUid.isNotBlank())
        require(decision in setOf("RECHAZAR", "ACEPTAR_CANCELACION", "ACEPTAR_REPROGRAMACION"))
        val requestRef = firestore.collection("solicitudesGestionCitas").document(solicitudId)
        if (decision == "RECHAZAR") {
            firestore.runTransaction { tx ->
                val request = tx.get(requestRef)
                check(request.getString("estado") == "PENDIENTE") { "La solicitud ya fue atendida." }
                tx.update(requestRef, mapOf(
                    "estado" to "RECHAZADA",
                    "resueltaEn" to FieldValue.serverTimestamp(),
                    "resueltaPor" to adminUid
                ))
                val citaId = request.getString("citaId").orEmpty()
                val patientId = request.getString("pacienteId").orEmpty()
                val eventRef = firestore.collection("notificaciones")
                    .document("gestion_${citaId}_${request.getTimestamp("creadaEn")?.toDate()?.time ?: System.currentTimeMillis()}_RECHAZADA")
                tx.set(eventRef, mapOf(
                    "id" to eventRef.id,
                    "usuarioId" to patientId,
                    "destinatarioRol" to "paciente",
                    "titulo" to "Solicitud no aprobada",
                    "mensaje" to "La clínica no aprobó tu solicitud. Tu cita actual sigue vigente; puedes revisar opciones desde su detalle.",
                    "tipo" to "GESTION_RECHAZADA",
                    "solicitudId" to citaId,
                    "citaId" to citaId,
                    "fechaCreacion" to FieldValue.serverTimestamp(),
                    "leida" to false
                ))
                Unit
            }.await()
            return
        }

        firestore.runTransaction { tx ->
            val request = tx.get(requestRef)
            val expectedType = if (decision == "ACEPTAR_CANCELACION") "CANCELACION" else "REPROGRAMACION"
            check(request.getString("estado") == "PENDIENTE" && request.getString("tipo") == expectedType) {
                "La solicitud ya no está pendiente o su tipo cambió."
            }
            val appointmentRef = firestore.collection("citas").document(request.getString("citaId").orEmpty())
            if (decision == "ACEPTAR_REPROGRAMACION") {
                moverCitaAlHorarioPropuesto(tx, request, appointmentRef, requestRef, adminUid)
            } else {
                cancelarCitaYLiberarTurno(tx, appointmentRef, requestRef, adminUid)
            }
            val citaId = request.getString("citaId").orEmpty()
            val patientId = request.getString("pacienteId").orEmpty()
            val acceptedType = if (decision == "ACEPTAR_REPROGRAMACION") "REPROGRAMACION" else "CANCELACION"
            val eventRef = firestore.collection("notificaciones")
                .document("gestion_${citaId}_${request.getTimestamp("creadaEn")?.toDate()?.time ?: System.currentTimeMillis()}_ACEPTADA")
            val message = if (acceptedType == "REPROGRAMACION") {
                "Tu solicitud fue aprobada. La cita ahora es el ${request.getString("fechaSolicitada")} a las ${request.getString("horaSolicitada")}."
            } else "Tu solicitud de cancelación fue aprobada. Si había un pago, la clínica revisará por separado si corresponde una devolución."
            tx.set(eventRef, mapOf(
                "id" to eventRef.id,
                "usuarioId" to patientId,
                "destinatarioRol" to "paciente",
                "titulo" to if (acceptedType == "REPROGRAMACION") "Cita reprogramada" else "Cancelación aprobada",
                "mensaje" to message,
                "tipo" to if (acceptedType == "REPROGRAMACION") "GESTION_REPROGRAMADA" else "GESTION_CANCELADA",
                "solicitudId" to citaId,
                "citaId" to citaId,
                "fechaCreacion" to FieldValue.serverTimestamp(),
                "leida" to false
            ))
            Unit
        }.await()
    }

    private fun cancelarCitaYLiberarTurno(
        tx: Transaction,
        appointmentRef: DocumentReference,
        requestRef: DocumentReference,
        adminUid: String
    ) {
        val appointment = tx.get(appointmentRef)
        check(appointment.exists() && appointment.getString("estado") == "PROGRAMADA") {
            "La cita ya no está programada."
        }
        val reservationId = appointment.getString("reservaId").orEmpty()
        check(reservationId.isNotBlank()) { "La cita no tiene una reserva asociada." }
        val reservationRef = firestore.collection("reservas").document(reservationId)
        val reservation = tx.get(reservationRef)
        check(reservation.exists() && reservation.getString("estado") == "CONFIRMADA") {
            "La reserva ya no está activa."
        }
        val lockRef = firestore.collection("horariosLaborales").document(reservation.getString("horarioId").orEmpty())
            .collection("turnos").document(reservation.getString("slotId").orEmpty())
        val lock = tx.get(lockRef)
        check(lock.exists() && lock.getString("reservaId") == reservationId) { "El turno no corresponde a la reserva." }
        tx.update(appointmentRef, mapOf("estado" to "CANCELADA", "canceladaEn" to FieldValue.serverTimestamp()))
        tx.update(reservationRef, "estado", "CANCELADA")
        tx.delete(lockRef)
        tx.update(requestRef, mapOf(
            "estado" to "ACEPTADA",
            "resueltaEn" to FieldValue.serverTimestamp(),
            "resueltaPor" to adminUid,
            "pagoPendienteRevision" to (appointment.getString("estadoPago") == "PAGADO" || appointment.getBoolean("pagado") == true)
        ))
    }

    private fun moverCitaAlHorarioPropuesto(
        tx: Transaction,
        request: DocumentSnapshot,
        appointmentRef: DocumentReference,
        requestRef: DocumentReference,
        adminUid: String
    ) {
        val appointment = tx.get(appointmentRef)
        check(appointment.exists() && appointment.getString("estado") == "PROGRAMADA") {
            "La cita ya no está programada."
        }
        val reservationId = appointment.getString("reservaId").orEmpty()
        check(reservationId.isNotBlank()) { "La cita no tiene una reserva asociada." }
        val reservationRef = firestore.collection("reservas").document(reservationId)
        val reservation = tx.get(reservationRef)
        check(reservation.exists() && reservation.getString("estado") == "CONFIRMADA") {
            "La reserva ya no está activa."
        }

        val inicioSolicitado = request.getLong("inicioEpochMillisSolicitado")
            ?: error("La solicitud no contiene un horario propuesto.")
        val horarioIdSolicitado = request.getString("horarioIdSolicitado").orEmpty()
        val bloqueIdSolicitado = request.getString("bloqueIdSolicitado").orEmpty()
        val fechaSolicitada = request.getString("fechaSolicitada").orEmpty()
        val horaSolicitada = request.getString("horaSolicitada").orEmpty()
        val duracionSolicitada = request.getLong("duracionMinutosSolicitada")?.toInt()
            ?: error("La solicitud no contiene la duración del horario.")
        val psicologoId = appointment.getString("psicologoId").orEmpty()
        val sedeId = appointment.getString("sedeId").orEmpty()
        val horarioEsperado = "$psicologoId~$sedeId"
        check(horarioIdSolicitado == horarioEsperado && horarioIdSolicitado == reservation.getString("horarioId")) {
            "El horario propuesto no corresponde al profesional y sede de esta cita."
        }
        check(inicioSolicitado > System.currentTimeMillis()) { "El horario propuesto ya pasó." }
        check(duracionSolicitada == (reservation.getLong("duracionMinutos")?.toInt() ?: 0)) {
            "La duración del horario propuesto no coincide con la cita."
        }

        val zona = ZoneId.of("America/Lima")
        val fechaHoraLocal = Instant.ofEpochMilli(inicioSolicitado).atZone(zona)
        check(fechaHoraLocal.toLocalDate().toString() == fechaSolicitada &&
            fechaHoraLocal.format(DateTimeFormatter.ofPattern("HH:mm")) == horaSolicitada) {
            "La fecha y hora propuestas no coinciden con su marca de tiempo."
        }
        val nuevoSlotId = inicioSolicitado.toString()
        val horarioRef = firestore.collection("horariosLaborales").document(horarioIdSolicitado)
        val nuevoTurnoRef = horarioRef.collection("turnos").document(nuevoSlotId)
        val antiguoHorarioId = reservation.getString("horarioId").orEmpty()
        val antiguoSlotId = reservation.getString("slotId").orEmpty()
        val antiguoTurnoRef = firestore.collection("horariosLaborales").document(antiguoHorarioId)
            .collection("turnos").document(antiguoSlotId)
        val scheduleRef = firestore.collection("horariosLaborales").document(horarioIdSolicitado)
        val exceptionRef = firestore.collection("excepcionesHorario")
            .document("${horarioIdSolicitado}~${nuevoSlotId}")
        val blockRef = scheduleRef.collection("bloques").document(bloqueIdSolicitado)

        // Leer todo antes de iniciar escrituras para cumplir las transacciones Firestore.
        val schedule = tx.get(scheduleRef)
        val exception = tx.get(exceptionRef)
        val block = tx.get(blockRef)
        val oldLock = tx.get(antiguoTurnoRef)
        val newLock = tx.get(nuevoTurnoRef)
        check(schedule.exists() && schedule.getBoolean("activo") == true &&
            schedule.getString("psicologoId") == psicologoId && schedule.getString("sedeId") == sedeId &&
            schedule.getString("zonaHoraria") == "America/Lima" &&
            schedule.getLong("duracionMinutos")?.toInt() == duracionSolicitada) {
            "El horario laboral ya no está activo o cambió."
        }
        check(!newLock.exists() || nuevoSlotId == antiguoSlotId) {
            "El horario propuesto acaba de ser reservado por otra persona. Pide al paciente que elija otro."
        }
        check(oldLock.exists() && oldLock.getString("reservaId") == reservationId) {
            "No se encontró el turno original de esta reserva."
        }
        check(nuevoSlotId != antiguoSlotId) { "Elige una hora distinta a la cita actual." }

        val minuteOfDay = fechaHoraLocal.hour * 60 + fechaHoraLocal.minute
        if (exception.exists()) {
            check(exception.getString("tipo") == "DISPONIBILIDAD_EXTRA" &&
                exception.getLong("inicioEpochMillis") == inicioSolicitado &&
                bloqueIdSolicitado == exception.id) {
                "El horario propuesto fue bloqueado o la excepción cambió."
            }
        } else {
            val blockStart = block.getLong("inicioMinuto")?.toInt() ?: Int.MAX_VALUE
            val blockEnd = block.getLong("finMinuto")?.toInt() ?: Int.MIN_VALUE
            check(block.exists() && block.getBoolean("activo") == true &&
                block.getLong("diaSemana")?.toInt() == fechaHoraLocal.dayOfWeek.value &&
                minuteOfDay >= blockStart && minuteOfDay + duracionSolicitada <= blockEnd &&
                (minuteOfDay - blockStart) % duracionSolicitada == 0) {
                "El turno propuesto ya no pertenece a un bloque laboral disponible."
            }
        }

        val inicioAt = Timestamp(Date(inicioSolicitado))
        tx.update(reservationRef, mapOf(
            "fecha" to fechaSolicitada,
            "hora" to horaSolicitada,
            "inicioEpochMillis" to inicioSolicitado,
            "inicioAt" to inicioAt,
            "slotId" to nuevoSlotId,
            "bloqueId" to bloqueIdSolicitado
        ))
        tx.update(appointmentRef, mapOf(
            "fecha" to fechaSolicitada,
            "hora" to horaSolicitada,
            "inicioEpochMillis" to inicioSolicitado,
            "inicioAt" to inicioAt,
            "slotId" to nuevoSlotId
        ))
        tx.set(nuevoTurnoRef, mapOf(
            "reservaId" to reservationId,
            "slotId" to nuevoSlotId,
            "fecha" to fechaSolicitada,
            "hora" to horaSolicitada,
            "inicioEpochMillis" to inicioSolicitado,
            "inicioAt" to inicioAt
        ))
        tx.delete(antiguoTurnoRef)
        tx.update(requestRef, mapOf(
            "estado" to "ACEPTADA",
            "resueltaEn" to FieldValue.serverTimestamp(),
            "resueltaPor" to adminUid
        ))
    }
}
