import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

class Wallpaper {
    private final String title;
    private final String url;
    private final String thumbnailUrl;
    private final String postUrl;
    private Image wallpaper;
    private Image thumbnail;


    public Wallpaper(String title, String url, String thumbnailUrl, String postUrl) {
        this.title = title;
        this.url = url;
        this.thumbnailUrl = thumbnailUrl;
        this.postUrl = postUrl;
    }

    public void download() throws IOException {
        wallpaper = ImageIO.read(new URL(url));
        thumbnail = ImageIO.read(new URL(thumbnailUrl));
        saveImage(wallpaper, false);
        saveImage(thumbnail, true);
    }

    public void saveImage(Image img, boolean thumbnail) throws IOException {
        String path;
        if (thumbnail == true) {
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
        ImageIO.write(bi, "jpg", new File(path + title +".jpg"));
    }
    // GETTERS
    public String getPath() {
        return "wallpapers/" + title + ".jpg";
    }
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

    public Image getWallpaper() {
        return wallpaper;
    }

    public Image getThumbnail() {
        return thumbnail;
    }
}
