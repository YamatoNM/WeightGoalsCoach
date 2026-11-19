package com.polar.pulsecoach.account

import kotlinx.coroutines.flow.StateFlow

/**
 * Account and session API. The current implementation is [LocalAuthRepository]
 * (accounts on this phone only); a server-backed one can replace it behind this interface.
 */
interface AuthRepository {
    /** The signed-in user, or null when signed out. */
    val session: StateFlow<Session?>

    suspend fun register(name: String, email: String, password: String, coachDataConsent: Boolean): AuthResult
    suspend fun login(email: String, password: String): AuthResult
    /** Saves the signed-in user's profile. The email cannot change. */
    suspend fun updateProfile(profile: UserProfile): AuthResult
    suspend fun logout()
}

sealed interface AuthResult {
    object Success : AuthResult
    data class Failure(val error: AuthError) : AuthResult
}

enum class AuthError(val message: String) {
    NAME_REQUIRED("Enter your name."),
    INVALID_EMAIL("Enter a valid email address."),
    WEAK_PASSWORD("Use at least ${PasswordStrength.MIN_LENGTH} characters."),
    EMAIL_TAKEN("An account with this email already exists. Log in instead."),
    WRONG_CREDENTIALS("Email or password is incorrect."),
    NOT_SIGNED_IN("You're signed out. Log in again."),
    STORAGE("Couldn't save your account on this phone. Try again.")
}
