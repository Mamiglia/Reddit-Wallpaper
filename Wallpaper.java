import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

class Wallpaper {
    public static final Date NEVER_USED = new Date(0);
    public static final String DEFAULT_PATH = "wallpapers/";
    private final String title;
    private final String url;
    private final String postUrl;
    private Date lastUsedDate;
    private Image wallpaper;


    public Wallpaper(String title, String url, String postUrl) {
        this.title = title;
        this.url = url;
        if (postUrl.contains("https://www.reddit.com")) this.postUrl = postUrl;
        else this.postUrl = "https://www.reddit.com" + postUrl;
        lastUsedDate = NEVER_USED;
    }

    public Wallpaper(String title, String url, String postUrl, long lastUsedDate) {
        this(title, url,postUrl);
        this.lastUsedDate = new Date(lastUsedDate);
    }

    public void download() throws IOException {
        wallpaper = ImageIO.read(new URL(url));
        saveImage(wallpaper, false);
        // when an image is downloaded it's also used for the first time
        updateDate();
    }

    public void saveImage(Image img, boolean thumbnail) throws IOException {
        String path;
        if (thumbnail) {
            path = ".utility/thumbnails/";
        } else {
            path = DEFAULT_PATH;
        }
        BufferedImage bi = new BufferedImage(
                img.getWidth(null),
                img.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = bi.createGraphics();
        g2.drawImage(img, 0, 0, null);
        g2.dispose();
        File f = new File(path + title +".png");
        f.mkdirs();
        f.createNewFile();
        ImageIO.write(bi, "png", f);
    }

    public void updateDate() {
        lastUsedDate = new Date();
    }

    public boolean isDownloaded() {
        File f = new File(getPath());
        return f.exists();
    }


    // GETTERS
    public void setDate() {
        lastUsedDate = new Date();
    }
    public double getRatio() {
        return (double) getWidth()/ (double) getHeight();
    }
    public int getWidth() {
        return wallpaper.getWidth(null);
    }
    public int getHeight() {
        return wallpaper.getHeight(null);
    }
    public String getPath() {
        return DEFAULT_PATH + title + ".png";
    }
    public String getTitle() {
        return title;
    }
    public Date getLastUsedDate() {
        return lastUsedDate;
    }
    public String getUrl() {
        return url;
    }
    public String getPostUrl() {
        return postUrl;
    }
    public Image getWallpaper() throws IOException {
        if (wallpaper == null && isDownloaded()) {
            wallpaper = ImageIO.read(new File(getPath()));
        } else if (wallpaper == null) {
            wallpaper = ImageIO.read(new URL(url));
        }
        return wallpaper;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && o.getClass().equals(getClass()) && url.equals(((Wallpaper) o).getUrl());
    }

    @Override
    public String toString() {
        return title + "\nimage url:" + url + "\npost url: " + postUrl + "\ndate:" + lastUsedDate + "\n";
    }

}
