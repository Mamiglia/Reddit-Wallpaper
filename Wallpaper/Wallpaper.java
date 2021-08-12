package Wallpaper;

import Settings.Settings;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.Files;
import java.util.Objects;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Wallpaper implements Serializable {
    public static final String DEFAULT_PATH = Settings.getWallpaperPath() + File.separator;
    public static final String FORMAT = "png";
    private final String id;
    private final File file;
    private final String title;
    private final String url;
    private final String postUrl;


    public Wallpaper(String id, String title, String url, String postUrl) {
        this.id = id;
        this.title = title;
        file = new File(DEFAULT_PATH + this.title + "." + FORMAT);
        this.url = url;
        if (postUrl.contains("https://www.reddit.com")) this.postUrl = postUrl;
        else this.postUrl = "https://www.reddit.com" + postUrl;
    }

    public void download() throws IOException {
        file.mkdirs();
        try(InputStream in = new URL(url).openStream()){
            Files.copy(in, file.toPath(), REPLACE_EXISTING);
        }
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

    /**
        @return the absolute path of the wallpaper
     */
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
        return "title:\t" + title + "\nimage url:\t" + url + "\npost url:\t" + postUrl;
    }
}
