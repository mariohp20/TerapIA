package com.pe.terapia.domain.model

data class CitaAdminResumen(
    val psicologoId: String,
    val inicioEpochMillis: Long,
    val estado: String
)

data class PagoAdminResumen(
    val fechaMillis: Long,
    val monto: Double,
    val moneda: String
)

data class PsicologoAdminResumen(
    val id: String,
    val nombre: String
)

data class DashboardAdminData(
    val citas: List<CitaAdminResumen>,
    val pagos: List<PagoAdminResumen>,
    val psicologos: List<PsicologoAdminResumen>
)

data class DatoMensualAdmin(val etiqueta: String, val cantidad: Double)
data class CargaPsicologoAdmin(val nombre: String, val atenciones: Int)
