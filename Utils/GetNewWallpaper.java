package Utils;

import Settings.Settings;
import Wallpaper.Wallpaper;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GetNewWallpaper implements Runnable {
	private boolean executed = false;
	private static final Logger log = DisplayLogger.getInstance("Get New Wallpaper");
	private final Settings settings;

	private Wallpaper result;

	public GetNewWallpaper(Settings settings) {
		this.settings = settings;
	}


	@Override
	public void run() {
		if (executed) return;
		executed = true;

		Searcher s = new Searcher(settings);
		s.generateSearchQuery();
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
			selector = new Selector(wallpapers, settings.doKeepWallpapers(), settings.getMaxDatabaseSize());
		} catch (IOException e) {
			//TODO bad practice
			e.printStackTrace();
		}
		w = selector.select();
		try {
			w.download();
		} catch (IOException | NullPointerException e) {
			log.log(Level.WARNING, "Couldn't download the image and/or update the database");
			result = null;
			return;
		}
		result = w;
	}

	public Wallpaper getResult() {
		return result;
	}
}
