package Utils;

import Settings.Settings;
import Wallpaper.Wallpaper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

class Searcher {
	private static final int MINIMUM_NUMBER_OF_UPVOTES = 15; // Number to not pick indecent wallpapers. This number is completely arbitrary, but it should be sufficient TODO could allow users to define minimum upvotes in the UI
	private final Settings settings;
	private String searchQuery = "";
	private Set<Wallpaper> proposed;
	private static final Logger log = DisplayLogger.getInstance("Searcher");
	private static final int QUERY_SIZE = 50;

	public Searcher(Settings settings) {
		this.settings = settings;
	}

	/**
	 * It generates a search query for reddit query API
	 */
	void generateSearchQuery() {
		String temp; //temporary holder for title or flair portion of query
		String test = ""; // populated to test if the temp field has anything added from getTitles() or getFlair()

 		searchQuery =
		//Query now builds a multisub out of listed subreddits, this should prevent issues with very large lists of subs
				"https://reddit.com/r/";

		 // TODO is there a better way to do this?
		// it feels redundant calling the same if check 3 times
		temp = String.join("+", settings.getSubreddits()).replaceAll(Settings.getRegWS(), "");
		if (!temp.equals(test)) {
			searchQuery += temp + "/";
		}
		searchQuery += "search.json?q=";

		// build temp string with title data
		temp = String.join("\" OR \"", settings.getTitles()).replaceAll(Settings.getRegWS(), "");
		if (!temp.equals(test)) {
			searchQuery += "title:(\"" + temp + "\")&";
		}

		// build temp string with flair data
		temp = String.join("\" OR \"", settings.getFlair()).replaceAll(Settings.getRegWS(), "");
		if (!temp.equals(test)) {
		// Flair string is delimited by commas and automatically wrapped in quotation marks to handle multi-word flairs
			searchQuery += "flair:(\"" + temp + "\")&";
		}

		searchQuery +=
			"self:no" //this means no text-only posts
				+ "&sort=" + settings.getSearchBy().value //how to sort them (hot, new ...)
				+ "&limit=" + QUERY_SIZE //how many posts
				+ "&t=" + settings.getMaxOldness().value //how old can a post be at most
				+ "&type=t3" //only link type posts, no text-only
				+ "&restrict_sr=true" //restrict results to defined subreddits (leave on true)
				+ settings.getNsfwLevel().query
		;
		
		searchQuery = searchQuery.replace("flair:()&", "");
		searchQuery = searchQuery.replace("title:()&", "");
		//Removes title and flair field if they are void and somehow made it in
		//What happens if some dumbhead tries to put as keyword to search "title:() "
		//or "flair:() "? Will it just break the program? Is this some sort of hijackable thing?
		//I don't know for I myself am too dumb - Don't be so hard on yourself <3

		log.log(Level.INFO, () -> "Search Query is: "+ searchQuery);
	}

	/**
	 * Manages the connection, connects to reddit, download the whole JSON, and refines it to make it useful
	 * @return the JSON containing the db with search results
	 * @throws IOException if unable to connect or download the JSON. Reasons: bad internet connection, dirty input string something like trying to research for "//" or "title:()"
	 */
	public Set<Wallpaper> getSearchResults() throws IOException {
		if (proposed == null) {
			URLConnection connect = initializeConnection();
			String rawData = getRawData(connect);
			proposed =  refineData(rawData);
		}
		return proposed;
	}

	private URLConnection initializeConnection() throws IOException {
		URLConnection connection = new URL(searchQuery).openConnection();
		connection.setRequestProperty("User-Agent", "Reddit-Wallpaper bot");
		connection.setRequestProperty("Accept-Charset", StandardCharsets.UTF_8.name());
		return connection;
	}

	private String getRawData(URLConnection connection) throws IOException {
		// gets the raw JSON file in String form
		Scanner s = new Scanner(connection.getInputStream()).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	private Set<Wallpaper> refineData(String rawData) {
		// converts the String JSON into a HashMap JSON, then selects the only things
		// we are interested in: the ID and the photo link
		Set<Wallpaper> res = new HashSet<>();
		Set<Wallpaper> gallery;
		Wallpaper wallpaper;
		String id;
		String title;
		String url;
		String permalink;
		JSONObject child;
		int score;
		boolean is_over_18;

		// check if Reddit returned an error json
		if (new JSONObject(rawData).keySet().contains("error")) {
			log.log(Level.FINE, () ->
				"Reddit returned error "
						+ new JSONObject(rawData).getJSONObject("error").getJSONObject("message")
						+ ". This can happen if you don't have any subbredits listed. "
						+ "Please check your settings and try again."
			);
			return res;
		}

		JSONArray children = new JSONObject(rawData).getJSONObject("data").getJSONArray("children");

		for (int i = 0; i < children.length(); i++) {
			child = children.getJSONObject(i).getJSONObject("data");
			score = child.getInt("score"); // # of upvotes
			is_over_18 = child.getBoolean("over_18");

			if (score < MINIMUM_NUMBER_OF_UPVOTES || (!is_over_18 && settings.getNsfwLevel() == Settings.NSFW_LEVEL.ONLY)) {
				// when a post has too few upvotes it's skipped
				// or if only over_18 content is allowed - no need to check in the other sense, because the query excludes them
				continue;
			}

			title = child.getString("title");
			permalink = child.getString("permalink");

			// hand off gallery as soon as possible
			// some posts can be in the form of galleries of wallpapers
			if (child.keySet().contains("is_gallery")) {
				//we are going to add all the posts from this gallery
				//note that in this way the proposed wallpapers will be slightly more than the QUERY_SIZE
				gallery = processGallery(
						child.getJSONObject("media_metadata"),
						title,
						permalink);
				res.addAll(gallery);
				continue;
			}

			id = child.getString("id");
			url = child.getString("url");

			if (child.keySet().contains("crosspost_parent_list")) {
				// some posts are crossposts
				// reassign variables with correct values for crosspost
				child = child.getJSONArray("crosspost_parent_list").getJSONObject(0);
				title = child.getString("title");
				permalink = child.getString("permalink");
				url = child.getString("url");
				id = child.getString("id");
			}

			child = child
					.getJSONObject("preview")
					.getJSONArray("images")
					.getJSONObject(0)
					.getJSONObject("source");

			if (imageSize(child.getInt("width"), child.getInt("height"), url)) {
				// if image doesn't meet minimum size, restart loop
				continue;
			}

			wallpaper = new Wallpaper(id, title, url, permalink);
			res.add(wallpaper);
		}
		return res;
	}

	private Set<Wallpaper> processGallery(JSONObject mediaMetadata, String title, String permalink) {
		Wallpaper wallpaper;
		Set<Wallpaper> res = new HashSet<>();
		JSONObject item;
		int height;
		int width;
		String type;
		String url;
		String titleGallery;

		for (String id : mediaMetadata.keySet()) {
			item = mediaMetadata.getJSONObject(id);
			height = item.getJSONObject("s").getInt("y");
			width = item.getJSONObject("s").getInt("x");
			type = item.getString("m");
			type = type.replace("image/", "");
			url = "https://i.redd.it/" + id + "." + type;

			if (imageSize(width, height, url)) {
				continue;
			}

			titleGallery = title + " " + id;
			wallpaper = new Wallpaper(id, titleGallery, url, permalink);
			res.add(wallpaper);
		}
		return res;
	}

	/**
	 * Tests to see if an image is an acceptable size
	 * Returns false if is at least minimum size
	 * Returns true if image is too small
	 * It seems a bit backwards but it prevents using a negative modifier on every portion that I've used it
	 * URL is required for output to the log
	 */
	private boolean imageSize(int x, int y, String url) {
		if (settings.getWidth() > x || settings.getHeight() > y) {
			log.log(Level.FINE, () ->
				"Detected wallpaper not compatible with screen dimensions: "
					+ x + "x" + y
					+  " Instead of "
					+ settings.getWidth() + "x" + settings.getHeight()
					+ ". Searching for another..."
				);
			log.log(Level.FINER, () -> "Wallpaper rejected was: " + url);
			// if the image is too small
			return true;
		}
		// if the image is large enough
		return false;
	}

}