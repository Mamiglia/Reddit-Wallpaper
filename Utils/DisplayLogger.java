package Utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

public class DisplayLogger {
    //Singleton
    private static final DisplayLogger uniqueInstance = new DisplayLogger();
    private static final String LOG_PATH = ".utility/log.txt";
    private static final Level level = Level.INFO; // !! set to INFO before pushing to production!!!
    private static FileHandler fh = null;

    public static Logger getInstance(String name) {
        Logger l = Logger.getLogger(name);
        l.setLevel(level);

        if (fh == null) {
            try {
                File logFile = new File(LOG_PATH);
                logFile.getParentFile().mkdirs();
                logFile.createNewFile();
                fh = new FileHandler(LOG_PATH);
                Formatter formatter = new Formatter() {
                    @Override
                    public String format(LogRecord record) {
                        StringBuilder strb = new StringBuilder();
                        strb.append(record.getLevel()).append(": ");
                        strb.append(record.getMessage()).append("\n");
                        return strb.toString();
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
