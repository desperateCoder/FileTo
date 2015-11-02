package main.java.de.c4.view.settings;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import main.java.de.c4.controller.shared.Settings;
import main.java.de.c4.view.i18n.I18N;
import main.java.de.c4.view.resources.EIcons;
import main.java.de.c4.view.resources.IconProvider;

public class SettingsFrame extends JFrame implements ActionListener, ItemListener {
	private static final long serialVersionUID = 1L;
	JLabel usernameLabel;
	JTextField usernameField;
	JLabel languageLabel;
	JTextField languageField;
	JLabel lookAndFeelLabel;
	JComboBox<String> lookAndFeelBox;
	JButton saveButton;

	public SettingsFrame() {
		setIconImage(IconProvider.getImage(EIcons.SETTINGS));
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
		languageLabel = new JLabel(I18N.get("settingsframe.language"));
		gbc.gridy++;
		add(languageLabel, gbc);
		lookAndFeelLabel = new JLabel(I18N.get("settingsframe.lookandfeel"));
		gbc.gridy++;
		add(lookAndFeelLabel, gbc);
		usernameField = new JTextField();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		add(usernameField, gbc);
		languageField = new JTextField();
		gbc.gridy++;
		add(languageField, gbc);
		lookAndFeelBox = new JComboBox<String>();
		lookAndFeelBox.addItem("System");
		for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			lookAndFeelBox.addItem(info.getName());
		}
		lookAndFeelBox.setSelectedIndex(0);
		gbc.gridy++;
		lookAndFeelBox.addItemListener(this);
		add(lookAndFeelBox, gbc);
		saveButton = new JButton(I18N.get("settingsframe.save"));
		saveButton.addActionListener(this);
		gbc.gridy++;
		add(saveButton, gbc);
		loadValues();
		this.pack();
		this.setVisible(true);
	}

	private void loadValues() {
		usernameField.setText(Settings.INSTANCE.get(Settings.CONTACT_NAME));
		languageField.setText(Settings.INSTANCE.get(Settings.LANGUAGE));
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
		Settings.INSTANCE.set(Settings.LANGUAGE, languageField.getText());
		if (lookAndFeelBox.getSelectedIndex() == 0) {
			Settings.INSTANCE.set(Settings.LOOK_AND_FEEL, "");
		} else {
			Settings.INSTANCE.set(Settings.LOOK_AND_FEEL, lookAndFeelBox.getSelectedItem().toString());
		}
		Settings.INSTANCE.save();
		dispose();
	}

	@Override
	public void itemStateChanged(ItemEvent arg0) {
		String name = arg0.getItem().toString();
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				System.out.println(info.getName());
				if (name.equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
			SwingUtilities.updateComponentTreeUI(this);
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}
}
