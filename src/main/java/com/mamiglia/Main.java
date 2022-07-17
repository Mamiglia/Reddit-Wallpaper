package com.mamiglia;

import com.mamiglia.gui.Background;
import com.mamiglia.gui.GUI;
import com.mamiglia.gui.Tray;
import com.mamiglia.settings.Settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
	private static final Logger log = LoggerFactory.getLogger("Main");

	public static void main(String []args) {
		System.out.println("Main Thread started");

		Background b = Background.getInstance();
		Thread bThread = new Thread(b);
		bThread.start();

		GUI.setLookFeel();
		Tray tray = Tray.getInstance(bThread, b);
		tray.startTray();


		log.debug("End of Main");
	}
}
