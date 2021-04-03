package Utils;

import Wallpaper.Wallpaper;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GetNewWallpaper implements Runnable {
	public enum SEARCH_BY {TOP, NEW, HOT, RELEVANCE}
	private boolean executed = false;
	private static final Logger log = Logger.getLogger("Get New Wallpaper")

	private final String[] titles;
	private final String[] subreddits;
	private final int width;
	private final int height;
	private final boolean nsfw;
	private final SEARCH_BY searchBy;

	private Wallpaper result;

	public GetNewWallpaper(String[] titles, String[] subreddits, int width, int height, boolean nsfw, SEARCH_BY searchBy) {
		this.titles = titles;
		this.subreddits = subreddits;
		this.width = width;
		this.height = height;
		this.nsfw = nsfw;
		this.searchBy = searchBy;
	}


	@Override
	public void run() {
		if (executed) return;
		executed = true;

		Searcher s = new Searcher(titles, subreddits, width, height, nsfw, searchBy);
		s.generateSearchQuery();
		log.log(Level.INFO, "Search query is: " + s.getSearchQuery());
		Map<String, Wallpaper> wallpapers = null;
		try {
			s.getSearchResults();
			wallpapers = s.getSearchResults();
		} catch (IOException e) {
			log.log(Level.WARNING, "Couldn't download the object, Internet error or Invalid input");
		}

		if (wallpapers == null) {
			log.log(Level.SEVERE, "Wallpaper JSON not found");
		}

		//SELECTOR
		Wallpaper w = null;
		Selector selector = null;
		try {
			selector = new Selector(wallpapers);
		} catch (IOException e) {
			//TODO bad practice
			e.printStackTrace();
		}
		w = selector.select();
		try {
			w.download();
		} catch (IOException e) {
			log.log(Level.WARNING, "Couldn't download the image and/or update the database");
		}
		result = w;
	}

	public Wallpaper getResult() {
		return result;
	}
}
