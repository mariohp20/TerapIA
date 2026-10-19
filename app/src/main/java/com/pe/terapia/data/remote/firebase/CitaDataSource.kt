package com.pe.terapia.data.remote.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.pe.terapia.domain.model.BloqueHorario
import com.pe.terapia.domain.model.Cita
import com.pe.terapia.domain.model.CitaPacienteDetalle
import com.pe.terapia.domain.model.DURACION_CITA_MINUTOS
import com.pe.terapia.domain.model.EstadoCita
import com.pe.terapia.domain.model.EstadoReserva
import com.pe.terapia.domain.model.Especialidad
import com.pe.terapia.domain.model.ExcepcionHorario
import com.pe.terapia.domain.model.HorarioLaboral
import com.pe.terapia.domain.model.Reserva
import com.pe.terapia.domain.model.Rol
import com.pe.terapia.domain.model.Sede
import com.pe.terapia.domain.model.SlotDisponible
import com.pe.terapia.domain.model.TipoExcepcionHorario
import com.pe.terapia.domain.model.Psicologo
import com.pe.terapia.domain.model.ProximaCita
import com.pe.terapia.domain.usecase.citas.GenerarSlotsDisponibles
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CancellationException

class CitaDataSource(private val firestore: FirebaseFirestore) {
    suspend fun obtenerProximaCita(pacienteId: String): ProximaCita? {
        require(pacienteId.isNotBlank()) {
            "No se pudo identificar al paciente."
        }

        val now = System.currentTimeMillis()
        val appointment = firestore.collection("citas")
            .whereEqualTo("pacienteId", pacienteId)
            .whereEqualTo("estado", EstadoCita.PROGRAMADA.name)
            .get()
            .await()
            .documents
            .mapNotNull { document ->
                val epoch = document.getLong("inicioEpochMillis")
                    ?: return@mapNotNull null

                if (epoch <= now) return@mapNotNull null
                document to epoch
            }
            .minByOrNull { it.second }
            ?.first
            ?: return null

        val psychologistName = readOrDefault("Profesional") {
            val psychologist = firestore.collection("psicologos")
                .document(appointment.getString("psicologoId").orEmpty())
                .get()
                .await()

            listOf(
                psychologist.getString("nombres"),
                psychologist.getString("apellidos")
            )
                .filterNotNull()
                .filter(String::isNotBlank)
                .joinToString(" ")
                .ifBlank { "Profesional" }
        }

        val specialtyName = readOrDefault("Especialidad") {
            firestore.collection("especialidades")
                .document(appointment.getString("especialidadId").orEmpty())
                .get()
                .await()
                .getString("nombre")
                ?: "Especialidad"
        }

        val (siteName, siteAddress) = readOrDefault("Sede" to "") {
            val site = firestore.collection("sedes")
                .document(appointment.getString("sedeId").orEmpty())
                .get()
                .await()

            val address = listOf(
                site.getString("direccion"),
                site.getString("distrito"),
                site.getString("ciudad")
            )
                .filterNotNull()
                .filter(String::isNotBlank)
                .distinct()
                .joinToString(", ")

            (site.getString("nombre") ?: "Sede") to address
        }

        return ProximaCita(
            id = appointment.id,
            psicologoNombre = psychologistName,
            especialidadNombre = specialtyName,
            fecha = appointment.getString("fecha").orEmpty(),
            hora = appointment.getString("hora").orEmpty(),
            sedeNombre = siteName,
            sedeDireccion = siteAddress
        )
    }

    private suspend fun <T> readOrDefault(default: T, read: suspend () -> T): T = try {
        read()
    } catch (exception: CancellationException) {
        throw exception
    } catch (_: Exception) {
        default
    }

    suspend fun obtenerEspecialidades(): List<Especialidad> {
        val snapshot = firestore.collection("especialidades").whereEqualTo("activa", true).get().await()
        return snapshot.documents.mapNotNull { document ->
            document.getString("nombre")?.let { nombre ->
                Especialidad(document.id, nombre, document.getString("descripcion").orEmpty(), true, document.getDouble("costo_cita")?: 0.00)
            }
        }
    }

    suspend fun obtenerPsicologosPorEspecialidad(especialidadId: String): List<Psicologo> {
        val psychologistDocs = firestore.collection("psicologos")
            .whereArrayContains("especialidadIds", especialidadId)
            .whereEqualTo("activo", true)
            .get().await()

        return psychologistDocs.documents.mapNotNull { psychologist ->
            val names = psychologist.getString("nombres") ?: return@mapNotNull null
            val surnames = psychologist.getString("apellidos") ?: return@mapNotNull null
            val specialties = (psychologist.get("especialidadIds") as? List<*>)?.filterIsInstance<String>().orEmpty()
            val sites = (psychologist.get("sedeIds") as? List<*>)?.filterIsInstance<String>().orEmpty()
            Psicologo(
                id = psychologist.id,
                usuarioId = psychologist.getString("usuarioId") ?: psychologist.id,
                nombres = names,
                apellidos = surnames,
                descripcionProfesional = psychologist.getString("descripcionProfesional").orEmpty(),
                especialidadIds = specialties,
                sedeIds = sites,
                activo = true,
                fotoUrl = psychologist.getString("fotoUrl")
            )
        }
    }

    suspend fun obtenerSedes(psicologoId: String): List<Sede> {
        val psychologist = firestore.collection("psicologos").document(psicologoId).get().await()
        if (!psychologist.exists() || psychologist.getBoolean("activo") != true) return emptyList()
        val siteIds = psychologist.get("sedeIds") as? List<*> ?: return emptyList()
        return siteIds.filterIsInstance<String>().mapNotNull { siteId ->
            val site = firestore.collection("sedes").document(siteId).get().await()
            if (!site.exists() || site.getBoolean("activa") != true) return@mapNotNull null
            Sede(
                id = site.id,
                nombre = site.getString("nombre") ?: return@mapNotNull null,
                direccion = site.getString("direccion").orEmpty(),
                distrito = site.getString("distrito").orEmpty(),
                ciudad = site.getString("ciudad").orEmpty(),
                latitud = site.getDouble("latitud") ?: 0.0,
                longitud = site.getDouble("longitud") ?: 0.0,
                telefono = site.getString("telefono").orEmpty(),
                activa = true
            )
        }
    }

    suspend fun obtenerDisponibilidad(psicologoId: String, sedeId: String, fechaText: String): List<SlotDisponible> {
        val date = LocalDate.parse(fechaText, DateTimeFormatter.ISO_LOCAL_DATE)
        val scheduleId = scheduleId(psicologoId, sedeId)
        val scheduleRef = firestore.collection("horariosLaborales").document(scheduleId)
        val scheduleSnapshot = scheduleRef.get().await()
        if (!scheduleSnapshot.exists() || scheduleSnapshot.getBoolean("activo") != true) return emptyList()

        val timeZone = scheduleSnapshot.getString("zonaHoraria")
            ?: error("El horario no tiene una zona horaria configurada.")
        val durationMinutes = scheduleSnapshot.getLong("duracionMinutos")?.toInt()
            ?: error("El horario no tiene una duración configurada.")
        check(timeZone == "America/Lima" && durationMinutes == DURACION_CITA_MINUTOS) {
            "El horario no cumple la configuración vigente de HU02."
        }
        val schedule = HorarioLaboral(
            id = scheduleId,
            psicologoId = psicologoId,
            sedeId = sedeId,
            zonaHoraria = timeZone,
            duracionMinutos = durationMinutes,
            activo = true
        )
        val blocksSnapshot = scheduleRef.collection("bloques")
            .whereEqualTo("diaSemana", date.dayOfWeek.value)
            .whereEqualTo("activo", true)
            .get().await()
        val blocks = blocksSnapshot.documents.mapNotNull { doc ->
            val day = doc.getLong("diaSemana")?.toInt() ?: return@mapNotNull null
            val start = doc.getLong("inicioMinuto")?.toInt() ?: return@mapNotNull null
            val end = doc.getLong("finMinuto")?.toInt() ?: return@mapNotNull null
            BloqueHorario(doc.id, day, start, end, true)
        }

        val exceptionsSnapshot = firestore.collection("excepcionesHorario")
            .whereEqualTo("horarioId", scheduleId)
            .whereEqualTo("fecha", date.toString())
            .get().await()
        val exceptions = exceptionsSnapshot.documents.mapNotNull { doc ->
            val type = runCatching { TipoExcepcionHorario.valueOf(doc.getString("tipo") ?: "") }.getOrNull()
                ?: return@mapNotNull null
            val startMinute = doc.getLong("inicioMinuto")?.toInt() ?: return@mapNotNull null
            ExcepcionHorario(doc.id, date, type, startMinute)
        }

        val occupiedSnapshot = scheduleRef.collection("turnos")
            .whereEqualTo("fecha", date.toString())
            .get().await()
        val occupied = occupiedSnapshot.documents.mapNotNull { it.getLong("inicioEpochMillis") }.toSet()
        return GenerarSlotsDisponibles.generar(schedule, blocks, exceptions, date, occupied)
    }

    suspend fun reservarYConfirmar(reserva: Reserva): Cita {
        val scheduleId = scheduleId(reserva.psicologoId, reserva.sedeId)
        check(reserva.horarioId == scheduleId) { "El horario seleccionado ya no es válido." }
        val startInstant = Instant.ofEpochMilli(reserva.inicioEpochMillis)
        val localStart = startInstant.atZone(ZoneId.of("America/Lima"))
        check(localStart.toLocalDate().toString() == reserva.fecha && localStart.format(DateTimeFormatter.ofPattern("HH:mm")) == reserva.hora) {
            "La fecha y hora no corresponden al turno seleccionado."
        }
        val startAt = Timestamp(Date.from(startInstant))
        val slotId = slotId(reserva.inicioEpochMillis)
        val profileRef = firestore.collection("usuarios").document(reserva.pacienteId)
        val psychologistRef = firestore.collection("psicologos").document(reserva.psicologoId)
        val specialtyRef = firestore.collection("especialidades").document(reserva.especialidadId)
        val siteRef = firestore.collection("sedes").document(reserva.sedeId)
        val scheduleRef = firestore.collection("horariosLaborales").document(scheduleId)
        val blockRef = scheduleRef.collection("bloques").document(reserva.bloqueId)
        val exceptionRef = firestore.collection("excepcionesHorario").document(exceptionId(scheduleId, slotId))
        val lockRef = scheduleRef.collection("turnos").document(slotId)
        val reservationRef = firestore.collection("reservas").document()
        val appointmentRef = firestore.collection("citas").document()


        return firestore.runTransaction { transaction ->
            val profile = transaction.get(profileRef)
            val psychologist = transaction.get(psychologistRef)
            val specialty = transaction.get(specialtyRef)
            val site = transaction.get(siteRef)
            val schedule = transaction.get(scheduleRef)
            val exception = transaction.get(exceptionRef)
            val lock = transaction.get(lockRef)
            val block = if (!exception.exists() || exception.getString("tipo") != TipoExcepcionHorario.DISPONIBILIDAD_EXTRA.name) {
                transaction.get(blockRef)
            } else null

            check(profile.getString("rol") == Rol.PACIENTE.valor && profile.getBoolean("activo") == true) {
                "Solo un paciente activo puede reservar una cita."
            }
            check(psychologist.getBoolean("activo") == true &&
                (psychologist.get("especialidadIds") as? List<*>)?.contains(reserva.especialidadId) == true &&
                (psychologist.get("sedeIds") as? List<*>)?.contains(reserva.sedeId) == true
            ) { "El profesional no atiende en la sede o especialidad seleccionada." }
            check(specialty.getBoolean("activa") == true && site.getBoolean("activa") == true) {
                "La especialidad o sede seleccionada ya no está disponible."
            }
            check(schedule.getBoolean("activo") == true &&
                schedule.getString("psicologoId") == reserva.psicologoId &&
                schedule.getString("sedeId") == reserva.sedeId &&
                schedule.getString("zonaHoraria") == "America/Lima" &&
                schedule.getLong("duracionMinutos")?.toInt() == DURACION_CITA_MINUTOS
            ) { "El horario ya no está disponible." }
            check(!lock.exists()) { "Ese turno acaba de ser reservado por otra persona." }
            if (exception.exists()) {
                check(exception.getLong("inicioEpochMillis") == reserva.inicioEpochMillis) {
                    "La excepción de horario no corresponde al turno seleccionado."
                }
                check(exception.getString("tipo") != TipoExcepcionHorario.BLOQUEADO.name) {
                    "Ese turno fue bloqueado por una excepción de horario."
                }
            } else {
                check(block?.getBoolean("activo") == true) { "El bloque de horario ya no está disponible." }
                val startMinute = localStart.hour * 60 + localStart.minute
                val duration = schedule.getLong("duracionMinutos")?.toInt() ?: 60
                check(block?.getLong("diaSemana")?.toInt() == localStart.dayOfWeek.value &&
                    startMinute >= (block.getLong("inicioMinuto")?.toInt() ?: Int.MAX_VALUE) &&
                    startMinute + duration <= (block.getLong("finMinuto")?.toInt() ?: Int.MIN_VALUE) &&
                    (startMinute - (block.getLong("inicioMinuto")?.toInt() ?: startMinute)) % duration == 0
                ) { "El turno ya no pertenece al horario laboral vigente." }
            }
            val duration = schedule.getLong("duracionMinutos")?.toInt() ?: 60
            val cita = Cita(
                id = appointmentRef.id,
                reservaId = reservationRef.id,
                pacienteId = reserva.pacienteId,
                psicologoId = reserva.psicologoId,
                sedeId = reserva.sedeId,
                especialidadId = reserva.especialidadId,
                fecha = reserva.fecha,
                hora = reserva.hora,
                inicioEpochMillis = reserva.inicioEpochMillis,
                duracionMinutos = duration,
                estado = EstadoCita.PROGRAMADA
            )
            val confirmedReservation = reserva.copy(
                id = reservationRef.id,
                estado = EstadoReserva.CONFIRMADA,
                duracionMinutos = duration
            )
            transaction.set(reservationRef, confirmedReservation.toFirestore(startAt, appointmentRef.id))
            transaction.set(appointmentRef, cita.toFirestore(startAt))

            transaction.set(lockRef, mapOf(
                "reservaId" to reservationRef.id,
                "slotId" to slotId,
                "fecha" to reserva.fecha,
                "hora" to reserva.hora,
                "inicioEpochMillis" to reserva.inicioEpochMillis,
                "inicioAt" to startAt
            ))
            cita
        }.await()
    }

    suspend fun obtenerCitasPsicologoHoy(psicologoId: String, fechaText: String): List<CitaPacienteDetalle> {
        // 1. Buscamos las citas programadas para este psicólogo en la fecha indicada
        val snapshot = firestore.collection("citas")
            .whereEqualTo("psicologoId", psicologoId)
            .whereEqualTo("fecha", fechaText)
            .whereEqualTo("estado", EstadoCita.PROGRAMADA.name)
            .get()
            .await()

        // 2. Mapeamos los resultados y buscamos el nombre de cada paciente
        return snapshot.documents.mapNotNull { doc ->
            val pacienteId = doc.getString("pacienteId") ?: return@mapNotNull null

            // Consultamos la colección usuarios para obtener el nombre
            val pacienteSnapshot = firestore.collection("usuarios").document(pacienteId).get().await()
            val nombres = pacienteSnapshot.getString("nombres") ?: ""
            val apellidos = pacienteSnapshot.getString("apellidos") ?: ""
            val nombreCompleto = "$nombres $apellidos".trim()

            CitaPacienteDetalle(
                citaId = doc.id,
                pacienteId = pacienteId,
                hora = doc.getString("hora").orEmpty(),
                nombrePaciente = nombreCompleto.ifBlank { "Paciente sin registrar" }
            )
        }.sortedBy { it.hora } // Las ordenamos por hora de atención
    }

    private fun Reserva.toFirestore(startAt: Timestamp, citaId: String): Map<String, Any> = mapOf(
        "id" to id,
        "pacienteId" to pacienteId,
        "psicologoId" to psicologoId,
        "sedeId" to sedeId,
        "horarioId" to horarioId,
        "bloqueId" to bloqueId,
        "slotId" to slotId(inicioEpochMillis),
        "especialidadId" to especialidadId,
        "fecha" to fecha,
        "hora" to hora,
        "inicioEpochMillis" to inicioEpochMillis,
        "inicioAt" to startAt,
        "duracionMinutos" to duracionMinutos,
        "estado" to EstadoReserva.CONFIRMADA.name,
        "citaId" to citaId,
        "creadaEn" to FieldValue.serverTimestamp()
    )

    private fun Cita.toFirestore(startAt: Timestamp): Map<String, Any> = mapOf(
        "id" to id,
        "reservaId" to reservaId,
        "pacienteId" to pacienteId,
        "psicologoId" to psicologoId,
        "sedeId" to sedeId,
        "especialidadId" to especialidadId,
        "slotId" to slotId(inicioEpochMillis),
        "fecha" to fecha,
        "hora" to hora,
        "inicioEpochMillis" to inicioEpochMillis,
        "inicioAt" to startAt,
        "duracionMinutos" to duracionMinutos,
        "estado" to estado.name,
        "creadaEn" to FieldValue.serverTimestamp()
    )

    suspend fun obtenerCitaPorId(citaId: String): ProximaCita{

        val now = System.currentTimeMillis()
        val appointment = firestore.collection("citas")
            .document(citaId).get().await()

        val psychologistName = readOrDefault("Profesional") {
            val psychologist = firestore.collection("psicologos")
                .document(appointment.getString("psicologoId").orEmpty())
                .get()
                .await()

            listOf(
                psychologist.getString("nombres"),
                psychologist.getString("apellidos")
            )
                .filterNotNull()
                .filter(String::isNotBlank)
                .joinToString(" ")
                .ifBlank { "Profesional" }
        }

        val specialtyName = readOrDefault("Especialidad") {
            firestore.collection("especialidades")
                .document(appointment.getString("especialidadId").orEmpty())
                .get()
                .await()
                .getString("nombre")
                ?: "Especialidad"
        }

        val (siteName, siteAddress) = readOrDefault("Sede" to "") {
            val site = firestore.collection("sedes")
                .document(appointment.getString("sedeId").orEmpty())
                .get()
                .await()

            val address = listOf(
                site.getString("direccion"),
                site.getString("distrito"),
                site.getString("ciudad")
            )
                .filterNotNull()
                .filter(String::isNotBlank)
                .distinct()
                .joinToString(", ")

            (site.getString("nombre") ?: "Sede") to address
        }

        return ProximaCita(
            id = appointment.id,
            psicologoNombre = psychologistName,
            especialidadNombre = specialtyName,
            fecha = appointment.getString("fecha").orEmpty(),
            hora = appointment.getString("hora").orEmpty(),
            sedeNombre = siteName,
            sedeDireccion = siteAddress
        )
    }


    companion object {
        fun scheduleId(psicologoId: String, sedeId: String): String = "$psicologoId~$sedeId"
        fun slotId(inicioEpochMillis: Long): String = inicioEpochMillis.toString()
        fun exceptionId(scheduleId: String, slotId: String): String = "${scheduleId}~${slotId}"
    }
}
