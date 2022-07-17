package com.mamiglia.gui;

import com.mamiglia.settings.Destination;
import com.mamiglia.settings.Settings;
import com.mamiglia.utils.GetNewWallpaper;
import com.mamiglia.utils.SetNewWallpaper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Background implements Runnable {
	//Singleton class
	// runs in background constantly:
	// 1 - Changes wallpaper
	// 2 - Sleep X time
	// 3 - repeat
	// until program is stopped
	private static final Logger log = LoggerFactory.getLogger("Background service");
	private static final Background uniqueInstance = new Background();
	private boolean stopped = false;

	private Background() {
	}

	public static Background getInstance() {
		return uniqueInstance;
	}

	public void stop() {
		stopped = true;
	}

	/* Changes wallpaper to a single destination (remember that a single destination could contain multiple monitors */
	public void changeWallpaper(Destination dest) {
		log.info("Changing wallpaper for destination {}", dest.getName());
		if (dest.getScreens().isEmpty() || dest.getSourcesId().isEmpty()) {
			log.warn("Wallpaper not changed for destination {}", dest.getName() + " because it has no sources or monitors associated");
			dest.updateLastChange();
			return;
		}
		GetNewWallpaper g = new GetNewWallpaper(dest.getSources(), dest);
		Thread t1 = new Thread(g);
		t1.start();
		try {
			t1.join();
		} catch (InterruptedException e) {
			log.error("Thread GetNewWallpaper was interrupted by unknown error");
		}
		dest.setCurrent(g.getResult());

		SetNewWallpaper set = new SetNewWallpaper(g.getResult(), dest);
		Thread t2 = new Thread(set);
		t2.start();
		try {
			t2.join();
		} catch (InterruptedException e) {
			log.error("Thread SetNewWallpaper was interrupted by unknown error");
		}
		dest.updateLastChange();

		Tray.getInstance().populateTray();

	}

	@Override
	public void run() {
		while (!stopped) {
			Long residualTime = getShortestTimer();
			try {
				Thread.sleep(residualTime);
			} catch (InterruptedException e) {
				log.info("Sleep is interrupted");
			}
			Settings.INSTANCE.writeSettings();
			if (!stopped) {
				changeWallpapers();
			}

		}
		log.info("Background Service has been stopped as requested");
	}

	private Long getShortestTimer() {
		long shortest = Long.MAX_VALUE;

		for (Destination dest : Settings.INSTANCE.getDests()) {
			shortest = Long.min(shortest, dest.getResidualTime());
		}

		return Long.max(shortest, 0);
	}

	private void changeWallpapers() {
		for (Destination dest : Settings.INSTANCE.getDests()) {
			log.debug("Destination {} has still {} milliseconds left", dest.getName(), dest.getResidualTime());
			if (dest.isTimeElapsed())
				changeWallpaper(dest);
		}
	}

	// GETTER & SETTER
	public boolean isStopped() {
		return stopped;
	}
	public static Thread getThread() {
		return Thread.currentThread();
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
}
