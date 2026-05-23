package com.canvasvibe.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canvasvibe.app.data.model.User
import com.canvasvibe.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle    : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Registered(val user: User) : AuthState()
    data class PasswordResetSent(val email: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class LoginViewModel : ViewModel() {
    private val repo = AuthRepository()

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            val result = repo.login(email, password)
            _state.value = if (result.isSuccess)
                AuthState.Success(result.getOrThrow())
            else
                AuthState.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
        }
    }

    fun register(email: String, password: String, name: String, role: String = "ROLE_BUYER") {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            val result = repo.register(email, password, name, role)
            _state.value = if (result.isSuccess) {
                repo.logout()
                AuthState.Registered(result.getOrThrow())
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
            }
        }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _state.value = AuthState.Error("Ingresa tu correo electrónico")
            return
        }
        viewModelScope.launch {
            _state.value = AuthState.Loading
            val result = repo.sendPasswordResetEmail(email.trim())
            _state.value = if (result.isSuccess)
                AuthState.PasswordResetSent(email.trim())
            else
                AuthState.Error(friendlyReset(result.exceptionOrNull()))
        }
    }

    private fun friendlyReset(e: Throwable?): String {
        val raw = e?.message.orEmpty()
        return when {
            raw.contains("no user record", true) ||
                raw.contains("there is no user", true) ->
                "No existe una cuenta con ese correo"
            raw.contains("badly formatted", true) ||
                raw.contains("email address is badly", true) ->
                "El correo no tiene un formato válido"
            raw.contains("network", true) -> "Sin conexión a internet"
            raw.isBlank() -> "No se pudo enviar el correo de recuperación"
            else -> raw
        }
    }

    fun resetState() { _state.value = AuthState.Idle }
}