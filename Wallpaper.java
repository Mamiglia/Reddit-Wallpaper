import java.awt.*;

class Wallpaper {
    private final String title;
    private final String url;
    private final String thumbnailUrl;
    private final String postUrl;
    private String location;
    private String thumbnailLocation;
    private Image wallpaper;
    private Image thumbnail;


    public Wallpaper(String title, String url, String thumbnailUrl, String postUrl) {
        this.title = title;
        this.url = url;
        this.thumbnailUrl = thumbnailUrl;
        this.postUrl = postUrl;
    }


    // GETTERS
    public String getTitle() {
        return title;
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

    public String getLocation() {
        return location;
    }

    public String getThumbnailLocation() {
        return thumbnailLocation;
    }

    public Image getWallpaper() {
        return wallpaper;
    }

    public Image getThumbnail() {
        return thumbnail;
    }
}
