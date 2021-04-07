package Utils;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class DisplayLogger {
    //Singleton
    private static final DisplayLogger uniqueInstance = new DisplayLogger();
    private static final String LOG_PATH = ".utility/log.txt";
    private static Level level = Level.WARNING;
    private static FileHandler fh = null;

    public static Logger getInstance(String name) {
        Logger l = Logger.getLogger(name);
        l.setLevel(level);

        if (fh == null) {
            try {
                fh = new FileHandler(LOG_PATH);
                SimpleFormatter formatter = new SimpleFormatter();
                fh.setFormatter(formatter);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        l.addHandler(fh);


        return l;
    }
}
