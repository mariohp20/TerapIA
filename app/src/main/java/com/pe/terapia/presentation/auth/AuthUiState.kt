package com.pe.terapia.presentation.auth

import com.pe.terapia.domain.model.Usuario

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Success(val usuario: Usuario) : AuthUiState
    data class Error(val mensaje: String) : AuthUiState
}