package com.polar.pulsecoach.account

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Minimal string storage, so the repository can be tested without Android. */
interface KeyValueStore {
    fun getString(key: String): String?
    fun putString(key: String, value: String?)
}

/**
 * Local stub of the account backend: accounts live only on this phone, in [store].
 * Passwords are stored as salted PBKDF2 hashes. Users whose email is in [adminEmails]
 * get [Role.ADMIN] when they sign in.
 */
class LocalAuthRepository(
    private val store: KeyValueStore,
    adminEmails: Set<String>,
    private val ioDispatcher: CoroutineDispatcher,
    private val hasher: PasswordHasher = PasswordHasher()
) : AuthRepository {

    private data class StoredAccount(
        val email: String,
        val salt: String,
        val passwordHash: String,
        val profile: UserProfile
    )

    private val gson = Gson()
    private val accountsType = object : TypeToken<List<StoredAccount>>() {}.type
    private val mutex = Mutex()
    private val adminEmails = adminEmails.map { normalize(it) }.filter { it.isNotEmpty() }.toSet()

    private val _session = MutableStateFlow(restoreSession())
    override val session: StateFlow<Session?> = _session.asStateFlow()

    override suspend fun register(name: String, email: String, password: String, coachDataConsent: Boolean): AuthResult {
        val cleanName = name.trim()
        val cleanEmail = normalize(email)
        when {
            cleanName.isEmpty() -> return fail(AuthError.NAME_REQUIRED)
            !isValidEmail(cleanEmail) -> return fail(AuthError.INVALID_EMAIL)
            password.length < PasswordStrength.MIN_LENGTH -> return fail(AuthError.WEAK_PASSWORD)
        }
        return locked {
            val accounts = readAccounts()
            if (accounts.any { it.email == cleanEmail }) return@locked fail(AuthError.EMAIL_TAKEN)
            val salt = hasher.newSalt()
            val profile = UserProfile(email = cleanEmail, name = cleanName, coachDataConsent = coachDataConsent)
            val account = StoredAccount(cleanEmail, salt, hasher.hash(password, salt), profile)
            writeAccounts(accounts + account)
            startSession(profile)
            AuthResult.Success
        }
    }

    override suspend fun login(email: String, password: String): AuthResult {
        val cleanEmail = normalize(email)
        return locked {
            val account = readAccounts().firstOrNull { it.email == cleanEmail }
            if (account == null || !hasher.verify(password, account.salt, account.passwordHash)) {
                return@locked fail(AuthError.WRONG_CREDENTIALS)
            }
            startSession(account.profile)
            AuthResult.Success
        }
    }

    override suspend fun updateProfile(profile: UserProfile): AuthResult = locked {
        val current = _session.value ?: return@locked fail(AuthError.NOT_SIGNED_IN)
        val accounts = readAccounts()
        val account = accounts.firstOrNull { it.email == current.profile.email }
            ?: return@locked fail(AuthError.NOT_SIGNED_IN)
        val updated = profile.copy(email = account.email)
        writeAccounts(accounts.map { if (it.email == account.email) it.copy(profile = updated) else it })
        _session.value = current.copy(profile = updated)
        AuthResult.Success
    }

    override suspend fun logout() {
        locked {
            store.putString(KEY_SESSION_EMAIL, null)
            _session.value = null
            AuthResult.Success
        }
    }

    private fun startSession(profile: UserProfile) {
        store.putString(KEY_SESSION_EMAIL, profile.email)
        _session.value = sessionFor(profile)
    }

    private fun restoreSession(): Session? {
        val email = store.getString(KEY_SESSION_EMAIL) ?: return null
        return readAccounts().firstOrNull { it.email == email }?.let { sessionFor(it.profile) }
    }

    private fun sessionFor(profile: UserProfile) =
        Session(profile, if (profile.email in adminEmails) Role.ADMIN else Role.USER)

    private fun readAccounts(): List<StoredAccount> {
        val json = store.getString(KEY_ACCOUNTS) ?: return emptyList()
        return gson.fromJson<List<StoredAccount>>(json, accountsType) ?: emptyList()
    }

    private fun writeAccounts(accounts: List<StoredAccount>) {
        store.putString(KEY_ACCOUNTS, gson.toJson(accounts, accountsType))
    }

    private suspend fun locked(block: suspend () -> AuthResult): AuthResult = withContext(ioDispatcher) {
        mutex.withLock {
            try {
                block()
            } catch (e: RuntimeException) {
                fail(AuthError.STORAGE)
            }
        }
    }

    private fun fail(error: AuthError) = AuthResult.Failure(error)

    companion object {
        private const val KEY_ACCOUNTS = "accounts"
        private const val KEY_SESSION_EMAIL = "session_email"
        private val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

        fun normalize(email: String) = email.trim().lowercase()
        fun isValidEmail(email: String) = EMAIL.matches(email)

        /** Parses the comma-separated `pulseAdminEmails` build property. */
        fun parseAdminEmails(csv: String): Set<String> =
            csv.split(',').map { normalize(it) }.filter { it.isNotEmpty() }.toSet()
    }
}
