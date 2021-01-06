import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Date;

class Wallpaper implements Serializable {
    private static final Date NEVER_USED = new Date(0);
    private final String title;
    private final String url;
    private final String thumbnailUrl;
    private final String postUrl;
    private Date lastUsedDate;
    private Image wallpaper;
    private Image thumbnail;


    public Wallpaper(String title, String url, String thumbnailUrl, String postUrl) {
        this.title = title;
        this.url = url;
        this.thumbnailUrl = thumbnailUrl;
        this.postUrl = "https://www.reddit.com" + postUrl;
        lastUsedDate = NEVER_USED;
    }

    public Wallpaper(String title, String url, String thumbnailUrl, String postUrl, int lastUsedDate) {
        this(title, url, thumbnailUrl, postUrl);
        this.lastUsedDate = new Date(lastUsedDate);
    }

    public void download() throws IOException {
        wallpaper = ImageIO.read(new URL(url));
        thumbnail = ImageIO.read(new URL(thumbnailUrl));
        saveImage(wallpaper, false);
        saveImage(thumbnail, true);
        // when an image is downloaded it's also used for the first time
        updateDate();
    }

    public void saveImage(Image img, boolean thumbnail) throws IOException {
        String path;
        if (thumbnail) {
            path = ".utility/thumbnails/";
        } else {
            path = "wallpapers/";
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
        return "wallpapers/" + title + ".jpg";
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

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getPostUrl() {
        return postUrl;
    }

    public Image getWallpaper() {
        return wallpaper;
    }

    public Image getThumbnail() {
        return thumbnail;
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
