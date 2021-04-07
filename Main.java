import GUI.Background;
import GUI.Tray;
import Settings.Settings;
import Utils.DisplayLogger;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
	private static final Logger log = DisplayLogger.getInstance("Main");

	public static void main(String []args) {
		Settings s = Settings.getInstance();
		s.readSettings();

		Background b = new Background();
		Thread bThread = new Thread(b);
		bThread.start();

		Tray tray = new Tray(bThread, b);
		tray.startTray();


		log.log(Level.FINE, "End of Main");
	}
}
