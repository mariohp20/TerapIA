package com.pe.terapia.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.pe.terapia.data.remote.firebase.UsuarioFirestoreMapper
import com.pe.terapia.domain.model.Especialidad
import com.pe.terapia.domain.model.Sede
import com.pe.terapia.domain.model.Usuario
class AdminDataSource(
    private val firestore: FirebaseFirestore
) {
    suspend fun promoverPacienteAPsicologo(
        usuarioId: String,
        nombres: String,
        apellidos: String,
        descripcionProfesional: String,
        especialidadIds: List<String>,
        sedeIds: List<String>
    ): Result<Unit> = runCatching {

        val usuarioRef = firestore.collection("usuarios").document(usuarioId)
        val psicologoRef = firestore.collection("psicologos").document(usuarioId)

        val usuarioSnapshot = usuarioRef.get().await()
        if (!usuarioSnapshot.exists()) {
            throw Exception("El usuario no existe en la base de datos.")
        }

        // Creamos el batch (lote de escritura)
        val batch = firestore.batch()

        // 1. Actualizamos el rol en la colección de usuarios
        batch.update(usuarioRef, "rol", "psicologo")

        // 2. Creamos el nuevo documento en la colección de psicólogos
        // Nota: Usamos el mismo ID de usuario para el documento
        val psicologoData = hashMapOf(
            "nombres" to nombres,
            "apellidos" to apellidos,
            "activo" to true,
            "descripcionProfesional" to descripcionProfesional,
            "usuarioId" to usuarioId,
            "especialidadIds" to especialidadIds,
            "sedeIds" to sedeIds
        )
        batch.set(psicologoRef, psicologoData)

        // Ejecutamos ambas operaciones de forma atómica
        batch.commit().await()
    }

    suspend fun obtenerPacientes(): List<Usuario> {
        val snapshot = firestore.collection("usuarios")
            .whereEqualTo("rol", "paciente")
            .whereEqualTo("activo", true)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            runCatching {
                UsuarioFirestoreMapper.fromFirestore(doc.id, doc.data, doc.getString("correo"))
            }.getOrNull()
        }
    }

    suspend fun obtenerSedesGlobales(): List<Sede> {
        val snapshot = firestore.collection("sedes")
            .whereEqualTo("activa", true)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            Sede(
                id = doc.id,
                nombre = doc.getString("nombre") ?: return@mapNotNull null,
                direccion = doc.getString("direccion").orEmpty(),
                distrito = doc.getString("distrito").orEmpty(),
                ciudad = doc.getString("ciudad").orEmpty(),
                latitud = doc.getDouble("latitud") ?: 0.0,
                longitud = doc.getDouble("longitud") ?: 0.0,
                telefono = doc.getString("telefono").orEmpty(),
                activa = true
            )
        }
    }

    suspend fun obtenerEspecialidadesGlobales(): List<Especialidad> {
        val snapshot = firestore.collection("especialidades")
            .whereEqualTo("activa", true)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            Especialidad(
                id = doc.id,
                nombre = doc.getString("nombre") ?: return@mapNotNull null,
                descripcion = doc.getString("descripcion").orEmpty(),
                activa = true,
                costo_cita = doc.getDouble("costo_cita")?: return@mapNotNull null
            )
        }
    }
    suspend fun asignarHorarioPsicologo(
        psicologoId: String,
        sedeId: String,
        diaSemana: Int,
        inicioMinuto: Int,
        finMinuto: Int
    ) {
        val horarioId = "${psicologoId}~${sedeId}"
        val horarioRef = firestore.collection("horariosLaborales").document(horarioId)

        val batch = firestore.batch()

        // 1. Crear el documento principal del horario
        val horarioData = mapOf(
            "activo" to true,
            "duracionMinutos" to 60,
            "psicologoId" to psicologoId,
            "sedeId" to sedeId,
            "zonaHoraria" to "America/Lima"
        )
        batch.set(horarioRef, horarioData)

        // 2. Crear el bloque de disponibilidad
        val bloqueRef = horarioRef.collection("bloques").document()
        val bloqueData = mapOf(
            "activo" to true,
            "diaSemana" to diaSemana,
            "inicioMinuto" to inicioMinuto,
            "finMinuto" to finMinuto
        )
        batch.set(bloqueRef, bloqueData)

        // Subimos todo a Firebase al mismo tiempo
        batch.commit().await()
    }
}