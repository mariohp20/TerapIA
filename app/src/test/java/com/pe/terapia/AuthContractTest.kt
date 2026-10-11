package com.pe.terapia

import com.google.firebase.Timestamp
import com.pe.terapia.data.remote.firebase.UsuarioFirestoreMapper
import com.pe.terapia.domain.model.PerfilIncompletoException
import com.pe.terapia.domain.model.Rol
import com.pe.terapia.domain.model.Usuario
import com.pe.terapia.domain.repository.AuthRepository
import com.pe.terapia.domain.usecase.auth.CompletarPerfilUseCase
import com.pe.terapia.domain.usecase.auth.LoginConGoogleUseCase
import com.pe.terapia.domain.usecase.auth.LoginUseCase
import com.pe.terapia.domain.usecase.auth.NormalizacionPerfil
import com.pe.terapia.domain.usecase.auth.RecuperarPasswordUseCase
import com.pe.terapia.domain.usecase.auth.RegistroUseCase
import java.util.Date
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthContractTest {
    @Test fun registroCreaPacienteConCamposCanonicos() = runBlocking {
        val repo = FakeAuthRepository()
        val result = RegistroUseCase(repo)(" Ana ", " Paz ", " ANA@EXAMPLE.COM ", "Password1!", null, "12345678")
        assertTrue(result.isSuccess)
        assertEquals("ana@example.com", repo.lastEmail)
        assertEquals("Ana", repo.lastNombres)
        assertEquals("Paz", repo.lastApellidos)
        assertEquals(Rol.PACIENTE, result.getOrThrow().rol)
    }

    @Test fun correoDuplicadoLoRechazaFirebaseAuth() = runBlocking {
        val repo = FakeAuthRepository().apply { registrationResult = Result.failure(IllegalStateException("EMAIL_EXISTS")) }
        val result = RegistroUseCase(repo)("Ana", "Paz", "ana@example.com", "Password1!", null, "12345678")
        assertTrue(result.isFailure)
        assertEquals("EMAIL_EXISTS", result.exceptionOrNull()?.message)
    }

    @Test fun telefonoInvalidoSeRechazaAntesDelRepositorio() = runBlocking {
        val repo = FakeAuthRepository()
        assertTrue(RegistroUseCase(repo)("Ana", "Paz", "a@example.com", "Password1!", "abc", "12345678").isFailure)
        assertFalse(repo.registrationCalled)
    }

    @Test fun dniDebeTenerOchoDigitosAntesDeRegistrar() = runBlocking {
        val repo = FakeAuthRepository()
        assertTrue(RegistroUseCase(repo)("Ana", "Paz", "a@example.com", "Password1!", null, "1234").isFailure)
        assertFalse(repo.registrationCalled)
        assertTrue(RegistroUseCase(repo)("Ana", "Paz", "a@example.com", "Password1!", null, "12 345678").isSuccess)
    }

    @Test fun nombresApellidosCorreoYPasswordSonObligatorios() = runBlocking {
        val repo = FakeAuthRepository()
        assertTrue(RegistroUseCase(repo)("", "Paz", "a@example.com", "Password1!", null, "12345678").isFailure)
        assertTrue(RegistroUseCase(repo)("Ana", "", "a@example.com", "Password1!", null, "12345678").isFailure)
        assertTrue(RegistroUseCase(repo)("Ana", "Paz", "no-es-correo", "Password1!", null, "12345678").isFailure)
        assertTrue(RegistroUseCase(repo)("Ana", "Paz", "a@example.com", "bad", null, "12345678").isFailure)
        assertFalse(repo.registrationCalled)
    }

    @Test fun loginGoogleYRecuperacionUsanCorreoNormalizado() = runBlocking {
        val repo = FakeAuthRepository()
        assertTrue(LoginUseCase(repo)(" ANA@EXAMPLE.COM ", "Password1!").isSuccess)
        assertEquals("ana@example.com", repo.lastEmail)
        assertTrue(LoginConGoogleUseCase(repo)("google-token").isSuccess)
        assertTrue(RecuperarPasswordUseCase(repo)(" ANA@EXAMPLE.COM ").isSuccess)
        assertEquals("ana@example.com", repo.lastEmail)
    }

    @Test fun mapperRechazaPerfilInactivoInexistenteORolInvalido() {
        assertTrue(runCatching { UsuarioFirestoreMapper.fromFirestore("u", null, "u@example.com") }.isFailure)
        assertTrue(runCatching { UsuarioFirestoreMapper.fromFirestore("u", canonical().plus("rol" to "root"), "u@example.com") }.isFailure)
        assertTrue(runCatching { UsuarioFirestoreMapper.fromFirestore("u", canonical().plus("activo" to false), "u@example.com") }.isFailure)
    }

    @Test fun mapperReconocePerfilPacienteLegacyComoPendienteSinInventarApellidos() {
        val legacy = mapOf("nombre" to "Ana Paz", "email" to "ana@example.com", "rol" to "paciente", "activo" to true)
        val error = runCatching { UsuarioFirestoreMapper.fromFirestore("u", legacy, "ana@example.com") }.exceptionOrNull()
        assertTrue(error is PerfilIncompletoException)
    }

    @Test fun mapperLeeSoloCamposCanonicosEnPerfilCompleto() {
        val user = UsuarioFirestoreMapper.fromFirestore("u", canonical(), "ANA@example.com")
        assertEquals("Ana", user.nombres)
        assertEquals("Paz", user.apellidos)
        assertEquals("ana@example.com", user.correo)
        assertEquals(Rol.PACIENTE, user.rol)
        assertEquals("ana@example.com", user.correo)
    }

    @Test fun perfilDeGoogleRequiereNombresYApellidosSinDerivarlos() = runBlocking {
        val repo = FakeAuthRepository().apply { completionResult = Result.failure(PerfilIncompletoException()) }
        val result = CompletarPerfilUseCase(repo)("", "", null, "")
        assertTrue(result.isFailure)
        assertTrue(LoginConGoogleUseCase(repo)("").isFailure)
    }

    @Test fun normalizacionEsDeterminista() {
        assertEquals("user@example.com", NormalizacionPerfil.correo(" User@Example.COM "))
        assertEquals("+51987654321", NormalizacionPerfil.telefono("+51 (987) 654-321"))
    }

    private fun canonical(): Map<String, Any?> = mapOf(
        "nombres" to "Ana", "apellidos" to "Paz", "correo" to "ana@example.com",
        "telefono" to null, "rol" to "paciente", "activo" to true,
        "fechaRegistro" to Timestamp(Date(1_700_000_000_000)), "fotoUrl" to null, "dni" to "12345678"
    )

    private class FakeAuthRepository : AuthRepository {
        var registrationCalled = false
        var registrationResult: Result<Usuario> = Result.success(sampleUser())
        var completionResult: Result<Usuario> = Result.success(sampleUser())
        var lastEmail: String? = null
        var lastNombres: String? = null
        var lastApellidos: String? = null

        override suspend fun login(email: String, password: String): Result<Usuario> {
            lastEmail = email
            return Result.success(sampleUser())
        }

        override suspend fun registro(nombres: String, apellidos: String, correo: String, password: String, telefono: String?, dni: String): Result<Usuario> {
            registrationCalled = true
            lastNombres = nombres
            lastApellidos = apellidos
            lastEmail = correo
            return registrationResult
        }

        override suspend fun loginConGoogle(idToken: String): Result<Usuario> = Result.success(sampleUser())
        override suspend fun completarPerfil(nombres: String, apellidos: String, telefono: String?, dni: String): Result<Usuario> = completionResult
        override suspend fun obtenerBorradorPerfil() = Result.success(com.pe.terapia.domain.model.PerfilPacienteBorrador())
        override suspend fun recuperarPassword(email: String): Result<Unit> { lastEmail = email; return Result.success(Unit) }
        override suspend fun obtenerUsuarioActual(): Result<Usuario?> = Result.success(sampleUser())
        override suspend fun cerrarSesion() = Unit

        companion object {
            fun sampleUser() = Usuario("u", "Ana", "Paz", "ana@example.com", null, Rol.PACIENTE, true, java.time.Instant.EPOCH, null)
        }
    }
}
