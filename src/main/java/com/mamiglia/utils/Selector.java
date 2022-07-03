package com.mamiglia.utils;

import com.mamiglia.db.WallpaperDAO;
import com.mamiglia.settings.Destination;
import com.mamiglia.settings.Settings;
import com.mamiglia.wallpaper.Wallpaper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

class Selector implements Runnable{
    private static final Logger log = LoggerFactory.getLogger(Selector.class);
    private boolean executed = false;
    private Wallpaper result = null;
    private WallpaperDAO db = null;
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

        db = new WallpaperDAO();

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
            log.debug("Selected new wallpaper from those proposed");
            db.insert(wp);
            db.close();
            this.result = wp;
            return;
        }
		
        if (proposal.isEmpty()) {
            log.warn("Not enough new wallpapers were proposed, setting from recent wallpapers. Maybe your query is too restrictive?");
        } else if (newWp.isEmpty()) {
            log.info("No unused or fitting wallpapers were found, setting from the oldest of those found");
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
            log.warn("Database is void, no wallpaper can be set.");
        }
        db.close();

    }

    public Wallpaper getResult() {
        if (!executed) {
            log.info("Result was requested but the functor was never executed");
		} else if (result == null) {
            log.info("Selector didn't select any wallpapers");
        }
        return result;
    }

}
