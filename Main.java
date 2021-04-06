import GUI.Background;
import GUI.GUI;
import Settings.Settings;

public class Main {
	public static void main(String []args) {
		Settings s = Settings.getInstance();
		s.readSettings();

		Background b = new Background();
		Thread bThread = new Thread(b);
		bThread.start();

		 new GUI();
	}
}
