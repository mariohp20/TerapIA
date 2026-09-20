package com.pe.terapia.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.usecase.auth.LoginUseCase
import com.pe.terapia.domain.usecase.auth.RegistroUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val registroUseCase: RegistroUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            loginUseCase(email, password)
                .onSuccess { _uiState.value = AuthUiState.Success(it) }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Ocurrió un error al iniciar sesión.") }
        }
    }

    fun registro(email: String, password: String, nombre: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            registroUseCase(email, password, nombre)
                .onSuccess { _uiState.value = AuthUiState.Success(it) }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Ocurrió un error al registrarte.") }
        }
    }
}