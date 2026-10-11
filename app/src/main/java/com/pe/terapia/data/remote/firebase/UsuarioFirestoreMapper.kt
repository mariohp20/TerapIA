package com.pe.terapia.data.remote.firebase

import com.google.firebase.Timestamp
import com.pe.terapia.domain.model.PerfilIncompletoException
import com.pe.terapia.domain.model.Rol
import com.pe.terapia.domain.model.Usuario
import com.pe.terapia.domain.usecase.auth.NormalizacionPerfil

object UsuarioFirestoreMapper {
    fun fromFirestore(uid: String, data: Map<String, Any?>?, authEmail: String?): Usuario {
        val fields = data ?: throw IllegalStateException("La cuenta no tiene un perfil autorizado.")
        check((fields["uid"] as? String).let { it == null || it == uid }) {
            "El UID del perfil no coincide con la sesión autenticada."
        }
        val role = Rol.fromValue(fields["rol"] as? String)
            ?: throw IllegalStateException("El perfil no tiene un rol válido.")
        val active = fields["activo"] as? Boolean
            ?: throw IllegalStateException("El perfil no tiene un estado válido.")
        check(active) { "La cuenta está desactivada. Contacta con soporte." }

        val normalizedAuthEmail = authEmail?.let(NormalizacionPerfil::correo)
            ?: throw IllegalStateException("La cuenta no tiene un correo de autenticación válido.")
        val email = (fields["correo"] as? String ?: fields["email"] as? String)
            ?.let(NormalizacionPerfil::correo)
            ?: throw IllegalStateException("El perfil no tiene un correo válido.")
        check(email == normalizedAuthEmail) { "El correo del perfil no coincide con Firebase Authentication." }

        val names = (fields["nombres"] as? String ?: fields["nombre"] as? String).orEmpty().trim()
        val surnames = (fields["apellidos"] as? String).orEmpty().trim()
        val registeredAt = (fields["fechaRegistro"] as? Timestamp)?.toDate()?.toInstant()
        val dni = (fields["dni"] as? String)?.takeIf { it.matches(Regex("\\d{8}")) }
        if (names.isBlank() || surnames.isBlank() || registeredAt == null || (role == Rol.PACIENTE && dni == null)) {
            if (role == Rol.PACIENTE) throw PerfilIncompletoException()
            throw IllegalStateException("El perfil requiere revisión administrativa antes de iniciar sesión.")
        }

        return Usuario(
            uid = uid,
            nombres = names,
            apellidos = surnames,
            correo = email,
            telefono = (fields["telefono"] as? String)?.takeIf(String::isNotBlank),
            rol = role,
            activo = active,
            fechaRegistro = registeredAt,
            fotoUrl = fields["fotoUrl"] as? String,
            dni = dni
        )
    }
}
