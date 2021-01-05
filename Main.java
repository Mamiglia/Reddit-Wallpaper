import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws IOException {
        String[] title = {"cat", "dog"};
        String[] subreddits = {"wallpapers", "dankmemes"};
        int length = 1, height = 1;
        boolean nsfw = false;
        String searchBy = Searcher.SEARCH_BY_HOT;
        Searcher s = new Searcher(title, subreddits, length, height, nsfw, searchBy);
        s.generateSearchQuery();
        System.out.println(s.getSearchQuery());
        System.out.println(s.getSearchResults());
        Map<String, Wallpaper> wallpapers = s.getSearchResults();

        //SELECTOR
        ArrayList<String> ids = new ArrayList<>();
        ids.addAll(wallpapers.keySet());
        Wallpaper test = wallpapers.get(ids.get(0));



    }


}
