package com.polar.pulsecoach.account

import com.polar.pulsecoach.ui.profile.ProfileField
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AccountModelTest {

    @Test
    fun zonesFromMaxHr190MatchDesign() {
        val zones = HeartRateZones.fromMaxHr(190)
        assertEquals(
            listOf("Z1 · 95–113", "Z2 · 114–132", "Z3 · 133–151", "Z4 · 152–170", "Z5 · 171+"),
            zones.map { it.label }
        )
    }

    @Test
    fun zoneOfPicksHighestReachedZone() {
        val zones = HeartRateZones.fromMaxHr(190)
        assertEquals(0, HeartRateZones.zoneOf(80, zones))
        assertEquals(1, HeartRateZones.zoneOf(95, zones))
        assertEquals(4, HeartRateZones.zoneOf(160, zones))
        assertEquals(5, HeartRateZones.zoneOf(185, zones))
    }

    @Test
    fun maxHrUsesOverrideElseAgeEstimate() {
        val p = UserProfile(email = "a@b.co", name = "A", age = 30)
        assertEquals(187, p.maxHr) // 208 − 0.7 × 30
        assertEquals(192, p.copy(maxHrOverride = 192).maxHr)
        assertNull(p.copy(age = null).maxHr)
    }

    @Test
    fun bodyCompositionFromProfile() {
        val p = UserProfile(email = "a@b.co", name = "A", heightCm = 180f, weightKg = 78.4f, bodyFatPercent = 18.6f)
        assertEquals(24.2f, p.bmi!!, 0.05f)
        assertEquals(14.58f, p.fatMassKg!!, 0.01f)
        assertEquals(63.82f, p.leanMassKg!!, 0.01f)
    }

    @Test
    fun passwordStrengthScores() {
        assertEquals(0, PasswordStrength.score("abc"))
        assertEquals(1, PasswordStrength.score("abcdefgh"))
        assertEquals(2, PasswordStrength.score("Abcdefgh"))
        assertEquals(3, PasswordStrength.score("Abcdefg1"))
        assertEquals(4, PasswordStrength.score("Abcdefg1!"))
        assertEquals("Strength: Strong", PasswordStrength.label("Abcdefg1!"))
    }

    @Test
    fun profileFieldValidation() {
        assertEquals("Required", ProfileField.HEIGHT.validate(""))
        assertNull(ProfileField.BODY_FAT.validate(""))
        assertEquals("Enter 100–250 cm", ProfileField.HEIGHT.validate("90"))
        assertNull(ProfileField.WEIGHT.validate("78,4"))
        assertEquals(78.4, ProfileField.WEIGHT.parse("78,4")!!, 0.001)
    }
}
