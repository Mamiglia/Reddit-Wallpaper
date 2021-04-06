package Wallpaper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

public class Wallpaper {
    public static final Date NEVER_USED = new Date(0);
    public static final String DEFAULT_PATH = "wallpapers/";
    private final String title;
    private final String url;
    private final String postUrl;
    private Date lastUsedDate;
    private Image image;


    public Wallpaper(String title, String url, String postUrl) {
        this.title = title.replace(";", "");
        // no ";" allowed for stability reasons
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
        image = ImageIO.read(new URL(url));
        saveImage(image);
        // when an image is downloaded it's also used for the first time
        updateDate();
    }

    public void saveImage(Image img) throws IOException {
        BufferedImage bi = new BufferedImage(
                img.getWidth(null),
                img.getHeight(null),
                BufferedImage.TYPE_INT_RGB);

        Graphics2D g2 = bi.createGraphics();
        g2.drawImage(img, 0, 0, null);
        g2.dispose();
        File f = new File(getPath());
        f.getParentFile().mkdirs();
        f.createNewFile();
        ImageIO.write(bi, "jpg", f);
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
        return image.getWidth(null);
    }
    public int getHeight() {
        return image.getHeight(null);
    }
    public String getPath() {
        return DEFAULT_PATH + cleanString(title) + ".jpg";
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
    public Image getImage() throws IOException {
        if (image == null && isDownloaded()) {
            image = ImageIO.read(new File(getPath()));
        } else if (image == null) {
            image = ImageIO.read(new URL(url));
        }
        return image;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && o.getClass().equals(getClass()) && url.equals(((Wallpaper) o).getUrl());
    }

    @Override
    public String toString() {
        return title + "\nimage url:" + url + "\npost url: " + postUrl + "\ndate:" + lastUsedDate + "\n";
    }

    public static String cleanString(String s) {
        // removes non alphanumerical characters from string
        String res = "";
        for (int i=0; i<s.length(); i++) {
            int k = s.charAt(i);
            if ((k>=48 && k<=57) || (k>=65 && k<=90) || (k>=97 && k<=122) || k==91 || k==93) {
                res += s.charAt(i);
            }
        }
        return res;
    }

}
