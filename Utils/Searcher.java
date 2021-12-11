package Utils;

import Settings.Settings;
import Wallpaper.Wallpaper;
import org.json.JSONArray;
import org.json.JSONObject;

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
	private static final int MINIMUM_NUMBER_OF_UPVOTES = 15; // Number to not pick indecent wallpapers. This number is completely arbitrary, but it should be sufficient

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
		temp = String.join("+", settings.getSubreddits()).replace(" ", ""); //@Mamiglia please check if this should work
		if (!test.equals(temp)) {
			searchQuery += temp + "/";
		}
		searchQuery += "search.json?q=";

		for (int i = 0; i < 2; i++) { // this loop should only ever run twice per wallpaper change
			if (i == 0) {// && !test.equals(temp)) { // if this is the first loop and the titles string isn't empty
				// build temp string with title data for first loop
				temp = String.join(" OR ", settings.getTitles()).replace("  ", " ");
				if (!temp.equals(test)) {
					searchQuery += "title:(" + temp + ")&";
				}

			}
			else if (i == 1) { // if this is the second loop and the flair string isn't empty
				// build temp string with flair data for second loop
				temp = String.join("\" OR \"", settings.getFlair()).replace("  ", " ");
				if (!temp.equals(test)) {

					searchQuery += "flair:(\"" + temp + "\")&";
				}
				/* A note on flairs:
					Flairs can be mulitword if they are wrapped in quotation marks. This code should work
					for any single word flairs but extra work would need to be put in to handle multiword flairs.
					TODO finish flair implementation -Iinfragon
				*/
			}
			else break;

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
		//What happens if some dumbhead tries to put as keyword to search "title:() " or "flair:() "? Will it just break the program? Is this some sort of hijackable thing?
		//I don't know for I myself am too dumb - Don't be so hard on yourself <3

		log.log(Level.INFO, () -> "Search Query is: "+ searchQuery);
	}
/*	Removed this portion for now as it is all handled above - Iinfragon

	private String generateQuery() {
		//Could be inserted in the other?
		String s =
				"title:("
						+ String.join(" OR ", settings.getTitles()).replace("  ", " ")
						//+ ")+subreddit:("
						//+ String.join(" OR ", settings.getSubreddits()).replace("  ", " ")
						+ ")&"
				// TODO add flairs?
				;
		s = s.replace("title:()&", "");//.replace("subreddits:()+", "");
		//Removes title and subreddit field if they are void
		//What happens if some dumbhead tries to put as keyword to search "title:() " or "subreddits:() "? Will it just break the program? Is this some sort of hijackable thing?
		//I don't know for I myself am too dumb - Don't be so hard on yourself <3
		return encodeURL(s);
	}*/

	/**
	 * Manages the connection, connects to reddit, download the whole JSON, and refines it to make it useful
	 * @return the JSON containing the db with search results
	 * @throws IOException if unable to connect or download the JSON. Reasons: bad internet connection, dirty input string (smth like trying to research for "//" or "title:() "
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
		JSONArray children = new JSONObject(rawData).getJSONObject("data").getJSONArray("children");

		Set<Wallpaper> res = new HashSet<>();
		for (int i=0; i<children.length(); i++) {
			JSONObject child = children.getJSONObject(i).getJSONObject("data");
			int score = child.getInt("score"); // # of upvotes
			boolean is_over_18 = child.getBoolean("over_18");
			if (score < MINIMUM_NUMBER_OF_UPVOTES || (!is_over_18 && settings.getNsfwLevel() == Settings.NSFW_LEVEL.ONLY)) {
				// when a post has too few upvotes it's skipped
				// or if only over_18 content is allowed		- no need to check in the other sense, because the query excludes them
				continue;
			}

			String url = child.getString("url");
			String title = child.getString("title");
			String permalink = child.getString("permalink");
			String id = child.getString("id");



			if (child.keySet().contains("crosspost_parent_list")) {
				// some posts are crossposts
				child = child.getJSONArray("crosspost_parent_list").getJSONObject(0);
			}

			// some posts can be in the form of galleries of wallpapers.
			// in such cases we are going to
			if (child.keySet().contains("is_gallery")) {
				//we are going to add all the posts from this gallery
				//note that in this way the proposed wallpapers will be slightly more than the QUERY_SIZE
				processGallery(
						child.getJSONObject("media_metadata"),
						title,
						permalink,
						res);

			} else {
				//case in which there's a single wallpaper (not a gallery)
				Wallpaper wallpaper = new Wallpaper(
						id,
						title,
						url,
						permalink
				);
				//this mess/nightmare is only fault of reddit nested JSON. I don't think there's a better way to do this

				JSONObject source =  child
						.getJSONObject("preview")
						.getJSONArray("images")
						.getJSONObject(0)
						.getJSONObject("source");

				int width = source.getInt("width");
				int height = source.getInt("height");

				if (settings.getWidth() <= width && settings.getHeight() <= height) {
					res.add(wallpaper);
				} else {
					log.log(Level.FINE, () ->
							"Detected wallpaper not compatible with screen dimensions: "
									+ width + "x" + height
									+  " Instead of "
									+ settings.getWidth() + "x" + settings.getHeight()
									+ ". Searching for another..."
					);
					log.log(Level.FINER, () -> "Wallpaper rejected was: " + wallpaper.getPostUrl());
				}
				//TODO should I merge this repeating part with the part above? they are really similar
			}
		}
		return res;
	}

	private void processGallery(JSONObject mediaMetadata, String title, String permalink, Set<Wallpaper> res) {
		int j=0;
		for (String idGallery : mediaMetadata.keySet()) {
			JSONObject galleryItem = mediaMetadata.getJSONObject(idGallery);
			int height = galleryItem.getJSONObject("s").getInt("y");
			int width = galleryItem.getJSONObject("s").getInt("x");
			if (settings.getWidth() > width || settings.getHeight() > height) {
				log.log(Level.FINE, () ->
						"Detected wallpaper not compatible with screen dimensions: "
								+ width + "x" + height
								+  " Instead of "
								+ settings.getWidth() + "x" + settings.getHeight()
								+ ". Searching for another..."
				);
				return;
			}

			String type = galleryItem.getString("m");
			type = type.replace("image/", "");

			String urlGallery = "https://i.redd.it/" + idGallery + "." + type;
			String titleGallery = title + " " + idGallery;

			Wallpaper wallpaper = new Wallpaper(
					idGallery,
					titleGallery,
					urlGallery,
					permalink
			);

			res.add(wallpaper);
		}
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
