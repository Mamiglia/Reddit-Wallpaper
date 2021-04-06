package Utils;

import Wallpaper.Wallpaper;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SetNewWallpaper implements Runnable {
    private final static Logger log = Logger.getLogger("SetNewWallpaper");
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
            log.log(Level.WARNING, "ERROR wallpaper file not found");
        }
        // Windows
        //System.loadLibarary("user32")
        //SystemParametersInfo(20, 0, wp.getPath(), 0);


        // Linux XFCE
        // probably won't work in machine different from mine
        try {
            String s = "xfconf-query -c xfce4-desktop -p /backdrop/screen0/monitorVGA-1/workspace0/last-image -s \"" + Paths.get(".").toAbsolutePath().normalize().toString() + "/" + wp.getPath() + "\"";
            System.out.println(s);
            ProcessBuilder processB = new ProcessBuilder().command("bash", "-c" , s);
            Process process = processB.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
