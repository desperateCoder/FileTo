package main.java.de.c4.controller.shared;

import java.net.InetAddress;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;

import main.java.de.c4.controller.FileTransferManager;
import main.java.de.c4.controller.Messenger;
import main.java.de.c4.model.messages.Alert;
import main.java.de.c4.model.messages.ChatMessage;
import main.java.de.c4.model.messages.ContactDto;
import main.java.de.c4.model.messages.ContactList;
import main.java.de.c4.model.messages.ContactListDto;
import main.java.de.c4.model.messages.EOnlineState;
import main.java.de.c4.model.messages.OnlineStateChange;
import main.java.de.c4.model.messages.PubKey;
import main.java.de.c4.model.messages.RequestKnownOnlineClients;
import main.java.de.c4.model.messages.SecondClientStarted;
import main.java.de.c4.model.messages.file.FileTransferAnswer;
import main.java.de.c4.model.messages.file.FileTransferRequest;

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
			
		} else if (object instanceof PubKey) {
			PubKey pubKey = (PubKey) object;
			try {
				Diffie.finalize(c, pubKey.key);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (object instanceof FileTransferRequest) {
			FileTransferRequest request = (FileTransferRequest)object;
			ContactDto contact = ContactList.INSTANCE.findByAddr(c.getRemoteAddressTCP().getAddress());
			Messenger.fileTransferRequestReceived(request, contact);
		} else if (object instanceof FileTransferAnswer) {
			FileTransferAnswer answer = (FileTransferAnswer)object;
//			ContactDto contact = ContactList.INSTANCE.findByAddr(c.getRemoteAddressTCP().getAddress());
			FileTransferManager.INSTANCE.requestAnswered(answer);
		} else if (object instanceof OnlineStateChange) {
			InetAddress ip = c.getRemoteAddressTCP().getAddress();
			OnlineStateChange onlineState = (OnlineStateChange) object;
			Log.debug("Online-State changed:  "+onlineState.contact.name+" ("+onlineState.newState+")");
			ContactList.INSTANCE.contactStateChanged(onlineState, ip);
			if (onlineState.newState!=EOnlineState.OFFLINE.getNr()) {
				ConnectionManager.registerConnection(onlineState.contact, c);
			}
		} else if (object instanceof Alert) {
			ContactDto contact = ContactList.INSTANCE.findByAddr(c.getRemoteAddressTCP().getAddress());
			Messenger.receiveAlertFrom(contact);
		} else if (object instanceof RequestKnownOnlineClients) {
			InetAddress address = c.getRemoteAddressTCP().getAddress();
			ContactListDto contacts = ContactList.INSTANCE.getContactListForContactsRequest(address);
			c.sendTCP(contacts);
		} else if (object instanceof ContactListDto){
			ContactListDto list = (ContactListDto)object;
			ContactList.INSTANCE.setOnlineContacts(list.contacts);
			c.close();
			Log.info("Recieved ContactList!");
		} else if (object instanceof SecondClientStarted){
			Messenger.secondClientStarted();
		}
	}

	@Override
	public void disconnected(Connection c) {
		super.disconnected(c);
		ConnectionManager.removeConnection(c);
	}
	
	
}
