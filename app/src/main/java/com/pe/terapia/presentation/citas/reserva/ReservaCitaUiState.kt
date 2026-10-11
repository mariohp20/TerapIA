package com.pe.terapia.presentation.citas.reserva

import com.pe.terapia.domain.model.Especialidad
import com.pe.terapia.domain.model.Psicologo
import com.pe.terapia.domain.model.Sede
import com.pe.terapia.domain.model.SlotDisponible

data class ReservaCitaUiState(
    val especialidades: List<Especialidad> = emptyList(),
    val cargandoEspecialidades: Boolean = false,
    val especialidadSeleccionada: Especialidad? = null,
    val cargandoPsicologos: Boolean = false,
    val psicologos: List<Psicologo> = emptyList(),
    val psicologoSeleccionado: Psicologo? = null,
    val sedes: List<Sede> = emptyList(),
    val cargandoSedes: Boolean = false,
    val sedeSeleccionada: Sede? = null,
    val fechaSeleccionada: String? = null,
    val fechasConsultadas: Set<String> = emptySet(),
    val fechasConDisponibilidad: Set<String> = emptySet(),
    val disponibilidadPorFecha: Map<String, List<SlotDisponible>> = emptyMap(),
    val cargandoFechas: Boolean = false,
    val cargandoHorarios: Boolean = false,
    val horariosDisponibles: List<String> = emptyList(),
    val slotsDisponibles: List<SlotDisponible> = emptyList(),
    val horaSeleccionada: String? = null,
    val error: String? = null
) {
    val puedeContinuar: Boolean
        get() = especialidadSeleccionada != null && psicologoSeleccionado != null && sedeSeleccionada != null &&
            fechaSeleccionada != null && horaSeleccionada != null
}
