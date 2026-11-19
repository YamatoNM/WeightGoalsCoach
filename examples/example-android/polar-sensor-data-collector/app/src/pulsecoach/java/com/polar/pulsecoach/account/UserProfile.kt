package com.polar.pulsecoach.account

import kotlin.math.roundToInt

/**
 * What Pulse Coach knows about the user. Body values are optional until the
 * profile step of registration is done ([profileCompleted]).
 */
data class UserProfile(
    val email: String,
    val name: String,
    val heightCm: Float? = null,
    val weightKg: Float? = null,
    val age: Int? = null,
    val bodyFatPercent: Float? = null,
    /** Measured max HR. When null, it is estimated from [age]. */
    val maxHrOverride: Int? = null,
    /** User allowed Coach to use heart-rate and body data for suggestions. */
    val coachDataConsent: Boolean = false,
    val profileCompleted: Boolean = false,
    /** Admin-only: shows the entry to the PSDC raw-data screens. Ignored for non-admins. */
    val developerToolsEnabled: Boolean = false
) {
    /** Max HR in bpm: the measured value, else the Tanaka estimate 208 − 0.7 × age. */
    val maxHr: Int? get() = maxHrOverride ?: age?.let { estimateMaxHr(it) }

    val bmi: Float?
        get() {
            val h = heightCm ?: return null
            val w = weightKg ?: return null
            if (h <= 0f) return null
            val m = h / 100f
            return w / (m * m)
        }

    val fatMassKg: Float? get() = weightKg?.let { w -> bodyFatPercent?.let { w * it / 100f } }
    val leanMassKg: Float? get() = weightKg?.let { w -> fatMassKg?.let { w - it } }

    val zones: List<HeartRateZone> get() = maxHr?.let { HeartRateZones.fromMaxHr(it) } ?: emptyList()

    companion object {
        fun estimateMaxHr(age: Int): Int = (208 - 0.7 * age).roundToInt()
    }
}

enum class Role { USER, ADMIN }

/** The signed-in user. [role] is derived at sign-in, never stored. */
data class Session(val profile: UserProfile, val role: Role) {
    val isAdmin: Boolean get() = role == Role.ADMIN
    /** Raw-data (PSDC) screens are reachable only for admins who turned the setting on. */
    val rawDataAccess: Boolean get() = isAdmin && profile.developerToolsEnabled
}
