package GUI;

import Utils.GetNewWallpaper;
import Utils.SetNewWallpaper;

import javax.swing.*;
import java.io.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GUI extends JFrame{
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(
					"javax.swing.plaf.nimbus.NimbusLookAndFeel"
					);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		new GUI();
	}
	private JPanel rootPane;
	private JTabbedPane tabbedPane1;
	private JPanel settingPane;
	private JPanel titlePane;
	private JPanel subredditPane;
	private JTextArea titleArea;
	private JTextArea subredditArea;
	private JComboBox oldSelecton;
	private JComboBox sortSelection;
	private JCheckBox nsfwCheckBox;
	private JTextArea logArea;
	private JButton applyButton;
	private JButton changeNowButton;
	private JCheckBox LogCheckBox;
	private JSpinner heightField;
	private JSpinner periodField;
	private JSpinner widthField;
	static final String PATH_TO_SAVEFILE = ".utility/settings.txt";
	static final Logger log = Logger.getLogger("GUI");
	private final Act act;
	private Settings settings;

	public GUI() {
		super("Reddit Wallpaper Downloader");
		add(rootPane);
		act = new Act(this);
		applyButton.addActionListener(act);
		changeNowButton.addActionListener(act);
		loadSettings();
		log.log(Level.INFO, "GUI started");



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
		//settings.getMaxOldness(oldSelecton.getSelectedItem())
		settings.setPeriod((int) periodField.getValue());
		//settings.setSearchBy(sortSelection.getSelectedItem());

		FileWriter wr = null;
		try {
			wr = new FileWriter(PATH_TO_SAVEFILE);
			wr.write(settings.toString());
			wr.close();
		} catch (IOException e) {
			log.log(Level.SEVERE, "Couldn't save the file");
			e.printStackTrace();
		}
		log.log(Level.INFO, "Saved settings");
	}

	private void loadSettings() {
		File settingFile = new File(PATH_TO_SAVEFILE);
		if (!settingFile.exists()) {
			settingFile.getParentFile().mkdirs();
			try {
				settingFile.createNewFile();
			} catch (IOException e) {
				//TODO bad practice
				e.printStackTrace();
			}
		}
		settings = new Settings();
		Scanner scan = null;
		try {
			 scan = new Scanner(settingFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		while (scan.hasNext()) {
			String[] s = scan.nextLine().split("=");
			settings.setProperty(s[0], s[1]);
		}

	}

	void changeWallpaper() {
		saveSettings();

		//TODO change upper part to depend on settings
		String[] title = {};
		String[] subreddits = {"wallpapers", "wallpaper", "worldpolitics"};
		int length = 1, height = 1;
		boolean nsfw = false;
		GetNewWallpaper.SEARCH_BY searchBy = GetNewWallpaper.SEARCH_BY.HOT;

		//TODO run in threads
		GetNewWallpaper g = new GetNewWallpaper(title, subreddits, length, height, nsfw, searchBy);
		g.run();
		SetNewWallpaper set = new SetNewWallpaper(g.getResult());
		set.run();
	}

	private void createUIComponents() {
		SpinnerNumberModel s = new SpinnerNumberModel(15, 0, 10000, 1);
		periodField = new JSpinner(s);
		s = new SpinnerNumberModel(1080, 0, 10000, 1);
		heightField = new JSpinner(s);
		s = new SpinnerNumberModel(1920, 0, 10000, 1);
		widthField = new JSpinner(s);
	}
}
