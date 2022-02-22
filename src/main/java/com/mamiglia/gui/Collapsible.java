package com.mamiglia.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

class Collapsible extends JPanel {
	private static final ImageIcon COLLAPSE_ICO = new ImageIcon("src/main/resources/collapse_ico.png");
	private static final ImageIcon EXPAND_ICO = new ImageIcon("src/main/resources/expand_ico.png");
	private static final int BOTTOM_PADDING = 5;
	private boolean collapsed = true;
	private JPanel bodyContainer;
	private JButton btn;
	private JLabel titleLabel;
	private JPanel root;
	private JPanel head;

	Collapsible(String title) {
		this.setLayout(new BorderLayout());
		this.add(root, BorderLayout.CENTER);
		this.setTitle(title);

		bodyContainer.setVisible(false);
		bodyContainer.setLayout(new BorderLayout());
		root.setBorder(new LineBorder(new Color(48,50,52), 2, true));
		this.setBorder(new EmptyBorder(0,0, BOTTOM_PADDING,0));

		root.setBackground(new Color(48,50,52));

		btn.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		btn.setContentAreaFilled(false);
		btn.addActionListener(event -> act());
	}

	public void setTitle(String title) {
		titleLabel.setText(title.substring(0, Math.min(40, title.length()))); //trimming is necessary
		titleLabel.setForeground(new Color(74, 136, 199));
	}


	public void setBody(JPanel body) {
		bodyContainer.removeAll();
		bodyContainer.add(body, BorderLayout.CENTER);
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
		btn.setIcon(collapsed? EXPAND_ICO : COLLAPSE_ICO);
	}

	@Override
	public Dimension getPreferredSize() {
		if (bodyContainer.getComponents().length == 0) {
			return new Dimension();
		}
		return new Dimension(bodyContainer.getPreferredSize().width, root.getPreferredSize().height + BOTTOM_PADDING);
	}




}
