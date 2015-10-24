package main.java.de.c4.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import com.esotericsoftware.minlog.Log;

import main.java.de.c4.controller.Messenger;
import main.java.de.c4.controller.shared.ContactList;
import main.java.de.c4.model.messages.ContactDto;
import main.java.de.c4.view.components.ChatPanel;
import main.java.de.c4.view.components.ChatTabPane;


public class ChatFrame extends JFrame implements ActionListener{
	
	private static final long serialVersionUID = 1L;
	
	private ChatTabPane tabbedPane = new ChatTabPane();

	
	public ChatFrame() {
		
		
		Set<ContactDto> contacts = ContactList.INSTANCE.getContacts();
		
		for (ContactDto contactDto : contacts) {
			tabbedPane.addTab(new ChatPanel(contactDto));
		}
		
		
		setContentPane(tabbedPane);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("Chat");
		pack();
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


}
