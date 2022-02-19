package com.mamiglia.gui;

import javax.swing.*;
import java.awt.*;

class Collapsible extends JPanel {
	private boolean collapsed = true;
	private JPanel bodyContainer;
	private JButton btn;
	private JLabel titleLabel;
	private JPanel root;
	private JPanel head;

	Collapsible(String title) {
		this.add(root);
		this.setTitle(title);

		bodyContainer.setVisible(false);


		btn.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		btn.setContentAreaFilled(false);
		btn.addActionListener(event -> act());
	}

	public void setTitle(String title) {
		titleLabel.setText(title.substring(0, Math.min(40, title.length()))); //trimming is necessary
	}


	public void setBody(JPanel body) {
		bodyContainer.removeAll();
		bodyContainer.add(body);
		bodyContainer.setVisible(false);
		head.setPreferredSize(new Dimension(bodyContainer.getPreferredSize().width, head.getPreferredSize().height));
	}

	public void removeBody() {
		bodyContainer.removeAll();
		this.setVisible(false);
	}

	private void act() {
		bodyContainer.setVisible(collapsed);
		collapsed = !collapsed;
		btn.setText(collapsed? "v" : "^"); // TODO add better icons
	}

	@Override
	public Dimension getPreferredSize() {
		if (bodyContainer.getComponents().length == 0) {
			return new Dimension();
		}
		return new Dimension(bodyContainer.getPreferredSize().width, root.getPreferredSize().height);
	}


}
