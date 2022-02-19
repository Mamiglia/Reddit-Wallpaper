package com.mamiglia.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;

class Act implements ActionListener {
	private final GUI frame;

	Act(GUI frame) {
		this.frame = frame;
	}


	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		Object src = actionEvent.getSource();
		if (!src.getClass().equals(JButton.class)) return;
		JButton btnPressed = (JButton) src;
		switch (btnPressed.getText()) {
			case "Change Now":
				frame.changeWallpaper(null);
				break;
			case "Open Directory":
				frame.displayFolder();
				break;
			case "Erase Wallpaper Database":
				frame.resetDB();
				break;
			case "Change":
				frame.folderPicker();
				break;
			default:
				GUI.log.log(Level.SEVERE, "Unrecognised button");

		}
	}
}
