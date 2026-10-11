package com.pe.terapia.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.usecase.auth.LoginConGoogleUseCase
import com.pe.terapia.domain.model.PerfilIncompletoException
import com.pe.terapia.domain.usecase.auth.LoginUseCase
import com.pe.terapia.domain.usecase.auth.CerrarSesionUseCase
import com.pe.terapia.domain.usecase.auth.RecuperarPasswordUseCase
import com.pe.terapia.domain.usecase.auth.RegistroUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val registroUseCase: RegistroUseCase,
    private val loginConGoogleUseCase: LoginConGoogleUseCase,
    private val recuperarPasswordUseCase: RecuperarPasswordUseCase,
    private val cerrarSesionUseCase: CerrarSesionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            loginUseCase(email, password)
                .onSuccess { _uiState.value = AuthUiState.Success(it) }
                .onFailure { exception ->
                    _uiState.value = if (exception is PerfilIncompletoException) AuthUiState.ProfileCompletionRequired(exception.message)
                    else AuthUiState.Error(exception.message ?: "Ocurrió un error al iniciar sesión.")
                }
        }
    }

    fun registro(nombres: String, apellidos: String, email: String, password: String, telefono: String?, dni: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            registroUseCase(nombres, apellidos, email, password, telefono, dni)
                .onSuccess { _uiState.value = AuthUiState.Success(it) }
                .onFailure { exception ->
                    _uiState.value = if (exception is PerfilIncompletoException) AuthUiState.ProfileCompletionRequired(exception.message)
                    else AuthUiState.Error(exception.message ?: "Ocurrió un error al registrarte.")
                }
        }
    }

    fun loginConGoogle(obtenerIdToken: suspend () -> Result<String>) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val tokenResult = try {
                obtenerIdToken()
            } catch (exception: CancellationException) {
                _uiState.value = AuthUiState.Idle
                throw exception
            } catch (exception: Exception) {
                _uiState.value = AuthUiState.Error(exception.message ?: "Ocurrió un error con Google.")
                return@launch
            }
            tokenResult
                .onSuccess { token ->
                    loginConGoogleUseCase(token)
                        .onSuccess { _uiState.value = AuthUiState.Success(it) }
                        .onFailure { exception ->
                            _uiState.value = if (exception is PerfilIncompletoException) {
                                AuthUiState.ProfileCompletionRequired(exception.message)
                            } else {
                                AuthUiState.Error(exception.message ?: "Ocurrió un error con Google.")
                            }
                        }
                }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Ocurrió un error con Google.") }
        }
    }

    fun recuperarPassword(email: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            recuperarPasswordUseCase(email)
                .onSuccess { _uiState.value = AuthUiState.PasswordRecoverySent }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "No se pudo enviar el correo de recuperación.") }
        }
    }

    fun informarError(mensaje: String) {
        _uiState.value = AuthUiState.Error(mensaje)
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            cerrarSesionUseCase()
                .onSuccess { _uiState.value = AuthUiState.SignedOut }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "No se pudo cerrar la sesión.") }
        }
    }
}
