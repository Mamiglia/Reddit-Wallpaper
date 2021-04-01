package Utils;

import Wallpaper.Wallpaper;

import java.io.IOException;
import java.util.Map;

public class GetNewWallpaper implements Runnable {
	public enum SEARCH_BY {TOP, NEW, HOT, RELEVANCE}
	private boolean executed = false;

	private final String[] titles;
	private final String[] subreddits;
	private final int width;
	private final int height;
	private final boolean nsfw;
	private final SEARCH_BY searchBy;

	private Wallpaper result;

	public GetNewWallpaper(String[] titles, String[] subreddits, int width, int height, boolean nsfw, SEARCH_BY searchBy) {
		this.titles = titles;
		this.subreddits = subreddits;
		this.width = width;
		this.height = height;
		this.nsfw = nsfw;
		this.searchBy = searchBy;
	}


	@Override
	public void run() {
		if (executed) return;
		executed = true;

		Searcher s = new Searcher(titles, subreddits, width, height, nsfw, searchBy);
		s.generateSearchQuery();
		//		System.out.println(s.getSearchQuery());
		s.getSearchQuery();
		Map<String, Wallpaper> wallpapers = null;
		try {
			s.getSearchResults();
			wallpapers = s.getSearchResults();
		} catch (IOException e) {
			System.err.println("Couldn't download the object, Internet error or Invalid input");
		}

		if (wallpapers == null) return;

		//SELECTOR
		Wallpaper w = null;
		try {
			Selector selector = new Selector(wallpapers);
			w = selector.select();
			w.download();
		} catch (IOException e) {
			System.err.println("Couldn't download the image and/or update the database");
		}
		result = w;
	}

	public Wallpaper getResult() {
		return result;
	}
}
