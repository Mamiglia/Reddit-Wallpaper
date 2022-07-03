package com.mamiglia.db

import com.mamiglia.settings.Settings
import com.mamiglia.settings.Settings.PATH_TO_DATABASE
import com.mamiglia.wallpaper.Wallpaper
import org.slf4j.LoggerFactory
import java.io.File
import java.io.InvalidClassException
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement

private val dbUrl = ("jdbc:h2:file:" + System.getProperty("user.dir")
        + File.separator + PATH_TO_DATABASE)

class WallpaperDAO {
    private val log = LoggerFactory.getLogger("DAO")
    private val conn: Connection = DriverManager.getConnection(dbUrl, "rw", "")
    private val db: Statement = conn.createStatement()
    private val maxDbSize : Int
        get() = Settings.maxDatabaseSize
    private val keepWallpapers : Boolean
        get() = Settings.keepWallpapers

    init {
        this.open()
    }

    private fun open() : WallpaperDAO? {
        try {
            db.execute("CREATE TABLE IF NOT EXISTS WALLPAPERS(id VARCHAR(100) PRIMARY KEY, wp OTHER NOT NULL, date TIMESTAMP NOT NULL)")
            log.debug("Database loaded: {}", dbUrl)
            return this
        } catch (e: SQLException) {
            log.error("Query error: Couldn't create database: \n{}", e.message );
        } catch (e: Exception) {
            log.error(e.message)
        }
        return null
    }

    fun close() {
        try {
            conn.close()
            db.close()
        } catch (throwables: SQLException) {
            log.error("Query error: couldn't close the database connection: \n{}", throwables.message)
            log.trace(throwables.message)
        }
    }

    fun erase() {
        try {
            conn.prepareStatement("DELETE FROM WALLPAPERS")?.use { p ->
                p.executeUpdate()
                log.debug("Successfully erased DB")
            }
        } catch (e: SQLException) {
            log.warn("Failed to insert entry in db: {}", e.message)
        }
    }

    fun removeWp(id: String) {
        try {
            conn.prepareStatement("DELETE FROM WALLPAPERS where id=?")?.use { p ->
                p.setString(1, id)
                p.executeUpdate()
                log.debug("Successfully deleted entry:\n{}", id )
            }
        } catch (e: SQLException) {
            log.warn("Failed to remove entry from db: {}", e.message )
        }
    }

    fun show(): String? {
        try {
            db.executeQuery("SELECT * FROM WALLPAPERS ORDER BY date")?.use { rs ->
                val str = StringBuilder()
                while (rs.next()) {
                    str.append(rs.getString("id"))
                        .append(": ")
                        .append((rs.getObject("wp") as Wallpaper).completeTitle)
                        .append(" ; ")
                        .append(rs.getTimestamp("date"))
                        .append("\n")
                }
                return str.toString()
            }
        } catch (throwables: SQLException) {
            log.warn("Query Error in showDB(): \n{}", throwables.message)
            log.trace(throwables.message)
        }
        return null
    }

    fun insert(wp: Wallpaper) {
        try {
            conn.prepareStatement("INSERT INTO WALLPAPERS VALUES (?, ?, CURRENT_TIMESTAMP())")?.use { p ->
                p.setString(1, wp.id)
                p.setObject(2, wp)
                p.executeUpdate()
                log.debug("Successfully inserted entry:\n{}", wp)
                cleanDB()
            }
        } catch (e: SQLException) {
            log.warn("Failed to insert entry in db: {}", e.message )
        }
    }

    fun updateDate(wp: Wallpaper) {
        try {
            conn.prepareStatement("UPDATE WALLPAPERS SET date=CURRENT_TIMESTAMP() WHERE id=?")?.use {p ->
                p.setString(1, wp.id)
                p.executeUpdate()
            }
        } catch (throwables: SQLException) {
            // consider the case in which the wp isn't in the table
            log.warn("Query Error in updateDate() :\n{}", throwables.message)
        }
    }

    /*
        removes items from Database if it has more than MAX_DB_SIZE elements
     */
    private fun cleanDB() {
        // the database will contain a maximum of MAX_DB_SIZE wallpapers (default N=50)
        // when the db gets bigger than N, the oldest wallpapers are deleted from the database
        // the user will set if he wants to delete also the wallpaper or the database entry only
        if (maxDbSize < 1) return
        try {
            db.executeQuery("SELECT COUNT(*) AS size FROM WALLPAPERS")?.use{ rs ->
                rs.next()
                if (rs.getInt("size") <= maxDbSize) return
            }
            db.executeQuery("SELECT wp FROM WALLPAPERS ORDER BY date FETCH FIRST 20 PERCENT ROWS ONLY")?.use { rs ->
                while (!keepWallpapers && rs.next()) {
                    val wp = rs.getObject("wp") as Wallpaper
                    log.trace(wp.toString())
                    log.debug("Cleaning of DB, removing {}", wp.id)
                    if (File(wp.path.toAbsolutePath().toString()).delete()) {
                        log.debug("Success!")
                    } else {
                        log.debug("Something went wrong...")
                    }
                }
                db.executeUpdate("DELETE FROM WALLPAPERS WHERE id IN (SELECT id FROM WALLPAPERS ORDER BY date fetch FIRST 20 PERCENT rows only)")
            }
        } catch (throwables: SQLException) {
            log.warn("Query Error in cleanDB(): \n {}", throwables.message)
        }

    }

    fun getAllId(): List<String> {
        val arr = ArrayList<String>()
        try {
            // selects all wallpapers from the oldest
            db.executeQuery("SELECT id FROM WALLPAPERS ORDER BY date DESC")?.use { rs ->
                while (rs.next()) {
                    arr.add(rs.getString("id"))
                }
            }
        } catch (throwables: SQLException) {
            log.warn("Query Error in getAllId(): \n{}", throwables.message)
        }
        return arr
    }

    fun getAllWallpapers() : List<Wallpaper> {
        val arr = ArrayList<Wallpaper>()
        try {
            db.executeQuery("SELECT wp FROM WALLPAPERS ORDER BY date ASC")?.use { rs ->
                while (rs.next()) {
                    arr.add(rs.getObject("wp") as Wallpaper)
                }
            }
        } catch (throwables: SQLException) {
            log.warn("Query Error in getAllWallpapers(): \n{}", throwables.message)
            if (throwables.cause!!.javaClass == InvalidClassException::class.java) {
                log.warn("Detected incompatible objects in DB, erasing the entire DB")
                erase()
            }
        }
        return arr
    }

    // I don't think this check is necessary at all
//    init {
//        try {
//            db.executeQuery(
//                "SELECT DATA_TYPE from INFORMATION_SCHEMA.COLUMNS where" +
//                        " table_name = 'WALLPAPERS'"
//            ).use { dataTypes ->  // Pull the data type information
//                while (dataTypes.next()) {
//                    val check = dataTypes.getString(1) // there is only 1 column
//
//                    // List of data types as integers can be found at https://www.geeksforgeeks.org/how-to-get-the-datatype-of-a-column-of-a-table-using-jdbc/
//                    if (!(check == "12" && dataTypes.row == 1 ||  // VARCHAR
//                                check == "1111" && dataTypes.row == 2 ||  // OTHER (Wallpaper)
//                                check == "93" && dataTypes.row == 3)
//                    ) { // TIMESTAMP
//                        Selector.log.log(
//                            Level.WARNING,
//                            "DB data types are incorrect. Please delete database."
//                        )
//                        break
//                    }
//                    Selector.log.log(
//                        Level.FINEST,
//                        check
//                    ) // this will output any data types integer value
//                }
//            }
//        } catch (throwables: SQLException) {
//            Selector.log.warn("SQL error in database check.");
//            Selector.log.trace(throwables.message);
//        }
//    }
}