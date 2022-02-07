package GUI;

import Utils.DisplayLogger;

import javax.swing.*;
import java.awt.*;
//import java.awt.event.*;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Tray {
	private static Tray uniqueInstance;
	public static final String PATH_TO_TRAY_ICON = "/resources/tray_icon.png";
//	public static final String PATH_TO_CHANGE_ICON = "/resources/change_ico.png";
//	public static final String PATH_TO_DOWNLOAD_ICON = "/resources/download_ico.png";
//	public static final String PATH_TO_POWER_ICON = "/resources/power_ico.png";
//	public static final String PATH_TO_SETTINGS_ICON = "/resources/settings_ico.png";
//	public static final String PATH_TO_WALLPAPER_ICON = "/resources/wallpaper_ico.png";
	private final Thread backThread; // it's the Thread of the backgound (the thing that runs always in background)
	private final Background background;
	private final TrayIcon trayIcon;
	private final SystemTray systemTray;

	private final Logger log = DisplayLogger.getInstance("Tray");

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
		Image image = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource(PATH_TO_TRAY_ICON));// icon by https://www.freepik.com
		this.trayIcon = new TrayIcon(image, "Reddit Wallpaper", null);
		this.systemTray = SystemTray.getSystemTray();
	}

	//start of main method
	public void startTray() {
		if(!SystemTray.isSupported()){
			//checking for support
			log.log(Level.SEVERE, "System tray is not supported !!!");
			return ;
		}

		populateTray(null);

		try{
			systemTray.add(trayIcon);
		}catch(AWTException awtException){
			log.log(Level.SEVERE, "Cannot set the system tray");
		}
	}

	public void populateTray(String title){
		PopupMenu trayPopupMenu = new PopupMenu("Reddit Wallpaper");

		if (title != null) {
			MenuItem titleItem = new MenuItem(title);
			titleItem.addActionListener(e -> openWebpage(background.getCurrent().getPostUrl()));
			trayPopupMenu.add(titleItem);

			MenuItem saveItem = new MenuItem("Save Wallpaper");
			saveItem.addActionListener(e -> {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setSelectedFile(background.getCurrent().getPath().toFile());
				chooser.setDialogTitle("Select where to save wallpaper");
				chooser.setAcceptAllFileFilterUsed(false);
				int ret = chooser.showSaveDialog(null);
				if (ret == JFileChooser.APPROVE_OPTION) {
					Path path = chooser.getSelectedFile().toPath();
					try {
						Files.copy(background.getCurrent().getPath(), path, StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException ioException) {
						ioException.printStackTrace();
					}
				}
			});
			trayPopupMenu.add(saveItem);

			MenuItem banItem = new MenuItem("Blacklist Wallpaper");
			banItem.addActionListener( e -> {
				if (backThread.getState() == Thread.State.TIMED_WAITING) {
					background.banWallpaper();
					backThread.interrupt(); //interrupting it makes it wake up and load new wallpaper
				} else {
					log.log(Level.INFO, "Blacklist button was pressed too early, still occupied changing wallpaper from the last time");
				}
			});
			trayPopupMenu.add(banItem);

			trayPopupMenu.addSeparator();
		}

		MenuItem changeItem = new MenuItem("Change Wallpaper");
		changeItem.addActionListener(e -> {
			if (backThread.getState() == Thread.State.TIMED_WAITING) {
				backThread.interrupt(); //interrupting it makes it wake up and load new wallpaper
			} else {
				log.log(Level.INFO, "Change button was pressed too early, still occupied changing wallpaper from the last time");
			}
			//interrupting the thread means waking it up. When it's awake it will automatically start searching for a new Wallpaper
		});
		trayPopupMenu.add(changeItem);

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

	public static void openWebpage(String urlString) {
		try {
			Desktop.getDesktop().browse(new URL(urlString).toURI());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
