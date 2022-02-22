package com.mamiglia.gui;

import com.mamiglia.settings.Destination;
import com.mamiglia.settings.RATIO_LIMIT;
import com.mamiglia.settings.Settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.logging.Level;

public class DestGUI extends Collapsible {
	private JSpinner widthField;
	private JSpinner heightField;
	private JComboBox<RATIO_LIMIT> ratioSelection;
	private JLabel wallpaperName;
	private JLabel wallpaperSubreddit;
	private JLabel wallpaperLink;
	private JSpinner periodField;
	private JPanel root;
	private JPanel monitorPanel;
	private JButton renameBtn;
	private JButton removeBtn;
	private JButton saveBtn;
	private JButton changeBtn;
	private final Destination dest;
	private final JCheckBox[] monitorList;


	DestGUI(Destination dest, GUI gui) {
		super(dest.getName());
		setBody(root);
		this.dest = dest;

		monitorPanel.setLayout(new BoxLayout(monitorPanel, BoxLayout.Y_AXIS));
		var g = Settings.INSTANCE.getMonitors();
		monitorList = new JCheckBox[g.length];
		for (int i=0; i<g.length; i++) {
			JCheckBox c = new JCheckBox(Destination.Companion.monitorName(g[i]));
			monitorList[i] = c;
			monitorPanel.add(c);
		}
		saveBtn.addActionListener(e->saveData());
		removeBtn.addActionListener(e->{
			Settings.INSTANCE.removeDestination(dest);
			this.removeAll();
		});
		renameBtn.addActionListener(e->{
			dest.setName(JOptionPane.showInputDialog(this, "Insert new name"));
			this.setTitle(dest.getName());
		});
		changeBtn.addActionListener(e->{
			saveData();
			gui.changeWallpaper(dest);
		});

		loadData();
	}

	private void loadData() {
		if (dest.getCurrent() != null) {
			wallpaperName.setText(dest.getCurrent().getTitle());
			createLink(wallpaperLink, "link", dest.getCurrent().getPostUrl());
		} else {
			wallpaperName.setText("None");
			createLink(wallpaperLink, "None", "");
		}
		this.setTitle(dest.getName());
		ratioSelection.setSelectedItem(dest.getRatioLimit());
		heightField.setValue(dest.getHeight());
		widthField.setValue(dest.getWidth());
		periodField.setValue(dest.getPeriod());
		for (int i=0; i<monitorList.length; i++) {
			monitorList[i].setSelected(dest.getScreens().contains(i));
		}
		GUI.log.log(Level.FINE, "Destination"+ dest.getName()+ "loaded");
	}

	private void saveData() {
		dest.setRatioLimit((RATIO_LIMIT) ratioSelection.getSelectedItem());
		dest.setHeight((Integer)heightField.getValue());
		dest.setWidth((Integer) widthField.getValue());
		dest.setPeriod((Integer) periodField.getValue());
		dest.setScreens(new HashSet<>());
		for (int i=0; i<monitorList.length; i++) {
			if (monitorList[i].isSelected()) {
				dest.getScreens().add(i);
			}
		}
		GUI.log.log(Level.FINE, "Destination " + dest.getName() + " Saved");
	}

	private void createUIComponents() {
		//TODO add image to icon
		heightField = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
		widthField = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
		periodField = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));

		ratioSelection = new JComboBox<>(RATIO_LIMIT.values());
	}

	private static void createLink(JLabel label, String text, String link) {
		label.setText(text);
		label.setForeground(Color.CYAN);
		label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		label.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					Desktop.getDesktop().browse(new URI(link));
				} catch (IOException | URISyntaxException ex) {
					ex.printStackTrace();
				}
			}
		});
	}

	public static void main(String[] args) {
//		JFrame f = new JFrame();
//		f.add(new DestGUI(new Destination(), f));
//		f.setVisible(true);
//		f.pack();
	}
}
