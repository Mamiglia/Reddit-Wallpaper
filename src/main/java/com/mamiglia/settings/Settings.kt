@file:Suppress("unused")

package com.mamiglia.settings

import com.mamiglia.wallpaper.Wallpaper
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.awt.GraphicsDevice
import java.awt.GraphicsEnvironment
import java.awt.HeadlessException
import java.io.File
import java.nio.file.Files

private const val DESTS_SAVEFILE = "destinations.json"
private const val SRCS_SAVEFILE = "sources.json"
private const val SETTINGS_SAVEFILE = "settings.json"
const val MIN_TO_MILLIS = 60000L
const val HOURS_TO_MILLIS = 60 * MIN_TO_MILLIS

@Serializable
object Settings {
    private val PATH_TO_SAVEFOLDER = System.getProperty("user.dir") + File.separator + "utility" + File.separator + "settings" + File.separator // const?
    val PATH_TO_DATABASE = "utility" + File.separator + "database"
    const val LOG_PATH = "utility/log.txt"

    var wallpaperPath = "Saved-Wallpapers" // path to wallpaper folder

    private val bannedList: MutableSet<String> = mutableSetOf() // the bannedList is kept until the pc is turned off, then it gets resetted
    private val log = LoggerFactory.getLogger("Settings")
    val sources: MutableMap<String,Source>  = mutableMapOf()
    val dests: MutableList<Destination> = mutableListOf()

    var keepWallpapers = false //keep wallpapers after eliminating them from db?
    var keepBlacklist : Boolean = false
    var displayNotification : Boolean = true
    var pinTime : Double = 24.0 // hours
    var maxDatabaseSize = 50
    val monitors: Array<GraphicsDevice>
        get() = try {
            GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices
        } catch (e: HeadlessException) {
            log.error("Could not get screens: " + e.message)
            arrayOf()
        }
    val monitorsNumber
        get() = GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices.size
    val isSingleDestination: Boolean
        get() = dests.size == 1
    val isSingleSource : Boolean
        get() = sources.size == 1


    init {
        log.info("Setting singleton invoked")
        try {
            readSettings()
        } catch (e: Exception) { // I would like to use a more specific exception, but i cannot find JsonDecodingException
            log.warn("Settings file is corrupted, cannot read it, starting with default settings: $e")
        }
        if (sources.isEmpty()) {
            val src = Source()
            sources[src.id] = src //add default source
        }
        if (dests.isEmpty()) {
            dests.add(Destination()) // add default destination
        }
    }


    fun writeSettings() {
        var file = File(PATH_TO_SAVEFOLDER + DESTS_SAVEFILE)
        Files.createDirectories(file.parentFile.toPath())
        file.createNewFile()
        file.writeText(Json.encodeToString(dests))
        file = File(PATH_TO_SAVEFOLDER + SRCS_SAVEFILE)
        file.createNewFile()
        file.writeText(Json.encodeToString(sources))
        file = File(PATH_TO_SAVEFOLDER + SETTINGS_SAVEFILE)
        file.createNewFile()


        file.writeText(Json.encodeToString(keepWallpapers) + '\n')
        file.appendText(Json.encodeToString(keepBlacklist)+ '\n')
        file.appendText(Json.encodeToString(displayNotification)+ '\n')
        file.appendText(Json.encodeToString(maxDatabaseSize)+ '\n')
        log.info("Settings saved")
    }

    fun readSettings() {
        val file = File(PATH_TO_SAVEFOLDER + SETTINGS_SAVEFILE)
        val lines : List<String>
        if (file.exists()) {
            lines = file.readLines()
        } else {
            return
        }
        keepBlacklist = Json.decodeFromString(lines[0]) //TODO find a better way to do this
        keepWallpapers = Json.decodeFromString(lines[1])  //FIX what happens if file is void? if there's no line there?
        displayNotification = Json.decodeFromString(lines[2])  //FIX what happens if file is void? if there's no line there?
        maxDatabaseSize = Json.decodeFromString(lines[3])
        sources.putAll(format.decodeFromString(File(PATH_TO_SAVEFOLDER+ SRCS_SAVEFILE).readText()))
        dests.addAll(format.decodeFromString(File(PATH_TO_SAVEFOLDER+ DESTS_SAVEFILE).readText()))

//        for (d in dests) {
//            d.sources = d.sources.intersect(sources) as MutableSet<Source>
//        }
        log.info("Setting read from files")
    }

    fun banWallpaper(wallpaper: Wallpaper) {
        bannedList.add(wallpaper.id)
        if (!keepBlacklist) {
            if (wallpaper.delete()) {
                log.info("Banned {} image removed.", wallpaper.id)
            } else {
                log.info("Banned {} image was not removed.", wallpaper.id)
            }
        }
    }

    fun isBanned(wallpaper: Wallpaper) :Boolean {
        return bannedList.contains(wallpaper.id)
    }

    fun newSource() : Source {
        val new = Source()
        sources[new.id] = new
        if (dests.size == 1) {
            dests.iterator().next().addSource(new)
        }
        return new
    }

    fun newDest() :Destination {
        val new = Destination()
        dests.add(new)
        return new
    }

    fun removeSource(src: Source) {
        sources.remove(src.id)
        for (dest in dests) {
            dest.sourcesId.remove(src.id)
        }
    }

    fun removeDestination(dest: Destination) {
        dests.remove(dest)
    }

    fun changesAllMonitors(dest: Destination) : Boolean {
        // returns true if this destination changes the wallpaper to all the monitors
        return dest.screens.size == monitorsNumber // TODO is this correct?
        // what happens if the number of monitors changes?
    }
}

val format = Json { ignoreUnknownKeys = true }

enum class TIME(val value: String) {
    HOUR("hour"),
    DAY("day"),
    WEEK("week"),
    MONTH("month"),
    YEAR("year"),
    ALL("all");
}

enum class NSFW_LEVEL(val value: Int, val query: String) {
    NEVER(-1, "&nsfw=no"),
    ALLOW(0, "&include_over_18=true&nsfw=yes"),
    ONLY(1, "&include_over_18=true&nsfw=yes");

    companion object { // TODO is this needed?
        @JvmStatic
        fun valueOf(i: Int): NSFW_LEVEL {
            return when (i) {
                -1 -> NEVER
                0 -> ALLOW
                1 -> ONLY
                else -> ALLOW //If not recongnized it will be considered as ALLOW
            }
        }
    }
}

enum class SEARCH_BY(val value: String) {
    TOP("top"),
    NEW("new"),
    HOT("hot"),
    RELEVANCE("relevance");
}

enum class RATIO_LIMIT(val value: String) {
    RELAXED("relaxed"),
    STRICT("strict"),
    NONE("none");
}

/* selects leading or trailing spaces on strings, or double (or greater) spaces between words (used in GUI and Searcher)
		\\s+:	Select all white spaces that are:	1) Preceeded by a comma OR start of a line AND (?<=,|\A)
													2) Followed by alphanumeric characters followed by a word start/end (?=[\w]+\b)
			OR	Select all white spaces that are:	1) Preceeded by the end of a word AND (?<=\b)
													2) Followed by a comma OR the end of a line (?=,|\Z)
			OR	Select all white spaces that are:	1) Preceeded by a word end then one space AND (?<=\b )
													2) Followed by a word start (?=\b)
	 */
const val REG_WS = "((?<=,|\\A)\\s+(?=[\\w]+\\b)|(?<=\\b)\\s+(?=,|\\Z)|(?<=\\b )\\s+(?=\\b))"
private const val REG_SB = "[\\[\\]]" // For removing square brackets
