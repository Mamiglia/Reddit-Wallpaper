package GUI;

import Settings.Settings;
import Settings.Settings.TIME;
import Settings.Settings.SEARCH_BY;
import Utils.DisplayLogger;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GUI extends JFrame{
	private JPanel rootPane;
	private JTabbedPane tabbedPane;
	private JPanel settingPane;
	private JPanel titlePane;
	private JPanel subredditPane;
	private JPanel flairPane;
	private JTextField subredditField;
	private JTextField flairField;
	private JComboBox<SEARCH_BY> sortSelection;
	private JTextArea logArea;
	private JButton applyButton;
	private JButton changeNowButton;
	private JCheckBox logCheckBox;
	private JSpinner heightField;
	private JSpinner periodField;
	private JSpinner widthField;
	private JSpinner dbSizeField;
	private JCheckBox keepCheckBox;
	private JComboBox<TIME> oldSelection;
	private JTextField titleField;
	private JPanel logTab;
	private JButton folderButton;
	private JButton resetButton;
	private JScrollPane scrollPane;
	private JTextField wallpaperPathText;
	private JButton changeDirectoryButton;
	private JSlider nsfwSlider;
	static final Logger log = DisplayLogger.getInstance("GUI");
	private final Act act;
	private final Settings settings = Settings.getInstance();
	private final Thread backThread;

	public GUI(Thread backThread) {
		super("Reddit Wallpaper Downloader");
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource(Tray.PATH_TO_TRAY_ICON)));/* icon by https://www.freepik.com */
		this.backThread = backThread;
		add(rootPane);
		act = new Act(this);
		applyButton.addActionListener(act);
		folderButton.addActionListener(act);
		resetButton.addActionListener(act);
		changeNowButton.addActionListener(act);
		changeDirectoryButton.addActionListener(act);
		scrollPane.setPreferredSize(new Dimension(-1, 3));
		nsfwSlider.setLabelTable(new Hashtable<Integer, JLabel>() {{
			put(-1, new JLabel("Never"));
			put(0, new JLabel("Allow"));
			put(1, new JLabel("Only"));
		}});

		// TODO was useless!
		logCheckBox.setVisible(false);

		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				JTabbedPane src = (JTabbedPane) changeEvent.getSource();
				int index = src.getSelectedIndex();
				if (src.getComponentAt(index).equals(logTab)) {
					showLog();
				}
			}
		});

		loadSettings();
		log.log(Level.FINER, "GUI started");

		setResizable(false);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
		setVisible(true);
	}

	void saveSettings() {
		// Settings.regex holds the regex string as it's accessible from both places I currnetly have it
		// Selects any extra space before or after a string with comma delimitation
		// Selects any extra space (any more than one) between words
		settings.setTitles(titleField.getText().replaceAll(Settings.getRegex(), "").split(","));
		settings.setSubreddits(subredditField.getText().replaceAll(Settings.getRegex(), "").split(","));
		settings.setFlair(flairField.getText().replaceAll(Settings.getRegex(), "").split(","));
		settings.setNsfwLevel(nsfwSlider.getValue());
		settings.setHeight((int) heightField.getValue());
		settings.setWidth((int) widthField.getValue());
		settings.setMaxOldness((TIME) oldSelection.getSelectedItem());
		settings.setPeriod((int) periodField.getValue());
		settings.setSearchBy((SEARCH_BY) sortSelection.getSelectedItem());
		settings.setKeepWallpapers(keepCheckBox.isSelected());
		settings.setMaxDatabaseSize((int) dbSizeField.getValue());

		settings.writeSettings();
	}

	void loadSettings() {
		titleField.setText(Arrays.toString(settings.getTitles()).replace("[", "").replace("]", ""));
		subredditField.setText(Arrays.toString(settings.getSubreddits()).replace("[", "").replace("]", ""));
		flairField.setText(Arrays.toString(settings.getFlair()).replace("[", "").replace("]", ""));
		sortSelection.setSelectedItem(settings.getSearchBy());
		heightField.setValue(settings.getHeight());
		widthField.setValue(settings.getWidth());
		periodField.setValue(settings.getPeriod());
		oldSelection.setSelectedItem(settings.getMaxOldness());
		dbSizeField.setValue(settings.getMaxDatabaseSize());
		keepCheckBox.setSelected(settings.doKeepWallpapers());
		wallpaperPathText.setText(Settings.getWallpaperPath());
		nsfwSlider.setValue(settings.getNsfwLevel().value);
	}

	void changeWallpaper() {
		saveSettings();
		showLog();

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
			Desktop.getDesktop().open(new File(Settings.getWallpaperPath()));
		} catch (IOException e) {
			log.log(Level.WARNING, e.getMessage());
		}
	}

	void resetDB() {
		int selectedOption = JOptionPane.showConfirmDialog(this, "You are going to remove your wallpaper database", "Alert", JOptionPane.OK_CANCEL_OPTION);

		if (selectedOption == JOptionPane.OK_OPTION) {
			//TODO actually reset the DB
			File dbFile = new File(Settings.PATH_TO_DATABASE);
			File wallpaperFolder = new File(Settings.getWallpaperPath());
			if (dbFile.exists()) {
				dbFile.delete();
				try {
					dbFile.createNewFile();
				} catch (IOException e) {
					log.log(Level.WARNING, "Failed erasing the database");
				}
			}
			if (wallpaperFolder.isDirectory()) {
				for (File walp: Objects.requireNonNull(wallpaperFolder.listFiles())) {
					walp.delete();
				}
			}

		}
	}

	private void createUIComponents() {
		SpinnerNumberModel s = new SpinnerNumberModel(15, 0, 1000000, 1);
		periodField = new JSpinner(s);
		s = new SpinnerNumberModel(1080, 0, 10000, 1);
		heightField = new JSpinner(s);
		s = new SpinnerNumberModel(1920, 0, 10000, 1);
		widthField = new JSpinner(s);
		s = new SpinnerNumberModel(50, 5, 10000, 1);
		dbSizeField = new JSpinner(s);
		oldSelection = new JComboBox<>(TIME.values());
		sortSelection = new JComboBox<>(SEARCH_BY.values());
	}

	private void showLog() {
		FileReader reader = null;
		try {
			reader = new FileReader(DisplayLogger.LOG_PATH);
			logArea.read(reader, DisplayLogger.LOG_PATH);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
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
		settings.setWallpaperPath(directory.toString() + File.separator);
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
