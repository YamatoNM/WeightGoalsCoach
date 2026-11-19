package com.polar.pulsecoach.account

/** Password strength as shown on the Register screen's four bars. */
object PasswordStrength {
    const val MIN_LENGTH = 8

    /** 0–4: length ≥ 8, mixed case, a digit, a symbol or length ≥ 14. */
    fun score(password: String): Int {
        var score = 0
        if (password.length >= MIN_LENGTH) score++
        if (password.any { it.isUpperCase() } && password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() } || password.length >= 14) score++
        return score
    }

    fun label(password: String): String {
        if (password.isEmpty()) return "Use 8+ characters with a mix of letters and numbers"
        return when (score(password)) {
            0 -> "Strength: Too weak"
            1 -> "Strength: Weak"
            2 -> "Strength: Fair"
            3 -> "Strength: Good"
            else -> "Strength: Strong"
        }
    }
}
