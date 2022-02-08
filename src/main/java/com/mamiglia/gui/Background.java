package com.mamiglia.gui;

import com.mamiglia.settings.Settings;
import com.mamiglia.utils.DisplayLogger;
import com.mamiglia.utils.GetNewWallpaper;
import com.mamiglia.utils.SetNewWallpaper;
import com.mamiglia.wallpaper.Wallpaper;

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

	public void banWallpaper() {
		settings.addBanned(current.getID());
		if (!settings.keepBlacklist) {
			if (current.delete()) {
				log.log(Level.INFO, "Blacklisted image removed.");
			} else {
				log.log(Level.INFO, "Blacklisted image was not removed.");
			}
		}
	}

	public void changeWallpaper() {
		int screens = settings.getScreens();
		boolean diff = settings.getDiffWallpapers();
		GetNewWallpaper g = new GetNewWallpaper(settings, screens, diff);
		Thread t1 = new Thread(g);
		t1.start();
		try {
			t1.join();
		} catch (InterruptedException e) {
			log.log(Level.SEVERE, "Thread GetNewWallpaper was interrupted by unknown error");
		}

		if (screens > 1 && diff) {
			int i = 0;
			for (Wallpaper c : g.getResult(screens)) {
				SetNewWallpaper set = new SetNewWallpaper(c, screens);
				Thread t2 = new Thread(set);
				t2.start();
				settings.updateDate();
				Tray.getInstance().populateTray(cosmetifyTitle(c.getTitle()));
				log.log(Level.INFO, () -> "Wallpaper is successfully set to:\n" + c);
				i++;
				if (i == screens) break;
			}
		} else {
			current = g.getResult();
			SetNewWallpaper set = new SetNewWallpaper(current, diff);
			Thread t2 = new Thread(set);
			t2.start();
			settings.updateDate();
			Tray.getInstance().populateTray(cosmetifyTitle(current.getTitle()));
			log.log(Level.INFO, () -> "Wallpaper is successfully set to:\n" + current);
		}
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
		String temp = title
				.replace('_', ' ')
				.replace("OC", "")
				.replaceAll("([0-9]{3,4} ?[*xX] ?[0-9]{3,4})|([^\\w ,-])|( [a-zA-Z]+[0-9]+[\\w]*$)",
						"");
		if (temp.length() > 23) {
			temp = temp.substring(0, 20);
			temp += "...";
		}
		return temp;


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
