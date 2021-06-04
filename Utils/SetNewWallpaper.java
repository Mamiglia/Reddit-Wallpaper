package Utils;

import Wallpaper.Wallpaper;

import java.io.IOException;
import com.sun.jna.Library;
import com.sun.jna.Native;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.jna.win32.*;
public class SetNewWallpaper implements Runnable {
    private final static Logger log = DisplayLogger.getInstance("SetNewWallpaper");
    private boolean executed = false;
    private final Wallpaper wp;

    public SetNewWallpaper(Wallpaper wp) {
        this.wp = wp;
    }


    @Override
    public void run() {
        if (executed) return;
        executed = true;

        if (!wp.isDownloaded()) {
            log.log(Level.SEVERE, "Wallpaper file not found, aborting");
            return;
        }
        String os = System.getProperty("os.name");
        switch (os) {
            case "Windows 10":
                windowsChange(wp.getPath());
                break;
            case "Linux":
                //TODO NOT SUPPORTED YET
                //only XFCE.
                //Probably won't work in machines different from mine
                try {
                    String s = "xfconf-query -c xfce4-desktop -p /backdrop/screen0/monitorVGA-1/workspace0/last-image -s \"" + Paths.get(".").toAbsolutePath().normalize().toString() + "/" + wp.getPath() + "\"";
                    //System.out.println(s);
                    ProcessBuilder processB = new ProcessBuilder().command("bash", "-c" , s);
                    Process process = processB.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                log.log(Level.SEVERE, () -> "Can't recognize OS: " + os);
        }
    }

    interface User32 extends Library {
        User32 INSTANCE = Native.load("user32",User32.class,W32APIOptions.DEFAULT_OPTIONS);
        boolean SystemParametersInfo (int one, int two, String s ,int three);
    }
    void windowsChange(String path) {
        log.log(Level.FINE, () ->"Detected Windows, setting wallpaper in " + path);
        boolean res = User32.INSTANCE.SystemParametersInfo(0x0014, 0, path , 1);
        //TODO delete the res var and the log below if res is inconclusive
        log.log(Level.FINEST, () -> "result is " + res);
    }

    static String windowsPathConverter(String s) {
        // Windows takes path as with double backslash:
        // home/Desktop/folder -> home\\Desktop\\folder
        String res = "";
        for (int i=0; i<s.length(); i++) {
            char k = s.charAt(i);
            if (k=='/') {
                res += "\\";
            } else {
                res += k;
            }

        }
        return res;
    }
}
