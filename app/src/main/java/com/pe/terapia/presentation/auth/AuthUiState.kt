package com.pe.terapia.presentation.auth

import com.pe.terapia.domain.model.Usuario

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data object PasswordRecoverySent : AuthUiState
    data object SignedOut : AuthUiState
    data class ProfileCompletionRequired(val mensaje: String? = null) : AuthUiState
    data class Success(val usuario: Usuario) : AuthUiState
    data class Error(val mensaje: String) : AuthUiState
}
