package Utils;

import Wallpaper.Wallpaper;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

class Selector {
    private final int MAX_DB_SIZE;
    private final boolean keepWallpapers;
    public static final String PATH_TO_DATABASE = ".utility/wallpaperDB.txt";
    private static final Logger log = Logger.getLogger("Selector");
    private final Map<String, Wallpaper> proposal;
    //has a structure like: { ...
    //                          id : Wallpaper
    private final Map<String, Wallpaper> db;
    //has a structure like: {...
    //                          id: Wallpaper


    public Selector(Map<String, Wallpaper> proposal, boolean keepWallpapers, int MAX_DB_SIZE) throws IOException{
        File f = new File(PATH_TO_DATABASE);
        this.proposal = proposal;
        this.keepWallpapers = keepWallpapers;
        this.MAX_DB_SIZE = MAX_DB_SIZE;

        this.db = loadDB(f);
    }

    public Wallpaper select() {
        Wallpaper res;
        List<String> listProposedID = getProposedWallpapersID();
        List<String> alreadyUsedID = getOldWallpapersID();
        for (String propID : listProposedID) {
            res = proposal.get(propID);
            if (!alreadyUsedID.contains(propID)) {
                res.updateDate();
                updateDB(propID, res);
                return res;
                // An unused wallpaper is found
            }
        }
        // OR No unused wallpapers are found, select oldest used wallpapers in the list
        String id;
        if (listProposedID.isEmpty()) {
            log.log(Level.WARNING, "No new wallpaper is found, setting a recent wallpaper");
            id = findOldestWallpaper(db);
        } else {
            log.log(Level.WARNING, "No unused wallpaper is found setting the oldest from those found");
            id = findOldestWallpaper(db, listProposedID);
        }
        res = db.get(id);
        res.updateDate();
        updateDB(id, res);
        return res;
    }

    public List<String> getOldWallpapersID() {
        return new ArrayList<>(db.keySet());
    }

    public List<String> getProposedWallpapersID() {
        return new ArrayList<>(proposal.keySet());
    }

    private Map<String, Wallpaper> loadDB(File f) throws IOException {
        if (!f.exists()) {
            //the DB file doesn't exist yet!
            f.getParentFile().mkdirs();
            f.createNewFile();
        }
        Scanner scan = new Scanner(new FileReader(f));
        Map<String, Wallpaper> d = new HashMap<>();
        while (scan.hasNext()) {
            // database is written in the file in the form of:
            // id(key);title;url;postUrl;ms_from_epoch \n
            String[] s = scan.nextLine().split(";");
            log.log(Level.FINE, Arrays.toString(s));
            Wallpaper w = new Wallpaper(s[1],s[2],s[3], Long.parseLong(s[4]));
            d.put(s[0], w);
        }
        scan.close();
        log.log(Level.INFO, "Database loaded");
        return d;
    }

    private void updateDB(String id, Wallpaper w) {
        // TODO terrible implementation, you just can't erase and rewrite the db every single time
        db.put(id, w);
        cleanDB();
        try{
            writeDB();
            log.log(Level.INFO, "Database successfully overwritten");
        } catch (IOException e) {
            log.log(Level.WARNING, "Database writing failed");
        }

    }

    /*
        removes items from Database if it has more than MAX_DB_SIZE elements
     */
    private void cleanDB() {
        // the database will contain a maximum of MAX_DB_SIZE wallpapers (default N=50)
        // when the db gets bigger then N, the oldest wallpapers are deleted from the database
        // the user will set if he wants to delete also the wallpaper or the database entry only
        if (MAX_DB_SIZE != -1 && getOldWallpapersID().size() > MAX_DB_SIZE) {
            String idOldestWalp = findOldestWallpaper(db);
            Wallpaper w = db.get(idOldestWalp);
            db.remove(idOldestWalp);
            log.log(Level.INFO, "Cleaning of DB, removing " + idOldestWalp);

            //Does the user want to keep the wallpaper after it's eliminated from the database?
            if (!keepWallpapers) {
                File f = new File(w.getPath());
                f.delete();
            }
            cleanDB();
        }
    }

    private void writeDB() throws IOException {
        File f = new File(PATH_TO_DATABASE);
        f.delete();
        f.createNewFile();
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        for (String id: db.keySet()) {
            Wallpaper w = db.get(id);
            // TODO someone could break everything if they put an ";" in their title???
            bw.write(id + ";" +  w.getTitle() + ";" + w.getUrl() + ";" + w.getPostUrl() + ";" + w.getLastUsedDate().getTime());
            bw.newLine();
            // database is written in the file in the form of:
            // id(key);title;url;postUrl;millisecondsFromEpoch \n
        }
        bw.flush();
        bw.close();
    }


    private static String findOldestWallpaper(Map<String, Wallpaper> map) {
        // to find the oldest wallpaper considering all the keys in the map itself
        return findOldestWallpaper(map, new ArrayList<>(map.keySet()));
    }

    private static String findOldestWallpaper(Map<String, Wallpaper> map, List<String> keyList) {
        // considering just the keys that are in the key list

        Date oldest = new Date(); // everything is older than the present moment
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
