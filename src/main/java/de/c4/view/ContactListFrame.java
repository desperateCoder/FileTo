package main.java.de.c4.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
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

import javax.swing.BorderFactory;
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

import com.esotericsoftware.minlog.Log;

public class ContactListFrame extends JFrame implements ActionListener, OnlineStateChangeListener, ContactListReceivedListener,
        ItemListener, MessageRecievedListener {

    private static final long serialVersionUID = 1L;

    private JList<ContactDto> contactList;

    private ChatFrame chatFrame = new ChatFrame();
    private ChatTrayIcon trayIcon;
    private JMenuBar menuBar;
    private JComboBox<EOnlineState> stateComboBox;

    public ContactListFrame() {
        ContactList.INSTANCE.addReceivedContactListListener(this);
        ContactList.INSTANCE.addOnlineStateChangeListener(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Messenger.addMessageReceivedListener(ContactListFrame.this);
            }
        }).start();

        JPanel contentPane = new JPanel(new BorderLayout());

        this.initMenuBar();
        contentPane.add(menuBar, BorderLayout.NORTH);

        this.initContactList();
        contentPane.add(new JScrollPane(contactList), BorderLayout.CENTER);

        this.initStateComboBox();
        contentPane.add(stateComboBox, BorderLayout.SOUTH);

        this.setContentPane(contentPane);

        final JFrame me = this;
        trayIcon = new ChatTrayIcon(IconProvider.getImage(EIcons.TRAY_ICON), this);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (SystemTray.isSupported()) {
                    trayIcon.addToTray();
                    setVisible(false);
                } else {
                    String[] options = new String[] { I18N.get("contactlist.cancel"), I18N.get("contactlist.exit") };
                    int response = JOptionPane.showOptionDialog(me, I18N.get("contactlist.exitonclose"),
                            I18N.get("contactlist.exitonclose.title"), JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                            null, options, options[0]);
                    if (response == 1) {
                        exit();
                    }
                }
            }
        });

        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.setTitle(I18N.get("contactlist.title"));
        this.setIconImage(IconProvider.getImage(EIcons.APP_ICON));
        this.pack();
        this.setLocation(100, 100);
        this.setMinimumSize(new Dimension(300, 600));
        this.setVisible(true);
    }

    /** Initializes the Menu Bar with File, Edit and Filetransfers */
    private void initMenuBar() {
        final int icon_size = 16;
        menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(I18N.get("contactlist.menu.file"));
        JMenuItem quitMenuItem = new JMenuItem(I18N.get("contactlist.menu.file.quit"));
        quitMenuItem.setIcon(new ImageIcon(IconProvider.getImage(EIcons.ABORT).getScaledInstance(icon_size, icon_size,
                Image.SCALE_SMOOTH)));
        quitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        });
        fileMenu.add(quitMenuItem);
        menuBar.add(fileMenu);
        JMenu editMenu = new JMenu(I18N.get("contactlist.menu.edit"));
        JMenuItem settingsMenuItem = new JMenuItem(I18N.get("contactlist.menu.settings"));
        settingsMenuItem.setIcon(new ImageIcon(IconProvider.getImage(EIcons.SETTINGS).getScaledInstance(icon_size, icon_size,
                Image.SCALE_SMOOTH)));
        settingsMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new SettingsFrame();
            }
        });
        editMenu.add(settingsMenuItem);
        menuBar.add(editMenu);

        JMenu windowMenu = new JMenu(I18N.get("contactlist.menu.window"));
        JMenuItem ftMenuItem = new JMenuItem(I18N.get("contactlist.menu.fileTransferFrame"));
        ftMenuItem.setIcon(new ImageIcon(IconProvider.getImage(EIcons.TRANSFER).getScaledInstance(icon_size, icon_size,
                Image.SCALE_SMOOTH)));
        ftMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                FileTransferFrame.INSTANCE.setVisible(true);
            }
        });
        windowMenu.add(ftMenuItem);
        menuBar.add(windowMenu);
    }

    /** Initializes the State ComboBox */
    private void initStateComboBox() {
        stateComboBox = new JComboBox<EOnlineState>(EOnlineState.values());
        stateComboBox.setRenderer(new ListCellRenderer<EOnlineState>() {
            private static final int state_icon_size = 20;
            private final Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 14);

            public Component getListCellRendererComponent(JList<? extends EOnlineState> list, EOnlineState value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JPanel comp = new JPanel(new BorderLayout());
                comp.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

                // State Name
                JLabel stateNameLabel = new JLabel(value.toString());
                stateNameLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
                stateNameLabel.setFont(font);
                comp.add(stateNameLabel, BorderLayout.CENTER);

                // State Icon
                JLabel image = new JLabel(new ImageIcon(IconProvider.getImage(value.getIcon()).getScaledInstance(state_icon_size,
                        state_icon_size, 0)), 0);
                comp.add(image, BorderLayout.WEST);
                return comp;
            }
        });
        stateComboBox.setSelectedItem(EOnlineState.getByNr(ContactList.getMe().state));
        stateComboBox.addItemListener(this);
    }

    /** Initializes the Contact List */
    private void initContactList() {
        contactList = new JList<ContactDto>(new DefaultListModel<ContactDto>());
        contactList.setCellRenderer(new ListCellRenderer<ContactDto>() {
            private static final int size = 40;
            private final Font font = new Font(Font.SANS_SERIF, Font.BOLD, 16);
            private final Font ip_font = new Font(Font.SANS_SERIF, Font.PLAIN, 14);

            public Component getListCellRendererComponent(JList<? extends ContactDto> list, ContactDto value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JPanel comp = new JPanel(new BorderLayout());
                JPanel text = new JPanel(new BorderLayout());

                // Contact Name
                JLabel label = new JLabel(value.name);
                label.setFont(font);
                text.add(label, BorderLayout.NORTH);

                // Contact IP-Address
                label = new JLabel(value.ip);
                label.setFont(ip_font);
                text.add(label, BorderLayout.SOUTH);

                comp.add(text, BorderLayout.CENTER);

                // Contact State Icon
                JLabel image = new JLabel(new ImageIcon(IconProvider.getImage(EOnlineState.getByNr(value.state).getIcon())
                        .getScaledInstance(size, size, 0)), 0);
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
    }

    public void exit() {
        setVisible(false);
        chatFrame.setVisible(false);
        FileTransferFrame.INSTANCE.setVisible(false);
        long millisTimeout = ContactList.INSTANCE.getContacts().size() * 100L;
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
                for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    System.out.println(info.getName());
                    if (lookAndFeel.equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } else {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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
            setOnlineState(state);
        }
    }

    public void setOnlineState(EOnlineState state) {
        ContactList.INSTANCE.setOnlineState(state);
        if (state == EOnlineState.OFFLINE) {
            ((DefaultListModel<ContactDto>) (contactList.getModel())).clear();
        }
        if ((EOnlineState) (stateComboBox.getSelectedItem()) != state) {
            stateComboBox.setSelectedItem(state);
        }
    }

    public void onlineStateChanged(OnlineStateChange change) {
        ((DefaultListModel<ContactDto>) (contactList.getModel())).clear();
        ArrayList<ContactDto> list = ContactList.INSTANCE.getContacts();
        for (ContactDto c : list) {
            ((DefaultListModel<ContactDto>) (contactList.getModel())).addElement(c);
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
