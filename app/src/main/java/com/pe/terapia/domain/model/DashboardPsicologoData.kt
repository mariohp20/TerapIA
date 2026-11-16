package com.pe.terapia.domain.model

data class ConteoEtapaPsicologo(val nombre: String, val cantidad: Int)
data class ConteoMensualPsicologo(val periodo: String, val etiqueta: String, val cantidad: Int)
data class ComparativaMensualPsicologo(val periodo: String, val etiqueta: String, val citas: Int, val atenciones: Int)

data class DashboardPsicologoData(
    val atencionesPorMes: List<ConteoMensualPsicologo>,
    val pacientesPorEtapa: List<ConteoEtapaPsicologo>,
    val comparativaAsistencia: List<ComparativaMensualPsicologo>
)
