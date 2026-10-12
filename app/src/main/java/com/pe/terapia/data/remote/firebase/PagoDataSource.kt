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

        for (citaDoc in citasSnapshot.documents) {
            val citaId = citaDoc.id
            val psicologoId = citaDoc.getString("psicologoId").orEmpty()
            val pagoExistente = firestore.collection("pagos")
                .whereEqualTo("citaId", citaId)
                .get()
                .await()
            if (pagoExistente.isEmpty) {
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
                runCatching { nuevoPagoRef.set(pagoMap).await() }
            }
        }

        val pagosSnapshot = firestore.collection("pagos")
            .whereEqualTo("pacienteId", pacienteId)
            .get()
            .await()

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

            // Buscar comprobante asociado
            val comprobanteSnapshot = firestore.collection("comprobantes")
                .whereEqualTo("pagoId", pago.id)
                .get()
                .await()

            val comprobante = comprobanteSnapshot.documents.firstOrNull()?.let { cDoc ->
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

            // Actualizar pago
            transaction.update(pagoRef, mapOf(
                "estado" to EstadoPago.PAGADO.name,
                "metodoPago" to MetodoPago.EFECTIVO_CLINICA.name,
                "actualizadaEn" to ahora
            ))

            // Actualizar cita (TASK-48: Integración de estados de pago con la cita)
            if (citaId.isNotBlank()) {
                val citaRef = firestore.collection("citas").document(citaId)
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

            // Actualizar pago
            transaction.update(pagoRef, mapOf(
                "estado" to EstadoPago.PAGADO.name,
                "metodoPago" to MetodoPago.ONLINE_MERCADOPAGO.name,
                "referenciaTransaccion" to referenciaTransaccion,
                "actualizadaEn" to ahora
            ))

            // Actualizar cita (TASK-48)
            if (citaId.isNotBlank()) {
                val citaRef = firestore.collection("citas").document(citaId)
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

            comprobante
        }.await()
    }

    suspend fun obtenerComprobantePorCita(citaId: String): Comprobante? {
        val snapshot = firestore.collection("comprobantes")
            .whereEqualTo("citaId", citaId)
            .get()
            .await()

        val doc = snapshot.documents.firstOrNull() ?: return null
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
        val snapshot = firestore.collection("pagos")
            .whereEqualTo("citaId", citaId)
            .get()
            .await()

        val doc = snapshot.documents.firstOrNull()
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

        val citaDoc = firestore.collection("citas").document(citaId).get().await()
        if (!citaDoc.exists()) return null
        val pacienteId = citaDoc.getString("pacienteId").orEmpty()
        val psicologoId = citaDoc.getString("psicologoId").orEmpty()

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
        runCatching { nuevoPagoRef.set(pagoMap).await() }

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
