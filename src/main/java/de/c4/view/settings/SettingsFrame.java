package main.java.de.c4.view.settings;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.jar.JarInputStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import main.java.de.c4.controller.shared.Settings;

public class SettingsFrame extends JFrame implements ActionListener {
	JLabel usernameLabel;
	JTextField usernameField;
	JLabel lookAndFeelLabel;
	JButton saveButton;
	
	public SettingsFrame() {
		setTitle("Settings"); //TODO i18n
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5, 5, 5, 5);
		usernameLabel = new JLabel("Username"); //TODO i18n
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.0;
		add(usernameLabel, gbc);
		usernameField = new JTextField();
		gbc.gridx = 1;
		gbc.weightx = 1.0;
		add(usernameField, gbc);
		lookAndFeelLabel = new JLabel("Look & Feel"); //TODO i18n
		gbc.gridx = 0;
		gbc.gridy = 1;
		add(lookAndFeelLabel, gbc);
		saveButton = new JButton("Save"); //TODO i18n
		saveButton.addActionListener(this);
		gbc.gridx = 1;
		gbc.gridy = 2;
		add(saveButton, gbc);
		loadValues();
		this.pack();
		this.setVisible(true);
	}
	
	private void loadValues() {
		usernameField.setText(Settings.INSTANCE.get(Settings.CONTACT_NAME));
	}

	public void actionPerformed(ActionEvent e) {
		Settings.INSTANCE.set(Settings.CONTACT_NAME, usernameField.getText());
		Settings.INSTANCE.save();
		dispose();
	}
}
