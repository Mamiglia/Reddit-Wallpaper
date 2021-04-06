package GUI;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;

public class Tray {
	private final Thread background;

	public Tray(Thread background) {
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

		//get default toolkit
		//Toolkit toolkit = Toolkit.getDefaultToolkit();
		//get image
		//Toolkit.getDefaultToolkit().getImage("src/resources/busylogo.jpg");
		Image image = Toolkit.getDefaultToolkit().getImage(".utility/external-content.duckduckgo.jpg");

		//popupmenu
		PopupMenu trayPopupMenu = new PopupMenu();

		//1st menuitem for popupmenu
		MenuItem gui = new MenuItem("Settings");
		gui.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new GUI(background);
			}
		});
		trayPopupMenu.add(gui);

		MenuItem change = new MenuItem("Change Wallpaper");
		change.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				background.interrupt();
			}
		});
		trayPopupMenu.add(change);

		MenuItem post = new MenuItem("Current thread");
		post.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openWebpage(Background.getInstance().getCurrent().getPostUrl());
			}
		});
		trayPopupMenu.add(post);

		MenuItem close = new MenuItem("Close");
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Background.stop();
				System.exit(0);
			}
		});
		trayPopupMenu.add(close);

		//setting tray icon
		TrayIcon trayIcon = new TrayIcon(image, "SystemTray Demo", trayPopupMenu);
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
