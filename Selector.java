import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Selector {
    public static final String databasePath = ".utility/wallpaperDB.sav";
    private final HashMap<String, Wallpaper> proposal;
    //has a structure like: { ...
    //                          id : Wallpaper
    //                     ...}
    private final HashMap<String, Wallpaper> db;
    //has a structure like: {...
    //                          id: Wallpaper


    public Selector(HashMap<String, Wallpaper> proposal) throws IOException, ClassNotFoundException {
        File f = new File(databasePath);
        f.createNewFile();
        this.proposal = proposal;
        this.db = loadDB(f);
    }
    public Wallpaper select() {
        Wallpaper res;
        ArrayList<String> listProposedID = getProposedWallpapersID(), alreadyUsedID = getOldWallpapersID();
        for (String propID : listProposedID) {
            if (!alreadyUsedID.contains(propID)) {
                res = proposal.get(propID);
                res.updateDate();
                updateDB(res, propID);
                return res;
                // An unused wallpaper is found
            }
        }

        // No unused wallpapers are found, select oldest used wallpapers in the list
        String id = findOldestWallpaper(db, listProposedID);
        res = db.get(id);
        res.updateDate();
        updateDB(res, id);
        return res;
    }
    public ArrayList<String> getOldWallpapersID() {
        return new ArrayList<>(db.keySet());
    }
    public ArrayList<String> getProposedWallpapersID() {
        return new ArrayList<>(proposal.keySet());
    }

    private HashMap<String, Wallpaper> loadDB(File f) throws IOException, ClassNotFoundException {
        FileInputStream fi = new FileInputStream(f);
        HashMap<String, Wallpaper> d;
        try {
            ObjectInputStream o = new ObjectInputStream(fi);
            d = (HashMap<String, Wallpaper>) o.readObject();
            o.close();
        } catch (EOFException e) {
            //means that file is void
            d = new HashMap<String, Wallpaper>();
        }

        return d;
    }
    private void updateDB(Wallpaper w, String id) {
        db.put(id, w);
        cleanDB();
        try{
            writeDB();
        } catch (IOException e) {
            System.out.println("writing Database failed");
        }



    }
    private void cleanDB() {
        // the database will contain a maximum of N wallpapers (default N=50)
        // when the db gets bigger then N, the oldest wallpapers are deleted from the database
        // the user will set if he wants to delete also the wallpaper or the database entry only
        if (getOldWallpapersID().size() >= 50) { // TODO replace 50 with N
            String idOldestWalp = findOldestWallpaper(db);
            Wallpaper w = db.get(idOldestWalp);
            db.remove(idOldestWalp);
            boolean USERCHOICE = true;
            if (USERCHOICE) {
                File f = new File(w.getPath());
                f.delete();
            }
            cleanDB();
        }
        return;
    }
    private void writeDB() throws IOException {
        File f = new File(databasePath);
        f.delete();
        f.createNewFile();
        ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(f));
        o.writeObject(db);
        o.flush();
    }
    private static String findOldestWallpaper(HashMap<String, Wallpaper> map) {
        return findOldestWallpaper(map, new ArrayList<>(map.keySet()));
    }
    private static String findOldestWallpaper(HashMap<String, Wallpaper> map, ArrayList<String> keyList) {
        Date oldest = new Date();
        // everything is older than the present moment
        String oldestID = "";
        for (String id : keyList) {
            if (oldest.after(map.get(id).getLastUsedDate())) {
                oldestID = id;
                oldest = map.get(id).getLastUsedDate();
            }
        }
        return oldestID;
    }
}
