package com.polar.pulsecoach.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polar.pulsecoach.account.AuthRepository
import com.polar.pulsecoach.account.AuthResult
import com.polar.pulsecoach.account.PasswordStrength
import com.polar.pulsecoach.account.Session
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Exposes the session so the app can pick: auth screens, profile setup, or the main tabs. */
@HiltViewModel
class RootViewModel @Inject constructor(repository: AuthRepository) : ViewModel() {
    val session: StateFlow<Session?> = repository.session
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onEmail(value: String) = _state.update { it.copy(email = value, error = null) }
    fun onPassword(value: String) = _state.update { it.copy(password = value, error = null) }

    fun login() {
        val s = _state.value
        if (s.loading) return
        if (s.email.isBlank() || s.password.isEmpty()) {
            _state.update { it.copy(error = "Enter your email and password.") }
            return
        }
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val result = repository.login(s.email, s.password)
            _state.update {
                it.copy(loading = false, error = (result as? AuthResult.Failure)?.error?.message)
            }
        }
    }
}

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val coachDataConsent: Boolean = false,
    val termsAccepted: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null
) {
    val strengthScore: Int get() = PasswordStrength.score(password)
    val strengthLabel: String get() = PasswordStrength.label(password)
    val canSubmit: Boolean get() = termsAccepted && !loading
}

@HiltViewModel
class RegisterViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    fun onName(value: String) = _state.update { it.copy(name = value, error = null) }
    fun onEmail(value: String) = _state.update { it.copy(email = value, error = null) }
    fun onPassword(value: String) = _state.update { it.copy(password = value, error = null) }
    fun onCoachConsent(value: Boolean) = _state.update { it.copy(coachDataConsent = value) }
    fun onTerms(value: Boolean) = _state.update { it.copy(termsAccepted = value) }

    /** On success the session starts and the app moves on to profile setup. */
    fun register() {
        val s = _state.value
        if (!s.canSubmit) return
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val result = repository.register(s.name, s.email, s.password, s.coachDataConsent)
            _state.update {
                it.copy(loading = false, error = (result as? AuthResult.Failure)?.error?.message)
            }
        }
    }
}
