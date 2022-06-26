package com.mamiglia.utils;

import com.mamiglia.settings.Destination;
import com.mamiglia.settings.Settings;
import com.mamiglia.wallpaper.Wallpaper;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

class Selector implements Runnable{
    private static final Logger log = DisplayLogger.getInstance("Selector");
    private boolean executed = false;
    private Wallpaper result = null;
    private final WallpaperDAO db = new WallpaperDAO();
    private final Set<Wallpaper> proposal;
    private final Destination dest;

    public Selector(Set<Wallpaper> proposal, Destination dest) throws IOException {
        this.proposal = proposal;
        this.dest = dest;

    }

    //TODO Add image title to image bottom left (possibly restrict to certain subreddits?)
    //TODO Imgur gallery handling???
    @Override
    public void run() {
        if (executed || proposal == null) return;
        executed = true;

        if (!db.open()) return;

        List<String> oldID = db.getAllId();
        var newWp = proposal.stream()
                .filter(wp -> !oldID.contains(wp.getID()) && this.dest.checkSize(wp))
                .collect(Collectors.toUnmodifiableList());

        for (Wallpaper wp : newWp) {
            if (Settings.INSTANCE.isBanned(wp)) {
                // if banned the wallpaper must not be considered
                db.removeWp(wp.getID());
                continue;
            }
            log.log(Level.FINE, "Selected new wallpaper from those proposed");
            db.insert(wp);
            db.close();
            this.result = wp;
            return;
        }
		
        if (proposal.isEmpty()) {
            log.log(Level.WARNING, "Not enough new wallpapers were proposed, setting from recent wallpapers. Maybe your query is too restrictive?");
        } else if (newWp.isEmpty()) {
            log.log(Level.INFO, "No unused or fitting wallpapers were found, setting from the oldest of those found");
        }

        // OR Not enough unused wallpapers are found //select oldest used wallpapers in the list
        var old = db.getAllWallpapers();
        old.addAll(proposal);
        for (Wallpaper wp : old) {
            if (dest.checkSize(wp) && !Settings.INSTANCE.isBanned(wp)) {
                this.result = wp;
                db.updateDate(wp);
                break;
            }
        }
        if (result == null) {
            log.log(Level.WARNING, "Database is void, no wallpaper can be set.");
        }
        System.out.println(db.show());
        db.close();

    }

    public Wallpaper getResult() {
        if (!executed) {
            log.log(Level.INFO, "Result was requested but the functor was never executed");
		} else if (result == null) {
            log.log(Level.INFO, "Selector didn't select any wallpapers");
        }
        return result;
    }

}
