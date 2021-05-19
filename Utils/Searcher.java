package Utils;

import Settings.Settings;
import Wallpaper.Wallpaper;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

class Searcher {
    private final Settings settings;
    private String searchQuery;
    private Map<String, Wallpaper> proposed;
    private static final Logger log = DisplayLogger.getInstance("Searcher");
    private static final int QUERY_SIZE = 50;

    public Searcher(Settings settings) {
        this.settings = settings;
    }

    /**
     * It generates a search query for reddit query API
     */
    void generateSearchQuery() {
        searchQuery =
                "https://reddit.com/search.json?"
                + "q=" + generateQuery()
                + "&sort=" + settings.getSearchBy().value //how to sort them (hot, new ...)
                + "&limit=" + QUERY_SIZE //how many posts
                + "&t=" + settings.getMaxOldness().value //how old can a post be at most
                + "&type=t3" //only link type posts, no text-only
                + "&restrict_sr=true" //i don't think it's useful but still have to figure out what it does
        ;
        log.log(Level.INFO, "Search Query is: "+ searchQuery);
    }

    /**
     * generates a part of the search query
     * @return
     */
    String generateQuery() {
        //Could be removed?
        String s =
                "title:("
                + String.join(" OR ", settings.getTitles()).replace("  ", " ")
                + ")+subreddit:("
                + String.join(" OR ", settings.getSubreddits()).replace("  ", " ")
                + ")"
                //+ "+nsfw:" // if true shows nsfw ONLY
                //+ (settings.isNsfwOnly()?"yes":"no")
                + "+self:no" //this means no text-only posts
                // TODO add flairs?
                ;
        s = s.replace("title:()+", "").replace("subreddits:()+", "");
        //Removes title and subreddit field if they are void
        //What happens if some dumbhead tries to put as keyword to search "title:() " or "subreddits:() "? Will it just break the program? Is this some sort of hijackable thing?
        //I don't know for I am too dumb
        return encodeURL(s);
    }

    /**
     * Manages the connection, connects to reddit, download the whole JSON, and refines it to make it useful
     * @return the JSON containing the db with search results
     * @throws IOException if unable to connect or download the JSON. Reasons: bad internet connection, dirty input string (smth like trying to research for "//" or "title:() "
     */
    public Map<String, Wallpaper> getSearchResults() throws IOException {
        if (proposed == null) {
            URLConnection connect = initializeConnection();
            String rawData = getRawData(connect);
            //System.out.println(rawData);
            proposed =  refineData(rawData);
        }
        return proposed;
    }

    private URLConnection initializeConnection() throws IOException {
        URLConnection connection = new URL(searchQuery).openConnection();
        connection.setRequestProperty("User-Agent", "wannabe wallpaper bot");
        connection.setRequestProperty("Accept-Charset", StandardCharsets.UTF_8.name());
        return connection;
    }

    private String getRawData(URLConnection connection) throws IOException {
        // gets the raw JSON file in String form
        Scanner s = new Scanner(connection.getInputStream()).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private Map<String,Wallpaper> refineData(String rawData) throws IOException {
        // converts the String JSON into a HashMap JSON, then selects the only things
        // we are interested in: the ID and the photo link
        HashMap<String,Object> result = null;
        result = new ObjectMapper().readValue(rawData, HashMap.class);
        ArrayList<HashMap> children = (ArrayList<HashMap>) ((HashMap<String, Object>) result.get("data")).get("children");

        HashMap<String, Wallpaper> res = new HashMap<>();
        for (int i=0; i<children.size(); i++) {
            // some posts can be in the form of galleries of wallpapers.
            // such cases can be detected when the url contains the word "gallery" TODO there must be a better way
            // it appears that there's a field "is_gallery" but there's only in gallery types sooo??
            // in such cases we are going to
            HashMap<String, Object> child = (HashMap<String, Object>) ( (HashMap<String, Object>) children.get(i)).get("data");
            String url = (String) child.get("url");
            String title = (String) child.get("title");
            String permalink = (String) child.get("permalink");
            String id = (String) child.get("id");
            if (url.contains("gallery")) {
                //we are going to add all the posts from this gallery
                //note that in this way the proposed wallpapers will be more than the QUERY_SIZE (TODO Is this a correct method?)
                HashMap<String, Object> media_metadata = (HashMap<String, Object>) child.get("media_metadata");
                int j=0;
                for (String idGallery : media_metadata.keySet()) {
                    HashMap<String, Object> galleryItem = (HashMap<String, Object>) media_metadata.get(idGallery);
                    String type = (String) galleryItem.get("m");
                    type = type.replace("image/", "");

                    String urlGallery = "https://i.redd.it/" + idGallery + "." + type;
                    String titleGallery = title + j;
                    if (title.length()>30) {
                        titleGallery = title.substring(0,30) + j;
                    }
                    j++;

                    Wallpaper wallpaper = new Wallpaper(
                            titleGallery,
                            urlGallery,
                            permalink
                    );
                    int height = (int) ((HashMap<String, Object>) galleryItem.get("s")).get("y");
                    int width = (int) ((HashMap<String, Object>) galleryItem.get("s")).get("x");

                    if (settings.getWidth() <= width && settings.getHeight() <= height) {
                        res.put(idGallery, wallpaper);
                    } else {
                        log.log(Level.FINE,
                                "Detected wallpaper not compatible with screen dimensions: "
                                        + width + "x" + height
                                        +  " Instead of "
                                        + settings.getWidth() + "x" + settings.getHeight()
                                        + ": "+ wallpaper.getPostUrl()
                                        + ". Searching for another..."
                        );
                    }

                }
            } else {
                //case in which there's a single wallpaper (not a gallery)
                Wallpaper wallpaper = new Wallpaper(
                        title,
                        url,
                        permalink
                );
                //this mess/nightmare is only fault of reddit nested JSON. I don't think there's a better way to do this

                try {
                    HashMap<String, Object> preview = ((HashMap<String, Object>) child.get("preview"));
                    //System.out.println(i + ": " + url + "\n "+ title + permalink);
                    ArrayList<HashMap> images = (ArrayList<HashMap>) preview.get("images");
                    HashMap<String, Object> zero = images.get(0);
                    HashMap<String, Object> source = (HashMap<String, Object>) zero.get("source");

                    int height = (int) source.get("width");
                    int width = (int) source.get("height");

                    if (settings.getWidth() <= width && settings.getHeight() <= height) {
                        res.put(id, wallpaper);
                    } else {
                        log.log(Level.FINE,
                                "Detected wallpaper not compatible with screen dimensions: "
                                        + width + "x" + height
                                        +  " Instead of "
                                        + settings.getWidth() + "x" + settings.getHeight()
                                        + ": "+ wallpaper.getPostUrl()
                                        + ". Searching for another..."
                        );
                    }
                        //TODO should I merge this repeating part with the part above? they are really similar

                } catch (NullPointerException e) {
                    // I still have to figure out why sometimes it gives this error for no reason
                    // It says that it can't find the "preview" field of the child but it's there
                    log.log(Level.WARNING, "Could not read input wallpaper");

                }

            }
        }
        return res;
    }

    private static String encodeURL(String value) {
        return value.replace(" ", "%20");
//        try {
//            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString()).replace("+", "%20");
//            //if you leave + as space sign it's counted as AND by reddit query, so you must use %20
//        } catch (UnsupportedEncodingException ex) {
//            throw new RuntimeException(ex.getCause());
//        }
    }

    // Getter

    public String getSearchQuery() {
        return searchQuery;
    }

}
