import java.io.IOException;
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



    }
    public String convertWithIteration(Map<Integer, ?> map) {
        StringBuilder mapAsString = new StringBuilder("{");
        for (Integer key : map.keySet()) {
            mapAsString.append(key + "=" + map.get(key) + ", ");
        }
        mapAsString.delete(mapAsString.length()-2, mapAsString.length()).append("}");
        return mapAsString.toString();
    }

}
