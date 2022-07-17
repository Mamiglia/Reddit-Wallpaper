package com.mamiglia.utils;

import com.mamiglia.settings.Destination;
import com.mamiglia.settings.Settings;
import com.mamiglia.settings.Source;
import com.mamiglia.wallpaper.Wallpaper;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetNewWallpaper implements Runnable {
	// This Functor is the main class that runs all the other needed in a wallpaper selection and download
	private boolean executed = false;
	private static final Logger log = LoggerFactory.getLogger("Get New Wallpaper");
	public static final Wallpaper ERROR_VALUE = null;
	private final Set<Source> sources;
	private final Destination dest;

	private Wallpaper result;

	public GetNewWallpaper(Set<Source> sources, Destination dest) {
		this.sources = sources;
		this.dest = dest;
	}


	@Override
	public void run() {
		if (executed) return;
		executed = true;

		Searcher s = new Searcher(sources);
		Set<Wallpaper> wallpapers = null;
		try {
			wallpapers = s.getSearchResults();
		} catch (IOException e) {
			log.warn("Couldn't download the object, Internet error or Invalid input");
			log.debug("{}", e.getMessage());
		}

		//SELECTOR
		Selector selector;

		try {
			selector = new Selector(wallpapers, dest);
		} catch (IOException e) {
			log.error("Loading DB is impossible. Aborting wallpaper set up");
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
					log.error("Couldn't download the image");
					log.debug(e.getMessage());
				}
			}
			log.debug("GetNewWallpaper selected:\n {}", result);
		}
		if (result == null) {
			log.error("The selection process found no wallpaper");
			abort();
		}
	}

	private void abort() {
		log.info("Something went wrong: couldn't download or select a new wallpaper");
		result = ERROR_VALUE;
	}

	public Wallpaper getResult() {
		if (!executed) {
			log.info("Result was requested but the functor was never executed");
		} else if (result == null) {
			log.info("No wallpaper was selected.");
		}
		return result;
	}
}
