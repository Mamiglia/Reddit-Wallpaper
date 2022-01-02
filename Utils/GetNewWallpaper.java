package Utils;

import Settings.Settings;
import Wallpaper.Wallpaper;

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
	private final int screens;
	private final boolean diff;

	private Wallpaper result;
	private Set<Wallpaper> results;

	public GetNewWallpaper(Settings settings, int screens, boolean diff) {
		this.settings = settings;
		this.screens = screens;
		this.diff = diff;
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
			selector = new Selector(wallpapers, settings.getKeepWallpapers(), settings.getMaxDatabaseSize(), screens, diff);
		} catch (IOException e) {
			log.log(Level.SEVERE, "Loading DB is impossible. Aborting wallpaper set up");
			abort();
			return;
		}

		selector.run();
		if (!diff || screens == 1) {
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
		} else if (screens > 1) {
			results = selector.getResult(screens);
			if (results != null) {
				for (Wallpaper r : results) {
					if (r.isDownloaded()) {
						try {
							r.download();
						} catch (IOException e) {
							log.log(Level.SEVERE, "Couldn't download the image");
							log.log(Level.FINER, e.getMessage());
						}
					}
					log.log(Level.FINER, () -> "GetNewWallpaper selected:\n" + r);
				}
			}
		}
		if (result == null && (screens == 1 || !diff) || (results == null && diff && screens > 1)) {
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

	public Set<Wallpaper> getResult(int i) {
		if (!executed) {
			log.log(Level.INFO, "Result was requested but the functor was never executed");
		} else if (results == null) {
			log.log(Level.INFO, "No wallpapers were selected.");
		} else if (results.size() < i) {
			log.log(Level.INFO, "Not enough wallpapers found for your screens");
		}
		return results;
	}
}
