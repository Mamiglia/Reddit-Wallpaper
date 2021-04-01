import Utils.GetNewWallpaper;
import Utils.SetNewWallpaper;
import Wallpaper.Wallpaper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

public class Main {

    public static void main(String[] args) {

        String[] title = {};
        String[] subreddits = {"wallpapers", "wallpaper", "worldpolitics"};
        int length = 1, height = 1;
        boolean nsfw = false;
        GetNewWallpaper.SEARCH_BY searchBy = GetNewWallpaper.SEARCH_BY.HOT;

        GetNewWallpaper g = new GetNewWallpaper(title, subreddits, length, height, nsfw, searchBy);
        g.run();
        SetNewWallpaper set = new SetNewWallpaper(g.getResult());
        set.run();



//        Searcher s = new Searcher(title, subreddits, length, height, nsfw, searchBy);
//        s.generateSearchQuery();
//        System.out.println(s.getSearchQuery());
//        HashMap<String, Wallpaper> wallpapers = s.getSearchResults();
//
//        //SELECTOR
//        try {
//            Selector selector = new Selector(wallpapers);
//            Wallpaper w = selector.select();
//            w.download();
//            Thread t = new Thread(new SetNewWallpaper(w));
//            t.start();
//            System.out.println(w.toString());
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }


    }


}
