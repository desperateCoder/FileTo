package main.java.de.c4.view.settings;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import main.java.de.c4.controller.shared.Settings;
import main.java.de.c4.view.i18n.I18N;

public class SettingsFrame extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	JLabel usernameLabel;
	JTextField usernameField;
	JLabel lookAndFeelLabel;
	JComboBox<String> lookAndFeelBox;
	JButton saveButton;

	public SettingsFrame() {
		setTitle(I18N.get("settingsframe.settings"));
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.weightx = 0.0;
		usernameLabel = new JLabel(I18N.get("settingsframe.username"));
		gbc.gridx = 0;
		gbc.gridy = 0;
		add(usernameLabel, gbc);
		lookAndFeelLabel = new JLabel(I18N.get("settingsframe.lookandfeel"));
		gbc.gridy = 1;
		add(lookAndFeelLabel, gbc);
		usernameField = new JTextField();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		add(usernameField, gbc);
		lookAndFeelBox = new JComboBox<String>();
		lookAndFeelBox.addItem("System");
		for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			lookAndFeelBox.addItem(info.getName());
		}
		lookAndFeelBox.setSelectedIndex(0);
		gbc.gridy = 1;
		add(lookAndFeelBox, gbc);
		saveButton = new JButton(I18N.get("settingsframe.save"));
		saveButton.addActionListener(this);
		gbc.gridy = 2;
		add(saveButton, gbc);
		loadValues();
		this.pack();
		this.setVisible(true);
	}

	private void loadValues() {
		usernameField.setText(Settings.INSTANCE.get(Settings.CONTACT_NAME));
		String lookAndFeel = Settings.INSTANCE.get(Settings.LOOK_AND_FEEL);
		if (lookAndFeel == null || lookAndFeel.isEmpty()) {
			lookAndFeelBox.setSelectedIndex(0);
		} else {
			for (int i = 0; i < lookAndFeelBox.getItemCount(); i++) {
				if (lookAndFeel.equals(lookAndFeelBox.getItemAt(i))) {
					lookAndFeelBox.setSelectedIndex(i);
				}
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		Settings.INSTANCE.set(Settings.CONTACT_NAME, usernameField.getText());
		if (lookAndFeelBox.getSelectedIndex() == 0) {
			Settings.INSTANCE.set(Settings.LOOK_AND_FEEL, "");
		} else {
			Settings.INSTANCE.set(Settings.LOOK_AND_FEEL, lookAndFeelBox.getSelectedItem().toString());
		}
		Settings.INSTANCE.save();
		dispose();
	}
}
