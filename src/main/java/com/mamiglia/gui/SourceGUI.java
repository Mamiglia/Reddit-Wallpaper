package com.mamiglia.gui;

import com.mamiglia.settings.NSFW_LEVEL;
import com.mamiglia.settings.SEARCH_BY;
import com.mamiglia.settings.Source;
import com.mamiglia.settings.TIME;

import javax.swing.*;
import java.util.Hashtable;

public class SourceGUI {
	private JTextField titleField;
	private JTextField subredditField;
	private JTextField flairField;
	private JComboBox<TIME> oldSelection;
	private JComboBox<SEARCH_BY> sortSelection;
	private JSlider nsfwSlider;
	private JSpinner scoreField;
	private JPanel source;
	private final Source src;

	SourceGUI(Source src) {
		this.src = src;
	}

	public void loadData() {
		titleField.setText(String.join(", ", src.getTitles()));
		subredditField.setText(String.join(", ", src.getSubreddits()));
		flairField.setText(String.join(", ", src.getFlairs()));
		oldSelection.setSelectedItem(src.getMaxOldness());
		sortSelection.setSelectedItem(src.getSearchBy());
		scoreField.setValue(src.getMinScore());
		nsfwSlider.setValue(src.getNsfwLevel().getValue());
	}

	private void createUIComponents() {
		scoreField = new JSpinner(new SpinnerNumberModel(src.getMinScore(), 0, 10000, 1));
		oldSelection = new JComboBox<>(TIME.values());
		sortSelection = new JComboBox<>(SEARCH_BY.values());

		nsfwSlider.setLabelTable(new Hashtable<Integer, JLabel>() {{
			put(NSFW_LEVEL.NEVER.getValue(), new JLabel(NSFW_LEVEL.NEVER.toString()));
			put(NSFW_LEVEL.ALLOW.getValue(), new JLabel(NSFW_LEVEL.ALLOW.toString()));
			put(NSFW_LEVEL.ONLY.getValue(), new JLabel(NSFW_LEVEL.ONLY.toString()));
		}});
	}
}
