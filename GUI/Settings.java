package GUI;

import Utils.GetNewWallpaper.SEARCH_BY;

import java.util.Arrays;
import java.util.Objects;

class Settings {
	private String[] titles = {};
	private String[] subreddits = {"wallpapers"};
	private SEARCH_BY searchBy = SEARCH_BY.HOT;
	private boolean nsfwOnly = false;
	private int height = 1080;
	private int width = 1920;
	private int period = 15; //mins
	private int maxOldness = 24; //hours

	public Settings() {
	}

	public Settings(String[] titles, String[] subreddits, SEARCH_BY searchBy, boolean nsfwOnly, int height, int width, int period, int maxOldness) {
		this.titles = titles;
		this.subreddits = subreddits;
		this.searchBy = searchBy;
		this.nsfwOnly = nsfwOnly;
		this.height = height;
		this.width = width;
		this.period = period;
		this.maxOldness = maxOldness;
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

	public int getMaxOldness() {
		return maxOldness;
	}

	public void setMaxOldness(int maxOldness) {
		this.maxOldness = maxOldness;
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

	public void setProperty(String property, String value) {
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
				maxOldness = Integer.parseInt(value);
				break;
			default:
				System.err.println("Property does not exist");
		}
	}

	@Override
	public String toString() {
		return	"titles=" + Arrays.toString(titles) +
				"\nsubreddits=" + Arrays.toString(subreddits) +
				"\nsearchBy=" + searchBy +
				"\nnsfwOnly=" + nsfwOnly +
				"\nheight=" + height +
				"\nwidth=" + width +
				"\nperiod=" + period +
				"\nmaxOldness=" + maxOldness;
	}
}
