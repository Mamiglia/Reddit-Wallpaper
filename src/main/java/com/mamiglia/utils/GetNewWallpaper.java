package com.mamiglia.utils;

import com.mamiglia.settings.Settings;
import com.mamiglia.wallpaper.Wallpaper;

import java.io.IOException;
import java.util.Set;
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
		Set<Wallpaper> wallpapers = null;
		try {
			wallpapers = s.getSearchResults();
		} catch (IOException e) {
			log.log(Level.WARNING, "Couldn't download the object, Internet error or Invalid input");
		}

		//SELECTOR
		Selector selector;

		try {
			selector = new Selector(wallpapers, settings.getKeepWallpapers(), settings.getMaxDatabaseSize());
		} catch (IOException e) {
			log.log(Level.SEVERE, "Loading DB is impossible. Aborting wallpaper set up");
			abort();
			return;
		}

		selector.run();
		result = selector.getResult();
		if (result != null) {
			if (result.isDownloaded()) {
				try {
					result.download();
				} catch (IOException e) {
					log.log(Level.SEVERE, "Couldn't download the image");
					log.log(Level.FINER, e.getMessage());
				}
			}
			log.log(Level.FINER, () -> "GetNewWallpaper selected:\n" + result);
		}
		if (result == null) {
			log.log(Level.SEVERE, "The selection process found no wallpaper");
			abort();
		}
	}

	private void abort() {
		log.log(Level.INFO, "Something went wrong: couldn't download or select a new wallpaper");
		result = ERROR_VALUE;
	}

	public Wallpaper getResult() {
		if (!executed) {
			log.log(Level.INFO, "Result was requested but the functor was never executed");
		} else if (result == null) {
			log.log(Level.INFO, "No wallpaper was selected.");
		}
		return result;
	}
}
