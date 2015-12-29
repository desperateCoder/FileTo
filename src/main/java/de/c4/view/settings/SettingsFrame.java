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

/**
 * Allows users to change their preferences.
 * 
 * @author stnieder
 *
 */
public class SettingsFrame extends JFrame implements ItemListener {
    private static final long serialVersionUID = 1L;
    private JLabel usernameLabel;
    private JTextField usernameField;
    private JLabel languageLabel;
    private JComboBox<String> languageBox;
    private JLabel lookAndFeelLabel;
    private JComboBox<String> lookAndFeelBox;
    private JButton saveButton;
    private JButton cancelButton;

    public SettingsFrame() {
        this.setIconImage(IconProvider.getImage(EIcons.SETTINGS));
        this.setTitle(I18N.get("settingsframe.settings"));
        this.initUsername();
        this.initLanguage();
        this.initLookAndFeel();
        this.initActionButtons();
        this.buildSurface();
        this.setExistingValues();
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    /** Aggregates all Components to one surface */
    private void buildSurface() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 0.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        this.add(usernameLabel, gbc);
        gbc.gridy++;
        this.add(languageLabel, gbc);
        gbc.gridy++;
        this.add(lookAndFeelLabel, gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth++;
        this.add(usernameField, gbc);
        gbc.gridy++;
        this.add(languageBox, gbc);
        gbc.gridy++;
        this.add(lookAndFeelBox, gbc);
        gbc.gridwidth--;
        gbc.gridy++;
        this.add(cancelButton, gbc);
        gbc.gridx++;
        this.add(saveButton, gbc);
    }

    /** Initializes Look and Feel Label and Combobox. */
    private void initLookAndFeel() {
        lookAndFeelLabel = new JLabel(I18N.get("settingsframe.lookandfeel"));
        lookAndFeelBox = new JComboBox<String>();
        lookAndFeelBox.addItemListener(this);
        lookAndFeelBox.addItem("System");
        for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            lookAndFeelBox.addItem(info.getName());
        }
        lookAndFeelBox.setSelectedIndex(0);
    }

    /** Initializes Language Label and Combobox. */
    private void initLanguage() {
        languageLabel = new JLabel(I18N.get("settingsframe.language"));
        languageBox = new JComboBox<String>(I18N.getAvailableTranslations());
    }

    /** Initializes Username Label and Textfield. */
    private void initUsername() {
        usernameLabel = new JLabel(I18N.get("settingsframe.username"));
        usernameField = new JTextField();
    }

    /** Initializes Cancel and Save Button. */
    private void initActionButtons() {
        cancelButton = new JButton(I18N.get("settingsframe.cancel"));
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        saveButton = new JButton(I18N.get("settingsframe.save"));
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Settings.INSTANCE.set(Settings.CONTACT_NAME, usernameField.getText());
                Settings.INSTANCE.set(Settings.LANGUAGE, (String) languageBox.getSelectedItem());
                if (lookAndFeelBox.getSelectedIndex() == 0) {
                    Settings.INSTANCE.set(Settings.LOOK_AND_FEEL, "");
                } else {
                    Settings.INSTANCE.set(Settings.LOOK_AND_FEEL, lookAndFeelBox.getSelectedItem().toString());
                }
                Settings.INSTANCE.save();
            }
        });
    }

    /** Loads existing Settings as initial values */
    private void setExistingValues() {
        usernameField.setText(Settings.INSTANCE.get(Settings.CONTACT_NAME));
        String language = Settings.INSTANCE.get(Settings.LANGUAGE);
        if (language == null || language.isEmpty()) {
            languageBox.setSelectedIndex(0);
        } else {
            for (int i = 0; i < languageBox.getItemCount(); i++) {
                if (language.equals(languageBox.getItemAt(i))) {
                    languageBox.setSelectedIndex(i);
                    break;
                }
            }
        }
        String lookAndFeel = Settings.INSTANCE.get(Settings.LOOK_AND_FEEL);
        if (lookAndFeel == null || lookAndFeel.isEmpty()) {
            lookAndFeelBox.setSelectedIndex(0);
        } else {
            for (int i = 0; i < lookAndFeelBox.getItemCount(); i++) {
                if (lookAndFeel.equals(lookAndFeelBox.getItemAt(i))) {
                    lookAndFeelBox.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    /** Provides an instant Look and Feel Change as preview */
    @Override
    public void itemStateChanged(ItemEvent event) {
        String name = event.getItem().toString();
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                System.out.println(info.getName());
                if (name.equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            SwingUtilities.updateComponentTreeUI(this);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }
}
