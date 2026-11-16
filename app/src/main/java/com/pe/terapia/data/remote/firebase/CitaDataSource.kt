package com.pe.terapia.data.remote.firebase

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.pe.terapia.domain.model.BloqueHorario
import com.pe.terapia.domain.model.Cita
import com.pe.terapia.domain.model.CitaQrInfo
import com.pe.terapia.domain.model.CitasPsicologoResumen
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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

private data class SedeInfo(val nombre: String, val direccion: String, val latitud: Double, val longitud: Double)

private class CacheRelaciones {
    val psicologos = ConcurrentHashMap<String, Pair<String, String>>()
    val especialidades = ConcurrentHashMap<String, String>()
    val sedes = ConcurrentHashMap<String, SedeInfo>()
}

class CitaDataSource(private val firestore: FirebaseFirestore) {

    suspend fun tieneSolicitudGestionPendiente(citaId: String, pacienteId: String): Boolean {
        val request = firestore.collection("solicitudesGestionCitas").document(citaId).get().await()
        return request.exists() && request.getString("pacienteId") == pacienteId &&
            request.getString("estado") == "PENDIENTE"
    }

    suspend fun obtenerSolicitudGestionPendiente(citaId: String, pacienteId: String): DocumentSnapshot? {
        val query = firestore.collection("solicitudesGestionCitas")
            .whereEqualTo("citaId", citaId)
            .whereEqualTo("pacienteId", pacienteId)
            .whereEqualTo("estado", "PENDIENTE")
            .limit(1)
            .get()
            .await()
        return query.documents.firstOrNull()
    }

    suspend fun solicitarGestionCita(citaId: String, pacienteId: String, tipo: String, slotSolicitado: SlotDisponible?) {
        require(citaId.isNotBlank() && pacienteId.isNotBlank()) { "Faltan datos de la cita." }
        require(tipo in setOf("CANCELACION", "REPROGRAMACION")) { "Tipo de solicitud no válido." }
        val cita = firestore.collection("citas").document(citaId).get().await()
        check(cita.exists() && cita.getString("pacienteId") == pacienteId) { "No puedes gestionar esta cita." }
        check(cita.getString("estado") == EstadoCita.PROGRAMADA.name) { "Solo se pueden gestionar citas programadas." }
        check((cita.getLong("inicioEpochMillis") ?: 0L) > System.currentTimeMillis()) { "La fecha de la cita ya pasó." }
        val psicologoId = cita.getString("psicologoId").orEmpty()
        val sedeId = cita.getString("sedeId").orEmpty()
        if (tipo == "REPROGRAMACION") {
            val propuesta = requireNotNull(slotSolicitado) { "Selecciona una nueva fecha y hora." }
            check(propuesta.psicologoId == psicologoId && propuesta.sedeId == sedeId) {
                "La nueva propuesta debe ser con el mismo profesional y en la misma sede."
            }
            check(propuesta.inicioEpochMillis > System.currentTimeMillis()) { "El horario propuesto ya pasó." }
        } else {
            require(slotSolicitado == null) { "La solicitud de cancelación no debe incluir un nuevo horario." }
        }
        val legacyPending = firestore.collection("solicitudesGestionCitas")
            .whereEqualTo("citaId", citaId)
            .whereEqualTo("pacienteId", pacienteId)
            .whereEqualTo("estado", "PENDIENTE")
            .get().await().documents
        check(legacyPending.isEmpty()) {
            "Ya tienes una solicitud pendiente. El administrador debe rechazar o resolver esa solicitud antes de crear otra."
        }
        // Un documento estable por cita impide que reentrar a la pantalla cree
        // una cola ilimitada de solicitudes iguales.
        val requestRef = firestore.collection("solicitudesGestionCitas").document(citaId)
        val adminNotificationRef = firestore.collection("notificaciones")
            .document("admin_solicitud_${citaId}_${System.currentTimeMillis()}")
        firestore.runTransaction { transaction ->
            val existing = transaction.get(requestRef)
            check(!existing.exists() || existing.getString("estado") !in setOf("PENDIENTE")) {
                "Ya tienes una solicitud pendiente para esta cita."
            }
            val data = mutableMapOf<String, Any>(
                "id" to citaId,
                "citaId" to citaId,
                "pacienteId" to pacienteId,
                "psicologoId" to psicologoId,
                "tipo" to tipo,
                "estado" to "PENDIENTE",
                "creadaEn" to FieldValue.serverTimestamp()
            )
            if (slotSolicitado != null) {
                data["fechaSolicitada"] = slotSolicitado.fecha
                data["horaSolicitada"] = slotSolicitado.hora
                data["inicioEpochMillisSolicitado"] = slotSolicitado.inicioEpochMillis
                data["horarioIdSolicitado"] = slotSolicitado.horarioId
                data["bloqueIdSolicitado"] = slotSolicitado.bloqueId
                data["duracionMinutosSolicitada"] = slotSolicitado.duracionMinutos
            }
            if (existing.exists()) {
                transaction.update(requestRef, data + mapOf(
                    "resueltaEn" to FieldValue.delete(),
                    "resueltaPor" to FieldValue.delete(),
                    "notaAdmin" to FieldValue.delete(),
                    "pagoPendienteRevision" to FieldValue.delete(),
                    "fechaSolicitada" to (slotSolicitado?.fecha ?: FieldValue.delete()),
                    "horaSolicitada" to (slotSolicitado?.hora ?: FieldValue.delete()),
                    "inicioEpochMillisSolicitado" to (slotSolicitado?.inicioEpochMillis ?: FieldValue.delete()),
                    "horarioIdSolicitado" to (slotSolicitado?.horarioId ?: FieldValue.delete()),
                    "bloqueIdSolicitado" to (slotSolicitado?.bloqueId ?: FieldValue.delete()),
                    "duracionMinutosSolicitada" to (slotSolicitado?.duracionMinutos ?: FieldValue.delete())
                ))
            } else {
                transaction.set(requestRef, data)
            }
            Unit
        }.await()

        // La solicitud es la operación de negocio principal. Un error al guardar
        // su aviso no debe hacer que el paciente la vuelva a enviar ni bloquearla.
        try {
            adminNotificationRef.set(mapOf(
                "id" to adminNotificationRef.id,
                "destinatarioRol" to Rol.ADMIN.valor,
                "titulo" to if (tipo == "REPROGRAMACION") "Solicitud de reprogramación" else "Solicitud de cancelación",
                "mensaje" to if (slotSolicitado != null) {
                    "Un paciente propuso ${slotSolicitado.fecha} a las ${slotSolicitado.hora}. Revisa la solicitud."
                } else "Un paciente solicitó cancelar una cita. Revisa la solicitud.",
                "tipo" to "SOLICITUD_GESTION",
                "solicitudId" to citaId,
                "citaId" to citaId,
                "fechaCreacion" to FieldValue.serverTimestamp(),
                "leida" to false
            )).await()
        } catch (error: Exception) {
            Log.w("CitaDataSource", "La solicitud quedó guardada, pero no se pudo crear su aviso al administrador.", error)
        }
    }

    suspend fun cancelarCitaPaciente(citaId: String, pacienteId: String) {
        require(citaId.isNotBlank() && pacienteId.isNotBlank())
        val citaRef = firestore.collection("citas").document(citaId)
        firestore.runTransaction { transaction ->
            val cita = transaction.get(citaRef)
            check(cita.exists() && cita.getString("pacienteId") == pacienteId) { "No puedes cancelar esta cita." }
            check(cita.getString("estado") == EstadoCita.PROGRAMADA.name) { "La cita ya no está programada." }
            check(cita.getString("estadoPago") != "PAGADO" && cita.getBoolean("pagado") != true) {
                "La cita pagada requiere revisión de la clínica."
            }
            val inicio = cita.getTimestamp("inicioAt")?.toDate()?.time ?: cita.getLong("inicioEpochMillis") ?: 0L
            check(inicio - System.currentTimeMillis() > CANCELACION_CUTOFF_MILLIS) {
                "Para cancelar con menos de 24 horas, contacta a la clínica."
            }
            val reservaId = cita.getString("reservaId").orEmpty()
            check(reservaId.isNotBlank()) { "La cita no tiene una reserva asociada." }
            val reservaRef = firestore.collection("reservas").document(reservaId)
            val reserva = transaction.get(reservaRef)
            check(reserva.exists() && reserva.getString("pacienteId") == pacienteId && reserva.getString("estado") == "CONFIRMADA") {
                "No se encontró una reserva activa asociada a esta cita."
            }
            val horarioId = reserva.getString("horarioId").orEmpty()
            val slotId = reserva.getString("slotId").orEmpty()
            check(horarioId.isNotBlank() && slotId.isNotBlank()) { "La reserva no contiene la información del turno." }
            val turnoRef = firestore.collection("horariosLaborales").document(horarioId).collection("turnos").document(slotId)
            val turno = transaction.get(turnoRef)
            check(turno.exists() && turno.getString("reservaId") == reservaId) { "El turno ya no corresponde a esta reserva." }

            transaction.update(citaRef, mapOf("estado" to EstadoCita.CANCELADA.name, "canceladaEn" to FieldValue.serverTimestamp()))
            transaction.update(reservaRef, "estado", "CANCELADA")
            transaction.delete(turnoRef)
            Unit
        }.await()
    }

    suspend fun validarCitaQr(citaId: String, psicologoId: String): CitaQrInfo {
        require(citaId.isNotBlank() && psicologoId.isNotBlank()) { "Faltan datos para validar el QR." }
        val cita = firestore.collection("citas").document(citaId).get().await()
        check(cita.exists()) { "No se encontró una cita asociada a este QR." }
        check(cita.getString("psicologoId") == psicologoId) {
            "Esta cita no está asignada a tu cuenta de psicólogo."
        }

        val pacienteId = cita.getString("pacienteId").orEmpty()
        check(pacienteId.isNotBlank()) { "La cita no tiene un paciente asociado." }
        // El nombre visible se guarda en la cita para que el psicólogo no necesite
        // leer el perfil privado del paciente desde /usuarios.
        val pacienteNombre = cita.getString("pacienteNombre").orEmpty().ifBlank { "Paciente" }
        val inicioMillis = cita.getTimestamp("inicioAt")?.toDate()?.time
            ?: cita.getLong("inicioEpochMillis")
            ?: 0L

        return CitaQrInfo(
            citaId = cita.id,
            pacienteId = pacienteId,
            pacienteNombre = pacienteNombre,
            fecha = cita.getString("fecha").orEmpty(),
            hora = cita.getString("hora").orEmpty(),
            especialidadId = cita.getString("especialidadId").orEmpty(),
            estado = cita.getString("estado") ?: EstadoCita.PROGRAMADA.name,
            inicioEpochMillis = inicioMillis
        )
    }

    suspend fun obtenerProximaCita(pacienteId: String): ProximaCita? {
        val ahora = System.currentTimeMillis()
        return obtenerCitasPaciente(pacienteId).firstOrNull {
            it.estado == EstadoCita.PROGRAMADA.name && it.inicioEpochMillis > ahora
        }
    }

    suspend fun obtenerCitasPaciente(pacienteId: String): List<ProximaCita> = coroutineScope {
        require(pacienteId.isNotBlank()) { "No se pudo identificar al paciente." }

        val cache = CacheRelaciones()

        val documentosCitas = firestore.collection("citas")
            .whereEqualTo("pacienteId", pacienteId)
            .get().await()
            .documents

        documentosCitas.map { doc ->
            async { doc.aProximaCita(cache) }
        }.awaitAll().sortedBy { it.inicioEpochMillis }
    }

    private suspend fun DocumentSnapshot.aProximaCita(cache: CacheRelaciones): ProximaCita {
        val psicologoId = getString("psicologoId").orEmpty()
        val especialidadId = getString("especialidadId").orEmpty()
        val sedeId = getString("sedeId").orEmpty()

        val (nombrePsicologo, descripcion) = cache.psicologos.getOrPut(psicologoId) {
            readOrDefault("Profesional" to "") {
                val doc = firestore.collection("psicologos").document(psicologoId).get().await()
                val nombre = listOf(doc.getString("nombres"), doc.getString("apellidos"))
                    .filterNotNull().filter(String::isNotBlank).joinToString(" ").ifBlank { "Profesional" }
                nombre to doc.getString("descripcionProfesional").orEmpty()
            }
        }
        val nombreEspecialidad = cache.especialidades.getOrPut(especialidadId) {
            readOrDefault("Especialidad") {
                firestore.collection("especialidades").document(especialidadId).get().await().getString("nombre") ?: "Especialidad"
            }
        }
        val sede = cache.sedes.getOrPut(sedeId) {
            readOrDefault(SedeInfo("Sede", "", 0.0, 0.0)) {
                val doc = firestore.collection("sedes").document(sedeId).get().await()
                val direccion = listOf(doc.getString("direccion"), doc.getString("distrito"), doc.getString("ciudad"))
                    .filterNotNull().filter(String::isNotBlank).distinct().joinToString(", ")
                SedeInfo(doc.getString("nombre") ?: "Sede", direccion, doc.getDouble("latitud") ?: 0.0, doc.getDouble("longitud") ?: 0.0)
            }
        }
        return ProximaCita(

            psicologoNombre = nombrePsicologo,
            especialidadNombre = nombreEspecialidad,
            fecha = getString("fecha").orEmpty(),
            hora = getString("hora").orEmpty(),
            sedeNombre = sede.nombre,
            sedeDireccion = sede.direccion,
            estadoPago = getString("estadoPago") ?: "PENDIENTE",
            id = id,
            estado = getString("estado").orEmpty(),
            inicioEpochMillis = getLong("inicioEpochMillis") ?: 0L,
            psicologoDescripcion = descripcion,
            sedeLatitud = sede.latitud,
            sedeLongitud = sede.longitud,
            pagado = getBoolean("pagado") == true || getString("estadoPago") == "PAGADO",
            psicologoId = psicologoId,
            sedeId = sedeId
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
                // Corrección del error de tipeo en esta línea:
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
                pacienteNombre = listOfNotNull(profile.getString("nombres"), profile.getString("apellidos"))
                    .filter(String::isNotBlank).joinToString(" "),
                estado = EstadoCita.PROGRAMADA
            )
            val confirmedReservation = reserva.copy(
                id = reservationRef.id,
                estado = EstadoReserva.CONFIRMADA,
                duracionMinutos = duration
            )
            transaction.set(reservationRef, confirmedReservation.toFirestore(startAt, appointmentRef.id))
            transaction.set(appointmentRef, cita.toFirestore(startAt))

            val psychologistNotificationRef = firestore.collection("notificaciones")
                .document("cita_reservada_${cita.id}")
            transaction.set(psychologistNotificationRef, mapOf(
                "id" to psychologistNotificationRef.id,
                "usuarioId" to reserva.psicologoId,
                "destinatarioRol" to Rol.PSICOLOGO.valor,
                "titulo" to "Nueva cita reservada",
                "mensaje" to "Se reservó una cita para el ${cita.fecha} a las ${cita.hora}.",
                "tipo" to "CITA_RESERVADA_PSICOLOGO",
                "citaId" to cita.id,
                "fechaCreacion" to FieldValue.serverTimestamp(),
                "leida" to false
            ))

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
        val snapshot = firestore.collection("citas")
            .whereEqualTo("psicologoId", psicologoId)
            .whereEqualTo("fecha", fechaText)
            .whereEqualTo("estado", EstadoCita.PROGRAMADA.name)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val pacienteId = doc.getString("pacienteId") ?: return@mapNotNull null
            CitaPacienteDetalle(
                citaId = doc.id,
                pacienteId = pacienteId,
                hora = doc.getString("hora").orEmpty(),
                nombrePaciente = doc.getString("pacienteNombre").orEmpty().ifBlank { "Paciente sin registrar" },
                fecha = doc.getString("fecha").orEmpty(),
                inicioEpochMillis = doc.getLong("inicioEpochMillis") ?: 0L
            )
        }.sortedBy { it.hora }
    }

    suspend fun obtenerCitasPsicologoResumen(psicologoId: String, ahoraEpochMillis: Long): CitasPsicologoResumen {
        require(psicologoId.isNotBlank()) { "No se pudo identificar al psicólogo." }
        val documentos = firestore.collection("citas")
            .whereEqualTo("psicologoId", psicologoId)
            .get().await().documents
        val documentosPorId = documentos.associateBy { it.id }
        val programadas = documentos.mapNotNull { doc ->
            if (doc.getString("estado") != EstadoCita.PROGRAMADA.name) return@mapNotNull null
            val inicio = doc.getLong("inicioEpochMillis") ?: return@mapNotNull null
            val pacienteId = doc.getString("pacienteId") ?: return@mapNotNull null
            CitaPacienteDetalle(
                citaId = doc.id,
                pacienteId = pacienteId,
                hora = doc.getString("hora").orEmpty(),
                nombrePaciente = doc.getString("pacienteNombre").orEmpty().ifBlank { "Paciente sin registrar" },
                fecha = doc.getString("fecha").orEmpty(),
                inicioEpochMillis = inicio
            )
        }
        val pendientes = programadas.filter { cita ->
            val duration = documentosPorId[cita.citaId]
                ?.getLong("duracionMinutos")?.toInt() ?: DURACION_CITA_MINUTOS
            cita.inicioEpochMillis + duration * 60_000L <= ahoraEpochMillis
        }.sortedBy { it.inicioEpochMillis }
        val futuras = programadas.filter { it.inicioEpochMillis > ahoraEpochMillis }
            .sortedBy { it.inicioEpochMillis }
        val citasSinRecordatorio = documentos.mapNotNull { doc ->
            val estado = doc.getString("estado")
            val inicio = doc.getLong("inicioEpochMillis") ?: 0L
            if (estado != EstadoCita.PROGRAMADA.name || inicio <= ahoraEpochMillis) doc.id else null
        }
        return CitasPsicologoResumen(pendientes, futuras, citasSinRecordatorio)
    }

    suspend fun obtenerCitasPendientesResultado(psicologoId: String, ahoraEpochMillis: Long): List<CitaPacienteDetalle> = coroutineScope {
        require(psicologoId.isNotBlank()) { "No se pudo identificar al psicólogo." }
        val citas = firestore.collection("citas")
            .whereEqualTo("psicologoId", psicologoId)
            .get().await().documents
            .filter { doc ->
                if (doc.getString("estado") != EstadoCita.PROGRAMADA.name) return@filter false
                val inicio = doc.getLong("inicioEpochMillis") ?: return@filter false
                val duracion = (doc.getLong("duracionMinutos") ?: DURACION_CITA_MINUTOS).toInt()
                inicio + duracion * 60_000L <= ahoraEpochMillis
            }

        citas.map { doc ->
            async {
                val pacienteId = doc.getString("pacienteId").orEmpty()
                CitaPacienteDetalle(
                    citaId = doc.id,
                    pacienteId = pacienteId,
                    hora = doc.getString("hora").orEmpty(),
                    nombrePaciente = doc.getString("pacienteNombre").orEmpty().ifBlank { "Paciente sin registrar" },
                    fecha = doc.getString("fecha").orEmpty(),
                    inicioEpochMillis = doc.getLong("inicioEpochMillis") ?: 0L
                )
            }
        }.awaitAll().sortedBy { it.inicioEpochMillis }
    }

    suspend fun marcarCitaNoAsistio(citaId: String, psicologoId: String, ahoraEpochMillis: Long) {
        require(citaId.isNotBlank() && psicologoId.isNotBlank()) { "Faltan datos de la cita." }
        val citaRef = firestore.collection("citas").document(citaId)
        firestore.runTransaction { transaction ->
            val cita = transaction.get(citaRef)
            check(cita.exists()) { "La cita ya no existe." }
            check(cita.getString("psicologoId") == psicologoId) { "No puedes actualizar una cita de otro profesional." }
            check(cita.getString("estado") == EstadoCita.PROGRAMADA.name) { "Esta cita ya tiene un resultado registrado." }
            val inicio = cita.getLong("inicioEpochMillis") ?: error("La cita no tiene una hora de inicio válida.")
            val duracion = (cita.getLong("duracionMinutos") ?: DURACION_CITA_MINUTOS).toInt()
            check(ahoraEpochMillis >= inicio + duracion * 60_000L) { "Solo puedes marcar la inasistencia después de que termine el turno." }
            transaction.update(citaRef, "estado", EstadoCita.NO_ASISTIO.name)
        }.await()
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
        "pacienteNombre" to pacienteNombre,
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
        private const val CANCELACION_CUTOFF_MILLIS = 24L * 60 * 60 * 1000
        fun scheduleId(psicologoId: String, sedeId: String): String = "$psicologoId~$sedeId"
        fun slotId(inicioEpochMillis: Long): String = inicioEpochMillis.toString()
        fun exceptionId(scheduleId: String, slotId: String): String = "${scheduleId}~${slotId}"
    }
}
