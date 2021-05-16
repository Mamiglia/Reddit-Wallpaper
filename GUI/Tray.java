package GUI;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;

public class Tray {
	private final Thread backThread; // it's the Thread of the backgound (the thing that runs always in background)
	private final Background background;

	public Tray(Thread backgroundThread, Background background) {
		this.backThread = backgroundThread;
		this.background = background;
	}


	//start of main method
	public void startTray(){
		//checking for support
		if(!SystemTray.isSupported()){
			System.out.println("System tray is not supported !!! ");
			return ;
		}
		//get the systemTray of the system
		SystemTray systemTray = SystemTray.getSystemTray();
		Image image = Toolkit.getDefaultToolkit().getImage(".resources/tray_icon.png");// icon by https://www.freepik.com

		//popupmenu
		PopupMenu trayPopupMenu = new PopupMenu();

		MenuItem gui = new MenuItem("Settings");
		gui.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new GUI(backThread);
			}
		});
		trayPopupMenu.add(gui);

		MenuItem change = new MenuItem("Change Wallpaper");
		change.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				backThread.interrupt(); //interrupting the thread means waking it up. When it's awake it will automatically start searching for a new Wallpaper
			}
		});
		trayPopupMenu.add(change);

		MenuItem post = new MenuItem("Current thread");
		post.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openWebpage(background.getCurrent().getPostUrl());
			}
		});
		trayPopupMenu.add(post);

		MenuItem close = new MenuItem("Close");
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				background.stop();
				backThread.interrupt();

				try {
					backThread.join();
				} catch (InterruptedException interruptedException) {
					interruptedException.printStackTrace();
				}

				System.exit(0);
			}
		});
		trayPopupMenu.add(close);

		//setting tray icon
		TrayIcon trayIcon = new TrayIcon(image, "Reddit Wallpaper", trayPopupMenu);
		//adjust to default size as per system recommendation
		trayIcon.setImageAutoSize(true);

		try{
			systemTray.add(trayIcon);
		}catch(AWTException awtException){
			awtException.printStackTrace();
		}
	}

	public static void openWebpage(String urlString) {
		try {
			Desktop.getDesktop().browse(new URL(urlString).toURI());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
