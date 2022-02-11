package com.mamiglia.utils;

import com.mamiglia.settings.Destination;
import com.mamiglia.settings.Settings;
import com.mamiglia.wallpaper.Wallpaper;

import java.io.BufferedReader;
import java.io.IOException;

import com.sun.jna.Library;
import com.sun.jna.Native;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.COM.COMException;
import com.sun.jna.platform.win32.COM.COMUtils;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.WTypes;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.*;
import com.sun.jna.Platform;

public class SetNewWallpaper implements Runnable {
    static private final Logger log = DisplayLogger.getInstance("SetNewWallpaper");
    private boolean executed = false;
    private final Wallpaper wp;
    private final Destination dest;

    public SetNewWallpaper(Wallpaper wp, Destination dest) {
        this.wp = wp;
        this.dest = dest;
    }


    @Override
    public void run() {
        if (executed) return;

        if (wp.isDownloaded()) {
            log.log(Level.WARNING, "Wallpaper file not found");
            return;
        }
        String wpPath = wp.getPath().toAbsolutePath().toString();
        int os = Platform.getOSType();
        switch (os) {
//            case 0: // Mac
            case 1: // Other Linux
                String de = identifyDE();

                if (de == null) {
                    log.log(Level.SEVERE, "Couldn't identify your Desktop Environment: {0}, {1}");
                    break;
                }

                switch (de) {
                    // Thanks StackOverflow
                    case "xfce":
                        executeProcess(
                            "xfconf-query -c xfce4-desktop -p /backdrop/screen0/monitorVGA-1/workspace0/last-image -s \"" + wpPath + "\"");
                        break;
                    // May not work on different XFCE installations??

                    case "kde":
                        executeProcess(
                            "qdbus org.kde.plasmashell /PlasmaShell org.kde.PlasmaShell.evaluateScript 'var allDesktops = desktops();print (allDesktops);for (i=0;i<allDesktops.length;i++) {d = allDesktops[i];d.wallpaperPlugin = \"org.kde.image\";d.currentConfigGroup = Array(\"Wallpaper\", \"org.kde.image\", \"General\");d.writeConfig(\"Image\", \"" + wpPath + "\")}'");
                        break;

                    // not tested
                    case "gnome":
                        executeProcess(
                            "gsettings set org.gnome.desktop.background draw-background false && gsettings set org.gnome.desktop.background picture-uri \"file://" + wpPath + "\" && gsettings set org.gnome.desktop.background draw-background true");
                        break;
                    case "unity":
                        executeProcess(
                            "gsettings set org.gnome.desktop.background picture-uri \"file://" + wpPath + "\"");
                        break;
                    case "cinnamon":
                        executeProcess(
                            "gsettings set org.cinnamon.desktop.background picture-uri  \"file://" + wpPath + "\"");
                        break;
                    default: log.log(Level.SEVERE, "Your DE is currently not supported: " + de);
                }
                break;
            case 2: // Other Windows
                if (Settings.INSTANCE.isSingleDestination()) {
                    windowsChange(wpPath);
                } else {
                    for (int idx : dest.getScreens()) {
                        windowsChange(wpPath, idx);
                    }
                }
                break;
//            case 3: // Solaris
//            case 4: // Free BSD
//            case 5: // Open BSD
//            case 6: // Windows CE
//            case 7: // AIX
//            case 8: // Android
//            case 9: // GNU
//            case 10: // GNU/kFreeBSD
//            case 11: // NetBSD
            default: //
                log.log(Level.WARNING, () -> "Can't recognize OS: " + os);
        }
    }

    public static String identifyDE() {
        int flag = 0;
        String de = null;

        try {
            de = System.getenv("XDG_CURRENT_DESKTOP").toLowerCase();
            flag = 1;
        } catch (NullPointerException e) {
            log.log(Level.FINE, "Not identifiable with: echo $XDG_CURRENT_DESKTOP: {1}" + e);
        } catch (SecurityException e) {
            log.log(Level.SEVERE, "Forbidden by security policy: " + e);
        }
        try {
            de = System.getenv("$GDM_SESSION").toLowerCase();
            flag = 2;
        } catch (NullPointerException e) {
            log.log(Level.FINE, "Not identifiable with: echo $GDM_SESSION" + e);
        } catch (SecurityException e) {
            log.log(Level.SEVERE, "Forbidden by security policy: " + e);
        }

        if (de != null) {
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
                switch (flag) {
                    case 1:
                        log.log(Level.FINE, "Not identifiable with: echo $XDG_CURRENT_DESKTOP: {1}", de);
                        break;
                    case 2:
                        log.log(Level.FINE, "Not identifiable with: echo $GDM_SESSION");
                        break;
                    default: log.log(Level.SEVERE, "Desktop environment not identifiable!");
                }
            }
        }
        return null;
    }

    public static void executeProcess(String s) {
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", s);
        pb.redirectErrorStream(true);
        Process p;
        try {
            p = pb.start();
        } catch (IOException e) {
            log.log(Level.WARNING, () -> "Error while executing command: " + s);
            return;
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
    }

    interface User32 extends Library {
        User32 INSTANCE = Native.load("user32", User32.class, W32APIOptions.DEFAULT_OPTIONS);
        int SETDESKWALLPAPER = 0x0014;

        void SystemParametersInfo(
                int uiAction,
                int uiParam,
                String pvParam,
                int fWinIni);
    }

    void windowsChange(String path) {
        log.log(Level.FINE, () -> "Detected Windows, setting wallpaper in " + path);
        User32.INSTANCE.SystemParametersInfo(User32.SETDESKWALLPAPER, 0, path, 1);
    }

    void windowsChange(String path, int i) {
        log.log(Level.FINE, () -> "Detected Windows, setting wallpaper in " + path + " to screen " + i);

        // Copy pasted from https://github.com/matthiasblaesing/JNA-Demos/blob/master/IDesktopWallpaper/src/main/java/eu/doppel_helix/dev/blaesing/IDesktopWallpaper/Main.java
        WinNT.HRESULT result = Ole32.INSTANCE.CoInitializeEx(Pointer.NULL, Ole32.COINIT_MULTITHREADED);
        COMUtils.checkRC(result);
        try {
            PointerByReference wallpaperBuffer = new PointerByReference();
            result = Ole32.INSTANCE.CoCreateInstance(
                    DesktopWallpaper.CLSID,
                    Pointer.NULL,
                    WTypes.CLSCTX_SERVER,
                    DesktopWallpaper.IID,
                    wallpaperBuffer);
            COMUtils.checkRC(result);
            DesktopWallpaper wallpaper = new DesktopWallpaper(wallpaperBuffer.getValue());
            try {
                    String deviceName = wallpaper.GetMonitorDevicePathAt(i);
                    wallpaper.SetWallpaper(deviceName, path);
            } catch(COMException e) {
                log.log(Level.WARNING, () -> "COM failed to use windows API, maybe you don't have that many monitors?");
            } finally {
                wallpaper.Release();
            }
        } finally {
            // Uninitialize COM
            Ole32.INSTANCE.CoUninitialize();
        }
    }
}

