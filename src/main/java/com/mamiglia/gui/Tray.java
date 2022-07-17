package com.mamiglia.gui;

import com.mamiglia.settings.Destination;
import com.mamiglia.settings.Settings;

import javax.swing.*;
import java.awt.*;
//import java.awt.event.*;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mamiglia.settings.SettingsKt.HOURS_TO_MILLIS;

public class Tray {
	private static Tray uniqueInstance;
	public static final String PATH_TO_TRAY_ICON = "tray_icon.png";
//	public static final String PATH_TO_CHANGE_ICON = "change_ico.png";
//	public static final String PATH_TO_DOWNLOAD_ICON = "download_ico.png";
//	public static final String PATH_TO_POWER_ICON = "power_ico.png";
//	public static final String PATH_TO_SETTINGS_ICON = "settings_ico.png";
//	public static final String PATH_TO_WALLPAPER_ICON = "wallpaper_ico.png";
	private final Thread backThread; // it's the Thread of the backgound (the thing that runs always in background)
	private final Background background;
	private final TrayIcon trayIcon;
	private final SystemTray systemTray;

	private final Logger log = LoggerFactory.getLogger("Tray");

	public static Tray getInstance() {
		if (uniqueInstance == null) throw new RuntimeException("Tray isn't initialized!");
		return uniqueInstance;
	}
	public static Tray getInstance(Thread backgroundThread, Background background) {
		if (uniqueInstance != null) return uniqueInstance;

		uniqueInstance = new Tray(backgroundThread, background);
		return uniqueInstance;
	}


	private Tray(Thread backgroundThread, Background background) {
		this.backThread = backgroundThread;
		this.background = background;
		Image image = Toolkit.getDefaultToolkit().getImage(this.getClass().getClassLoader().getResource(PATH_TO_TRAY_ICON));// icon by https://www.freepik.com
		this.trayIcon = new TrayIcon(image, "Reddit Wallpaper", null);
		this.systemTray = SystemTray.getSystemTray();
	}

	//start of main method
	public void startTray() {
		if(!SystemTray.isSupported()){
			//checking for support
			log.error("System tray is not supported !!!");
			return ;
		}

		populateTray();

		try{
			systemTray.add(trayIcon);
		}catch(AWTException awtException){
			log.error("Cannot set the system tray");
		}
	}

	public void populateTray(){
		PopupMenu trayPopupMenu = new PopupMenu("Reddit Wallpaper");

		for (Destination dest : Settings.INSTANCE.getDests()) {
			var subMenuDest = new Menu(dest.getName());

			if (dest.getCurrent() != null) {
				MenuItem titleItem = new MenuItem(dest.getCurrent().getTitle());
				titleItem.addActionListener(e -> openWebpage(dest.getCurrent().getPostUrl()));
				subMenuDest.add(titleItem);
				subMenuDest.addSeparator();

				MenuItem saveItem = new MenuItem("Save Wallpaper");
				saveItem.addActionListener(e -> {
					JFileChooser chooser = new JFileChooser();
					chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					chooser.setSelectedFile(dest.getCurrent().getPath().toFile());
					chooser.setDialogTitle("Select where to save wallpaper");
					chooser.setAcceptAllFileFilterUsed(false);
					int ret = chooser.showSaveDialog(null);
					if (ret == JFileChooser.APPROVE_OPTION) {
						Path path = chooser.getSelectedFile().toPath();
						try {
							Files.copy(dest.getCurrent().getPath(), path, StandardCopyOption.REPLACE_EXISTING);
						} catch (IOException ioException) {
							ioException.printStackTrace();
						}
					}
				});
				subMenuDest.add(saveItem);

				MenuItem pinItem = new MenuItem("Pin wallpaper");
				pinItem.addActionListener(e -> {
					dest.setResidualTime((long) (Settings.INSTANCE.getPinTime() * HOURS_TO_MILLIS));
				});
				subMenuDest.add(pinItem);

				MenuItem banItem = new MenuItem("Ban wallpaper");
				banItem.addActionListener(e -> {
					if (backThread.getState() == Thread.State.TIMED_WAITING) {
						Settings.INSTANCE.banWallpaper(dest.getCurrent());
						dest.updateNext();
						backThread.interrupt(); //interrupting it makes it wake up and load new wallpaper
					} else {
						log.info("\"Ban wallpaper\" button was pressed too early, still occupied changing wallpaper from the last time");
					}
				});
				subMenuDest.add(banItem);
			}

			MenuItem changeItem = new MenuItem("Change Wallpaper");
			changeItem.addActionListener(e -> {
				if (backThread.getState() == Thread.State.TIMED_WAITING) {
					dest.updateNext();
					backThread.interrupt(); //interrupting it makes it wake up and load new wallpaper
				} else {
					log.info("\"Change\" button was pressed too early, still occupied changing wallpaper from the last time");
				}
				//interrupting the thread means waking it up. When it's awake it will automatically start searching for a new Wallpaper
			});
			subMenuDest.add(changeItem);
			trayPopupMenu.add(subMenuDest);

			trayPopupMenu.addSeparator();
		}

		MenuItem guiItem = new MenuItem("Settings");
		guiItem.addActionListener(e -> new GUI(backThread));
		trayPopupMenu.add(guiItem);

		MenuItem closeItem = new MenuItem("Close");
		closeItem.addActionListener(e -> {
			background.stop();
			backThread.interrupt();

			try {
				backThread.join();
			} catch (InterruptedException interruptedException) {
				interruptedException.printStackTrace();
			}

			System.exit(0);
		});
		trayPopupMenu.add(closeItem);

		//setting tray icon menu
		trayIcon.setPopupMenu(trayPopupMenu);

		//adjust to default size as per system recommendation
		trayIcon.setImageAutoSize(true);
	}

	public void notify(String title, String message) {
		this.notify(title,message, TrayIcon.MessageType.INFO);
	}

	public void notify(String title, String message, TrayIcon.MessageType type) {
		if (Settings.INSTANCE.getDisplayNotification())
			trayIcon.displayMessage(title, message, type);
	}

	public static void openWebpage(String urlString) {
		try {
			Desktop.getDesktop().browse(new URL(urlString).toURI());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
