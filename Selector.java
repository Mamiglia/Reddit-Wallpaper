import java.io.*;
import java.util.ArrayList;
import java.util.Map;

public class Selector {
    public static final String databasePath = ".utility/wallpaperDatabase";
    private final Map<String, Wallpaper> proposal;
    //has a structure like: { ...
    //                          id : Wallpaper
    //                     ...}
    private final Map<String, Wallpaper> db;
    //has a structure like: {...
    //                          id: Wallpaper


    public Selector(Map<String, Wallpaper> proposal) throws IOException, ClassNotFoundException {
        File f = new File(databasePath);
        f.mkdirs();
        f.createNewFile();
        this.proposal = proposal;
        this.db = loadDB(f);
    }

    public ArrayList<String> getOldWallpapersID() {
        return new ArrayList<>(db.keySet());
    }
    public ArrayList<String> getProposedWallpapersID() {
        return new ArrayList<>(proposal.keySet());
    }

    private Map<String, Wallpaper> loadDB(File f) throws IOException, ClassNotFoundException {
        FileInputStream fi = new FileInputStream(new File("myObjects.txt"));
        ObjectInputStream o = new ObjectInputStream(fi);
        Map<String, Wallpaper> d = (Map<String, Wallpaper>) o.readObject()
        o.close();
        return d;
    }
    private void writeDB() throws IOException {
        File f = new File(databasePath);
        f.delete();
        f.createNewFile();
        ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(f));
        o.writeObject(db);
        o.flush();
    }
}
