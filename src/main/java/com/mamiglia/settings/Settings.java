package com.mamiglia.settings;

import com.mamiglia.utils.DisplayLogger;

import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Settings {
	//Singleton
	private static Settings uniqueInstance;
	public static final String PATH_TO_SAVEFILE = "utility" + File.separator + "settings.txt";
	public static final String PATH_TO_DATABASE = "utility" + File.separator + "database";
	private final File settingFile = new File(PATH_TO_SAVEFILE);
	public boolean keepBlacklist = false;
	private String[] titles = {};
	private String[] flair = {};
	private String[] subreddits = {"wallpaper", "wallpapers"};
	private SEARCH_BY searchBy = SEARCH_BY.HOT;
	private NSFW_LEVEL nsfwLevel = NSFW_LEVEL.ALLOW;
	private int height = 1080;
	private int width = 1920;
	private int period = 15; //mins
	private int minScore = 15;
	private TIME maxOldness = TIME.DAY;
	private int maxDatabaseSize = 50;
	private final Set<String> bannedList;
	private boolean keepWallpapers = false; //keep wallpapers after eliminating them from db?
	private final boolean diffWallpapers = false; //Different wallpaper per screen?
	private static String wallpaperPath = "Saved-Wallpapers"; // path to wallpaper folder
	private RATIO_LIMIT ratioLimit = RATIO_LIMIT.RELAXED;
	private static final Logger log = DisplayLogger.getInstance("Settings");
	private static final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	private static int screens; // Number of physical screens connected to the system

	/* selects leading or trailing spaces on strings, or double (or greater) spaces between words (used in GUI and Searcher)
		\\s+:	Select all white spaces that are:	1) Preceeded by a comma OR start of a line AND (?<=,|\A)
													2) Followed by alphanumeric characters followed by a word start/end (?=[\w]+\b)
			OR	Select all white spaces that are:	1) Preceeded by the end of a word AND (?<=\b)
													2) Followed by a comma OR the end of a line (?=,|\Z)
			OR	Select all white spaces that are:	1) Preceeded by a word end then one space AND (?<=\b )
													2) Followed by a word start (?=\b)
	 */
	private static final String REG_WS = "((?<=,|\\A)\\s+(?=[\\w]+\\b)|(?<=\\b)\\s+(?=,|\\Z)|(?<=\\b )\\s+(?=\\b))";
	private static final String REG_SB = "[\\[\\]]"; // For removing square brackets

	public static void eraseDB() {
		File dbFile = new File(PATH_TO_DATABASE + ".mv.db");
		if (dbFile.exists()) {
			if (dbFile.delete()) {
				log.log(Level.WARNING, () -> "Database has been deleted.");
			} else {
				log.log(Level.WARNING, () -> "Database remains.");
			}
		} else {
			log.log(Level.WARNING, () -> "Could not find database file.");
		}
	}

	public enum TIME {
		HOUR("hour"),
		DAY("day"),
		WEEK("week"),
		MONTH("month"),
		YEAR("year"),
		ALL("all");

		public final String value;

		TIME(String value) {
			this.value = value;
		}
	}

	public enum NSFW_LEVEL {
		NEVER(-1, "&nsfw=no"),
		ALLOW(0, "&include_over_18=true&nsfw=yes"),
		ONLY(1, "&include_over_18=true&nsfw=yes");

		public final int value;
		public final String query;

		NSFW_LEVEL(int value, String query) {
			this.value = value;
			this.query = query;
		}

		public static NSFW_LEVEL valueOf(Integer i) {
			switch (i) {
				case -1:
					return NEVER;
				case 0:
					return ALLOW;
				case 1:
					return ONLY;
				default:
					return ALLOW; //If not recongnized it will be considered as ALLOW
			}
		}
	}

	public enum SEARCH_BY {
		TOP("top"),
		NEW("new"),
		HOT("hot"),
		RELEVANCE("relevance");

		public final String value;

		SEARCH_BY(String value) {
			this.value = value;
		}
	}

	public enum RATIO_LIMIT {
		RELAXED("relaxed"),
		STRICT("strict"),
		NONE("none");

		public final String value;
		RATIO_LIMIT(String value) {
			this.value = value;
		}
	}

	private Settings() {
		if (!settingFile.exists()) {
			log.log(Level.WARNING, "No settings file is found, generating a new stock one");
			settingFile.getParentFile().mkdirs();
			try {
				if (settingFile.createNewFile()) {
					log.log(Level.FINE, "Success!");
				}
				writeSettings();
				Files.setLastModifiedTime(settingFile.toPath(), FileTime.fromMillis(0));
			} catch (IOException e) {
				log.log(Level.SEVERE, "I/O error: Can't create settings file");
			}

		}
		bannedList = new HashSet<>();
		try {screens = ge.getScreenDevices().length;}
		catch (HeadlessException e) {
			log.log(Level.WARNING, "Could not get screens: " + e.getMessage());
		}
	}

	public static synchronized Settings getInstance() {
		if (uniqueInstance == null) {
			log.setLevel(Level.WARNING);
			uniqueInstance = new Settings();
		}
		return uniqueInstance;
	}

	public void readSettings() {
		try (Scanner scan = new Scanner(settingFile)) {
			while (scan.hasNext()) {
				String[] s = scan.nextLine().split("=");
				boolean b = setProperty(s[0], s[1]);
				if (!b) {
					log.log(Level.WARNING, "Property not recognized: {0}", s[0]);
				} else {
					log.log(Level.FINE, "Set property: {0}",  s[0]);
				}
			}
		} catch (FileNotFoundException e) {
			log.log(Level.WARNING, "I/O error: Can't create settings file");

		}
	}

	public void writeSettings() {
		try (FileWriter wr = new FileWriter(PATH_TO_SAVEFILE)) {
			wr.write(this.toString());
			wr.flush();
		} catch (IOException e) {
			log.log(Level.SEVERE, "Couldn't save the file");
			e.printStackTrace();
		}
		log.log(Level.INFO, "Saved settings");
	}

	public String[] getTitles() {
		return titles;
	}

	public void setTitles(String[] titles) {
		this.titles = titles;
	}

	public String[] getFlair() {
		return flair;
	}

	public void setFlair(String[] flair) {
		this.flair = flair;
	}

	public String[] getSubreddits() {
		return subreddits;
	}

	public void setSubreddits(String[] subreddits) {
		this.subreddits = subreddits;
	}

	public SEARCH_BY getSearchBy() {
		return searchBy;
	}

	public void setSearchBy(SEARCH_BY searchBy) {
		this.searchBy = searchBy;
	}

	public NSFW_LEVEL getNsfwLevel() {
		return nsfwLevel;
	}

	public void setNsfwLevel(int level) {
		this.nsfwLevel = NSFW_LEVEL.valueOf(level);
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getPeriod() {
		//DEPRECATED
		return period;
	}

	public void setPeriod(int period) {
		//DEPRECATED
		this.period = period;
	}

	public int getScore() {
		return minScore;
	}

	public void setScore(int score) {
		this.minScore = score;
	}

	public TIME getMaxOldness() {
		return maxOldness;
	}

	public void setMaxOldness(TIME maxOldness) {
		this.maxOldness = maxOldness;
	}

	public int getMaxDatabaseSize() {
		return maxDatabaseSize;
	}

	public void setMaxDatabaseSize(int maxDatabaseSize) {
		this.maxDatabaseSize = maxDatabaseSize;
	}

	public static String getWallpaperPath() {
		return wallpaperPath;
	}

	public void setWallpaperPath(String path) {
		wallpaperPath = path;
	}

	public void setKeepWallpapers(boolean keepWallpapers) {
		this.keepWallpapers = keepWallpapers;
	}

	public boolean getKeepWallpapers() {
		return keepWallpapers;
	}

	public void setKeepBlacklist(boolean keepBlacklist) {
		this.keepBlacklist = keepBlacklist;
	}

	public boolean getKeepBlacklist() {
		return keepBlacklist;
	}

	public boolean getDiffWallpapers() {
		return diffWallpapers;
	}

	public long getLastTimeWallpaperChanged(int i) {
		// The settings file is updated each time a wallpaper is changed
		// TODO change behaviour to save timestamp every time a wallpaper is changed
		return settingFile.lastModified();
	}

	public Object getRatioLimit() {
		return ratioLimit;
	}

	public void setRatioLimit(RATIO_LIMIT ratioLimit) {
		this.ratioLimit = ratioLimit;
	}

	public String getRegWS() {
		return REG_WS;
	}

	public String getRegSB() {
		return REG_SB;
	}
	
	public int getScreens() {
		try {screens = ge.getScreenDevices().length;}// update the number of screens just in case a screen has been unplugged/replugged
		catch (HeadlessException e) {
			log.log(Level.WARNING, "Could not check screens: " + e.getMessage());
		}
		return screens;
	}

	public Long getTimerForMonitor(int i) {
		// returns timer time for monitor #i
		return 0L;
	}


	public void updateDate(int idx) {
		// TODO change to something that saves every wallpaper change
		try {
			Files.setLastModifiedTime(settingFile.toPath(), FileTime.fromMillis(System.currentTimeMillis()));
		} catch (IOException e) {
			log.log(Level.WARNING, "Can't update settings file");
		}
	}

	public boolean isBanned(String id) {
		return bannedList.contains(id);
	}

	public void addBanned(String id) {
		bannedList.add(id);
	}

	@Override
	public String toString() {
		return "titles=" + Arrays.toString(titles) +
				"\nsubreddits=" + Arrays.toString(subreddits) +
				"\nflair=" + Arrays.toString(flair) +
				"\nsearchBy=" + searchBy +
				"\nnsfwLevel=" + nsfwLevel.value +
				"\nheight=" + height +
				"\nwidth=" + width +
				"\nratioLimit=" + ratioLimit +
				"\nperiod=" + period +
				"\nscore=" + minScore +
				"\nmaxOldness=" + maxOldness +
				"\nmaxDatabaseSize=" + maxDatabaseSize +
				"\nkeepWallpapers=" + keepWallpapers +
				"\nkeepBlacklist=" + keepBlacklist +
				"\nwallpaperPath=" + wallpaperPath;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Settings settings = (Settings) o;
		return nsfwLevel == settings.nsfwLevel
				&& height == settings.height
				&& width == settings.width
				&& period == settings.period
				&& minScore == settings.minScore
				&& maxOldness == settings.maxOldness
				&& ratioLimit.equals(settings.ratioLimit)
				&& Arrays.equals(titles, settings.titles)
				&& Arrays.equals(subreddits, settings.subreddits)
				&& Arrays.equals(flair, settings.flair)
				&& searchBy == settings.searchBy;
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(searchBy, nsfwLevel, height, width, period, maxOldness, minScore, ratioLimit);
		result = 31 * result + Arrays.hashCode(titles);
		result = 31 * result + Arrays.hashCode(subreddits);
		result = 31 * result + Arrays.hashCode(flair);
		return result;
	}

	public boolean setProperty(String property, String value) {
		String[] split = value.replaceAll(REG_SB, "").split(", ");
		switch (property) {
			case "titles":
				titles = split;
				break;
			case "subreddits":
				subreddits = split;
				break;
			case "flair":
				flair = split;
				break;
			case "searchBy":
				searchBy = SEARCH_BY.valueOf(value);
				break;
			case "nsfwLevel":
				setNsfwLevel(Integer.parseInt(value));
				break;
			case "height":
				height = Integer.parseInt(value);
				break;
			case "width":
				width = Integer.parseInt(value);
				break;
			case "score":
				minScore = Integer.parseInt(value);
				break;
			case "period":
				period = Integer.parseInt(value);
				break;
			case "ratioLimit":
				ratioLimit = RATIO_LIMIT.valueOf(value);
				break;
			case "maxOldness":
				maxOldness = TIME.valueOf(value);
				break;
			case "maxDatabaseSize":
				maxDatabaseSize = Integer.parseInt(value);
				break;
			case "keepWallpapers":
				keepWallpapers = Boolean.parseBoolean(value);
				break;
			case "keepBlacklist":
				keepBlacklist = Boolean.parseBoolean(value);
				break;
			case "wallpaperPath":
				wallpaperPath = split[0];
				break;

			default:
				return false;
		}
		return true;
	}
}
