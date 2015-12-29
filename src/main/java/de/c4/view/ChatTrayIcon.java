package main.java.de.c4.view;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import com.esotericsoftware.minlog.Log;

import main.java.de.c4.model.messages.ContactList;
import main.java.de.c4.model.messages.EOnlineState;

public class ChatTrayIcon extends TrayIcon implements ActionListener{
	private static final String BTN_QUIT = "qui";
	private static final String BTN_OPEN = "ope";
	private static final String STATE_ONLINE = "on";
	private static final String STATE_OFFLINE = "off";
	private static final String STATE_AFK = "afk";
	private static final String STATE_DND = "dnd";
	private final JPopupMenu popup = new JPopupMenu();
	private JMenu stateItem;
	private final ContactListFrame frame;
	private JRadioButtonMenuItem onlineStateRadio;
	private JRadioButtonMenuItem offlineStateRadio;
	private JRadioButtonMenuItem afkStateRadio;
	private JRadioButtonMenuItem dndStateRadio;
	
	private boolean isInTray = false;

	public ChatTrayIcon(Image image, final ContactListFrame frame) {
		super(image);
		this.frame = frame;
		// setToolTipMin(Patience.INSTANCE.getRemTimeString());
		JMenuItem openItem = new JMenuItem("Öffnen");
		JMenuItem quitItem = new JMenuItem("Beenden");
		stateItem = new JMenu("Status");
		ButtonGroup group = new ButtonGroup();
		JRadioButtonMenuItem radio = new JRadioButtonMenuItem("Online");
		radio.addActionListener(this);
		radio.setActionCommand(STATE_ONLINE);
		group.add(radio);
		stateItem.add(radio);
		onlineStateRadio = radio;
		
		radio = new JRadioButtonMenuItem("AFK (Abwesend)");
		radio.addActionListener(this);
		radio.setActionCommand(STATE_AFK);
		group.add(radio);
		stateItem.add(radio);
		afkStateRadio = radio;
		
		radio = new JRadioButtonMenuItem("DND (Bitte nicht stören)");
		radio.addActionListener(this);
		radio.setActionCommand(STATE_DND);
		group.add(radio);
		stateItem.add(radio);
		dndStateRadio = radio;
		
		radio = new JRadioButtonMenuItem("Offline");
		radio.addActionListener(this);
		radio.setActionCommand(STATE_OFFLINE);
		group.add(radio);
		stateItem.add(radio);
		offlineStateRadio = radio;
		
		popup.add(openItem);
		popup.add(stateItem);
		popup.add(quitItem);
		openItem.addActionListener(this);
		openItem.setActionCommand(BTN_OPEN);
		quitItem.addActionListener(this);
		quitItem.setActionCommand(BTN_QUIT);

		addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}

			public void mouseClicked(MouseEvent e) {
				if (e.getButton()==MouseEvent.BUTTON1 && e.getClickCount() > 1){
					openFrame();
				} else if (e.getButton()==MouseEvent.BUTTON3) {
					popup.setLocation(e.getX(), e.getY());
					popup.setInvoker(popup);
					popup.setVisible(true);
				}
			}

		});
		
	}

	public void openFrame() {
		SystemTray.getSystemTray().remove(this);
		FrameUtil.bringToFront(frame);
		isInTray = false;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		popup.setVisible(false);
		String c = e.getActionCommand();
		if (BTN_QUIT.equals(c)) {
			frame.exit();
		} else if (BTN_OPEN.equals(c)) {
			openFrame();
		} else if (STATE_AFK.equals(c)) {
			frame.setOnlineState(EOnlineState.AFK);
			updatePopupLabels();
		} else if (STATE_DND.equals(c)) {
			frame.setOnlineState(EOnlineState.DND);
			updatePopupLabels();
		} else if (STATE_OFFLINE.equals(c)) {
			frame.setOnlineState(EOnlineState.OFFLINE);
			updatePopupLabels();
		} else if (STATE_ONLINE.equals(c)) {
			frame.setOnlineState(EOnlineState.ONLINE);
			updatePopupLabels();
		} 
	}

	public void addToTray() {
		updatePopupLabels();
		try {
			SystemTray.getSystemTray().add(this);
			isInTray = true;
		} catch (AWTException e) {
			Log.error("Could not add systemtray icon: "+e.getMessage());
		}
	}
	
	private void updatePopupLabels(){
		EOnlineState state = EOnlineState.getByNr(ContactList.getMe().state);
		String text = "Status: "+state.toString();
		stateItem.setText(text);
		setToolTip(text);
		switch (state) {
		case ONLINE:
			setSelectedStateRadio(onlineStateRadio);
			break;
		case AFK:
			setSelectedStateRadio(afkStateRadio);
			break;
		case DND:
			setSelectedStateRadio(dndStateRadio);
			break;
		case OFFLINE:
			setSelectedStateRadio(offlineStateRadio);
			break;
		}
	}
	private void setSelectedStateRadio(JRadioButtonMenuItem item){
		item.removeActionListener(this);
		item.setSelected(true);
		item.addActionListener(this);
	}

	public boolean isInTray() {
		return isInTray;
	}

}