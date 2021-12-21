package Utils;

import Settings.Settings;
import Wallpaper.Wallpaper;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

class Selector implements Runnable{
    private final int maxDbSize;
    private final boolean keepWallpapers;
    private static final String dbUrl = "jdbc:h2:file:" + System.getProperty("user.dir")
            + File.separator + Settings.PATH_TO_DATABASE;
    private static final Logger log = DisplayLogger.getInstance("Selector");
    private Connection conn = null;
    private Statement db = null;
    private boolean executed = false;
    private Wallpaper result = null;
    private Set<Wallpaper> results = new HashSet<>();
    private final Settings settings = Settings.getInstance();
    private final Set<Wallpaper> proposal;
    private int screens;
    private boolean diff;

    public Selector(Set<Wallpaper> proposal, boolean keepWallpapers, int maxDbSize, int screens, boolean diff) throws IOException {
        this.proposal = proposal;
        this.keepWallpapers = keepWallpapers;
        this.maxDbSize = maxDbSize;
        this.screens = screens;
        this.diff = diff;

        loadDB();

        if (conn == null) {
            throw new IOException();
        }
    }

    @Override
    public void run() {
        if (executed || proposal == null) return;
        executed = true;
        List<String> oldID = getOldWallpapersID();

        for (Wallpaper propWallpaper : proposal) {
            if (!oldID.contains(propWallpaper.getID())) {
                if (settings.isBanned(propWallpaper.getID())) {
                    // if banned the wallpaper must not be considered
                    removeWp(propWallpaper.getID());
                    continue;
                }
                log.log(Level.FINE, "Selected new wallpaper from those proposed");
                insertDB(propWallpaper);
                if (screens == 1 || !diff) {
                    closeDB();
                    result = propWallpaper;
                    return;
                } else if (results == null || results.size() < screens){
                    results.add(propWallpaper);
                    continue;
                } else {
					closeDB();
					return;
                }
            }
        }
		
        // OR Not enough unused wallpapers are found //select oldest used wallpapers in the list
        if (proposal.isEmpty()) {
            log.log(Level.WARNING, "Not enough new wallpapers were proposed, setting from recent wallpapers. Maybe your query is too restrictive?");
        } else {
            log.log(Level.INFO, "No unused wallpapers were found, setting from the oldest of those found");
        }

        while (true) {
            try (ResultSet rs = db.executeQuery("SELECT wp FROM WALLPAPERS ORDER BY date LIMIT 1")) {
                rs.next();
				result = (Wallpaper) rs.getObject("wp");
				if (result == null || results.size() == screens) break;
				else if (!settings.isBanned(result.getID())) {
					if (screens == 1) {
						updateDate(result);
						break;
					} else if (results.size() < screens) {
						results.add(result);
						updateDate(result);
						continue;
					} else break;
				}
                removeWp(result.getID());
            } catch (SQLException throwables) {
                log.log(Level.WARNING, "DB Query error in select()");
                log.log(Level.FINEST, throwables.getMessage());
            }
        }

		if (results.size() != screens && screens > 1) {
            log.log(Level.WARNING, "Not enough images available to set one per screen.");
		} else if (result == null) {
            log.log(Level.WARNING, "Database is void, no wallpaper can be set.");
		}
        closeDB();
    }

    public Wallpaper getResult() {
        if (!executed) {
            log.log(Level.INFO, "Result was requested but the functor was never executed");
		} else if (result == null) {
            log.log(Level.INFO, "Selector didn't select any wallpapers");
        }
        return result;
    }
	
	public Set<Wallpaper> getResult(int screens) {
		if (!executed) {
            log.log(Level.INFO, "Result was requested but the functor was never executed.");
		} else if (results == null || results.size() == 0) {
            log.log(Level.INFO, "Selector didn't select any wallpapers.");
        } else if (results.size() < screens) {
			log.log(Level.INFO, "Selector didn't find enough images for your screen.");
		}
        return results;
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

                    if (new File(wp.getPath().toAbsolutePath().toString()).delete()) {
                        log.log(Level.FINE, () -> "Success!");
                    } else {
                        log.log(Level.FINE, () -> "Something went wrong...");
                    }
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
            log.log(Level.FINER, () -> "Successfully inserted entry:\n" + wp);
            cleanDB();

        } catch (SQLException e) {
            log.log(Level.WARNING, () -> "Failed to insert entry in db: " + e.getMessage());
        }
    }

    private void updateDate(Wallpaper wp) {
        try {
            db.executeUpdate("UPDATE WALLPAPERS SET date=CURRENT_TIMESTAMP() WHERE id='" + wp.getID() + "'");
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
            log.log(Level.SEVERE, e.getMessage());
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage());
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

    public void removeWp(String id) {
        try (PreparedStatement p = conn.prepareStatement("DELETE FROM WALLPAPERS where id=?")) {
            p.setString(1, id);
            p.executeUpdate();
            log.log(Level.FINER, () -> "Successfully deleted entry:\n" + id);
        } catch (SQLException e) {
            log.log(Level.WARNING, () -> "Failed to insert entry in db: " + e.getMessage());
        }
    }

    public boolean isDBloaded() {
        return conn != null;
    }
}
