import com.fasterxml.jackson.databind.*;

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


public class Searcher {
    final static String SEARCH_BY_TOP = "top";
    final static String SEARCH_BY_NEW = "new";
    final static String SEARCH_BY_HOT = "hot";
    final static String SEARCH_BY_RELEVANCE = "relevance";
    private final String[] titles;
    private final String[] subreddits;
    private final int length;
    private final int height;
    private final boolean nsfw;
    private final String searchBy;
    private String searchQuery;

    public Searcher(
            String[] title,
            String[] subreddits,
            int length,
            int height,
            boolean nsfw,
            String searchBy) {
        this.titles = title;
        this.subreddits = subreddits;
        this.length = length;
        this.height = height;
        this.nsfw = nsfw;
        this.searchBy = searchBy;
    }

    void generateSearchQuery() {
        searchQuery = "" +
                "https://reddit.com/search.json?"
                + "q="
                + generateQuery()
                + "&sort="
                + searchBy
                + "&limit=20"
                + "&t=day"
                + "&type=t3"
                // + "&restrict_sr=true" i don't think it's useful
        ;
        }

    String generateQuery() {
        String s =
                "(title:("
                + String.join(" OR ", titles)
                + ") subreddit:("
                + String.join(" OR ", subreddits)
                + ") nsfw:" // if true shows nsfw ONLY
                + (nsfw?"yes":"no")
                + " self:no)" //this means no text-only posts
                // add flairs?
                ;
        return encodeURL(s);
    }

    public Map<String, String> getSearchResults() throws IOException {
        URLConnection connection = new URL(searchQuery).openConnection();
        connection.setRequestProperty("User-Agent", "wannabe wallpaper bot");
        connection.setRequestProperty("Accept-Charset", StandardCharsets.UTF_8.name());

        Scanner s = new Scanner(connection.getInputStream()).useDelimiter("\\A");
        String s1 = s.hasNext() ? s.next() : "";

        Map<String,Object> result =
                new ObjectMapper().readValue(s1, HashMap.class);
        System.out.println(result.get("data").getClass());
        ArrayList<Map> children = (ArrayList<Map>) ((Map<String, Object>) result.get("data")).get("children");

        Map<String, String> res = new HashMap<String, String>();
        for (int i=0; i<children.size(); i++) {
            Map<String, Object> child = (Map<String, Object>) ( (Map<String, Object>) children.get(i)).get("data");
            res.put((String) child.get("id"), (String) child.get("url"));
        }

        return res;
    }

    private static String encodeURL(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString()).replace("+", "%20");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

    // Getter
    public String[] getTitle() {
        return titles;
    }

    public String[] getSubreddits() {
        return subreddits;
    }

    public int getLength() {
        return length;
    }

    public int getHeight() {
        return height;
    }

    public boolean isNsfw() {
        return nsfw;
    }

    public String getSearchBy() {
        return searchBy;
    }

    public String getSearchQuery() {
        return searchQuery;
    }
}
