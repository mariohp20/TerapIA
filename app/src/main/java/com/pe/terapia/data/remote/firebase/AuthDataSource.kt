package com.pe.terapia.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthDataSource(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun login(email: String, password: String): String {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user?.uid ?: throw IllegalStateException("No se pudo obtener el usuario.")
    }

    suspend fun registro(email: String, password: String): String {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user?.uid ?: throw IllegalStateException("No se pudo crear el usuario.")
    }

    suspend fun loginConGoogle(idToken: String): String {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        return result.user?.uid ?: throw IllegalStateException("No se pudo iniciar sesión con Google.")
    }

    suspend fun guardarUsuarioEnFirestore(uid: String, nombre: String, email: String, rol: String) {
        val data = mapOf("uid" to uid, "nombre" to nombre, "email" to email, "rol" to rol)
        firestore.collection("usuarios").document(uid).set(data).await()
    }

    suspend fun obtenerDatosUsuario(uid: String): Map<String, Any>? {
        val snapshot = firestore.collection("usuarios").document(uid).get().await()
        return if (snapshot.exists()) snapshot.data else null
    }

    suspend fun recuperarPassword(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    fun obtenerUsuarioActual(): FirebaseUser? = auth.currentUser

    fun obtenerUidActual(): String? = auth.currentUser?.uid

    suspend fun cerrarSesion() {
        auth.signOut()
    }
}