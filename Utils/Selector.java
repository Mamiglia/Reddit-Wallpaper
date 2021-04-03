package Utils;

import Wallpaper.Wallpaper;

import java.io.*;
import java.util.*;

class Selector {
    private static int MAX_DB_SIZE = 50;
    public static final String PATH_TO_DATABASE = ".utility/wallpaperDB.txt";
    private final Map<String, Wallpaper> proposal;
    //has a structure like: { ...
    //                          id : Wallpaper
    //                     ...}
    private final Map<String, Wallpaper> db;
    //has a structure like: {...
    //                          id: Wallpaper


    public Selector(Map<String, Wallpaper> proposal) throws IOException{
        File f = new File(PATH_TO_DATABASE);
        this.proposal = proposal;

        this.db = loadDB(f);
    }

    public Wallpaper select() {
        Wallpaper res;
        ArrayList<String> listProposedID = getProposedWallpapersID(),
                alreadyUsedID = getOldWallpapersID();
        for (String propID : listProposedID) {
            if (!alreadyUsedID.contains(propID)) {
                res = proposal.get(propID);
                res.updateDate();
                updateDB(propID, res);
                return res;
                // An unused wallpaper is found
            }
        }
        // OR No unused wallpapers are found, select oldest used wallpapers in the list
        String id = findOldestWallpaper(db, listProposedID);
        res = db.get(id);
        res.updateDate();
        updateDB(id, res);
        return res;
    }

    public ArrayList<String> getOldWallpapersID() {
        return new ArrayList<>(db.keySet());
    }

    public ArrayList<String> getProposedWallpapersID() {
        return new ArrayList<>(proposal.keySet());
    }

    private Map<String, Wallpaper> loadDB(File f) throws IOException {
        if (!f.exists()) {
            //the DB file doesn't exist yet!
            f.getParentFile().mkdirs();
            f.createNewFile();
        }
        Scanner scan = new Scanner(new FileReader(f));
        Map<String, Wallpaper> d = new HashMap<String, Wallpaper>();
        while (scan.hasNext()) {
            // database is written in the file in the form of:
            // id(key);title;url;postUrl;ms_from_epoch \n
            String[] s = scan.nextLine().split(";");
            System.out.println(s.toString());
            Wallpaper w = new Wallpaper(s[1],s[2],s[3], Long.parseLong(s[4]));
            d.put(s[0], w);
        }

        return d;
    }

    private void updateDB(String id, Wallpaper w) {
        // TODO terrible implementation, you just can't erase and rewrite the db every single time
        db.put(id, w);
        cleanDB();
        try{
            writeDB();
            System.out.println("Database succesfully overwritten");
        } catch (IOException e) {
            System.out.println("Database writing failed");
        }



    }

    private void cleanDB() {
        // the database will contain a maximum of MAX_DB_SIZE wallpapers (default N=50)
        // when the db gets bigger then N, the oldest wallpapers are deleted from the database
        // the user will set if he wants to delete also the wallpaper or the database entry only
        if (getOldWallpapersID().size() >= MAX_DB_SIZE) {
            String idOldestWalp = findOldestWallpaper(db);
            Wallpaper w = db.get(idOldestWalp);
            db.remove(idOldestWalp);
            System.out.println("Cleaning of DB, removing " + idOldestWalp);
            boolean USERCHOICE = true; //what userchoice??
            if (USERCHOICE) {
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
            // TODO someone could break everything if they put an ";" in their title
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

    private static String findOldestWallpaper(Map<String, Wallpaper> map, ArrayList<String> keyList) {
        // considering just the keys that are in the key list

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
