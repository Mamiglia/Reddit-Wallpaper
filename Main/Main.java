package Main;

import GUI.Background;
import GUI.GUI;
import GUI.Tray;
import Settings.Settings;
import Utils.DisplayLogger;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
	private static final Logger log = DisplayLogger.getInstance("Main");

	public static void main(String []args) {
		System.out.println("Main Thread started");
		Settings s = Settings.getInstance();
		s.readSettings();

		Background b = new Background();
		Thread bThread = new Thread(b);
		bThread.start();

		GUI.setLookFeel();
		Tray tray = new Tray(bThread, b);
		tray.startTray();


		log.log(Level.FINER, "End of Main");
	}
}
