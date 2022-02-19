package com.mamiglia.gui;

import com.mamiglia.settings.*;
import com.mamiglia.utils.DisplayLogger;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

class SourceGUI extends Collapsible{
	private JTextField titleField;
	private JTextField subredditField;
	private JTextField flairField;
	private JComboBox<TIME> timeSelection;
	private JComboBox<SEARCH_BY> sortSelection;
	private JSlider nsfwSlider;
	private JSpinner scoreField;
	private JPanel root;
	private JButton saveSrcBtn;
	private JButton removeSrcBtn;
	private JButton renameSrcBtn;
	private final Source src;

	SourceGUI(Source src) {
		super(src.getName());
		this.src = src;
		setBody(root);

		nsfwSlider.setLabelTable(new Hashtable<Integer, JLabel>() {{
			put(NSFW_LEVEL.NEVER.getValue(), new JLabel(NSFW_LEVEL.NEVER.toString()));
			put(NSFW_LEVEL.ALLOW.getValue(), new JLabel(NSFW_LEVEL.ALLOW.toString()));
			put(NSFW_LEVEL.ONLY.getValue(), new JLabel(NSFW_LEVEL.ONLY.toString()));
		}});
		saveSrcBtn.addActionListener(e-> saveData());
		removeSrcBtn.addActionListener(e -> {
			Settings.INSTANCE.removeSource(src);
			this.removeBody();
		});
		renameSrcBtn.addActionListener(e -> {
			src.setName(JOptionPane.showInputDialog(this, "Insert new name"));
			this.setTitle(src.getName());
		});

		loadData();
	}

	private void loadData() {
		setTitle(src.getName());
		titleField.setText(String.join(", ", src.getTitles()));
		subredditField.setText(String.join(", ", src.getSubreddits()));
		flairField.setText(String.join(", ", src.getFlairs()));
		timeSelection.setSelectedItem(src.getMaxOldness());
		sortSelection.setSelectedItem(src.getSearchBy());
		scoreField.setValue(src.getMinScore());
		nsfwSlider.setValue(src.getNsfwLevel().getValue());
		GUI.log.log(Level.FINE, () -> "Source "+ src.getName() + " loaded");

	}

	private void saveData() {
		src.setTitles(new HashSet<>(List.of(titleField.getText().split(","))));
		src.setSubreddits(new HashSet<>(List.of(subredditField.getText().split(","))));
		src.setFlairs(new HashSet<>(List.of(flairField.getText().split(","))));
		src.setMaxOldness((TIME) Objects.requireNonNull(timeSelection.getSelectedItem()));
		src.setSearchBy((SEARCH_BY) sortSelection.getSelectedItem());
		src.setMinScore((Integer) scoreField.getValue());
		src.setNsfwLevel(NSFW_LEVEL.valueOf(nsfwSlider.getValue()));
		GUI.log.log(Level.FINE, () -> "Source "+ src.getName() + " saved");
	}

	private void createUIComponents() {
		scoreField = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
		timeSelection = new JComboBox<>(TIME.values());
		sortSelection = new JComboBox<>(SEARCH_BY.values());
	}

	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.add(new SourceGUI(new Source()));
		f.setVisible(true);
		f.pack();
	}

}
