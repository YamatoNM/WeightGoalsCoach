package com.polar.pulsecoach.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polar.pulsecoach.account.AuthRepository
import com.polar.pulsecoach.account.AuthResult
import com.polar.pulsecoach.account.HeartRateZone
import com.polar.pulsecoach.account.HeartRateZones
import com.polar.pulsecoach.account.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Profile form. Values are kept as typed text and parsed on save. */
data class ProfileUiState(
    val name: String = "",
    val heightCm: String = "",
    val weightKg: String = "",
    val age: String = "",
    val bodyFatPercent: String = "",
    val maxHr: String = "",
    val errors: Map<ProfileField, String> = emptyMap(),
    val saving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
) {
    /** Max HR shown in the zone preview: typed value, else the estimate from age. */
    val effectiveMaxHr: Int?
        get() = ProfileField.MAX_HR.parseInt(maxHr)
            ?: ProfileField.AGE.parseInt(age)?.let { UserProfile.estimateMaxHr(it) }

    val estimatedMaxHrHint: String
        get() = ProfileField.AGE.parseInt(age)?.let { "Estimated ${UserProfile.estimateMaxHr(it)} from your age" }
            ?: "Leave empty to estimate from your age"

    val zonePreview: List<HeartRateZone> get() = effectiveMaxHr?.let { HeartRateZones.fromMaxHr(it) } ?: emptyList()
}

enum class ProfileField(val min: Double, val max: Double, val required: Boolean, val unit: String) {
    HEIGHT(100.0, 250.0, true, "cm"),
    WEIGHT(30.0, 300.0, true, "kg"),
    AGE(13.0, 100.0, true, "years"),
    BODY_FAT(3.0, 60.0, false, "%"),
    MAX_HR(120.0, 230.0, false, "bpm");

    fun parse(text: String): Double? = text.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it in min..max }
    fun parseInt(text: String): Int? = parse(text)?.toInt()

    /** Error text for [text], or null when it is valid. */
    fun validate(text: String): String? = when {
        text.isBlank() -> if (required) "Required" else null
        parse(text) == null -> "Enter ${fmt(min)}–${fmt(max)} $unit"
        else -> null
    }

    private fun fmt(v: Double) = if (v % 1.0 == 0.0) v.toInt().toString() else v.toString()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(fromProfile(repository.session.value?.profile))
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    fun onName(v: String) = edit { copy(name = v) }
    fun onHeight(v: String) = edit { copy(heightCm = v, errors = errors - ProfileField.HEIGHT) }
    fun onWeight(v: String) = edit { copy(weightKg = v, errors = errors - ProfileField.WEIGHT) }
    fun onAge(v: String) = edit { copy(age = v, errors = errors - ProfileField.AGE) }
    fun onBodyFat(v: String) = edit { copy(bodyFatPercent = v, errors = errors - ProfileField.BODY_FAT) }
    fun onMaxHr(v: String) = edit { copy(maxHr = v, errors = errors - ProfileField.MAX_HR) }

    fun save() {
        val s = _state.value
        val current = repository.session.value?.profile ?: return
        val errors = mapOf(
            ProfileField.HEIGHT to s.heightCm,
            ProfileField.WEIGHT to s.weightKg,
            ProfileField.AGE to s.age,
            ProfileField.BODY_FAT to s.bodyFatPercent,
            ProfileField.MAX_HR to s.maxHr
        ).mapNotNull { (field, text) -> field.validate(text)?.let { field to it } }.toMap()
        if (errors.isNotEmpty() || s.name.isBlank()) {
            _state.update { it.copy(errors = errors, error = if (s.name.isBlank()) "Enter your name." else null) }
            return
        }
        val updated = current.copy(
            name = s.name.trim(),
            heightCm = ProfileField.HEIGHT.parse(s.heightCm)?.toFloat(),
            weightKg = ProfileField.WEIGHT.parse(s.weightKg)?.toFloat(),
            age = ProfileField.AGE.parseInt(s.age),
            bodyFatPercent = ProfileField.BODY_FAT.parse(s.bodyFatPercent)?.toFloat(),
            maxHrOverride = ProfileField.MAX_HR.parseInt(s.maxHr),
            profileCompleted = true
        )
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            val result = repository.updateProfile(updated)
            _state.update {
                it.copy(saving = false, saved = result is AuthResult.Success, error = (result as? AuthResult.Failure)?.error?.message)
            }
        }
    }

    private inline fun edit(crossinline change: ProfileUiState.() -> ProfileUiState) =
        _state.update { it.change().copy(saved = false, error = null) }

    private fun fromProfile(p: UserProfile?) = ProfileUiState(
        name = p?.name.orEmpty(),
        heightCm = p?.heightCm?.let(::trim).orEmpty(),
        weightKg = p?.weightKg?.let(::trim).orEmpty(),
        age = p?.age?.toString().orEmpty(),
        bodyFatPercent = p?.bodyFatPercent?.let(::trim).orEmpty(),
        maxHr = p?.maxHrOverride?.toString().orEmpty()
    )

    private fun trim(v: Float) = if (v % 1f == 0f) v.toInt().toString() else v.toString()
}
