package GUI;

import Settings.Settings;
import Utils.GetNewWallpaper;
import Utils.SetNewWallpaper;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Background implements Runnable {
	//Singleton class
	// runs in background constantly:
	// 1 - Changes wallpaper
	// 2 - Sleep X time
	// 3 - repeat
	//until program is stopped
	private static final Logger log = Logger.getLogger("background service");
	private static final Background uniqueInstance = new Background();
	private Settings settings = Settings.getInstance();
	private boolean stopped = false;

	public Background() {
	}

	public Background getInstance() {
		return uniqueInstance;
	}

	public static void stop() {
		uniqueInstance.stopped = true;
		uniqueInstance.getThread().interrupt();
	}

	public static void changeNow() {
		uniqueInstance.getThread().interrupt();
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
		log.log(Level.INFO, "Wallpapers is successfully set to: " + g.getResult().getTitle());
	}



	// GETTER & SETTER
	public boolean isStopped() {
		return stopped;
	}
	public Thread getThread() {
		return Thread.currentThread();
	}

	@Override
	public void run() {
		changeWallpaper();
		while (!stopped) {
			try {
				Thread.sleep(settings.getPeriod() * 60 * 1000);
			} catch (InterruptedException e) {
				log.log(Level.INFO, "Sleep is interrupted, change of wallpaper is anticipated");
			} finally {
				changeWallpaper();
			}
		}
		log.log(Level.INFO, "Background Service has been stopped as requested");
	}
}
