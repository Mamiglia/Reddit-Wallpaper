package com.mamiglia.gui;

import com.mamiglia.settings.Settings;
import com.mamiglia.utils.DisplayLogger;
import com.mamiglia.utils.GetNewWallpaper;
import com.mamiglia.utils.SetNewWallpaper;
import com.mamiglia.wallpaper.Wallpaper;

import java.util.*;
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
	private List<Long> timers;

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

	public void changeWallpaper(List<Integer> monitorIdx) {
		GetNewWallpaper g = new GetNewWallpaper(settings);
		Thread t1 = new Thread(g);
		t1.start();
		try {
			t1.join();
		} catch (InterruptedException e) {
			log.log(Level.SEVERE, "Thread GetNewWallpaper was interrupted by unknown error");
		}

		if (monitorIdx != null) { //monitor index == null means change to all monitors
			List<Thread> threadList = new LinkedList<>();
			for (Integer idx : monitorIdx) {
				SetNewWallpaper set = new SetNewWallpaper(g.getResult(), idx);
				Thread t2 = new Thread(set);
				t2.start();
				threadList.add(t2);
				t2.setName(idx.toString());
				settings.updateDate(idx);
			}
			log.log(Level.INFO, () -> "Wallpaper is being set to:\n" + current);
			for (Thread t2 : threadList) {
				try {
					t2.join();
				} catch (InterruptedException e) {
					log.log(Level.SEVERE, "Thread SetNewWallpaper was interrupted by unknown error");
				}
				log.log(Level.INFO, () -> "Succes for monitor #" + t2.getName());
			}
		} else {
			current = g.getResult();
			SetNewWallpaper set = new SetNewWallpaper(current, -1);
			Thread t2 = new Thread(set);
			t2.start();
			try {
				t2.join();
			} catch (InterruptedException e) {
				log.log(Level.SEVERE, "Thread SetNewWallpaper was interrupted by unknown error");
			}
			settings.updateDate(0);
			log.log(Level.INFO, () -> "Wallpaper is successfully set to:\n" + current);
		}
		Tray.getInstance().populateTray();

	}

	@Override
	public void run() {
		initializeTimers(settings.getScreens());

		Boolean[] timeOvers = updateTimers(0L);
		changeWallpapers(timeOvers);


		while (!stopped) {
			Long residualTime = getShortestTimer();
			long start = System.currentTimeMillis();
			try {
				Thread.sleep(residualTime); //
			} catch (InterruptedException e) {
				log.log(Level.INFO, "Sleep is interrupted");
			}

			timeOvers = updateTimers(System.currentTimeMillis() - start);
			changeWallpapers(timeOvers);
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

	private void initializeTimers(int size) {
		timers = new ArrayList<>(size);
		for (int i=0; i < size; i++) {
			timers.set(i, settings.getLastTimeWallpaperChanged(i) + settings.getTimerForMonitor(i) * 60000L - System.currentTimeMillis());
		}
	}

	private Long getShortestTimer() {
		return Collections.min(timers);
	}

	private Boolean[] updateTimers(Long elapsedTime) {
		Boolean[] timeovers = new Boolean[timers.size()];
		Arrays.fill(timeovers, false);
		for (int i=0; i<timers.size(); i++) {
			Long residualTime = timers.get(i) - elapsedTime;
			if (residualTime <= 60000L) { //if residualTime is under a minute I change the wallpaper anyway
				residualTime = settings.getTimerForMonitor(i) * 60000L;
				timeovers[i] = true;
				if (!settings.getDiffWallpapers()) {
					Arrays.fill(timeovers, true);
					Collections.fill(timers, residualTime);
					break;
				}
			}
			timers.set(i, residualTime);
		}
		return timeovers;
	}

	private void changeWallpapers(Boolean[] timeovers) {
		if (settings.getDiffWallpapers()) {
			changeWallpaper(null);
			return;
		}
		List<Integer> indexes = new LinkedList<>();
		for (int i=0; i<timeovers.length; i++) {
			if (timeovers[i]) indexes.add(i);
		}
		changeWallpaper(indexes);

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
