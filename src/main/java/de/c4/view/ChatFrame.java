package main.java.de.c4.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import main.java.de.c4.controller.Messenger;
import main.java.de.c4.controller.shared.ContactList;
import main.java.de.c4.model.messages.ContactDto;


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
		try {
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		} catch (Exception e) {
		    // If Nimbus is not available, you can set the GUI to another look and feel.
		}
		new ChatFrame().setVisible(true);
	}


}
