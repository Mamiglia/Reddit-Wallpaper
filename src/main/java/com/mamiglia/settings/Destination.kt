package com.mamiglia.settings

import com.mamiglia.wallpaper.Wallpaper
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.awt.GraphicsDevice
import java.time.Instant

@Serializable
data class Destination(
    var height: Int = 1080,
    var width: Int = 1920,
    var period: Int = 15, //mins
    var screens: Set<Int> = setOf(0),
    var ratioLimit: RATIO_LIMIT = RATIO_LIMIT.RELAXED,
    private var lastChange: Long = 0,  // TODO Should I use Date, Instant or some other time-specific class instead of millis from epoch?
    var sourcesId : MutableSet<String> = mutableSetOf(),
    @Transient var current: Wallpaper? = null
) {
    var residualTime :Long
        get() = - Instant.now().toEpochMilli() + lastChange + period*MIN_TO_MILLIS
        set(value) {
            lastChange = if (value>0)
                Instant.now().toEpochMilli() + value- period*MIN_TO_MILLIS
            else
                Long.MAX_VALUE
        }


    val isTimeElapsed :Boolean
        get() = residualTime < 1 * MIN_TO_MILLIS

    val ratio : Double
        get() = width.toDouble() / height

    val isLandscape : Boolean
        get() = ratio > 1

    fun updateLastChange() {
        lastChange = Instant.now().toEpochMilli()
    }

    fun updateNext() {
        // sets this destination as to be updated in the next update
        lastChange = 0L
    }

    fun addSource(src: Source) {
        if (Settings.sources.keys.contains(src.id))
            sourcesId.add(src.id)
    }

    fun removeSource(src: Source) {
        sourcesId.remove(src.id)
    }

    fun getSources(): Set<Source>{
        return HashSet<Source>(
            Settings.sources.filterKeys { sourcesId.contains(it) }
                .values)
    }

    var name :String = ""
        get() = if (field == "") "${screens.map{ monitorName( Settings.monitors[it] )}}" else field

    fun checkSize(wp: Wallpaper): Boolean {
        return when (ratioLimit) {
            RATIO_LIMIT.NONE -> height < wp.height && width < wp.width
            RATIO_LIMIT.RELAXED -> isLandscape == wp.isLandscape && height < wp.height && width < wp.width
            RATIO_LIMIT.STRICT -> ratio == wp.ratio && height < wp.height && width < wp.width
        }
    }

    fun optimizeDimension() {
        if (screens.size != 1) return
        val monitor = Settings.monitors[screens.iterator().next()]
        width = monitor.displayMode.width
        height = monitor.displayMode.height

    }

    companion object {
        fun monitorName(g : GraphicsDevice) : String {
            return "${g.iDstring[g.iDstring.lastIndex]}_${g.displayMode.width}x${g.displayMode.height}"
        }

        fun monitorWidth(g : GraphicsDevice) : Int {
            return g.displayMode.width
        }

        fun monitorHeight(g: GraphicsDevice) : Int {
            return g.displayMode.height
        }
    }
}



