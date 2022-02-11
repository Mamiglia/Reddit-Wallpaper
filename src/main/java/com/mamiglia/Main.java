package com.mamiglia;

import com.mamiglia.gui.Background;
import com.mamiglia.gui.GUI;
import com.mamiglia.gui.Tray;
import com.mamiglia.settings.Settings;
import com.mamiglia.utils.DisplayLogger;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
	private static final Logger log = DisplayLogger.getInstance("Main");

	public static void main(String []args) {
		System.out.println("Main Thread started");
		Settings s = Settings.INSTANCE;
		s.readSettings();

		Background b = Background.getInstance();
		Thread bThread = new Thread(b);
		bThread.start();

		GUI.setLookFeel();
		Tray tray = Tray.getInstance(bThread, b);
		tray.startTray();


		log.log(Level.FINER, "End of Main");
	}
}
