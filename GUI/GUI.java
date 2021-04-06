package GUI;

import Settings.Settings;
import Settings.Settings.TIME;
import Settings.Settings.SEARCH_BY;

import javax.swing.*;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GUI extends JFrame{
//	public static void main(String[] args) {
//		try {
//			UIManager.setLookAndFeel(
//					"javax.swing.plaf.nimbus.NimbusLookAndFeel"
//					);
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		} catch (InstantiationException e) {
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		} catch (UnsupportedLookAndFeelException e) {
//			e.printStackTrace();
//		}
//		new GUI();
//	}
	private JPanel rootPane;
	private JTabbedPane tabbedPane1;
	private JPanel settingPane;
	private JPanel titlePane;
	private JPanel subredditPane;
	private JTextArea titleArea;
	private JTextArea subredditArea;
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
	static final Logger log = Logger.getLogger("GUI");
	private final Act act;
	private Settings settings = Settings.getInstance();
	private Thread backThread;

	public GUI(Thread backThread) {
		super("Reddit Wallpaper Downloader");
		this.backThread = backThread;
		add(rootPane);
		act = new Act(this);
		applyButton.addActionListener(act);
		changeNowButton.addActionListener(act);
		loadSettings();
		log.log(Level.FINE, "GUI started");


		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	void saveSettings() {
		settings.setTitles(titleArea.getText().replace(" ", "").split(","));
		settings.setSubreddits(subredditArea.getText().replace(" ", "").split(","));
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
		titleArea.setText(Arrays.toString(settings.getTitles()).replace("[", "").replace("]", ""));
		subredditArea.setText(Arrays.toString(settings.getSubreddits()).replace("[", "").replace("]", ""));
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
		s = new SpinnerNumberModel(50, -1, 10000, 1);
		dbSizeField = new JSpinner(s);
		oldSelection = new JComboBox<>(TIME.values());
		sortSelection = new JComboBox<>(SEARCH_BY.values());
	}
}
