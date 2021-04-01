package GUI;

import javax.swing.*;

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
	private JFormattedTextField a15FormattedTextField;
	private JButton changeNowButton;

	public GUI() {
		super("Reddit Wallpaper Downloader");
		add(rootPane);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}



}
