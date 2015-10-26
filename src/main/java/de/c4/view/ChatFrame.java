package main.java.de.c4.view;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import main.java.de.c4.controller.Messenger;
import main.java.de.c4.controller.shared.Network.ChatMessage;
import main.java.de.c4.controller.shared.listener.MessageRecievedListener;
import main.java.de.c4.model.messages.ContactDto;
import main.java.de.c4.view.components.ChatPanel;
import main.java.de.c4.view.components.ChatTabPane;

import com.esotericsoftware.minlog.Log;


public class ChatFrame extends JFrame implements ActionListener, MessageRecievedListener{
	
	private static final long serialVersionUID = 1L;
	
	private ChatTabPane tabbedPane = new ChatTabPane(this);

	
	public ChatFrame() {

		Messenger.addMessageReceivedListener(this);
		
//		List<ContactDto> contacts = ContactList.INSTANCE.getContacts();
//		
//		for (ContactDto contactDto : contacts) {
//			tabbedPane.addTab(new ChatPanel(contactDto));
//		}
		
		
		setContentPane(tabbedPane);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("Chat");
//		setVisible(true);
	}
	
	
	public void actionPerformed(ActionEvent e) {
	}
	
	
	public static void main(String[] args) {
		Messenger.init();
		Log.set(Log.LEVEL_DEBUG);
		try {
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		} catch (Exception e) {
		    Log.info("Nimbus LaF not found, proceeding with the default one!");
		}
		new ChatFrame().setVisible(true);
	}


	public void allTabsClosed() {
		this.setVisible(false);
	}


	public void showContactTab(ContactDto c) {
		
		int indexOf = tabbedPane.indexOf(c);
		if (indexOf>=0) {
			tabbedPane.setSelectedIndex(indexOf);
		} else tabbedPane.addTab(new ChatPanel(c));
		
		if (getSize().getHeight()<10) {
			setSize(400, 500);
		}
		bringToFront();
	}


	private void bringToFront() {
		EventQueue.invokeLater(new Runnable() {
		    public void run() {
		    	setVisible(true);
		        toFront();
		        repaint();
		    }
		});
	}


	public void messageRecieved(ContactDto contact, ChatMessage message) {
		//TODO: Make window blink or something to get Attention
		tabbedPane.messageReceived(contact, message);
		if (!isVisible()) {
			bringToFront();
		}
	}


}
