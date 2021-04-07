package Settings;

import Utils.DisplayLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Settings {
	//Singleton
	private static Settings uniqueInstance = new Settings();
	static final String PATH_TO_SAVEFILE = ".utility/settings.txt";
	private String[] titles = {};
	private String[] subreddits = {"wallpapers"};
	private SEARCH_BY searchBy = SEARCH_BY.HOT;
	private boolean nsfwOnly = false;
	private int height = 1080;
	private int width = 1920;
	private int period = 15; //mins
	private TIME maxOldness = TIME.DAY; //days
	private int maxDatabaseSize = 50;
	private boolean keepWallpapers = false; //keep wallpapers after eliminating them from db?
	private static final Logger log = DisplayLogger.getInstance("Settings");

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

	private Settings() {
	}

	public static synchronized Settings getInstance() {
		log.setLevel(Level.WARNING);
		return uniqueInstance;
	}

	public void readSettings() {
		File settingFile = new File(PATH_TO_SAVEFILE);
		if (!settingFile.exists()) {
			settingFile.getParentFile().mkdirs();
			try {
				settingFile.createNewFile();
			} catch (IOException e) {
				//TODO bad practice
				e.printStackTrace();
			}
			return;
		}

		try (Scanner scan = new Scanner(settingFile)) {
			while (scan.hasNext()) {
				String[] s = scan.nextLine().split("=");
				boolean b = setProperty(s[0], s[1]);
				if (!b) {
					log.log(Level.WARNING, "Property not recognized: " + s[0]);
				} else {
					log.log(Level.FINE, "Set property: " + s[0]);
				}
			}
		} catch (FileNotFoundException e) {
			//TODO add some useful error message?
			e.printStackTrace();
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

	public boolean isNsfwOnly() {
		return nsfwOnly;
	}

	public void setNsfwOnly(boolean nsfwOnly) {
		this.nsfwOnly = nsfwOnly;
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
		return period;
	}

	public void setPeriod(int period) {
		this.period = period;
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

	public boolean doKeepWallpapers() {
		return keepWallpapers;
	}

	@Override
	public String toString() {
		return "titles=" + Arrays.toString(titles) +
				"\nsubreddits=" + Arrays.toString(subreddits) +
				"\nsearchBy=" + searchBy +
				"\nnsfwOnly=" + nsfwOnly +
				"\nheight=" + height +
				"\nwidth=" + width +
				"\nperiod=" + period +
				"\nmaxOldness=" + maxOldness +
				"\nmaxDatabaseSize=" + maxDatabaseSize +
				"\nkeepWallpapers=" + keepWallpapers;
	}

	public void setKeepWallpapers(boolean keepWallpapers) {
		this.keepWallpapers = keepWallpapers;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Settings settings = (Settings) o;
		return nsfwOnly == settings.nsfwOnly && height == settings.height && width == settings.width && period == settings.period && maxOldness == settings.maxOldness && Arrays.equals(titles, settings.titles) && Arrays.equals(subreddits, settings.subreddits) && searchBy == settings.searchBy;
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(searchBy, nsfwOnly, height, width, period, maxOldness);
		result = 31 * result + Arrays.hashCode(titles);
		result = 31 * result + Arrays.hashCode(subreddits);
		return result;
	}

	public boolean setProperty(String property, String value) {
		switch (property) {
			case "titles":
				titles = value.replace("[", "").replace("]","").split(",");
				break;
			case "subreddits":
				subreddits = value.replace("[", "").replace("]","").split(",");
				break;
			case "searchBy":
				searchBy = SEARCH_BY.valueOf(value);
				break;
			case "nsfwOnly":
				nsfwOnly = Boolean.parseBoolean(value);
				break;
			case "height":
				height = Integer.parseInt(value);
				break;
			case "width":
				width = Integer.parseInt(value);
				break;
			case "period":
				period = Integer.parseInt(value);
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

			default:
				return false;
		}
		return true;
	}

}
