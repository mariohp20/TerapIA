package com.pe.terapia.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.pe.terapia.domain.model.PerfilPacienteBorrador
import kotlinx.coroutines.tasks.await

class AuthDataSource(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    suspend fun login(email: String, password: String): String {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user?.uid ?: throw IllegalStateException("No se pudo obtener el usuario.")
    }

    suspend fun registro(email: String, password: String): String {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user?.uid ?: throw IllegalStateException("No se pudo crear el usuario.")
    }

    suspend fun loginConGoogle(idToken: String): FirebaseUser {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        return result.user ?: throw IllegalStateException("No se pudo iniciar sesión con Google.")
    }

    suspend fun completarPerfilPaciente(
        nombres: String,
        apellidos: String,
        telefono: String?,
        dni: String
    ) {
        val user = auth.currentUser ?: throw IllegalStateException("La sesión expiró. Vuelve a iniciar sesión.")
        user.reload().await()
        val email = user.email?.trim()?.lowercase()
            ?: throw IllegalStateException("La cuenta no tiene un correo de autenticación.")
        val profileRef = firestore.collection("usuarios").document(user.uid)
        firestore.runTransaction { transaction ->
            val existing = transaction.get(profileRef)
            val previousRole = existing.getString("rol")
            check(previousRole == null || previousRole == "paciente") {
                "Solo una cuenta paciente puede completar este perfil."
            }
            check(existing.getBoolean("activo") != false) { "La cuenta está desactivada." }
            check(existing.getString("uid") == null || existing.getString("uid") == user.uid) {
                "El perfil legacy no coincide con la sesión autenticada. Contacta con soporte."
            }
            val legacyEmail = (existing.getString("correo") ?: existing.getString("email"))?.trim()?.lowercase()
            check(legacyEmail == null || legacyEmail == email) {
                "El correo del perfil no coincide con Firebase Authentication. Contacta con soporte."
            }
            val previousDni = existing.getString("dni")
            check(previousDni.isNullOrBlank() || previousDni == dni) {
                "El DNI ya asociado al perfil no se puede cambiar desde la aplicación. Contacta con soporte."
            }
            val data = mutableMapOf<String, Any>(
                "nombres" to nombres.trim(),
                "apellidos" to apellidos.trim(),
                "correo" to email,
                "rol" to "paciente",
                "activo" to true,
                "fechaRegistro" to (existing.get("fechaRegistro") as? Timestamp ?: FieldValue.serverTimestamp()),
                "dni" to (previousDni ?: dni)
            )
            (telefono ?: existing.getString("telefono"))?.let { data["telefono"] = it }
            (user.photoUrl?.toString() ?: existing.getString("fotoUrl"))?.let { data["fotoUrl"] = it }
            if (existing.exists()) {
                transaction.update(profileRef, data)
            } else {
                transaction.set(profileRef, data)
            }
            null
        }.await()
    }

    suspend fun obtenerDatosUsuario(uid: String): Map<String, Any?>? {
        val snapshot = firestore.collection("usuarios").document(uid).get().await()
        return if (snapshot.exists()) snapshot.data else null
    }

    suspend fun obtenerBorradorPerfil(): PerfilPacienteBorrador {
        val user = auth.currentUser ?: throw IllegalStateException("La sesión expiró. Inicia sesión nuevamente.")
        val fields = obtenerDatosUsuario(user.uid).orEmpty()
        val storedNames = (fields["nombres"] as? String).orEmpty().trim()
        val legacyName = (fields["nombre"] as? String).orEmpty().trim()
        val storedSurnames = (fields["apellidos"] as? String).orEmpty().trim()
        return PerfilPacienteBorrador(
            // El nombre del proveedor puede combinar nombres y apellidos; no inferimos campos personales.
            nombres = storedNames.ifBlank { legacyName.ifBlank { user.displayName.orEmpty().trim() } },
            apellidos = storedSurnames,
            telefono = (fields["telefono"] as? String).orEmpty(),
            dni = (fields["dni"] as? String).orEmpty(),
            correo = user.email.orEmpty(),
            dniYaAsociado = !(fields["dni"] as? String).isNullOrBlank()
        )
    }

    suspend fun recuperarPassword(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    fun obtenerUsuarioActual(): FirebaseUser? = auth.currentUser

    suspend fun cerrarSesion() {
        auth.signOut()
    }

}
