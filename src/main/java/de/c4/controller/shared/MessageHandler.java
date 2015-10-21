package main.java.de.c4.controller.shared;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

import main.java.de.c4.controller.shared.Network.ChatMessage;
import main.java.de.c4.controller.shared.listener.MessageRecievedListener;
import main.java.de.c4.model.messages.ContactDto;
import main.java.de.c4.model.messages.ContactListDto;
import main.java.de.c4.model.messages.OnlineStateChange;
import main.java.de.c4.model.messages.RequestKnownOnlineClients;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;

public class MessageHandler extends Listener{

	private Set<MessageRecievedListener> messageRecievedListener = new HashSet<MessageRecievedListener>();

	@Override
	public void received(Connection c, Object object) {
		// We know all connections for this server are actually
		// ChatConnections.

		if (object instanceof ChatMessage) {
			
			// Ignore the object if a client tries to chat before it is registered.
			ChatMessage chatMessage = (ChatMessage) object;
			ContactDto contact = ContactList.INSTANCE.findByAddr(c.getRemoteAddressTCP().getAddress());
			if (contact == null)
				return;
			
			// Ignore the object if the chat message is invalid.
			String message = chatMessage.text;
			if (message == null || message.length()==0)
				return;
			Log.debug("Nachricht von "+contact.name+" ("+contact.ip+") bekommen: "+message);
			messageRecieved(contact, chatMessage);
			
		} else if (object instanceof OnlineStateChange) {
			
			InetAddress ip = c.getRemoteAddressTCP().getAddress();
			OnlineStateChange onlineState = (OnlineStateChange) object;
			ContactList.INSTANCE.contactStateChanged(onlineState, ip);
			ConnectionManager.registerConnection(onlineState.contact, c);
		} else if (object instanceof RequestKnownOnlineClients) {
			InetAddress address = c.getRemoteAddressTCP().getAddress();
			ContactListDto contacts = ContactList.INSTANCE.getContactListForContactsRequest(address);
			c.sendTCP(contacts);
		}
	}

	@Override
	public void disconnected(Connection c) {
		super.disconnected(c);
		ConnectionManager.removeConnection(c);
	}
	
	@Override
	public void connected(Connection connection) {
		super.connected(connection);
	}
	
	
	private void messageRecieved(ContactDto contact, ChatMessage chatMessage){
		for (MessageRecievedListener l : messageRecievedListener) {
			l.messageRecieved(contact, chatMessage);
		}
	}
	
	private void addMessageRecievedListener(MessageRecievedListener l) {
		messageRecievedListener.add(l);
	}

}
