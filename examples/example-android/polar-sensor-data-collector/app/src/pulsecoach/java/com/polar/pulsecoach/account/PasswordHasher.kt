package com.polar.pulsecoach.account

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Salted PBKDF2-HMAC-SHA256 password hashing for the local account stub. */
class PasswordHasher(private val iterations: Int = 120_000) {
    private val random = SecureRandom()

    fun newSalt(): String = ByteArray(16).also(random::nextBytes).let(::encode)

    fun hash(password: String, salt: String): String {
        val spec = PBEKeySpec(password.toCharArray(), decode(salt), iterations, 256)
        try {
            return encode(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded)
        } finally {
            spec.clearPassword()
        }
    }

    fun verify(password: String, salt: String, expectedHash: String): Boolean =
        MessageDigest.isEqual(decode(hash(password, salt)), decode(expectedHash))

    private fun encode(bytes: ByteArray) = Base64.getEncoder().encodeToString(bytes)
    private fun decode(text: String) = Base64.getDecoder().decode(text)
}
