package com.mamiglia.gui;

import com.mamiglia.settings.Destination;
import com.mamiglia.settings.RATIO_LIMIT;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class DestGUI {
	private JSpinner widthField;
	private JSpinner heightField;
	private JComboBox<RATIO_LIMIT> ratioSelection;
	private JList<Integer> monitorList;
	private JLabel wallpaperName;
	private JLabel wallpaperSubreddit;
	private JLabel wallpaperLink;
	private JPanel iconPanel;
	private JSpinner periodField;
	private JPanel destination;
	private Destination dest;

	DestGUI(Destination dest) {
		this.dest = dest;

		loadData();
	}

	public void loadData() {
		if (dest.getCurrent() != null) {
			wallpaperName.setText(dest.getName());
			createLink(wallpaperLink, "link", dest.getCurrent().getPostUrl());
		} else {
			wallpaperName.setText("NULL");
			createLink(wallpaperLink, "None", "");
		}
		monitorList.setListData(dest.getScreens().toArray(new Integer[0]));
		ratioSelection.setSelectedItem(dest.getRatioLimit());
		heightField.setValue(dest.getHeight());
		widthField.setValue(dest.getWidth());
		periodField.setValue(dest.getPeriod());
	}


	private void createUIComponents() {
		//TODO add image to icon
		heightField = new JSpinner(new SpinnerNumberModel(dest.getHeight(), 0, 10000, 1));
		widthField = new JSpinner(new SpinnerNumberModel(dest.getWidth(), 0, 10000, 1));
		periodField = new JSpinner(new SpinnerNumberModel(dest.getPeriod(), 0, 10000, 1));

		ratioSelection = new JComboBox<>(RATIO_LIMIT.values());
	}

	private static void createLink(JLabel label, String text, String link) {
		label.setText(text);
		label.setForeground(Color.BLUE.darker());
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

			@Override
			public void mouseEntered(MouseEvent e) {
				label.setText("<U>" + text + "</U>");
			}

			@Override
			public void mouseExited(MouseEvent e) {
				label.setText(text);
			}
		});
	}
}
