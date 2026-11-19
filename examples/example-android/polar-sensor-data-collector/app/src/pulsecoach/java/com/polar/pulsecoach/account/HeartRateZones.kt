package com.polar.pulsecoach.account

import kotlin.math.roundToInt

/** One HR zone. [highBpm] is null for Zone 5 (open-ended: "171+"). */
data class HeartRateZone(val number: Int, val lowBpm: Int, val highBpm: Int?) {
    val label: String get() = if (highBpm == null) "Z$number · $lowBpm+" else "Z$number · $lowBpm–$highBpm"
}

object HeartRateZones {
    /** Lower bounds of zones 1–5 as a share of max HR. */
    private val LOWER_BOUNDS = listOf(0.5, 0.6, 0.7, 0.8, 0.9)

    /** Five zones at 50/60/70/80/90 % of max HR, e.g. max 190 → Z1 95–113 … Z5 171+. */
    fun fromMaxHr(maxHr: Int): List<HeartRateZone> {
        require(maxHr > 0) { "maxHr must be positive" }
        val lows = LOWER_BOUNDS.map { (maxHr * it).roundToInt() }
        return lows.mapIndexed { i, low ->
            HeartRateZone(number = i + 1, lowBpm = low, highBpm = lows.getOrNull(i + 1)?.minus(1))
        }
    }

    /** Zone number (1–5) for [bpm], or 0 when below Zone 1. */
    fun zoneOf(bpm: Int, zones: List<HeartRateZone>): Int =
        zones.lastOrNull { bpm >= it.lowBpm }?.number ?: 0
}
