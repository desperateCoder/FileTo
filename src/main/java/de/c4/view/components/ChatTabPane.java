package main.java.de.c4.view.components;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.JTabbedPane;

import main.java.de.c4.controller.shared.Network.ChatMessage;
import main.java.de.c4.model.messages.ContactDto;
import main.java.de.c4.view.ChatFrame;
import main.java.de.c4.view.listener.TabCloseListener;


public class ChatTabPane extends JTabbedPane implements ActionListener, TabCloseListener{
	
	private static final long serialVersionUID = 1L;
	
	private ChatFrame parent;
//	private List<ContactDto> contacts = new ArrayList<ContactDto>();
	
	public ChatTabPane(ChatFrame parent) {
		super(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		this.parent = parent;
	}
	
//	public void addContact(ContactDto c) {
//		contacts.add(c);
//	}
	
	@Override
	public void addTab(String title, Component component) {
		super.addTab(title, component);
		setTabComponentAt(indexOfComponent(component), new ButtonTabComponent(this, this));
	}
	
	public int indexOf(ContactDto c){
		for (int i = 0; i < getTabCount(); i++) {
			ChatPanel panel = (ChatPanel)getComponentAt(i);
			Set<ContactDto> list = panel.getContacts();
			if (list.size()==1 && list.iterator().next().equals(c)) {
				return i;
			}
		}
		return -1;
	}
	
	public int indexOf(long chatId){
		for (int i = 0; i < getTabCount(); i++) {
			ChatPanel panel = (ChatPanel)getComponentAt(i);
			if (chatId==panel.getChatId()) {
				return i;
			}
		}
		return -1;
	}
	
	public void addTab(ChatPanel component) {
		
		for (int i = 0; i < getTabCount(); i++) {
			if (component.getChatId()==((ChatPanel)getTabComponentAt(i)).getChatId()) {
				setSelectedIndex(i);
				return;
			}
		}
		addTab(null, component);
		setSelectedComponent(component);
	}
	
	
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getTitleAt(int index) {
		return ((ChatPanel)getComponentAt(index)).getTitle();
	}
	
	
	public void tabClosed(int tabIndex) {
		remove(tabIndex);
		if (getTabCount()<1) {
			parent.allTabsClosed();
		}
	}

	public void messageReceived(ContactDto contact, ChatMessage message) {
		for (int i = 0; i < getTabCount(); i++) {
			ChatPanel p = (ChatPanel) getComponentAt(i);
			if (p.getChatId()==message.id) {
				p.messageRecieved(contact, message);
				return;
			}
		}
		addTab(new ChatPanel(contact));
	}

}
