package com.mamiglia.settings

import com.mamiglia.gui.Background
import com.mamiglia.utils.DisplayLogger
import com.mamiglia.wallpaper.Wallpaper
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.awt.GraphicsDevice
import java.awt.GraphicsEnvironment
import java.awt.HeadlessException
import java.io.File
import java.util.logging.Level

private const val DESTS_SAVEFILE = "destinations.json"
private const val SRCS_SAVEFILE = "sources.json"
private const val SETTINGS_SAVEFILE = "settings.json"
const val MIN_TO_MILLIS = 60000L

@Serializable
object Settings {
    val PATH_TO_SAVEFOLDER = "utility" + File.separator + "settings" + File.separator // const?
    val PATH_TO_DATABASE = "utility" + File.separator + "database"
    var wallpaperPath = "Saved-Wallpapers" // path to wallpaper folder

    private val bannedList: MutableSet<String> = mutableSetOf() // the bannedList is kept until the pc is turned off, then it gets resetted
    private val log = DisplayLogger.getInstance("Settings")
    val sources: MutableSet<Source>  = mutableSetOf()
    val dests: MutableList<Destination> = mutableListOf()

    var keepWallpapers = false //keep wallpapers after eliminating them from db?
    var keepBlacklist : Boolean = false
    var maxDatabaseSize = 50
    val monitors: Array<GraphicsDevice>
        get() = try {
            GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices
        } catch (e: HeadlessException) {
            log.log(Level.SEVERE, "Could not get screens: " + e.message)
            arrayOf()
        }
    val monitorsNumber
        get() = GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices.size
    val isSingleDestination: Boolean
        get() = dests.size == 1
    val isSingleSource : Boolean
        get() = sources.size == 1


    init {
        log.log(Level.INFO, "Setting singleton invoked")
        readSettings()
        if (sources.isEmpty()) {
            sources.add(Source()) //add default source
        }
        if (dests.isEmpty()) {
            dests.add(Destination()) // add default destination
        }
    }


    fun writeSettings() {
        File(PATH_TO_SAVEFOLDER + DESTS_SAVEFILE)
            .writeText(Json.encodeToString(dests))
        File(PATH_TO_SAVEFOLDER + SRCS_SAVEFILE)
            .writeText(Json.encodeToString(sources))
        val file = File(PATH_TO_SAVEFOLDER + SETTINGS_SAVEFILE)

        // how to write and save dest X src map?
        file.appendText(Json.encodeToString(keepWallpapers) + '\n')
        file.appendText(Json.encodeToString(keepBlacklist)+ '\n')
        file.appendText(Json.encodeToString(maxDatabaseSize)+ '\n')
        log.log(Level.INFO, "Settings saved")
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
        maxDatabaseSize = Json.decodeFromString(lines[2])
        sources.addAll(Json.decodeFromString(File(PATH_TO_SAVEFOLDER+ SRCS_SAVEFILE).readText()))
        dests.addAll(Json.decodeFromString(File(PATH_TO_SAVEFOLDER+ DESTS_SAVEFILE).readText()))
        log.log(Level.INFO, "Setting read from files")
    }

    fun banWallpaper(wallpaper: Wallpaper) {
        bannedList.add(wallpaper.id)
        if (!keepBlacklist) {
            if (wallpaper.delete()) {
                log.log(Level.INFO, "Banned ${wallpaper.id} image removed.")
            } else {
                log.log(Level.INFO, "Banned ${wallpaper.id} image was not removed.")
            }
        }
    }

    fun newSource() : Source {
        val new = Source()
        sources.add(new)
        return new
    }

    fun newDest() :Destination {
        val new = Destination()
        dests.add(new)
        return new
    }

    fun removeSource(src: Source) {
        sources.remove(src)
        for (dest in dests) {
            dest.sources.remove(src)
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

    //TODO add eraseDB()
}

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
