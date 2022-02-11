package com.mamiglia.utils;

import com.mamiglia.settings.Destination;
import com.mamiglia.settings.Settings;
import com.mamiglia.settings.Source;
import com.mamiglia.wallpaper.Wallpaper;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GetNewWallpaper implements Runnable {
	// This Functor is the main class that runs all the other needed in a wallpaper selection and download
	private boolean executed = false;
	private static final Logger log = DisplayLogger.getInstance("Get New Wallpaper");
	public static final Wallpaper ERROR_VALUE = null;
	private final Set<Source> src;
	private final Destination dest;

	private Wallpaper result;

	public GetNewWallpaper(Set<Source> src, Destination dest) {
		this.src = src;
		this.dest = dest;
	}


	@Override
	public void run() {
		if (executed) return;
		executed = true;

		Searcher s = new Searcher(src);
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
			selector = new Selector(wallpapers, Settings.INSTANCE.getKeepWallpapers(), Settings.INSTANCE.getMaxDatabaseSize());
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
