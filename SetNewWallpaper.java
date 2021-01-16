import java.io.IOException;

class SetNewWallpaper implements Runnable {
    private final Wallpaper wp;

    SetNewWallpaper(Wallpaper wp) {
        this.wp = wp;
    }


    @Override
    public void run() {
        if (!wp.isDownloaded()) {
            System.out.println("ERROR wallpaper file not found");
        }
        // Windows
        //System.loadLibarary("user32")
        //SystemParametersInfo(20, 0, wp.getPath(), 0);

        // Linux XFCE
        try {
            Runtime.getRuntime().exec("xfconf-query -c xfce4-desktop -p /backdrop/screen0/monitorVGA-1/workspace0/last-image -s " + wp.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
