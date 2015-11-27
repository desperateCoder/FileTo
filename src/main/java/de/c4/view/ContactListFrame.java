package main.java.de.c4.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.SystemTray;
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
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import com.esotericsoftware.minlog.Log;

import main.java.de.c4.controller.Messenger;
import main.java.de.c4.controller.shared.Settings;
import main.java.de.c4.controller.shared.listener.ContactListReceivedListener;
import main.java.de.c4.controller.shared.listener.MessageRecievedListener;
import main.java.de.c4.controller.shared.listener.OnlineStateChangeListener;
import main.java.de.c4.model.messages.ChatMessage;
import main.java.de.c4.model.messages.ContactDto;
import main.java.de.c4.model.messages.ContactList;
import main.java.de.c4.model.messages.EOnlineState;
import main.java.de.c4.model.messages.OnlineStateChange;
import main.java.de.c4.model.messages.file.FileTransferRequest;
import main.java.de.c4.view.i18n.I18N;
import main.java.de.c4.view.resources.EIcons;
import main.java.de.c4.view.resources.IconProvider;
import main.java.de.c4.view.settings.SettingsFrame;

public class ContactListFrame extends JFrame implements ActionListener,
		OnlineStateChangeListener, ContactListReceivedListener, ItemListener, MessageRecievedListener {

	private static final long serialVersionUID = 1L;

	private JList<ContactDto> contactList = new JList<ContactDto>(
			new DefaultListModel<ContactDto>());

	private ChatFrame chatFrame = new ChatFrame();
	private ChatTrayIcon trayIcon;
	private JComboBox<EOnlineState> stateCombo;

	public ContactListFrame() {
		ContactList.INSTANCE.addReceivedContactListListener(this);
		ContactList.INSTANCE.addOnlineStateChangeListener(this);
		new Thread(new Runnable() {
			@Override
			public void run() {
				Messenger.addMessageReceivedListener(ContactListFrame.this);
			}
		}).start();

		setIconImage(IconProvider.getImage(EIcons.APP_ICON));
		trayIcon = new ChatTrayIcon(IconProvider.getImage(EIcons.TRAY_ICON),
				this);
		JPanel content = new JPanel(new BorderLayout());

		JPanel labelPanel = new JPanel(new BorderLayout());
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu(I18N.get("contactlist.menu.file"));
		JMenuItem quitMenuItem = new JMenuItem(
				I18N.get("contactlist.menu.file.quit"));
		quitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exit();
			}
		});
		fileMenu.add(quitMenuItem);
		menuBar.add(fileMenu);
		JMenu editMenu = new JMenu(I18N.get("contactlist.menu.edit"));
		JMenuItem settingsMenuItem = new JMenuItem(
				I18N.get("contactlist.menu.settings"));
		settingsMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new SettingsFrame();
			}
		});
		editMenu.add(settingsMenuItem);
		menuBar.add(editMenu);
		
		JMenu windowMenu = new JMenu(I18N.get("contactlist.menu.window"));
		JMenuItem ftMenuItem = new JMenuItem(
				I18N.get("contactlist.menu.fileTransferFrame"));
		ftMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileTransferFrame.INSTANCE.setVisible(true);
			}
		});
		windowMenu.add(ftMenuItem);
		menuBar.add(windowMenu);

		labelPanel.add(menuBar, BorderLayout.CENTER);
		content.add(labelPanel, BorderLayout.NORTH);

		content.add(new JScrollPane(contactList), BorderLayout.CENTER);
		contactList.setCellRenderer(new ListCellRenderer<ContactDto>() {
			private static final int SIZE = 40;
			private final Font FONT = new Font("SansSerif", Font.BOLD, 16);
			private final Font IP_FONT = new Font("SansSerif", Font.PLAIN, 14);

			public Component getListCellRendererComponent(
					JList<? extends ContactDto> list, ContactDto value,
					int index, boolean isSelected, boolean cellHasFocus) {
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
						new ImageIcon(IconProvider.getImage(
								EOnlineState.getByNr(value.state).getIcon()).getScaledInstance(SIZE,
								SIZE, 0)), 0);
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

		stateCombo = new JComboBox<EOnlineState>(EOnlineState.values());
		stateCombo.setRenderer(new ListCellRenderer<EOnlineState>() {
			private static final int SIZE = 26;
			private final Font FONT = new Font("SansSerif", Font.BOLD, 18);

			public Component getListCellRendererComponent(
					JList<? extends EOnlineState> list, EOnlineState value,
					int index, boolean isSelected, boolean cellHasFocus) {
				JPanel comp = new JPanel(new BorderLayout());
				JLabel l = new JLabel(value.toString());
				l.setFont(FONT);
				comp.add(l, BorderLayout.CENTER);

				JLabel image = new JLabel(new ImageIcon(IconProvider.getImage(
						value.getIcon()).getScaledInstance(SIZE, SIZE, 0)), 0);
				comp.add(image, BorderLayout.WEST);
				return comp;
			}
		});
		stateCombo.setSelectedItem(EOnlineState.getByNr(ContactList.getMe().state));
		stateCombo.addItemListener(this);

		content.add(stateCombo, BorderLayout.SOUTH);
		final JFrame me = this;
		setContentPane(content);

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				if (SystemTray.isSupported()) {
					trayIcon.addToTray();
					setVisible(false);
				} else {
					String[] options = new String[] {
							I18N.get("contactlist.cancel"),
							I18N.get("contactlist.exit") };
					// TODO i18n
					int response = JOptionPane.showOptionDialog(me,
							"Soll die Anwendung beendet werden?",
							"Wirklich beenden?", JOptionPane.DEFAULT_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options,
							options[0]);
					if (response == 1) {
						exit();
					}
				}
			}

		});
		setTitle(I18N.get("contactlist.title"));
		pack();
		setVisible(true);
	}

	public void exit() {
		setVisible(false);
		chatFrame.setVisible(false);
		FileTransferFrame.INSTANCE.setVisible(false);
		long millisTimeout = ContactList.INSTANCE.getContacts().size()*100L;
		ContactList.INSTANCE.setOnlineState(EOnlineState.OFFLINE, false);
		try {
			Thread.sleep(millisTimeout);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		System.exit(0);
	}

	public void actionPerformed(ActionEvent e) {

	}

	public static void main(String[] args) {
		Log.set(Log.LEVEL_DEBUG); // TODO: change before release!
		System.setProperty("java.net.preferIPv4Stack", "true");
		
		try {
			String lookAndFeel = Settings.INSTANCE.get(Settings.LOOK_AND_FEEL);
			if (lookAndFeel != null && !lookAndFeel.isEmpty()) {
				for (LookAndFeelInfo info : UIManager
						.getInstalledLookAndFeels()) {
					System.out.println(info.getName());
					if (lookAndFeel.equals(info.getName())) {
						UIManager.setLookAndFeel(info.getClassName());
						break;
					}
				}
			} else {
				UIManager.setLookAndFeel(UIManager
						.getSystemLookAndFeelClassName());
			}
		} catch (Exception e) {
			Log.error("Error settings Look and Feel.");
		}
		new ContactListFrame();
	}

	public void receivedContactList(ContactDto[] list) {
		for (ContactDto c : list) {
			((DefaultListModel<ContactDto>) (contactList.getModel()))
					.addElement(c);
		}
		Messenger.goOnline();
	}

	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			EOnlineState state = (EOnlineState) e.getItem();
			setOnlineState(state);
		}
	}

	public void setOnlineState(EOnlineState state) {
		ContactList.INSTANCE.setOnlineState(state);
		if (state == EOnlineState.OFFLINE) {
			((DefaultListModel<ContactDto>) (contactList.getModel())).clear();
		}
		if ((EOnlineState) (stateCombo.getSelectedItem()) != state) {
			stateCombo.setSelectedItem(state);
		}
	}

	public void onlineStateChanged(OnlineStateChange change) {
		((DefaultListModel<ContactDto>) (contactList.getModel())).clear();
		ArrayList<ContactDto> list = ContactList.INSTANCE.getContacts();
		for (ContactDto c : list) {
			((DefaultListModel<ContactDto>) (contactList.getModel()))
					.addElement(c);
		}
	}

	@Override
	public void messageRecieved(ContactDto contact, ChatMessage message) {
		// TODO maybe show icon for recieved message
	}

	@Override
	public void fileTransferRequestRecieved(ContactDto contact, FileTransferRequest request) {
		// ignore
	}

	@Override
	public void alert(ContactDto contact) {
		// nothing
	}

	@Override
	public void secondClientStarted() {
		if (trayIcon.isInTray()) {
			trayIcon.openFrame();
		} 
		FrameUtil.bringToFront(this);
		FrameUtil.shake(this);
	}
}
