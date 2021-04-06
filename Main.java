import GUI.Background;
import GUI.GUI;
import Settings.Settings;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
	private static final Logger log = Logger.getLogger("Main");

	public static void main(String []args) {
		Settings s = Settings.getInstance();
		s.readSettings();

		Background b = new Background();
		Thread bThread = new Thread(b);
		bThread.start();

		new GUI(bThread);


		log.log(Level.FINE, "End of Main");
	}
}
