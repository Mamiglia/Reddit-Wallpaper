package GUI;

import Settings.Settings;
import Settings.Settings.TIME;
import Settings.Settings.SEARCH_BY;
import Utils.DisplayLogger;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GUI extends JFrame{
	private JPanel rootPane;
	private JTabbedPane tabbedPane;
	private JPanel settingPane;
	private JPanel titlePane;
	private JPanel subredditPane;
	private JTextField subredditField;
	private JComboBox<SEARCH_BY> sortSelection;
	private JCheckBox nsfwCheckBox;
	private JTextArea logArea;
	private JButton applyButton;
	private JButton changeNowButton;
	private JCheckBox LogCheckBox;
	private JSpinner heightField;
	private JSpinner periodField;
	private JSpinner widthField;
	private JSpinner dbSizeField;
	private JCheckBox keepCheckBox;
	private JComboBox<TIME> oldSelection;
	private JTextField titleField;
	private JPanel logTab;
	static final Logger log = DisplayLogger.getInstance("GUI");
	private final Act act;
	private static final String LOG_PATH = ".utility/log.txt";
	private final Settings settings = Settings.getInstance();
	private final Thread backThread;

	public GUI(Thread backThread) {
		super("Reddit Wallpaper Downloader");
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(".resources/tray_icon.png"));
		this.backThread = backThread;
		add(rootPane);
		act = new Act(this);
		applyButton.addActionListener(act);
		changeNowButton.addActionListener(act);
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
		log.log(Level.FINE, "GUI started");





		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
		setVisible(true);
	}

	void saveSettings() {
		settings.setTitles(titleField.getText().replace(" ", "").split(","));
		settings.setSubreddits(subredditField.getText().replace(" ", "").split(","));
		settings.setNsfwOnly(nsfwCheckBox.isSelected());
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
		if (settings == null) {
			log.log(Level.WARNING, "No settings file loaded");
		}
		titleField.setText(Arrays.toString(settings.getTitles()).replace("[", "").replace("]", ""));
		subredditField.setText(Arrays.toString(settings.getSubreddits()).replace("[", "").replace("]", ""));
		sortSelection.setSelectedItem(settings.getSearchBy());
		nsfwCheckBox.setSelected(settings.isNsfwOnly());
		heightField.setValue(settings.getHeight());
		widthField.setValue(settings.getWidth());
		periodField.setValue(settings.getPeriod());
		oldSelection.setSelectedItem(settings.getMaxOldness());
		dbSizeField.setValue(settings.getMaxDatabaseSize());
		keepCheckBox.setSelected(settings.doKeepWallpapers());

	}

	void changeWallpaper() {
		saveSettings();

		backThread.interrupt(); //interrupting it makes it wake up and load new wallpaper
	}

	private void createUIComponents() {
		SpinnerNumberModel s = new SpinnerNumberModel(15, 0, 10000, 1);
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
			reader = new FileReader(LOG_PATH);
			logArea.read(reader, LOG_PATH);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
}
