package com.mamiglia.settings

import com.mamiglia.wallpaper.Wallpaper
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.awt.GraphicsEnvironment
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

@Serializable
data class Destination(
    val height: Int = 1920,
    val width: Int = 1080,
    val period: Int = 15, //mins
    val screens: Set<Int> = setOf(1),
    val ratioLimit: RATIO_LIMIT = RATIO_LIMIT.RELAXED,
    private var lastChange: Long = 0,  // TODO Should I use Date, Instant or some other time-specific class instead of millis from epoch?
    var name :String = screens.toString(), // TODO set a better name (display name?)
    @Transient var current: Wallpaper? = null
) {
    val residualTime :Long
        get() = Instant.now().toEpochMilli() - lastChange

    val isTimeElapsed :Boolean
        get() = residualTime < period * MIN_TO_MILLIS

    fun updateLastChange() {
        lastChange = Instant.now().toEpochMilli()
    }

}

