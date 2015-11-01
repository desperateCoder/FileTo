package main.java.de.c4.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import com.esotericsoftware.minlog.Log;

import main.java.de.c4.controller.Messenger;
import main.java.de.c4.controller.shared.ContactList;
import main.java.de.c4.controller.shared.Settings;
import main.java.de.c4.controller.shared.listener.ContactListReceivedListener;
import main.java.de.c4.controller.shared.listener.OnlineStateChangeListener;
import main.java.de.c4.model.messages.ContactDto;
import main.java.de.c4.model.messages.EOnlineState;
import main.java.de.c4.model.messages.OnlineStateChange;
import main.java.de.c4.view.resources.IconProvider;

public class ContactListFrame extends JFrame
		implements ActionListener, OnlineStateChangeListener, ContactListReceivedListener, ItemListener {

	private static final long serialVersionUID = 1L;

	private JList<ContactDto> contactList = new JList<ContactDto>(new DefaultListModel<ContactDto>());

	private ChatFrame chatFrame = new ChatFrame();

	public ContactListFrame() {
		ContactList.INSTANCE.addReceivedContactListListener(this);
		ContactList.INSTANCE.addOnlineStateChangeListener(this);

		JPanel content = new JPanel(new BorderLayout());

		JPanel labelPanel = new JPanel(new BorderLayout());
		labelPanel.add(new JLabel("Kontakte:"), BorderLayout.CENTER);
		content.add(labelPanel, BorderLayout.NORTH);

		content.add(new JScrollPane(contactList), BorderLayout.CENTER);
		contactList.setCellRenderer(new ListCellRenderer<ContactDto>() {
			private static final int SIZE = 40;
			private final Font FONT = new Font("SansSerif", Font.BOLD, 16);
			private final Font IP_FONT = new Font("SansSerif", Font.PLAIN, 14);

			public Component getListCellRendererComponent(JList<? extends ContactDto> list, ContactDto value, int index,
					boolean isSelected, boolean cellHasFocus) {
				JPanel comp = new JPanel(new BorderLayout());
				JLabel l = new JLabel(value.name);
				l.setFont(FONT);
				JPanel text = new JPanel(new BorderLayout());
				text.add(l, BorderLayout.NORTH);
				l = new JLabel(value.ip);
				l.setFont(IP_FONT);
				text.add(l, BorderLayout.SOUTH);

				comp.add(text, BorderLayout.CENTER);

				JLabel image = new JLabel(
						new ImageIcon(IconProvider.getImage(value.state.getIcon()).getScaledInstance(SIZE, SIZE, 0)),
						0);
				comp.add(image, BorderLayout.WEST);
				return comp;
			}
		});
		contactList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				@SuppressWarnings("unchecked")
				JList<ContactDto> list = (JList<ContactDto>) evt.getSource();
				if (evt.getClickCount() == 2) {
					int index = list.locationToIndex(evt.getPoint());
					ContactDto c = list.getModel().getElementAt(index);
					chatFrame.showContactTab(c);
				}
			}
		});

		JComboBox<EOnlineState> stateCombo = new JComboBox<EOnlineState>(EOnlineState.values());
		stateCombo.setRenderer(new ListCellRenderer<EOnlineState>() {
			private static final int SIZE = 26;
			private final Font FONT = new Font("SansSerif", Font.BOLD, 18);

			public Component getListCellRendererComponent(JList<? extends EOnlineState> list, EOnlineState value,
					int index, boolean isSelected, boolean cellHasFocus) {
				JPanel comp = new JPanel(new BorderLayout());
				JLabel l = new JLabel(value.toString());
				l.setFont(FONT);
				comp.add(l, BorderLayout.CENTER);

				JLabel image = new JLabel(
						new ImageIcon(IconProvider.getImage(value.getIcon()).getScaledInstance(SIZE, SIZE, 0)), 0);
				comp.add(image, BorderLayout.WEST);
				return comp;
			}
		});
		stateCombo.setSelectedItem(ContactList.getMe().state);
		stateCombo.addItemListener(this);

		content.add(stateCombo, BorderLayout.SOUTH);
		final JFrame me = this;
		setContentPane(content);

		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				String[] options = new String[] { "Abbrechen", "Minimieren", "Beenden" };
				int response = JOptionPane.showOptionDialog(me, "Soll die Anwendung beendet oder minimiert werden?",
						"Wirklich beenden?", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
						options[0]);
				switch (response) {
				case 0:
					return;
				case 1:
					// TODO: minimize to tray
					break;
				case 2:
					ContactList.INSTANCE.setOnlineState(EOnlineState.OFFLINE, false);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					System.exit(0);
					break;
				}
			}
		});
		setTitle("Kontaktliste");
		pack();
		setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {

	}

	public static void main(String[] args) {
		// new JLabel(new
		// ImageIcon(IconProvider.getImage(EOnlineState.OFFLINE.getIcon())), 0);
		System.setProperty("java.net.preferIPv4Stack", "true");
		new Thread(new Runnable() {

			public void run() {
				Messenger.init();
			}
		}).start();
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				String lookAndFeel = Settings.INSTANCE.get(Settings.LOOK_AND_FEEL);
				if (lookAndFeel != null) {
					if (lookAndFeel.equals(info.getName())) {
						UIManager.setLookAndFeel(info.getClassName());
						break;
					}
				} else {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				}
			}
		} catch (Exception e) {
			Log.error("Error settings Look and Feel.");
		}
		new ContactListFrame();
	}

	public void receivedContactList(ContactDto[] list) {
		for (ContactDto c : list) {
			((DefaultListModel<ContactDto>) (contactList.getModel())).addElement(c);
		}
		Messenger.goOnline();
	}

	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			EOnlineState state = (EOnlineState) e.getItem();
			ContactList.INSTANCE.setOnlineState(state);
			if (state == EOnlineState.OFFLINE) {
				((DefaultListModel<ContactDto>) (contactList.getModel())).clear();
			}
		}
	}

	public void onlineStateChanged(OnlineStateChange change) {
		((DefaultListModel<ContactDto>) (contactList.getModel())).clear();
		ArrayList<ContactDto> list = ContactList.INSTANCE.getContacts();
		for (ContactDto c : list) {
			((DefaultListModel<ContactDto>) (contactList.getModel())).addElement(c);
		}
	}
}
