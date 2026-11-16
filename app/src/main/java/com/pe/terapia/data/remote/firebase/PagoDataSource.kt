package com.pe.terapia.data.remote.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.pe.terapia.domain.model.Comprobante
import com.pe.terapia.domain.model.EstadoPago
import com.pe.terapia.domain.model.MetodoPago
import com.pe.terapia.domain.model.Pago
import com.pe.terapia.domain.model.PRECIO_ESTANDAR_CITA
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.tasks.await

class PagoDataSource(private val firestore: FirebaseFirestore) {

    private fun DocumentSnapshot.getSafeLong(field: String): Long {
        val value = get(field)
        return when (value) {
            is Long -> value
            is Number -> value.toLong()
            is Timestamp -> value.toDate().time
            is String -> value.toLongOrNull() ?: System.currentTimeMillis()
            else -> System.currentTimeMillis()
        }
    }

    private fun DocumentSnapshot.getSafeLongOrNull(field: String): Long? {
        val value = get(field) ?: return null
        return when (value) {
            is Long -> value
            is Number -> value.toLong()
            is Timestamp -> value.toDate().time
            is String -> value.toLongOrNull()
            else -> null
        }
    }

    suspend fun obtenerHistorialPagos(pacienteId: String): List<Pair<Pago, Comprobante?>> {
        require(pacienteId.isNotBlank()) { "Paciente ID inválido" }

        // Lazy/on-demand: asegurar que todas las citas del paciente tengan su pago creado
        val citasSnapshot = firestore.collection("citas")
            .whereEqualTo("pacienteId", pacienteId)
            .get()
            .await()

        val pagosExistentes = firestore.collection("pagos")
            .whereEqualTo("pacienteId", pacienteId)
            .get()
            .await()
        val citasConPago = pagosExistentes.documents.mapNotNull { it.getString("citaId") }.toMutableSet()

        for (citaDoc in citasSnapshot.documents) {
            val citaId = citaDoc.id
            val psicologoId = citaDoc.getString("psicologoId").orEmpty()
            val sigueProgramada = citaDoc.getString("estado") == "PROGRAMADA"
            val yaPagada = citaDoc.getString("estadoPago") == EstadoPago.PAGADO.name || citaDoc.getBoolean("pagado") == true
            if (sigueProgramada && !yaPagada && psicologoId.isNotBlank() && citaId !in citasConPago) {
                val nuevoPagoRef = firestore.collection("pagos").document()
                val pagoMap = mapOf(
                    "id" to nuevoPagoRef.id,
                    "citaId" to citaId,
                    "pacienteId" to pacienteId,
                    "psicologoId" to psicologoId,
                    "monto" to PRECIO_ESTANDAR_CITA,
                    "moneda" to "PEN",
                    "estado" to EstadoPago.PENDIENTE.name,
                    "creadaEn" to FieldValue.serverTimestamp()
                )
                nuevoPagoRef.set(pagoMap).await()
                citasConPago += citaId
            }
        }

        val pagosSnapshot = firestore.collection("pagos")
            .whereEqualTo("pacienteId", pacienteId)
            .get()
            .await()
        val comprobantesSnapshot = firestore.collection("comprobantes")
            .whereEqualTo("pacienteId", pacienteId)
            .get()
            .await()
        val comprobantesPorPago = comprobantesSnapshot.documents.associateBy { it.getString("pagoId").orEmpty() }

        val resultado = mutableListOf<Pair<Pago, Comprobante?>>()
        for (doc in pagosSnapshot.documents) {
            val pago = Pago(
                id = doc.getString("id") ?: doc.id,
                citaId = doc.getString("citaId").orEmpty(),
                pacienteId = doc.getString("pacienteId").orEmpty(),
                psicologoId = doc.getString("psicologoId").orEmpty(),
                monto = doc.getDouble("monto") ?: PRECIO_ESTANDAR_CITA,
                moneda = doc.getString("moneda") ?: "PEN",
                estado = runCatching { EstadoPago.valueOf(doc.getString("estado") ?: "") }.getOrDefault(EstadoPago.PENDIENTE),
                metodoPago = doc.getString("metodoPago")?.let { runCatching { MetodoPago.valueOf(it) }.getOrNull() },
                referenciaTransaccion = doc.getString("referenciaTransaccion"),
                creadaEn = doc.getSafeLong("creadaEn"),
                actualizadaEn = doc.getSafeLongOrNull("actualizadaEn")
            )

            // La consulta se acota por paciente para cumplir las reglas de
            // Firestore; el vínculo por pago se resuelve localmente.
            val comprobante = comprobantesPorPago[pago.id]?.let { cDoc ->
                Comprobante(
                    id = cDoc.getString("id") ?: cDoc.id,
                    pagoId = cDoc.getString("pagoId").orEmpty(),
                    citaId = cDoc.getString("citaId").orEmpty(),
                    pacienteId = cDoc.getString("pacienteId").orEmpty(),
                    monto = cDoc.getDouble("monto") ?: PRECIO_ESTANDAR_CITA,
                    moneda = cDoc.getString("moneda") ?: "PEN",
                    fechaEmision = cDoc.getString("fechaEmision").orEmpty(),
                    tipo = cDoc.getString("tipo") ?: "BOLETA",
                    numeroComprobante = cDoc.getString("numeroComprobante").orEmpty(),
                    detalles = cDoc.getString("detalles").orEmpty()
                )
            }

            resultado.add(pago to comprobante)
        }
        return resultado.sortedByDescending { it.first.creadaEn }
    }

    suspend fun obtenerPagosPendientesPsicologo(psicologoId: String): List<Pago> {
        require(psicologoId.isNotBlank()) { "Psicólogo ID inválido" }
        val snapshot = firestore.collection("pagos")
            .whereEqualTo("psicologoId", psicologoId)
            .whereEqualTo("estado", EstadoPago.PENDIENTE.name)
            .get()
            .await()

        return snapshot.documents.map { doc ->
            Pago(
                id = doc.getString("id") ?: doc.id,
                citaId = doc.getString("citaId").orEmpty(),
                pacienteId = doc.getString("pacienteId").orEmpty(),
                psicologoId = doc.getString("psicologoId").orEmpty(),
                monto = doc.getDouble("monto") ?: PRECIO_ESTANDAR_CITA,
                moneda = doc.getString("moneda") ?: "PEN",
                estado = EstadoPago.PENDIENTE,
                creadaEn = doc.getSafeLong("creadaEn")
            )
        }
    }

    suspend fun confirmarPagoClinica(pagoId: String): Comprobante {
        val pagoRef = firestore.collection("pagos").document(pagoId)
        val comprobanteRef = firestore.collection("comprobantes").document()

        return firestore.runTransaction { transaction ->
            val pagoSnapshot = transaction.get(pagoRef)
            check(pagoSnapshot.exists()) { "El pago no existe." }
            val estadoActual = pagoSnapshot.getString("estado")
            check(estadoActual == EstadoPago.PENDIENTE.name) { "El pago ya fue procesado o no está pendiente." }

            val citaId = pagoSnapshot.getString("citaId").orEmpty()
            val pacienteId = pagoSnapshot.getString("pacienteId").orEmpty()
            val monto = pagoSnapshot.getDouble("monto") ?: PRECIO_ESTANDAR_CITA
            val ahora = System.currentTimeMillis()
            val fechaEmision = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val numeroComprobante = "B001-" + (100000..999999).random()
            val citaRef = citaId.takeIf(String::isNotBlank)?.let { firestore.collection("citas").document(it) }
            val citaSnapshot = citaRef?.let(transaction::get)
            if (citaRef != null) check(citaSnapshot?.exists() == true && citaSnapshot.getString("estado") == "PROGRAMADA") {
                "La cita ya no está activa."
            }

            // Actualizar pago
            transaction.update(pagoRef, mapOf(
                "estado" to EstadoPago.PAGADO.name,
                "metodoPago" to MetodoPago.EFECTIVO_CLINICA.name,
                "actualizadaEn" to ahora
            ))

            // Actualizar cita (TASK-48: Integración de estados de pago con la cita)
            if (citaRef != null) {
                transaction.update(citaRef, mapOf(
                    "estadoPago" to EstadoPago.PAGADO.name,
                    "pagado" to true
                ))
            }

            // Crear comprobante (TASK-46)
            val comprobante = Comprobante(
                id = comprobanteRef.id,
                pagoId = pagoId,
                citaId = citaId,
                pacienteId = pacienteId,
                monto = monto,
                moneda = "PEN",
                fechaEmision = fechaEmision,
                tipo = "BOLETA",
                numeroComprobante = numeroComprobante,
                detalles = "Pago en clínica (Efectivo) - Consulta psicológica TerapIA"
            )

            transaction.set(comprobanteRef, mapOf(
                "id" to comprobante.id,
                "pagoId" to comprobante.pagoId,
                "citaId" to comprobante.citaId,
                "pacienteId" to comprobante.pacienteId,
                "monto" to comprobante.monto,
                "moneda" to comprobante.moneda,
                "fechaEmision" to comprobante.fechaEmision,
                "tipo" to comprobante.tipo,
                "numeroComprobante" to comprobante.numeroComprobante,
                "detalles" to comprobante.detalles,
                "creadaEn" to FieldValue.serverTimestamp()
            ))

            val notificationRef = firestore.collection("notificaciones").document("pago_confirmado_$pagoId")
            transaction.set(notificationRef, mapOf(
                "id" to notificationRef.id,
                "usuarioId" to pacienteId,
                "destinatarioRol" to "paciente",
                "titulo" to "Pago confirmado",
                "mensaje" to "Registramos el pago de tu cita. Tu comprobante está disponible en Mis pagos.",
                "tipo" to "PAGO_CONFIRMADO",
                "citaId" to citaId,
                "pagoId" to pagoId,
                "fechaCreacion" to FieldValue.serverTimestamp(),
                "leida" to false
            ))
            val adminNotificationRef = firestore.collection("notificaciones").document("admin_pago_confirmado_$pagoId")
            transaction.set(adminNotificationRef, mapOf(
                "id" to adminNotificationRef.id,
                "destinatarioRol" to "admin",
                "titulo" to "Pago confirmado",
                "mensaje" to "Se confirmó el pago de una cita por ${monto} ${pagoSnapshot.getString("moneda") ?: "PEN"}.",
                "tipo" to "PAGO_CONFIRMADO_ADMIN",
                "citaId" to citaId,
                "pagoId" to pagoId,
                "fechaCreacion" to FieldValue.serverTimestamp(),
                "leida" to false
            ))

            comprobante
        }.await()
    }

    suspend fun procesarPagoOnline(pagoId: String, referenciaTransaccion: String): Comprobante {
        val pagoRef = firestore.collection("pagos").document(pagoId)
        val comprobanteRef = firestore.collection("comprobantes").document()

        return firestore.runTransaction { transaction ->
            val pagoSnapshot = transaction.get(pagoRef)
            check(pagoSnapshot.exists()) { "El pago no existe." }
            val estadoActual = pagoSnapshot.getString("estado")
            check(estadoActual == EstadoPago.PENDIENTE.name) { "El pago ya fue procesado o no está pendiente." }

            val citaId = pagoSnapshot.getString("citaId").orEmpty()
            val pacienteId = pagoSnapshot.getString("pacienteId").orEmpty()
            val monto = pagoSnapshot.getDouble("monto") ?: PRECIO_ESTANDAR_CITA
            val ahora = System.currentTimeMillis()
            val fechaEmision = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val numeroComprobante = "B001-" + (100000..999999).random()
            val citaRef = citaId.takeIf(String::isNotBlank)?.let { firestore.collection("citas").document(it) }
            val citaSnapshot = citaRef?.let(transaction::get)
            if (citaRef != null) check(citaSnapshot?.exists() == true && citaSnapshot.getString("estado") == "PROGRAMADA") {
                "La cita ya no está activa."
            }

            // Actualizar pago
            transaction.update(pagoRef, mapOf(
                "estado" to EstadoPago.PAGADO.name,
                "metodoPago" to MetodoPago.ONLINE_MERCADOPAGO.name,
                "referenciaTransaccion" to referenciaTransaccion,
                "actualizadaEn" to ahora
            ))

            // Actualizar cita
            if (citaRef != null) {
                transaction.update(citaRef, mapOf(
                    "estadoPago" to EstadoPago.PAGADO.name,
                    "pagado" to true
                ))
            }

            // Crear comprobante
            val comprobante = Comprobante(
                id = comprobanteRef.id,
                pagoId = pagoId,
                citaId = citaId,
                pacienteId = pacienteId,
                monto = monto,
                moneda = "PEN",
                fechaEmision = fechaEmision,
                tipo = "BOLETA",
                numeroComprobante = numeroComprobante,
                detalles = "Pago online (Mercado Pago Ficticio) - Transacción: $referenciaTransaccion"
            )

            transaction.set(comprobanteRef, mapOf(
                "id" to comprobante.id,
                "pagoId" to comprobante.pagoId,
                "citaId" to comprobante.citaId,
                "pacienteId" to comprobante.pacienteId,
                "monto" to comprobante.monto,
                "moneda" to comprobante.moneda,
                "fechaEmision" to comprobante.fechaEmision,
                "tipo" to comprobante.tipo,
                "numeroComprobante" to comprobante.numeroComprobante,
                "detalles" to comprobante.detalles,
                "creadaEn" to FieldValue.serverTimestamp()
            ))

            val notificationRef = firestore.collection("notificaciones").document("pago_confirmado_$pagoId")
            transaction.set(notificationRef, mapOf(
                "id" to notificationRef.id,
                "usuarioId" to pacienteId,
                "destinatarioRol" to "paciente",
                "titulo" to "Pago confirmado",
                "mensaje" to "Registramos el pago de tu cita. Tu comprobante está disponible en Mis pagos.",
                "tipo" to "PAGO_CONFIRMADO",
                "citaId" to citaId,
                "pagoId" to pagoId,
                "fechaCreacion" to FieldValue.serverTimestamp(),
                "leida" to false
            ))
            val adminNotificationRef = firestore.collection("notificaciones").document("admin_pago_confirmado_$pagoId")
            transaction.set(adminNotificationRef, mapOf(
                "id" to adminNotificationRef.id,
                "destinatarioRol" to "admin",
                "titulo" to "Pago confirmado",
                "mensaje" to "Se confirmó el pago de una cita por ${monto} ${pagoSnapshot.getString("moneda") ?: "PEN"}.",
                "tipo" to "PAGO_CONFIRMADO_ADMIN",
                "citaId" to citaId,
                "pagoId" to pagoId,
                "fechaCreacion" to FieldValue.serverTimestamp(),
                "leida" to false
            ))

            comprobante
        }.await()
    }

    suspend fun obtenerComprobantePorCita(citaId: String): Comprobante? {
        require(citaId.isNotBlank()) { "Cita inválida para buscar el comprobante." }
        val cita = firestore.collection("citas").document(citaId).get().await()
        if (!cita.exists()) return null
        val pacienteId = cita.getString("pacienteId").orEmpty()
        check(pacienteId.isNotBlank()) { "La cita no tiene un paciente asociado." }
        val snapshot = firestore.collection("comprobantes")
            .whereEqualTo("pacienteId", pacienteId)
            .get()
            .await()

        val doc = snapshot.documents.firstOrNull { it.getString("citaId") == citaId } ?: return null
        return Comprobante(
            id = doc.getString("id") ?: doc.id,
            pagoId = doc.getString("pagoId").orEmpty(),
            citaId = doc.getString("citaId").orEmpty(),
            pacienteId = doc.getString("pacienteId").orEmpty(),
            monto = doc.getDouble("monto") ?: PRECIO_ESTANDAR_CITA,
            moneda = doc.getString("moneda") ?: "PEN",
            fechaEmision = doc.getString("fechaEmision").orEmpty(),
            tipo = doc.getString("tipo") ?: "BOLETA",
            numeroComprobante = doc.getString("numeroComprobante").orEmpty(),
            detalles = doc.getString("detalles").orEmpty()
        )
    }

    suspend fun obtenerPagoPorCita(citaId: String): Pago? {
        require(citaId.isNotBlank()) { "Cita inválida para buscar el pago." }

        // Las reglas de Firestore permiten listar pagos únicamente cuando la
        // consulta queda acotada por pacienteId (o psicologoId). Consultar solo
        // por citaId provoca PERMISSION_DENIED aunque la cita sea del paciente.
        val citaDoc = firestore.collection("citas").document(citaId).get().await()
        if (!citaDoc.exists()) return null
        val pacienteId = citaDoc.getString("pacienteId").orEmpty()
        val psicologoId = citaDoc.getString("psicologoId").orEmpty()
        check(pacienteId.isNotBlank() && psicologoId.isNotBlank()) {
            "La cita no tiene paciente o profesional asociado."
        }

        val pagosPaciente = firestore.collection("pagos")
            .whereEqualTo("pacienteId", pacienteId)
            .get()
            .await()

        val doc = pagosPaciente.documents.firstOrNull { it.getString("citaId") == citaId }
        if (doc != null) {
            return Pago(
                id = doc.getString("id") ?: doc.id,
                citaId = doc.getString("citaId").orEmpty(),
                pacienteId = doc.getString("pacienteId").orEmpty(),
                psicologoId = doc.getString("psicologoId").orEmpty(),
                monto = doc.getDouble("monto") ?: PRECIO_ESTANDAR_CITA,
                moneda = doc.getString("moneda") ?: "PEN",
                estado = runCatching { EstadoPago.valueOf(doc.getString("estado") ?: "") }.getOrDefault(EstadoPago.PENDIENTE),
                metodoPago = doc.getString("metodoPago")?.let { runCatching { MetodoPago.valueOf(it) }.getOrNull() },
                referenciaTransaccion = doc.getString("referenciaTransaccion"),
                creadaEn = doc.getSafeLong("creadaEn"),
                actualizadaEn = doc.getSafeLongOrNull("actualizadaEn")
            )
        }

        val nuevoPagoRef = firestore.collection("pagos").document()
        val pagoMap = mapOf(
            "id" to nuevoPagoRef.id,
            "citaId" to citaId,
            "pacienteId" to pacienteId,
            "psicologoId" to psicologoId,
            "monto" to PRECIO_ESTANDAR_CITA,
            "moneda" to "PEN",
            "estado" to EstadoPago.PENDIENTE.name,
            "creadaEn" to FieldValue.serverTimestamp()
        )
        // No se devuelve un ID local si Firestore rechazó la creación: eso
        // dejaba avanzar a la pantalla de pago con un documento inexistente.
        nuevoPagoRef.set(pagoMap).await()

        return Pago(
            id = nuevoPagoRef.id,
            citaId = citaId,
            pacienteId = pacienteId,
            psicologoId = psicologoId,
            monto = PRECIO_ESTANDAR_CITA,
            moneda = "PEN",
            estado = EstadoPago.PENDIENTE,
            creadaEn = System.currentTimeMillis()
        )
    }

    suspend fun seleccionarPagoEnClinica(pagoId: String) {
        firestore.collection("pagos").document(pagoId).update(
            mapOf(
                "metodoPago" to MetodoPago.EFECTIVO_CLINICA.name,
                "actualizadaEn" to System.currentTimeMillis()
            )
        ).await()
    }
}
