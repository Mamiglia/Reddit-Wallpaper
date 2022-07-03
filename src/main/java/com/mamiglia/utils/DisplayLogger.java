package com.mamiglia.utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;

import org.apache.logging.log4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisplayLogger {
    public static final String LOG_PATH = "utility" + File.separator +"log.txt";
    private static final Level level = Level.INFO; // !! set to INFO before pushing to production!!!
    private static final FileHandler fh = null;

    public static Logger getInstance(String name) {
        Logger l = LoggerFactory.getLogger(name);
//        l.setLevel(level);
//
//        if (fh == null) {
//            try {
//                File logFile = new File(LOG_PATH);
//
//                // Check if the log file exists first, sometimes you need old log information
//                if (!logFile.exists()) {
//                    logFile.getParentFile().mkdirs();
//                    logFile.createNewFile();
//                }
//                fh = new FileHandler(LOG_PATH);
//                Formatter formatter = new Formatter() {
//                    @Override
//                    public String format(LogRecord record) {
//                        return record.getLevel() + ": " +
//                                record.getMessage() + "\n";
//                    }
//                };
//                fh.setFormatter(formatter);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        l.addHandler(fh);
//

        return l;
    }

    private DisplayLogger() {}

    public static void main(String[] args) {
        var l = DisplayLogger.
                getInstance("WowW");
        l.warn("You");
        l.info("Yara {} {}", "la", "lo");
        l.trace("Ciao");
        l.debug("ss");
        l.error("sss");
    }
}
