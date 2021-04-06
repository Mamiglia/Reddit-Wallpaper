import GUI.Background;
import GUI.Tray;
import Settings.Settings;
import Utils.SetNewWallpaper;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.*;
import com.sun.jna.win32.*;

import java.util.logging.Level;
import java.util.logging.Logger;

//public class Main {
//	public static void main(String []args) {
//		windowsChange("C:\\Users\\Archimede\\Documents\\GitHub\\Reddit-Wallpaper\\wallpapers\\IronLotus[5120x2880]Moreincomments.jpg");
//	}
//
//	interface User32 extends Library {
//		User32 INSTANCE = (User32) Native.load("user32",User32.class,W32APIOptions.DEFAULT_OPTIONS);
//		boolean SystemParametersInfo (int one, int two, String s ,int three);
//	}
//	static void windowsChange(String path) {
//		User32.INSTANCE.SystemParametersInfo(0x0014, 0, path , 1);
//	}
//}

public class Main {
	private static final Logger log = Logger.getLogger("Main");

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
