package Utils;

import Settings.Settings;
import Wallpaper.Wallpaper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GetNewWallpaper implements Runnable {
	// This Functor is the main class that runs all the other needed in a wallpaper selection and download
	private boolean executed = false;
	private static final Logger log = DisplayLogger.getInstance("Get New Wallpaper");
	private final Settings settings;
	public static final Wallpaper ERROR_VALUE = null;

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

		//SELECTOR
		Wallpaper w = null;
		Selector selector = null;

		HashSet<Wallpaper> wp_s = new HashSet<>();
		for (String id: wallpapers.keySet()) {
			wp_s.add(wallpapers.get(id));
		}

		try {
			selector = new Selector(wp_s, settings.doKeepWallpapers(), settings.getMaxDatabaseSize());
		} catch (IOException e) {
			log.log(Level.SEVERE, "Loading DB is impossible. Aborting wallpaper set up");
			abort();
			return;
		}


		w = selector.select();
		try {
			if (!w.isDownloaded())
				w.download();
		} catch (IOException | NullPointerException e) {
			log.log(Level.SEVERE, "Couldn't download the image and/or update the database");
			abort();
			return;
		}
		result = w;
	}

	private void abort() {
		log.log(Level.INFO, "Something went wrong: couldn't download or select a new wallpaper");
		result = null;

	}

	public Wallpaper getResult() {
		return result;
	}
}
