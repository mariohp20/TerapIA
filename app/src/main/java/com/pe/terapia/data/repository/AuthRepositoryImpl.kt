package com.pe.terapia.data.repository

import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.pe.terapia.data.remote.firebase.AuthDataSource
import com.pe.terapia.data.remote.firebase.UsuarioFirestoreMapper
import com.pe.terapia.domain.model.PerfilIncompletoException
import com.pe.terapia.domain.model.PerfilPacienteBorrador
import com.pe.terapia.domain.model.Usuario
import com.pe.terapia.domain.repository.AuthRepository
import com.pe.terapia.domain.usecase.auth.NormalizacionPerfil
import kotlinx.coroutines.CancellationException

class AuthRepositoryImpl(private val dataSource: AuthDataSource) : AuthRepository {
    override suspend fun login(email: String, password: String): Result<Usuario> = capture {
        val uid = dataSource.login(NormalizacionPerfil.correo(email), password)
        obtenerPerfilConLimpieza(uid)
    }

    override suspend fun registro(
        nombres: String,
        apellidos: String,
        correo: String,
        password: String,
        telefono: String?,
        dni: String
    ): Result<Usuario> {
        val uid = try {
            dataSource.registro(NormalizacionPerfil.correo(correo), password)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            return Result.failure(exception)
        }

        return capture {
            try {
                dataSource.completarPerfilPaciente(nombres, apellidos, telefono, dni)
                obtenerPerfilConLimpieza(uid)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                throw PerfilIncompletoException(exception.message ?: "Completa los datos de tu perfil para continuar.")
            }
        }
    }

    override suspend fun loginConGoogle(idToken: String): Result<Usuario> {
        val user = try {
            dataSource.loginConGoogle(idToken)
        } catch (exception: FirebaseAuthUserCollisionException) {
            return Result.failure(IllegalStateException("Ya existe una cuenta con este correo usando otro método de acceso.", exception))
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            return Result.failure(exception)
        }

        return capture {
            val perfil = dataSource.obtenerDatosUsuario(user.uid)
            if (perfil == null) throw PerfilIncompletoException()
            obtenerPerfilConLimpieza(user.uid)
        }
    }

    override suspend fun completarPerfil(
        nombres: String,
        apellidos: String,
        telefono: String?,
        dni: String
    ): Result<Usuario> = capture {
        val uid = dataSource.obtenerUsuarioActual()?.uid
            ?: throw IllegalStateException("La sesión expiró. Inicia sesión nuevamente.")
        dataSource.completarPerfilPaciente(nombres, apellidos, telefono, dni)
        obtenerPerfilConLimpieza(uid)
    }

    override suspend fun obtenerBorradorPerfil(): Result<PerfilPacienteBorrador> = capture {
        dataSource.obtenerBorradorPerfil()
    }

    override suspend fun recuperarPassword(email: String): Result<Unit> = capture {
        dataSource.recuperarPassword(NormalizacionPerfil.correo(email))
    }

    override suspend fun obtenerUsuarioActual(): Result<Usuario?> = capture {
        val user = dataSource.obtenerUsuarioActual() ?: return@capture null
        if (dataSource.obtenerDatosUsuario(user.uid) == null && user.providerData.any { it.providerId == "google.com" }) {
            throw PerfilIncompletoException()
        }
        obtenerPerfilConLimpieza(user.uid)
    }

    override suspend fun cerrarSesion() = dataSource.cerrarSesion()

    private suspend fun obtenerPerfil(uid: String): Usuario {
        val datos = dataSource.obtenerDatosUsuario(uid) ?: throw PerfilIncompletoException()
        val authEmail = dataSource.obtenerUsuarioActual()?.email?.let(NormalizacionPerfil::correo)
        return UsuarioFirestoreMapper.fromFirestore(uid, datos, authEmail)
    }

    private suspend fun obtenerPerfilConLimpieza(uid: String): Usuario = try {
        obtenerPerfil(uid)
    } catch (exception: PerfilIncompletoException) {
        throw exception
    } catch (exception: IllegalStateException) {
        try {
            dataSource.cerrarSesion()
        } catch (cleanupException: Exception) {
            exception.addSuppressed(cleanupException)
        }
        throw exception
    }

    private suspend fun <T> capture(action: suspend () -> T): Result<T> = try {
        Result.success(action())
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }
}
