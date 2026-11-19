package com.polar.pulsecoach.account

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalAuthRepositoryTest {

    private class MemoryStore : KeyValueStore {
        val values = mutableMapOf<String, String>()
        override fun getString(key: String) = values[key]
        override fun putString(key: String, value: String?) {
            if (value == null) values.remove(key) else values[key] = value
        }
    }

    private val store = MemoryStore()
    // Few iterations keep the tests fast; production uses the default.
    private fun repo(admins: Set<String> = emptySet()) =
        LocalAuthRepository(store, admins, Dispatchers.Unconfined, PasswordHasher(iterations = 1_000))

    @Test
    fun registerStartsSessionAsUser() = runBlocking {
        val repo = repo()
        assertEquals(AuthResult.Success, repo.register("Ana", " Ana@Example.com ", "password1", coachDataConsent = true))
        val session = repo.session.value!!
        assertEquals("ana@example.com", session.profile.email)
        assertEquals(Role.USER, session.role)
        assertTrue(session.profile.coachDataConsent)
        assertFalse(session.profile.profileCompleted)
    }

    @Test
    fun registerValidatesInput() = runBlocking {
        val repo = repo()
        assertEquals(AuthResult.Failure(AuthError.NAME_REQUIRED), repo.register(" ", "a@b.co", "password1", false))
        assertEquals(AuthResult.Failure(AuthError.INVALID_EMAIL), repo.register("A", "not-an-email", "password1", false))
        assertEquals(AuthResult.Failure(AuthError.WEAK_PASSWORD), repo.register("A", "a@b.co", "short", false))
        repo.register("A", "a@b.co", "password1", false)
        assertEquals(AuthResult.Failure(AuthError.EMAIL_TAKEN), repo.register("B", "A@B.co", "password2", false))
    }

    @Test
    fun passwordIsNotStoredInPlainText() = runBlocking {
        repo().register("A", "a@b.co", "password1", false)
        assertFalse(store.values.values.any { it.contains("password1") })
    }

    @Test
    fun loginChecksPasswordAndRestoresSession() = runBlocking {
        repo().register("A", "a@b.co", "password1", false)
        val repo = repo()
        assertNotEquals(null, repo.session.value) // session survives "restart"
        repo.logout()
        assertNull(repo.session.value)
        assertEquals(AuthResult.Failure(AuthError.WRONG_CREDENTIALS), repo.login("a@b.co", "wrong-pass"))
        assertEquals(AuthResult.Failure(AuthError.WRONG_CREDENTIALS), repo.login("nobody@b.co", "password1"))
        assertEquals(AuthResult.Success, repo.login("A@B.CO", "password1"))
        assertEquals("a@b.co", repo.session.value!!.profile.email)
    }

    @Test
    fun adminRoleComesFromAdminEmails() = runBlocking {
        val repo = repo(LocalAuthRepository.parseAdminEmails(" Boss@b.co , "))
        repo.register("Boss", "boss@b.co", "password1", false)
        val session = repo.session.value!!
        assertTrue(session.isAdmin)
        assertFalse(session.rawDataAccess) // off until the admin turns it on
        repo.updateProfile(session.profile.copy(developerToolsEnabled = true))
        assertTrue(repo.session.value!!.rawDataAccess)
    }

    @Test
    fun nonAdminNeverGetsRawDataAccess() = runBlocking {
        val repo = repo(setOf("boss@b.co"))
        repo.register("A", "a@b.co", "password1", false)
        repo.updateProfile(repo.session.value!!.profile.copy(developerToolsEnabled = true))
        assertFalse(repo.session.value!!.rawDataAccess)
    }

    @Test
    fun updateProfileKeepsEmailAndPersists() = runBlocking {
        val repo = repo()
        repo.register("A", "a@b.co", "password1", false)
        val p = repo.session.value!!.profile
        repo.updateProfile(p.copy(email = "hacker@b.co", heightCm = 180f, weightKg = 78f, age = 30, profileCompleted = true))
        val restored = repo().session.value!!.profile
        assertEquals("a@b.co", restored.email)
        assertEquals(180f, restored.heightCm)
        assertTrue(restored.profileCompleted)
    }
}
