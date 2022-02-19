package com.mamiglia.gui;

import com.mamiglia.settings.*;
import com.mamiglia.utils.DisplayLogger;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GUI extends JFrame{
	private JPanel rootPane;
	private JTabbedPane tabbedPane;
	private JTextArea logArea;
	private JButton changeNowButton;
	private JSpinner dbSizeField;
	private JCheckBox keepCheckBox;
	private JCheckBox blacklistCheckBox;
	private JPanel logTab;
	private JButton folderButton;
	private JButton resetButton;
	private JButton changeDirectoryButton;
	private JTextField wallpaperPathText;
	private JPanel sourcesPane;
	private JButton addSrcBtn;
	private JPanel sourcesButtonsPane;
	private JPanel destsPane;
	private JButton addDestBtn;
	private JPanel destButtonPane;
	private JScrollPane destScrollBar;
	private JScrollPane srcScrollBar;
	private JPanel associationPane;
	private JButton refreshButton;
	static final Logger log = DisplayLogger.getInstance("GUI");
	private final Thread backThread;

	public GUI(Thread backThread) {
		super("Reddit Wallpaper Downloader");
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getClassLoader().getResource(Tray.PATH_TO_TRAY_ICON)));/* icon by https://www.freepik.com */
		this.backThread = backThread;
		add(rootPane);

		setupUI();
		loadSettings();
		log.log(Level.FINER, "GUI started");

		//setResizable(false);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
		setVisible(true);
	}

	void saveSettings() {
		Settings.INSTANCE.writeSettings();
	}

	void loadSettings() {
		keepCheckBox.setSelected(Settings.INSTANCE.getKeepWallpapers());
		blacklistCheckBox.setSelected(Settings.INSTANCE.getKeepBlacklist());
		dbSizeField.setValue(Settings.INSTANCE.getMaxDatabaseSize());
		wallpaperPathText.setText(Settings.INSTANCE.getWallpaperPath());
	}

	void changeWallpaper(Destination dest) {
		saveSettings();
		showLog();

		if (dest == null) { // if dest == null then change them all
			for (Destination d : Settings.INSTANCE.getDests())
				d.updateNext();
		} else {
			dest.updateNext();
		}
		if (backThread.getState() == Thread.State.TIMED_WAITING) {
			backThread.interrupt(); //interrupting it makes it wake up and load new wallpaper
		} else {
			log.log(Level.INFO, "Change button was pressed too early, still occupied changing wallpaper from the last time");
		}
		//interrupting the thread means waking it up. When it's awake it will automatically start searching for a new Wallpaper


		}

	/*
		Opens in explorer the Wallpaper Folder
		TODO verify that this works in Linux also
	 */
	void displayFolder() {
		try {
			Desktop.getDesktop().open(new File(Settings.INSTANCE.getWallpaperPath()));
		} catch (IOException e) {
			log.log(Level.WARNING, e.getMessage());
		}
	}

	void resetDB() {
		int selectedOption = JOptionPane.showConfirmDialog(this, "You are going to remove your wallpaper database", "Alert", JOptionPane.OK_CANCEL_OPTION);

		if (selectedOption == JOptionPane.OK_OPTION) {

			File wallpaperFolder = new File(Settings.INSTANCE.getWallpaperPath());
			// Settings.INSTANCE.eraseDB(); TODO

			// Requires the directory exists and wallpapers should not be kept
			if (wallpaperFolder.isDirectory() && !Settings.INSTANCE.getKeepWallpapers()) {
				for (File walp : Objects.requireNonNull(wallpaperFolder.listFiles())) {
					if (walp.delete()) {
						log.log(Level.FINE, () -> walp + " deleted.");
					}
				}
				log.log(Level.FINE, () -> "Wallpapers successfully purged.");
			}
			else if (Settings.INSTANCE.getKeepWallpapers()) {
				log.log(Level.FINE, () -> "Wallpapers have not been removed by preference.");
			}
			else {
				log.log(Level.FINE, () -> "Wallpapers directory is missing.");
			}
		}
	}

	private void setupUI() {
		Act act = new Act(this);
		//applyButton.addActionListener(act);
		folderButton.addActionListener(act);
		resetButton.addActionListener(act);
		changeNowButton.addActionListener(act);
		changeDirectoryButton.addActionListener(act);
		tabbedPane.addChangeListener(changeEvent -> {
			JTabbedPane src = (JTabbedPane) changeEvent.getSource();
			int index = src.getSelectedIndex();
			if (src.getComponentAt(index).equals(logTab)) {
				showLog();
			}
		});
		refreshButton.addActionListener(e -> {
			refreshListDest();
			refreshListSrc();
			refreshGridAssociations();
			loadSettings();
		});
		keepCheckBox.addActionListener(e->
				Settings.INSTANCE.setKeepWallpapers(keepCheckBox.isSelected()));
		blacklistCheckBox.addActionListener(e->
				Settings.INSTANCE.setKeepBlacklist(blacklistCheckBox.isSelected()));
		dbSizeField.addChangeListener(e-> Settings.INSTANCE.setMaxDatabaseSize((Integer) dbSizeField.getValue()));

		destScrollBar.getVerticalScrollBar().setUnitIncrement(14);
		srcScrollBar.getVerticalScrollBar().setUnitIncrement(14);

		refreshListSrc();
		refreshListDest();
		addSrcBtn.addActionListener(e -> {
			Settings.INSTANCE.newSource();
			refreshListSrc();
		});
		addDestBtn.addActionListener(e->{
			Settings.INSTANCE.newDest();
			refreshListDest();
		});


		refreshGridAssociations();
	}

	private void refreshListSrc() {
		sourcesPane.removeAll();
		sourcesPane.setLayout(new BoxLayout(sourcesPane, BoxLayout.Y_AXIS));
		for (Source src : Settings.INSTANCE.getSources()) {
			sourcesPane.add(new SourceGUI(src));
		}
		sourcesPane.add(new Box.Filler(new Dimension(), new Dimension(), new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE))); // makes everything align at top
		sourcesPane.add(sourcesButtonsPane);
	}

	private void refreshListDest() {
		destsPane.removeAll();
		destsPane.setLayout(new BoxLayout(destsPane, BoxLayout.Y_AXIS));
		for (Destination dest : Settings.INSTANCE.getDests()) {
			destsPane.add(new DestGUI(dest, this));
		}
		destsPane.add(new Box.Filler(new Dimension(), new Dimension(), new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE))); // makes everything align at top
		destsPane.add(destButtonPane);
	}

	private void refreshGridAssociations() {
		if (Settings.INSTANCE.isSingleDestination() && Settings.INSTANCE.isSingleSource()) {
			tabbedPane.setEnabledAt(2, false);
			return;
		} else {
			tabbedPane.setEnabledAt(2, true);
		}
		associationPane.removeAll();
		associationPane.setLayout(new GridLayout(Settings.INSTANCE.getSources().size()+1, Settings.INSTANCE.getDests().size()+1));
		associationPane.add(new JPanel());
		for (Destination d: Settings.INSTANCE.getDests()) {
			associationPane.add(new JLabel(d.getName()));
		}
		for (Source s : Settings.INSTANCE.getSources()) {
			associationPane.add(new JLabel(s.getName()));
			for (Destination d : Settings.INSTANCE.getDests()) {
				JCheckBox c = new JCheckBox();
				c.addActionListener(e-> {
					if (c.isSelected()) {
						d.getSources().add(s);
					} else {
						d.getSources().remove(s);
					}
				});
				associationPane.add(c);
			}
		}
	}

	private void createUIComponents() {
		// custom create
		SpinnerNumberModel s = new SpinnerNumberModel(50, 5, 10000, 1);
		dbSizeField = new JSpinner(s);

	}

	private void showLog() {
		FileReader reader;
		try {
			reader = new FileReader(DisplayLogger.LOG_PATH);
			logArea.read(reader, DisplayLogger.LOG_PATH);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void folderPicker() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle("Select new wallpaper path");
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.showOpenDialog(this);
		File directory = chooser.getSelectedFile();
		Settings.INSTANCE.setWallpaperPath(directory.toString() + File.separator);
		loadSettings();
	}

	public static void setLookFeel() {
		try {
			UIManager.setLookAndFeel( new FlatDarkLaf() );
			// Set System L&F
//			UIManager.setLookAndFeel(
//					UIManager.getSystemLookAndFeelClassName());
			// Nimbus Style
//			UIManager.setLookAndFeel(
//					"javax.swing.plaf.nimbus.NimbusLookAndFeel"
//					);
		}
		catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
//		catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		}
//		catch (InstantiationException e) {
//			e.printStackTrace();
//		}
//		catch (IllegalAccessException e) {
//			e.printStackTrace();
//		}
	}

	public static void main(String[] args) {
		// GUI Tests
		setLookFeel();
		new GUI(null);
	}
}
