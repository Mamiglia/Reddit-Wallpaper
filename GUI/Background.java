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
	private static final Logger log = DisplayLogger.getInstance("background service");
	private static final Background uniqueInstance = new Background();
	private Settings settings = Settings.getInstance();
	private boolean stopped = false;
	private Wallpaper current = null;

	public Background() {
	}

	public static Background getInstance() {
		return uniqueInstance;
	}

	public void stop() {
		stopped = true;
	}

	public static void changeNow() {
		// TODO remove due to deprecated
		Thread.currentThread().interrupt();
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
			log.log(Level.WARNING, "Wallpaper not set");
			return;
		}
		SetNewWallpaper set = new SetNewWallpaper(g.getResult());
		Thread t2 = new Thread(set);
		t2.start();
		current = g.getResult();
		log.log(Level.INFO, "Wallpapers is successfully set to: " + g.getResult().toString());
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

	@Override
	public void run() {
		while (!stopped) {
			changeWallpaper();
			try {
				Thread.sleep(settings.getPeriod() * 60 * 1000);
			} catch (InterruptedException e) {
				log.log(Level.INFO, "Sleep is interrupted");
			}
		}
		log.log(Level.INFO, "Background Service has been stopped as requested");
	}
}
