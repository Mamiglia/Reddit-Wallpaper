package com.mamiglia.settings

import com.mamiglia.wallpaper.Wallpaper
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.awt.GraphicsDevice
import java.time.Instant


@Serializable
data class Destination(
    var height: Int = 1920,
    var width: Int = 1080,
    var period: Int = 15, //mins
    var screens: Set<Int> = setOf(),
    var ratioLimit: RATIO_LIMIT = RATIO_LIMIT.RELAXED,
    private var lastChange: Long = 0,  // TODO Should I use Date, Instant or some other time-specific class instead of millis from epoch?
    val sources : MutableSet<Source> = mutableSetOf(), // TODO mutableSetOf()
    @Transient var current: Wallpaper? = null
) {
    val residualTime :Long
        get() = - Instant.now().toEpochMilli() + lastChange + period* MIN_TO_MILLIS

    val isTimeElapsed :Boolean
        get() = residualTime < period * MIN_TO_MILLIS

    fun updateLastChange() {
        lastChange = Instant.now().toEpochMilli()
    }

    fun updateNext() {
        // sets this destination as to be updated in the next update
        lastChange = 0L
    }

    fun addSource(src: Source) {
        sources.add(src)
    }

    fun removeSource(src: Source) {
        sources.remove(src)
    }

    var name :String = ""
        get() = if (field == "") "${screens.map{ monitorName( Settings.monitors[it] )}}" else field



    companion object {
        fun monitorName(g : GraphicsDevice) : String {
            return "${g.iDstring[g.iDstring.lastIndex]}_${g.displayMode.width}x${g.displayMode.height}"
        }
    }
}



