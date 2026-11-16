package com.pe.terapia.data.local.room

import androidx.room.Entity
import androidx.room.Index

@Entity(tableName = "agenda_psicologo_local", primaryKeys = ["psicologoId", "fecha", "citaId"], indices = [Index(value = ["psicologoId", "fecha"])])
data class AgendaPsicologoEntity(
    val psicologoId: String,
    val fecha: String,
    val citaId: String,
    val pacienteId: String,
    val hora: String,
    val nombrePaciente: String,
    val inicioEpochMillis: Long
)
