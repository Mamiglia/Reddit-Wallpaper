import java.io.IOException;
import java.util.HashMap;

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
        HashMap<String, Wallpaper> wallpapers = s.getSearchResults();

        //SELECTOR
        try {
            Selector selector = new Selector(wallpapers);
            Wallpaper w = selector.select();
            System.out.println(w.toString());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


    }


}
