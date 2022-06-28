package com.mamiglia.gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.mamiglia.settings.*;
import com.mamiglia.utils.DisplayLogger;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class GUI extends JFrame {
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
	private JScrollPane associationScrollPane;
	static final Logger log = DisplayLogger.getInstance("GUI");
	private final Thread backThread;

	public GUI(Thread backThread) {
		super("Reddit Wallpaper Downloader");
		$$$setupUI$$$();
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getClassLoader().getResource(Tray.PATH_TO_TRAY_ICON)));/* icon by https://www.freepik.com */
		this.backThread = backThread;
		this.add(rootPane);

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
			} else if (Settings.INSTANCE.getKeepWallpapers()) {
				log.log(Level.FINE, () -> "Wallpapers have not been removed by preference.");
			} else {
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
		keepCheckBox.addActionListener(e ->
				Settings.INSTANCE.setKeepWallpapers(keepCheckBox.isSelected()));
		blacklistCheckBox.addActionListener(e ->
				Settings.INSTANCE.setKeepBlacklist(blacklistCheckBox.isSelected()));
		dbSizeField.addChangeListener(e -> Settings.INSTANCE.setMaxDatabaseSize((Integer) dbSizeField.getValue()));

		destScrollBar.getVerticalScrollBar().setUnitIncrement(14);
		srcScrollBar.getVerticalScrollBar().setUnitIncrement(14);

		refreshListSrc();
		refreshListDest();
		addSrcBtn.addActionListener(e -> {
			Settings.INSTANCE.newSource();
			refreshListSrc();
		});
		addDestBtn.addActionListener(e -> {
			Settings.INSTANCE.newDest();
			refreshListDest();
		});
		refreshGridAssociations();

		this.setPreferredSize(new Dimension(this.getPreferredSize().width, this.getPreferredSize().height + SourceGUI.STANDARD_HEIGHT));
	}

	private void refreshListSrc() {
		generateList(
				sourcesPane,
				Settings.INSTANCE.getSources().stream().map(src -> new SourceGUI(src)).collect(Collectors.toList()),
				sourcesButtonsPane);
	}

	private void refreshListDest() {
		generateList(
				destsPane,
				Settings.INSTANCE.getDests().stream().map(d -> new DestGUI(d, this)).collect(Collectors.toList()),
				destButtonPane
		);
	}

	private void refreshGridAssociations() {
		associationPane.removeAll();
		int destNumber = Settings.INSTANCE.getDests().size();
		int srcNumber = Settings.INSTANCE.getSources().size();
		var tableData = new Object[srcNumber][destNumber + 1];
		Iterator<Source> it = Settings.INSTANCE.getSources().iterator();

		for (int i = 0; i < srcNumber; i++) {
			Source src = it.next();
			tableData[i][0] = src;
			for (int j = 0; j < destNumber; j++) {
				tableData[i][j + 1] = Settings.INSTANCE.getDests().get(j).getSources().contains(src);
			}
		}

		var columns = new ArrayList<>(Settings.INSTANCE.getDests());
		columns.add(0, null);

		JTable t = new JTable(new AbstractTableModel() {
			private final ArrayList<Destination> columnNames = columns;
			private final Object[][] data = tableData;

			@Override
			public String getColumnName(int col) {
				return col > 0 ? columnNames.get(col).getName() : "";
			}

			@Override
			public Class getColumnClass(int c) {
				return c >= 1 ? Boolean.class : String.class;
			}

			@Override
			public boolean isCellEditable(int row, int col) {
				return col >= 1;
			}

			@Override
			public int getRowCount() {
				return data.length;
			}

			@Override
			public int getColumnCount() {
				return columnNames.size();
			}

			@Override
			public Object getValueAt(int row, int col) {
				return data[row][col];
			}

			@Override
			public void setValueAt(Object value, int row, int col) {
				data[row][col] = value;

				if ((boolean) value) {
					columnNames.get(col).addSource((Source) data[row][0]);
				} else {
					columnNames.get(col).removeSource((Source) data[row][0]);
				}

			}
		});
		associationPane.add(t, BorderLayout.NORTH);
		associationScrollPane.setColumnHeaderView(t.getTableHeader());
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

	private static void generateList(JPanel pane, List<JPanel> elements, JPanel ending) {
		pane.removeAll();
		pane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		for (JPanel el : elements) {
			pane.add(el, c);
			c.gridy++;
		}
		c.weighty = 1;
		pane.add(new JPanel(), c); // makes everything align at top
		c.weighty = 0;
		c.gridy++;
		pane.add(ending, c);
	}

	public static void setLookFeel() {
		try {
			UIManager.put("Button.arc", 999);
			UIManager.put("ScrollBar.showButtons", false);
			UIManager.setLookAndFeel(new FlatDarkLaf());

			// Set System L&F
//			UIManager.setLookAndFeel(
//					UIManager.getSystemLookAndFeelClassName());
			// Nimbus Style
//			UIManager.setLookAndFeel(
//					"javax.swing.plaf.nimbus.NimbusLookAndFeel"
//					);
		} catch (UnsupportedLookAndFeelException e) {
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

	/**
	 * Method generated by IntelliJ IDEA GUI Designer
	 * >>> IMPORTANT!! <<<
	 * DO NOT edit this method OR call it in your code!
	 *
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		createUIComponents();
		rootPane = new JPanel();
		rootPane.setLayout(new BorderLayout(0, 0));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		rootPane.add(panel1, BorderLayout.SOUTH);
		refreshButton = new JButton();
		refreshButton.setText("Refresh");
		panel1.add(refreshButton);
		changeNowButton = new JButton();
		changeNowButton.setText("Change Now");
		panel1.add(changeNowButton);
		tabbedPane = new JTabbedPane();
		tabbedPane.setVisible(true);
		rootPane.add(tabbedPane, BorderLayout.CENTER);
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new BorderLayout(0, 0));
		tabbedPane.addTab("Sources", panel2);
		srcScrollBar = new JScrollPane();
		srcScrollBar.setHorizontalScrollBarPolicy(31);
		srcScrollBar.setVerticalScrollBarPolicy(22);
		panel2.add(srcScrollBar, BorderLayout.CENTER);
		sourcesPane = new JPanel();
		sourcesPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		srcScrollBar.setViewportView(sourcesPane);
		sourcesButtonsPane = new JPanel();
		sourcesButtonsPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		sourcesPane.add(sourcesButtonsPane);
		addSrcBtn = new JButton();
		addSrcBtn.setText("+");
		sourcesButtonsPane.add(addSrcBtn);
		final JPanel panel3 = new JPanel();
		panel3.setLayout(new BorderLayout(0, 0));
		tabbedPane.addTab("Destinations", panel3);
		destScrollBar = new JScrollPane();
		destScrollBar.setHorizontalScrollBarPolicy(31);
		destScrollBar.setVerticalScrollBarPolicy(22);
		panel3.add(destScrollBar, BorderLayout.CENTER);
		destsPane = new JPanel();
		destsPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		destScrollBar.setViewportView(destsPane);
		destButtonPane = new JPanel();
		destButtonPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		destsPane.add(destButtonPane);
		addDestBtn = new JButton();
		addDestBtn.setText("+");
		destButtonPane.add(addDestBtn);
		final JPanel panel4 = new JPanel();
		panel4.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
		tabbedPane.addTab("Associations", panel4);
		associationScrollPane = new JScrollPane();
		panel4.add(associationScrollPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		associationPane = new JPanel();
		associationPane.setLayout(new BorderLayout(0, 0));
		associationScrollPane.setViewportView(associationPane);
		final Spacer spacer1 = new Spacer();
		panel4.add(spacer1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		final JPanel panel5 = new JPanel();
		panel5.setLayout(new GridLayoutManager(5, 1, new Insets(0, 0, 0, 0), -1, -1));
		tabbedPane.addTab("Settings", panel5);
		final JPanel panel6 = new JPanel();
		panel6.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		panel6.setToolTipText("Wallpapers may occupy much disk space. So only a certain number of wallpapers are kept, those exceeding will be eliminated");
		panel5.add(panel6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		final JLabel label1 = new JLabel();
		label1.setText("Maximum Size of Database");
		label1.setToolTipText("Wallpapers may occupy much disk space. So only a certain number of wallpapers are kept, those exceeding will be eliminated");
		panel6.add(label1);
		dbSizeField.setInheritsPopupMenu(true);
		dbSizeField.setToolTipText("Wallpapers may occupy much disk space. So only a certain number of wallpapers are kept, those exceeding will be eliminated");
		panel6.add(dbSizeField);
		resetButton = new JButton();
		resetButton.setText("Erase Database");
		resetButton.setToolTipText("Safely erases database file and all the wallpapers in the folder unless above box is checked");
		panel6.add(resetButton);
		final Spacer spacer2 = new Spacer();
		panel5.add(spacer2, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		final JPanel panel7 = new JPanel();
		panel7.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		panel7.setToolTipText("If checked wallpapers will be kept indefinetely. Pay attention to your disk space!");
		panel5.add(panel7, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		blacklistCheckBox = new JCheckBox();
		blacklistCheckBox.setText("Keep wallpapers even after they have been banned?");
		blacklistCheckBox.setToolTipText("If checked, images will be kept when they are blacklisted");
		panel7.add(blacklistCheckBox);
		final JPanel panel8 = new JPanel();
		panel8.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		panel5.add(panel8, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		keepCheckBox = new JCheckBox();
		keepCheckBox.setInheritsPopupMenu(true);
		keepCheckBox.setText("Keep wallpapers even after they're deleted from database?");
		keepCheckBox.setToolTipText("If checked wallpapers will be kept indefinetely. Pay attention to your disk space!");
		panel8.add(keepCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		final JPanel panel9 = new JPanel();
		panel9.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		panel5.add(panel9, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		final JLabel label2 = new JLabel();
		label2.setText("Wallpaper Directory:");
		panel9.add(label2);
		wallpaperPathText = new JTextField();
		wallpaperPathText.setEditable(false);
		wallpaperPathText.setHorizontalAlignment(0);
		wallpaperPathText.setText("C:/your/path/to/wallpapers");
		wallpaperPathText.setToolTipText("The folder in which your wallpapers are saved");
		panel9.add(wallpaperPathText);
		folderButton = new JButton();
		folderButton.setText("Open Directory");
		folderButton.setToolTipText("Opens the folder");
		panel9.add(folderButton);
		changeDirectoryButton = new JButton();
		changeDirectoryButton.setText("Change");
		changeDirectoryButton.setToolTipText("Changes the folder");
		panel9.add(changeDirectoryButton);
		logTab = new JPanel();
		logTab.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		tabbedPane.addTab("Log", logTab);
		final JScrollPane scrollPane1 = new JScrollPane();
		logTab.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		logArea = new JTextArea();
		logArea.setEditable(false);
		scrollPane1.setViewportView(logArea);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return rootPane;
	}
}
