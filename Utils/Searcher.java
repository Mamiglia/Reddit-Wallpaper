package Utils;

import GUI.Settings;
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
    private static final Logger log = Logger.getLogger("Searcher");

    public Searcher(Settings settings) {
        this.settings = settings;
    }

    /**
     * It generates a search query for reddit query API
     */
    void generateSearchQuery() {
        searchQuery = "" +
                "https://reddit.com/search.json?"
                + "q="
                + generateQuery()
                + "&sort=" //how to sort them (hot, new ...)
                + translate(settings.getSearchBy())
                + "&limit=20" //how many posts
                // + "&t=day" //how old can a post be at most
                + "&type=t3" //only link type posts, no text-only
                // + "&restrict_sr=true" i don't think it's useful but still have to figure out what it does
        ;
    }

    /**
     * generates a part of the search query
     * @return
     */
    String generateQuery() {
        //Could be removed?
        String s =
                "(title:("
                + String.join(" OR ", settings.getTitles())
                + ") subreddit:("
                + String.join(" OR ", settings.getSubreddits())
                + ") nsfw:" // if true shows nsfw ONLY
                + (settings.isNsfwOnly()?"yes":"no")
                + " self:no)" //this means no text-only posts
                // TODO add flairs?
                ;
        s = s.replace("title:() ", "").replace("subreddits:() ", "");
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
            proposed =  refineData(rawData);
        }
        log.log(Level.INFO, "Search Result is " + proposed.toString());
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
            HashMap<String, Object> child = (HashMap<String, Object>) ( (HashMap<String, Object>) children.get(i)).get("data");
            Wallpaper wallpaper = new Wallpaper(
                    (String) child.get("title"),
                    (String) child.get("url"),
                    (String) child.get("permalink")
            );
            res.put((String) child.get("id"), wallpaper);
        }
        return res;
    }

    private static String encodeURL(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString()).replace("+", "%20");
            //if you leave + as space sign it's counted as AND by reddit query, so you must use %20
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

    // Getter

    public String getSearchQuery() {
        return searchQuery;
    }

    public static String translate(GetNewWallpaper.SEARCH_BY searchBy) {
        String s = null;
        switch (searchBy) {
            case HOT:
                s = "hot";
                break;
            case NEW:
                s = "new";
                break;
            case TOP:
                s = "top";
                break;
            case RELEVANCE:
                s = "relevance";
                break;
            default:
                System.err.println("Impossible error!");
                // should never happen to get here
            }
        return s;
    }
}
