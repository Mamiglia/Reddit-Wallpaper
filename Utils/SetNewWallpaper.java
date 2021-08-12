package Utils;

import Wallpaper.Wallpaper;

import java.io.BufferedReader;
import java.io.IOException;
import com.sun.jna.Library;
import com.sun.jna.Native;

import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.jna.win32.*;
import org.h2.util.StringUtils;

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
            log.log(Level.WARNING, "Wallpaper file not found");
            return;
        }
        String wpPath = wp.getPath();
        String os = System.getProperty("os.name");
        switch (os) {
            case "Windows 10":
                windowsChange(wpPath);
                break;
            case "Linux":
                String de = identifyDE();
                if (de == null) {
                    log.log(Level.SEVERE, "Couldn't identify your Desktop Environment");
                    break;
                }

                switch (de) {
                    // Thanks StackOverflow
                    case "xfce":
                        executeProcess("xfconf-query -c xfce4-desktop -p /backdrop/screen0/monitorVGA-1/workspace0/last-image -s \"" + wpPath + "\"");
                        // May not work on different XFCE installations??
                        break;
                    case "gnome":
                        executeProcess("gsettings set org.gnome.desktop.background draw-background false && gsettings set org.gnome.desktop.background picture-uri \"file://" + wpPath + "\" && gsettings set org.gnome.desktop.background draw-background true");
                        // not tested
                        break;
                    case "kde":
                        executeProcess("qdbus org.kde.plasmashell /PlasmaShell org.kde.PlasmaShell.evaluateScript 'var allDesktops = desktops();print (allDesktops);for (i=0;i<allDesktops.length;i++) {d = allDesktops[i];d.wallpaperPlugin = \"org.kde.image\";d.currentConfigGroup = Array(\"Wallpaper\", \"org.kde.image\", \"General\");d.writeConfig(\"Image\", \"" + wpPath + "\")}'");
                        break;
                    case "unity":
                        executeProcess("gsettings set org.gnome.desktop.background picture-uri \"file://" + wpPath + "\"");
                        // not tested
                        break;
                    case "cinnamon":
                        executeProcess("gsettings set org.cinnamon.desktop.background picture-uri  \"file://" + wpPath + "\"");
                        // not tested
                        break;
                    default:
                        log.log(Level.SEVERE, "Your DE is currently not supported: " + de);
                }
                break;
            default:
                log.log(Level.WARNING, () -> "Can't recognize OS: " + os);
        }
    }

    public static String identifyDE() {
        String de;
        de = executeProcess("echo $XDG_CURRENT_DESKTOP").toLowerCase();

        if (de.contains("xfce")) {
            return "xfce";
        } else if (de.contains("kde")) {
            return "kde";
        } else if (de.contains("unity")) {
            return "unity";
        } else if (de.contains("gnome")) {
            return "gnome";
        } else if (de.contains("cinnamon")) {
            return "cinnamon";
        } else if (de.contains("mate")) {
            return "mate";
        } else if (de.contains("deepin")) {
            return "deepin";
        } else if (de.contains("budgie")) {
            return "budgie";
        } else if (de.contains("lxqt")) {
            return "lxqt";
        } else {
            log.log(Level.FINE, () -> "Not identifiable with: echo $XDG_CURRENT_DESKTOP: " + de);
        }

        de = executeProcess("echo $GDM_SESSION").toLowerCase();

        if (de.contains("xfce")) {
            return "xfce";
        } else if (de.contains("kde")) {
            return "kde";
        } else if (de.contains("unity")) {
            return "unity";
        } else if (de.contains("gnome")) {
            return "gnome";
        } else if (de.contains("cinnamon")) {
            return "cinnamon";
        }  else if (de.contains("mate")) {
            return "mate";
        } else if (de.contains("deepin")) {
            return "deepin";
        } else if (de.contains("budgie")) {
            return "budgie";
        } else if (de.contains("lxqt")) {
            return "lxqt";
        } else {
            log.log(Level.FINE, "Not identifiable with: echo $GDM_SESSION");
        }

        return null;
    }

    public static String executeProcess(String s) {
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", s);
        pb.redirectErrorStream(true);
        Process p = null;
        try {
            p = pb.start();
        } catch (IOException e) {
            log.log(Level.WARNING, () -> "Error while executing command: " + s);
            return null;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

        StringBuilder res = new StringBuilder();
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                res.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return res.toString();
    }

    interface User32 extends Library {
        User32 INSTANCE = Native.load("user32", User32.class, W32APIOptions.DEFAULT_OPTIONS);

        boolean SystemParametersInfo(int one, int two, String s, int three);
    }

    void windowsChange(String path) {
        log.log(Level.FINE, () -> "Detected Windows, setting wallpaper in " + path);
        User32.INSTANCE.SystemParametersInfo(0x0014, 0, path, 1);
        // Note: result of this ^ function is useless
    }

}
