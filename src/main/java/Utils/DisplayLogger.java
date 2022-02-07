package Utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

public class DisplayLogger {
    //Singleton
    private static final DisplayLogger uniqueInstance = new DisplayLogger();
    public static final String LOG_PATH = "utility" + File.separator +"log.txt";
    private static final Level level = Level.INFO; // !! set to INFO before pushing to production!!!
    private static FileHandler fh = null;

    public static Logger getInstance(String name) {
        Logger l = Logger.getLogger(name);
        l.setLevel(level);

        if (fh == null) {
            try {
                File logFile = new File(LOG_PATH);

                // Check if the log file exists first, sometimes you need old log information
                if (!logFile.exists()) {
                    logFile.getParentFile().mkdirs();
                    logFile.createNewFile();
                }
                fh = new FileHandler(LOG_PATH);
                Formatter formatter = new Formatter() {
                    @Override
                    public String format(LogRecord record) {
                        return record.getLevel() + ": " +
                                record.getMessage() + "\n";
                    }
                };
                fh.setFormatter(formatter);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        l.addHandler(fh);


        return l;
    }
}
