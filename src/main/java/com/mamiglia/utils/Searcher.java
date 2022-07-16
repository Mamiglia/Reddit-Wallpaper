package com.mamiglia.utils;

import com.mamiglia.settings.Destination;
import com.mamiglia.settings.NSFW_LEVEL;
import com.mamiglia.settings.RATIO_LIMIT;
import com.mamiglia.settings.Source;
import com.mamiglia.wallpaper.Wallpaper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mamiglia.settings.SettingsKt.REG_WS;

class Searcher {
	private final Set<Source> sources;
	private Set<Wallpaper> proposed;
	private static final Logger log = LoggerFactory.getLogger("Searcher");
	private static final int QUERY_SIZE = 50;

	public Searcher(Set<Source> sources) {
		this.sources = sources;
	}

	/**
	 * It generates a search query for reddit query API
	 */
	String generateSearchQuery(Source src) {
		StringBuilder strQuery = new StringBuilder("https://reddit.com/r/");

		//Query now builds a multireddit out of listed subreddits, this should prevent issues with very large lists of subs
		strQuery.append(String.join("+", src.getSubreddits()).replaceAll(REG_WS, ""));

		strQuery.append("/search.json?q=");

		// Subreddit selection is made through the multireddit build

		var insideQuery = new StringBuilder("(");

		if (!src.getTitles().isEmpty()) {
			insideQuery.append("title:(\"")
					.append(String.join("\" OR \"", src.getTitles()))
					.append("\")");
		}

		if (!src.getFlairs().isEmpty()) {
			insideQuery.append(" flair:(\"")
					.append(String.join("\" OR \"", src.getFlairs()))
					.append("\")");
		}
		insideQuery.append(")");
		strQuery.append(encodeValue(insideQuery.toString()));

		strQuery.append(")&self:no" //this means no text-only posts
				+ "&sort=").append(src.getSearchBy().getValue() //how to sort them (hot, new ...)
		).append("&limit=").append(QUERY_SIZE //how many posts
		).append("&t=").append(src.getMaxOldness().getValue() //how old can a post be at most
		).append("&type=t3" //only link type posts, no text-only
		).append("&restrict_sr=true" //restrict results to defined subreddits (leave on true)
		).append(src.getNsfwLevel().getQuery()).append("&rawjson=1");
		//Removes title and flair field if they are void and somehow made it in
		//What happens if some dumbhead tries to put as keyword to search "title:() "
		//or "flair:() "? Will it just break the program? Is this some sort of hijackable thing?
		//I don't know for I myself am too dumb - Don't be so hard on yourself <3 - Thanks bro <3

		log.info("Search Query for source {}", src.getName() + " is: "+ strQuery);
		return strQuery.toString();
	}

	/**
	 * Manages the connection, connects to reddit, download the whole JSON, and refines it to make it useful
	 * @return the JSON containing the db with search results
	 * @throws IOException if unable to connect or download the JSON. Reasons: bad internet connection, dirty input string something like trying to research for "//" or "title:()"
	 */
	public Set<Wallpaper> getSearchResults() throws IOException {
		if (proposed == null) {
			proposed= new HashSet<>();
			if (sources.isEmpty()) {
				log.error("Sources list is void");
				return null;
			}
			for (Source src: sources) {
				URLConnection connect = initializeConnection(generateSearchQuery(src));
				String rawData = getRawData(connect);
				proposed.addAll(refineData(rawData, src));
			}
		}
		return proposed;
	}

	private URLConnection initializeConnection(String searchQuery) throws IOException {
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

	private Set<Wallpaper> refineData(String rawData, Source src) {
		// converts the String JSON into a HashMap JSON, then selects the only things
		// we are interested in: the ID and the photo link
		Set<Wallpaper> res = new HashSet<>();
		Wallpaper wallpaper;
		String id;
		String title;
		String url;
		String permalink;
		JSONObject child;
		int score;
		boolean is_over_18;

		if (rawData.contains("error")) { // FIX What happens if someone puts "error" in the title of their post?
			log.warn("Reddit returned an error:\n{}", rawData);
			return res; // res is void at this stage
		}

		JSONArray children = new JSONObject(rawData).getJSONObject("data").getJSONArray("children");

		for (int i = 0; i < children.length(); i++) {
			child = children.getJSONObject(i).getJSONObject("data");

			// If the post isn't an image, we don't want it
			if (child.keySet().contains("post_hint")) { // gallaries don't have a post hint
				if (!child.getString("post_hint").equals("image")) {
					log.debug("This post isn't a valid wallpaper. Skipping.");
					continue;
				}
			}

			score = child.getInt("score"); // # of upvotes
			is_over_18 = child.getBoolean("over_18");

			if (score < src.getMinScore() || (!is_over_18 && src.getNsfwLevel() == NSFW_LEVEL.ONLY)) {
				// when a post has too few upvotes it's skipped
				// or if only over_18 content is allowed - no need to check in the other sense, because the query excludes them
				continue;
			}

			title = child.getString("title");
			permalink = child.getString("permalink");
			url = child.getString("url");
			id = child.getString("id");

			if (child.keySet().contains("crosspost_parent_list")) {
				// some posts are crossposts
				// reassign variables with correct values for crosspost
				child = child.getJSONArray("crosspost_parent_list").getJSONObject(0);
				title = child.getString("title");
				permalink = child.getString("permalink");
				url = child.getString("url");
				id = child.getString("id");
			}

			// hand off gallery as soon as possible
			// some posts can be in the form of galleries of wallpapers
			if (child.keySet().contains("is_gallery")) {
				//we are going to add all the posts from this gallery
				//note that in this way the proposed wallpapers will be slightly more than the QUERY_SIZE
				res.addAll(
						processGallery(
								child.getJSONObject("media_metadata"),
								title,
								permalink));
				continue;
			}

			// if the url doesn't have a file extension (a web page)
			if (!(url.matches("(.*)\\.\\w+"))) {
				log.debug("This post isn't a valid wallpaper. Skipping.");
				continue;
			}

			// preview keyword is required for the rest of the json handling. If preview is missing the program will
			// throw an error. Easiest just to exclude these results
			if (!child.keySet().contains("preview")) {
				log.debug("This entry is problematic. Skipping.");
				continue;
			}

			child = child
					.getJSONObject("preview")
					.getJSONArray("images")
					.getJSONObject(0)
					.getJSONObject("source");

//			if (imageSize(, , url)) {
//				// if image doesn't meet minimum size, restart loop
//				continue;
//			}
			// TODO move this check in selector

			wallpaper = new Wallpaper(id, title, url, permalink, child.getInt("width"), child.getInt("height"));
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

			titleGallery = title + " " + id;
			wallpaper = new Wallpaper(id, titleGallery, url, permalink, width, height);
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
	private boolean imageSize(int x, int y, String url, Destination dest) {
		int width = dest.getWidth();
		int height = dest.getHeight();
		float ratio = (float) width/height;
		if ((width > x || height > y) || // image doesn't meet resolution
				(ratio != (float) x/y && dest.getRatioLimit() == RATIO_LIMIT.STRICT) || // image doesn't meet exact screen ratio
				(((ratio > 1 && 1 > (float) x/y) || (ratio < 1 && 1 < (float) x/y)) && dest.getRatioLimit() == RATIO_LIMIT.RELAXED)) { // image isn't somewhere between screen ratio and square
			log.debug(
					"Detected wallpaper not compatible with screen dimensions: {}x{} ratio: {} Instead of {}x{} ratio: {}. Searching for another...",
					x, y, x / y, width, height, ratio
				);
			log.debug("Wallpaper rejected was: {}", url);
			// if the image is rejected
			return true;
		}
		// if the image is accepted
		return false;
	}


	private static String encodeValue(String value) {
		try {
			return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex.getCause());
		}
	}
}