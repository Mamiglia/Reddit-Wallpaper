package GUI;

import Settings.Settings;
import Utils.DisplayLogger;
import Utils.GetNewWallpaper;
import Utils.SetNewWallpaper;
import Wallpaper.Wallpaper;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Background implements Runnable {
	//Singleton class
	// runs in background constantly:
	// 1 - Changes wallpaper
	// 2 - Sleep X time
	// 3 - repeat
	// until program is stopped
	private static final Logger log = DisplayLogger.getInstance("Background service");
	private static final Background uniqueInstance = new Background();
	private final Settings settings = Settings.getInstance();
	private boolean stopped = false;
	private Wallpaper current = null;

	private Background() {
	}

	public static Background getInstance() {
		return uniqueInstance;
	}

	public void stop() {
		stopped = true;
	}

	public void changeWallpaper() {
		GetNewWallpaper g = new GetNewWallpaper(settings);
		Thread t1 = new Thread(g);
		t1.start();
		try {
			t1.join();
		} catch (InterruptedException e) {
			log.log(Level.SEVERE, "Thread GetNewWallpaper was interrupted by unknown error");
		}

		if (g.getResult() == null) {
			log.log(Level.WARNING, "No Wallpaper found, aborting...");
			return;
		}
		current = g.getResult();

		SetNewWallpaper set = new SetNewWallpaper(current);
		Thread t2 = new Thread(set);
		t2.start();
		settings.updateDate();
		Tray.getInstance().populateTray(cosmetifyTitle(current.getTitle()));
		log.log(Level.INFO, () -> "Wallpapers is successfully set to:\n" + current.toString());
	}

	@Override
	public void run() {
		long residualTime = settings.getLastTimeWallpaperChanged() + settings.getPeriod() * 60000L - System.currentTimeMillis();
		if (residualTime <= 60000L) { //if residualTime is under a minute I change the wallpaper anyway
			changeWallpaper();
			residualTime = settings.getPeriod() * 60000L;
		}

		while (!stopped) {
			try {
				Thread.sleep(residualTime); //
			} catch (InterruptedException e) {
				log.log(Level.INFO, "Sleep is interrupted");
			}
			residualTime = settings.getPeriod() * 60000L;
			changeWallpaper();
		}
		log.log(Level.INFO, "Background Service has been stopped as requested");
	}

	private static String cosmetifyTitle (String title) {
		return title
				.replace('_', ' ')
				.replace("OC", "")
				.replaceAll("[^a-zA-Z0-9 ,-]", "")
				.replaceAll(" [a-zA-Z]+[0-9]+[a-zA-Z0-9]*$", "")
				.replaceAll("[0-9]?[0-9][0-9][0-9] ?[*xX] ?[0-9][0-9][0-9][0-9]?", "");

	}

	// GETTER & SETTER

	public Wallpaper getCurrent() {
		return current;
	}
	public boolean isStopped() {
		return stopped;
	}
	public static Thread getThread() {
		return Thread.currentThread();
	}
}
