import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class Selector {
    public static final String databasePath = ".utility/wallpaperDB";
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
    public Wallpaper select() {
        ArrayList<String> listProposedID = getProposedWallpapersID(), alreadyUsedID = getOldWallpapersID();
        for (String propID : listProposedID) {
            if (!alreadyUsedID.contains(propID)) {

                return proposal.get(propID);
                // An unused wallpaper is found
            }
        }

        // No unused wallpapers are found, select oldest used wallpapers in the list
        Date oldest = new Date();
        // everything is older than the present moment
        Wallpaper walp = proposal.get(listProposedID.get(0));
        for (String propID : listProposedID) {
            if (oldest.after(db.get(propID).getLastUsedDate())) {
                walp = db.get(propID);
                oldest = walp.getLastUsedDate();
            }
        }
        return walp;
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
        Map<String, Wallpaper> d = (Map<String, Wallpaper>) o.readObject();
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
