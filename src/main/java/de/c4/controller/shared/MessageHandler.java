package main.java.de.c4.controller.shared;

import java.net.InetAddress;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;

import main.java.de.c4.controller.Messenger;
import main.java.de.c4.controller.shared.Network.ChatMessage;
import main.java.de.c4.model.messages.ContactDto;
import main.java.de.c4.model.messages.ContactListDto;
import main.java.de.c4.model.messages.EOnlineState;
import main.java.de.c4.model.messages.OnlineStateChange;
import main.java.de.c4.model.messages.RequestKnownOnlineClients;

public class MessageHandler extends Listener{

//	private Set<MessageRecievedListener> messageRecievedListener = new HashSet<MessageRecievedListener>();

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
			Messenger.receiveMessageFrom(contact, chatMessage);
			
		} else if (object instanceof OnlineStateChange) {
			InetAddress ip = c.getRemoteAddressTCP().getAddress();
			OnlineStateChange onlineState = (OnlineStateChange) object;
			Log.debug("Online-State changed:  "+onlineState.contact.name+" ("+onlineState.newState+")");
			ContactList.INSTANCE.contactStateChanged(onlineState, ip);
			if (onlineState.newState!=EOnlineState.OFFLINE) {
				ConnectionManager.registerConnection(onlineState.contact, c);
			}
		} else if (object instanceof RequestKnownOnlineClients) {
			InetAddress address = c.getRemoteAddressTCP().getAddress();
			ContactListDto contacts = ContactList.INSTANCE.getContactListForContactsRequest(address);
			c.sendTCP(contacts);
			c.close();
		} else if (object instanceof ContactListDto){
			ContactListDto list = (ContactListDto)object;
			ContactList.INSTANCE.setOnlineContacts(list.contacts);
			Log.info("Recieved ContactList!");
//			for (ContactDto dto : list.contacts) {
//				System.out.println(dto.name + " ("+dto.ip+"): "+dto.state);
//			}
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
}
