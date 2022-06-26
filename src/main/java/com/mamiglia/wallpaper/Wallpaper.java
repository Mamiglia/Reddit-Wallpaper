package com.mamiglia.wallpaper;

import com.mamiglia.settings.Settings;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Wallpaper implements Serializable {
    private static final int MAX_TITLE_SIZE = 100;
    private final String id;
    private final File file;
    private final String title;
    private final String url;
    private final String postUrl;
    private final int width;
    private final int height;



    public Wallpaper(String id, String title, String url, String postUrl, int width, int height) {
        this.id = id;
        this.title = title;
        this.width = width;
        this.height = height;
        String format = url.replaceAll("^.*(?=\\.\\w+$)", "");
        file = new File(getWallpaperDirectory() + cleanTitle(title) + format);
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
    public double getRatio() {
        return (double) getWidth() / (double) getHeight();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
        @return the absolute path of the wallpaper
     */
    public Path getPath() {
        return file.toPath();
    }
    public String getTitle() {
        return title.split(",")[0];
    }
    public String getCompleteTitle() { return title;}
    public String getUrl() {
        return url;
    }
    public String getSubreddit() {
        return postUrl.split("/")[4];
        //https://www.reddit.com/r/SUBREDDIT_NAME/comments/ID/POST_NAME/
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
    public Boolean isLandscape() {
        return this.getRatio() > 1;
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

    private static String getWallpaperDirectory() {
        return Settings.INSTANCE.getWallpaperPath() + File.separator;
    }

    public static String cleanTitle(String title) {
        title = title.replace(' ', '_')
                .replaceAll("[\\W]", "");
        title = title.substring(0, Math.min(MAX_TITLE_SIZE, title.length()));
        return title;
    }

    public boolean delete() {
        return Objects.requireNonNull(file).delete();
    }
}
