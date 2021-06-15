package Wallpaper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Objects;

public class Wallpaper implements Serializable {
    public static final String DEFAULT_PATH = "wallpapers" + File.separator;
    public static final String FORMAT = "png";
    private final String id;
    private final File file;
    private final String title;
    private final String url;
    private final String postUrl;
//    private transient Image image;


    public Wallpaper(String id, String title, String url, String postUrl) {
        this.id = id;
        this.title = title;
        // no ";" allowed for stability reasons
        file = new File(DEFAULT_PATH + this.title + "." + FORMAT);
        this.url = url;
        if (postUrl.contains("https://www.reddit.com")) this.postUrl = postUrl;
        else this.postUrl = "https://www.reddit.com" + postUrl;
    }

    public void download() throws IOException {
        Image image = ImageIO.read(new URL(url));
        saveImage(image);
    }

    public void saveImage(Image img) throws IOException {
        BufferedImage bi = new BufferedImage(
                img.getWidth(null),
                img.getHeight(null),
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g2 = bi.createGraphics();
        g2.drawImage(img, 0, 0, null);
        g2.dispose();
        file.getParentFile().mkdirs();
        file.createNewFile();
        ImageIO.write(bi, FORMAT, file);
    }

    public boolean isDownloaded() {
        return file.exists();
    }


    // GETTERS
//    public double getRatio() {
//        return (double) getWidth() / (double) getHeight();
//    }
//
//    public int getWidth() {
//        return image.getWidth(null);
//    }
//
//    public int getHeight() {
//        return image.getHeight(null);
//    }

    public String getPath() {
        return file.getAbsolutePath();
    }
    public String getTitle() {
        return title;
    }
    public String getUrl() {
        return url;
    }
    public String getPostUrl() {
        return postUrl;
    }
//    public Image getImage() throws IOException {
//        if (image == null && isDownloaded()) {
//            image = ImageIO.read(new File(getPath()));
//        } else if (image == null) {
//            image = ImageIO.read(new URL(url));
//        }
//        return image;
//    }
    public String getID() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && o.getClass().equals(getClass()) && url.equals(((Wallpaper) o).getUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, url, postUrl);
    }

    @Override
    public String toString() {
        return title + "\nimage url:" + url + "\npost url: " + postUrl;
    }
}
