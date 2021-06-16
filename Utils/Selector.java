package Utils;

import Wallpaper.Wallpaper;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

class Selector implements Runnable{
    private final int maxDbSize;
    private final boolean keepWallpapers;
    public static final String PATH_TO_DATABASE = ".utility"+ File.separator + "db";
    private static final String dbUrl = "jdbc:h2:file:" + System.getProperty("user.dir") + File.separator + PATH_TO_DATABASE;
    private static final Logger log = DisplayLogger.getInstance("Selector");
    private Connection conn = null;
    private Statement db = null;
    private boolean executed = false;
    private Wallpaper result = null;
    private final Set<Wallpaper> proposal;

    public Selector(Set<Wallpaper> proposal, boolean keepWallpapers, int maxDbSize) throws IOException {
        this.proposal = proposal;
        this.keepWallpapers = keepWallpapers;
        this.maxDbSize = maxDbSize;

        loadDB();

        if (conn == null) {
            throw new IOException();
        }
    }

    @Override
    public void run() {
        if (executed) return;
        executed = true;
        List<String> oldID = getOldWallpapersID();

        for (Wallpaper propWallpaper : proposal) {
            if (!oldID.contains(propWallpaper.getID())) {
                log.log(Level.FINE, "Selected new wallpaper from those proposed");
                insertDB(propWallpaper);
                closeDB();
                result = propWallpaper;
                return;
            }
        }
        // OR No unused wallpapers are found, select oldest used wallpapers in the list
        if (proposal.isEmpty()) {
            log.log(Level.WARNING, "No new wallpaper is proposed, setting a recent wallpaper. Maybe your query is too restrictive?");
        } else {
            log.log(Level.INFO, "No unused wallpaper is found, setting the oldest from those found");
        }
        try (ResultSet rs = db.executeQuery("SELECT wp FROM WALLPAPERS ORDER BY date LIMIT 1")) {
            rs.next();
            result = (Wallpaper) rs.getObject("wp");
            updateDate(result);
        } catch (SQLException throwables) {
            log.log(Level.WARNING, "Query error in select()");
            log.log(Level.FINEST, throwables.getMessage());
        }

        if (result == null ) {
            log.log(Level.WARNING, "Database is void, no wallpaper can be set");
        }
        closeDB();
    }

    public Wallpaper getResult() {
        if (!executed) {
            log.log(Level.INFO, "Result was requested but the functor was never executed");
        } else if (result == null) {
            log.log(Level.INFO, "Selector didn't select any wallpaper");
        }
        return result;
    }

    public List<String> getOldWallpapersID() {
        ArrayList<String> arr = new ArrayList<>();
        try (ResultSet rs = db.executeQuery("SELECT id FROM WALLPAPERS")) {
             while (rs.next()) {
                 arr.add(rs.getString("id"));
             }
        } catch (SQLException throwables) {
            log.log(Level.WARNING, "Query Error in getOldWallpapersID()");
            log.log(Level.FINEST, throwables.getMessage());
        }
        return arr;
    }

    /*
        removes items from Database if it has more than MAX_DB_SIZE elements
     */
    private void cleanDB() {
        // the database will contain a maximum of MAX_DB_SIZE wallpapers (default N=50)
        // when the db gets bigger then N, the oldest wallpapers are deleted from the database
        // the user will set if he wants to delete also the wallpaper or the database entry only
        if (maxDbSize != -1 && getOldWallpapersID().size() > maxDbSize) {
            try (ResultSet rs = db.executeQuery("SELECT wp FROM WALLPAPERS ORDER BY date FETCH FIRST 20 PERCENT ROWS ONLY")) {
                while (!keepWallpapers && rs.next()) {
                    Wallpaper wp = (Wallpaper) rs.getObject("wp");
                    log.log(Level.FINEST, wp::toString);
                    log.log(Level.FINE, () -> "Cleaning of DB, removing " + wp.getID());

                    new File(wp.getPath()).delete();
                }
                db.executeUpdate("DELETE FROM WALLPAPERS WHERE id IN (SELECT id FROM WALLPAPERS ORDER BY date fetch FIRST 20 PERCENT rows only)");

            } catch (SQLException throwables) {
                log.log(Level.WARNING, "Query Error in cleanDB()");
                log.log(Level.FINEST, throwables.getMessage());
            }
        }
    }

    private void insertDB(Wallpaper wp) {
        try (PreparedStatement p = conn.prepareStatement("INSERT INTO WALLPAPERS VALUES (?, ?, CURRENT_TIMESTAMP())")) {
            p.setString(1, wp.getID());
            p.setObject(2, wp);
            p.executeUpdate();
            log.log(Level.FINER, () -> "Successfully inserted entry:\n" + wp.toString());
            cleanDB();

        } catch (SQLException e) {
            log.log(Level.WARNING, () -> "Failed to insert entry in db: " + e.getMessage());
        }
    }

    private void updateDate(Wallpaper wp) {
        try {
            db.executeUpdate("UPDATE WALLPAPERS SET date=CURRENT_TIMESTAMP() WHERE id=\'"+wp.getID()+"\'");
        } catch (SQLException throwables) {
            // consider the case in which the wp isn't in the table
            log.log(Level.WARNING, "Query Error in updateDate()");
            log.log(Level.FINEST, throwables.getMessage());
        }
    }

    private void loadDB() {
        try {
            conn = DriverManager.getConnection(dbUrl, "rw", "");
            db = conn.createStatement();
            db.execute("CREATE TABLE IF NOT EXISTS WALLPAPERS(id VARCHAR(100) PRIMARY KEY, wp OTHER NOT NULL, date TIMESTAMP NOT NULL)");
            log.log(Level.FINE, "Database loaded: " + dbUrl);

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Query error: Couldn't create database");
            log.log(Level.FINEST, e.getMessage());
        }
    }

    public String showDB() {
        try (ResultSet rs = db.executeQuery("SELECT * FROM WALLPAPERS ORDER BY date")) {
            StringBuilder str = new StringBuilder();
            while (rs.next()) {
                str.append(rs.getString("id"))
                        .append(": ")
                        .append(((Wallpaper) rs.getObject("wp")).getTitle())
                        .append(" ; ")
                        .append(rs.getTimestamp("date"))
                        .append("\n");
            }
            return str.toString();
        } catch (SQLException throwables) {
            log.log(Level.WARNING, "Query Error in showDB()");
            log.log(Level.FINEST, throwables.getMessage());
        }
        return null;

    }

    public void closeDB() {
        try {
            conn.close();
            db.close();

        } catch (SQLException throwables) {
            log.log(Level.SEVERE, "Query error: couldn't close the database connection");
            log.log(Level.FINEST, throwables.getMessage());
        }
    }

    public boolean isDBloaded() {
        return conn != null;
    }
}
